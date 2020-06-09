package nitin.com.myapplication;

public class POIBean {
    private int ID;
    private String name;
    private Double lat;
    private Double lng;

    POIBean(int ID, String name,Double lat,Double lng){
       this.ID = ID;
       this.name = name;
       this.lat = lat;
       this.lng = lng;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
