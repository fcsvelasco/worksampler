package com.example.marasigan.worksampler;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marasigan.worksampler.entities.ObjectActivity;
import com.example.marasigan.worksampler.entities.Project;
import com.example.marasigan.worksampler.entities.StudyObject;
import com.example.marasigan.worksampler.views.ObjectActivityButton;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

public class ResultsFragment extends Fragment implements OnChartValueSelectedListener {
    private StudyObject studyObject;
    private PieChart pieChart;
    private int projectMode;
    private float zValue;



    public ResultsFragment() {
        // Required empty public constructor
    }

    public static ResultsFragment newInstance(StudyObject studyObject, int projectMode, float zValue) {
        ResultsFragment fragment = new ResultsFragment();
        Bundle args = new Bundle();
        args.putParcelable(StudyObject.TAG, studyObject);
        args.putInt(Project.MODE_TAG, projectMode);
        args.putFloat(Project.Z_VALUE_TAG, zValue);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            studyObject = getArguments().getParcelable(StudyObject.TAG);
            projectMode = getArguments().getInt(Project.MODE_TAG);
            zValue = getArguments().getFloat(Project.Z_VALUE_TAG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setLayout();
        setPieChartData();
        setPieChartLegend();
        createActivityButtons();
    }

    private void setLayout(){
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int pxHeight = displayMetrics.heightPixels;

        RelativeLayout.LayoutParams pieChartLayoutParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pxHeight*45/100);
//        pieChartLayoutParams.addRule(RelativeLayout.BELOW, R.id.tv_number_of_samples_taken);

        ScrollView scrollView = getView().findViewById(R.id.results_activities_scroll_view);
        RelativeLayout.LayoutParams scrollViewLayoutParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pxHeight*25/100);
        scrollViewLayoutParams.addRule(RelativeLayout.BELOW,R.id.results_pie_chart);
//        scrollView.setLayoutParams(scrollViewLayoutParams);

        pieChart = getView().findViewById(R.id.results_pie_chart);
//        pieChart.setLayoutParams(pieChartLayoutParams);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.getDescription().setEnabled(false);
        pieChart.setRotationEnabled(false);
        pieChart.setExtraOffsets(0,0,0,-15);
        pieChart.setOnChartValueSelectedListener(this);

        TextView tvSamplesTaken = getView().findViewById(R.id.tv_results_samples_taken);
        String samplesTaken = "Samples taken: " + studyObject.getNoOfSamplesTaken();
        tvSamplesTaken.setText(samplesTaken);

        TextView tvUtilization = getView().findViewById(R.id.tv_results_utilization);
        String utilization = "Utilization: " + studyObject.getStringUtilization();
        tvUtilization.setText(utilization);

        TextView tvAllowances = getView().findViewById(R.id.tv_results_allowances);
        String allowances = "Allowances: " + studyObject.getStringAllowances();
        tvAllowances.setText(allowances);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        PieEntry pe = (PieEntry) e;

        float totalCount = 0;
        for (ObjectActivity activity : studyObject. getActivities()) totalCount += activity.getNoOfSamplesTaken();

        float proportion = e.getY()/totalCount;
        float error = 100f*zValue*((float)Math.sqrt(proportion*(1-proportion)/totalCount));

        Toast.makeText(getContext(), "Activity: " + pe.getLabel() + "\nSamples taken: " + e.getY()
                        + "\nProportion: " + String.format(Locale.US,"%.1f",100f*proportion) + "% ± "
                        + String.format(Locale.US,"%.1f",error) + "%",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected() {

    }

    private void setPieChartData(){
        ArrayList<PieEntry> yValues = new ArrayList<>();
//        ArrayList<String> xValues = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        for (int i=0; i<studyObject.getActivities().size(); i++){
            ObjectActivity activity = studyObject.getActivities().get(i);
            if (activity.getNoOfSamplesTaken() > 0) {
                yValues.add(new PieEntry(activity.getNoOfSamplesTaken(), activity.getName()));
//                xValues.add(studyObject.getActivities().get(i).getName());
                if (activity.getType().equals(ObjectActivity.VA)) colors.add(getResources().getColor(R.color.pieChartVA));
                if (activity.getType().equals(ObjectActivity.NVAE)) colors.add(getResources().getColor(R.color.pieChartNVAE));
                if (activity.getType().equals(ObjectActivity.NVAN)) colors.add(getResources().getColor(R.color.pieChartNVAN));
            }
        }

        if (studyObject.getRemarksCount() > 0){
            yValues.add(new PieEntry(studyObject.getRemarksCount(), "Others"));
            colors.add(getResources().getColor(R.color.pieChartOthers));
        }

        if (yValues.size()>0){
            PieDataSet pieDataSet = new PieDataSet(yValues, "");
            pieDataSet.setSliceSpace(2f);
            pieDataSet.setValueTextSize(10);

            pieDataSet.setColors(colors);
            pieDataSet.setHighlightEnabled(true);
            PieData pieData = new PieData(pieDataSet);
            pieData.setValueFormatter(new PercentFormatter());
//            pieData.setValueTextSize(20f);
            pieChart.setData(pieData);
        }


        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawEntryLabels(true);
        pieChart.setNoDataText("No samples taken");
        pieChart.setNoDataTextColor(Color.BLACK);
        pieChart.invalidate();
    }

