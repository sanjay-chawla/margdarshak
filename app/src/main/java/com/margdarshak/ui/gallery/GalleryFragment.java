package com.margdarshak.ui.gallery;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.margdarshak.ActivityPermissionListener;
import com.margdarshak.R;
import com.margdarshak.ui.data.model.BikesData;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.switchCase;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

public class GalleryFragment extends Fragment implements
        OnMapReadyCallback, MapboxMap.OnMapClickListener {

    private static FeatureCollection featureCollection;
    private GalleryViewModel galleryViewModel;
    private MapboxMap mapboxMap;
    private ActivityPermissionListener permissionResultListener;
    private static GeoJsonSource source;
    private static final String PROPERTY_SELECTED = "selected";
    private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
    private static final String MARKER_IMAGE_ID = "COLOR_IMAGE_ID";
    private static final String GREY_IMAGE_ID = "GREY_IMAGE_ID";
    private static final String MARKER_LAYER_ID = "COLOR_LAYER_ID";
    private static final String CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID";
    private static final String PROPERTY_NAME = "name";
    private static final String AVAILABLE_BIKE_STANDS = "available_bike_stands";

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof ActivityPermissionListener) {
            permissionResultListener = (ActivityPermissionListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement ActivityPermissionListener");
        }
    }

    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS,
            style ->
            {
                mapboxMap.addOnMapClickListener(GalleryFragment.this);
                permissionResultListener
                    .requestLocationPermission(new ActivityPermissionListener
                            .LocationPermissionCallback(mapboxMap, style) {
                        @Override
                        public void onGrant() {
                            Log.d(TAG, "granted.. now enabling location component");
                            enableLocationComponent(style);
                        }

                        @Override
                        public void onDenial() {
                            Log.d(TAG, "denied.. should show location button");
                        }
                    });
                new LoadGeoJsonDataTask(getActivity(), mapboxMap).execute();
            });
        //createThreadGetForDublinBikes();
        /*mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
         @Override
           public void onStyleLoaded(@NonNull Style style) {

          mapboxMap.addOnMapClickListener(GalleryFragment.this);
         mapboxMap.setOnMarkerClickListener(new mapboxMap.OnMarkerClickListener() {
        @Override
         public boolean onMarkerClick(Marker marker) {
        }
         });*/
    }


// This contains the MapView in XML and needs to be called after the access token is configured.


