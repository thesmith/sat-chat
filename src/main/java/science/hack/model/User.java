package science.hack.model;

public class User {
    
    private final String jid;
    private final String sat;

    public User(String jid, String sat) {
        this.jid = jid;
        this.sat = sat;
    }
    
    public String getJid() {
        return jid;
    }
    
    public String getSat() {
        return sat;
    }
}
