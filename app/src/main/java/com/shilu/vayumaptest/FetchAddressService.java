package com.shilu.vayumaptest;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FetchAddressService extends IntentService {

    private static final String TAG = "Service";
    private static final String SERVICE_NAME = "FetchAddressService";

    private static final String ERROR_NO_GEOCODER = "No geocoder available";
    private static final String ERROR_INVALID_LAT_LNG = "Invalid latitude or longitude used";
    private static final String ERROR_NO_ADDRESS_FOUND = "Sorry, no address found";
    private int maxResult = 10;

    public FetchAddressService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mReceiver = intent.getParcelableExtra(Constants.Map.RECEIVER);

        // Check if receiver was properly registered.
        if (mReceiver == null) {
            Log.wtf(TAG, "No receiver received. There is nowhere to send the results.");
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        String errorMessage = "";

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(
                Constants.Map.LOCATION_DATA_EXTRA);
        String locationName = intent.getStringExtra(
                Constants.Map.LOCATION_NAME);
        // Incase needed
        String getFrom = intent.getStringExtra(Constants.Map.GET_LOCATION_FROM);


        List<Address> addresses = null;
        MapObject mapObject = new MapObject();

        try {
            if (getFrom.equals(Constants.Map.GET_LOCATION_FROM_LOCATION)) {
                if (!TextUtils.isEmpty(locationName)) {
                    addresses = geocoder.getFromLocationName(locationName, maxResult);
                }
            } else {
                addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        maxResult);
            }

        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = ERROR_NO_GEOCODER;
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = ERROR_INVALID_LAT_LNG;
            Log.e(TAG, errorMessage, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = ERROR_NO_ADDRESS_FOUND;
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.Map.FAILURE_RESULT, errorMessage, mapObject);
        } else {
            for (int i = 0; i < addresses.size(); i++) {
                Address address = addresses.get(i);
                System.out.println("Address " + i + " " + address.getAddressLine(0));

            }
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            mapObject.setLat(address.getLatitude());
            mapObject.setLng(address.getLongitude());

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for (int j = 0; j < address.getMaxAddressLineIndex(); j++) {
                addressFragments.add(address.getAddressLine(j));
            }
            mapObject.setAddress(TextUtils.join(System.getProperty(Constants.Map.LINE_SEPARATOR),
                    addressFragments));

            deliverResultToReceiver(Constants.Map.SUCCESS_RESULT, "Location Fetched", mapObject);
        }

    }

    ResultReceiver mReceiver;

    // for later case when list is used
    private void deliverResultToReceiver(int resultCode, String message, ArrayList<MapObject> mapList) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constants.Map.RESULT_DATA_MAP_LIST_KEY, mapList);
        bundle.putString(Constants.Map.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

    private void deliverResultToReceiver(int resultCode, String message, MapObject mapObject) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.Map.RESULT_DATA_MAP_KEY, mapObject);
        bundle.putString(Constants.Map.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }
}
