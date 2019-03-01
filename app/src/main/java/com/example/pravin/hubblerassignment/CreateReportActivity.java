package com.example.pravin.hubblerassignment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

public class CreateReportActivity extends AppCompatActivity {

    LinearLayout parentLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        parentLinearLayout = findViewById(R.id.linear_layout);

        JSONArray inputJson = convertStringToJson(readJsonInputFromAssets());
        if (inputJson != null)
            buildFormFromJSON(inputJson);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            JSONObject formData = getFormData();

            //formData will be null, when required fields are not given value.
            if (formData != null && formData.length() > 0) {
                //Toast.makeText(this, formData.toString(), Toast.LENGTH_SHORT).show();
                SharedPreferences preference = getSharedPreferences("MyPreference", MODE_PRIVATE);
                String reportListString = preference.getString("ReportList", null);
                SharedPreferences.Editor editor = preference.edit();

                //When there are existing reports, get the existing reports and add the new report
                if (reportListString != null) {
                    try {
                        JSONArray reportList = new JSONArray(reportListString);
                        reportList.put(formData);
                        editor.putString("ReportList", reportList.toString());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //No existing reports. So add the first report to preference storage
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(formData);
                    editor.putString("ReportList", jsonArray.toString());
                }
                editor.commit();
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
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

    private JSONArray convertStringToJson(String jsonString) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    //To build form views based on JSON file
    private void buildFormFromJSON(JSONArray jsonInput) {
        for (int i = 0; i < jsonInput.length(); i++) {
            JSONObject jsonObject = null;
            try {
                jsonObject = jsonInput.getJSONObject(i);
                if (jsonObject != null) {
                    //Type of the widget
                    String type = jsonObject.getString("type");

                    //Label of the widget
                    String fieldName = jsonObject.getString("field-name");

                    LinearLayout linearLayout = null;

                    /*Every form input will have a linear layout followed by textview and the widget specified in the json
                    <LinearLayout>
                        <TextView/>
                        <Dynamic widget created/>
                    </LinearLayout>
                    This linear layout is added to the parent linear layout
                    */
                    linearLayout = new LinearLayout(this);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, 0, 0, 10);
                    linearLayout.setLayoutParams(layoutParams);
                    linearLayout.addView(createTextView(fieldName));
                    linearLayout.setTag(jsonObject); //Used later while retrieving form data

                    switch (type) {
                        case "text":
                        case "number":
                        case "multiline":

                            linearLayout.addView(createEditText(type));
                            parentLinearLayout.addView(linearLayout);
                            break;
                        case "dropdown":
                            linearLayout.addView(createSpinner(jsonObject.getJSONArray("options")));
                            parentLinearLayout.addView(linearLayout);
                            break;

                        default:
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private TextView createTextView(String textviewLabel) {
        TextView textView = new TextView(this);
        textviewLabel = textviewLabel.substring(0,1).toUpperCase() + textviewLabel.substring(1).toLowerCase();
        textView.setText(textviewLabel);
        return textView;
    }

    private EditText createEditText(String type) {
        EditText editText = new EditText(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(layoutParams);
        switch (type) {
            case "number":
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case "multiline":
                editText.setSingleLine(false);
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                break;
            default:
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        return editText;
    }

    private Spinner createSpinner(JSONArray jsonArray) {
        Spinner spinner = new Spinner(this);
        String[] inputData = createStringArrayFromJSONArray(jsonArray);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, inputData);
        spinner.setAdapter(spinnerAdapter);
        return spinner;
    }

    private String[] createStringArrayFromJSONArray(JSONArray jsonArray) {
        String[] stringArray = null;
        if (jsonArray != null) {
            int length = jsonArray.length();
            stringArray = new String[length];
            for (int i = 0; i < length; i++) {
                stringArray[i] = jsonArray.optString(i);
            }
        }
        return stringArray;
    }

    private JSONObject getFormData() {
        JSONObject formInputJson = null;
        if (parentLinearLayout.getChildCount() > 0) {
            formInputJson = new JSONObject();
            for (int i = 0; i < parentLinearLayout.getChildCount(); i++) {
                LinearLayout linearLayout = (LinearLayout) parentLinearLayout.getChildAt(i);

                //In the linear layout, always the first child is the textview which holds the label
                TextView textView = (TextView) linearLayout.getChildAt(0);

                ////In the linear layout, the second view is the dynamic view created
                View view = linearLayout.getChildAt(1);

                String key = textView.getText().toString();
                String value = null;

                JSONObject jsonObject = (JSONObject) linearLayout.getTag();
                String type = null;
                boolean required = false;
                try {
                    type = jsonObject.getString("type");
                    if (jsonObject.has("required"))
                        required = jsonObject.getBoolean("required");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (type) {
                    case "text":
                    case "number":
                    case "multiline":
                        EditText editText = (EditText) view;
                        value = editText.getText().toString();
                        break;

                    case "dropdown":
                        Spinner spinner = (Spinner) view;
                        value = spinner.getSelectedItem().toString();
                        break;
                }

                //If the widget has required = true and if the value is not provided, show a message
                if (jsonObject.has("required") && required && (value == null || value.isEmpty())) {
                    Toast.makeText(this, key + " is mandatory", Toast.LENGTH_SHORT).show();
                    return null;
                }
                try {
                    formInputJson.put(key, value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return formInputJson;
    }

}
