package com.example.gyane.wifiintensitymeasurement;

import android.os.AsyncTask;
import android.util.Pair;
import android.view.View;

import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by gyane on 2016/05/17.
 */
public abstract class DummyHttpClient extends HttpClient {

    public abstract JSONObject generateExampleJsonResponse();

    protected String urlStr;
    protected String httpMethod;
    protected ArrayList<Pair<String, String>> getParams;
    protected JSONObject postParams;

    class DummyHttpRequest extends AsyncTask<Void, Void, Void> {
        private void sleep(long msec) {
            try {
                synchronized (this) {
                    wait(msec);
                }
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... arg) {
            sleep(1000);
            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {
            JSONObject json = generateExampleJsonResponse();
            listener.onReceived(json);
        }
    }


    private void setTask(OnReceivedListener listener) {
        this.listener = listener;
        DummyHttpRequest dummy = new DummyHttpRequest();
        dummy.execute();
    }


    @Override
    public boolean getRequest(String urlStr, ArrayList<Pair<String, String>> params, OnReceivedListener listener) {
        this.httpMethod = HttpRequest.GET;
        this.urlStr = urlStr;
        this.getParams = params;
        setTask(listener);
        return true;
    }

    @Override
    public boolean postRequest(String urlStr, JSONObject jsonParams, OnReceivedListener listener) {
        this.httpMethod = HttpRequest.POST;
        this.urlStr = urlStr;
        this.postParams = jsonParams;
        setTask(listener);
        return true;
    }
}
