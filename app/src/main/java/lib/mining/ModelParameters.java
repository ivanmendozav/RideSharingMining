package lib.mining;

/**
 * Created by Ivan on 05/05/2015.
 */
public class ModelParameters {
    //STAY POINTS
    public static double distance_threshold = 0.15; //km. To look for points around a stay point
    public static long time_threshold = 900000; //milliseconds. To dwell around a stay point
    public static int cluster_size = 10; //integer. visits to a stay point before converting to a POI
    public static int earth_radius = 6371; //average radius
    public static String csv_stay_points = "stay_points.txt";
    public static double merging_interval = 600000; //milliseconds

    //GENERAL SETTINGS
    public static String csv_delimiter = ","; //to parse a CSV file's line
    public static int csv_longitude_column = 1;
    public static int csv_latitude_column = 0;
    public static int csv_altitude_column = 2;
    public static int csv_timestamp_column = 3;

    //DEVELOPER
    public static boolean debug_mode = true;
}
