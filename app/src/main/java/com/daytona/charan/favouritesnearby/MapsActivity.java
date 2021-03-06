package com.daytona.charan.favouritesnearby;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<PlacesPOJO.CustomA> placesResults;
    private String currentLatLngString;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mToolbar.setTitle(R.string.title_activity_maps);
        getSupportActionBar().setTitle(R.string.title_activity_maps);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_navigation);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mToolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_place_option));

        if (getIntent().getSerializableExtra("PLACES_RES") != null)
            placesResults = (ArrayList<PlacesPOJO.CustomA>) getIntent().getSerializableExtra("PLACES_RES");

        if (getIntent().getSerializableExtra("CURRENT_LOC") != null)
            currentLatLngString = (String) getIntent().getSerializableExtra("CURRENT_LOC");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_places_list, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())   {
            case R.id.idPlacesList:
                Intent placesIntent = new Intent(MapsActivity.this, PlacesListActivity.class);
                placesIntent.putExtra("PLACES_RES", placesResults);
                placesIntent.putExtra("CURRENT_LOC", currentLatLngString);
                startActivity(placesIntent);
//                Toast.makeText(this, "Places List clicked", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        String lat = currentLatLngString.split(",")[0];
        String lon = currentLatLngString.split(",")[1];

        LatLng currlatlong = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currlatlong, 12.0F));

        for (int i = 0; i < placesResults.size(); i++) {
            PlacesPOJO.CustomA info = placesResults.get(i);
            String placeLat = info.geometry.locationA.lat;
            String placeLong = info.geometry.locationA.lng;

            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(placeLat),
                    Double.parseDouble(placeLong))).title(info.name));
        }
    }
}