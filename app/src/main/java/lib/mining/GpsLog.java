package lib.mining;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lib.ContextManager;

/**
 * Created by Ivan on 05/05/2015.
 * Set of n Gps points<lon,lat,timestamp> = {p1,p2,..,pn-1,pn} measured within a time interval
 * Timestamp interval between points can be fixed or dynamic (depending on signal strength)
 */
public class GpsLog {
    protected List<GpsPoint> gps_points;
    public GpsLog(){
        this.gps_points = new ArrayList<>();
    }
    /**
     * Adds a point to the Log
     * @param gps_point
     */
    public void AddPoint(GpsPoint gps_point){
        this.gps_points.add(gps_point);
    }

    public void setGps_points(List<GpsPoint> gps_points) {
        this.gps_points = gps_points;
    }

    public List<GpsPoint> getGps_points() {
        return gps_points;
    }

    /**
     * Instantiates a GPS log object from a CSV file (longitude, latitude, timestamp).
     * Fields must conform ModelParameters static attributes
     * @param filename
     */
    public void loadPointsFromCSV(String filename){
        String delimiter = ModelParameters.csv_delimiter;
        int longitude_index = ModelParameters.csv_longitude_column;
        int latitude_index = ModelParameters.csv_latitude_column;
        int altitude_index = ModelParameters.csv_altitude_column;
        int timestamp_index = ModelParameters.csv_timestamp_column;

        Iterator<String> it = CsvParser.ParseFile(filename).iterator();
        while(it.hasNext()){
            String line = it.next(); String[] row = line.split(delimiter);
            double longitude = Double.parseDouble(row[longitude_index]);
            double latitude = Double.parseDouble(row[latitude_index]);
            double altitude = Double.parseDouble(row[altitude_index]);
            long timestamp = Long.parseLong(row[timestamp_index]);
            this.AddPoint(new GpsPoint(longitude, latitude, altitude, timestamp));
        }
    }

    /**
     * Identity points pm to po of extended dwell for the GPS points in the interval [k,n]
     * Default values are k = 0 and n = length of log
     * A stay point requires distance(pm, pi) <= delta.  For time(pm)< time(pi)< time(pi)<=po
     * And also that time(po)- time(pm) >= epsilon
     * Moreover keep track of last pm's time (start point where last analysis[pm-po] was interrupted so it can continue from it)
     */
    public List<StayPoint> GetStayPoints(int k, int n){
        List<StayPoint> stay_points = new ArrayList<>();
        //model parameters
        boolean debug = ModelParameters.debug_mode;
        double delta = ModelParameters.distance_threshold;
        double epsilon = ModelParameters.time_threshold;
        long start = System.currentTimeMillis();

        GpsLog subset_log = null; //set of points {pm,.., po} that form a stay point
        int m = k; int o = 0;
        GpsPoint po = null;GpsPoint pm = null; GpsPoint pi = null;

        while(m < n-1){
            //for two successive points
            subset_log = new GpsLog();
            pm = this.getPoint(m);
            int i = m+1; //pm < pi <po
            pi = this.getPoint(i);

            //count next points within a distance lower than delta
            while(ModelFormulas.GCDistance(pm, pi) <= delta && i<n){
                o = i; po = this.getPoint(o); //last point po under requirements
                subset_log.AddPoint(pi);
                i++; if(i<n) {pi = this.getPoint(i);}
            }

            if (subset_log.length()>0){
                subset_log.AddPoint(pm);
                //check whether dwelling time is greater than epsilon
                long stay_time = Math.abs(po.getTimestamp() - pm.getTimestamp());

                if(stay_time >= epsilon){
                    //create stay point from subset
                    double avg_latitude = this.averageLatitudes(subset_log);
                    double avg_longitude = this.averageLongitudes(subset_log);
                    long arrival = pm.getTimestamp();
                    long departure = po.getTimestamp();
                    int cardinality = subset_log.length();
                    StayPoint stay = new StayPoint(avg_longitude,avg_latitude,arrival,departure,cardinality, "", pm);
                    stay.setPoints(subset_log.getGps_points());
                    stay_points.add(stay);
                    //if(debug){ ContextManager.writeAppLog("Stay point:" + arrival + "-->" + departure + " :" + avg_latitude + "," + avg_longitude + "(" + stay_time + "ms)");}
                }
            }
            m = i; // proceed searching from last visited point
        }
        if(debug){ContextManager.writeAppLog(stay_points.size() + " stay point(s) were identified.");
            ContextManager.writeAppLog("Finished in " + String.valueOf(System.currentTimeMillis() - start) + "ms");}
        return stay_points;
    }

    /**
     * Returns point in $index position (zero-based)
     * @param index
     * @return GpsPoint
     */
    public GpsPoint getPoint(int index){
        return this.gps_points.get(index);
    }

    /**
     * Returns average latitude of input GPS points
     * Default whole GPS log
     * @param points
     * @return float
     */
    protected double averageLatitudes(GpsLog points){
        double sum = 0; int length = points.length();
        Iterator<GpsPoint> it = points.getGps_points().iterator();
        while(it.hasNext()){
            GpsPoint p = it.next();
            sum+= (double) p.getLatitude();
        }
        return (length > 0 ? sum/length : 0);
    }

    /**
     * Returns average latitude of input GPS points
     * Default whole GPS log
     * @param points
     * @return float
     */
    protected double averageLongitudes(GpsLog points){
        double sum = 0; int length = points.length();
        Iterator<GpsPoint> it = points.getGps_points().iterator();
        while(it.hasNext()){
            GpsPoint p = it.next();
            sum+= (double) p.getLongitude();
        }
        return (length > 0 ? sum/length : 0);
    }

    /**
     * Size of the log (number of points in it)
     * @return int
     */
    public int length(){
        return this.gps_points.size();
    }
}
