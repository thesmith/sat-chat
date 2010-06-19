package science.hack;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.appengine.repackaged.org.joda.time.DateTime;
import com.google.appengine.repackaged.org.joda.time.DateTimeZone;
import com.google.appengine.repackaged.org.joda.time.Interval;
import com.google.appengine.repackaged.org.joda.time.Period;

import science.hack.model.Location;

public class ConversationService {

    private static final Pattern HELLO = Pattern.compile("^hello\\s*(\\w+).*$");
    private static final Pattern WHEN = Pattern.compile("^where\\s*will\\s*you\\s*be\\s*at\\s*(.+)\\??$");
    private static final Pattern WHERE = Pattern.compile("^.*in\\s*(.+)\\??$");
    private static final Pattern JID = Pattern.compile("^(\\w+).*");

    private final UserService userService;
    private final LocationService locationService;

    public ConversationService() {
        this(new UserService(), new LocationService());
    }

    public ConversationService(UserService userService, LocationService locationService) {
        this.userService = userService;
        this.locationService = locationService;
    }

    public String chat(String jid, String chat) {
        if (chat.contains("hello")) {
            return sayHello(jid, chat);
        } else if (chat.contains("where")) {
            return sayWhere(jid, chat);
        } else if (chat.contains("im in") || chat.contains("when")) {
            return sayWhen(jid, chat);
        } else {
            return sayBye();
        }
    }

    private String sayHello(String jid, String chat) {
        Matcher matcher = HELLO.matcher(chat);
        if (matcher.matches()) {
            String sat = matcher.group(1);
            userService.setCurrentSat(jid, sat);
        }

        return "Hello " + getName(jid) + "!";
    }

    private String sayWhere(String jid, String chat) {
        String sat = userService.getCurrentSat(jid);

        String where = null;
        if (chat.contains("now")) {
            where = locationService.location(new Location(23.0f, 23.0f));
        } else if (chat.contains("will")) {
            Matcher matcher = WHEN.matcher(chat);
            if (matcher.matches()) {
                String when = matcher.group(1);
                where = locationService.location(new Location(55.0f, 24.0f));
            }
        }

        if (where == null) {
            where = "here";
        }
        return "I'm " + where + "!";
    }

    private String sayWhen(String jid, String chat) {
        Matcher matcher = WHERE.matcher(chat);
        if (matcher.matches()) {
            Location location = locationService.location(matcher.group(1));
            String sat = userService.getCurrentSat(jid);

            return "I'll be there in "+in(new DateTime(DateTimeZone.UTC).plusDays(2).toDate());
        }

        return "Soon";
    }

    private String sayBye() {
        return "Bye!";
    }

    private String getName(String jid) {
        Matcher matcher = JID.matcher(jid);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "you";
    }

    private String in(Date expected) {
        
        DateTime now = new DateTime(DateTimeZone.UTC);
        DateTime then = new DateTime(expected.getTime(), DateTimeZone.UTC);
        Period period = new Interval(now, then).toPeriod();
        int printed = 0;

        StringBuffer ago = new StringBuffer();
        printed = printPeriod(ago, printed, period.getYears(), "year");
        printed = printPeriod(ago, printed, period.getMonths(), "month");
        printed = printPeriod(ago, printed, period.getWeeks(), "week");
        printed = printPeriod(ago, printed, period.getDays(), "day");
        printed = printPeriod(ago, printed, period.getHours(), "hour");
        printed = printPeriod(ago, printed, period.getMinutes(), "minute");

        return ago.toString();
    }

    private int printPeriod(StringBuffer ago, int printed, int value, String desc) {
        if (printed > 1) {
            return printed;
        }

        if (value > 1) {
            if (printed == 1)
                ago.append(" and ");
            ago.append(value + " " + desc + "s");
        } else if (value == 1) {
            if (printed == 1)
                ago.append(" and ");
            ago.append(value + " " + desc);
        } else {
            return printed;
        }
        return printed + 1;
    }
}
