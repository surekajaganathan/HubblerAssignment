package com.example.pravin.hubblerassignment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class ReportListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView tvReportCount;
    LinearLayoutManager linearLayoutManager;
    ReportListAdapter reportListAdapter;
    ArrayList<DefaultModelClass> reportList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.rv_report_list);
        tvReportCount = findViewById(R.id.tv_report_count);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ReportListActivity.this, CreateReportActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preference = getSharedPreferences("MyPreference", MODE_PRIVATE);
        String reportListString = preference.getString("ReportList", null);
        int reportCount = 0;

        if (reportListString != null) {
            try {
                JSONArray jsonArray = new JSONArray(reportListString);
                convertJsonArrayToArrayList(jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (reportList != null) {
                reportListAdapter = new ReportListAdapter(reportList, this);
                recyclerView.setAdapter(reportListAdapter);
                reportCount = reportList.size();
            }
            tvReportCount.setText("Total Reports " + reportCount);
        }
    }

    private void convertJsonArrayToArrayList(JSONArray jsonArray) {
        reportList = new ArrayList<>();
        DefaultModelClass modelClass = null;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                modelClass = new DefaultModelClass();
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                Iterator<?> keys = jsonObject.keys();
                int counter = 0;
                while (keys.hasNext() && ++counter <= 2) {

                    String key = (String) keys.next();
                    String value = (String) jsonObject.get(key);

                    if (counter == 1) {
                        modelClass.setKey1(key);
                        modelClass.setValue1(value);
                    } else {
                        modelClass.setKey2(key);
                        modelClass.setValue2(value);
                    }
                }
                reportList.add(modelClass);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
