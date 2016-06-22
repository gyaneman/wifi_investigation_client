package com.example.gyane.wifiintensitymeasurement;

import android.net.wifi.ScanResult;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

public class OutputActivity extends AppCompatActivity implements View.OnClickListener, HttpClient.OnReceivedListener {

    static final String defaultServerUrl = "https://wifiinvestivation.herokuapp.com/";     //"http://192.168.56.1:3000/";
    HttpClient httpClient;
    String[] csvdatas;
    Boolean isSending = false;

    Button sendButton;
    EditText urlField;
    EditText tagField;
    EditText placeField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);

        sendButton = (Button) findViewById(R.id.sendButton);
        urlField = (EditText) findViewById(R.id.serverIpField);
        tagField = (EditText) findViewById(R.id.tagField);
        placeField = (EditText) findViewById(R.id.placeField);

        urlField.setText(defaultServerUrl);
        tagField.setText("test");
        placeField.setText("ugawa-lab");

        sendButton.setOnClickListener(this);

        httpClient = new HttpClient(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        csv = getIntent().getStringExtra("csv");
        csvdatas = getIntent().getStringArrayExtra("csv");

        for (String csv : csvdatas) {
            if (csv.isEmpty()) {
                Log.i("OutputActivity", "Error");
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view == sendButton) {
            sendButton.setFocusable(false);
            urlField.setFocusable(false);
            tagField.setFocusable(false);
            placeField.setFocusable(false);
            isSending = true;
            String url = urlField.getText().toString();
            for (String csv : csvdatas) {
                Boolean isSuccess = httpClient.postRequest(url, createJson(csv), this);
                if (!isSuccess) {
                    isSending = false;
                }
            }
        }
    }


    JSONObject createJson(String csv) {
        Log.i("json", "createJson");
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonResults = new JSONArray();
        try {
            jsonObject.put("device", Build.DEVICE);
            jsonObject.put("host", Build.HOST);
            String tag = tagField.getText().toString();
            if (!tag.isEmpty()) {
                jsonObject.put("tag", tag);
            }
            String place = placeField.getText().toString();
            jsonObject.put("place", place);
            // jsonObject.put("results", jsonResults);
            jsonObject.put("csv_data", csv);
            Log.i("json", jsonObject.toString());
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    @Override
    public void onReceived(JSONObject jsonObject) {
        sendButton.setFocusable(true);
        urlField.setFocusable(true);
        tagField.setFocusable(true);
        placeField.setFocusable(true);
        isSending = true;
        finish();
    }
}
