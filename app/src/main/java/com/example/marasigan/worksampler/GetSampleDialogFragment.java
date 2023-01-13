package com.example.marasigan.worksampler;

//import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marasigan.worksampler.entities.ObjectActivity;
import com.example.marasigan.worksampler.entities.Project;
import com.example.marasigan.worksampler.entities.Sample;
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
import java.util.Calendar;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

public class GetSampleDialogFragment extends DialogFragment implements OnChartValueSelectedListener {
    public static final byte GET_SAMPLE = 1;
    public static final byte CHECK_STATUS = 0;

    private int projectMode, getSampleDuration, index;
    private byte mode;
    private float zValue;
    private StudyObject studyObject;
    private Sample sample;
    private PieChart pieChart;
    private TextView tvNoOfSamplesTaken;
    private LinearLayout activitiesLinearLayout;
    private CountDownTimer countDownTimer;
    private AlertDialog remarksDialog;
    private android.app.AlertDialog addActivityDialog;
    private boolean activityAddedSuccessfully;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_get_sample_dialog,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        assert bundle != null;

        studyObject = bundle.getParcelable(StudyObject.TAG);
        if(bundle.getParcelable(Sample.TAG)!= null) sample = bundle.getParcelable(Sample.TAG);
        mode = bundle.getByte("Mode");
        projectMode = bundle.getInt(Project.MODE_TAG);
        getSampleDuration = bundle.getInt(Project.SAMPLE_DURATION_TAG);
        zValue = bundle.getFloat(Project.Z_VALUE_TAG);
        if (projectMode != Project.MODE_SELF_WS) index = bundle.getInt("Index");

        setLayout();
        setPieChartData();
        setPieChartLegend();
        setTvNoOfSamplesTakenText();
        createActivityButtons();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private void setLayout(){
        final DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int pxHeight = displayMetrics.heightPixels;

        if(projectMode == Project.MODE_SELF_WS){
            LinearLayout linearLayout = getView().findViewById(R.id.get_sample_dialog_title_bar);
            linearLayout.setVisibility(View.GONE);

            TextView tvGetSample = getView().findViewById(R.id.tv_get_sample_now);
            if (mode == GET_SAMPLE) setCountDownTimer(tvGetSample);
            else tvGetSample.setText("Activities:");
//            setCountDownTimer(tvGetSample);
        }else {
            TextView tvOperatorMachineName = getView().findViewById(R.id.tv_get_sample_operator_name);
            tvOperatorMachineName.setText(studyObject.getName());

            TextView tvGetSample = getView().findViewById(R.id.tv_get_sample_now);
            if (sample!= null) setCountDownTimer(tvGetSample);
            else tvGetSample.setText("Activities:");

            ScrollView scrollView = getView().findViewById(R.id.activities_scroll_view);
            RelativeLayout.LayoutParams scrollViewLayoutParams =
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pxHeight*25/100);
            scrollViewLayoutParams.addRule(RelativeLayout.BELOW,R.id.tv_get_sample_now);
            scrollView.setLayoutParams(scrollViewLayoutParams);
        }

        Button btnClose = getView().findViewById(R.id.btn_close_dialog);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        tvNoOfSamplesTaken = getView().findViewById(R.id.tv_number_of_samples_taken);
        activitiesLinearLayout = getView().findViewById(R.id.activities_linear_layout);

        RelativeLayout.LayoutParams pieChartLayoutParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        pxHeight*(projectMode == Project.MODE_SELF_WS ?  50 : 45)/100);
        pieChartLayoutParams.addRule(RelativeLayout.BELOW, R.id.tv_number_of_samples_taken);

        pieChart = getView().findViewById(R.id.pie_chart);
        pieChart.setLayoutParams(pieChartLayoutParams);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.getDescription().setEnabled(false);
        pieChart.setRotationEnabled(false);
        pieChart.setOnChartValueSelectedListener(this);
        pieChart.setExtraOffsets(0,0,0,-10);
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
            PieData pieData = new PieData(pieDataSet);
            pieData.setValueFormatter(new PercentFormatter());
