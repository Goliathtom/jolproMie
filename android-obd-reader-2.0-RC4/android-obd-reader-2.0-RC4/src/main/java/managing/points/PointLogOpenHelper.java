package managing.points;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Jungki on 2016-11-14.
 */

public class PointLogOpenHelper extends SQLiteOpenHelper {
    /// tag for logging
    private static final String TAG = PointLogOpenHelper.class.getName();

    public PointLogOpenHelper(Context context) {
        super(context, PointLog.DATABASE_NAME, null, PointLog.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        execSQL(db, PointLog.DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void execSQL(SQLiteDatabase db, String[] statements) {
        final String tag = TAG + ".execSQL()";
        for (String sql : statements) {
            Log.d(tag, sql);
            db.execSQL(sql);
        }
    }
}
