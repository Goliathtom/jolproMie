package managing.points;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Jungki on 2016-11-14.
 */

public class PointLog {

    /// the database version number
    public static final int DATABASE_VERSION = 2;
    /// the name of the database
    public static final String DATABASE_NAME = "pointLog_test5.db";
    /// a tag string for debug logging (the name of this class)
    private static final String TAG = PointLog.class.getName();
    /// database table names
    private static final String RECORDS_TABLE = "Point";
    /// SQL commands to delete the database
    public static final String[] DATABASE_DELETE = new String[]{
            "drop table if exists " + RECORDS_TABLE + ";",
    };

    /// column names for RECORDS_TABLE
    private static final String RECORD_ID = "id";
    private static final String RECORD_DATE = "recordDate";
    private static final String RECORD_PREV_POINT = "prevPoint";
    private static final String RECORD_SPENT_POINT = "spentPoint";
    private static final String RECORD_GOT_POINT = "gotPoint";
    private static final String RECORD_CUR_POINT = "currentPoint";
    /// SQL commands to create the database
    public static final String[] DATABASE_CREATE = new String[]{
            "create table " + RECORDS_TABLE + " ( " +
                    RECORD_ID + " integer primary key autoincrement, " +
                    RECORD_DATE + " integer not null, " +
                    RECORD_PREV_POINT + " integer, " +
                    RECORD_SPENT_POINT + " integer, " +
                    RECORD_GOT_POINT + " integer, " +
                    RECORD_CUR_POINT + " integer" +
                    ");"
    };
    /// array of all column names for RECORDS_TABLE
    private static final String[] RECORDS_TABLE_COLUMNS = new String[]{
            RECORD_ID,
            RECORD_DATE,
            RECORD_PREV_POINT,
            RECORD_SPENT_POINT,
            RECORD_GOT_POINT,
            RECORD_CUR_POINT
    };

    /// singleton instance
    private static PointLog instance;
    /// context of the instance creator
    private final Context context;
    /// a helper instance used to open and close the database
    private final PointLogOpenHelper helper;
    /// the database
    private final SQLiteDatabase db;
    //
    private int saved_Point = 0;

    private PointLog(Context context) {
        this.context = context;
        this.helper = new PointLogOpenHelper(this.context);
        this.db = helper.getWritableDatabase();
    }

    /**
     * DESCRIPTION:
     * Returns a single instance, creating it if necessary.
     *
     * @return GasLog - singleton instance.
     */
    public static PointLog getInstance(Context context) {
        if (instance == null) {
            instance = new PointLog(context);
        }
        return instance;
    }

    /**
     * DESCRIPTION:
     * Convenience method to test assertion.
     *
     * @param assertion - an asserted boolean condition.
     * @param tag       - a tag String identifying the calling method.
     * @param msg       - an error message to display/log.
     * @throws RuntimeException if the assertion is false
     */
    private void ASSERT(boolean assertion, String tag, String msg) {
        if (!assertion) {
            String assert_msg = "ASSERT failed: " + msg;
            Log.e(tag, assert_msg);
            throw new RuntimeException(assert_msg);
        }
    }

    public PointRecord startTrip() {
        final String tag = TAG + ".createRecord()";

        try {
            PointRecord record = new PointRecord();
            long rowID = db.insertOrThrow(RECORDS_TABLE, null, getContentValues(record));
            record.setID((int) rowID);
            return record;
        } catch (SQLiteConstraintException e) {
            Log.e(tag, "SQLiteConstraintException: " + e.getMessage());
        } catch (SQLException e) {
            Log.e(tag, "SQLException: " + e.getMessage());
        }
        return null;
    }
    /**
     * DESCRIPTION:
     * Updates a trip record in the log.
     *
     * @param record - the PointRecords to update.
     * @return boolean flag indicating success/failure (true=success)
     */
    public boolean updateRecord(PointRecord record) {
        final String tag = TAG + ".updateRecord()";
        ASSERT((record.getID() != null), tag, "record id cannot be null");
        boolean success = false;
        try {
            ContentValues values = getContentValues(record);
            values.remove(RECORD_ID);
            String whereClause = RECORD_ID + "=" + record.getID();
            int count = db.update(RECORDS_TABLE, values, whereClause, null);
            success = (count > 0);
        } catch (SQLiteConstraintException e) {
            Log.e(tag, "SQLiteConstraintException: " + e.getMessage());
        } catch (SQLException e) {
            Log.e(tag, "SQLException: " + e.getMessage());
        }
        return success;
    }

