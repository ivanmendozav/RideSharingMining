package lib.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ivan on 08/05/2015.
 */
public class AppSQLiteHelper extends SQLiteOpenHelper {
    public static final String SQL_DATABASE_NAME = "ride_mining.db";
    public static final int SQL_DATABASE_VERSION = 3;
    public static final String SQL_COLUMN_ID = "_id";


    public static final String SQL_CREATION_SCRIPT =
            //Gps sensor data table
            "create table if not exists " + GpsDataObject.TABLE_NAME + " ("
            + SQL_COLUMN_ID + " integer primary key autoincrement, "
            + GpsDataObject.COLUMN_LONGITUDE + " real not null, "
            + GpsDataObject.COLUMN_LATITUDE + " real not null, "
            + GpsDataObject.COLUMN_ALTITUDE + " real not null, "
            + GpsDataObject.COLUMN_TIMESTAMP + " integer not null);";

    public static final String SQL_CREATION_SCRIPT_2 =
            "create table if not exists " + StayPointDataObject.TABLE_NAME + " ("
            + SQL_COLUMN_ID + " integer primary key autoincrement, "
            + StayPointDataObject.COLUMN_AVG_LATITUDE + " real not null, "
            + StayPointDataObject.COLUMN_AVG_LONGITUDE + " real not null, "
            + StayPointDataObject.COLUMN_ARRIVAL + " integer not null, "
            + StayPointDataObject.COLUMN_DEPARTURE + " integer not null,"
            + StayPointDataObject.COLUMN_CARDINALITY + " integer not null,"
            + StayPointDataObject.COLUMN_LABEL + " string not null,"
            + StayPointDataObject.COLUMN_START_LATITUDE + " real not null, "
            + StayPointDataObject.COLUMN_START_LONGITUDE + " real not null);";

    public AppSQLiteHelper(Context context) {
        super(context, SQL_DATABASE_NAME, null, SQL_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATION_SCRIPT);
        db.execSQL(SQL_CREATION_SCRIPT_2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + GpsDataObject.TABLE_NAME);
        onCreate(db);
    }
}
