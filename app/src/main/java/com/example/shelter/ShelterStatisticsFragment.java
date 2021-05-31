package com.example.shelter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

import com.example.shelter.data.ShelterDBContract.HouseTypeEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

public class ShelterStatisticsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static public final String TAG = ShelterStatisticsFragment.class.getName();
    static private final int DATA_CHART_LOADER = 0;


    private Activity mActivity;
    private Context mContext;


    //Chart layout
    private HorizontalBarChart barChart;
    //Data
    private ArrayList<BarEntry> houseTypeCountWishData;

    //Label
    private ArrayList<String> barEntryLabels;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getContext();
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shelter_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpToolbar(view);
        //Init barchart
        barChart = view.findViewById(R.id.bar_chart);
        //Kick the loader to get data and fetch into barchart
        LoaderManager.getInstance(this).initLoader(DATA_CHART_LOADER, null, this);
    }


    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.shelter_statistics_app_bar);
        AppCompatActivity activity = (AppCompatActivity) mActivity;
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext,
                HouseTypeEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            barEntryLabels = new ArrayList<>();
            houseTypeCountWishData = new ArrayList<>();
            do {
                String houseTypeName = data.getString(data.getColumnIndex(HouseTypeEntry.COLUMN_HOUSE_TYPE_NAME));
                int count_wish = data.getInt(data.getColumnIndex(HouseTypeEntry.COLUMN_HOUSE_COUNT_WISH));
                int _id = data.getInt(data.getColumnIndex(HouseTypeEntry._ID));
                houseTypeCountWishData.add(new BarEntry( _id-1, count_wish, houseTypeName));
                barEntryLabels.add(houseTypeName);
            } while (data.moveToNext());

            BarDataSet barDataSet = new BarDataSet(houseTypeCountWishData, "Count wishes for each house type");
            barDataSet.setValueTextColor(Color.BLACK);
            barDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
            barDataSet.setValueTextSize(16.f);

            BarData barData = new BarData(barDataSet);
            //Set data
            barChart.setFitBars(true);
            barChart.setData(barData);
            //Set X label
            barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(barEntryLabels));
            barChart.getXAxis().setLabelCount(barEntryLabels.size());
            barChart.getXAxis().setPosition(XAxis.XAxisPosition.TOP_INSIDE);
            barChart.getXAxis().setLabelRotationAngle(30.f);
            barChart.getXAxis().setGranularity(1f);
            barChart.getXAxis().setGranularityEnabled(true);
            //Set y axis
            barChart.getAxisRight().setEnabled(false);
            //Description
            barChart.getDescription().setText("");
            //Custom
            barChart.setDrawGridBackground(false);
            barChart.animateY(2000);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}