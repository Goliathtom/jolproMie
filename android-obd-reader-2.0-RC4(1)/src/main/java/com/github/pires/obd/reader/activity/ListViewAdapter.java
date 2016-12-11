package com.github.pires.obd.reader.activity;

/**
 * Created by TerryOoops on 2016-11-16.
 */

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.pires.obd.reader.R;

import java.text.DecimalFormat;

public class ListViewAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
   // private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;

    // ListViewAdapter의 생성자
    public ListViewAdapter() {
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return staticVar.m_mapPoint.size() ;
    }



    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.tmap_listview_item, parent, false);
        }



        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView nameTextView = (TextView) convertView.findViewById(R.id.tmap_name);
       // Button dirButton = (Button) convertView.findViewById(R.id.direction) ;
        TextView addrTextView = (TextView) convertView.findViewById(R.id.tmap_addr) ;
        TextView priceTextView = (TextView) convertView.findViewById(R.id.tmap_price) ;
        TextView distTextView = (TextView) convertView.findViewById(R.id.tmap_distance) ;
        TextView useTimeTextView = (TextView) convertView.findViewById(R.id.tmap_useTime) ;
        TextView chargerTypeTextView = (TextView) convertView.findViewById(R.id.tmap_chargerType) ;
        TextView statTextView = (TextView) convertView.findViewById(R.id.tmap_stat) ;

        DecimalFormat df1 = new DecimalFormat(".00");
        DecimalFormat df2 = new DecimalFormat(".0");
        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득

        MapPoint listViewItem = staticVar.m_mapPoint.get(position);
        //if(staticVar.m_mapPoint.get(position).getDistance()>10000)
          // listViewItem = null;
        Log.d("List",listViewItem.getName());

        // 아이템 내 각 위젯에 데이터 반영
        //if(listViewItem != null) {
            nameTextView.setText(listViewItem.getName());
            addrTextView.setText(listViewItem.getAddr());
            priceTextView.setText("충전요금 : " + df2.format(listViewItem.getPrice()) + "원/kwh");
            distTextView.setText((df1.format(listViewItem.getDistance() / 1000)) + "km");

            if (listViewItem.getUsetime() == null)
                useTimeTextView.setText("이용시간 : 24시간 이용가능");
            else
                useTimeTextView.setText("이용시간 : " + listViewItem.getUsetime());

            if (listViewItem.getChargerType() == 1)
                chargerTypeTextView.setText("충전기타입 : DC차데모");
            else if (listViewItem.getChargerType() == 3)
                chargerTypeTextView.setText("충전기타입 : DC차데모+AC3상");
            else if (listViewItem.getChargerType() == 6)
                chargerTypeTextView.setText("충전기타입 : DC차데모+AC3상+DC콤보");

            if (listViewItem.getStat() == 1)
                statTextView.setText("충전기상태 : 통신이상");
            else if (listViewItem.getStat() == 2)
                statTextView.setText("충전기상태 : 충전대기");
            else if (listViewItem.getStat() == 3)
                statTextView.setText("충전기상태 : 충전중");
            else if (listViewItem.getStat() == 4)
                statTextView.setText("충전기상태 : 운영중지");
            else if (listViewItem.getStat() == 5)
                statTextView.setText("충전기상태 : 점검중");
       // }

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public MapPoint getItem(int position) {
        return staticVar.m_mapPoint.get(position) ;
    }

    /*// 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String name, String addr, Double distance) {
        ListViewItem item = new ListViewItem();

        item.setName(name);
        item.setAddr(addr);
        item.setDistance(distance);

        listViewItemList.add(item);
    }*/
}
