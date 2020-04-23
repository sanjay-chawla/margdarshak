package com.margdarshak;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.margdarshak.RouteFragment.OnListFragmentInteractionListener;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DirectionsRoute} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyRouteRecyclerViewAdapter extends RecyclerView.Adapter<MyRouteRecyclerViewAdapter.ViewHolder> {

    private final List<DirectionsRoute> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyRouteRecyclerViewAdapter(List<DirectionsRoute> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_route, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).routeOptions().profile());
        StringBuilder duration = new StringBuilder("");
        long hour = TimeUnit.SECONDS.toHours(holder.mItem.duration().longValue());
        if(hour!=0) {duration.append(String.format("%d hr, ", hour));}
        long min = TimeUnit.SECONDS.toMinutes(holder.mItem.duration().longValue() - TimeUnit.HOURS.toSeconds(hour));
        if(min!=0) {duration.append(String.format("%d min, ", min));}
        //long sec = TimeUnit.SECONDS.toSeconds(holder.mItem.duration().longValue()- TimeUnit.MINUTES.toSeconds(min));
        //if(sec!=0) {duration.append(String.format("%d sec", sec));}
        holder.mDuration.setText(duration.toString());

        holder.mDistance.setText(String.valueOf(new DecimalFormat("#.##").format(holder.mItem.distance()/1000)).concat(" km"));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    Log.d("RouteFragment", "sending notification");
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues != null? mValues.size():0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mDistance;
        public final TextView mDuration;
        public DirectionsRoute mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mDistance = (TextView) view.findViewById(R.id.distance);
            mDuration = (TextView) view.findViewById(R.id.duration);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDistance.getText() + "'";
        }
    }
}
