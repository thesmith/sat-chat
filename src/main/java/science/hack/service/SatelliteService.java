package science.hack.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import science.hack.model.Location;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.repackaged.com.google.common.collect.Maps;
import com.google.appengine.repackaged.org.joda.time.DateTime;

@Component
public class SatelliteService {

    private static final String URL = "http://randomorbit.net/satellites/%s/positions/iso:%s.json";
    private static final String DAY_URL = "http://randomorbit.net/satellites/%s/positions.json?interval=60&length=24";

    private final Map<String, String> satMappings = Maps.newHashMap();

    public SatelliteService() {
        satMappings.put("hubble", "20580");
        satMappings.put("zayra", "25544");
        satMappings.put("international space station", "25544");
        satMappings.put("iss", "25544");
    }

    public Location location(String sat, DateTime when) {
        try {
            if (satMappings.containsKey(sat)) {
                sat = satMappings.get(sat);
            }

            URL url = new URL(String.format(URL, sat, URLEncoder.encode(when.toDate().toString(), "UTF-8")));
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            StringBuffer json = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();

            JSONObject query = new JSONObject(json.toString());
            JSONObject position = query.getJSONObject("position");

            return new Location(Float.valueOf(position.getString("latitude")), Float.valueOf(position
                            .getString("longitude")));
        } catch (Exception e) {
            return null;
        }
    }

    public List<Location> locations(String sat) {
        List<Location> locations = Lists.newArrayList();

        try {
            if (satMappings.containsKey(sat)) {
                sat = satMappings.get(sat);
            }

            URL url = new URL(String.format(DAY_URL, sat));
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            StringBuffer json = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();

            JSONObject query = new JSONObject(json.toString());
            JSONArray positions = query.getJSONArray("positions");

            for (int i = 0; i < positions.length(); i++) {
                JSONObject position = positions.getJSONObject(i);

                locations.add(new Location(Float.valueOf(position.getString("latitude")), Float.valueOf(position
                                .getString("longitude"))));
            }

        } catch (Exception e) {
        }

        return locations;
    }
}
