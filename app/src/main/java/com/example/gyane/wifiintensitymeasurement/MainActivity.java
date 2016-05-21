package com.example.gyane.wifiintensitymeasurement;

import java.util.*;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.CheckBox;

import static android.view.View.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mesurementButton = (Button) findViewById(R.id.mesureButton);
        Button outputButton = (Button) findViewById(R.id.outputButton);

        mesurementButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                getWifi();
            }
        });

        /* outputButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                output();
            }
        }); */
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

    public void getWifi() {
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
    }
}