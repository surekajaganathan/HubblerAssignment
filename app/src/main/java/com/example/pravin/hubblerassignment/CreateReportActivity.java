package com.example.pravin.hubblerassignment;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CreateReportActivity extends AppCompatActivity implements CreateReportFragment.OnFragmentInteractionListener {

    LinearLayout parentLinearLayout;
    FragmentManager fragmentManager;
    public static JSONArray inputJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();

        String inputJsonAsString = readJsonInputFromAssets();
        inputJson = convertStringToJson(inputJsonAsString);
        if (inputJsonAsString != null)
            adddFragment();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void adddFragment() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        CreateReportFragment reportFragment = CreateReportFragment.newInstance(null);
        transaction.add(R.id.fragment_container, reportFragment);
        transaction.commit();
    }

    private JSONArray convertStringToJson(String jsonString) {
        JSONArray jsonArray = null;
        try {
            if (jsonString != null)
                jsonArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    //Reads the JSON file from asset folder and returns the data as String
    private String readJsonInputFromAssets() {
        String json = null;
        try {
            InputStream inputStream = getAssets().open("input.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public void onBackPressed() {
        int count = fragmentManager.getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {
            //When back button is pressed, remove the composite key that belongs to that level
            if (CreateReportFragment.jsonCompositeKeys != null && CreateReportFragment.jsonCompositeKeys.size() > 0)
                CreateReportFragment.jsonCompositeKeys.remove(CreateReportFragment.jsonCompositeKeys.size() - 1);
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onFragmentInteraction(ArrayList<String> jsonCompositeKeys) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        CreateReportFragment reportFragment = CreateReportFragment.newInstance(jsonCompositeKeys);
        transaction.replace(R.id.fragment_container, reportFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
