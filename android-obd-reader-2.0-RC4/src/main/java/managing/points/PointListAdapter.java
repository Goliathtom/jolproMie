package managing.points;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.pires.obd.reader.R;
import managing.points.PointRecord;

import java.util.Date;
import java.util.List;

public class PointListAdapter extends ArrayAdapter<PointRecord> {
    /// the Android Activity owning the ListView
    private final Activity activity;

    /// a list of trip records for display
    private final List<PointRecord> records;

    /**
     * DESCRIPTION:
     * Constructs an instance of TripListAdapter.
     *
     * @param activity - the Android Activity instance that owns the ListView.
     * @param records  - the List of TripRecord instances for display in the ListView.
     */
    public PointListAdapter(Activity activity, List<PointRecord> records) {
        super(activity, R.layout.row_point_list, records);
        this.activity = activity;
        this.records = records;
    }

    /**
     * DESCRIPTION:
     * Constructs and populates a View for display of the TripRecord at the index
     * of the List specified by the position parameter.
     *
     * @see ArrayAdapter#getView(int, View, ViewGroup)
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {

        // create a view for the row if it doesn't already exist
        if (view == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            view = inflater.inflate(R.layout.row_point_list, null);
        }

        // get widgets from the view
        TextView pointDate = (TextView) view.findViewById(R.id.pointDate);
       // TextView columnDuration = (TextView) view.findViewById(R.id.columnDuration);
        TextView rowPrevPoint = (TextView) view.findViewById(R.id.rowPrevPoint);
        TextView rowGotPoint = (TextView) view.findViewById(R.id.rowGotPoint);
        TextView rowSpentPoint = (TextView) view.findViewById(R.id.rowSpentPoint);
        TextView rowCurPoint = (TextView) view.findViewById(R.id.rowCurPoint);

        // populate row widgets from record data
        PointRecord record = records.get(position);

        // date
        pointDate.setText(record.getDateString());
        //columnDuration.setText(calcDiffTime(record.getStartDate(), record.getEndDate()));

        String PrevPoint = String.valueOf(record.getPrevPoint());
        String GotPoint = String.valueOf(record.getgotPoint());
        String SpentPoint = String.valueOf(record.getspentPoint());
        String CurPoint = String.valueOf(record.getcurrentPoint());

        rowPrevPoint.setText("Previous Point: " + PrevPoint + "\tPoints");
        rowGotPoint.setText("Got Point: " + GotPoint + "\tPoints");
        rowSpentPoint.setText("Spent Point: " + SpentPoint + "\tPoints");
        rowCurPoint.setText("Current Point: " + CurPoint + "\tPoints");

        return view;
    }

    private String calcDiffTime(Date start, Date end) {
        long diff = end.getTime() - start.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        StringBuffer res = new StringBuffer();

        if (diffDays > 0)
            res.append(diffDays + "d");

        if (diffHours > 0) {
            if (res.length() > 0) {
                res.append(" ");
            }
            res.append(diffHours + "h");
        }

        if (diffMinutes > 0) {
            if (res.length() > 0) {
                res.append(" ");
            }
            res.append(diffMinutes + "m");
        }

        if (diffSeconds > 0) {
            if (res.length() > 0) {
                res.append(" ");
            }

            res.append(diffSeconds + "s");
        }
        return res.toString();
    }

    /**
     * DESCRIPTION:
     * Called by parent when the underlying data set changes.
     *
     * @see ArrayAdapter#notifyDataSetChanged()
     */
    @Override
    public void notifyDataSetChanged() {

        // configuration may have changed - get current settings
        //todo
        //getSettings();

        super.notifyDataSetChanged();
    }
}
