package com.margdarshak.ui.slideshow;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mapbox.api.directions.v5.models.DirectionsRoute;

import java.util.List;

public class SlideshowViewModel extends ViewModel {

    private MutableLiveData<List> mRoutes;

    public SlideshowViewModel() {
        mRoutes = new MutableLiveData<>();
    }
    public SlideshowViewModel(List<DirectionsRoute> routes) {
        mRoutes = new MutableLiveData<>();
        mRoutes.setValue(routes);
    }
    public MutableLiveData<List> getRoutes() {
        return mRoutes;
    }
}