package lib;

/**
 * Created by Ivan on 15/04/2015.
 */
public class ParameterSettings {
    public static int TimerInterval = 60*60*1000; //60 minutes (in milliseconds)
    public static int GPSPointsInterval = 5 * 1000; //5 seconds (in milliseconds)
    public static int SensorDelay = 1*1000*1000;// 1 second for the rest of sensors
    //public static Str+ing ServerUrl = "http://10.0.2.2:88/ridesharingmining/receive.php"; //local
    public static String ServerUrl = "http://www.proyectomed.org/receive.php"; //server (godaddy)
    public static String LogFile = "error_log.txt";
    public static int LastEvents = 10; //number of last events to display when starting the app
    public static int StayPointsSensorID = 100; //pseudo sensor id to identify data from stay points detector
    public static String default_user = "test";
}