//            pieData.setValueTextSize(20f);
            pieChart.setData(pieData);
        }

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
        for (ObjectActivity activity : studyObject.getActivities()){
            ObjectActivityButton btnActivity = (ObjectActivityButton) getLayoutInflater().inflate(R.layout.view_operator_activity_button, null);

            if (mode == GET_SAMPLE) {
                if (activity.getType().equals(ObjectActivity.VA)) btnActivity.setStyle(ObjectActivityButton.STYLE_VA);
                if (activity.getType().equals(ObjectActivity.NVAE)) btnActivity.setStyle(ObjectActivityButton.STYLE_NVAE);
                if (activity.getType().equals(ObjectActivity.NVAN)) btnActivity.setStyle(ObjectActivityButton.STYLE_NVAN);
            }

            btnActivity.setOnClickListener(new BtnActivityOnClickListener());

            btnActivity.setObjectActivity(activity);
            btnActivity.setText(activity.getName());
            activitiesLinearLayout.addView(btnActivity);
        }

        ObjectActivityButton btnOthers = (ObjectActivityButton)getLayoutInflater().inflate(R.layout.view_operator_activity_button, null);
        btnOthers.setText("Others");
        if(mode == GET_SAMPLE) btnOthers.setStyle(ObjectActivityButton.STYLE_OTHERS);
        btnOthers.setOnClickListener(new BtnOthersOnClickListener());
        activitiesLinearLayout.addView(btnOthers);

        ObjectActivityButton btnAddActivity = (ObjectActivityButton)getLayoutInflater().inflate(R.layout.view_operator_activity_button, null);
        btnAddActivity.setText("Add activity");
        if(mode == GET_SAMPLE) btnAddActivity.setStyle(ObjectActivityButton.STYLE_OTHERS);
        btnAddActivity.setOnClickListener(new BtnAddActivityOnClickListener());
        activitiesLinearLayout.addView(btnAddActivity);
    }

    private void setTvNoOfSamplesTakenText(){
        String samplesTaken;
        if (projectMode == Project.MODE_SELF_WS){
            if (((SelfWorkSamplingInProgress) getActivity()).getProject().getPhase() == Project.PHASE_ACTUAL)
                samplesTaken = "Samples Taken: " + studyObject.getNoOfSamplesTaken() + " out of " + studyObject.getNoOfSamplesRequired();
            else samplesTaken = "Samples Taken: " + studyObject.getNoOfSamplesTaken();
            tvNoOfSamplesTaken.setText(samplesTaken);
        }else {
            if (((ProjectInProgress) getActivity()).getProject().getPhase() == Project.PHASE_ACTUAL)
                samplesTaken = "Samples Taken: " + studyObject.getNoOfSamplesTaken() + " out of " + studyObject.getNoOfSamplesRequired();
            else samplesTaken = "Samples Taken: " + studyObject.getNoOfSamplesTaken();
            tvNoOfSamplesTaken.setText(samplesTaken);
        }
    }

    private void setCountDownTimer (View view){
        final TextView textView = (TextView) view;
        long countDownMillis;



        if (mode == CHECK_STATUS) {
            if (studyObject.findNextSample() == null){
                textView.setText("Activities:");
                return;
            }else {
                countDownMillis = studyObject.findNextSample()
                        .getCalendarDate().getTimeInMillis() - Project.artificialNow().getTimeInMillis();
            }
        }
        else countDownMillis = studyObject.currentSample().getCalendarDate().getTimeInMillis()
                - Project.artificialNow().getTimeInMillis() + getSampleDuration*1000;

        countDownTimer = new CountDownTimer(countDownMillis, 1000) {
            @Override
            public void onTick(long l) {
                int hours = (int) TimeUnit.MILLISECONDS.toHours(l);
                int minutes = (int) (TimeUnit.MILLISECONDS.toMinutes(l) % TimeUnit.HOURS.toMinutes(1));
                int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(l) % TimeUnit.MINUTES.toSeconds(1));

                if (mode == CHECK_STATUS) {
                    String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                    textView.setText("Next sample in: " + time);
                } else {
                    String time = String.format("%02d", seconds);
                    textView.setText("Get Sample (" + time + "):");
                }
            }

            @Override
            public void onFinish() {
            }
        }.start();
    }

    public void modeChanged(){
        if(countDownTimer != null) countDownTimer.cancel();
        activitiesLinearLayout.removeAllViews();
        if (remarksDialog != null){
            if (remarksDialog.isShowing()) remarksDialog.dismiss();
        }
        if (addActivityDialog != null){
            if (addActivityDialog.isShowing()) addActivityDialog.dismiss();
        }
        onViewCreated(getView(),null);
    }

    private void getSample(ObjectActivity activity){
        activity.addSampleTaken();
        studyObject.currentSample().setActivityName(activity.getName());
        studyObject.currentSample().setStatus(Sample.TAKEN);
        studyObject.addSampleTaken();
        studyObject.computeActivityProportions();

        setPieChartData();
        setPieChartLegend();
        setTvNoOfSamplesTakenText();
        Toast.makeText(getContext(), "Sample recorded!", Toast.LENGTH_SHORT).show();

        if(projectMode == Project.MODE_SELF_WS){
            ((SelfWorkSamplingInProgress) getActivity()).onSampleRecorded();
        }else {
            ((ProjectInProgress) getActivity()).onSampleRecorded();
        }
    }

    private void getRatedSample(final ObjectActivity activity){
        View ratingDialogLayout = getLayoutInflater().inflate(R.layout.remarks_dialog,null);
        final EditText etRating = ratingDialogLayout.findViewById(R.id.et_remarks);
        etRating.setHint("%");
        etRating.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            etRating.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TextView tvRating = ratingDialogLayout.findViewById(R.id.tv_remarks);
        tvRating.setText("Enter performance rating: ");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(ratingDialogLayout);
        builder.setPositiveButton("Done", null);
        builder.setNegativeButton("Cancel", null);
//            builder.create().show();

        remarksDialog = builder.create();
        remarksDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnDone = remarksDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                btnDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(etRating.getText().toString()))
                            Toast.makeText(getContext(), "Please input performance rating", Toast.LENGTH_SHORT).show();
                        else {
                            float rating = Float.parseFloat(etRating.getText().toString());
                            if (rating < 80 || rating > 120){
                                Toast.makeText(getContext(), "Performance rating must be between 80 and 120.",
                                        Toast.LENGTH_LONG).show();
                            }else {
                                getSample(activity);
                                activity.addPerformanceRating(rating);
                                sample.setRating(Float.parseFloat(etRating.getText().toString()));
                                if (projectMode == Project.MODE_SELF_WS) {
                                    ((SelfWorkSamplingInProgress) getActivity()).onSampleRecorded();
                                } else {
                                    ((ProjectInProgress) getActivity()).onSampleRecorded();
                                }
                                if (addActivityDialog != null && addActivityDialog.isShowing())
                                    addActivityDialog.dismiss();
                                remarksDialog.dismiss();
                            }
                        }
                    }
                });
            }
        });
        remarksDialog.show();
    }

    private class BtnActivityOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            ObjectActivity activity = ((ObjectActivityButton) view).getObjectActivity();

            if (mode == GET_SAMPLE) {

                if(projectMode == Project.MODE_RATED_WS && activity.getType().equals(ObjectActivity.VA)){
                    getRatedSample(activity);
                }else {
                    getSample(activity);
                }
            }else Toast.makeText(getContext(), "Not yet time to get sample.", Toast.LENGTH_SHORT).show();
        }

    }

    private class BtnOthersOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            if (mode == GET_SAMPLE) showRemarksDialog();
            else Toast.makeText(getContext(), "Not yet time to get sample.", Toast.LENGTH_SHORT).show();
        }

        private void showRemarksDialog(){
            final View remarksDailogLayout = getLayoutInflater().inflate(R.layout.remarks_dialog,null);
            final EditText etRemarks = remarksDailogLayout.findViewById(R.id.et_remarks);
            etRemarks.setInputType(InputType.TYPE_CLASS_TEXT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                etRemarks.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(remarksDailogLayout);
            builder.setPositiveButton("Done", null);
            builder.setNegativeButton("Cancel", null);

            remarksDialog = builder.create();
            remarksDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button btnDone = remarksDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    btnDone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (TextUtils.isEmpty(etRemarks.getText().toString()))
                                Toast.makeText(getContext(), "Please input remarks.", Toast.LENGTH_SHORT).show();
                            else {
                                String remarks = etRemarks.getText().toString();
                                if (remarks.length() > 100){
                                    Toast.makeText(getContext(), "Remarks too long. " +
                                            "Remarks must not be more thant 100 characters.", Toast.LENGTH_LONG).show();
                                }else {
                                    sample.setRemarks(remarks);
                                    Toast.makeText(getContext(), "Remarks recorded.", Toast.LENGTH_SHORT).show();
                                    sample.setStatus(Sample.WITH_REMARKS);
                                    studyObject.addRemarksCount();
                                    if (projectMode == Project.MODE_SELF_WS) {
                                        ((SelfWorkSamplingInProgress) getActivity()).onSampleRecorded();
                                    } else {
                                        ((ProjectInProgress) getActivity()).onSampleRecorded();
                                    }

                                    remarksDialog.dismiss();
                                }
                            }
                        }
                    });

                    Button btnCancel = remarksDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            remarksDialog.dismiss();
                        }
                    });
                }
            });
            remarksDialog.show();
        }
    }

    private class BtnAddActivityOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            if (mode == GET_SAMPLE) showAddActivityDialog("");
            else Toast.makeText(getContext(), "Not yet time to get sample.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddActivityDialog(String activityName){
        android.app.AlertDialog.Builder addActivityDialogBuilder = new android.app.AlertDialog.Builder(getContext());
        final View add_activity_dialog = getLayoutInflater().inflate(R.layout.add_activity_dialog, null);
        addActivityDialogBuilder.setView(add_activity_dialog);

        addActivityDialogBuilder.setPositiveButton("Add", null);
        addActivityDialogBuilder.setNegativeButton("Cancel", null);
        EditText etName = add_activity_dialog.findViewById(R.id.et_activity_name);
        etName.setText(activityName);
        final EditText etEstimatedP = add_activity_dialog.findViewById(R.id.et_estimated_p);
        final RadioButton rbVA = add_activity_dialog.findViewById(R.id.rb_VA);
        addActivityDialog = addActivityDialogBuilder.create();
//        if (projectMode == Project.MODE_SELF_WS);
//        final boolean conductsPresampling = ((ProjectInProgress) getActivity()).getProject().conductsPreSampling();

        addActivityDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                if (projectMode == Project.MODE_SELF_WS){
                    if (((SelfWorkSamplingInProgress) getActivity()).getProject().conductsPreSampling()) etEstimatedP.setEnabled(false);
                }else {
                    if (((ProjectInProgress) getActivity()).getProject().conductsPreSampling()) etEstimatedP.setEnabled(false);
                }

                rbVA.setChecked(true);

                Button btnAdd = addActivityDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addActivity(add_activity_dialog);

                        if(activityAddedSuccessfully){
                            if (projectMode == Project.MODE_SELF_WS){
                                if (((SelfWorkSamplingInProgress) getActivity()).getCountDownForGettingSample() != null)
                                    ((SelfWorkSamplingInProgress) getActivity()).getCountDownForGettingSample().cancel();
                                if (((SelfWorkSamplingInProgress) getActivity()).getCountDownToNextSample() != null)
                                    ((SelfWorkSamplingInProgress) getActivity()).getCountDownToNextSample().cancel();
                            }else {
                                if (((ProjectInProgress) getActivity()).getOperatorsTab().getCountDownToNextSample(index) != null)
                                    ((ProjectInProgress) getActivity()).getOperatorsTab().getCountDownToNextSample(index).cancel();

                                if (((ProjectInProgress) getActivity()).getOperatorsTab().getCountDownForGettingSample(index) != null)
                                    ((ProjectInProgress) getActivity()).getOperatorsTab().getCountDownForGettingSample(index).cancel();
                            }

                            ObjectActivity activity = studyObject.getActivities().get(studyObject.getActivities().size() - 1);
                            if(projectMode == Project.MODE_RATED_WS && activity.getType().equals(ObjectActivity.VA)){
                                getRatedSample(activity);
                            }else {
                                getSample(activity);
                                addActivityDialog.dismiss();
                            }
                        }
                    }
                });
            }
        });
        addActivityDialog.show();
