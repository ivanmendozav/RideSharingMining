package lib.mining;

/**
 * Created by Ivan on 05/05/2015.
 * A coordinated measured with a navigation device
 * Attributes: latitude, longitude, timestamp, Optional:altitude
 * Timestamp represents the time the point as measured
 * Timestamp interval between points can be fixed or dynamic (depending on signal strength)
 */
public class GpsPoint {
    protected double longitude;
    protected double latitude;
    protected double altitude;
    protected long timestamp; //UNIX timestamp

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Constructor
     * @param longitude
     * @param latitude
     * @param altitude
     * @param timestamp
     */
    public GpsPoint(double longitude, double latitude, double altitude, long timestamp){
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.timestamp = timestamp;
    }
}
