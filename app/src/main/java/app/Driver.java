package app;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ivan on 02/03/2015.
 */
public class Driver {
    private String name;
    private double distance;
    private double time;
    private double rating;
    private long id;

    public Driver(){

    }

    public Driver(JSONObject jo){
        try {
            this.setName(jo.getString("name"));
            this.setDistance(jo.getDouble("distance"));
            this.setTime(jo.getDouble("time"));
            this.setRating(jo.getDouble("rating"));
            this.setId(jo.getInt("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
