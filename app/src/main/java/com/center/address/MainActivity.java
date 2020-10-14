package com.center.address;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.center.address.Helper.HttpJsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String county1 = "countyKey";
    public static final String coCode1 = "coCodeKey";
    public static final String proCode1 = "proCodeKey";
    public static final String region1 = "regionKey";
    public static final String regionNumber1 = "regionNumberKey";
    public static final String MyPREFERENCES = "MyPrefs";

    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String KEY_COUNTY = "county";
    private static final String KEY_COUNTY_CODE = "co_number";
    private static final String KEY_PROVINCE_NUMBER = "pro_number";

    private static final String KEY_REGION = "region";
    private static final String KEY_REGION_NUMBER = "full_number";
    private static final String KEY_RE_NUMBER = "re_number";
    private static final String KEY_DIRECTION_CODE = "di_code";

    private static final String KEY_ROAD = "road";
    private static final String KEY_ROAD_NUMBER = "road_unique";
    private static final String KEY_ROUTE_NUMBER = "route_number";
    private static final String KEY_ROUTE_UNIQUE = "route_unique";

    private static final String BASE_URL = "http://www.anwani.net/seya/";
    String[] direction = {"---Select---", "North", "South","East", "West", "Center"};

    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    String mainCounty, countyNumber, provinceNumber, selectedCounty, selectedDirection, directionCode, coCode, proCode,
            theRegion, theRegionNumber, selectedRegion, regionCode, theRoad, theCode, selectedRoute, routeNumber,
            roadNumber, theRoadNumber;

    private ArrayList<HashMap<String, String>> listCounties;
    ArrayList<String> listOnlyCounty;
    private ArrayList<HashMap<String, String>> listRegions, listRoutes;
    ArrayList<String> listOnlyRegions, listOnlyRoads, listNameRoads, listOnlyRoutes;
    ProgressDialog UstartDialog;

    Spinner countySpin, regionSpin, directionSpin;
    ConstraintLayout consAddress;
    int success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listCounties= new ArrayList<>();
        listOnlyCounty= new ArrayList<String>();
        listRegions= new ArrayList<>();
        listOnlyRoads= new ArrayList<String>();
        listOnlyRegions= new ArrayList<String>();
        listNameRoads= new ArrayList<>();
        listOnlyRoutes= new ArrayList<>();
        listRoutes= new ArrayList<>();

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        UstartDialog = new ProgressDialog(MainActivity.this, R.style.mydialog);

        new LoadCounties().execute();
        countySpin=(Spinner) findViewById(R.id.spin_county_home);
        directionSpin=(Spinner) findViewById(R.id.spin_direction_home);
        regionSpin=(Spinner) findViewById(R.id.spin_region_home);
        consAddress=(ConstraintLayout) findViewById(R.id.cons_address_home);

        consAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!selectedCounty.contentEquals("---Select---") &&
                        !selectedRegion.contentEquals("---Select---")) {
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    final View dialogView = inflater.inflate(R.layout.select_route_dialog, null);
                    final AlertDialog dialogBuilder = new AlertDialog.Builder(MainActivity.this).create();
                    dialogBuilder.setView(dialogView);

                    TextView regionTxt = (TextView)dialogView.findViewById(R.id.txt_region_name);
                    final TextView routeTxt = (TextView)dialogView.findViewById(R.id.txt_selected_route_home);
                    final Button nextBtn = (Button)dialogView.findViewById(R.id.btn_next_home);
                    Button cancelBtn = (Button)dialogView.findViewById(R.id.btn_cancel_home);
                    final Spinner routeSpin = (Spinner)dialogView.findViewById(R.id.spin_routes_home);

                    regionTxt.setText(selectedRegion+ ",   "+ selectedDirection + "-" + selectedCounty);
                    ArrayAdapter dir = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, listOnlyRoutes);
                    dir.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    routeSpin.setAdapter(dir);

                    routeSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            selectedRoute= String.valueOf(routeSpin.getSelectedItem());
                            if (selectedRoute.contentEquals("---Select---")){

                            }else{
                                for (int j = 0; j < listRoutes.size(); j++) {
                                    if(selectedRoute.contentEquals(listRoutes.get(j).get(KEY_ROAD))){
                                        routeNumber= listRoutes.get(j).get(KEY_ROUTE_UNIQUE);
                                        roadNumber= listRoutes.get(j).get(KEY_ROAD_NUMBER);
                                    }
                                }

                                routeTxt.setText(selectedRoute);
                                nextBtn.setVisibility(View.VISIBLE);
                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    nextBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent i = new Intent(MainActivity.this, CreateActivity.class);
                            Bundle location = new Bundle();
                            location.putString("roadname", selectedRoute);
                            location.putString("roadnumber", roadNumber);
                            location.putString("routenumber", routeNumber);

                            i.putExtras(location);
                            startActivity(i);

                            dialogBuilder.dismiss();

                        }
                    });

                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogBuilder.dismiss();

                        }
                    });

                    dialogBuilder.show();
                }
            }
        });
    }

    private class LoadCounties extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_counties.php", "GET", null);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                if (success == 1) {
                    JSONArray incidences = jsonObject.getJSONArray(KEY_DATA);
                    listOnlyCounty.add("---Select---");

                    for (int i = 0; i < incidences.length(); i++) {
                        JSONObject incidence = incidences.getJSONObject(i);

                        mainCounty = incidence.getString(KEY_COUNTY);
                        countyNumber = incidence.getString(KEY_COUNTY_CODE);
                        provinceNumber = incidence.getString(KEY_PROVINCE_NUMBER);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_COUNTY, mainCounty);
                        map.put(KEY_COUNTY_CODE, countyNumber);
                        map.put(KEY_PROVINCE_NUMBER, provinceNumber);
                        listCounties.add(map);
                        listOnlyCounty.add(mainCounty);
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
                        ArrayAdapter scsc = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, listOnlyCounty);
                        scsc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        countySpin.setAdapter(scsc);

                        countySpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                selectedCounty = String.valueOf(countySpin.getSelectedItem());
                                if (!selectedCounty.contentEquals("---Select---")) {
                                    for (int j = 0; j < listCounties.size(); j++) {
                                        if (selectedCounty.contentEquals(listCounties.get(j).get(KEY_COUNTY))) {
                                            coCode = listCounties.get(j).get(KEY_COUNTY_CODE);
                                            proCode = listCounties.get(j).get(KEY_PROVINCE_NUMBER);
                                        }
                                    }
                          //direction Spin
                                    ArrayAdapter dir = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, direction);
                                    dir.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    directionSpin.setAdapter(dir);

                                    directionSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                            selectedDirection = String.valueOf(directionSpin.getSelectedItem());

                                            if (!selectedCounty.contentEquals("---Select---")) {
                                                if (selectedDirection.contentEquals("North")) {
                                                    new LoadRegions().execute();
                                                    directionCode = "2";

                                                } else if (selectedDirection.contentEquals("South")) {
                                                    new LoadRegions().execute();
                                                    directionCode = "6";

                                                } else if (selectedDirection.contentEquals("East")) {
                                                    new LoadRegions().execute();
                                                    directionCode = "4";

                                                } else if (selectedDirection.contentEquals("West")) {
                                                    new LoadRegions().execute();
                                                    directionCode = "8";

                                                } else if (selectedDirection.contentEquals("Center")) {
                                                    new LoadRegions().execute();
                                                    directionCode = "1";

                                                } else {
                                                    selectedDirection = "None";
                                                }
                                            }

                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> adapterView) {

                                        }
                                    });

                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });


                    } else {
                        Toast.makeText(MainActivity.this, "Error loading addresses", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private class LoadRegions extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            UstartDialog.setMessage("Fetching regions. Please wait...");
            UstartDialog.setIndeterminate(false);
            UstartDialog.setCancelable(false);
            UstartDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(KEY_DIRECTION_CODE, directionCode);
            httpParams.put(KEY_COUNTY, selectedCounty);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_regions.php", "GET", httpParams);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                if (success == 1) {
                    JSONArray incidences = jsonObject.getJSONArray(KEY_DATA);
                    listRegions.clear();
                    listOnlyRegions.clear();
                    listOnlyRegions.add("---Select---");

                    for (int i = 0; i < incidences.length(); i++) {
                        JSONObject incidence = incidences.getJSONObject(i);

                        theRegion = incidence.getString(KEY_REGION);
                        theRegionNumber = incidence.getString(KEY_REGION_NUMBER);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_REGION, theRegion);
                        map.put(KEY_REGION_NUMBER, theRegionNumber);
                        listRegions.add(map);
                        listOnlyRegions.add(theRegion);
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

                        ArrayAdapter scsc = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, listOnlyRegions);
                        scsc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        regionSpin.setAdapter(scsc);
                        regionSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                selectedRegion= String.valueOf(regionSpin.getSelectedItem());
                                if (selectedRegion.contentEquals("---Select---")){

                                }else{
                                    for (int j = 0; j < listRegions.size(); j++) {
                                        if(selectedRegion.contentEquals(listRegions.get(j).get(KEY_REGION))){
                                            regionCode= listRegions.get(j).get(KEY_REGION_NUMBER);
                                        }
                                    }

                                    new LoadRoutes().execute();
                                    editor = sharedpreferences.edit();
                                    editor.putString(county1, selectedCounty);
                                    editor.putString(coCode1, coCode);
                                    editor.putString(proCode1, proCode);
                                    editor.putString(region1, selectedRegion);
                                    editor.putString(regionNumber1, regionCode);
                                    editor.commit();
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                    } else {

                        Toast.makeText(MainActivity.this,"Initializing Failed",Toast.LENGTH_LONG).show();

                    }

                    UstartDialog.dismiss();
                }
            });
        }
    }

    private class LoadRoutes extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(KEY_COUNTY, selectedCounty);
            httpParams.put(KEY_RE_NUMBER, regionCode);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_routes_region.php", "GET", httpParams);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                if (success == 1) {
                    JSONArray incidences = jsonObject.getJSONArray(KEY_DATA);

                    listRoutes.clear();
                    listOnlyRoutes.clear();
                    listOnlyRoutes.add("---Select---");

                    for (int i = 0; i < incidences.length(); i++) {
                        JSONObject incidence = incidences.getJSONObject(i);

                        theRoad = incidence.getString(KEY_ROAD);
                        theCode = incidence.getString(KEY_ROUTE_UNIQUE);
                        theRoadNumber = incidence.getString(KEY_ROAD_NUMBER);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_ROAD, theRoad);
                        map.put(KEY_ROUTE_UNIQUE, theCode);
                        map.put(KEY_ROAD_NUMBER, theRoadNumber);
                        listRoutes.add(map);
                        listOnlyRoutes.add(theRoad);
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

                    } else {
                        Toast.makeText(MainActivity.this, "Error loading Roads", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
