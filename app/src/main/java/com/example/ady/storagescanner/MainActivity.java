package com.example.ady.storagescanner;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;



public class MainActivity extends AppCompatActivity {
    public final int MY_PERMISSIONS_REQUEST_READ_External = 111;

    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean canfilesacess;
    TextView big10,avrsize,popular;
    Button share;
    boolean cancell;
    boolean restart;
    public ProgressDialog myDialog;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                TreeMap<String, Long> sorted_map = (TreeMap<String, Long>) msg.obj;
                big10 = findViewById(R.id.big10);
                share = findViewById(R.id.sharethis);
                int i = 1;

                for (Map.Entry<String, Long> entry : sorted_map.entrySet()) {
                    big10.append(i + ". " + "File Name: " + entry.getKey() + " Size: " + entry.getValue() + "\n");
                    //Log.d(TAG, "File Name: "+entry.getKey() + " Size: "+ entry.getValue()+ "\n");
                    i++;
                }
                share.setVisibility(View.VISIBLE);
            }

             else if (msg.what == 2) {
                avrsize = findViewById(R.id.avrsize);
                double avr = (double) msg.obj;
                avrsize.setText(" " + avr + " bits");
            } else if(msg.what == 3) {
                popular = findViewById(R.id.popular);
                Map.Entry<String, Integer> mostRepeated =(Map.Entry<String, Integer>) msg.obj;
                popular.setText("The Most popular file is "+ mostRepeated.getKey() + ". Your phone has "+ mostRepeated.getValue()
                + " " + mostRepeated.getKey() + " files"
                );


        } else {

                myDialog.incrementProgressBy(1);
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        canfilesacess = false;
        cancell = false;
        checkpermission();
        managePermission();
        myDialog = new ProgressDialog(MainActivity.this, 0);
        myDialog.setTitle("Scanning your Files");
        myDialog.setMessage("hang in there...");
        myDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        myDialog.setProgress(0);
        myDialog.setMax(100);
        myDialog.setCancelable(false);
        myDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                myDialog.dismiss();
                cancell = true;
            }
        });

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

            canfilesacess = true;

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

    public void StartScan(View view) {
        if (canfilesacess) {


            Log.d(TAG, "AcessFiles: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles()[0]);

            if (isExternalStorageReadable()) {
                myDialog.show();
                new Thread(new Runnable() {
                    File dir = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
                    HashMap<String, Long> map = new HashMap<String, Long>();
                    ValueComparator bvc = new ValueComparator(map);
                    TreeMap<String, Long> sorted_map = new TreeMap<String, Long>(bvc);
                    double avr = 0.0;
                    Map.Entry<String, Integer> mostRepeated;

                    List<String> extension = new ArrayList<>();


                    @Override
                    public void run() {
                        while (myDialog.getProgress() < myDialog.getMax()) {

                            for (int i = 0; i < dir.listFiles().length; i++) {
                                if (!cancell) {
                                    try {
                                        Thread.sleep(50); // make this thread sleep so The progress bar run slower
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    //Log.d(TAG, "AcessFiles: Name "+ dir.listFiles()[i].getName() + "\n"
                                    //+ " total space "+ dir.listFiles()[i].getTotalSpace() +" \n" );
                                    map.put(dir.listFiles()[i].getName(), dir.listFiles()[i].getUsableSpace());
                                    avr += dir.listFiles()[i].getUsableSpace();
                                    //Log.d(TAG, "AcessFiles: Extention " + dir.listFiles()[i].getName().substring(dir.listFiles()[i].getName().length()-3));
                                    extension.add(dir.listFiles()[i].getName().substring(dir.listFiles()[i].getName().length() - 3));
                                    handler.sendMessage(handler.obtainMessage());
                                }else{
                                    myDialog.setProgress(0);

                                    Thread.currentThread().interrupt();
                                }
                            }


                        }
                        if (myDialog.getProgress() == myDialog.getMax()) {
                            if(!cancell) {
                                myDialog.dismiss();
                                sorted_map.putAll(map);
                                sorted_map = putFirstEntries(10, sorted_map);
                                avr = avr / dir.listFiles().length;
                                Message message = handler.obtainMessage();
                                message.what = 1;
                                message.obj = sorted_map;
                                handler.sendMessage(message);

                                Log.d(TAG, "AcessFiles: 10 most " + sorted_map);
                                Log.d(TAG, "AcessFiles: avr size " + avr);
                                Message message1 = handler.obtainMessage();
                                message1.what = 2;
                                message1.obj = avr;
                                handler.sendMessage(message1);
                                mostRepeated = get3MostCommon(extension);
                                Message message2 = handler.obtainMessage();
                                message2.what = 3;
                                message2.obj = mostRepeated;
                                handler.sendMessage(message2);
                                Log.d(TAG, "AcessFiles: Frequency " + mostRepeated);
                            }else{
                                Thread.currentThread().interrupt();
                                myDialog.setProgress(0);
                                return;
                            }
                        }
                    }
                }).start();
            } else {
                Toast.makeText(this, "You have no Permission to read you files", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            Toast.makeText(this, "You have no Permission to acess you files", Toast.LENGTH_LONG).show();
            return;
        }
    }
    public class ValueComparator implements Comparator<String> {

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
