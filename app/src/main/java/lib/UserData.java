package lib;

import android.content.Context;

import lib.db.PrefeDataObject;

/**
 * Created by Ivan on 22/05/2015.
 */
public class UserData {
    public synchronized static String Get(Context context, String name) {
        PrefeDataObject preferences = PrefeDataObject.GetInstance(context);
        return preferences.Get(name);
    }
    public synchronized static void Set(Context context, String name, String value) {
        PrefeDataObject preferences = PrefeDataObject.GetInstance(context);
        preferences.Set(name, value);
    }
}
