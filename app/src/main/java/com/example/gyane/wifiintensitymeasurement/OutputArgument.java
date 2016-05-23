package com.example.gyane.wifiintensitymeasurement;

import android.net.wifi.ScanResult;

import java.io.Serializable;
import java.util.List;

/**
 * Created by gyane on 2016/05/23.
 */
public class OutputArgument implements Serializable {
    public List<ScanResult> scanResults;
    public OutputArgument(List<ScanResult> scanResults) {
        this.scanResults = scanResults;
    }
}
