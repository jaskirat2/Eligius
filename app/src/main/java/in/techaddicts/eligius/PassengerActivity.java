package in.techaddicts.eligius;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import es.dmoral.toasty.Toasty;

public class PassengerActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    public void jasmethod(View view)
    {
        final Intent y1 = new Intent(this,MainActivity2.class);
        startActivity(y1);
    }


    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private Button btnRequestCar;

    private Button btnBeep;

    private boolean isUberCancelled = true;

    private boolean isCarReady = false;

    private Timer t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnRequestCar=findViewById(R.id.btnRequestCar);
        btnRequestCar.setOnClickListener(this);

        btnBeep=findViewById(R.id.btnBeepBeep);                  //When user click on the beep beep button then show the message to passenger.....
        btnBeep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDriverUpdates();              //updates show  in the passenger activity if the driver accepts the request....
            }
        });

        ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");                 //When we open our app and if there is same username saved in the database
        carRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());          // Then a request is send automativcally..
        carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e)
            {
                if (objects.size() > 0 && e == null) {

                    isUberCancelled = false;
                    btnRequestCar.setText("Cancel request!");

                    //getDriverUpdates();
                }
            }
        });

        findViewById(R.id.btnLogoutFromPassengerActivity).setOnClickListener(new View.OnClickListener() //Log Out The User....
        {
            @Override
            public void onClick(View v)
            {
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            finish();
                        }
                    }
                });
            }
        });



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

        /*
        OnMapReadyCallback
         public interface OnMapReadyCallback
         Callback interface for when the map is ready to be used.
         Once an instance of this interface is set on a MapFragment or MapView object, the onMapReady(GoogleMap) method is triggered when the map is ready to be used and provides a non-null instance of GoogleMap.
          If Google Play services is not installed on the device, the user will be prompted to install it, and the onMapReady(GoogleMap) method will only be triggered when the user has installed it and returned to the app.
         */

        mMap = googleMap;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location)      //This method is always called when the person changes her location..
            {
                updateCameraPassengerLocation(location);
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

        };

        if (Build.VERSION.SDK_INT < 23)          //Then dont ask for permissiom also ask for requestLocationUpdate..             //Without this the app crashed..            ..... Allow or reject option..
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);          //Then ask location update.

        }
        else if (Build.VERSION.SDK_INT >= 23)
        {
            if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)  //if permission is not granted by the user to access location..
            {
                ActivityCompat.requestPermissions(PassengerActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);    //Then ask the user to give permission..

            } else {            //If user give us the permission then
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCameraPassengerLocation(currentPassengerLocation);

            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)  //Result of request send by the user.. Whether permission is granted or not,.
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)         //If permission is granred..
        {
            if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);   //Give us last location of user..
                updateCameraPassengerLocation(currentPassengerLocation);
            }
        }
    }

    private void updateCameraPassengerLocation(Location pLocation)
    {
        //On location from your phone.......
        //Location is based on latitude And Longitude...
        if(isCarReady==false)
        {
            LatLng passengerLocation1 = new LatLng(pLocation.getLatitude(), pLocation.getLongitude());
            mMap.clear();                                                                                   //Clear the previous location or marker...
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation1, 15));                   //Update the map and move the camera to passenger location..         //15 is level of zoom

            mMap.addMarker(new MarkerOptions().position(passengerLocation1).title("You are here!!!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));        //Marker to specify passenger s position...
        }
    }

    @Override
    public void onClick(View view)              //When button is tapped
    {
        if (isUberCancelled)            //if boolean is true means request is not send then click on the button and send a request...
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)      //if user has granted the permission to us..
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location passengerCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (passengerCurrentLocation != null)    //if location of user is not null....
                {
                    ParseObject requestCar = new ParseObject("RequestCar");              //Request For Car
                    requestCar.put("username", ParseUser.getCurrentUser().getUsername());

                    ParseGeoPoint userLocation = new ParseGeoPoint(passengerCurrentLocation.getLatitude(), passengerCurrentLocation.getLongitude());
                    requestCar.put("passengerLocation", userLocation);           //Save Current location of user to database....

                    requestCar.saveInBackground(new SaveCallback() {              //Save the changes in background means in database..
                        @Override
                        public void done(ParseException e) {

                            if (e == null)
                            {
                                Toast t = Toasty.success(PassengerActivity.this, "Request is sent", Toast.LENGTH_SHORT);
                                t.setGravity(Gravity.CENTER, 0,-400);
                                t.show();

                                btnRequestCar.setText("Cancel Request");
                                isUberCancelled = false;


                            }
                        }
                    });

                }
            }
            else
            {
                Toast t = Toasty.error(this, "Unknown Error. Something went wrong!!!", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0,-400);
                t.show();
            }
        }
        else                      //if user click on the button again  ...then after clicking on the same button request is cancelled...
        {
            ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
            carRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
            carRequestQuery.findInBackground(new FindCallback<ParseObject>()
            {
                @Override
                public void done(List<ParseObject> requestList, ParseException e)
                {
                    if (requestList.size() > 0 && e == null)
                    {
                        isUberCancelled = true;
                        btnRequestCar.setText("Request for Assistance");

                        for (ParseObject uberRequest : requestList)
                        {
                            uberRequest.deleteInBackground(new DeleteCallback()           //delete all the requests of same person....
                            {
                                @Override
                                public void done(ParseException e)
                                {
                                    if (e == null) {
                                        Toast t = Toasty.warning(PassengerActivity.this, "Request deleted", Toast.LENGTH_SHORT);
                                        t.setGravity(Gravity.CENTER, 0,-400);
                                        t.show();
                                    }
                                }
                            });
                        }

                    }
                }
            });

        }
    }

    private void getDriverUpdates()
    {
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                ParseQuery<ParseObject> uberRequestQuery = ParseQuery.getQuery("RequestCar");
                uberRequestQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
                uberRequestQuery.whereEqualTo("requestAccepted", true);
                uberRequestQuery.whereExists("driverOfMe");

                uberRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {

                        if (objects.size() > 0 && e == null) {

                            isCarReady = true;
                            for (final ParseObject requestObject : objects) {

                                ParseQuery<ParseUser> driverQuery = ParseUser.getQuery();
                                driverQuery.whereEqualTo("username", requestObject.getString("driverOfMe"));
                                driverQuery.findInBackground(new FindCallback<ParseUser>() {
                                    @Override
                                    public void done(List<ParseUser> drivers, ParseException e) {
                                        if (drivers.size() > 0 && e == null) {

                                            for (ParseUser driverOfRequest : drivers) {

                                                ParseGeoPoint driverOfRequestLocation = driverOfRequest.getParseGeoPoint("driverLocation");
                                                if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                                    Location passengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                                    ParseGeoPoint pLocationAsParseGeoPoint = new ParseGeoPoint(passengerLocation.getLatitude(), passengerLocation.getLongitude());

                                                    double milesDistance = driverOfRequestLocation.distanceInMilesTo(pLocationAsParseGeoPoint);

                                                    if (milesDistance < 0.3) {                                                         


                                                        requestObject.deleteInBackground(new DeleteCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if (e == null) {
                                                                    Toast t = Toasty.success(PassengerActivity.this, "Assistance is arriving !!", Toast.LENGTH_SHORT);
                                                                    t.setGravity(Gravity.CENTER, 0,-400);
                                                                    t.show();
                                                                    isCarReady = false;
                                                                    isUberCancelled = true;
                                                                    btnRequestCar.setText("Request again!");
                                                                }
                                                            }
                                                        });

                                                    } else {

                                                        float roundedDistance = Math.round(milesDistance * 10) / 10;
                                                        Toast t = Toasty.info(PassengerActivity.this,requestObject.getString("driverOfMe") + " is " + roundedDistance + "miles away from you ! Please wait!!", Toast.LENGTH_SHORT);
                                                        t.setGravity(Gravity.CENTER, 0,-400);
                                                        t.show();


                                                        LatLng dLocation = new LatLng(driverOfRequestLocation.getLatitude(),
                                                                driverOfRequestLocation.getLongitude());


                                                        LatLng pLocation = new LatLng(pLocationAsParseGeoPoint.getLatitude(), pLocationAsParseGeoPoint.getLongitude());

                                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver Location"));
                                                        Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(pLocation));

                                                        ArrayList<Marker> myMarkers = new ArrayList<>();
                                                        myMarkers.add(driverMarker);
                                                        myMarkers.add(passengerMarker);

                                                        for (Marker marker : myMarkers) {

                                                            builder.include(marker.getPosition());

                                                        }

                                                        LatLngBounds bounds = builder.build();

                                                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 80);
                                                        mMap.animateCamera(cameraUpdate);
                                                    }

                                                }

                                            }

                                        }
                                    }
                                });



                            }
                        } else {
                            isCarReady = false;
                        }
                    }
                });

            }

        }, 0, 3000);                                             // The location of Driver will be updated every 3 seconds which will be shown to User who requested for the service



    }

}