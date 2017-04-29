package xyz.jcdc.cupquake.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by jcdc on 4/29/17.
 */

public class Item implements ClusterItem {

    private LatLng mPosition;
    private String mTitle;
    private String mSnippet;

    public Item(LatLng mPosition) {
        this.mPosition = mPosition;
    }

    public Item(LatLng mPosition, String mTitle, String mSnippet) {
        this.mPosition = mPosition;
        this.mTitle = mTitle;
        this.mSnippet = mSnippet;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }
}
