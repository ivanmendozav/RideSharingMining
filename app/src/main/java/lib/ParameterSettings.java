package lib;

/**
 * Created by Ivan on 15/04/2015.
 */
public class ParameterSettings {
    public static int TimerInterval = 30*60*1000; //30 minutes (in milliseconds)
    public static int GPSPointsInterval = 10 * 1000; //10 seconds (in milliseconds)
    public static int SensorDelay = 1*1000*1000;// 1 second (in microseconds)
    //public static String ServerUrl = "http://10.0.2.2:88/ridesharingmining/receive.php"; //local
    public static String ServerUrl = "http://www.proyectomed.org/receive.php"; //server (godaddy)
}
