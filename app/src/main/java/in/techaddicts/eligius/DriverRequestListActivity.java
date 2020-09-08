package in.techaddicts.eligius;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

import es.dmoral.toasty.Toasty;

public class DriverRequestListActivity extends AppCompatActivity implements View.OnClickListener , AdapterView.OnItemClickListener {

    private Button btnGetRequests;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private ListView listView;
    private ArrayList<String> nearByDriveRequests;
    private ArrayAdapter adapter;

    private ArrayList<Double> passengersLatitudes;
    private ArrayList<Double> passengersLongitudes;

    private ArrayList<String> requestcarUsernames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);

        btnGetRequests = findViewById(R.id.btnGetRequests);
        btnGetRequests.setOnClickListener(this);

        listView = findViewById(R.id.requestListView);
        nearByDriveRequests = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nearByDriveRequests);
        listView.setAdapter(adapter);

        nearByDriveRequests.clear();          //clear the arraylist...

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT < 23 ||ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)     //if permission is granted to access the location..
        {
            initializeLocationListener();
        }

        listView.setOnItemClickListener(this);    //when person click on item in the list..
        passengersLatitudes = new ArrayList<>();
        passengersLongitudes = new ArrayList<>();

        requestcarUsernames = new ArrayList<>();     //Initialize....

    }



    //Start code of logout driver...
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.driverLogoutItem) {
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null)
                    {
                        finish();
                    }
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }                                                     //Ending of code of logout driver....


    @Override
    public void onClick(View view)                   //When user taps on get nearby requests....
    {
        if (Build.VERSION.SDK_INT < 23)          //Then dont ask for permissiom also ask for requestLocationUpdate..             //Without this the app crashed..            ..... Allow or reject option..
        {
            Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestsListView(currentDriverLocation);
        }
        else if (Build.VERSION.SDK_INT >= 23)
        {
            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)  //if permission is not granted by the user to access location..
            {
                ActivityCompat.requestPermissions(DriverRequestListActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);    //Then ask the user to give permission..

            } else {            //If user give us the permission then
                // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestsListView(currentDriverLocation);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            if (ContextCompat.checkSelfPermission(DriverRequestListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                initializeLocationListener();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestsListView(currentDriverLocation);

            }
        }

    }

    private void updateRequestsListView(Location driverLocation)
    {
        if (driverLocation != null)
        {
            saveDriverLocationToParse(driverLocation);

            final ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(driverLocation.getLatitude(), driverLocation.getLongitude());
            ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");                 //Who are requesting for cars..
            requestCarQuery.whereNear("passengerLocation", driverCurrentLocation);                 // Near to driver location..
            requestCarQuery.whereDoesNotExist("driverOfMe");                                //if driver is not assigned then add that request query ....

            requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e)
                {
                    if(e==null)
                    {
                        if (objects.size() > 0 )
                        {

                            if (nearByDriveRequests.size() > 0) {
                                nearByDriveRequests.clear();
                            }
                            if (passengersLatitudes.size() > 0) {
                                passengersLatitudes.clear();
                            }
                            if (passengersLongitudes.size() > 0) {
                                passengersLongitudes.clear();
                            }
                            if (requestcarUsernames.size() > 0) {               //when person click on the button then clear the previous array
                                requestcarUsernames.clear();                   // Below the values are added to the arraylist...
                            }


                            for (ParseObject nearRequest : objects)
                            {
                                ParseGeoPoint pLocation = (ParseGeoPoint) nearRequest.get("passengerLocation");
                                Double milesDistanceToPassenger = driverCurrentLocation.distanceInMilesTo(pLocation);

                                // 5.87594834787398943 * 10

                                //  58.246789 // Result
                                // 58
                                float roundedDistanceValue = milesDistanceToPassenger.floatValue();

                                String ja = String.format("%.2f", roundedDistanceValue);

                                nearByDriveRequests.add("There are " + ja + " miles to " + nearRequest.get("username"));

                                passengersLatitudes.add(pLocation.getLatitude());
                                passengersLongitudes.add(pLocation.getLongitude());

                                requestcarUsernames.add(nearRequest.get("username") + "");

                            }
                        }
                        else
                        {
                            Toast t = Toasty.info(DriverRequestListActivity.this, "No Assistance requests", Toast.LENGTH_SHORT);
                            t.setGravity(Gravity.CENTER, 0,-400);
                            t.show();
                        }
                        adapter.notifyDataSetChanged();    //update list view..
                    }
                }
            });


        }
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)  //When a person click on item means click on user s location in the list..
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            Location cdLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (cdLocation != null)
            {
                Intent intent = new Intent(this, ViewLocationsMapActivity.class);
                intent.putExtra("dLatitude", cdLocation.getLatitude());
                intent.putExtra("dLongitude", cdLocation.getLongitude());
                intent.putExtra("pLatitude", passengersLatitudes.get(position));        //array
                intent.putExtra("pLongitude", passengersLongitudes.get(position));      //array

                intent.putExtra("rUsername", requestcarUsernames.get(position));
                startActivity(intent);
            }
        }
    }

    private void initializeLocationListener()
    {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
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
    }

    private void saveDriverLocationToParse(Location location)  //save driver location to parse...
    {
        ParseUser driver = ParseUser.getCurrentUser();
        ParseGeoPoint driverLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        driver.put("driverLocation", driverLocation);
        driver.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast t = Toasty.success(DriverRequestListActivity.this, "Location Saved", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER, 0,0);
                    t.show();
                }
            }
        });
    }

}