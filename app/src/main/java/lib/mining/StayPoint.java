package lib.mining;

import java.util.Iterator;
import java.util.List;

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
    protected GpsPoint startPoint = null; //pm
    protected long stay_time;
    protected List<GpsPoint> points;
    public long id; //for database storage

    /**
     * Constructor
     * @param avg_longitude
     * @param avg_latitude
     * @param arrival
     * @param departure
     * @param cardinality
     * @param label
     */
    public StayPoint(double avg_longitude, double avg_latitude, long arrival,long departure, int cardinality, String label, GpsPoint startPoint){
        this.avg_longitude = avg_longitude;
        this.avg_latitude = avg_latitude;
        this.arrival = arrival;
        this.departure = departure;
        this.cardinality = cardinality;
        this.label = label;
        this.startPoint = startPoint;
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

    public GpsPoint getStartPoint() {
        return startPoint;
    }

    public void setDeparture(long departure) {
        this.departure = departure;
    }

    public void setCardinality(int cardinality) {
        this.cardinality = cardinality;
    }

    public void setAvg_latitude(double avg_latitude) {
        this.avg_latitude = avg_latitude;
    }

    public void setAvg_longitude(double avg_longitude) {
        this.avg_longitude = avg_longitude;
    }

    public List<GpsPoint> getPoints() {
        return points;
    }

    public void setPoints(List<GpsPoint> points) {
        this.points = points;
    }

    /**
     * Tells whether two stay points in log G start from the same start_point.
     * sp1.start = sp2.start, sp1.departure < sp2.departure, and sp1 & sp2 belongs to G
     * then sp1 is subset of sp2. Where sp.start = pm.timestamp
     * @param set
     * @return
     */
    public boolean IsSubsetOf(StayPoint set){
        if ((this.departure <= set.getDeparture()) && (this.startPoint.IsSameAs(set.getStartPoint()))) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Tells whether two consecutive stay points in log G can be merged into the same point.
     * sp1.departure_time ~ sp2.start_time, dist(sp1.arrival_point,sp2.points) still lower than Dth,
     * and sp1 & sp2 belongs to G then sp2 is a extension of sp2.
     * Where sp.start_time = pm.timestamp, sp.departure_time = po.timestamp, sp.points = all points from pm-po in sp
     * @param previousPoint
     * @return
     */
    public boolean IsExtensionOf(StayPoint previousPoint){
        if (this.arrival - previousPoint.getDeparture() <= ModelParameters.merging_interval){
            Iterator<GpsPoint> it = this.getPoints().iterator();
            boolean reachable = true; //if all points are reachable
            while(it.hasNext()){
                if(ModelFormulas.GCDistance(previousPoint.getStartPoint(),it.next()) > ModelParameters.distance_threshold){
                    reachable = false;
                }
            }
            if(reachable)
                return true;
        }
        return false;
    }
}
