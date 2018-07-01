package com.daytona.charan.favouritesnearby;

import android.Manifest;
import android.annotation.TargetApi;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.daytona.charan.favouritesnearby.adapter.RecyclerPlacesAdapter;
import com.daytona.charan.favouritesnearby.adapter.RecyclerViewAdapter;
import com.daytona.charan.favouritesnearby.data.AppDatabase;
import com.daytona.charan.favouritesnearby.data.Place;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        RecyclerPlacesAdapter.ISelectedPlace{


    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 101;
    List<com.daytona.charan.favouritesnearby.StoreModel> storeModels;
    com.daytona.charan.favouritesnearby.ApiInterface apiService;

    String latLngString;
    LatLng latLng;

    RecyclerView recyclerView;
    EditText editText;
    Button button;
    ArrayList<PlacesPOJO.CustomA> results;
    /**
     * The M google api client.
     */
    GoogleApiClient mGoogleApiClient;
    /**
     * The M last location.
     */
    Location mLastLocation;
    /**
     * The M curr location marker.
     */
    Marker mCurrLocationMarker;
    /**
     * The M location request.
     */
    LocationRequest mLocationRequest;
    /**
     * The M location manager.
     */
    LocationManager mLocationManager;

    private ProgressBar progressBar;
    private AppDatabase db;
    private Place place;
    private List<Place> placeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "places-db").build();

        progressBar = findViewById(R.id.progressBar);
        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        apiService = com.daytona.charan.favouritesnearby.APIClient.getClient().create(
                com.daytona.charan.favouritesnearby.ApiInterface.class);

        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = editText.getText().toString().toLowerCase().trim();
                String[] split = s.split("\\s+");


                if (split.length == 1) {
                    fetchStores(split[0]);
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter text in the required format", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();

        Toast.makeText(this, "Inside Resume", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            if (db.mPlaceDao().countPlace() > 0)    {
                placeList = db.mPlaceDao().loadAllUsers();
                runOnUiThread(()    ->  {
                    if (placeList != null && placeList.size() > 0)  {
                        RecyclerPlacesAdapter adapterStores = new RecyclerPlacesAdapter(placeList, MainActivity.this);
                        recyclerView.setAdapter(adapterStores);
                    }
                });
            }
        }).start();
    }

    private void fetchStores(String placeType) {

        /**
         * For Locations In India McDonalds stores aren't returned accurately
         */

        //Call<PlacesPOJO.Root> call = apiService.doPlaces(placeType, latLngString,"\""+ businessName +"\"", true, "distance", APIClient.GOOGLE_PLACE_API_KEY);

//        Call<PlacesPOJO.Root> call = apiService.doPlaces(placeType, latLngString, businessName, true, "distance", APIClient.GOOGLE_PLACE_API_KEY);

        if (mGoogleApiClient == null)
            buildGoogleApiClient();

        if (latLngString == null || latLngString.isEmpty())
            fetchLocation();

        progressBar.setVisibility(View.VISIBLE);

        Call<com.daytona.charan.favouritesnearby.PlacesPOJO.Root> call = apiService.doPlaces(latLngString, placeType,
                "5000", com.daytona.charan.favouritesnearby.APIClient.GOOGLE_PLACE_API_KEY);
//        https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=5000
// &type=hospital&keyword=cruise&key=AIzaSyDn29L87mlc-21CJBsXAuxI2l833ECMMtY
/*
        Call<com.daytona.charan.favouritesnearby.PlacesPOJO.Root> call = apiService.doPlaces("-33.8670522,151.1957362",
                "hospital", "5000", com.daytona.charan.favouritesnearby.APIClient.GOOGLE_PLACE_API_KEY);
*/

        call.enqueue(new Callback<com.daytona.charan.favouritesnearby.PlacesPOJO.Root>() {
            @Override
            public void onResponse(Call<com.daytona.charan.favouritesnearby.PlacesPOJO.Root> call, Response<com.daytona.charan.favouritesnearby.PlacesPOJO.Root> response) {
                progressBar.setVisibility(View.GONE);

                com.daytona.charan.favouritesnearby.PlacesPOJO.Root root = response.body();

                if (response.isSuccessful()) {
                    if (root.status.equals("OK")) {

                        place = new Place();
                        place.setName(editText.getText().toString().toLowerCase().trim());

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                db.mPlaceDao().insertUsers(place);
                            }
                        }).start();

                        results = root.customA;

                        Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
                        mapIntent.putExtra("PLACES_RES", results);
                        mapIntent.putExtra("CURRENT_LOC", latLngString);
                        startActivity(mapIntent);
/*
                        storeModels = new ArrayList<>();
                        for (int i = 0; i < results.size(); i++) {
                            PlacesPOJO.CustomA info = results.get(i);
                            fetchDistance(info);
                        }
*/
                    } else {
                        try {
                            Toast.makeText(getApplicationContext(), "No matches found near you " + response.body().status, Toast.LENGTH_SHORT).show();
                        }   catch (NullPointerException e)  {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "No matches found near you ", Toast.LENGTH_SHORT).show();
                        }
                    }

                } else if (response.code() != 200) {
                    Toast.makeText(getApplicationContext(), "Error " + response.code() + " found.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<PlacesPOJO.Root> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                // Log error here since request failed
                call.cancel();
            }
        });


    }

