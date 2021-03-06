package science.hack.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import science.hack.service.ConversationService;

import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

@Controller
public class ChatController {
    
    private static final Pattern JID = Pattern.compile("^(.*)\\/?.*$");

    private final ConversationService conversationService;

    @Autowired
    public ChatController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @RequestMapping(value = "/*")
    public void processChat(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            XMPPService xmpp = XMPPServiceFactory.getXMPPService();
            Message msg = xmpp.parseMessage(request);
            JID fromJid = msg.getFromJid();
            String body = msg.getBody();

            String userId = fromJid.getId();
            Matcher matcher = JID.matcher(fromJid.getId());
            if (matcher.matches()) {
                userId = matcher.group(1);
            }
            
            String reply = conversationService.chat(userId, body.toLowerCase().replace("'", ""));
            Message replyMessage = new MessageBuilder().withRecipientJids(fromJid).withBody(reply).build();

            boolean messageSent = false;
            if (xmpp.getPresence(fromJid).isAvailable()) {
                SendResponse status = xmpp.sendMessage(replyMessage);
                messageSent = (status.getStatusMap().get(fromJid) == SendResponse.Status.SUCCESS);
            }

            response.setStatus(messageSent ? HttpServletResponse.SC_NO_CONTENT
                            : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            String message = request.getParameter("message");

            String reply = conversationService.chat("ben.thesmith@gmail.com", message.toLowerCase());
            response.setStatus(HttpServletResponse.SC_OK);

            response.setContentType("text/html");
            PrintWriter out = response.getWriter();

            out.println(reply);

            out.close();
        }
    }
}
