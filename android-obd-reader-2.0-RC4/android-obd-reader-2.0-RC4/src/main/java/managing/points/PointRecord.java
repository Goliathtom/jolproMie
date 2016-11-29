package managing.points;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Jungki on 2016-11-14.
 */

public class PointRecord {
    /// record id for database use (primary key)
    private Integer id;

    /// the date the point got
    private Date recordDate;

    // Previous amount of Point
    private int prevPoint = 0;

    // Spent amount of Point
    private int spentPoint = 0;

    // Gotten amount of Point
    private int gotPoint = 0;

    // Current amount of Point
    private int currentPoint = 0;

    /**
     * DESCRIPTION:
     * Getter method for the id attribute.
     *
     * @return Integer - the id value.
     */
    public Integer getID() {
        return id;
    }

    /**
     * DESCRIPTION:
     * Setter method for the id attribute.
     *
     * @param id - the Integer id value.
     */
    public void setID(Integer id) {
        this.id = id;
    }

    /**
     * DESCRIPTION:
     * Getter method for the date attribute.
     *
     * @return Date - the end date value
     */
    public Date getEndDate() {
        return recordDate;
    }

    /**
     * DESCRIPTION:
     * Setter method for the date attribute.
     *
     * @param date - the Date value.
     */
    public void setEndDate(Date date) {
        this.recordDate = date;
    }

    /**
     * DESCRIPTION:
     * Getter method for the date attribute as a String value.
     *
     * @return String - the date value (MM/dd/yyyy).
     */
    public String getDateString() {
        //todo
        //return dateFormatter.format(this.startDate);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        return sdf.format(this.recordDate);
    }

    public PointRecord() {
        recordDate = new Date();
    }

    // Control of Previous Points
    public void setprevPoint(int _prevPoint){ prevPoint = _prevPoint; }
    public int getPrevPoint(){ return prevPoint; }

    // Control of Previous Points
    public void setspentPoint(int _spentPoint){ spentPoint = _spentPoint; }
    public int getspentPoint(){ return spentPoint; }

    // Control of Previous Points
    public void setgotPoint(int _gotPoint){ gotPoint = _gotPoint; }
    public int getgotPoint(){ return gotPoint; }

    // Control of Previous Points
    public void setcurrentPoint(){ currentPoint = prevPoint + gotPoint - spentPoint; }
    public int getcurrentPoint(){ return currentPoint; }

}
