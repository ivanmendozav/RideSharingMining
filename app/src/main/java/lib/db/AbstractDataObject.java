package lib.db;

import android.content.Context;

/**
 * Created by Ivan on 12/05/2015.
 */
public abstract class AbstractDataObject {
    protected boolean locked = false;
    protected AppSQLiteHelper helper;

    /**
     * Construct. Start connection
     * @param c
     */
    public AbstractDataObject(Context c){
        this.helper = new AppSQLiteHelper(c);
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
