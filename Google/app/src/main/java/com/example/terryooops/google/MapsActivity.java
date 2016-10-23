package com.example.terryooops.google;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    //파싱할 주소
    private static final String parsing_url = "http://open.ev.or.kr:8080/openapi/services/rest/EvChargerService?serviceKey=RYcrXC2hSdE0Xikh9L96%2BEt0%2FoG7vcg7nLsFfyFPlmvlBVUmU32TxSt%2BUMqgLKG6oyOQjuClxyLmEwcP8jSZbw%3D%3D";
    //파싱결과를 담는 리스트
    private List<EvChargerItem> mList = new ArrayList<EvChargerItem>();

    AQuery aq;
    //MyAdapter myAdapter;

    private Button btnShowLocation;
    private TextView txtLat;
    private TextView txtLon;


    private GoogleMap mMap;
    private GpsInfo gps;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aq = new AQuery(this);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        gps = new GpsInfo(MapsActivity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
        }

        mapFragment.getMapAsync(this);

        setData();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng seoul = new LatLng(37.5666103, 126.9783882);
//        LatLng seou = new LatLng(37.5666000, 126.9783882);

        mMap.addMarker(new MarkerOptions().position(seoul).title("Marker in seoul"));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 15));



  //      mMap.addMarker(new MarkerOptions().position(seou).title(Integer.toString(mList.size())));
        //LatLng a = new LatLng(mList.get(1).getLat(), mList.get(1).getLng());
        //mMap.addMarker(new MarkerOptions().position(a).title("Marker in ev"));
    }


    //MyAdapter myAdapter;
    private void setData() {
        mList.clear();

        //aq.ajax() -  비동기적으로 데이터를 통신하고자 할때 사용(부분적 업데이터가 가능)
        aq.ajax(parsing_url, XmlDom.class, new AjaxCallback<XmlDom>() {
            @Override
            public void callback(String url, XmlDom xml, AjaxStatus status) {
                if (xml != null) {

                    List<XmlDom> items = xml.tags("item");


                    //XML로 가져와서 DOM 에서 하나씩 끄집어 내서 for문으로 돌리기
                    for (XmlDom item : items) {
                        EvChargerItem evChargerItem = new EvChargerItem();
                        String statId = item.text("statId");
                        String statNm = item.text("statNm");
                        String chgerId = item.text("chgerId");
                        String chgerType = item.text("chgerType");
                        String stat = item.text("stat");
                        String lat = item.text("lat");
                        String lng = item.text("lng");
                        String addr = item.text("addrDoro");
                        String useTime = item.text("useTime");

                        evChargerItem.setStatId(statId);
                        evChargerItem.setStatNm(statNm);
                        evChargerItem.setChgerId(chgerId);
                        evChargerItem.setChgerType(Integer.parseInt(chgerType));
                        if (stat != null)
                            evChargerItem.setStat(Integer.parseInt(stat));
                        evChargerItem.setLat(Double.parseDouble(lat));
                        evChargerItem.setLng(Double.parseDouble(lng));
                        evChargerItem.setAddr(addr);
                        evChargerItem.setUserTime(useTime);

                        Log.d("KTH", evChargerItem.toString()+mList.size());

                        mList.add(evChargerItem);
                        //그리고 리스트뷰에 연동된 리스트에 하나씩 담기
                        //System.out.print("dddddddddddddddd: "+mList.size());

                        mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng))).title(statNm).snippet("chager type : "+chgerType+" || userTime : "+useTime));
                    }

                   // myAdapter.notifyDataSetChanged();
                    //마지막으로 다 담으면 어뎁터를 갱신
                }
            }
        });
    }


}
