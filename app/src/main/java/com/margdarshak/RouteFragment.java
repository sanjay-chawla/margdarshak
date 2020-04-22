package com.margdarshak;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.DirectionsService;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.core.MapboxService;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.margdarshak.dummy.DummyContent;
import com.margdarshak.dummy.DummyContent.DummyItem;
import com.margdarshak.routing.MargdarshakDirection;
import com.margdarshak.routing.OSRMService;
import com.margdarshak.ui.home.HomeFragment;
import com.margdarshak.ui.home.HomeViewModel;
import com.margdarshak.ui.slideshow.SlideshowViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class RouteFragment extends Fragment {

    private static final String TAG = RouteFragment.class.getSimpleName();

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ORIG_LAT = "origin-latitude";
    private static final String ORIG_LON = "origin-longitude";
    private static final String DEST_LAT = "destination-latitude";
    private static final String DEST_LON = "destination-longitude";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private double originLatitude = 1;
    private double originLongitude = 1;
    private double destinationLatitude = 1;
    private double destinationLongitude = 1;
    private List<DirectionsRoute> routes;
    private OnListFragmentInteractionListener mListener;
    private MyRouteRecyclerViewAdapter adapter;
    private HomeViewModel homeViewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RouteFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static RouteFragment newInstance(int columnCount, double orig_lat, double orig_lon, double dest_lat, double dest_lon) {
        RouteFragment fragment = new RouteFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putDouble(ORIG_LAT, orig_lat);
        args.putDouble(ORIG_LON, orig_lon);
        args.putDouble(DEST_LAT, dest_lat);
        args.putDouble(DEST_LON, dest_lon);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("unused")
    public static RouteFragment newInstance(int columnCount) {
        RouteFragment fragment = new RouteFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(getActivity()).get(HomeViewModel.class);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            originLatitude = getArguments().getDouble(ORIG_LAT);
            originLongitude = getArguments().getDouble(ORIG_LON);
            destinationLatitude = getArguments().getDouble(DEST_LAT);
            destinationLongitude = getArguments().getDouble(DEST_LON);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            getRoute(Point.fromLngLat(originLongitude, originLatitude), Point.fromLngLat(destinationLongitude, destinationLatitude), recyclerView);
            adapter = new MyRouteRecyclerViewAdapter(routes, mListener);
            recyclerView.setAdapter(adapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(DirectionsRoute item);
    }

    private void getRoute(Point origin, Point destination, RecyclerView recyclerView) {
        MapboxService<DirectionsResponse, DirectionsService> client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                .alternatives(Boolean.TRUE)
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
                routes = response.body().routes();
                adapter = new MyRouteRecyclerViewAdapter(routes, mListener);
                recyclerView.setAdapter(adapter);
                Log.d(TAG, "routes lenght: " + adapter.getItemCount());
                homeViewModel.setRoutes(routes);
                //adapter.notifyDataSetChanged();



                /*
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
                }*/
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
                List<DirectionsRoute> routes = response.body().routes();

                /*
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
                */
            }
            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Log.e(TAG, "Error: " + throwable.getMessage());
                Toast.makeText(getContext(), "Error: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }
}