// Initialize the map view


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //setContentView(R.layout.fragment_gallery);

        super.onCreate(savedInstanceState);

        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        final TextView textView = root.findViewById(R.id.text_gallery);
        galleryViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        MapView mapView = root.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return root;
    }

    private void moveCameraTo(Location target) {
        // Toggle GPS position updates
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(target))
                .zoom(14) // Sets the zoom
                .build(); // Creates a CameraPosition from the builder
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000, new MapboxMap.CancelableCallback() {
            @Override
            public void onCancel() {

            }

            @Override
            public void onFinish() {
                mapboxMap.getLocationComponent().setCameraMode(CameraMode.TRACKING);
            }
        });
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
            final LocationComponent locationComponent = mapboxMap.getLocationComponent();
            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions
                            .builder(getContext(), loadedMapStyle)
                            .build());
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
            locationComponent.getLocationComponentOptions().trackingInitialMoveThreshold();
            locationComponent.getLocationComponentOptions().trackingMultiFingerMoveThreshold();

        } else {
            final LocationComponent locationComponent = mapboxMap.getLocationComponent();
            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions
                            .builder(getContext(), loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build());
            locationComponent.setLocationComponentEnabled(false);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
            locationComponent.getLocationComponentOptions().trackingInitialMoveThreshold();
            locationComponent.getLocationComponentOptions().trackingMultiFingerMoveThreshold();
        }
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
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
    /**
     * This method handles click events for SymbolLayer symbols.
     * <p>
     * When a SymbolLayer icon is clicked, we moved that feature to the selected state.
     * </p>
     *
     * @param screenPoint the point on screen clicked
     */
    private boolean handleClickIcon(PointF screenPoint) {
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, MARKER_LAYER_ID);
        Log.d(TAG, ">> features: "+ features.size() + "point " + screenPoint.toString());
        if (!features.isEmpty()) {
            String name = features.get(0).getStringProperty(PROPERTY_NAME);
            List<Feature> featureList = featureCollection.features();
            if (featureList != null) {
                for (int i = 0; i < featureList.size(); i++) {
                    if (featureList.get(i).getStringProperty(PROPERTY_NAME).equals(name)) {
                        setSelected(i);
                        Log.d(TAG, ">> feature selected: "+ i);
                    } else if (featureSelectStatus(i)) {
                        setFeatureSelectState(featureList.get(i), false);
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set a feature selected state.
     *
     * @param index the index of selected feature
     */
    private void setSelected(int index) {
        if (featureCollection.features() != null) {
            Feature feature = featureCollection.features().get(index);
            setFeatureSelectState(feature, true);
            refreshSource();
        }
    }

    /**
     * Selects the state of a feature
     *
     * @param feature the feature to be selected.
     */
    private void setFeatureSelectState(Feature feature, boolean selectedState) {
        if (feature.properties() != null) {
            feature.properties().addProperty(PROPERTY_SELECTED, selectedState);
            refreshSource();
        }
    }

    /**
     * Checks whether a Feature's boolean "selected" property is true or false
     *
     * @param index the specific Feature's index position in the FeatureCollection's list of Features.
     * @return true if "selected" is true. False if the boolean property is false.
     */
    private boolean featureSelectStatus(int index) {
        if (featureCollection == null) {
            return false;
        }
        return featureCollection.features().get(index).getBooleanProperty(PROPERTY_SELECTED);
    }

    private void refreshSource() {
        if (source != null && featureCollection != null) {
            Log.d(TAG, ">> refreshing source: "+ featureCollection.features().size());
            source.setGeoJson(featureCollection);
        }
    }

    /**
     * AsyncTask to load data from the assets folder.
     */
    private static class LoadGeoJsonDataTask extends AsyncTask<Void, Void, FeatureCollection> {

        private final WeakReference<Activity> activityRef;
        private final MapboxMap mapboxMap;
        private GeoJsonSource source;
        private FeatureCollection featureCollection;

        LoadGeoJsonDataTask(Activity activity, MapboxMap mapboxMap) {
            this.activityRef = new WeakReference<>(activity);
            this.mapboxMap = mapboxMap;
            this.featureCollection = GalleryFragment.featureCollection;
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
            GalleryFragment.featureCollection = featureCollection;
            if (mapboxMap != null) {
                mapboxMap.getStyle(style -> {
                    source = new GeoJsonSource(GEOJSON_SOURCE_ID, featureCollection);
                    style.addSource(source);
                    GalleryFragment.source = source;
                    style.addImage(MARKER_IMAGE_ID, BitmapFactory.decodeResource(
                            activity.getResources(), R.drawable.bikes_logo_50));
                    style.addImage(GREY_IMAGE_ID, BitmapFactory.decodeResource(
                            activity.getResources(), R.drawable.bikes_logo_grey));
                    style.addLayer(new SymbolLayer(MARKER_LAYER_ID, GEOJSON_SOURCE_ID)
                            .withProperties(
                                    iconSize(0.05f),
                                    iconImage(
                                            switchCase(
                                                    Expression.get("is_empty"), literal(GREY_IMAGE_ID),
                                                    literal(MARKER_IMAGE_ID) // default value
                                                    )
                                    ),
                                    iconAllowOverlap(true),
                                    iconOffset(new Float[] {0f, -8f})
                            ));
                    setUpInfoWindowLayer(style);
                    new GenerateViewIconTask(activity, mapboxMap).execute(featureCollection);
                });
            }
        }

        private void setUpInfoWindowLayer(@NonNull Style loadedStyle) {
            loadedStyle.addLayer(new SymbolLayer(CALLOUT_LAYER_ID, GEOJSON_SOURCE_ID)
                    .withProperties(
                            iconImage("{name}"),
                            iconAnchor(ICON_ANCHOR_BOTTOM),
                            iconAllowOverlap(true),
                            iconOffset(new Float[] {-2f, -28f})
                    )
                    .withFilter(eq((Expression.get(PROPERTY_SELECTED)), literal(true))));
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
                    properties.add("available_bike_stands", gson.toJsonTree(bd.getAvailable_bike_stands()));
                    properties.add("available_bikes", gson.toJsonTree(bd.getAvailable_bikes()));
                    properties.add("bike_stands", gson.toJsonTree(bd.getBike_stands()));
                    properties.add("is_empty", gson.toJsonTree(bd.isEmpty()));
                    properties.add("is_full", gson.toJsonTree(bd.isFull()));
                    Feature f = Feature
                            .fromGeometry(Point.fromLngLat(bd.getPosition().getLng(),bd.getPosition().getLat()), properties, String.valueOf(bd.getNumber()));
                    features.add(f);
                }
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
            return features;
        }
    }

    private static class GenerateViewIconTask extends AsyncTask<FeatureCollection, Void, HashMap<String, Bitmap>> {

        private final HashMap<String, View> viewMap = new HashMap<>();
        private final WeakReference<Activity> activityRef;
        private final boolean refreshSource;
        private MapboxMap mapboxMap;

        GenerateViewIconTask(Activity activity, boolean refreshSource, MapboxMap mapboxMap) {
            this.activityRef = new WeakReference<>(activity);
            this.refreshSource = refreshSource;
            this.mapboxMap = mapboxMap;
        }

        GenerateViewIconTask(Activity activity, MapboxMap mapboxMap) {
            this(activity, false, mapboxMap);
        }

        @SuppressWarnings("WrongThread")
        @Override
        protected HashMap<String, Bitmap> doInBackground(FeatureCollection... params) {
            Activity activity = activityRef.get();
            if (activity != null) {
                HashMap<String, Bitmap> imagesMap = new HashMap<>();
                LayoutInflater inflater = LayoutInflater.from(activity);

                FeatureCollection featureCollection = params[0];

                for (Feature feature : featureCollection.features()) {

                    BubbleLayout bubbleLayout = (BubbleLayout)
                            inflater.inflate(R.layout.symbol_layer_info_window_layout_callout, null);

                    String name = feature.getStringProperty(PROPERTY_NAME);
                    TextView titleTextView = bubbleLayout.findViewById(R.id.info_window_title);
                    titleTextView.setText(name);

                    String style = feature.getStringProperty(AVAILABLE_BIKE_STANDS);
                    TextView descriptionTextView = bubbleLayout.findViewById(R.id.info_window_description);
                    descriptionTextView.setText(
                            String.format(activity.getString(R.string.available_bike_stands), style));

                    int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    bubbleLayout.measure(measureSpec, measureSpec);

                    float measuredWidth = bubbleLayout.getMeasuredWidth();

                    bubbleLayout.setArrowPosition(measuredWidth / 2 - 5);

                    Bitmap bitmap = SymbolGenerator.generate(bubbleLayout);
                    imagesMap.put(name, bitmap);
                    viewMap.put(name, bubbleLayout);
                    Log.d(TAG, ">> images map: "+ imagesMap.size());
                }

                return imagesMap;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(HashMap<String, Bitmap> bitmapHashMap) {
            super.onPostExecute(bitmapHashMap);
            Activity activity = activityRef.get();
            if (activity != null && bitmapHashMap != null) {
                if (mapboxMap != null) {
                    mapboxMap.getStyle(style -> {
                        style.addImages(bitmapHashMap);
                    });
                }
                if (refreshSource) {
                    if (source != null && featureCollection != null) {
                        source.setGeoJson(featureCollection);
                    }
                }
            }
            Toast.makeText(activity, R.string.tap_on_marker_instruction, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Utility class to generate Bitmaps for Symbol.
     */
    private static class SymbolGenerator {

        /**
         * Generate a Bitmap from an Android SDK View.
         *
         * @param view the View to be drawn to a Bitmap
         * @return the generated bitmap
         */
        static Bitmap generate(@NonNull View view) {
            int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            view.measure(measureSpec, measureSpec);

            int measuredWidth = view.getMeasuredWidth();
            int measuredHeight = view.getMeasuredHeight();

            view.layout(0, 0, measuredWidth, measuredHeight);
            Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        }
    }

}