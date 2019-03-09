package com.androidapps.muradahmad.ficonic_demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.opencsv.CSVWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {


    private SensorManager sensorManager;

    private LocationManager locationManager;
    private Location location;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;

    double axValue, ayValue, azValue;
    double timestamp;


    private Double myLatitude;
    private Double myLongitude;

    //TextView
    private TextView txtTime;
    private TextView txtPosition;
    private TextView txtBearing;
    private TextView txtAltitude;
    private TextView txtSpeed;
    private TextView txtAccuracy;
    private TextView txtX;
    private TextView txtY;
    private TextView txtZ;


    //Buttons
    private Button btnStart;
    private Button btnStop;

    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds


    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 104;
    private boolean permissionIsGranted = false;



    final JSONObject locationJsonObject = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        txtTime = (TextView) findViewById(R.id.txtTime);
        txtPosition = (TextView) findViewById(R.id.txtPos);
        txtAltitude = (TextView) findViewById(R.id.txtAlt);
        txtAccuracy = (TextView) findViewById(R.id.txtAccuracy);
        txtBearing = (TextView) findViewById(R.id.txtBear);
        txtSpeed = (TextView) findViewById(R.id.txtSpeed);
        txtX = (TextView) findViewById(R.id.txtX);
        txtY = (TextView) findViewById(R.id.txtY);
        txtZ = (TextView) findViewById(R.id.txtZ);


        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);





        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getUserLocation();
                getAccelerometerValues();
                getTime();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stopData();

            }
        });





    }

    public void getUserLocation() {

        //user Location

        locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
            } else {
                permissionIsGranted = true;
            }
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                MINIMUM_TIME_BETWEEN_UPDATES,
                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                this);


    }

    public void getAccelerometerValues(){

        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

    }
    public void getTime(){

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strCurrenttime = simpleDateFormat.format(calendar.getTime());


        txtTime.setText(String.valueOf((strCurrenttime)));

        // Timestamp in millisecond
        /*timestamp = System.currentTimeMillis();
        txtTime.setText(String.valueOf((timestamp)));*/

        try {
            locationJsonObject.put("timestamp", strCurrenttime);
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    public void stopData(){

        saveDataCSV();
        location = null;
        locationManager = null;
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        // Permission check for Marshmallow and newer
        int permissionCoarseLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        int permissionWriteExternal = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (permissionCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (permissionWriteExternal != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
        }


    }

    @Override
    public void onLocationChanged(Location location) {

        try {
            locationJsonObject.put("latitude", location.getLatitude());
            locationJsonObject.put("longitude", location.getLongitude());
            locationJsonObject.put("accuracy", location.getAccuracy());
            locationJsonObject.put("altitude", location.getAltitude());
            locationJsonObject.put("speed", location.getSpeed());
            locationJsonObject.put("bearing",location.getBearing());

            Log.d("GPSLocation", String.valueOf(location.getLatitude()));

            txtPosition.setText(String.valueOf(location.getLatitude())+ "," + String.valueOf(location.getLongitude()));
            txtBearing.setText(String.valueOf(location.getBearing()));
            txtSpeed.setText(String.valueOf(location.getSpeed()));
            txtAccuracy.setText(String.valueOf(location.getAccuracy()));
            txtAltitude.setText(String.valueOf(location.getAltitude()));
            //txtAccuracy.setText(String.valueOf(location.getSpeedAccuracyMetersPerSecond()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
            } else {
                permissionIsGranted = true;
            }
            return;
        }
        //LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            axValue=event.values[0];
            ayValue=event.values[1];
            azValue=event.values[2];

            txtX.setText(String.valueOf(axValue));
            txtY.setText(String.valueOf(ayValue));
            txtZ.setText(String.valueOf(azValue));
        }
        try {
            locationJsonObject.put("xValue", axValue);
            locationJsonObject.put("yValue", ayValue);
            locationJsonObject.put("zValue", azValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void saveDataCSV() {

        File exportDir = new File(Environment.getExternalStorageDirectory(), "LocationData");

        if (!exportDir.exists()) {

            if (!exportDir.mkdirs()) {

                Log.d("App", "failed to create directory");

            }

            }else {
            Log.d("App", "Directory created");
            }

        try {

                File file = new File(exportDir, "location_data.csv");

                FileWriter fileWriter = new FileWriter(file, file.exists());
                CSVWriter writer = new CSVWriter(fileWriter);

                String latitude = locationJsonObject.getString("latitude");
                String longitude = locationJsonObject.getString("longitude");
                String accuracy = locationJsonObject.getString("accuracy");
                String altitude =  locationJsonObject.getString("altitude");
                String speed = locationJsonObject.getString("speed");
                String bearing = locationJsonObject.getString("bearing");
                String xValue = locationJsonObject.getString("xValue");
                String yValue = locationJsonObject.getString("yValue");
                String zValue = locationJsonObject.getString("zValue");
                String timestamp =locationJsonObject.getString("timestamp");



                String[] values = {
                        timestamp,
                        latitude,
                        longitude,
                        accuracy,
                        altitude,
                        speed,
                        bearing,
                        xValue,
                        yValue,
                        zValue

                };

                writer.writeNext(values);
                writer.close();
                fileWriter.close();
            Toast.makeText(this, "Data saved to CSV",
                    Toast.LENGTH_LONG).show();
            Log.d("App", "Data saved to CSV");


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
