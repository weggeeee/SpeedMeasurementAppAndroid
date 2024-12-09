package com.example.bedier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, SensorEventListener {

    //Initialization of variables
    private LocationManager locationMangaer     = null;
    private LocationManager locationMangaer2    = null;
    private LocationListener locationListener   = null;
    private Button btnGetLocation               = null;
    private Button btnStop                      = null;
    private TextView v_longitude                = null;
    private TextView v_latitude                 = null;
    private TextView v_location                 = null;
    private TextView v_speed                    = null;
    private TextView v_speed_accu               = null;
    private TextView v_altitude                 = null;
    private TextView v_sat                      = null;
    private TextView v_gps_status               = null;
    private TextView v_update_status            = null;
    private TextView v_filename                 = null;
    private TextView v_provider                 = null;
    private TextView v_lux                      = null;
    private TextView yaw_rate                   = null;
    private boolean wrote                       = false;
    private boolean Aufzeichnung                = false;

    String pattern                              = "HH:mm:ss.SSS";
    SimpleDateFormat simpleDateFormat           = new SimpleDateFormat(pattern);

    private SensorManager sensorMan;
    private Sensor accelerometer;
    private Sensor gyroscope;

    private float[] mGravity;
    private double mAccel;
    private double mAccelCurrent;
    private double mAccelLast;
    private double acc_x;
    private double acc_y;
    private double acc_z;
    private double yaw_x;
    private double yaw_y;
    private double yaw_z;

    String pathFile                             = "";
    String pathFile2                            = "";
    String pathFile3                            = "";
    String lastTime                             = "";
    String longitude                            = "";
    String latitude                             = "";
    String location                             = "";
    String speed                                = "";
    String altitude                             = "";
    String sat_nr                               = "";
    String filename                             = "";
    String speed_accu                           = "";
    String provider                             = "";
    String time                                 = "";
    String realtimeNanos                        = "";
    String circleAccu                           = "";
    String bearing                              = "";
    String verticalAccu                         = "";
    String bearingAccu                          = "";
    String content                              = "";
    String yaw                                  = "";
    String acc                                  = "";
    String root                                 = "";
    String state                                = "";
    String fname                                = "";
    String fname2                               = "";
    String fname3                               = "";
    String timeDevice                           = "";

    //Function called with hit on start button
    @Override
    public void onClick(View v) {
        Aufzeichnung = true;
        //Start location services
        if ( !locationMangaer.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }
        locationListener = new APPLocationListener();

        //Check if GPS is enabled
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            v_gps_status.setText("Wait for signal");
            v_gps_status.setTextColor(Color.parseColor("#0066ff"));
            locationMangaer.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);           //request for updates of GPS
            //locationMangaer2.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);      //request for updates of network

            //Headers for tables in the files
            content = "Latitude, Longitude, circleAccuracy, Speed, SpeedAccuracy, Bearing, BearingAccuracy, Height, verticalAccuracy," +
                    " Location, Provider, SatelliteNumber, elapsedRealtime, Time,TimeDevice" + System.getProperty("line.separator");
            acc = "x, y, z, time, timeMS" + System.getProperty("line.separator");
            yaw = "x, y, z, time, timeMS" + System.getProperty("line.separator");

            filename = v_filename.getText().toString();
            writeFileExternalStorage(); //Start file
        } else {
            v_gps_status.setText("No GPS-Access!!!");
            v_gps_status.setTextColor(Color.parseColor("#ff0000"));

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10); //request permission if not given
                }
                return;
            }
        }
    }

    //Function called when new Sensor data available
    //Get all the data and save it to the file
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values.clone();
            // Shake detection
            acc_x = mGravity[0];
            acc_y = mGravity[1];
            acc_z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
            double delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            if(!wrote) {
                Date date = Calendar.getInstance().getTime();
                acc = acc_x + ", " + acc_y + ", " + acc_z + ", " + simpleDateFormat.format(date) + ", ";
                appendToCheckbook(pathFile2, acc);
                wrote = true;
                v_lux.setText("ACCEL" + System.getProperty("line.separator") + "x: " + ((float) acc_x) + System.getProperty("line.separator") + "y: " + ((float) acc_y) + System.getProperty("line.separator") + "z: " + ((float) acc_z) + " ");
            }
        }
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            mGravity = event.values.clone();
            // Shake detection
            yaw_x = mGravity[0];
            yaw_y = mGravity[1];
            yaw_z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = Math.sqrt(yaw_x * yaw_x + yaw_y * yaw_y + yaw_z * yaw_z);
            double delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            if(wrote) {
                Date date = Calendar.getInstance().getTime();
                yaw = yaw_x + ", " + yaw_y + ", " + yaw_z + ", " + simpleDateFormat.format(date) + ", ";
                appendToCheckbook(pathFile3, yaw);
                wrote = false;
                yaw_rate.setText("YAW RATE" + System.getProperty("line.separator") + "x: " + ((float) yaw_x) + System.getProperty("line.separator") + "y: " + ((float) yaw_y) + System.getProperty("line.separator") + "z: " + ((float) yaw_z) + " ");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //Append new data to file
    public void appendToCheckbook (String filename, String data) {

        if (!Aufzeichnung) return;

        BufferedWriter bw = null;

        try {
            // APPEND MODE SET HERE
            bw = new BufferedWriter(new FileWriter(filename, true));

            Date date = Calendar.getInstance().getTime();
            bw.write(data + date.getTime() + System.getProperty("line.separator"));
            bw.flush();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {                       // always close the file
            if (bw != null) try {
                bw.close();
            } catch (IOException ioe2) {
                // just ignore it
            }
        } // end try/catch/finally

    } // end test()

    private class APPLocationListener implements LocationListener {
        @SuppressLint("MissingPermission")
        //Function called when new location data available
        //Get all the Information of new location
        @Override
        public void onLocationChanged(Location loc) {
            //If not a location received by GPS use network
            if (loc == null) {
                loc = locationMangaer.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }


            //Get the address
            String city_name = null;
            Geocoder gcd = new Geocoder(getBaseContext(),
                    Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                if (addresses.size() > 0)
                    city_name = addresses.get(0).getLocality();
            } catch (IOException e) {
                city_name = "unknown";
                e.printStackTrace();
            }

            //Save data as string
            latitude        = "" + loc.getLatitude(); //Get latitude
            longitude       = "" + loc.getLongitude(); //Get longitude
            location        = "" + city_name; //Get city name
            altitude        = "" + loc.getAltitude(); //Get height in meters
            sat_nr          = "" + loc.getExtras().getInt("satellites"); //get number of available satellites
            provider        = "" + loc.getProvider();  //get Provider Info
            time            = new SimpleDateFormat("HH:mm:ss.SSS").format(loc.getTime());// get time of last fix
             Date date      = Calendar.getInstance().getTime();
            timeDevice      = "" + date.getTime();

            //Call functions of higher API level
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                realtimeNanos   = new SimpleDateFormat("HH:mm:ss.SSS").format(loc.getElapsedRealtimeNanos()); // auch bei mobile verwendbar?
            }
            if (loc.hasSpeed()) {
                speed = "" + loc.getSpeed();    //Get speed in meters per second
            } else {
                speed = "0";                   //Say not available
            }
            if (loc.hasAccuracy()) {
                circleAccu = "" + loc.getAccuracy();
            } else {
                circleAccu = "0";
            }
            if (loc.hasBearing()) {
                bearing = "" + loc.getBearing();
            }else{
                bearing = "0";
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(loc.hasSpeedAccuracy()) {
                    speed_accu = "" + loc.getSpeedAccuracyMetersPerSecond(); //get accuracy of speed
                }else{
                    speed_accu = "0";
                }
                if (loc.hasBearingAccuracy()) {
                    bearingAccu = "" + loc.getBearingAccuracyDegrees();//here it fails
                }else{
                    bearingAccu = "0";
                }
                if (loc.hasVerticalAccuracy()) {
                    verticalAccu = "" + loc.getVerticalAccuracyMeters();
                }else{
                    verticalAccu = "0";
                }
            } else {
                speed_accu      = "0";//value is not available
                bearingAccu     = "0";
                verticalAccu    = "0";
            }

            //Display some values in App
            v_latitude.setText(latitude);
            v_longitude.setText(longitude);
            v_location.setText(location);
            v_speed.setText(speed + " m/s");
            v_speed_accu.setText(speed_accu);
            v_provider.setText(provider);
            v_altitude.setText(altitude + "m");
            v_sat.setText(sat_nr);
            v_gps_status.setText("Locationservices working");
            v_gps_status.setTextColor(Color.parseColor("#33cc33"));
            Calendar c = Calendar.getInstance(); //Get time on system
            v_update_status.setText("Last update: " + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND));

            //Append to result String


            // For any reason that onLocationChanged Function is called 3 times but returns same parameters -> no location change
            // Updates are actually received every second -> easy fix on that compare timestamp
            // Concatenate the String together
            if(!timeDevice.equals(lastTime)) {
                content = latitude + ", " + longitude + ", " + circleAccu + ", " + speed + ", " + speed_accu + ", " + bearing + ", " +
                        bearingAccu + ", " + altitude + ", " + verticalAccu + ", " + location + ", " + provider + ", " + sat_nr + ", " + realtimeNanos + ", " +
                        time + ", " ;
                appendToCheckbook(pathFile, content);
                lastTime = timeDevice;
            }
        }

        @Override
    public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    //Function called when App started
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // always in portrait mode

        //Create relation to textViews of GUI
        v_longitude             = (TextView) findViewById(R.id.Lon_view);
        v_latitude              = (TextView) findViewById(R.id.Lat_view);
        v_location              = (TextView) findViewById(R.id.Loc_view);
        v_speed                 = (TextView) findViewById(R.id.Spd_view);
        v_altitude              = (TextView) findViewById(R.id.Alt_view);
        v_sat                   = (TextView) findViewById(R.id.Sat_view);
        v_update_status         = (TextView) findViewById(R.id.Update_status);
        v_gps_status            = (TextView) findViewById(R.id.textView);
        v_filename              = (TextView) findViewById(R.id.fileName);
        v_speed_accu            = (TextView) findViewById(R.id.Speed_accu);
        v_provider              = (TextView) findViewById(R.id.Provider);
        v_lux                   = (TextView) findViewById(R.id.Lux);
        yaw_rate                = (TextView) findViewById(R.id.yawRate);
        btnGetLocation          = (Button)   findViewById(R.id.buttonStart);
        btnGetLocation.setOnClickListener(this);// If button clicked, jump to on click



        //Start locationManagers for Location Service
        locationMangaer         = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationMangaer2        = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        sensorMan               = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer           = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope               = sensorMan.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccel                  = 0.00f;
        mAccelCurrent           = SensorManager.GRAVITY_EARTH;
        mAccelLast              = SensorManager.GRAVITY_EARTH;

        //Initialize Sensors
        sensorMan.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorMan.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        btnStop                 = (Button) findViewById(R.id.buttonStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "File saved completely", Toast.LENGTH_SHORT).show();
                Aufzeichnung = false;
            }
        });
    }

    //Called when GPS Check fails
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    //Called when storage permission is not given
    public boolean isStoragePermissionGranted() {
        String TAG = "Storage Permission";
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    //Save data to a file
    public void writeFileExternalStorage() {
        state = Environment.getExternalStorageState();
        root = Environment.getExternalStorageDirectory().toString();
        FileOutputStream outputStream = null;
        FileOutputStream outputStream2 = null;
        FileOutputStream outputStream3 = null;


        //external storage availability check
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return;
        }
        if (isStoragePermissionGranted()) { // check or ask permission
            File myDir = new File(root, "/Messungen");
            if (!myDir.exists()) {
                myDir.mkdirs();
            }
            fname           = "Messung-" + filename + ".txt";
            fname2          = "Messung-" + filename + "-SensorDatenAcc.txt";
            fname3          = "Messung-" + filename + "-SensorDatenYaw.txt";

            File file       = new File(myDir, fname);
            pathFile        = file.getPath();
            File file2      = new File(myDir, fname2);
            pathFile2        = file2.getPath();
            File file3      = new File(myDir, fname3);
            pathFile3        = file3.getPath();

            try {
                if (file.isFile()) {
                    Toast.makeText(getApplicationContext(), "Filename already exists", Toast.LENGTH_SHORT).show();
                    return;
                }
                file.createNewFile();
                file2.createNewFile();
                file3.createNewFile();
                //second argument of FileOutputStream constructor indicates whether to append or create new file if one exists
                outputStream = new FileOutputStream(file, true);
                outputStream.write(content.getBytes());
                outputStream.flush();
                outputStream.close();
                outputStream2 = new FileOutputStream(file2, true);
                outputStream2.write(acc.getBytes());
                outputStream2.flush();
                outputStream2.close();
                outputStream3 = new FileOutputStream(file3, true);
                outputStream3.write(yaw.getBytes());
                outputStream3.flush();
                outputStream3.close();
                Toast.makeText(getApplicationContext(), "File saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Saving file was not successful", Toast.LENGTH_SHORT).show();
            }
        }
    }
}