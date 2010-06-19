package science.hack;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import com.google.appengine.repackaged.com.google.common.collect.Maps;

public class UserService {
    
    private final Cache cache;
    
    public UserService() {
        try {
            cache = CacheManager.getInstance().getCacheFactory().createCache(Maps.newHashMap());
        } catch (CacheException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCurrentSat(String user, String sat) {
        cache.put(user, sat);
    }
    
    public String getCurrentSat(String user) {
        return (String) cache.get(user);
    }
}
