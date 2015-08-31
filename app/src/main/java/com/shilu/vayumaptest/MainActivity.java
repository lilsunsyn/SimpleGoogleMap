package com.shilu.vayumaptest;

import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, UpdateMap {

    private static final String ERROR_NO_GEOCODER = "No Geocoder Available";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private AddressResultReceiver mResultReceiver;
    private MarkerOptions marker;
    private TextWatcher textWatcher;
    private EditText etAddress;
    private EditText etStreet;
    private EditText etPostal;
    private EditText etZipCode;
    private boolean firstEntry = true;

    private ArrayList<String> addressFragments = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResultReceiver = new AddressResultReceiver(new Handler(), this);

        marker = new MarkerOptions();

        buildGoogleApiClient();

        setUpMapIfNeeded();

        etAddress = (EditText) findViewById(R.id.address);
        etStreet = (EditText) findViewById(R.id.street);
        etPostal = (EditText) findViewById(R.id.postal);
        etZipCode = (EditText) findViewById(R.id.zip);

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                if (charSequence.toString().contains(" ") || charSequence.toString().contains(",")) {
                    if (!TextUtils.isEmpty(etAddress.getText().toString())) {
                        addressFragments.set(0, etAddress.getText().toString());
                    }
                    if (!TextUtils.isEmpty(etPostal.getText().toString())) {
                        addressFragments.set(2, etPostal.getText().toString());
                    }
                    if (!TextUtils.isEmpty(etZipCode.getText().toString())) {
                        addressFragments.set(3, etZipCode.getText().toString());
                    }
                    if (!TextUtils.isEmpty(etStreet.getText().toString())) {
                        addressFragments.set(1, etStreet.getText().toString());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String address = TextUtils.join(System.getProperty(Constants.Map.LINE_SEPARATOR),
                        addressFragments);

                if (address.toString().contains(" ") || address.toString().contains(",")) {
                    fetchAddressButtonHandler(address);
                }

            }
        };

        // set array of length 4 and create a index position for each address which can be replaced later
        if (firstEntry) {
            addressFragments.add(0, etAddress.getText().toString());
            addressFragments.add(1, etStreet.getText().toString());
            addressFragments.add(2, etPostal.getText().toString());
            addressFragments.add(3, etZipCode.getText().toString());
            firstEntry = false;
        }

        etAddress.addTextChangedListener(textWatcher);
        etStreet.addTextChangedListener(textWatcher);
        etPostal.addTextChangedListener(textWatcher);
        etZipCode.addTextChangedListener(textWatcher);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    private void fetchAddressButtonHandler(String address) {
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startIntentService(address);
        }
    }

    private void startIntentService(String address) {
        Intent intent = new Intent(this, FetchAddressService.class);
        intent.putExtra(Constants.Map.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.Map.LOCATION_DATA_EXTRA, mLastLocation);
        intent.putExtra(Constants.Map.LOCATION_NAME, address);
        intent.putExtra(Constants.Map.GET_LOCATION_FROM, Constants.Map.GET_LOCATION_FROM_LOCATION);

        startService(intent);
    }

    private void setMarker(MapObject mAddressOutput) {
        if(mMap != null){
            LatLng latLng = new LatLng(mAddressOutput.getLat(), mAddressOutput.getLng());
            mMap.clear();
            mMap.addMarker(marker.position(latLng).title(""));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0f));
            mMap.setMyLocationEnabled(true);
        }
    }


    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Gets the best and most recent location currently available,
        // which may be null in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null && mMap != null) {
            LatLng latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.addMarker(marker.position(latlng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 14.0f));
            mMap.setMyLocationEnabled(true);

            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, ERROR_NO_GEOCODER,
                        Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        buildGoogleApiClient();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia, and move the camera.
        mMap = googleMap;
    }

    //for dropdown list
    @Override
    public void updateMapUI(int resultCode, ArrayList<MapObject> mapListOutput) {

    }

    @Override
    public void updateMapUI(int resultCode, String message, MapObject mapObject) {
        if (resultCode == Constants.Map.SUCCESS_RESULT) {
            showToast(mapObject.getAddress());
            setMarker(mapObject);
        } else {
            showToast(message);
        }
    }
}
