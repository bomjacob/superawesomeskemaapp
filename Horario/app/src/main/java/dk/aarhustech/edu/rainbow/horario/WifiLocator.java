package dk.aarhustech.edu.rainbow.horario;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

class WifiLocator {
    private static final String TAG = WifiLocator.class.getSimpleName();
    private final OnLocationCallback callback;
    private final WifiScanReceiver wifiScanReceiver;
    private final WifiManager wifiManager;
    private final ToneGenerator toneGen;
    private RandomForestClassifier classifier;
    private boolean adding = false;
    private double threshold = 1.0;
    private Timer timer;
    private RoomData roomData;
    private OnAddProgress onAddProgress;

    WifiLocator(Context context, OnLocationCallback callback) {
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.wifiScanReceiver = new WifiScanReceiver();
        this.callback = callback;

        AssetManager assets = context.getAssets();
        try {
            classifier = new RandomForestClassifier(assets.open("forest.json"));
        } catch (IOException e) {
            Log.e(TAG, "Couldn't load forest.json.", e);
        }

        toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    }

    void register(Context context) {
        context.registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    void unregister(Context context) {
        context.unregisterReceiver(wifiScanReceiver);
    }

    void scan() {
        wifiManager.startScan();
    }

    void startAdd(String name, OnAddProgress onAddProgress) {
        this.onAddProgress = onAddProgress;
        roomData = new RoomData(name);
        adding = true;
        scan();
    }

    void stopAdd() {
        if (timer != null) timer.cancel();
        adding = false;
    }

    RoomData getRoomData() {
        return roomData;
    }

    void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    private List<RoomResult> predictionToList(Map<String, Double> map, double max) {
        List<Map.Entry<String, Double>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        List<RoomResult> result = new ArrayList<>();
        double highest = list.get(0).getValue();
        for (Map.Entry<String, Double> entry : list) {
            if (highest - entry.getValue() <= highest * threshold) {
                result.add(new RoomResult(entry.getKey(), entry.getValue() / max));
            }
        }
        return result;
    }

    interface OnAddProgress {
        void onAddProgress(int aps, int dataPoints);
    }

    interface OnLocationCallback {
        void onLocation(List<RoomResult> rooms);
    }

    static class RoomResult {
        String name;
        double score;

        RoomResult(String name, double score) {
            this.name = name;
            this.score = score;
        }
    }

    private class WifiScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> scanResults = wifiManager.getScanResults();
            if (adding) {
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 50);
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        scan();
                    }
                }, 100);
                ArrayList<RoomData.AP> aps = new ArrayList<>();
                for (ScanResult sr : scanResults) {
                    if (sr.SSID.contains("AARHUS TECH")) { // Only use school wifi
                        aps.add(new RoomData.AP(sr.BSSID, sr.level));
                    }
                }
                roomData.add(aps);
                onAddProgress.onAddProgress(aps.size(), roomData.size());
            } else {
                boolean atSchool = false;
                Map<String, Double> features = new HashMap<>();
                for (ScanResult sr : scanResults) {
                    if (sr.SSID.contains("AARHUS TECH")) atSchool = true;
                    features.put(sr.BSSID, (double) sr.level);
                }
                if (atSchool) {
                    Map<String, Double> prediction = classifier.predict_proba(features);
                    callback.onLocation(predictionToList(prediction, classifier.getMaxScore()));
                } else {
                    callback.onLocation(null);
                }
            }
        }
    }
}
