package com.github.pires.obd.reader.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.pires.obd.reader.R;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapGpsManager;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;



public class TmapActivity extends Activity implements TMapGpsManager.onLocationChangedCallback {
    Document doc = null;
    private Context mContext = null;
    private boolean m_bTrackingMode = true;

    private GpsInfo gps;

    private TMapGpsManager tmapgps = null;
    private TMapView tmapview = null;
    private static String mApiKey = "5d8fea47-699f-3d33-8a53-068e253fd885";
    private static int mMakerID;

    private double curLatitude;
    private double curLongitude;

    private static final String parsing_url = "http://open.ev.or.kr:8080/openapi/services/rest/EvChargerService?serviceKey=xhB%2Fk%2Fd7bWDAwUcHWG1ldntzamhmKo7BV764oZ%2BcVZESj29TTU1d3dPiieO412ft59el8byBuwdaygyY8T7JWQ%3D%3D";
    //파싱결과를 담는 리스트

    private ArrayList<TMapPoint> m_tmapPoint = new ArrayList<TMapPoint>();
    private ArrayList<String> mArrayMakerID = new ArrayList<String>();

    @Override
    public void onLocationChange(Location location) {
        if (m_bTrackingMode) {
            tmapview.setLocationPoint(location.getLongitude(), location.getLatitude());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(staticVar.mm!=null) {
            searchRoute(new TMapPoint(curLatitude, curLongitude), new TMapPoint(staticVar.mm.getLatitude(), staticVar.mm.getLongitude()));
            //showSearchMarkerPoint();
            staticVar.mm=null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tmap_activity_main);

        mContext = this;

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.tmap_mapview);
        tmapview = new TMapView(this);
        linearLayout.addView(tmapview);
        tmapview.setSKPMapApiKey(mApiKey);

        /* 현재 보는 방향 */
       // tmapview.setCompassMode(true);

        /* 현위치 아이콘표시 */
        //tmapview.setIconVisibility(true);

        /* 줌레벨 */
        tmapview.setZoomLevel(12);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);

