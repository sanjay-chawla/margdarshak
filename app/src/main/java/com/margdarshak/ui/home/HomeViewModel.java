package com.margdarshak.ui.home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mapbox.api.directions.v5.models.DirectionsRoute;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<List<DirectionsRoute>> mRoutes;
    private MutableLiveData<DirectionsRoute> selectedRoute;
    private MutableLiveData<Boolean> routeSelected;

    public HomeViewModel() {
        routeSelected = new MutableLiveData<>();
        selectedRoute = new MutableLiveData<>();
        mRoutes = new MutableLiveData<>();
    }

    public void setRoutes(List<DirectionsRoute> routes){
        mRoutes.setValue(routes);
    }
    public MutableLiveData<List<DirectionsRoute>> getRoutes() {
        return mRoutes;
    }

    public MutableLiveData<Boolean> getRouteSelected() {
        return routeSelected;
    }

    public void setRouteSelected(Boolean routeSelected) {
        this.routeSelected.setValue(routeSelected);
    }
    public MutableLiveData<DirectionsRoute> getSelectedRoute() {
        return selectedRoute;
    }

    public void setSelectedRoute(DirectionsRoute selectedRoute) {
        this.selectedRoute.setValue(selectedRoute);
    }

}