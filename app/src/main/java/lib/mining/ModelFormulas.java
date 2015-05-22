package lib.mining;

/**
 * Created by Ivan on 05/05/2015.
 */
public class ModelFormulas {
    /**
     * Great-circle distance between two GPS points p1, p2 (Arc length between two locations on Earth)
     * From spherical law of cosines
     * Earth radius r ~ 6371 km.
     * Distance = r*delta. With delta the central angle = arccos(sin(lat1)*sin(lat2) + cos(lat1)*cos(lat2)*cos(lon1-lon2))
     * @param p1
     * @param p2
     */
    public static double GCDistance(GpsPoint p1,GpsPoint p2){
        int r = ModelParameters.earth_radius;
        double lat1 = Math.toRadians(p1.getLatitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double deltalon =  Math.toRadians(p1.getLongitude()-p2.getLongitude());

        double delta = Math.acos(Math.sin(lat1)*Math.sin(lat2) + Math.cos(lat1)*Math.cos(lat2)*Math.cos(deltalon));
        double distance = r*delta;
        return distance;
    }

    /**
     * Haversine formula for shortest distance on a sphere (recommended)
     * @param p1    50.861674, 4.681372
     * @param p2    50.861116, 4.682084  (around 80 meters distance)
     * @return
     */
    public static double HavDistance(GpsPoint p1,GpsPoint p2){
        int R = ModelParameters.earth_radius;; // metres
        double lat1 = Math.toRadians(p1.getLatitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double lon1 = Math.toRadians(p1.getLongitude());
        double lon2 = Math.toRadians(p2.getLongitude());

        double a = Math.sin((lat2-lat1)/2)*Math.sin((lat2-lat1)/2) + Math.cos(lat1)*Math.cos(lat2)*Math.sin((lon2-lon1)/2)*Math.sin((lon2-lon1)/2);
        double c = 2*R*Math.asin(Math.sqrt(a));//Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double distance = c;//R * c;
        return distance;
    }

    /**
     * for small distances Pythagoras’ theorem can be used (much less accurate)
     * @param p1
     * @param p2
     * @return
     */
    public static double PytDistance(GpsPoint p1,GpsPoint p2){
        int R = ModelParameters.earth_radius;; // metres
        double lat1 = Math.toRadians(p1.getLatitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double lon1 = Math.toRadians(p1.getLongitude());
        double lon2 = Math.toRadians(p2.getLongitude());

        double x = (lon2-lon1) * Math.cos((lat1+lat2)/2);
        double y = (lat2-lat1);
        double distance = Math.sqrt(x*x + y*y) * R;
        return distance;
    }

    public static void test(){
        //test distance measurements, real (around 80 meters distance)
        GpsPoint p1 = new GpsPoint(4.681372, 50.861674, 0,0);
        GpsPoint p2 = new GpsPoint(4.682084, 50.861116, 0,0);
        double d1 = ModelFormulas.GCDistance(p1,p2);//79.6683648
        double d2 = ModelFormulas.HavDistance(p1,p2);//79.66838124
        double d3 = ModelFormulas.PytDistance(p1,p2);//79.66838124
    }
}
