package com.margdarshak.ui.home;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.location.Location;
import android.opengl.Visibility;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonElement;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.DirectionsService;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenContext;
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
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.OnLocationCameraTransitionListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceAutocompleteFragment;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceSelectionListener;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.margdarshak.R;
import com.margdarshak.routing.MargdarshakDirection;
import com.margdarshak.routing.OSRMService;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class HomeFragment extends Fragment implements
        OnMapReadyCallback {

    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private static final String ICON_LAYER_ID = "icon-layer-id";
    private static final String ICON_SOURCE_ID = "icon-source-id";
    private static final String PLACE_ICON_SOURCE_ID = "place-icon-source-id";
    private static final String RED_PIN_ICON_ID = "red-pin-icon-id";
    private static final String PLACE_MARKER = "placeMarker";
    private static final String PLACE_ICON_LAYER_ID = "place-layer-id";
    public static final int CAMERA_ANIMATION_TIME = 2000;
    private static final int PLACE_SELECTOR_REQUEST_CODE = 899;
    private static final String PLACE_PICKER_LAYER_ID = "place-picker-layer-id";
    private MapboxMap mapboxMap;
    private ImageButton myLocationButton;
    private Chip getDirectionButton;
    private ActivityPermissionListener permissionResultListener;
    private FrameLayout searchFragmentContainer;
    private EditText searchTextBox;
    private LocationEngine locationEngine;
    private LocationComponent locationComponent;
    private PlaceAutocompleteFragment autocompleteFragment;
    private LatLng selectedPoint;

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
        mapboxMap.setStyle(Style.MAPBOX_STREETS,
                style -> {
                    permissionResultListener
                            .requestLocationPermission(new LocationPermissionCallback(mapboxMap, style));
                    style.addImage(PLACE_MARKER, getResources().getDrawable(R.drawable.location_on_accent_36dp, null));
                    style.addSource(new GeoJsonSource(PLACE_ICON_SOURCE_ID));
                    style.addLayer(new SymbolLayer(PLACE_ICON_LAYER_ID, PLACE_ICON_SOURCE_ID).withProperties(
                            iconImage(PLACE_MARKER),
                            iconIgnorePlacement(true),
                            iconAllowOverlap(true),
                            iconOffset(new Float[] {0f, -8f})
                    ));
                });
        setUpSearch();
        mapboxMap.addOnMapClickListener(point -> {
            PointF screenPoint = mapboxMap.getProjection().toScreenLocation(point);
            getPointFeatures(screenPoint);
            makeGeocodeSearch(point);
            selectedPoint = point;
            return false;
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(ViewModelStore::new).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), s -> textView.setText(s));
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

    private boolean getPointFeatures(PointF screenPoint) {
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint);
        if (!features.isEmpty()) {
            Feature feature = features.get(0);

            StringBuilder stringBuilder = new StringBuilder();

            if (feature.properties() != null) {
                for (Map.Entry<String, JsonElement> entry : feature.properties().entrySet()) {
                    stringBuilder.append(String.format("%s - %s", entry.getKey(), entry.getValue()));
                    stringBuilder.append(System.getProperty("line.separator"));
                }
            }
            // TODO: use features if needed
            // Toast.makeText(getActivity(), stringBuilder.toString(), Toast.LENGTH_SHORT).show();
        } else {
            // Toast.makeText(getActivity(), "No properties found", Toast.LENGTH_SHORT).show();
        }
        return true;
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
                            displayPlaceInfo(feature);
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

    private void displayPlaceInfo(CarmenFeature carmenFeature) {
        CardView infoCard = getView().findViewById(R.id.info_frame);
        infoCard.setVisibility(View.VISIBLE);
        getView().findViewById(R.id.close_info).setOnClickListener(v -> {
            infoCard.setVisibility(View.GONE);
        });
        ((TextView)getView().findViewById(R.id.selected_location_info_text)).setText(carmenFeature.text());
        String address = carmenFeature.placeName().replaceFirst(carmenFeature.text().concat(", "),"");
        ((TextView)getView().findViewById(R.id.selected_location_info_address)).setText(address);
    }

    private void setUpSearchFragment(Bundle savedInstanceState){
        if (savedInstanceState == null) {
            PlaceOptions placeOptions = PlaceOptions.builder()
                    .backgroundColor(getResources().getColor(R.color.colorWhite, null))
                    .build(PlaceOptions.MODE_FULLSCREEN);
            autocompleteFragment = PlaceAutocompleteFragment.newInstance(getContext()
                    .getResources().getString(R.string.mapbox_access_token), placeOptions);

            getChildFragmentManager().beginTransaction()
                    .add(R.id.search_fragment_container, autocompleteFragment, TAG)
                    .hide(autocompleteFragment)
                    .commit();
        } else {
            autocompleteFragment = (PlaceAutocompleteFragment)
                    getParentFragmentManager().findFragmentByTag(TAG);
        }
    }


    private void setUpSearch() {
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
                                new Feature[] {Feature.fromJson(carmenFeature.toJson())}));
                    }
                    loadedMapStyle.removeLayer(ROUTE_LAYER_ID);
                    // Move map camera to the selected location
                    moveCameraTo(((Point) carmenFeature.geometry()).latitude(),
                            ((Point) carmenFeature.geometry()).longitude());


                });
                displayPlaceInfo(carmenFeature);
                selectedPoint = new LatLng(((Point) carmenFeature.geometry()).latitude(),
                        ((Point) carmenFeature.geometry()).longitude());
                locationComponent.setCameraMode(CameraMode.NONE);
                myLocationButton.setVisibility(View.VISIBLE);
                finish();
            }

            @Override
            public void onCancel() {
                finish();
            }

            private void finish(){
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                contractSearch(autocompleteFragment, imm);
            }
        });

        searchTextBox.setOnFocusChangeListener((view, hasFocus) -> {
            if(hasFocus) {
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
            if(keyCode == KeyEvent.KEYCODE_ENTER){
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                fragmentSearch.clearFocus();
                int resultCount = resultView.getAdapter().getItemCount();
                if(fragmentSearch.getText().length() < 3){
                    Toast.makeText(autocompleteFragment.getActivity(), "Enter at least 3 characters for accurate results", Toast.LENGTH_LONG).show();
                } else if(resultCount == 0){
                    Toast.makeText(autocompleteFragment.getActivity(), "No result found", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(autocompleteFragment.getActivity(), "No place selected", Toast.LENGTH_LONG).show();
                }
                return true;
            } else if(keyCode == KeyEvent.KEYCODE_BACK){
                autocompleteFragment.onBackButtonPress();
                return true;
            }
            return false;
        });
        fragmentSearch.requestFocus();
        imm.showSoftInput(getView(), InputMethodManager.SHOW_IMPLICIT);
    }

    private void contractSearch(PlaceAutocompleteFragment autocompleteFragment, InputMethodManager imm) {
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

    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine(){
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
    private void initializeLocationComponent(@NonNull Style loadedMapStyle){
        locationComponent = mapboxMap.getLocationComponent();
        // Activate with options
        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions
                        .builder(getContext(), loadedMapStyle)
                        .build());
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING, CAMERA_ANIMATION_TIME, (double)14, null, null, null);
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

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
            initializeLocationEngine();
            initializeLocationComponent(loadedMapStyle);
            myLocationButton.setOnClickListener(v -> {
                myLocationButton.setVisibility(View.INVISIBLE);
                locationComponent.setCameraMode(CameraMode.TRACKING, CAMERA_ANIMATION_TIME, (double)14, null, null, null);
            });

            // TODO: remove this
            getDirectionButton.setOnClickListener(view -> {
                if(selectedPoint != null) {
                    mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
                        Location currentLocation = mapboxMap.getLocationComponent().getLastKnownLocation();
                        Point origin = Point.fromLngLat(selectedPoint.getLongitude(), selectedPoint.getLatitude());
                        Point destination = Point.fromLngLat(currentLocation.getLongitude(), currentLocation.getLatitude());

                        initSource(style, origin, destination);

                        initLayers(style);

                        getRoute(mapboxMap, origin, destination);

                    });
                    Snackbar.make(view, "Http call complete", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
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
            locationComponent.setCameraMode(CameraMode.TRACKING, CAMERA_ANIMATION_TIME, (double)14, null, null, null);
            locationComponent.setRenderMode(RenderMode.COMPASS);
        }
    }

    private void initSource(@NonNull Style loadedMapStyle, Point origin, Point destination) {
        loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID,
                FeatureCollection.fromFeatures(new Feature[] {})));

        GeoJsonSource iconGeoJsonSource = new GeoJsonSource(ICON_SOURCE_ID, FeatureCollection.fromFeatures(new Feature[] {
                Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude())),
                Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude()))}));
        loadedMapStyle.addSource(iconGeoJsonSource);
    }

    private void initDroppedMarker(@NonNull Style loadedMapStyle) {
        // Add the marker image to map
        //loadedMapStyle.addImage("dropped-icon-image", BitmapFactory.decodeResource(
        //        getResources(), R.drawable.location_on_accent_36dp));
        //loadedMapStyle.addSource(new GeoJsonSource(PLACE_ICON_SOURCE_ID));
        loadedMapStyle.addLayer(new SymbolLayer(PLACE_PICKER_LAYER_ID,
                PLACE_ICON_SOURCE_ID).withProperties(
                iconImage("dropped-icon-image"),
                visibility(NONE),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        ));
    }
    private void initLayers(@NonNull Style loadedMapStyle) {
        LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);

        // Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(5f),
                lineColor(Color.parseColor("#009688"))
        );
        loadedMapStyle.addLayer(routeLayer);

        // Add icons
        loadedMapStyle.addImage(RED_PIN_ICON_ID, BitmapFactory.decodeResource(getResources(),
                R.drawable.mapbox_marker_icon_default));

        // Add icon-layers to the map
        loadedMapStyle.addLayer(new SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID).withProperties(
                iconImage(RED_PIN_ICON_ID),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
                iconOffset(new Float[] {0f, -9f})));
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
                    mapboxMap.getStyle( style -> {
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
                    mapboxMap.getStyle( style -> {
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

    // Define the events that the fragment will use to communicate
    public interface ActivityPermissionListener extends PermissionsListener{
        void requestLocationPermission(LocationPermissionCallback locationPermissionCallback);
    }

    public class LocationPermissionCallback {
        MapboxMap mapboxMap;
        Style style;

        public LocationPermissionCallback(MapboxMap mapboxMap, Style style) {
            this.mapboxMap = mapboxMap;
            this.style = style;
        }

        public void onGrant() {
            Log.d(TAG, "granted.. now enabling location component");
            enableLocationComponent(style);
        }
        public void onDenial() {
            Log.d(TAG, "denied.. should show location button");
            enableLocationComponent(style);
            myLocationButton.setOnClickListener(v -> permissionResultListener.requestLocationPermission(this));
            myLocationButton.setVisibility(View.VISIBLE);
        }
    }
}