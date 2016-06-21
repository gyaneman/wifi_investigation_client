package com.example.gyane.wifiintensitymeasurement;

import android.app.Activity;
import android.util.Pair;
import android.view.View;

import java.util.*;

import org.json.JSONObject;

/**
 * Created by gyane on 2016/05/16.
 */
public class HttpClient implements HttpRequest.OnReceivedListener {

    HttpRequest httpRequest;
    OnReceivedListener listener;

    public HttpClient() {}
    public HttpClient(Activity activity) {
        httpRequest = new HttpRequest(activity);
    }

    public interface OnReceivedListener {
        public void onReceived(JSONObject jsonObject);
    }

    public boolean getRequest(String urlStr, ArrayList<Pair<String, String>> params, OnReceivedListener listener) {
//        if (this.httpRequest != null) {
//            return false;
//        }

        this.listener = listener;
//        httpRequest = new HttpRequest();
        httpRequest.setGetParams(urlStr, params);
        httpRequest.setListener(this);
        httpRequest.execute();     // 非同期処理開始
        return true;
    }

    public boolean postRequest(String urlStr, JSONObject jsonParams, OnReceivedListener listener) {
//        if (this.httpRequest != null) {
//            return false;
//        }

        this.listener = listener;
//        httpRequest = new HttpRequest();
        httpRequest.setPostParams(urlStr, jsonParams);
        httpRequest.setListener(this);
        httpRequest.execute();     // 非同期処理開始
        return true;
    }

    @Override
    public void onReceived(JSONObject jsonObject) {
        httpRequest = null;
        this.listener.onReceived(jsonObject);
    }
}
