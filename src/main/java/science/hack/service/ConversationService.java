package science.hack.service;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import science.hack.model.Location;

import com.google.appengine.repackaged.org.joda.time.DateTime;
import com.google.appengine.repackaged.org.joda.time.DateTimeZone;
import com.google.appengine.repackaged.org.joda.time.Interval;
import com.google.appengine.repackaged.org.joda.time.Period;

@Component
public class ConversationService {

    private static final Pattern HELLO = Pattern.compile("^hello\\s*(\\w+).*$");
    private static final Pattern WHERE = Pattern.compile("^.*\\s+in\\s*(.+)\\s*soon\\??$");
    private static final Pattern WHEN = Pattern.compile("^.*in\\s*(\\d+)\\s*(\\w+)\\??$");
    private static final Pattern ONE_WHEN = Pattern.compile("^.*in\\s*a\\s*(\\w+)\\??$");
    private static final Pattern NEXT_WHEN = Pattern.compile("^.*in\\s*next\\s*(\\w+)\\??$");
    private static final Pattern JID = Pattern.compile("^(\\w+).*");

    private final UserService userService;
    private final LocationService locationService;
    private final SatelliteService satelliteService;

    @Autowired
    public ConversationService(UserService userService, LocationService locationService,
                    SatelliteService satelliteService) {
        this.userService = userService;
        this.locationService = locationService;
        this.satelliteService = satelliteService;
    }

    public String chat(String jid, String chat) {
        if (chat.contains("hello")) {
            return sayHello(jid, chat);
        } else if (chat.contains("where")) {
            return sayWhere(jid, chat);
        } else if (chat.contains("soon")) {
            return sayWhen(jid, chat);
        } else if (chat.contains("in")) {
            return sayWhere(jid, chat);
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

        if (sat == null) {
            return "Sorry, there're a lot of us up here. Who were you after?";
        }
        
        String where = null;
        String prefix = "I'm ";
        DateTime when = new DateTime(DateTimeZone.UTC);
        
        if (chat.contains("in")) {
            prefix = "I'll be ";
            Matcher matcher = WHEN.matcher(chat);
            if (matcher.matches()) {
                when = getDate(Integer.valueOf(matcher.group(1)), matcher.group(2));
            } else {
                matcher = ONE_WHEN.matcher(chat);
                if (matcher.matches()) {
                    when = getDate(Integer.valueOf(1), matcher.group(1));
                } else if (chat.contains("tomorrow")) {
                    when = getDate(Integer.valueOf(1), "day");
                } else if (chat.contains("next week")) {
                    when = getDate(Integer.valueOf(1), "week");
                } else if (chat.contains("fortnight")) {
                    when = getDate(Integer.valueOf(2), "week");
                } else {
                    matcher = NEXT_WHEN.matcher(chat);
                    if (matcher.matches()) {
                        when = getDate(Integer.valueOf(1), matcher.group(1));
                    }
                }
            }
        }

        Location location = satelliteService.location(sat, when);
        if (location != null) {
            where = locationService.location(location);
            if (where == null) {
                where = "hovering over the ocean";
            } else {
                where = "in " + where;
            }
        }

        if (where == null) {
            where = "here";
        }
        return prefix + where + ".";
    }

    private String sayWhen(String jid, String chat) {
        Matcher matcher = WHERE.matcher(chat);
        if (matcher.matches()) {
            String sat = userService.getCurrentSat(jid);
            if (sat == null) {
                return "Sorry, there're a lot of us up here. Who were you after?";
            }
            
            List<Location> locations = satelliteService.locations(sat);
            String name = matcher.group(1);
            Location location = locationService.locationMatch(locations, name);
            if (location != null) {
                int hours = locations.indexOf(location);
                if (hours > 0) {
                    return "I'll be there in " + hours + " hours.";
                } else {
                    return "I'm there right now!";
                }
            } else {
                return "Not in the next day :(";
            }
        }

        return "Hopefully.";
    }

    private String sayBye() {
        return "I'm sorry old bean, you'll have to speak up. I'm very far away!";
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

    private DateTime getDate(Integer period, String type) {
        if (type.endsWith("s")) {
            type = type.substring(0, type.length() - 1);
        }
        DateTime date = new DateTime(DateTimeZone.UTC);

        if ("second".equals(type)) {
            return date.plusSeconds(period);
        } else if ("minute".equals(type)) {
            return date.plusMinutes(period);
        } else if ("hour".equals(type)) {
            return date.plusHours(period);
        } else if ("day".equals(type)) {
            return date.plusDays(period);
        } else if ("month".equals(type)) {
            return date.plusMonths(period);
        } else if ("year".equals(type)) {
            return date.plusYears(period);
        }

        return date;
    }
}