//        findViewById(R.id.main_layout).requestFocus();
    }

    private void addActivity(View dialog){
        EditText etActivityName = dialog.findViewById(R.id.et_activity_name);
        EditText etError = dialog.findViewById(R.id.et_error);
        EditText etEstimatedP = dialog.findViewById(R.id.et_estimated_p);
        RadioButton rbVA = dialog.findViewById(R.id.rb_VA);
        RadioButton rbNVAE = dialog.findViewById(R.id.rb_NVAE);
        RadioButton rbNVAN = dialog.findViewById(R.id.rb_NVAN);
        SwitchCompat switchUtilization = dialog.findViewById(R.id.switch_utilization);
        SwitchCompat switchAllowances = dialog.findViewById(R.id.switch_allowances);

        if (TextUtils.isEmpty(etActivityName.getText().toString())){
            Toast.makeText(getContext(), "Please enter name of activity.", Toast.LENGTH_LONG).show();
            activityAddedSuccessfully = false;
            return;
        }
        String name = etActivityName.getText().toString();
        for (ObjectActivity activity : studyObject.getActivities()){
            if (name.equals(activity.getName())){
                Toast.makeText(getContext(), "Name of activity is already taken. Please enter new name.", Toast.LENGTH_LONG).show();
                activityAddedSuccessfully = false;
                return;
            }
        }

        if (name.length() > Project.MAX_LENGTH_NAME){
            Toast.makeText(getContext(),"Invalid input for name: must not be longer than "
                    + Project.MAX_LENGTH_NAME + " characters.", Toast.LENGTH_LONG).show();
            activityAddedSuccessfully = false;
            return;
        }

        if (TextUtils.isEmpty(etError.getText().toString())){
            Toast.makeText(getContext(), "Please enter acceptable error for this activity.", Toast.LENGTH_SHORT).show();
            activityAddedSuccessfully = false;
            return;
        }
        float error = Float.parseFloat(etError.getText().toString())/100f;
        if (error <= 0 || error > 0.10f){
            Toast.makeText(getContext(), "Invalid input for acceptable error: " +
                    "must be more than 0 and not more than 10.", Toast.LENGTH_LONG).show();
            activityAddedSuccessfully = false;
            return;
        }

        if (etEstimatedP.isEnabled() && TextUtils.isEmpty(etEstimatedP.getText().toString())){
            Toast.makeText(getContext(), "Please enter proportion estimate for this activity.", Toast.LENGTH_LONG).show();
            activityAddedSuccessfully = false;
            return;
        }


        ObjectActivity objectActivity = new ObjectActivity();
        objectActivity.setName(name);
        objectActivity.setError(error);

        if (rbVA.isChecked()) objectActivity.setType(ObjectActivity.VA);
        if (rbNVAE.isChecked()) objectActivity.setType(ObjectActivity.NVAE);
        if (rbNVAN.isChecked()) objectActivity.setType(ObjectActivity.NVAN);

        if (projectMode == Project.MODE_SELF_WS){
            if (!((SelfWorkSamplingInProgress) getActivity()).getProject().conductsPreSampling()) {
                float p_estimate = Float.parseFloat(etEstimatedP.getText().toString())/100f;
                if (p_estimate <= 0f || p_estimate >= 1.00f){
                    Toast.makeText(getContext(), "Invalid input for proportion estimate.", Toast.LENGTH_LONG).show();
                    activityAddedSuccessfully = false;
                    return;
                }
                objectActivity.setEstimatedProportion(p_estimate);
            }
        }else {
            if (!((ProjectInProgress) getActivity()).getProject().conductsPreSampling()){
                float p_estimate = Float.parseFloat(etEstimatedP.getText().toString())/100f;
                if (p_estimate <= 0f || p_estimate >= 1.00f){
                    Toast.makeText(getContext(), "Invalid input for proportion estimate.", Toast.LENGTH_LONG).show();
                    activityAddedSuccessfully = false;
                    return;
                }
                objectActivity.setEstimatedProportion(p_estimate);
            }
        }
        if (switchUtilization.isChecked()) objectActivity.setForUtilization(true);
        if (switchAllowances.isChecked()) objectActivity.setForAllowances(true);

//        objectActivity.setStudyObject(studyObject);
        studyObject.addActivity(objectActivity);
        objectActivity.setIndex(studyObject.getActivities().size()-1);

        activityAddedSuccessfully = true;

//        resetActivityButtons();
        activitiesLinearLayout.removeAllViews();
        createActivityButtons();
        Toast.makeText(getContext(), "Activity successfully added", Toast.LENGTH_SHORT).show();
    }

//    public android.app.AlertDialog getAddActivityDialog() {
//        return addActivityDialog;
//    }
}
