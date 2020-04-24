package com.margdarshak.ui.home;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.DirectionsService;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.MapboxService;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceAutocompleteFragment;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceSelectionListener;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.margdarshak.ActivityPermissionListener;
import com.margdarshak.R;
import com.margdarshak.RouteFragment;
import com.margdarshak.routing.MargdarshakDirection;
import com.margdarshak.routing.OSRMService;
import com.margdarshak.ui.data.model.BikesData;
import com.margdarshak.ui.data.model.BusData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.switchCase;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineDasharray;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class HomeFragment extends Fragment implements
        OnMapReadyCallback {

    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final String SEARCH_FRAGMENT_TAG = HomeFragment.class.getSimpleName().concat("_search");
    private static final String ROUTE_FRAGMENT_TAG = HomeFragment.class.getSimpleName().concat("_route");
    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private static final String PLACE_ICON_SOURCE_ID = "place-icon-source-id";
    private static final String PLACE_MARKER = "placeMarker";
    private static final String PLACE_ICON_LAYER_ID = "place-layer-id";
    private static final String ICON_LAYER_ID = "icon-layer-id";
    private static final String ICON_SOURCE_ID = "icon-source-id";
    private static final String RED_PIN_ICON_ID = "red-pin-icon-id";
    private static final String PROPERTY_SELECTED = "selected";
    private static final String BUS_PROPERTY_SELECTED = "selected";
    private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
    private static final String BUS_GEOJSON_SOURCE_ID = "BUS_GEOJSON_SOURCE_ID";
    private static final String MARKER_IMAGE_ID = "COLOR_IMAGE_ID";
    private static final String BUS_MARKER_IMAGE_ID = "BUS_COLOR_IMAGE_ID";
    private static final String GREY_IMAGE_ID = "GREY_IMAGE_ID";
    private static final String MARKER_LAYER_ID = "COLOR_LAYER_ID";
    private static final String BUS_MARKER_LAYER_ID = "BUS_COLOR_LAYER_ID";
    private static final String CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID";
    private static final String BUS_CALLOUT_LAYER_ID = "BUS_CALLOUT_LAYER_ID";
    private static final String PROPERTY_NAME = "name";
    private static final String BUS_PROPERTY_NAME = "fullname";
    private static final String AVAILABLE_BIKE_STANDS = "Available bike stands";
    private static final String AVAILABLE_BIKES = "Available bikes";
    public static final int CAMERA_ANIMATION_TIME = 2000;
    private static final int PLACE_SELECTOR_REQUEST_CODE = 899;
    private static final String PLACE_PICKER_LAYER_ID = "place-picker-layer-id";
    public static final String ROUTES = "Routes";
    private MapboxMap mapboxMap;
    private ImageButton myLocationButton;
    private Chip getDirectionButton;
    private ActivityPermissionListener permissionResultListener;
    private FrameLayout searchFragmentContainer;
    private EditText searchTextBox;
    private LocationEngine locationEngine;
    private LocationComponent locationComponent;
    private PlaceAutocompleteFragment autocompleteFragment;
    private RouteFragment routeFragment;
    private CarmenFeature selectedPoint;
    private HomeViewModel homeViewModel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ActivityPermissionListener) {
            permissionResultListener = (ActivityPermissionListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement ActivityPermissionListener");
        }
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        this.mapboxMap.setStyle(Style.MAPBOX_STREETS,
                style -> {
                    permissionResultListener
                            .requestLocationPermission(new ActivityPermissionListener.LocationPermissionCallback(mapboxMap, style) {
                                @Override
                                public void onGrant() {
                                    Log.d(TAG, "granted.. now enabling location component");
                                    enableLocationComponent(style);
                                }

                                @Override
                                public void onDenial() {
                                    Log.d(TAG, "denied.. should show location button");
                                    enableLocationComponent(style);
                                    myLocationButton.setOnClickListener(v -> permissionResultListener.requestLocationPermission(this));
                                    myLocationButton.setVisibility(View.VISIBLE);
                                }
                            });
                    style.addImage(PLACE_MARKER, getResources().getDrawable(R.drawable.location_on_accent_36dp, null));
                    style.addSource(new GeoJsonSource(PLACE_ICON_SOURCE_ID));
                    style.addLayer(new SymbolLayer(PLACE_ICON_LAYER_ID, PLACE_ICON_SOURCE_ID).withProperties(
                            iconImage(PLACE_MARKER),
                            iconIgnorePlacement(true),
                            iconAllowOverlap(true),
                            iconOffset(new Float[]{0f, -8f})
                    ));
                    new LoadGeoJsonDataTask(getActivity(), mapboxMap).execute();
                    new LoadGeoJsonDataTask_bus(getActivity(), mapboxMap).execute();
                });
        setUpSearch();
        mapboxMap.addOnMapClickListener(point -> {
            makeGeocodeSearch(point);
            return false;
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(getActivity()).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        homeViewModel.getRoutes().observe(getViewLifecycleOwner(), routes -> {

            if (mapboxMap != null) {
                for (DirectionsRoute route : routes) {
                    mapboxMap.getStyle(style -> {
                        Log.d(TAG, "onResponse: source != null");
                        LineString ls = LineString.fromPolyline(route.geometry(),
                                PRECISION_6);
                        initRouteSource(style, Integer.parseInt(route.routeIndex()), route.routeOptions().profile(), Feature.fromGeometry(ls));
                        initRouteLayer(style, Integer.parseInt(route.routeIndex()), route.routeOptions().profile());

                    });
                }
            }
        });
        homeViewModel.getSelectedRoute().observe(getViewLifecycleOwner(), selectedRoute -> {
            hideRouteFragment();
            setUpSearch();
            if (mapboxMap != null) {
                for (DirectionsRoute route : homeViewModel.getRoutes().getValue()) {
                    if (route != selectedRoute) {
                        mapboxMap.getStyle(style -> {
                            style.removeLayer(ROUTE_LAYER_ID.concat("_" + route.routeOptions().profile()).concat("_" + route.routeIndex()));
                        });
                    }
                }
            }
        });

        getDirectionButton = root.findViewById(R.id.get_directions);
        MapView mapView = root.findViewById(R.id.mapView);
        myLocationButton = root.findViewById(R.id.locationFAB);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        searchFragmentContainer = root.findViewById(R.id.search_fragment_container);
        searchTextBox = root.findViewById(R.id.search_box_text);
        setUpSearchFragment(savedInstanceState);
        return root;
    }

    private Feature getPointFeatures(LatLng point) {
        PointF screenPoint = mapboxMap.getProjection().toScreenLocation(point);
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, MARKER_LAYER_ID, BUS_MARKER_LAYER_ID);
        Feature feature = null;
        if (!features.isEmpty()) {
            feature = features.get(0);
            // TODO: use features if needed
            //Toast.makeText(getActivity(), stringBuilder.toString(), Toast.LENGTH_SHORT).show();
        } else {
            // Toast.makeText(getActivity(), "No properties found", Toast.LENGTH_SHORT).show();
        }
        return feature;
    }

    private void makeGeocodeSearch(final LatLng latLng) {
        try {
            mapboxMap.getStyle(loadedMapStyle -> {
                GeoJsonSource source = loadedMapStyle.getSourceAs(PLACE_ICON_SOURCE_ID);
                if (source != null) {
                    source.setGeoJson(Point.fromLngLat(latLng.getLongitude(),
                            latLng.getLatitude()));
                }
            });
            Feature customFeature = getPointFeatures(latLng);
            // Build a Mapbox geocoding request
            MapboxGeocoding client = MapboxGeocoding.builder()
                    .accessToken(getString(R.string.mapbox_access_token))
                    .query(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()))
                    .geocodingTypes(GeocodingCriteria.TYPE_POI,
                            GeocodingCriteria.TYPE_POI_LANDMARK,
                            GeocodingCriteria.TYPE_ADDRESS,
                            GeocodingCriteria.TYPE_PLACE
                    )
                    .build();
            client.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call,
                                       Response<GeocodingResponse> response) {
                    if (response.body() != null) {
                        List<CarmenFeature> results = response.body().features();
                        if (results.size() > 0) {
                            CarmenFeature feature = results.get(0);
                            displayPlaceInfo(feature, customFeature);
                            selectedPoint = feature;
                        } else {
                            Toast.makeText(getActivity(), "No result found",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                    Log.e(TAG, "Geocoding Failure: " + throwable.getMessage());
                }
            });
        } catch (ServicesException servicesException) {
            Log.e(TAG, "Error geocoding: " + servicesException.toString());
        }
    }

    private void displayPlaceInfo(CarmenFeature carmenFeature, Feature customFeature) {
        CardView infoCard = getView().findViewById(R.id.info_frame);
        getView().findViewById(R.id.customPanel).setVisibility(View.GONE);
        infoCard.setVisibility(View.VISIBLE);
        getView().findViewById(R.id.close_info).setOnClickListener(v -> {
            infoCard.setVisibility(View.GONE);
        });
        if (customFeature != null) {
            if (customFeature.properties() != null) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Map.Entry<String, JsonElement> entry : customFeature.properties().entrySet()) {
                    if (customFeature.getStringProperty("type").equals("bus")) {
                        ((TextView) getView().findViewById(R.id.selected_location_info_text)).setText("DublinBus Stop: " + customFeature.getStringProperty("stopid"));
                        ((TextView) getView().findViewById(R.id.selected_location_info_address)).setText(customFeature.getStringProperty("name"));
                        ((ImageView) getView().findViewById(R.id.customFeatureIcon)).setImageResource(R.drawable.bus);
                        if (ROUTES.equals(entry.getKey())) {
                            String arrayString = entry.getValue().toString();
                            arrayString = arrayString.replace("[","");
                            arrayString = arrayString.replace("]","");
                            arrayString = arrayString.replace("\""," ");
                            stringBuilder.append(String.format("%s - %s", entry.getKey(), arrayString));
                            stringBuilder.append(System.getProperty("line.separator"));
                        }
                    } else if (customFeature.getStringProperty("type").equals("bike")) {
                        ((TextView) getView().findViewById(R.id.selected_location_info_text)).setText("DublinBikes: " + customFeature.getStringProperty("name"));
                        ((TextView) getView().findViewById(R.id.selected_location_info_address)).setText(customFeature.getStringProperty("address"));
                        ((ImageView) getView().findViewById(R.id.customFeatureIcon)).setImageResource(R.drawable.bikes_logo_50);
                        if (AVAILABLE_BIKE_STANDS.equals(entry.getKey()) || AVAILABLE_BIKES.equals(entry.getKey())) {
                            stringBuilder.append(String.format("%s - %s", entry.getKey(), (int) Float.parseFloat(String.valueOf(entry.getValue()))));
                            stringBuilder.append(System.getProperty("line.separator"));
                        }
                    }
                    ((TextView) getView().findViewById(R.id.customFeatureText)).setText(stringBuilder);
                }
            }
            getView().findViewById(R.id.customPanel).setVisibility(View.VISIBLE);
        } else {
            ((TextView) getView().findViewById(R.id.selected_location_info_text)).setText(carmenFeature.text());
            String address = carmenFeature.placeName().replaceFirst(carmenFeature.text().concat(", "), "");
            ((TextView) getView().findViewById(R.id.selected_location_info_address)).setText(address);
        }
    }

    private void hidePlaceInfo() {
        CardView infoCard = getView().findViewById(R.id.info_frame);
        infoCard.setVisibility(View.GONE);
    }

    private void setUpSearchFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            PlaceOptions placeOptions = PlaceOptions.builder()
                    .backgroundColor(getResources().getColor(R.color.colorWhite, null))
                    .build(PlaceOptions.MODE_FULLSCREEN);
            autocompleteFragment = PlaceAutocompleteFragment.newInstance(getContext()
                    .getResources().getString(R.string.mapbox_access_token), placeOptions);

            getChildFragmentManager().beginTransaction()
                    .add(R.id.search_fragment_container, autocompleteFragment, SEARCH_FRAGMENT_TAG)
                    .hide(autocompleteFragment)
                    .commit();
        } else {
            autocompleteFragment = (PlaceAutocompleteFragment)
                    getParentFragmentManager().findFragmentByTag(SEARCH_FRAGMENT_TAG);
        }
    }

    private void setUpRouteFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (mapboxMap != null && selectedPoint != null) {
                Location currentLocation = mapboxMap.getLocationComponent().getLastKnownLocation();
                routeFragment = RouteFragment.newInstance(1, selectedPoint.center().latitude(), selectedPoint.center().longitude(),
                        currentLocation.getLatitude(), currentLocation.getLongitude());
            } else {
                routeFragment = RouteFragment.newInstance(1);
            }
            getChildFragmentManager().beginTransaction()
                    .add(R.id.route_list_fragment_container, routeFragment, ROUTE_FRAGMENT_TAG)
                    .hide(routeFragment)
                    .commit();
        } else {
            routeFragment = (RouteFragment)
                    getParentFragmentManager().findFragmentByTag(ROUTE_FRAGMENT_TAG);
        }
    }

    private void showRouteFragment() {
        getView().findViewById(R.id.route_lower).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.route_upper).setVisibility(View.VISIBLE);
        ((TextInputEditText) getView().findViewById(R.id.destination_text)).setText(selectedPoint.placeName());
        getChildFragmentManager().beginTransaction()
                .replace(R.id.route_list_fragment_container, routeFragment, ROUTE_FRAGMENT_TAG)
                .show(routeFragment)
                .commit();

        getView().findViewById(R.id.route_back).setOnClickListener(v -> {
            hideRouteFragment();
            setUpSearch();
            if (mapboxMap != null) {
                for (DirectionsRoute route : homeViewModel.getRoutes().getValue()) {
                    mapboxMap.getStyle(style -> {
                        style.removeLayer(ROUTE_LAYER_ID.concat("_" + route.routeOptions().profile()).concat("_" + route.routeIndex()));
                        style.removeLayer(ICON_LAYER_ID);
                        style.removeSource(ROUTE_SOURCE_ID.concat("_" + route.routeOptions().profile()).concat("_" + route.routeIndex()));
                    });
                }
            }
        });

        mapboxMap.addOnMapClickListener(point -> {
            return false;
        });

    }

    private void hideRouteFragment() {
        getView().findViewById(R.id.route_lower).setVisibility(View.GONE);
        getView().findViewById(R.id.route_upper).setVisibility(View.GONE);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.route_list_fragment_container, routeFragment, ROUTE_FRAGMENT_TAG)
                .hide(routeFragment)
                .commit();
        moveCameraTo(selectedPoint.center().latitude(), selectedPoint.center().longitude());
        mapboxMap.addOnMapClickListener(point -> {
            makeGeocodeSearch(point);
            return false;
        });
    }

    private void hideSearch() {
        contractSearch(autocompleteFragment);
        searchTextBox.setVisibility(View.GONE);
    }

    private void setUpSearch() {
        searchTextBox.setVisibility(View.VISIBLE);
        searchTextBox.setBackgroundColor(getResources().getColor(R.color.colorWhite, null));
        searchFragmentContainer.setClipToOutline(true);
        searchTextBox.setClipToOutline(true);
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(CarmenFeature carmenFeature) {
                Toast.makeText(getContext(), carmenFeature.text(), Toast.LENGTH_LONG).show();
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                mapboxMap.getStyle(loadedMapStyle -> {
                    GeoJsonSource source = loadedMapStyle.getSourceAs(PLACE_ICON_SOURCE_ID);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[]{Feature.fromJson(carmenFeature.toJson())}));
                    }
                    //loadedMapStyle.removeLayer(ROUTE_LAYER_ID);
                    // Move map camera to the selected location
                    moveCameraTo(((Point) carmenFeature.geometry()).latitude(),
                            ((Point) carmenFeature.geometry()).longitude());


                });
                displayPlaceInfo(carmenFeature, null);
                selectedPoint = carmenFeature;
                locationComponent.setCameraMode(CameraMode.NONE);
                myLocationButton.setVisibility(View.VISIBLE);
                finish();
            }

            @Override
            public void onCancel() {
                finish();
            }

            private void finish() {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                contractSearch(autocompleteFragment);
            }
        });

        searchTextBox.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                expandSearch(autocompleteFragment, imm);
            }
        });
    }

    private void expandSearch(PlaceAutocompleteFragment autocompleteFragment, InputMethodManager imm) {
        getChildFragmentManager().beginTransaction()
                .show(autocompleteFragment)
                .commit();
        EditText fragmentSearch = autocompleteFragment.getView().findViewById(R.id.edittext_search);
        RecyclerView resultView = autocompleteFragment.getView().findViewById(R.id.rv_search_results);
        autocompleteFragment.getView().setFocusableInTouchMode(true);
        autocompleteFragment.getView().findViewById(R.id.edittext_search).setOnKeyListener((view1, keyCode, keyEvent) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                fragmentSearch.clearFocus();
                int resultCount = resultView.getAdapter().getItemCount();
                if (fragmentSearch.getText().length() < 3) {
                    Toast.makeText(autocompleteFragment.getActivity(), "Enter at least 3 characters for accurate results", Toast.LENGTH_LONG).show();
                } else if (resultCount == 0) {
                    Toast.makeText(autocompleteFragment.getActivity(), "No result found", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(autocompleteFragment.getActivity(), "No place selected", Toast.LENGTH_LONG).show();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                autocompleteFragment.onBackButtonPress();
                return true;
            }
            return false;
        });
        fragmentSearch.requestFocus();
        imm.showSoftInput(getView(), InputMethodManager.SHOW_IMPLICIT);
    }

    private void contractSearch(PlaceAutocompleteFragment autocompleteFragment) {
        getChildFragmentManager().beginTransaction()
                .hide(autocompleteFragment)
                .commit();
        //imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        searchTextBox.clearFocus();
    }

    private CameraPosition moveCameraTo(double latitude, double longitude) {
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(14) // Sets the zoom
                .build(); // Creates a CameraPosition from the builder
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), CAMERA_ANIMATION_TIME);
        return position;

    }

    private void moveCameraTo(LatLng origin, LatLng destination) {
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(origin)
                .include(destination)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 500), CAMERA_ANIMATION_TIME);
    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(getContext());
        locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                moveCameraTo(result.getLastLocation().getLatitude(),
                        result.getLastLocation().getLongitude());
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "No last location");
            }
        });

    }

    @SuppressWarnings("MissingPermission")
    private void initializeLocationComponent(@NonNull Style loadedMapStyle) {
        locationComponent = mapboxMap.getLocationComponent();
        // Activate with options
        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions
                        .builder(getContext(), loadedMapStyle)
                        .build());
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING, CAMERA_ANIMATION_TIME, (double) 14, null, null, null);
        locationComponent.setRenderMode(RenderMode.COMPASS);
        locationComponent.addOnCameraTrackingChangedListener(new OnCameraTrackingChangedListener() {
            @Override
            public void onCameraTrackingDismissed() {
                Log.d(TAG, "tracking dismissed");
                myLocationButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCameraTrackingChanged(int currentMode) {
            }
        });
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
            initializeLocationEngine();
            initializeLocationComponent(loadedMapStyle);
            myLocationButton.setOnClickListener(v -> {
                myLocationButton.setVisibility(View.INVISIBLE);
                locationComponent.setCameraMode(CameraMode.TRACKING, CAMERA_ANIMATION_TIME, (double) 14, null, null, null);
            });


            // TODO: remove this
            getDirectionButton.setOnClickListener(view -> {
                if (selectedPoint != null) {
                    mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
                        Location currentLocation = mapboxMap.getLocationComponent().getLastKnownLocation();
                        Point origin = Point.fromLngLat(selectedPoint.center().longitude(), selectedPoint.center().latitude());
                        Point destination = Point.fromLngLat(currentLocation.getLongitude(), currentLocation.getLatitude());

                        // Add icons
                        style.addImage(RED_PIN_ICON_ID, BitmapFactory.decodeResource(getResources(),
                                R.drawable.mapbox_marker_icon_default));

                        // Add icon-layers to the map
                        style.addLayer(new SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID).withProperties(
                                iconImage(RED_PIN_ICON_ID),
                                iconIgnorePlacement(true),
                                iconAllowOverlap(true),
                                iconOffset(new Float[]{0f, -9f})));

                        GeoJsonSource iconGeoJsonSource = new GeoJsonSource(ICON_SOURCE_ID, FeatureCollection.fromFeatures(new Feature[]{
                                Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude())),
                                Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude()))}));
                        style.addSource(iconGeoJsonSource);

                        setUpRouteFragment(null);
                        showRouteFragment();
                        hidePlaceInfo();
                        hideSearch();

                        getView().findViewById(R.id.route_walk).setOnClickListener(v -> {
                            if (mapboxMap != null) {
                                for (DirectionsRoute route : homeViewModel.getRoutes().getValue()) {
                                    mapboxMap.getStyle(s -> {
                                        s.removeLayer(ROUTE_LAYER_ID.concat("_" + route.routeOptions().profile()).concat("_" + route.routeIndex()));
                                        s.removeSource(ROUTE_SOURCE_ID.concat("_" + route.routeOptions().profile()).concat("_" + route.routeIndex()));
                                    });
                                }
                            }
                            routeFragment.getRoute(origin, destination, DirectionsCriteria.PROFILE_WALKING);
                        });
                        getView().findViewById(R.id.route_bike).setOnClickListener(v -> {
                            if (mapboxMap != null) {
                                for (DirectionsRoute route : homeViewModel.getRoutes().getValue()) {
                                    mapboxMap.getStyle(s -> {
                                        s.removeLayer(ROUTE_LAYER_ID.concat("_" + route.routeOptions().profile()).concat("_" + route.routeIndex()));
                                        s.removeSource(ROUTE_SOURCE_ID.concat("_" + route.routeOptions().profile()).concat("_" + route.routeIndex()));
                                    });
                                }
                            }
                            routeFragment.getRoute(origin, destination, DirectionsCriteria.PROFILE_CYCLING);
                        });
                        getView().findViewById(R.id.route_bus).setOnClickListener(v -> {
                            if (mapboxMap != null) {
                                for (DirectionsRoute route : homeViewModel.getRoutes().getValue()) {
                                    mapboxMap.getStyle(s -> {
                                        s.removeLayer(ROUTE_LAYER_ID.concat("_" + route.routeOptions().profile()).concat("_" + route.routeIndex()));
                                        s.removeSource(ROUTE_SOURCE_ID.concat("_" + route.routeOptions().profile()).concat("_" + route.routeIndex()));
                                    });
                                }
                            }
                            routeFragment.getRouteCustom(origin, destination, DirectionsCriteria.PROFILE_DRIVING);
                        });
                        getView().findViewById(R.id.route_drive).setOnClickListener(v -> {
                            if (mapboxMap != null) {
                                for (DirectionsRoute route : homeViewModel.getRoutes().getValue()) {
                                    mapboxMap.getStyle(s -> {
                                        s.removeLayer(ROUTE_LAYER_ID.concat("_" + route.routeOptions().profile()).concat("_" + route.routeIndex()));
                                        s.removeSource(ROUTE_SOURCE_ID.concat("_" + route.routeOptions().profile()).concat("_" + route.routeIndex()));
                                    });
                                }
                            }
                            routeFragment.getRoute(origin, destination, DirectionsCriteria.PROFILE_DRIVING_TRAFFIC);
                        });

                        moveCameraTo(new LatLng(selectedPoint.center().latitude(), selectedPoint.center().longitude()),
                                new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                    });
                }
            });
        } else {
            final LocationComponent locationComponent = mapboxMap.getLocationComponent();
            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions
                            .builder(getContext(), loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build());
            locationComponent.setLocationComponentEnabled(false);
            locationComponent.setCameraMode(CameraMode.TRACKING, CAMERA_ANIMATION_TIME, (double) 14, null, null, null);
            locationComponent.setRenderMode(RenderMode.COMPASS);
        }
    }


    private void initRouteSource(@NonNull Style loadedMapStyle, int routeNumber, String profile, Feature feature) {
        feature.addStringProperty("profile", profile);
        loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID.concat("_" + profile).concat("_" + routeNumber),
                FeatureCollection.fromFeature(feature)));
    }


    private void initRouteLayer(@NonNull Style loadedMapStyle, int routeNumber, String profile) {
        String color = routeNumber == 0 ? "#009688" : "#" + Integer.toHexString(0xBBBBBBBB - 0x11111111 * routeNumber);
        Log.d(TAG, color);
        LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID.concat("_" + profile).concat("_" + routeNumber), ROUTE_SOURCE_ID.concat("_" + profile).concat("_" + routeNumber));
        Float[] f = new Float[2];
        if (profile == DirectionsCriteria.PROFILE_WALKING) {
            f[0] = 2.0f;
            f[1] = 2.0f;
        } else {
            f[0] = 20.0f;
            f[1] = 0.0f;
        }
        // Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(5f),
                lineColor(Color.parseColor(color)),
                lineDasharray(
                        f
                )
        );
        if (routeNumber == 0) {
            loadedMapStyle.addLayer(routeLayer);
        } else {
            loadedMapStyle.addLayerBelow(routeLayer, ROUTE_LAYER_ID.concat("_" + profile).concat("_0"));
        }
    }

    private void getRoute(MapboxMap mapboxMap, Point origin, Point destination) {
        MapboxService<DirectionsResponse, DirectionsService> client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(getString(R.string.mapbox_access_token))
                .build();
        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                Log.d(TAG, "call success with response: " + response);

                // You can get the generic HTTP info about the response
                Log.d(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.d(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Log.d(TAG, "No routes found");
                    return;
                }
                Log.d(TAG, "Response from mapbox: " + response.body().toString());
                // Get the directions route
                DirectionsRoute currentRoute = response.body().routes().get(0);

                // Make a toast which displays the route's distance
                Toast.makeText(getContext(), String.format(
                        getString(R.string.directions_activity_toast_message),
                        currentRoute.distance()), Toast.LENGTH_SHORT).show();

                if (mapboxMap != null) {
                    mapboxMap.getStyle(style -> {
                        // Retrieve and update the source designated for showing the directions route
                        GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID);

                        // Create a LineString with the directions route's geometry and
                        // reset the GeoJSON source for the route LineLayer source
                        if (source != null) {
                            Log.d(TAG, "onResponse: source != null");
                            source.setGeoJson(FeatureCollection.fromFeature(
                                    Feature.fromGeometry(LineString.fromPolyline(currentRoute.geometry(),
                                            PRECISION_6))));
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Log.e(TAG, "Error: " + throwable.getMessage());
                Toast.makeText(getContext(), "Error: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getRouteCustom(MapboxMap mapboxMap, Point origin, Point destination) {
        MapboxService<DirectionsResponse, OSRMService> client = MargdarshakDirection.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .baseUrl("http://34.93.158.237:5000/")
                .accessToken(getString(R.string.mapbox_access_token))
                .build();
        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                Log.d(TAG, "call success with response: " + response);

                // You can get the generic HTTP info about the response
                Log.d(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.d(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Log.d(TAG, "No routes found");
                    return;
                }
                Log.d(TAG, "Response from mapbox: " + response.body().toString());
                // Get the directions route
                DirectionsRoute currentRoute = response.body().routes().get(0);

                // Make a toast which displays the route's distance
                Toast.makeText(getContext(), String.format(
                        getString(R.string.directions_activity_toast_message),
                        currentRoute.distance()), Toast.LENGTH_SHORT).show();

                if (mapboxMap != null) {
                    mapboxMap.getStyle(style -> {
                        // Retrieve and update the source designated for showing the directions route
                        GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID);

                        // Create a LineString with the directions route's geometry and
                        // reset the GeoJSON source for the route LineLayer source
                        if (source != null) {
                            Log.d(TAG, "onResponse: source != null");
                            source.setGeoJson(FeatureCollection.fromFeature(
                                    Feature.fromGeometry(LineString.fromPolyline(currentRoute.geometry(),
                                            PRECISION_6))));
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Log.e(TAG, "Error: " + throwable.getMessage());
                Toast.makeText(getContext(), "Error: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class LoadGeoJsonDataTask extends AsyncTask<Void, Void, FeatureCollection> {

        private final WeakReference<Activity> activityRef;
        private final MapboxMap mapboxMap;
        private GeoJsonSource source;
        private FeatureCollection featureCollection;


        LoadGeoJsonDataTask(Activity activity, MapboxMap mapboxMap) {
            this.activityRef = new WeakReference<>(activity);
            this.mapboxMap = mapboxMap;
            //this.featureCollection = GalleryFragment.featureCollection;

        }

        @Override
        protected FeatureCollection doInBackground(Void... params) {
            Activity activity = activityRef.get();

            if (activity == null) {
                return null;
            }

            return FeatureCollection.fromFeatures(loadGeoJsonFromAsset(activity));

        }

        @Override
        protected void onPostExecute(FeatureCollection featureCollection) {
            super.onPostExecute(featureCollection);
            Activity activity = activityRef.get();

            if (featureCollection == null || activity == null) {
                return;
            }

            for (Feature singleFeature : featureCollection.features()) {
                singleFeature.addBooleanProperty(PROPERTY_SELECTED, false);
            }

            this.featureCollection = featureCollection;
            if (mapboxMap != null) {
                mapboxMap.getStyle(style -> {
                    source = new GeoJsonSource(GEOJSON_SOURCE_ID, featureCollection);
                    style.addSource(source);
                    style.addImage(MARKER_IMAGE_ID, BitmapFactory.decodeResource(
                            activity.getResources(), R.drawable.bikes_logo_50));
                    style.addImage(GREY_IMAGE_ID, BitmapFactory.decodeResource(
                            activity.getResources(), R.drawable.bikes_logo_grey));
                    style.addLayer(new SymbolLayer(MARKER_LAYER_ID, GEOJSON_SOURCE_ID)
                            .withProperties(
                                    iconSize(0.04f),
                                    iconImage(
                                            switchCase(
                                                    Expression.get("is_empty"), literal(GREY_IMAGE_ID),
                                                    literal(MARKER_IMAGE_ID) // default value
                                            )
                                    ),
                                    iconAllowOverlap(true),
                                    iconOffset(new Float[]{0f, -8f})
                            ));
                });
            }
        }

        static List<Feature> loadGeoJsonFromAsset(Context context) {
            List<Feature> features = new ArrayList<>();

            try {
                String response = getRestApi("https://api.jcdecaux.com/vls/v1/stations?contract=dublin&apiKey=3790d87f2538477738d9f1b19723c8194f16779a");
                JSONArray standData = new JSONArray(response);
                GsonBuilder builder = new GsonBuilder();
                builder.setPrettyPrinting().serializeNulls();
                Gson gson = builder.create();
                for (int i = 0; i < standData.length(); i++) {
                    JSONObject object = standData.getJSONObject(i);
                    JsonObject properties = new JsonObject();
                    BikesData bd = gson.fromJson(object.toString(), BikesData.class);
                    properties.add("name", gson.toJsonTree(bd.getName()));
                    properties.add("address", gson.toJsonTree(bd.getAddress()));
                    properties.add(AVAILABLE_BIKE_STANDS, gson.toJsonTree(bd.getAvailable_bike_stands()));
                    properties.add(AVAILABLE_BIKES, gson.toJsonTree(bd.getAvailable_bikes()));
                    properties.add("bike_stands", gson.toJsonTree(bd.getBike_stands()));
                    properties.add("is_empty", gson.toJsonTree(bd.isEmpty()));
                    properties.add("is_full", gson.toJsonTree(bd.isFull()));
                    properties.add("type", gson.toJsonTree("bike"));
                    Feature f = Feature
                            .fromGeometry(Point.fromLngLat(bd.getPosition().getLng(), bd.getPosition().getLat()), properties, String.valueOf(bd.getNumber()));
                    features.add(f);
                }

            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
            return features;
        }
    }

    private static class LoadGeoJsonDataTask_bus extends AsyncTask<Void, Void, FeatureCollection> {

        private final WeakReference<Activity> activityRef;
        private final MapboxMap mapboxMap;
        private GeoJsonSource bussource;
        private FeatureCollection busfeatureCollection;


        LoadGeoJsonDataTask_bus(Activity activity, MapboxMap mapboxMap) {
            this.activityRef = new WeakReference<>(activity);
            this.mapboxMap = mapboxMap;
        }


        @Override
        protected FeatureCollection doInBackground(Void... params) {
            Activity activity = activityRef.get();

            if (activity == null) {
                return null;
            }

            return FeatureCollection.fromFeatures(loadGeoJsonFromAsset_bus(activity));
        }

        @Override
        protected void onPostExecute(FeatureCollection busfeatureCollection) {
            super.onPostExecute(busfeatureCollection);
            Activity activity = activityRef.get();

            if (busfeatureCollection == null || activity == null) {
                return;
            }

            for (Feature singleFeature : busfeatureCollection.features()) {
                singleFeature.addBooleanProperty(BUS_PROPERTY_SELECTED, false);
            }

            this.busfeatureCollection = busfeatureCollection;
            if (mapboxMap != null) {
                mapboxMap.getStyle(style -> {
                    bussource = new GeoJsonSource(BUS_GEOJSON_SOURCE_ID, busfeatureCollection);
                    style.addSource(bussource);
                    style.addImage(BUS_MARKER_IMAGE_ID, BitmapFactory.decodeResource(
                            activity.getResources(), R.drawable.bus));
                    style.addLayer(new SymbolLayer(BUS_MARKER_LAYER_ID, BUS_GEOJSON_SOURCE_ID)
                            .withProperties(
                                    iconSize(0.05f),
                                    iconImage(BUS_MARKER_IMAGE_ID), // default value
                                    iconAllowOverlap(true),
                                    iconOffset(new Float[]{0f, -8f})
                            ));
                });
            }
        }

        static List<Feature> loadGeoJsonFromAsset_bus(Context context) {
            List<Feature> features = new ArrayList<>();

            try {
                String response = getRestApi("https://data.smartdublin.ie/cgi-bin/rtpi/busstopinformation");
                JSONObject busData = new JSONObject(response);
                GsonBuilder builder = new GsonBuilder();
                builder.setPrettyPrinting().serializeNulls();
                Gson gson = builder.create();
                JSONArray test = busData.getJSONArray("results");
                for (int i = 0; i < test.length(); i++) {
                    JSONObject object = test.getJSONObject(i);
                    JsonObject properties = new JsonObject();
                    BusData.Results bsd = gson.fromJson(object.toString(), BusData.Results.class);
                    properties.add("stopid", gson.toJsonTree(bsd.getStopid()));
                    properties.add("name", gson.toJsonTree(bsd.getFullname()));
                    properties.add("type", gson.toJsonTree("bus"));
                    for (BusData.Results.Operators op : bsd.getOperators()) {
                        properties.add(ROUTES, gson.toJsonTree(op.getRoutes()));
                    }
                    Feature f = Feature
                            .fromGeometry(Point.fromLngLat(bsd.getLongitude(), bsd.getLatitude()), properties);
                    features.add(f);
                }
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
            return features;
        }
    }

    public static String getRestApi(String url) throws NullPointerException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            okhttp3.Response response = client.newCall(request).execute();
            return Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            e.printStackTrace();
            return "IO-Error";
        }

    }

}