package com.example.gyane.wifiintensitymeasurement;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by misaki on 16/07/08.
 */
public class ResultItem extends LinearLayout {

    private View layout;
    private TextView text;

    public ResultItem(Context context) {
        super(context);

        layout = LayoutInflater.from(context).inflate(R.layout.result_item, this);
        text = (TextView) layout.findViewById(R.id.resultView);
    }

    public void setText(String text) {
        this.text.setText(text);
        Log.d("Item", text);
    }
}
