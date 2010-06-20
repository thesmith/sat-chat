package science.hack.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.appengine.repackaged.com.google.common.collect.Maps;

import science.hack.model.Location;

@Component
public class LocationService {

    private static final String QUERY = "select * from flickr.places where lat=%f and lon=%f";
    private static final String URL = "http://query.yahooapis.com/v1/public/yql?q=%s&format=json";
    private static final String PLACES_QUERY = "select * from flickr.places where query=\"%s\"";

    @Autowired
    private CacheFactory cacheFactory;
    private Cache cache;

    public String location(Location location) {
        try {
            String name = (String) getCache().get(location.toKey());
            if (name != null) {
                return name;
            }
            
            URL url = new URL(String.format(URL, URLEncoder.encode(String.format(QUERY, location.getLatitude(),
                            location.getLongitude()), "UTF-8")));
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            StringBuffer json = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();

            JSONObject query = new JSONObject(json.toString());
            JSONObject places = query.getJSONObject("query").getJSONObject("results").getJSONObject("places");

            if (places.has("place")) {
                JSONObject place = places.getJSONObject("place");
                name = place.getString("name");
                getCache().put(location.toKey(), name);
                return name;
            }

            return null;
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

            Location location = new Location(Float.valueOf(place.getString("latitude")), Float.valueOf(place.getString("longitude")));
            getCache().put(location.toKey(), name);
            return location;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Location locationMatch(List<Location> locations, String name) {
        name = name.toLowerCase().trim();

        for (Location location : locations) {
            String currentName = location(location);
            if (currentName != null) {
                currentName = currentName.toLowerCase();
                
                if (currentName.indexOf(name) != -1) {
                    return location;
                }
            }
        }

        return null;
    }

    private Cache getCache() {
        if (cache == null) {
            final Map<String, String> props = Maps.newHashMap();
            // props.put(GCacheFactory.EXPIRATION_DELTA, EXPIREY);
            try {
                cache = cacheFactory.createCache(props);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return cache;
    }
}
