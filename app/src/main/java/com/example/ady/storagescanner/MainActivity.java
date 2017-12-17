package com.example.ady.storagescanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    public final int MY_PERMISSIONS_REQUEST_READ_External = 111;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkpermission();
        managePermission();
    }

    private void managePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_External);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            AcessFiles();
        }
    }

    private void AcessFiles() {
        Log.d(TAG, "AcessFiles: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles()[0]);
        File dir = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));


        HashMap<String, Long> map = new HashMap<String, Long>();
        ValueComparator bvc = new ValueComparator(map);
        TreeMap<String, Long> sorted_map = new TreeMap<String, Long>(bvc);
        double avr = 0.0;
        Map.Entry<String, Integer> mostRepeated;

        List<String> extension = new ArrayList<>();
        if (isExternalStorageReadable()) {

            for (int i = 0; i < dir.listFiles().length; i++) {
                //Log.d(TAG, "AcessFiles: Name "+ dir.listFiles()[i].getName() + "\n"
                //+ " total space "+ dir.listFiles()[i].getTotalSpace() +" \n" );
                map.put(dir.listFiles()[i].getName(), dir.listFiles()[i].getUsableSpace());
                avr += dir.listFiles()[i].getUsableSpace();
                //Log.d(TAG, "AcessFiles: Extention " + dir.listFiles()[i].getName().substring(dir.listFiles()[i].getName().length()-3));
                extension.add(dir.listFiles()[i].getName().substring(dir.listFiles()[i].getName().length() - 3));
            }
            sorted_map.putAll(map);
            sorted_map = putFirstEntries(10, sorted_map);
            avr = avr / dir.listFiles().length;
            Log.d(TAG, "AcessFiles: 10 most " + sorted_map);
            Log.d(TAG, "AcessFiles: avr size " + avr);
            mostRepeated = get3MostCommon(extension);
            Log.d(TAG, "AcessFiles: Frequency " + mostRepeated);


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_External: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void checkpermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        Log.d(TAG, "checkpermission: " + permissionCheck);
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    class ValueComparator implements Comparator<String> {

        Map<String, Long> base;

        public ValueComparator(Map<String, Long> base) {
            this.base = base;
        }

        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            }
        }
    }
    public static TreeMap<String, Long> putFirstEntries(int max, TreeMap<String, Long> source) {
        int count = 0;
        TreeMap<String, Long> target = new TreeMap<String, Long>();
        for (Map.Entry<String,Long> entry:source.entrySet()) {
            if (count >= max) break;

            target.put(entry.getKey(), entry.getValue());
            count++;
        }
        return target;
    }
    public static Map.Entry<String, Integer> get3MostCommon(List<String> list) {


        Map<String, Integer> stringsCount = new HashMap<>();
        Map.Entry<String, Integer> mostRepeated = null;


        for (String s : list) {
            Integer c = stringsCount.get(s);
            if (c == null)
                c = new Integer(0);
            c++;
            stringsCount.put(s, c);
        }

        for (Map.Entry<String, Integer> e : stringsCount.entrySet()) {
            if (mostRepeated == null || mostRepeated.getValue() < e.getValue())
                mostRepeated = e;
        }
        return mostRepeated;
    }



}
