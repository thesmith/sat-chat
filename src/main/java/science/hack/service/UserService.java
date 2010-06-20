package science.hack.service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import science.hack.model.User;

@Component
@Transactional
public class UserService {

    @PersistenceContext
    private EntityManager em;

    // private final Cache cache;

    // public UserService() {
    // try {
    // cache =
    // CacheManager.getInstance().getCacheFactory().createCache(Maps.newHashMap());
    // } catch (Exception e) {
    // throw new RuntimeException();
    // }
    // }

    // @Autowired
    // public UserService(CacheFactory cacheFactory) {
    // try {
    // cache = cacheFactory.createCache(Maps.newHashMap());
    // } catch (CacheException e) {
    // throw new RuntimeException(e);
    // }
    // }

    public void setCurrentSat(String jid, String sat) {
        // cache.put(user, sat);

        User user = null;
        try {
            user = (User) em.createQuery("select u from User u where u.jid=:jid").setParameter("jid", jid)
                            .getSingleResult();
        } catch (NoResultException e) {
        }
        if (user != null) {
            user.setSat(sat);
            em.merge(user);
        } else {
            em.persist(new User(jid, sat));
        }
    }

    public String getCurrentSat(String jid) {
        // return (String) cache.get(user);

        User user = null;
        try {
            user = (User) em.createQuery("select u from User u where u.jid=:jid").setParameter("jid", jid)
                            .getSingleResult();
        } catch (NoResultException e) {
        }
        if (user != null) {
            return user.getSat();
        }
        return null;
    }
}
