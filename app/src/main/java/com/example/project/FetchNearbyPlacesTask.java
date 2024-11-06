package com.example.project;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchNearbyPlacesTask extends AsyncTask<String, Void, String> {

    private GoogleMap googleMap;

    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    @Override
    protected String doInBackground(String... urls) {
        String response = "";
        try {
            URL url = new URL(urls[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            response = stringBuilder.toString();
            reader.close();
        } catch (Exception e) {
            Log.e("FetchPlaces", "Error fetching data", e);
        }
        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray resultsArray = jsonObject.getJSONArray("results");

            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject placeObject = resultsArray.getJSONObject(i);
                String placeName = placeObject.getString("name");
                JSONObject location = placeObject.getJSONObject("geometry").getJSONObject("location");
                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");


                LatLng placeLatLng = new LatLng(lat, lng);
                googleMap.addMarker(new MarkerOptions()
                        .position(placeLatLng)
                        .title(placeName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        } catch (Exception e) {
            Log.e("FetchPlaces", "Error parsing places data", e);
        }
    }
}