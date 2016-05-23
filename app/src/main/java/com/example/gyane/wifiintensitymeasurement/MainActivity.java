package com.example.gyane.wifiintensitymeasurement;

import java.util.*;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.CheckBox;
import org.json.JSONObject;
import org.json.JSONArray;
import android.content.Intent;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, HttpClient.OnReceivedListener {

    HttpClient httpClient;
    JSONObject json;
    List<ScanResult> scanResults;

    Button mesurementButton;
    Button outputButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mesurementButton = (Button) findViewById(R.id.mesureButton);
        mesurementButton.setOnClickListener(this);
        outputButton = (Button) findViewById(R.id.outputButton);
        outputButton.setOnClickListener(this);

        httpClient = new HttpClient();
    }

    public void sortScanResult(List<ScanResult> scanResults) {
        Collections.sort(scanResults, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult scanResult, ScanResult t1) {
                if (scanResult.level == t1.level) {
                    return 0;
                }
                if (scanResult.level < t1.level) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    public List<ScanResult> getWifi() {
        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        List<ScanResult> scanResults = wifiManager.getScanResults();
        TextView stateView = (TextView) findViewById(R.id.textView);
        TextView resultView = (TextView)findViewById(R.id.resultView);
        CheckBox detailCheckBox = (CheckBox) findViewById(R.id.detailCheckBox);
        stateView.setText(Integer.toString(scanResults.size()));

        sortScanResult(scanResults);
        boolean isCheckedDetailView = detailCheckBox.isChecked();

        if (scanResults == null) {
            stateView.setText("取得できませんでした");
        }

        String text = "";
        for (ScanResult scanResult : scanResults) {
            if (isCheckedDetailView) {
                String t = scanResult.toString() + "\n";
                text += t;
                Log.i("WIFI_INFO", t);
            } else {
                String ssid = scanResult.SSID;
                if (ssid.isEmpty()) {
                    ssid = "NODATA";
                }
                String t =
                        ssid + " "
                        + scanResult.BSSID + " "
                        + scanResult.frequency + " "
                        + scanResult.level
                        + "\n";
                    text += t;
                Log.i("WIFI_INFO", t);
            }
        }
        resultView.setText(text);
        Log.i("WIFI_INFO", "##########################");

        return scanResults;
    }

    String createCSVFromScanResults(List<ScanResult> scanResults) {
        String csv = "";
        String sep = " ";
        for (ScanResult scan : scanResults) {
                /*
                JSONObject jsonScan = new JSONObject();
                jsonScan.put("ssid", scan.SSID);
                jsonScan.put("bssid", scan.BSSID);
                jsonScan.put("freq", scan.frequency);
                jsonScan.put("level", scan.level);
                jsonResults.put(jsonScan);
                */
            String scanStr =
                    scan.BSSID + sep + scan.frequency + sep + scan.level + "\n";
            csv = csv + scanStr;
        }
        return csv;
    }


    @Override
    public void onReceived(JSONObject jsonObject) {
        if (jsonObject == null) {
            Log.i("json response", "null");
        } else {
            Log.i("json response", jsonObject.toString());
        }
    }

    @Override
    public void onClick(View view) {
        Log.i("BUTTON", "clicked");
        if (view == mesurementButton) {
            this.scanResults = getWifi();
        } else if (view == outputButton) {
            if (scanResults == null) { return; }
            Intent intent = new Intent(MainActivity.this, OutputActivity.class);
            intent.putExtra("csv", createCSVFromScanResults(scanResults));
            startActivity(intent);
        }
    }
}