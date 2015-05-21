package lib.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import lib.ContextManager;
import lib.mining.GpsPoint;
import lib.mining.StayPoint;

/**
 * Created by Ivan on 08/05/2015.
 */
public class StayPointDataObject extends AbstractDataObject{
    public static final String TABLE_NAME = "_stay_points";
    public static final String COLUMN_AVG_LATITUDE = "_avg_latitude";
    public static final String COLUMN_AVG_LONGITUDE = "_avg_longitude";
    public static final String COLUMN_ARRIVAL = "_arrival";
    public static final String COLUMN_DEPARTURE = "_departure";
    public static final String COLUMN_CARDINALITY = "_cardinality";
    public static final String COLUMN_LABEL = "_label";
    public static final String COLUMN_START_LATITUDE = "_start_latitude";
    public static final String COLUMN_START_LONGITUDE = "_start_longitude";
    protected static StayPointDataObject instance = null;

    /**
     * Overrides constructor
     * @param context
     */
    private StayPointDataObject(Context context){
        super(context);
    }

    public static  synchronized StayPointDataObject GetInstance(Context context){
        if(instance == null)
            instance = new StayPointDataObject(context);
        return instance;
    }

    /**
     * Store to database
     * @param newStay
     * @return
     */
    public long Persist(StayPoint newStay){
        StayPoint previousStay = this.GetLastRecord();
        if(previousStay != null) {
            //if stay point already exists from a previous scan
            if (previousStay.IsSubsetOf(newStay)) {
                previousStay.setDeparture(newStay.getDeparture());
                previousStay.setCardinality(newStay.getCardinality());
                previousStay.setAvg_longitude(newStay.getAvg_longitude());
                previousStay.setAvg_latitude(newStay.getAvg_latitude());
                this.Update(previousStay);
                ContextManager.writeAppLog("Last stay time updated!");
                return previousStay.id;
            }
            //if stay point already exists from an interruption (merge with new one)
            if(newStay.IsExtensionOf(previousStay)){
                previousStay.setDeparture(newStay.getDeparture());
                previousStay.setCardinality(previousStay.getCardinality()+newStay.getCardinality());
                //weighted average for update coordinates Wp = |sp| / |sp|+|sn|
                double weight_newStay = newStay.getCardinality() / previousStay.getCardinality()+newStay.getCardinality();
                double weight_previousStay = previousStay.getCardinality() / previousStay.getCardinality()+newStay.getCardinality();
                double updated_longitude = weight_previousStay*previousStay.getAvg_longitude() + weight_newStay*newStay.getAvg_longitude();
                double updated_latitude = weight_previousStay*previousStay.getAvg_latitude() + weight_newStay*newStay.getAvg_latitude();

                previousStay.setAvg_longitude(updated_longitude);
                previousStay.setAvg_latitude(updated_latitude);
                this.Update(previousStay);
                ContextManager.writeAppLog("Last stay merged!");
                return previousStay.id;
            }
        }
        this.locked = true;
        SQLiteDatabase database = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AVG_LATITUDE, newStay.getAvg_latitude());
        values.put(COLUMN_AVG_LONGITUDE, newStay.getAvg_longitude());
        values.put(COLUMN_ARRIVAL, newStay.getArrival());
        values.put(COLUMN_DEPARTURE, newStay.getDeparture());
        values.put(COLUMN_CARDINALITY, newStay.getCardinality());
        values.put(COLUMN_LABEL, newStay.getLabel());
        values.put(COLUMN_START_LATITUDE, newStay.getStartPoint().getLatitude());
        values.put(COLUMN_START_LONGITUDE, newStay.getStartPoint().getLongitude());
        long _id = database.insert(StayPointDataObject.TABLE_NAME, null, values);
        ContextManager.writeAppLog("Stay detected at " + newStay.getAvg_latitude() + " , " + newStay.getAvg_longitude());
        this.locked = false;
        database.close();
        return _id;
    }

    /**
     * Update stay point attributes
     * @param p
     */
    public void Update(StayPoint p){
        this.locked = true;
        SQLiteDatabase database = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEPARTURE, p.getDeparture());
        values.put(COLUMN_CARDINALITY, p.getCardinality());
        values.put(COLUMN_AVG_LONGITUDE, p.getAvg_longitude());
        values.put(COLUMN_AVG_LATITUDE, p.getAvg_latitude());
        database.update(StayPointDataObject.TABLE_NAME, values, AppSQLiteHelper.SQL_COLUMN_ID + "=" + p.id, null);
        this.locked = false;
        database.close();
    }

    /**
     * GetAll all points in database
     * @return
     */
    public List<StayPoint> GetAll(){
        this.locked = true;
        SQLiteDatabase database = this.helper.getReadableDatabase();
        List<StayPoint> list = new ArrayList<>();
        String[] fields = new String[] {AppSQLiteHelper.SQL_COLUMN_ID,COLUMN_AVG_LONGITUDE,COLUMN_AVG_LATITUDE,COLUMN_ARRIVAL,COLUMN_DEPARTURE,COLUMN_CARDINALITY,COLUMN_LABEL,COLUMN_START_LATITUDE, COLUMN_START_LONGITUDE};
        Cursor c = database.query(StayPointDataObject.TABLE_NAME, fields, null, null, null, null, null);

    //If there are records
        if (c.moveToFirst()) {
            do {
                long id = c.getInt(0);
                double avg_longitude = c.getDouble(1); //avg_longitude
                double avg_latitude= c.getDouble(2); //avg_latitude
                long arrival= c.getLong(3); //arrival
                long departure= c.getLong(4); //departure
                int cardinality= c.getInt(5); //cardinality
                String label = c.getString(6); //label
                double start_latitude= c.getDouble(7); //first point latitude
                double start_longitude = c.getDouble(8); //first point longitude
                GpsPoint pm = new GpsPoint(start_longitude,start_latitude,0,arrival);
                StayPoint p = new StayPoint(avg_longitude,avg_latitude,arrival,departure,cardinality,label,pm);
                p.id = id;
                list.add(p);
            } while(c.moveToNext());
        }
        this.locked = false;
        database.close();
        return list;
    }

    /**
     * Get last stay point in database (to verify if user has changed position)
     * @return
     */
    public StayPoint GetLastRecord(){
        List<StayPoint> list = this.GetAll();
        StayPoint p = null;
        if(list.size() > 0){
            p = list.get(list.size()-1);
        }
        return p;
    }
}
