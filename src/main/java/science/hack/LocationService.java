package science.hack;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import science.hack.model.Location;

public class LocationService {

    private static final String QUERY = "select * from flickr.places where lat=%f and lon=%f";
    private static final String URL = "http://query.yahooapis.com/v1/public/yql?q=%s&format=json";
    private static final String PLACES_QUERY = "select * from flickr.places where query=\"%s\"";

    public String location(Location location) {
        try {
            URL url = new URL(String.format(URL, URLEncoder.encode(String.format(QUERY, location.getLatitude(), location.getLongitude()), "UTF-8")));
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            StringBuffer json = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();

            JSONObject query = new JSONObject(json.toString());
            JSONObject place = query.getJSONObject("query").getJSONObject("results").getJSONObject("places").getJSONObject("place");
            
            return place.getString("name");
        } catch (Exception e) {
            return null;
        }
    }
    
    public Location location(String name) {
        try {
            URL url = new URL(String.format(URL, URLEncoder.encode(String.format(PLACES_QUERY, name), "UTF-8")));
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            StringBuffer json = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();

            JSONObject query = new JSONObject(json.toString());
            Object result = query.getJSONObject("query").getJSONObject("results").get("place");
            JSONObject place = null;
            if (result instanceof JSONObject) {
                place = ((JSONObject) result);
            } else {
                place = ((JSONArray) result).getJSONObject(0);
            }
            
            return new Location(Float.valueOf(place.getString("latitude")), Float.valueOf(place.getString("longitude")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
