package lib.mining;

import android.graphics.AvoidXfermode;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import listeners.GPSListener;

/**
 * Created by Ivan on 05/05/2015.
 */
public class CsvParser {
    /**
     * Parse a csv file and produce a list of strings for each line
     * @param filename
     * @return
     */
    public static List<String> ParseFile(String filename){
        List<String> lines = new ArrayList<>();
        File fFilePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Documents/"+filename);

        try {
            if(fFilePath.exists()) {
                Scanner scanner = new Scanner(fFilePath);
                while (scanner.hasNextLine()) {
                    lines.add(scanner.nextLine());
                }
             }
            return lines;
        }catch(Exception e){
            return null;
        }
    }

    /**
     * Creates or appends to an existing CSV file of stay points
     * @param stay_points
     */
    public static void PersistStayPoints(List<StayPoint> stay_points){
        try {
            String filename = ModelParameters.csv_stay_points;
            String dirname = Environment.getExternalStorageDirectory().getPath()+"/Documents/";
            String string = "";

            File file = new File(dirname+filename);
            file.createNewFile(); //create if doesn't exist
            FileWriter filewriter = new FileWriter(dirname+filename, true); //true for append
            BufferedWriter out = new BufferedWriter(filewriter);

            Iterator<StayPoint> it = stay_points.iterator();
            while(it.hasNext()){
                StayPoint s = it.next();
                string = s.getAvg_longitude()+","+s.getAvg_latitude()+","+s.getArrival()+","+s.getDeparture()+","+s.getCardinality()+","+s.getLabel()+","+s.getStartPoint().getLongitude()+","+s.getStartPoint().getLatitude()+ ","+s.id;
                out.write(string +"\n"); //CSV format with line break between measures
            }
            out.close();
            filewriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates or appends to an existing CSV file of gps points
     * @param gps_points
     */
    public static void PersistGpsPoints(List<GpsPoint> gps_points){
        try {
            String filename = GPSListener.getFileName();
            String dirname = Environment.getExternalStorageDirectory().getPath()+"/Documents/";
            String string = "";

            File file = new File(dirname+filename);
            file.createNewFile(); //create if doesn't exist
            FileWriter filewriter = new FileWriter(dirname+filename, true); //true for append
            BufferedWriter out = new BufferedWriter(filewriter);

            Iterator<GpsPoint> it = gps_points.iterator();
            while(it.hasNext()){
                GpsPoint p = it.next();
                string = p.getLatitude()+","+p.getLongitude()+","+p.getAltitude()+","+p.getTimestamp();
                out.write(string +"\n"); //CSV format with line break between measures
            }
            out.close();
            filewriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
