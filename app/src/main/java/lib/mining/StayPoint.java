package lib.mining;

/**
 * Created by Ivan on 05/05/2015.
 * A Stay point S is defined as a virtual space region defined by |S| points(pm,..po) where a user has spend a long amount of time.
 * Given a GPS log G of n Gps points<lon,lat,timestamp> = {p1,p2,pm, pm+1,..,po,..,pn} is a total order over time p3>p2>p1
 * a threshold epsilon that represents the minimum time a user has to stay in a region(between points pm and po)
 * to be consider a Stay point (E.g. 1 hour)
 * a parameter delta for the maximum distance to search around a point pm.
 * A stay point requires distance(pm, pi) <= delta.  For time(pi) > time(pm) and time(pi)<=po
 * And also that time(po)- time(pm) >= epsilon
 *
 * Attributes are: average latitude, average longitude, arrival and depature timestamps. Optional: label
 * Example: 4.689, 50.81233, 481234654, 48124657, home.
 */
public class StayPoint {
    protected double avg_latitude; // sum(pi.lat)/|S|  For pm<=pi<=po
    protected double avg_longitude; // sum(pi.lon)/|S|  For pm<=pi<=po
    protected long arrival; // time(pm)
    protected long departure; // time(po)
    protected int cardinality; // |S|
    protected String label; //optional description (if filled in by the user)
    protected long stay_time;

    /**
     * Constructor
     * @param avg_longitude
     * @param avg_latitude
     * @param arrival
     * @param departure
     * @param cardinality
     * @param label
     */
    public StayPoint(double avg_longitude, double avg_latitude, long arrival,long departure, int cardinality, String label){
        this.avg_longitude = avg_longitude;
        this.avg_latitude = avg_latitude;
        this.arrival = arrival;
        this.departure = departure;
        this.cardinality = cardinality;
        this.label = label;
        this.stay_time = Math.abs(departure - arrival);
    }

    public double getAvg_latitude() {
        return avg_latitude;
    }

    public double getAvg_longitude() {
        return avg_longitude;
    }

    public long getArrival() {
        return arrival;
    }

    public long getDeparture() {
        return departure;
    }

    public int getCardinality() {
        return cardinality;
    }

    public String getLabel() {
        return label;
    }

    public long getStay_time() {
        return stay_time;
    }
}
