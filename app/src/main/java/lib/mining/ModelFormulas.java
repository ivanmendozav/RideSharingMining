package lib.mining;

/**
 * Created by Ivan on 05/05/2015.
 */
public class ModelFormulas {
    /**
     * Great-circle distance between two GPS points p1, p2 (Arc length between two locations on Earth)
     * Earth radius r ~ 6371 km.
     * Distance = r*delta. With delta the central angle = arccos(sin(lat1)*sin(lat2) + cos(lat1)*cos(lat2)*cos(lon1-lon2))
     * @param p1
     * @param p2
     */
    public static double GCDistance(GpsPoint p1,GpsPoint p2){
        int r = ModelParameters.earth_radius;
        double lat1 = Math.toRadians(p1.getLatitude());
        double lon1 = Math.toRadians(p1.getLongitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double lon2 = Math.toRadians(p2.getLongitude());

        double delta = Math.acos(Math.sin(lat1)*Math.sin(lat2) + Math.cos(lat1)*Math.cos(lat2)*Math.cos(Math.abs(lon1-lon2)));
        double distance = r*delta;
        return distance;
    }
    /**
     * AVOID TO USE: Only for testing (not real distance, use great-circle distance instead)
     * Euclidean distance between two GPS points
     * For 2 dimensions : pithagorean distance
     * @param p1
     * @param p2
     */
    public static double EDistance(GpsPoint p1, GpsPoint p2){
        double lat1 = p1.getLatitude();
        double lon1 = p1.getLongitude();
        double lat2 = p2.getLatitude();
        double lon2 = p2.getLongitude();

        return Math.sqrt(Math.pow(lat1-lat2,2) + Math.pow(lon1-lon2,2));
    }
}
