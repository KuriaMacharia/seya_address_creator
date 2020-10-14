package com.center.address;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.center.address.Helper.HttpJsonParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.LocationListener;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CreateActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String county1 = "countyKey";
    public static final String coCode1 = "coCodeKey";
    public static final String proCode1 = "proCodeKey";
    public static final String region1 = "regionKey";
    public static final String regionNumber1 = "regionNumberKey";
    public static final String MyPREFERENCES = "MyPrefs" ;

    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String KEY_ROAD = "road";
    private static final String KEY_REGION = "region";
    private static final String KEY_COUNTY = "county";
    private static final String KEY_REGION_NUMBER = "region_number";
    private static final String KEY_ROAD_NUMBER = "road_number";
    private static final String KEY_ROAD_ADDRESS_NUMBER = "road_address_number";
    private static final String KEY_ADDRESS_NUMBER = "address_number";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_CREATOR = "creator";
    private static final String KEY_STATUS = "status";

    private static final String BASE_URL = "http://www.anwani.net/seya/";

    SharedPreferences sharedpreferences;
    String selectedCounty, coCode, proCode, selectedRegion, regionCode, roadName, roadNumber, routeNumber, Officer,
            fullRouteAddressNumber, fullRoadAddressNumber;
    ImageView homeImg, refreshImg, capturingImg;
    TextView regionTxt, roadTxt, gapTxt, accuracyTxt, coordinatesTxt, addressTxt, addressNumberTxt;
    EditText manualNumberEdt;
    ConstraintLayout directionCons, progressCons, completeCons, manualCons;
    Button rightBtn, leftBtn, captureBtn, generateBtn, saveBtn, cancelBtn, manualBtn, manualGenerateBtn;
    int mCounterL, mCounterR, addressNumber, changeCounter, previousCounterL, previousCounterR;

    private Location mylocation;
    private GoogleApiClient googleApiClient;
    private final static int REQUEST_CHECK_SETTINGS_GPS = 0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;
    LocationRequest locationRequest;
    Context context;
    TextWatcher textwatcher;

    Double lat1, lat2, lon1, lon2, reLat1, reLat2, reLong1, reLong2;
    ArrayList<String> listAllDistance, listReDistance, list,listR, leftList;
    List<String> list1, list2, list3, list4;
    ArrayList<Double> lisLat, resultLat, latitudeList, lisLon, resultLong, longitudeList,  listDistance;
    List<Double> listAverageDistance;
    ArrayList<HashMap<String, String>> distanceListRe;
    List<Integer> counterList;
    Double diffMax,selLat, selLon, selDis;
    int mCounter, indexL, success, counter;
    String fullAddress, Latitude, Longitude, absoluteLat, addressName, upLat, upLong, propertyCategory, propertyType, theNumber;
    ProgressDialog UstartDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        selectedCounty = sharedpreferences.getString(county1,"");
        coCode = sharedpreferences.getString(coCode1,"");
        proCode = sharedpreferences.getString(proCode1,"");
        selectedRegion = sharedpreferences.getString(region1,"");
        regionCode = sharedpreferences.getString(regionNumber1,"");
        UstartDialog = new ProgressDialog(CreateActivity.this, R.style.mydialog);
        context = getApplicationContext();
        setUpGClient();

        counterList = new ArrayList<>();
        distanceListRe = new ArrayList<>();

        listDistance = new ArrayList<Double>();
        listAllDistance = new ArrayList<String>();
        listReDistance = new ArrayList<String>();
        lisLat =new ArrayList<Double>();
        lisLon =new ArrayList<Double>();
        resultLat =new ArrayList<Double>();
        resultLong =new ArrayList<Double>();
        latitudeList =new ArrayList<Double>();
        longitudeList =new ArrayList<Double>();
        listAverageDistance =new ArrayList<>();

        list1 = new ArrayList<>();
        list2 =new ArrayList<>();
        list3 =new ArrayList<>();
        list4 =new ArrayList<>();

        list = new ArrayList<String>();
        listR = new ArrayList<String>();

        mCounterL=0;
        mCounterR=0;

        homeImg=(ImageView) findViewById(R.id.img_home);
        refreshImg=(ImageView) findViewById(R.id.img_refresh);
        capturingImg=(ImageView) findViewById(R.id.img_locating_create);
        regionTxt=(TextView) findViewById(R.id.txt_region_name_create);
        roadTxt=(TextView) findViewById(R.id.txt_road_name_create);
        gapTxt=(TextView) findViewById(R.id.txt_gap_create);
        accuracyTxt=(TextView) findViewById(R.id.txt_progress_create);
        coordinatesTxt=(TextView) findViewById(R.id.txt_coordinates_create);
        addressTxt=(TextView) findViewById(R.id.txt_address_create);
        addressNumberTxt=(TextView) findViewById(R.id.txt_address_number_create);
        directionCons=(ConstraintLayout) findViewById(R.id.cons_direction_create);
        progressCons=(ConstraintLayout) findViewById(R.id.cons_capturing_create);
        completeCons=(ConstraintLayout) findViewById(R.id.cons_capture_complete_create);
        manualCons=(ConstraintLayout) findViewById(R.id.cons_manual_create);
        rightBtn=(Button) findViewById(R.id.btn_right_create);
        leftBtn=(Button) findViewById(R.id.btn_left_create);
        captureBtn=(Button) findViewById(R.id.btn_capture_create);
        generateBtn=(Button) findViewById(R.id.btn_generate_create);
        saveBtn=(Button) findViewById(R.id.btn_save_create);
        cancelBtn=(Button) findViewById(R.id.btn_cancel_create);
        manualBtn=(Button) findViewById(R.id.btn_manual_create);
        manualGenerateBtn=(Button) findViewById(R.id.btn_generate_manual_create);
        manualNumberEdt=(EditText) findViewById(R.id.edt_manual_number_create);

        //new LoadPrevious().execute();
        leftBtn.setEnabled(false);
        rightBtn.setEnabled(false);

        Bundle loca=getIntent().getExtras();
        if (loca != null) {
            roadName=String.valueOf(loca.getCharSequence("roadname"));
            roadNumber=String.valueOf(loca.getCharSequence("roadnumber"));
            routeNumber=String.valueOf(loca.getCharSequence("routenumber"));
        }

        new LoadPrevious().execute();

        ObjectAnimator animation = ObjectAnimator.ofFloat(capturingImg, "rotationY", 360f, 0f);
        animation.setDuration(2600);
        animation.setRepeatCount(ObjectAnimator.INFINITE);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.start();


        homeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CreateActivity.this, MainActivity.class));
            }
        });

        refreshImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CreateActivity.this, CreateActivity.class));
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                directionCons.setVisibility(View.GONE);
                progressCons.setVisibility(View.VISIBLE);

                mCounterL = 2;
                if(previousCounterL == 0){
                    addressNumber= 9 + mCounterL;
                }else{
                    addressNumber = previousCounterL + 2;
                }

                distanceListRe.clear();
                coordinatesTxt.addTextChangedListener(textwatcher);
            }
        });

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                directionCons.setVisibility(View.GONE);
                progressCons.setVisibility(View.VISIBLE);

                mCounterR = 2;
                if (previousCounterR == 0){
                    addressNumber = 8 + mCounterR;
                }else{
                    addressNumber = previousCounterR + 2;
                }

                distanceListRe.clear();
                coordinatesTxt.addTextChangedListener(textwatcher);

            }
        });

        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lisLat.clear();
                lisLon.clear();
                listDistance.clear();
                Latitude = distanceListRe.get(distanceListRe.size()-2).get("latitude");
                Longitude = distanceListRe.get(distanceListRe.size()-2).get("longitude");
                absoluteLat = String.valueOf(Double.parseDouble(distanceListRe.get(distanceListRe.size()-2).get("latitude"))+ 100);

                addressTxt.setText("00 Road, Region, County");
                addressNumberTxt.setText("000.000.0000.00");
                captureBtn.setVisibility(View.GONE);
                generateBtn.setVisibility(View.VISIBLE);
                manualBtn.setVisibility(View.VISIBLE);
                progressCons.setVisibility(View.GONE);
                completeCons.setVisibility(View.VISIBLE);
                coordinatesTxt.removeTextChangedListener(textwatcher);
            }
        });

        generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullAddress = Integer.toString(addressNumber) + " " + roadName + ", " +
                        selectedRegion + ", " + selectedCounty;
                addressTxt.setText(fullAddress);

                fullRouteAddressNumber=routeNumber + Integer.toString(addressNumber);
                fullRoadAddressNumber=roadNumber + Integer.toString(addressNumber);

                addressNumberTxt.setText(routeNumber + Integer.toString(addressNumber)+ " || " +
                                    roadNumber + Integer.toString(addressNumber));

                saveBtn.setVisibility(View.VISIBLE);
                generateBtn.setVisibility(View.GONE);
                manualBtn.setVisibility(View.GONE);

            }
        });

        manualBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manualCons.setVisibility(View.VISIBLE);
                generateBtn.setVisibility(View.GONE);
                manualBtn.setVisibility(View.GONE);
            }
        });

        manualGenerateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(manualNumberEdt.getText().toString().length()>0){
                    fullAddress = manualNumberEdt.getText().toString() + " " + roadName + ", " +
                            selectedRegion + ", " + selectedCounty;
                    addressTxt.setText(fullAddress);

                    addressNumberTxt.setText(routeNumber +"."+ manualNumberEdt.getText().toString()+ "|| " +
                            roadNumber+"."+ manualNumberEdt.getText().toString());
                    addressNumber= Integer.parseInt(manualNumberEdt.getText().toString());

                    saveBtn.setVisibility(View.VISIBLE);
                    generateBtn.setVisibility(View.GONE);
                }
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AddAddress().execute();

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBtn.setVisibility(View.GONE);
                completeCons.setVisibility(View.GONE);
                directionCons.setVisibility(View.VISIBLE);
                lisLat.clear();
                lisLon.clear();
                listDistance.clear();

            }
        });

        textwatcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                counter++;

                lisLat.add((mylocation.getLatitude()));
                lisLon.add(mylocation.getLongitude());

                int a=lisLat.size();

                if(lisLat.size()>2) {

                    lat1 = lisLat.get(a-1);
                    lon1 = lisLon.get(a-1);
                    lat2 = lisLat.get(a - 2);
                    lon2 = lisLon.get(a - 2);

                    Location loc1 = new Location("");
                    loc1.setLatitude(lat1);
                    loc1.setLongitude(lon1);

                    Location loc2 = new Location("");
                    loc2.setLatitude(lat2);
                    loc2.setLongitude(lon2);

                    float distanceInMeters = loc1.distanceTo(loc2);
                    gapTxt.setText(String.format("%.3f", distanceInMeters));
                    //gapTxt.setText(String.valueOf(distanceInMeters));

                    //String disT=String.format("%.2f", String.valueOf(distanceInMeters));
                    String disT=String.valueOf(distanceInMeters);
                    Double disD=Double.parseDouble(disT);

                    if (disD>9){
                        accuracyTxt.setText(String.valueOf(10)+ "%");
                    }else if(disD>6&&disD<9){
                        accuracyTxt.setText(String.valueOf(20)+ "%");
                    } else if(disD>3&& disD<6){
                        accuracyTxt.setText(String.valueOf(25)+ "%");
                    }else if(disD>1&& disD<3){
                        accuracyTxt.setText(String.valueOf(30)+ "%");
                    }else if(disD>0.5&& disD<1){
                        accuracyTxt.setText(String.valueOf(40)+ "%");
                    }else if(disD<0.5){
                        Double acc=100*(1-disD);
                        accuracyTxt.setText(String.format("%.0f", acc) + "%");
                    }

//Add the coordinates storage list
                    HashMap<String, String> map = new HashMap<>();
                    map.put("latitude", String.valueOf(Double.valueOf(lat1)));
                    map.put("longitude", String.valueOf(Double.valueOf(lon1)));
                    map.put("distance", String.valueOf(distanceInMeters));
                    distanceListRe.add(map);

//Compute distance list and process accuracy

                    listDistance.add(Double.parseDouble(String.valueOf(distanceInMeters)));
                    //ss.notifyDataSetChanged();

                    int j=listDistance.size();

                    if(listDistance.size()>10 && captureBtn.getVisibility()==View.GONE) {
                        if (listDistance.get(j - 1) < 0.3
                                && listDistance.get(j - 2) < 0.3
                                && listDistance.get(j - 3) < 0.3
                                && listDistance.get(j - 4) < 0.3
                                && listDistance.get(j - 5) < 0.3) {

                            captureBtn.setVisibility(View.VISIBLE);
                        }
                    }


                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    private synchronized void setUpGClient() {
            googleApiClient = new GoogleApiClient.Builder(context)
            .enableAutoManage(this, 0, this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build();
            getMyLocation();
            //googleApiClient.connect();

    }

    @Override
    public void onLocationChanged(Location location) {

        mylocation = location;
        if (mylocation != null) {
            Double latitude1 = mylocation.getLatitude();
            Double longitude1 = mylocation.getLongitude();

            String latString= String.format("%.6f", latitude1);
            String longString= String.format("%.6f", longitude1);
            if(latString.length()>3 && longString.length()>3){
                String latDisplay= latString.substring(latString.length()-4);
                String longDisplay= longString.substring(longString.length()-4);
                coordinatesTxt.setText(latDisplay + longDisplay);
            }else{
                coordinatesTxt.setText(String.format("%.6f", latitude1) + String.format("%.6f", longitude1));
            }

        }
        //coordinatesTxt.addTextChangedListener(textwatcher);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkPermissions();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS_GPS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getMyLocation();
                        break;
                    case Activity.RESULT_CANCELED:

                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        int permissionLocation = ContextCompat.checkSelfPermission(CreateActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        }
    }


    private void getMyLocation(){
        if(googleApiClient!=null) {
            if (googleApiClient.isConnected()) {
                int permissionLocation = ContextCompat.checkSelfPermission(CreateActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    mylocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    locationRequest = new LocationRequest();
                    locationRequest.setInterval(20);
                    locationRequest.setFastestInterval(20);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
                    PendingResult<LocationSettingsResult> result =
                            LocationServices.SettingsApi
                                    .checkLocationSettings(googleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    // All location settings are satisfied.
                                    // You can initialize location requests here.
                                    int permissionLocation = ContextCompat
                                            .checkSelfPermission(CreateActivity.this,
                                                    Manifest.permission.ACCESS_FINE_LOCATION);
                                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                                        mylocation = LocationServices.FusedLocationApi
                                                .getLastLocation(googleApiClient);
                                    }
                                    break;
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    // Location settings are not satisfied.
                                    // But could be fixed by showing the user a dialog.
                                    try {
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        // Ask to turn on GPS automatically
                                        status.startResolutionForResult(CreateActivity.this,
                                                REQUEST_CHECK_SETTINGS_GPS);
                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    // Location settings are not satisfied.
                                    // However, we have no way
                                    // to fix the
                                    // settings so we won't show the dialog.
                                    // finish();
                                    break;
                            }
                        }
                    });
                }
            }
        }
    }

    private void checkPermissions(){
        int permissionLocation = ContextCompat.checkSelfPermission(CreateActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        }else{
            getMyLocation();
        }
    }

    private class LoadPrevious extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();

            httpParams.put(KEY_NUMBER, theNumber);
            httpParams.put(KEY_ROAD, roadName);
            httpParams.put(KEY_REGION, selectedRegion);
            httpParams.put(KEY_COUNTY, selectedCounty);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_create_number.php", "GET", httpParams);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                if (success == 1) {
                    JSONArray incidences = jsonObject.getJSONArray(KEY_DATA);
                    list.clear();
                    listR.clear();

                    for (int i = 0; i < incidences.length(); i++) {
                        JSONObject incidence = incidences.getJSONObject(i);
                        theNumber = incidence.getString(KEY_NUMBER);
                        list.add(theNumber);
                        listR.add(theNumber);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String result) {
            runOnUiThread(new Runnable() {
                public void run() {

                    if (success == 1) {

                        if(list.size()>0) {
                            for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
                                Integer lEven = Integer.valueOf(iterator.next());
                                if (lEven % 2 == 0) {
                                    //System.out.println("This is Even Number: " + lEven);
                                    iterator.remove();
                                }
                            }
                            Collections.sort(list);
                            //Collections.reverse(list);
                            if(list.size()!=0) {
                                previousCounterL = Integer.parseInt(list.get(list.size()-1));
                            }

                            for (Iterator<String> iterator = listR.iterator(); iterator.hasNext();) {
                                Integer lEven = Integer.valueOf(iterator.next());
                                if (lEven % 2 != 0) {
                                    iterator.remove();
                                }
                            }
                            Collections.sort(listR);
                            //Collections.reverse(listR);
                            if(listR.size()!=0) {
                                previousCounterR = Integer.parseInt(listR.get(listR.size()-1));
                            }
                        }else{
                            previousCounterR = 0;
                        }
                        leftBtn.setEnabled(true);
                        rightBtn.setEnabled(true);

                    } else {
                        leftBtn.setEnabled(false);
                        rightBtn.setEnabled(false);

                        Toast.makeText(CreateActivity.this,"Initializing Failed",Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }

    private class AddAddress extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            UstartDialog.setMessage("Verifying. Please wait...");
            UstartDialog.setIndeterminate(false);
            UstartDialog.setCancelable(false);
            UstartDialog.show();

        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();

            httpParams.put(KEY_ROAD, roadName);
            httpParams.put(KEY_REGION, selectedRegion);
            httpParams.put(KEY_COUNTY, selectedCounty);

            httpParams.put(KEY_REGION_NUMBER, regionCode);
            httpParams.put(KEY_ROAD_NUMBER, roadNumber);
            httpParams.put(KEY_ADDRESS_NUMBER, fullRouteAddressNumber);
            httpParams.put(KEY_ROAD_ADDRESS_NUMBER, fullRoadAddressNumber);

            httpParams.put(KEY_NUMBER, Integer.toString(addressNumber));
            httpParams.put(KEY_ADDRESS, fullAddress);
            httpParams.put(KEY_LATITUDE, Latitude);
            httpParams.put(KEY_LONGITUDE, Longitude);
            httpParams.put(KEY_CREATOR, Officer);
            httpParams.put(KEY_STATUS, "Active");

            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "add_address.php", "POST", httpParams);
            if(success==1)
                try {
                    success = jsonObject.getInt(KEY_SUCCESS);

                }catch (JSONException e) {
                    e.printStackTrace();
                }
            return null;
        }

        protected void onPostExecute(String result) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        new LoadPrevious().execute();
                        saveBtn.setVisibility(View.GONE);
                        completeCons.setVisibility(View.GONE);
                        directionCons.setVisibility(View.VISIBLE);
                        lisLat.clear();
                        lisLon.clear();
                        listDistance.clear();

                        UstartDialog.dismiss();
                    } else {
                        Toast.makeText(CreateActivity.this,"Address Adding Failed",Toast.LENGTH_LONG).show();
                        UstartDialog.dismiss();
                    }
                }
            });
        }
    }

}
