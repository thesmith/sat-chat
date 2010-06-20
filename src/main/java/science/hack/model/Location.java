package science.hack.model;

public class Location {

    private final Float latitude;
    private final Float longitude;

    public Location(Float latitude, Float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Float getLatitude() {
        return latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public static Location build(String loc) {
        if (loc != null) {
            String[] values = loc.split(",");
            if (values.length == 2) {
                return new Location(Float.valueOf(values[0]), Float.valueOf(values[1]));
            }
        }
        return null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Location) {
            Location loc = (Location) obj;
            return latitude.equals(loc.latitude) && longitude.equals(loc.longitude);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "Longitude: "+longitude+", Latitude: "+latitude;
    }
    
    public String toKey() {
        return "location_"+longitude.intValue()+"_"+latitude.intValue();
    }
}
