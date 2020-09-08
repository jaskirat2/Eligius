package in.techaddicts.eligius;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ViewLocationsMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Button btnRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_locations_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnRide = findViewById(R.id.btnRide);
        btnRide.setText("I'm on my way " + getIntent().getStringExtra("rUsername"));

        btnRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
                carRequestQuery.whereEqualTo("username", getIntent().getStringExtra("rUsername"));   //Want passenger that is clicked by driver....//Not all the requests are allowe..
                carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e)
                    {
                        if (objects.size() > 0 && e == null)
                        {
                            for (ParseObject uberRequest : objects)
                            {
                                uberRequest.put("requestAccepted", true);                             //driver wants passenger a ride...
                                uberRequest.put("driverOfMe", ParseUser.getCurrentUser().getUsername());

                                uberRequest.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e)
                                    {
                                        if(e == null)
                                        {
                                            Intent googleIntent = new Intent(Intent.ACTION_VIEW,                         //Open a ride in the google map...
                                                    Uri.parse("http://maps.google.com/maps?saddr="
                                                            + getIntent().getDoubleExtra("dLatitude",
                                                            0) + ","
                                                            + getIntent().getDoubleExtra("dLongitude",
                                                            0) + "&" + "daddr="
                                                            + getIntent().getDoubleExtra("pLatitude",
                                                            0) + "," +
                                                            getIntent().getDoubleExtra("pLongitude",
                                                                    0)));
                                            startActivity(googleIntent);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;


        // Add a marker in Sydney and move the camera
        LatLng dLocation = new LatLng(getIntent().getDoubleExtra("dLatitude",0),    //Location of driver..
                getIntent().getDoubleExtra("dLongitude",0));                       //DoubleExtra is used because value of out longitude and latitude is in double...

        LatLng pLocation = new LatLng(getIntent().getDoubleExtra("pLatitude", 0),
                getIntent().getDoubleExtra("pLongitude", 0));                    //Location of passenger....

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker driverMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver Location"));
        Marker passengerMarker = mMap.addMarker(new MarkerOptions().position(pLocation));

        ArrayList<Marker> myMarkers = new ArrayList<>();                          //Create array list...
        myMarkers.add(driverMarker);                                               //add these markers in the array list....
        myMarkers.add(passengerMarker);

        for (Marker marker : myMarkers) {
            builder.include(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        mMap.animateCamera(cameraUpdate);
    }                                                   //With this code there are two markers in the map..
}