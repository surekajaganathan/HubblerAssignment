package com.example.pravin.hubblerassignment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateReportFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateReportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateReportFragment extends Fragment {

    private Context context;
    //private String inputString;
    public static ArrayList<String> jsonCompositeKeys;
    private String mParam2;
    private JSONArray inputJsonArray;
    private OnFragmentInteractionListener mListener;
    private LinearLayout parentLinearLayout;
    FragmentManager fragmentManager;

    public CreateReportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CreateReportFragment.
     */
    public static CreateReportFragment newInstance(ArrayList<String> jsonKeys) {
        CreateReportFragment fragment = new CreateReportFragment();
        jsonCompositeKeys = jsonKeys;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentManager = getActivity().getSupportFragmentManager();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_report, container, false);
        parentLinearLayout = view.findViewById(R.id.linear_layout);
        context = getContext();

        inputJsonArray = CreateReportActivity.inputJson; //This contains the entire JSON
        /*jsonCompositeKeys - Has the track of composite keys from the parent level.
        This gets filled whenever a new report screen is opened for the composite key.
         */

        //The below loop parses the main parent json to get the sub json based on the composite key
        if (jsonCompositeKeys != null && jsonCompositeKeys.size() > 0) {
            for (String string : jsonCompositeKeys) {
                JSONObject obj = getDesiredJsonObject(inputJsonArray, string);
                try {
                    inputJsonArray = obj.getJSONArray("fields");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            //it is parent level, use the main json as it is
        }

        if (inputJsonArray != null)
            buildFormFromJSON(inputJsonArray);

        /*if (inputString != null) {
            JSONArray jsonArray = convertStringToJson(inputString);
            if (jsonArray != null)
                buildFormFromJSON(jsonArray);
        }*/
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_create_report, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            JSONObject formData = getFormData(true);
            //formData will be null, when required fields are not given value.
            if (formData != null) {
                int fragmentCount = fragmentManager.getBackStackEntryCount();
                //Toast.makeText(this, formData.toString(), Toast.LENGTH_SHORT).show();
                SharedPreferences preference = getActivity().getSharedPreferences("MyPreference", MODE_PRIVATE);
                String reportListString = preference.getString("ReportList", null);
                SharedPreferences.Editor editor = preference.edit();

                if (fragmentCount == 0) {
                    //When there are existing reports, get the existing reports and add the new report
                    jsonCompositeKeys = null;
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
                    getActivity().finish();

                } else {
                    goToPreviousFragment();
                }
            }

        }
        return super.onOptionsItemSelected(item);
    }

    private void goToPreviousFragment() {
        if (jsonCompositeKeys != null && jsonCompositeKeys.size() > 0)
            jsonCompositeKeys.remove(jsonCompositeKeys.size() - 1);
        fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStack();
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

                    String value = null;
                    if (jsonObject.has("value"))
                        value = jsonObject.getString("value");

                    LinearLayout linearLayout = null;

                    /*Every form input will have a linear layout followed by textview and the widget specified in the json
                    <LinearLayout>
                        <TextView/>
                        <Dynamic widget created/>
                    </LinearLayout>
                    This linear layout is added to the parent linear layout
                    */
                    linearLayout = new LinearLayout(context);
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

                            linearLayout.addView(createEditText(type, value));
                            parentLinearLayout.addView(linearLayout);
                            break;
                        case "dropdown":
                            linearLayout.addView(createSpinner(jsonObject.getJSONArray("options"), value));
                            parentLinearLayout.addView(linearLayout);
                            break;

                        case "composite":
                            linearLayout.addView(createButton(fieldName));
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
        TextView textView = new TextView(context);
        textviewLabel = textviewLabel.substring(0, 1).toUpperCase() + textviewLabel.substring(1).toLowerCase();
        textView.setText(textviewLabel);
        return textView;
    }

    private EditText createEditText(String type, String value) {
        EditText editText = new EditText(context);
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
        editText.setText(value);
        return editText;
    }

    private Spinner createSpinner(JSONArray jsonArray, String value) {
        Spinner spinner = new Spinner(context);
        String[] inputData = createStringArrayFromJSONArray(jsonArray);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, inputData);
        spinner.setAdapter(spinnerAdapter);
        if (value != null && inputData.length > 0) {
            for (int idx = 0; idx < inputData.length; idx++) {
                if (inputData[idx].equalsIgnoreCase(value)) {
                    spinner.setSelection(idx);
                    break;
                }
            }
        }
        return spinner;
    }

    private Button createButton(final String fieldName) {
        Button button = new Button(context);
        button.setText("Add");
        button.setBackground(null);
        button.setTextColor(getResources().getColor(R.color.colorPrimary));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        button.setLayoutParams(params);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jsonCompositeKeys == null) {
                    jsonCompositeKeys = new ArrayList<>();
                }

                //This collects the data entered in the current form and saves in the parent json against key "value"
                getFormData(false);

                //To traverse to the inner json, store the json key which has the composite fields
                jsonCompositeKeys.add(fieldName);

                onButtonPressed(jsonCompositeKeys);
            }
        });
        return button;
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

    //To get the nested json from the main json based on key
    private JSONObject getDesiredJsonObject(JSONArray array, String key) {
        for (int idx = 0; idx < array.length(); idx++) {
            JSONObject object = null;
            try {
                object = (JSONObject) array.get(idx);
                if (object.getString("field-name").equalsIgnoreCase(key)
                        && object.getString("type").equalsIgnoreCase("composite")) {
                    return object;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    private JSONObject getFormData(boolean isSave) {
        JSONObject formResultJson = null;
        if (parentLinearLayout.getChildCount() > 0) {


            formResultJson = new JSONObject();
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

                    case "composite":
                        break;

                }
                if (isSave) {
                    //If the widget has required = true and if the value is not provided, show a message
                    if (jsonObject.has("required") && required && (value == null || value.isEmpty())) {
                        Toast.makeText(context, key + " is mandatory", Toast.LENGTH_SHORT).show();
                        return null;
                    }

                    /*try {
                        formResultJson.put(key, value);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/

                }
                try {
                    //parse input json and fill values given by user against "value" key
                    for (int idx = 0; idx < inputJsonArray.length(); idx++) {
                        JSONObject object = null;
                        try {
                            object = (JSONObject) inputJsonArray.get(idx);
                            if (object.getString("field-name").equalsIgnoreCase(key)) {
                                object.put("value", value);
                                break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //Construct final result json when save is clicked and when the activity holds only one fragment
            if (isSave && fragmentManager.getBackStackEntryCount() == 0) {
                JSONArray masterJSONArray = CreateReportActivity.inputJson;
                JSONObject resultJson = constructFinalResultJson(masterJSONArray);
                Log.i("Result Json", resultJson.toString());
                return resultJson;

            }
        }
        return new JSONObject(); //dummmy json
    }

    //The input json contains the data given by the user. This is saved against key "value"
    //So traverse the entire json, to pick only key and values and construct the final result json
    private JSONObject constructFinalResultJson(JSONArray jsonArray) {
        JSONObject result = new JSONObject();
        for (int idx = 0; idx < jsonArray.length(); idx++) {
            try {
                JSONObject jsonObject = (JSONObject) jsonArray.get(idx);
                String key = jsonObject.getString("field-name");
                String value = null;
                if (jsonObject.getString("type").equalsIgnoreCase("composite")) {
                    JSONObject compJson = constructFinalResultJson(jsonObject.getJSONArray("fields"));
                    result.put(key, compJson);
                } else {
                    if (jsonObject.has("value"))
                        value = jsonObject.getString("value");
                    result.put(key, value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return result;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(ArrayList<String> jsonCompositeKeys) {
        if (mListener != null) {
            mListener.onFragmentInteraction(jsonCompositeKeys);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(ArrayList<String> jsonCompositeKeys);
    }
}
