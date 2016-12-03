package managing;

/***********************************************************/
/*               Managing Point by Total Distance          */
/***********************************************************/


public class ManagingPoint  {
    private int amount_of_point = 0;
    private int point_count = 0;

    public void setPoint(int _point){
        amount_of_point = _point;
    }

    public int getPoint(){
        return amount_of_point;
    }

    public void setPointCount(int _point_count){
        point_count = _point_count;
    }

    public int getPointCount(){
        return point_count;
    }
}
