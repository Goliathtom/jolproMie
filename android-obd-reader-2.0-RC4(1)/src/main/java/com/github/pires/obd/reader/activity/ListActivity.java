package com.github.pires.obd.reader.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.pires.obd.reader.R;

public class ListActivity extends Activity {
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tmap_activity_list);

        listView = (ListView)findViewById(R.id.tmap_listview);
        ListViewAdapter adapter = new ListViewAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // TODO : item click
                    staticVar.mm=staticVar.m_mapPoint.get(position);
                finish();
            }
        }) ;


    }
}
