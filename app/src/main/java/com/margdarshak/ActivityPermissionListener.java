package com.margdarshak;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

public interface ActivityPermissionListener extends PermissionsListener {
    void requestLocationPermission(LocationPermissionCallback locationPermissionCallback);

    abstract class LocationPermissionCallback {
        MapboxMap mapboxMap;
        Style style;

        public LocationPermissionCallback(MapboxMap mapboxMap, Style style) {
            this.mapboxMap = mapboxMap;
            this.style = style;
        }

        abstract public void onGrant();
        abstract public void onDenial();
    }
}
