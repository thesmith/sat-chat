package science.hack.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import lombok.Data;

import com.google.appengine.api.datastore.Key;

@Entity
@Table(name = "user")
public @Data class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key id;
    
    @Version
    @Column(name = "version")
    protected long version;
    
    @Basic
    @Column(name = "jid", length = 255)
    private String jid;
    
    @Basic
    @Column(name = "sat", length = 255)
    private String sat;

    public User(String jid, String sat) {
        this.jid = jid;
        this.sat = sat;
    }
}
