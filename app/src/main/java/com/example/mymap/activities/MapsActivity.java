package com.example.mymap.activities;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import com.example.mymap.ApiClient.ApiClient;
import com.example.mymap.ApiInterface.ApiInterface;
import com.example.mymap.R;
import com.example.mymap.database.AppDatabase;
import com.example.mymap.database.AppExecutors;
import com.example.mymap.model.DirectionResponse;
import com.example.mymap.model.PlacesResponse;
import com.example.mymap.model.UserData;
import com.example.mymap.utils.GetDirectionsData;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener
{
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    private GoogleMap mMap;
    Geocoder geocoder;
    List<Address> addresses;
    UserData userData;
    public AppDatabase mDb;
    String address = ""; // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
    String city = "";
    String state = "";
    String country = "";
    String postalCode = "";
    String knownName = "";
    String apicode = "";
    String apicity = "";
    double latitude = 0;
    double longitiude = 0;
    boolean mapedit = false;
    LatLng latLng = null;
    double currentlat = 0;
    double currentlong = 0;
    String markerapicode = "";
    String markerapicity = "";
    String currentmarkercity = "";
    String currentmarkercode = "";
    boolean changestatus = false;
    PlacesResponse placesResponse;
    DirectionResponse directionResponse;
    double placelat = 0;
    double placelong = 0;

    public static boolean directionRequested = true;

    @BindView(R.id.but_saveplace)
    Button but_saveplace;

    @BindView(R.id.img_direction)
    ImageView img_direction;

    @BindView(R.id.img_back)
    ImageView img_back;

    @BindView(R.id.img_places)
    ImageView img_places;

    @BindView(R.id.change_view)
    ImageView change_view;

    ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ButterKnife.bind(this);
        but_saveplace.setOnClickListener(this);
        img_back.setOnClickListener(this);
        change_view.setOnClickListener(this);
        img_direction.setOnClickListener(this);
        img_places.setOnClickListener(this);
        placesResponse = new PlacesResponse();
        directionResponse= new DirectionResponse();
        change_view.setImageDrawable(ContextCompat.getDrawable(MapsActivity.this, R.drawable.satellite));
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.api_key), Locale.US);
        }

        if(getIntent().getIntExtra("from",0) == 0)
        {
            mapedit = false;
            img_direction.setVisibility(View.GONE);
            but_saveplace.setVisibility(View.VISIBLE);
        }
        else
        {
            address = getIntent().getStringExtra("address"); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            city = getIntent().getStringExtra("city");
            state = getIntent().getStringExtra("state");
            country = getIntent().getStringExtra("country");
            postalCode = getIntent().getStringExtra("postal");
            markerapicode = getIntent().getStringExtra("markercode");
            markerapicity = getIntent().getStringExtra("markercity");
            latitude = getIntent().getDoubleExtra("latitude",0);
            longitiude = getIntent().getDoubleExtra("longitude",0);
            img_direction.setVisibility(View.VISIBLE);
            but_saveplace.setVisibility(View.GONE);

            mapedit = true;

        }

        fetchLocation();

        userData = new UserData();
        mDb = AppDatabase.getInstance(getApplicationContext());
        geocoder = new Geocoder(this, Locale.getDefault());
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

         // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));

        autocompleteFragment.getView().setBackgroundColor(Color.WHITE);
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            private static final String TAG ="" ;

            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());

                mMap.clear();
