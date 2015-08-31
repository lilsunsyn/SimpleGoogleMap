package com.shilu.vayumaptest;

import java.util.ArrayList;

/**
 * Created by shilushrestha on 8/31/15.
 */
public interface UpdateMap {
    public void updateMapUI(int resultCode, ArrayList<MapObject> mapListOutput);
    public void updateMapUI(int resultCode, String mAddressOutput, MapObject mapObject);
}
