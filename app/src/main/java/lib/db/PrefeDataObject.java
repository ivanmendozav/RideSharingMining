package lib.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import lib.mining.GpsPoint;
import lib.mining.StayPoint;

/**
 * Created by Ivan on 21/05/2015.
 */
public class PrefeDataObject {
    public static final String TABLE_NAME = "_prefs_data";
    public static final String COLUMN_NAME = "_name";
    public static final String COLUMN_VALUE = "_value";
    protected static PrefeDataObject instance = null;
    protected boolean locked = false;
    protected AppSQLiteHelper helper;

    /**
     * Construct. Start connection
     * @param c
     */
    private PrefeDataObject(Context c){
        this.helper = new AppSQLiteHelper(c);
    }

    /**
     * Singleton to get one single instance
     * @param context
     * @return
     */
    public static synchronized PrefeDataObject GetInstance(Context context){
        if(instance == null)
            instance = new PrefeDataObject(context);
        return instance;
    }

    /**
     * Store to database
     * @param name
     * @param value
     * @return
     */
    public long Set(String name, String value){
        String stored_user = this.Get(name);
        if(stored_user == null){
            this.locked = true;
            SQLiteDatabase database = this.helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(PrefeDataObject.COLUMN_NAME, name);
            values.put(PrefeDataObject.COLUMN_VALUE, value);

            long _id = database.insert(PrefeDataObject.TABLE_NAME, null, values);
            this.locked = false;
            database.close();
            return _id;
        }else{
            this.Update(name, value);
            return 0;
        }
    }

    /**
     * Update a parameter value
     * @param name
     * @param value
     */
    public void Update(String name, String value){
        this.locked = true;
        SQLiteDatabase database = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PrefeDataObject.COLUMN_VALUE, value);
        database.update(PrefeDataObject.TABLE_NAME, values, PrefeDataObject.COLUMN_NAME + "=" + "'" + name + "'", null);
        this.locked = false;
        database.close();
    }

    /**
     * Get the stored value for param name
     * @param name value to look in database
     * @return
     */
    public String Get(String name){
        this.locked = true;
        SQLiteDatabase database = this.helper.getReadableDatabase();
        String[] fields = new String[] {PrefeDataObject.COLUMN_VALUE};
        Cursor c = database.query(PrefeDataObject.TABLE_NAME, fields, PrefeDataObject.COLUMN_NAME + "=" + "'" + name + "'", null, null, null, null);
        String value = null;

        //If there are records
        if (c.moveToFirst()) {
            do {
                value = c.getString(0); //parameter value
            } while(c.moveToNext());
        }
        this.locked = false;
        database.close();
        return value;
    }
}
