package com.example.gyane.wifiintensitymeasurement;

import java.util.*;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CheckBox;
import org.json.JSONObject;
import android.content.Intent;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, HttpClient.OnReceivedListener {

    public final static int REPEAT_SCAN = 3;
    public final static int DELAY_SEARCH = 6000;

    private ResultDatas[] mResultData = new ResultDatas[REPEAT_SCAN];

    HttpClient httpClient;
    JSONObject json;

    Button mesurementButton;
    Button outputButton;
    TextView stateView;
    LinearLayout layout;

    class ResultDatas {
        public List<ScanResult> scanResults;
        public ResultDatas() {
            scanResults = new ArrayList<ScanResult>();
        }

        public void setWifi() {
            this.scanResults = getWifi();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mesurementButton = (Button) findViewById(R.id.mesureButton);
        mesurementButton.setOnClickListener(this);
        outputButton = (Button) findViewById(R.id.outputButton);
        outputButton.setOnClickListener(this);
        stateView = (TextView) findViewById(R.id.textView);
        layout = (LinearLayout) findViewById(R.id.result);

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
        Log.i("WIFI_INFO", "start");
        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        List<ScanResult> scanResults = wifiManager.getScanResults();
        //stateView.setText(Integer.toString(scanResults.size()));

        sortScanResult(scanResults);

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
            String scanStr = scan.BSSID + sep + scan.frequency + sep + scan.level + "\n";
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
//            resultDatases = new ResultDatas[REPEAT_SCAN];
//            for (int i = 0; i < REPEAT_SCAN; i++) {
//                resultDatases[i] = new ResultDatas();
//                resultDatases[i].setWifi();
//            }
            MesureAsync mesureAsync = new MesureAsync(this);
            mesureAsync.execute();
        } else if (view == outputButton) {
            if (mResultData == null) { return; }
            String[] csvDatas = new String[REPEAT_SCAN];
            for (int i = 0; i < REPEAT_SCAN; i++) {
                csvDatas[i] = createCSVFromScanResults(mResultData[i].scanResults);
            }
            Intent intent = new Intent(MainActivity.this, OutputActivity.class);
            intent.putExtra("csv", csvDatas);
            startActivity(intent);
        }
    }

    public class MesureAsync extends AsyncTask<Void, Integer, Void> {

        private Context mContext;
        private ProgressDialog mProgressDialog;
        private ResultDatas mResultItem;

        public MesureAsync(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            showDialog();
            layout.removeAllViews();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (int count = 0; count < REPEAT_SCAN; count++) {
                    if (isCancelled()) {
                        return null;
                    }

                    mResultItem = new ResultDatas();
                    mResultItem.setWifi();

                    mResultData[count] = mResultItem;
                    publishProgress(count+1);

                    if (count != REPEAT_SCAN-1) Thread.sleep(DELAY_SEARCH);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("MainAsync", "Exception");
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setProgress(values[0]);

            addResultView(mResultData[values[0]-1].scanResults);
        }

        @Override
        protected void onPostExecute(Void resultDatas) {
            super.onPostExecute(resultDatas);

            dismissDialog();
            Log.d("MainAsync", "onPostExecute");
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            dismissDialog();
        }

        private void showDialog() {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage("測定中");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMax(REPEAT_SCAN);
            mProgressDialog.incrementProgressBy(0);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setButton("キャンセル", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancel(true);
                }
            });
            mProgressDialog.show();
        }

        private void dismissDialog() {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

        private void addResultView(List<ScanResult> scanResults) {
            CheckBox detailCheckBox = (CheckBox) findViewById(R.id.detailCheckBox);
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

            ResultItem item = new ResultItem(mContext);
            item.setText(text);
            layout.addView(item);
        }
    }
}