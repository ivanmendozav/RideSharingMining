package lib;

/**
 * Created by Ivan on 15/04/2015.
 */
public class ParameterSettings {
    public static int TimerInterval = 60*60*1000; //60 minutes (in milliseconds)
    public static int GPSPointsInterval = 5 * 1000; //5 seconds (in milliseconds)
    public static int SensorDelay = 1*1000*1000;// 1 second (in microseconds) if another app gets a GPS point earlier
    //public static Str+ing ServerUrl = "http://10.0.2.2:88/ridesharingmining/receive.php"; //local
    public static String ServerUrl = "http://www.proyectomed.org/receive.php"; //server (godaddy)
}
