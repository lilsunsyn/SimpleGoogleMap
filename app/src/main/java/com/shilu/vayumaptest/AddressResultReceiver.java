package com.shilu.vayumaptest;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import java.util.ArrayList;

/**
 * Created by shilushrestha on 8/31/15.
 */
public class AddressResultReceiver extends ResultReceiver {
    UpdateMap updateMapListener;

    public AddressResultReceiver(Handler handler, UpdateMap updateMapListener) {
        super(handler);
        this.updateMapListener = updateMapListener;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        // Display the address string
        // or an error message sent from the intent service.
        String mAddressOutput = resultData.getString(Constants.Map.RESULT_DATA_KEY);
        MapObject mMapAddressOutput = resultData.getParcelable(Constants.Map.RESULT_DATA_MAP_KEY);

        // MapObject list for Dropdown
        // mMapListOutput = resultData.getParcelableArrayList(Constants.Map.RESULT_DATA_MAP_LIST_KEY);

        updateMapListener.updateMapUI(resultCode, mAddressOutput, mMapAddressOutput);
    }
}
