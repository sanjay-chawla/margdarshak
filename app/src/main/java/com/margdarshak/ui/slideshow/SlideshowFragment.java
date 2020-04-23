package com.margdarshak.ui.slideshow;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonElement;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceAutocompleteFragment;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.margdarshak.ActivityPermissionListener;
import com.margdarshak.R;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class SlideshowFragment extends Fragment implements
        OnMapReadyCallback {

    private static final String TAG = SlideshowFragment.class.getSimpleName();
    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private static final String ICON_LAYER_ID = "icon-layer-id";
    private static final String ICON_SOURCE_ID = "icon-source-id";
    private static final String RED_PIN_ICON_ID = "red-pin-icon-id";
    private static final String PLACE_ICON_SOURCE_ID = "place-icon-source-id";
    private static final String PLACE_MARKER = "placeMarker";
    private static final String PLACE_ICON_LAYER_ID = "place-layer-id";

    private SlideshowViewModel slideshowViewModel;
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
    public static final int CAMERA_ANIMATION_TIME = 2000;

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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel =
                new ViewModelProvider(ViewModelStore::new).get(SlideshowViewModel.class);
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);
        final TextView textView = root.findViewById(R.id.text_slideshow);
        slideshowViewModel.getRoutes().observe(getViewLifecycleOwner(), new Observer<List>() {
            @Override
            public void onChanged(@Nullable List s) {
                //textView.setText(s);
            }
        });
        getDirectionButton = root.findViewById(R.id.get_directions);
        MapView mapView = root.findViewById(R.id.mapView);
        myLocationButton = root.findViewById(R.id.locationFAB);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return root;
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS,
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
                            iconOffset(new Float[] {0f, -8f})
                    ));
                });
        mapboxMap.addOnMapClickListener(point -> {
            PointF screenPoint = mapboxMap.getProjection().toScreenLocation(point);
            getPointFeatures(screenPoint);
            makeGeocodeSearch(point);
            selectedPoint = point;
            return false;
        });
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

    private CameraPosition moveCameraTo(double latitude, double longitude) {
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(14) // Sets the zoom
                .build(); // Creates a CameraPosition from the builder
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), CAMERA_ANIMATION_TIME);
        return position;
    }

    private void initSource(@NonNull Style loadedMapStyle, Point origin, Point destination) {
        loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID,
                FeatureCollection.fromFeatures(new Feature[] {})));

        GeoJsonSource iconGeoJsonSource = new GeoJsonSource(ICON_SOURCE_ID, FeatureCollection.fromFeatures(new Feature[] {
                Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude())),
                Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude()))}));
        loadedMapStyle.addSource(iconGeoJsonSource);
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
}