        /* gps */
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        gps = new GpsInfo(TmapActivity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {

            curLatitude = gps.getLatitude();
            curLongitude = gps.getLongitude();
            tmapview.setCenterPoint(curLongitude, curLatitude);
            Toast.makeText(getApplicationContext(), "위치 불러오기 성공", Toast.LENGTH_SHORT).show();
            showGpsPoint();
            //staticVar.m_mapPoint.add(new MapPoint("현재위치", curLatitude, curLongitude, "", "",0));
        }
        else
            Toast.makeText(getApplicationContext(), "위치를 불러오지 못했습니다. 다시한번 실행해주세요", Toast.LENGTH_SHORT).show();

        GetXMLTask task = new GetXMLTask();
        task.execute(parsing_url);

        // tmapview.setTrackingMode(true);

        tmapview.setSightVisible(true);

        tmapview.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(TMapMarkerItem markerItem) {
                searchRoute(new TMapPoint(curLatitude, curLongitude), markerItem.getTMapPoint());
            }
        });
    }

    public void onClick_button(View v){
        Intent intent_01 = new Intent(TmapActivity.this, ListActivity.class);
        startActivity(intent_01);
    }

    private class GetXMLTask extends AsyncTask<String, Void, Document> {

        @Override
        protected Document doInBackground(String... urls) {
            URL url;
            try {
                url = new URL(urls[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder(); //XML문서 빌더 객체를 생성
                doc = db.parse(new InputSource(url.openStream())); //XML문서를 파싱한다.
                doc.getDocumentElement().normalize();

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Parsing Error", Toast.LENGTH_SHORT).show();
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document doc) {

            TMapPOIItem tMapPOIItem = new TMapPOIItem();

            String s = "";
            //data태그가 있는 노드를 찾아서 리스트 형태로 만들어서 반환
            NodeList nodeList = doc.getElementsByTagName("item");
            //data 태그를 가지는 노드를 찾음, 계층적인 노드 구조를 반환

            for(int i = 0; i< nodeList.getLength(); i++){
                EvChargerItem evChargerItem = new EvChargerItem();
                // 데이터를 추출
                Node node = nodeList.item(i); //data엘리먼트 노드
                Element fstElmnt = (Element) node;
                NodeList statId  = fstElmnt.getElementsByTagName("statId");
                NodeList statNm   = fstElmnt.getElementsByTagName("statNm");
                NodeList chgerId   = fstElmnt.getElementsByTagName("chgerId");
                NodeList chgerType   = fstElmnt.getElementsByTagName("chgerType");
                NodeList stat = fstElmnt.getElementsByTagName("stat");
                NodeList lat = fstElmnt.getElementsByTagName("lat");
                NodeList lng  = fstElmnt.getElementsByTagName("lng");
                NodeList addr  = fstElmnt.getElementsByTagName("addrDoro");
                NodeList useTime  = fstElmnt.getElementsByTagName("useTime");

                evChargerItem.setStatId(statId.item(0).getChildNodes().item(0).getNodeValue());
                evChargerItem.setStatNm(statNm.item(0).getChildNodes().item(0).getNodeValue());
                evChargerItem.setChgerId(chgerId.item(0).getChildNodes().item(0).getNodeValue());
                evChargerItem.setChgerType(Integer.parseInt(chgerType.item(0).getChildNodes().item(0).getNodeValue()));
                if (stat.getLength() != 0)
                    evChargerItem.setStat(Integer.parseInt(stat.item(0).getChildNodes().item(0).getNodeValue()));
                evChargerItem.setLat(Double.parseDouble(lat.item(0).getChildNodes().item(0).getNodeValue()));
                evChargerItem.setLng(Double.parseDouble(lng.item(0).getChildNodes().item(0).getNodeValue()));
                evChargerItem.setAddr(addr.item(0).getChildNodes().item(0).getNodeValue());
                if (useTime.getLength() != 0)
                    evChargerItem.setUserTime(useTime.item(0).getChildNodes().item(0).getNodeValue());

                //staticVar.mList.add(evChargerItem);
                tMapPOIItem.noorLat = lat.item(0).getChildNodes().item(0).getNodeValue();
                tMapPOIItem.noorLon = lng.item(0).getChildNodes().item(0).getNodeValue();

                //객체생성
                Random random = new Random();
                //x에는 0~5 사이의 정수가 담김
                int a = random.nextInt(40);
                double b = Math.random();



                //Log.d("Log: ", evChargerItem.toString()+ staticVar.mList.size());

                staticVar.m_mapPoint.add(new MapPoint(evChargerItem.getStatNm(), evChargerItem.getLat(), evChargerItem.getLng(), evChargerItem.getUserTime(), evChargerItem.getAddr(),tMapPOIItem.getDistance(new TMapPoint(curLatitude, curLongitude)), evChargerItem.getChgerType(), evChargerItem.getStat(), 280+a+b ));
                System.out.println(i+1+" :거리계산 :"+tMapPOIItem.getDistance(new TMapPoint(curLatitude, curLongitude)));
                Collections.sort(staticVar.m_mapPoint, new Comparator<MapPoint>(){
                    public int compare(MapPoint obj1, MapPoint obj2)
                    {
                        // TODO Auto-generated method stub
                        return (obj1.getDistance() < obj2.getDistance()) ? -1: (obj1.getDistance() > obj2.getDistance()) ? 1:0 ;
                    }
                });

            }
            for(int i=0; i<staticVar.m_mapPoint.size(); i++) {
                System.out.println("맵포인트 "+(i+1)+" :"+staticVar.m_mapPoint.get(i).getName()+", 거리 : "+staticVar.m_mapPoint.get(i).getDistance());
            }
            showMarkerPoint();

            super.onPostExecute(doc);
        }


    }//end inner class - GetXMLTask

    /* 길찾기 함수*/
    private void searchRoute(TMapPoint start, TMapPoint end){
            TMapData data = new TMapData();
        data.findPathData(start, end, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(final TMapPolyLine path) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DecimalFormat df = new DecimalFormat(".00");
                        path.setLineWidth(15);
                        path.setLineColor(Color.BLUE);
                        tmapview.addTMapPath(path);
                        Toast.makeText(getApplicationContext(), df.format(path.getDistance()/1000)+"km", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /* 맵포인트(마커) 지도에 보이기*/
    public void showMarkerPoint(){
        for(int i=0; i<staticVar.m_mapPoint.size(); i++){
            TMapPoint point = new TMapPoint(staticVar.m_mapPoint.get(i).getLatitude(), staticVar.m_mapPoint.get(i).getLongitude());
            TMapMarkerItem item1 = new TMapMarkerItem();
            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.poi_dot);

            item1.setTMapPoint(point);
            item1.setName(staticVar.m_mapPoint.get(i).getName());
            item1.setVisible(item1.VISIBLE);

            item1.setIcon(bitmap);

            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.poi_dot);

            item1.setCalloutTitle(staticVar.m_mapPoint.get(i).getName());
            item1.setCalloutSubTitle(staticVar.m_mapPoint.get(i).getAddr());
            item1.setCanShowCallout(true);
            item1.setAutoCalloutVisible(true);

            Bitmap bitmap_i = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.i_go);

            item1.setCalloutRightButtonImage(bitmap_i);

            String strID = String.format("pmarkerd%d", mMakerID++);

            tmapview.addMarkerItem(strID, item1);
            mArrayMakerID.add(strID);
        }
    }

    /* 현재 위치 지도에 보이기*/
    public void showGpsPoint(){

            TMapPoint point = new TMapPoint(curLatitude, curLongitude);
            TMapMarkerItem item1 = new TMapMarkerItem();
            Bitmap bitmap = null;
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.location);

            item1.setTMapPoint(point);
            item1.setVisible(item1.VISIBLE);
            item1.setIcon(bitmap);

            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.location);

            item1.setCalloutTitle("현재위치");
            item1.setCanShowCallout(true);
            item1.setAutoCalloutVisible(true);

            Bitmap bitmap_i = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.i_go);

            item1.setCalloutRightButtonImage(bitmap_i);

            String strID = String.format("pmarkerd%d", mMakerID++);

            tmapview.addMarkerItem(strID, item1);
            mArrayMakerID.add(strID);
        }

}