    /**
     * DESCRIPTION:
     * Convenience method to convert a PointRecords instance to a set of key/value
     * pairs in a ContentValues instance utilized by SQLite access methods.
     *
     * @param record - the GasRecord to convert.
     * @return a ContentValues instance representing the specified GasRecord.
     */
    private ContentValues getContentValues(PointRecord record) {
        ContentValues values = new ContentValues();
        values.put(RECORD_ID, record.getID());
        if (record.getEndDate() != null)
            values.put(RECORD_DATE, record.getEndDate().getTime());
        record.setprevPoint(saved_Point);
        values.put(RECORD_PREV_POINT, record.getPrevPoint());
        values.put(RECORD_SPENT_POINT, record.getspentPoint());
        values.put(RECORD_GOT_POINT, record.getgotPoint());
        values.put(RECORD_CUR_POINT, record.calccurrentPoint());

        return values;
    }

    public List<PointRecord> readAllRecords() {

        //update();

        final String tag = TAG + ".readAllRecords()";
        List<PointRecord> list = new ArrayList<>();
        Cursor cursor = null;

        try {
            String orderBy = RECORD_DATE;
            cursor = db.query(
                    RECORDS_TABLE,
                    RECORDS_TABLE_COLUMNS,
                    null,
                    null, null, null,
                    orderBy,
                    null
            );

            // create a list of PointRecords from the data
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        PointRecord record = getRecordFromCursor(cursor);
                        list.add(record);
                    } while (cursor.moveToNext());
                }
            }

        } catch (SQLException e) {
            Log.e(tag, "SQLException: " + e.getMessage());
            list.clear();
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }
    /**
     * DESCRIPTION:
     * Deletes a specified trip record from the log.
     *
     * @param id - the TripRecord to delete.
     * @return boolean flag indicating success/failure (true=success)
     */
    public boolean deleteTrip(long id) {

        final String tag = TAG + ".deleteRecord()";

        boolean success = false;

        try {
            String whereClause = RECORD_ID + "=" + id;
            String[] whereArgs = null;
            int count = db.delete(RECORDS_TABLE, whereClause, whereArgs);
            success = (count == 1);
        } catch (SQLException e) {
            Log.e(tag, "SQLException: " + e.getMessage());
        }

        return success;
    }
    /**
     * DESCRIPTION:
     * Convenience method to create a PointRecords instance from values read
     * from the database.
     *
     * @param c - a Cursor containing results of a database query.
     * @return a GasRecord instance (null if no data).
     */
    private PointRecord getRecordFromCursor(Cursor c) {
        final String tag = TAG + ".getRecordFromCursor()";
        PointRecord record = null;
        if (c != null) {
            record = new PointRecord();
            int cursor_position = c.getPosition();
            int id = c.getInt(c.getColumnIndex(RECORD_ID));
            long endTime = c.getLong(c.getColumnIndex(RECORD_DATE));
            int spent_point = c.getInt(c.getColumnIndex(RECORD_SPENT_POINT));
            int got_point = c.getInt(c.getColumnIndex(RECORD_GOT_POINT));
            int cur_point = c.getInt(c.getColumnIndex(RECORD_CUR_POINT));
            record.setID(id);
            record.setEndDate(new Date(endTime));

            if(cursor_position == 0){
                record.setprevPoint(0);
            }else{
                c.moveToPrevious();
                saved_Point = saved_Point+ got_point - spent_point;
                record.setprevPoint(saved_Point);
                c.moveToNext();
            }
            record.setspentPoint(spent_point);
            record.setgotPoint(got_point);
            record.setcurrentPoint(record.calccurrentPoint());
        }
        return record;
    }

}

