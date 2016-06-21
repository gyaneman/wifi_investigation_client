package com.example.gyane.wifiintensitymeasurement;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by kataoka on 2016/05/21.
 */
public class HttpRequest  extends AsyncTask<Void, Void, JSONObject> {

    protected final static String GET = "GET";
    protected final static String POST = "POST";

    public interface OnReceivedListener {
        void onReceived(final JSONObject jsonObject);
    }

    private String urlStr;
    private String httpMethod;
    private String paramsStr;


    protected OnReceivedListener listener;

    private Activity activity;
    private ProgressDialog progressDialog;

    protected static final int BUSY = 1;
    protected static final int LISTEN = 2;
    private int state = LISTEN;
    protected void shiftToBusy() {
        this.state = BUSY;
    }
    protected void shiftToListen() {
        this.state = LISTEN;
    }
    protected int getState() {
        return state;
    }


    public HttpRequest(Activity activity) {
        this.activity = activity;
    }

    // getパラメータ
    public void setGetParams(String urlStr, ArrayList<Pair<String, String>> params) {
        this.urlStr = urlStr;
        this.httpMethod = GET;
        this.paramsStr = createGetParamsStr(params);
    }

    // postパラメータ
    public void setPostParams(String urlStr, JSONObject jsonParams) {
        this.urlStr = urlStr;
        this.httpMethod = POST;
        this.paramsStr = jsonParams.toString();
    }

    private String createGetParamsStr(ArrayList<Pair<String, String>> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        String paramsStr = "?";
        for (int i = 0; i < params.size(); i++) {
            String paramStr = params.get(i).first + "=" + params.get(i).second +
                    (i!=(params.size()-1) ? "&":"");  // 最後のパラメータの最後には"&"をつけない
            paramsStr = paramsStr + paramStr;
        }
        return paramsStr;
    }

    public URL getURL() {
        URL url;
        if (httpMethod == GET) {
            try {
                url = new URL(urlStr + paramsStr);
            } catch(MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
            return url;
        }

        if (httpMethod == POST) {
            try {
                url = new URL(urlStr);
            } catch(MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
            return url;
        }
        return null;
    }


    public String getPostParams() {
            return (httpMethod == POST ? paramsStr : null);
        }


    public void setListener(OnReceivedListener listener) {
        this.listener = listener;
    }


    private String getBody(InputStream instream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(instream));

        String body = "";

        while (true) {
            try {
                String line = reader.readLine();
                if (line == null) break;
                body = body + line;
            } catch(IOException e) {
                e.printStackTrace();
                Log.i("HTTP", "ERROR");
                break;
            }
        }

        return body;
    }


    private JSONObject doGet(URL url) {
        HttpURLConnection connection = null;
        InputStream instream;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(GET);
            instream = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            connection.disconnect();
            return null;
        }

        String body = getBody(instream);

        connection.disconnect();
        Log.i("HTTP", "GET... " + body);
        JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }

    private JSONObject doPost(URL url, String paramsStr) {
        HttpURLConnection connection = null;
        InputStream instream;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(POST);
            connection.setFixedLengthStreamingMode(paramsStr.getBytes().length);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.connect();
            DataOutputStream os = new DataOutputStream(connection.getOutputStream());
            os.write(paramsStr.getBytes("UTF-8"));
            os.flush();
            os.close();
            instream = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            connection.disconnect();
            return null;
        }

        String body = getBody(instream);

        connection.disconnect();
        Log.i("HTTP", "POST... " + body);

        JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }


    @Override
    protected void onPreExecute() {
        shiftToBusy();
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Sending data ...");
        progressDialog.show();
    }

    @Override
    protected JSONObject doInBackground(Void... arg) {

        URL url = getURL();
        if (url == null) { return null; }

        JSONObject ret = null;
        switch (this.httpMethod) {
            case GET:
                ret = doGet(url);
                break;
            case POST:
                ret = doPost(url, paramsStr);
                break;
            default:
                break;
        }
        return ret;
    }

    /*
    このメソッドはUIスレッドで動作する．
     */
    @Override
    protected void onPostExecute(JSONObject json) {
        shiftToListen();
        listener.onReceived(json);
        progressDialog.dismiss();
    }
}
