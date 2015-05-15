package lib.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import lib.mining.GpsPoint;

/**
 * Created by Ivan on 08/05/2015.
 */
public class GpsDataObject {
    public static final String TABLE_NAME = "_gps_sensor_data";
    public static final String COLUMN_LONGITUDE = "_longitude";
    public static final String COLUMN_LATITUDE = "_latitude";
    public static final String COLUMN_ALTITUDE = "_altitude";
    public static final String COLUMN_TIMESTAMP = "_timestamp";
    protected static GpsDataObject instance = null;
    protected boolean locked = false;
    protected AppSQLiteHelper helper;

    /**
     * Construct. Start connection
     * @param c
     */
    private GpsDataObject(Context c){
        this.helper = new AppSQLiteHelper(c);
    }

    public static  synchronized GpsDataObject GetInstance(Context context){
        if(instance == null)
            instance = new GpsDataObject(context);
        return instance;
    }

    /**
     * Store to database
     * @param p
     * @return
     */
    public long Persist(GpsPoint p){
        this.locked = true;
        SQLiteDatabase database = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LONGITUDE, p.getLongitude());
        values.put(COLUMN_LATITUDE, p.getLatitude());
        values.put(COLUMN_ALTITUDE, p.getAltitude());
        values.put(COLUMN_TIMESTAMP, p.getTimestamp());

        long _id = database.insert(GpsDataObject.TABLE_NAME, null, values);
        this.locked = false;
        database.close();
        return _id;
    }

    /**
     * GetAll all points in database
     * @return
     */
    public List<GpsPoint> GetAll(){
        this.locked = true;
        SQLiteDatabase database = this.helper.getReadableDatabase();
        List<GpsPoint> list = new ArrayList<>();
        String[] fields = new String[] {COLUMN_LONGITUDE, COLUMN_LATITUDE,COLUMN_ALTITUDE,COLUMN_TIMESTAMP};
        Cursor c = database.query(GpsDataObject.TABLE_NAME, fields, null, null, null, null, null);

    //If there are records
        if (c.moveToFirst()) {
            do {
                double longitude= c.getDouble(0); //longitude
                double latitude= c.getDouble(1); //latitude
                double altitude= c.getDouble(2); //altitude
                long timestamp= c.getLong(3); //timestamp
                GpsPoint p = new GpsPoint(longitude,latitude,altitude,timestamp);
                list.add(p);
            } while(c.moveToNext());
        }
        this.locked = false;
        database.close();
        return list;
    }

    /**
     * Purge data already processed
     * @param toTimesStamp
     * @return
     */
    public int Purge(long toTimesStamp){
        this.locked = true;
        SQLiteDatabase database = this.helper.getWritableDatabase();
        int affected = database.delete(GpsDataObject.TABLE_NAME, COLUMN_TIMESTAMP + "<" + toTimesStamp , null);
        database.close();
        this.locked = false;
        return affected;
    }

    /**
     * Close connection
     */
    public void Close(){
        this.helper.close();
    }

    public boolean isLocked(){
        return this.locked;
    }
}