    private void setPieChartLegend(){
        float VApercent, NVAEpercent, NVANpercent, othersPercent, VAerror, NVAEerror, NVANerror, othersError,
                VAcount = 0, NVAEcount = 0, NVANcount = 0, totalCount = 0, othersCount = 0;

        for (ObjectActivity activity : studyObject.getActivities()){
            if (activity.getType().equals(ObjectActivity.VA)) VAcount += activity.getNoOfSamplesTaken();
            if (activity.getType().equals(ObjectActivity.NVAE)) NVAEcount += activity.getNoOfSamplesTaken();
            if (activity.getType().equals(ObjectActivity.NVAN)) NVANcount += activity.getNoOfSamplesTaken();

            totalCount += activity.getNoOfSamplesTaken();
        }

        othersCount += studyObject.getRemarksCount();
        totalCount += othersCount;

        VApercent = 100f*(VAcount/totalCount);
        NVAEpercent = 100f*(NVAEcount/totalCount);
        NVANpercent = 100f*(NVANcount/totalCount);
        othersPercent = 100f*(othersCount/totalCount);

        VAerror = 100*zValue*((float)Math.sqrt((VApercent/100f)*(1-VApercent/100f)/totalCount));
        NVAEerror = 100*zValue*((float)Math.sqrt((NVAEpercent/100f)*(1-NVAEpercent/100f)/totalCount));
        NVANerror = 100*zValue*((float)Math.sqrt((NVANpercent/100f)*(1-NVANpercent/100f)/totalCount));
        othersError = 100*zValue*((float)Math.sqrt((othersPercent/100f)*(1-othersPercent/100f)/totalCount));

        Legend legend = pieChart.getLegend();

        ArrayList<LegendEntry> legendEntries = new ArrayList<>();

        LegendEntry legendEntryVA = new LegendEntry();
        legendEntryVA.label = "Value adding (" +String.format(Locale.US,"%.1f",VApercent) + "% ± "
                + String.format(Locale.US,"%.1f",VAerror) + "%)";
        legendEntryVA.formColor = getResources().getColor(R.color.pieChartVA);
        legendEntries.add(legendEntryVA);

        LegendEntry legendEntryNVAE = new LegendEntry();
        legendEntryNVAE.label = "Non value adding (Essential) (" +String.format(Locale.US,"%.1f",NVAEpercent) + "% ± "
                + String.format(Locale.US,"%.1f",NVAEerror) + "%)";
        legendEntryNVAE.formColor = getResources().getColor(R.color.pieChartNVAE);
        legendEntries.add(legendEntryNVAE);

        LegendEntry legendEntryNVAN = new LegendEntry();
        legendEntryNVAN.label = "Non value adding (Non essential) (" + String.format(Locale.US,"%.1f",NVANpercent) + "% ± "
                + String.format(Locale.US,"%.1f",NVANerror) + "%)";
        legendEntryNVAN.formColor = getResources().getColor(R.color.pieChartNVAN);
        legendEntries.add(legendEntryNVAN);

        LegendEntry legendEntryOthers = new LegendEntry();
        legendEntryOthers.label = "Others (" + String.format(Locale.US,"%.1f",othersPercent) + "% ± "
                + String.format(Locale.US,"%.1f",othersError) + "%)";
        legendEntryOthers.formColor = getResources().getColor(R.color.pieChartOthers);
        legendEntries.add(legendEntryOthers);

        legend.setCustom(legendEntries);
        legend.setWordWrapEnabled(true);
        legend.setMaxSizePercent(0.30f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }

    private void createActivityButtons(){
        LinearLayout activitiesLinearLayout = getView().findViewById(R.id.results_activities_linear_layout);

        for (ObjectActivity activity : studyObject.getActivities()){
            ObjectActivityButton btnActivity =
                    (ObjectActivityButton) getLayoutInflater().inflate(R.layout.view_operator_activity_button, null);

//            if (activity.getType().equals(ObjectActivity.VA)) btnActivity.setStyle(ObjectActivityButton.STYLE_GREEN);
//            if (activity.getType().equals(ObjectActivity.NVAE)) btnActivity.setStyle(ObjectActivityButton.STYLE_YELLOW);
//            if (activity.getType().equals(ObjectActivity.NVAN)) btnActivity.setStyle(ObjectActivityButton.STYLE_RED);


            btnActivity.setObjectActivity(activity);
            btnActivity.setText(activity.getName());
            activitiesLinearLayout.addView(btnActivity);

            btnActivity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnActivityOnClick(view);
                }
            });
        }
    }

    private void btnActivityOnClick(View view){
        ObjectActivityButton btnActivity = (ObjectActivityButton) view;

        ActivityResultsDialogFragment dialog = new ActivityResultsDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ObjectActivity.TAG, btnActivity.getObjectActivity());
        bundle.putFloat("Allowances",studyObject.getAllowances());
        bundle.putInt(Project.MODE_TAG,projectMode);
        dialog.setArguments(bundle);
        dialog.show(getActivity().getSupportFragmentManager(),"Activity Results");
    }


}