//                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getAddress().toString()));
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));
                but_saveplace.setVisibility(View.VISIBLE);
                latLng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                currentddata(place.getLatLng().latitude, place.getLatLng().longitude);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    currentlat = location.getLatitude();
                    currentlong = location.getLongitude();
                    Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    assert supportMapFragment != null;
                    supportMapFragment.getMapAsync(MapsActivity.this);


                    try {
                        addresses = geocoder.getFromLocation(currentlat, currentlong, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    currentmarkercode = addresses.get(0).getCountryCode(); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    currentmarkercity = addresses.get(0).getLocality();

                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Integer id = (Integer) marker.getTag();

               // Toast.makeText(getApplicationContext(), marker.getPosition() + "id = " + id, Toast.LENGTH_SHORT).show();

                currentddata(marker.getPosition().latitude, marker.getPosition().longitude);
                return false;
            }
        });

        if(mapedit)
        {
            latLng = new LatLng(latitude, longitiude);
        }
        else
        {
            latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            latitude = currentLocation.getLatitude();
            longitiude = currentLocation.getLongitude();
        }

        mMap.setMyLocationEnabled(true);
        currentddata(latitude,longitiude);
        settap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocation();
                }
                break;
        }
    }

    private void settap() {

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                // allPoints.add(point);
                mMap.clear();
                img_direction.setVisibility(View.VISIBLE);
                but_saveplace.setVisibility(View.VISIBLE);
                //mMap.addMarker(new MarkerOptions().position(point));
                System.out.println(">>>>>>>>"+point);

                try {
                    addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                } catch (IOException e) {
                    e.printStackTrace();
                }

                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                city = addresses.get(0).getLocality();
                state = addresses.get(0).getAdminArea();
                country = addresses.get(0).getCountryName();
                postalCode = addresses.get(0).getPostalCode();// Only if available else return NULL
                knownName = addresses.get(0).getFeatureName();
                latitude = point.latitude;
                longitiude = point.longitude;
                apicity = addresses.get(0).getLocality();
                apicode = addresses.get(0).getCountryCode();

                System.out.println("++++"+country);
                System.out.println("++++"+state);
                System.out.println("++++"+city);
                System.out.println("++++"+address);
                System.out.println("++++"+postalCode);

                String x = country +" "+ state +" "+ city +" "+address +" "+ postalCode +" "+latitude +" "+longitiude;
                MarkerOptions markerOptions = new MarkerOptions().position(point).title(address);
                mMap.addMarker(markerOptions);
            }
        });
    }


    public void savenote()
    {
        Dialog dialog = new Dialog(MapsActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        MapsActivity.this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.savediag);
        dialog.setCancelable(false);

        Button savedata= dialog.findViewById(R.id.but_save);

        savedata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        userData.setCountry(country);
                        userData.setState(state);
                        userData.setCity(city);
                        userData.setAddress(address);
                        userData.setPostalCode(postalCode);
                        userData.setLatitude(latitude);
                        userData.setLongitude(longitiude);
                        userData.setApicity(apicity);
                        userData.setApicountrycode(apicode);
                        mDb.favDao().insertPerson(userData);

                        Intent intent = new Intent(MapsActivity.this, FavoritePlaces.class);
                        startActivity(intent);
                    }
                });
                }
                });
        dialog.show();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.img_back:
                Intent intent = new Intent(MapsActivity.this, FavoritePlaces.class);
                startActivity(intent);
                break;


            case R.id.img_places:

                Dialog dialog = new Dialog(MapsActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                MapsActivity.this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setContentView(R.layout.option_places);
                dialog.setCancelable(true);

                ImageView img_res= dialog.findViewById(R.id.img_res);

                img_res.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        hitplacesapi("restaurant");
                        dialog.cancel();

                    }
                });

                ImageView img_mesum= dialog.findViewById(R.id.img_mesum);

                img_mesum.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        hitplacesapi("museum");
                        dialog.cancel();

                    }
                });

                ImageView img_cafe= dialog.findViewById(R.id.img_cafe);

                img_cafe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        hitplacesapi("cafe");
                        dialog.cancel();

                    }
                });

                dialog.show();


                break;

            case R.id.but_saveplace:

                savenote();
                break;

            case R.id.change_view:

                if(changestatus)
                {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    change_view.setImageDrawable(ContextCompat.getDrawable(MapsActivity.this, R.drawable.satellite));
                    changestatus = false;
                }
                else
                {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    change_view.setImageDrawable(ContextCompat.getDrawable(MapsActivity.this, R.drawable.twod));
                    changestatus = true;
                }
                break;

            case R.id.img_direction:
                Object[] dataTransfer;
                dataTransfer = new Object[3];
                String url = getDirectionUrl();
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;
                dataTransfer[2] = new LatLng(latitude, longitiude);

                GetDirectionsData getDirectionsData = new GetDirectionsData(MapsActivity.this);
                // execute asynchronously
                getDirectionsData.execute(dataTransfer);
              // directionhit();
                break;

        }

    }

    private void directionhit() {

        Call<DirectionResponse> call = apiInterface.fetchdirection("directions/json?origin="+currentmarkercity+","+currentmarkercode+"&destination="+markerapicity+","+markerapicode+"&waypoints=via:"+currentlat+"%2C"+currentlong+"%7Cvia:"+latitude+"%2C"+longitiude+"&key=AIzaSyBRd92e28XluWndc-T2hsaDfup4LtN6gU8");
        call.enqueue(new Callback<DirectionResponse>() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<DirectionResponse> call, retrofit2.Response<DirectionResponse> response) {


                if (response.isSuccessful()) {
                    directionResponse = response.body();

                    double templang = currentlat;
                    double templong = currentlong;


                    for(int i=0; i< directionResponse.getRoutes().get(0).getLegs().get(0).getSteps().size(); i++)
                    {

                PolylineOptions routeLine;
                LatLng pos = new LatLng(templang, templong);
                routeLine = new PolylineOptions().add(pos);
               // At this point the line won't show, to make it show there are two requirements: Add it to the map and Add at least one more point:

                routeLine.add(new LatLng(directionResponse.getRoutes().get(0).getLegs().get(0).getSteps().get(i).getEndLocation().getLat(), directionResponse.getRoutes().get(0).getLegs().get(0).getSteps().get(i).getEndLocation().getLng()));
                mMap.addPolyline(routeLine);

                         templang = directionResponse.getRoutes().get(0).getLegs().get(0).getSteps().get(i).getEndLocation().getLat();
                         templong = directionResponse.getRoutes().get(0).getLegs().get(0).getSteps().get(i).getEndLocation().getLng();
                    }
                }

                else
                {
                    System.out.println(">>>>>>>"+response);
                }
            }

            @Override
            public void onFailure(Call<DirectionResponse> call, Throwable t) {

                System.out.println(">>>>>>>"+t);

                call.cancel();
            }

        });
    }

    private void hitplacesapi(String type) {



        if(mapedit)
        {
            placelat = latitude;
            placelong = longitiude;
        }
        else
        {
            placelat = currentlat;
            placelong = currentlong;
        }

        Call<PlacesResponse> call = apiInterface.fetchdata("place/nearbysearch/json?location="+placelat+","+placelong+"&radius=2500&type="+type+"&keyword="+type+"&key=AIzaSyBRd92e28XluWndc-T2hsaDfup4LtN6gU8");
        call.enqueue(new Callback<PlacesResponse>() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<PlacesResponse> call, retrofit2.Response<PlacesResponse> response) {


                if (response.isSuccessful()) {

                    mMap.clear();
                    placesResponse = response.body();
                    System.out.println(">>>>>>>"+response);

                    for(int i=0; i<placesResponse.getResults().size(); i++)
                    {
                        LatLng placespin = new LatLng(placesResponse.getResults().get(i).getGeometry().getLocation().getLat(), placesResponse.getResults().get(i).getGeometry().getLocation().getLng());
                        MarkerOptions placesmarkeroption = new MarkerOptions().position(placespin).title( placesResponse.getResults().get(i).getName());
                        mMap.addMarker(new MarkerOptions()
                            .position(placespin)
                            .title(placesResponse.getResults().get(i).getName())
                            .snippet(placesResponse.getResults().get(i).getVicinity())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                       // mMap.addMarker(placesmarkeroption);
                    }
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(placelat, placelong)).title(address);
                    mMap.addMarker(markerOptions);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng( new LatLng(placelat, placelong)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(placelat, placelong), 13.0f));
                }

                else
                {
                    System.out.println(">>>>>>>"+response);
                }
            }

            @Override
            public void onFailure(Call<PlacesResponse> call, Throwable t) {

                System.out.println(">>>>>>>"+t);

                call.cancel();
            }

        });
    }

    public void currentddata(double latitude, double longitiude)
    {
        try {
            addresses = geocoder.getFromLocation(latitude, longitiude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        city = addresses.get(0).getLocality();
        state = addresses.get(0).getAdminArea();
        country = addresses.get(0).getCountryName();
        postalCode = addresses.get(0).getPostalCode();// Only if available else return NULL
        knownName = addresses.get(0).getFeatureName();
        this.latitude = latitude;
        this.longitiude = longitiude;
        apicity = addresses.get(0).getLocality();
        apicode = addresses.get(0).getCountryCode();

        String x = country +" "+ state +" "+ city +" "+address +" "+ postalCode +" "+latitude +" "+longitiude;
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(address);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        mMap.addMarker(markerOptions);

        System.out.println("++++"+country);
        System.out.println("++++"+state);
        System.out.println("++++"+city);
        System.out.println("++++"+address);
        System.out.println("++++"+postalCode);
    }

    private String getDirectionUrl() {
        StringBuilder googleDirectionUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");//
        googleDirectionUrl.append("&destination="+latitude+","+longitiude);
        googleDirectionUrl.append("&origin="+currentlat+","+currentlong);
        googleDirectionUrl.append("&key="+getString(R.string.api_key));
        Log.d("toddler", "getDirectionUrl: "+googleDirectionUrl);
        return googleDirectionUrl.toString();
    }
}