/*
    private void fetchDistance(final PlacesPOJO.CustomA info) {

        Call<ResultDistanceMatrix> call = apiService.getDistance(APIClient.GOOGLE_PLACE_API_KEY, latLngString, info.geometry.locationA.lat + "," + info.geometry.locationA.lng);
        call.enqueue(new Callback<ResultDistanceMatrix>() {
            @Override
            public void onResponse(Call<ResultDistanceMatrix> call, Response<ResultDistanceMatrix> response) {

                ResultDistanceMatrix resultDistance = response.body();
                if ("OK".equalsIgnoreCase(resultDistance.status)) {

                    ResultDistanceMatrix.InfoDistanceMatrix infoDistanceMatrix = resultDistance.rows.get(0);
                    ResultDistanceMatrix.InfoDistanceMatrix.DistanceElement distanceElement = infoDistanceMatrix.elements.get(0);
                    if ("OK".equalsIgnoreCase(distanceElement.status)) {
                        ResultDistanceMatrix.InfoDistanceMatrix.ValueItem itemDuration = distanceElement.duration;
                        ResultDistanceMatrix.InfoDistanceMatrix.ValueItem itemDistance = distanceElement.distance;
                        String totalDistance = String.valueOf(itemDistance.text);
                        String totalDuration = String.valueOf(itemDuration.text);

                        storeModels.add(new StoreModel(info.name, info.vicinity, totalDistance, totalDuration));

                        if (storeModels.size() == results.size()) {
                            RecyclerViewAdapter adapterStores = new RecyclerViewAdapter(results, storeModels);
                            recyclerView.setAdapter(adapterStores);
                        }

                    }

                }

            }

            @Override
            public void onFailure(Call<ResultDistanceMatrix> call, Throwable t) {
                call.cancel();
            }
        });

    }

*/
    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
//                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, R.string.access_denied_to_location, Toast.LENGTH_LONG).show();
                }
                break;

        }

    }

    /**
     * Build google api client.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClientConnect();
    }

    /*
    Method to connect GoogleApiClient.
     */
    private void googleApiClientConnect() {
        if (mGoogleApiClient != null) mGoogleApiClient.connect();
    }

    /*
    Method to disconnect GoogleApiClient.
     */
    private void googleApiClientDisconnect() {
        if (mGoogleApiClient != null) mGoogleApiClient.disconnect();
    }


    private void fetchLocation() {
        if (isGpsEnabled()) {
            if (mGoogleApiClient == null)
                buildGoogleApiClient();

            if (mGoogleApiClient.isConnected()) {
                setLocation();
            }

        } else {
            Toast.makeText(this, "No GPS", Toast.LENGTH_SHORT).show();
        }

    }

    private void setLocation()  {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setSmallestDisplacement(1.0F);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            latLngString = location.getLatitude() + "," + location.getLongitude();
                            latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    });

    }

    /**
     * Check location permission boolean.
     *
     * @return the boolean
     */
    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ALL_PERMISSIONS_RESULT);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ALL_PERMISSIONS_RESULT);
            }
        }
    }


    /*
Method to check whether GPS is enabled or not.
 */
    private boolean isGpsEnabled() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        fetchLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClientDisconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        googleApiClientDisconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClientConnect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClientDisconnect();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        buildGoogleApiClient();
    }

    @Override
    public void onPlaceSelected(String placeName) {
        String s = editText.getText().toString().toLowerCase().trim();
        String[] split = s.split("\\s+");


        if (split.length == 1) {
            fetchStores(split[0]);
        } else {
            Toast.makeText(getApplicationContext(), "Please enter text in the required format", Toast.LENGTH_SHORT).show();
        }
    }
}