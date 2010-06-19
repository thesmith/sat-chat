package science.hack.dispatcher;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import science.hack.controller.ChatController;

@SuppressWarnings("serial")
public class ChatDispatcher extends HttpServlet {
    private final ChatController controller = new ChatController();
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        controller.processChat(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        controller.processChat(request, response);
    }
}
