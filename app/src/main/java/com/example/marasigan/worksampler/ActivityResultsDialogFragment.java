package com.example.marasigan.worksampler;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marasigan.worksampler.entities.ObjectActivity;
import com.example.marasigan.worksampler.entities.Project;

import org.w3c.dom.Text;

public class ActivityResultsDialogFragment extends DialogFragment {
    private ObjectActivity activity;
    private float calculatedAllowances;
    private LinearLayout quantityLinearLayout, productionHoursLinearLayout,ratingLinearLayout;
    private RelativeLayout allowancesRelativeLayout;
    private Button btnCalculate, btnCalculateStandardTime;
    private TextView tvSTCalculation, tvStandardTime;
    private RadioButton rbCalculatedAllowances, rbOtherAllowances;
    private EditText etOtherAllowances, etQuantityProduced, etProductionHours, etPerformanceRating;
    private int projectMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_results_dialog,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        assert bundle != null;
        activity = bundle.getParcelable(ObjectActivity.TAG);
        calculatedAllowances = bundle.getFloat("Allowances");
        projectMode = bundle.getInt(Project.MODE_TAG);

        setLayout();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private void setLayout(){
        TextView tvActivityName = getView().findViewById(R.id.tv_results_activity_name);
        tvActivityName.setText(activity.getName());

        TextView tvType = getView().findViewById(R.id.results_activity_type);
        String type = "Type: " + activity.getType();
        tvType.setText(type);

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int pxWidth = displayMetrics.widthPixels;

        Button btnClose = getView().findViewById(R.id.btn_close_activity_results_dialog);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        LinearLayout.LayoutParams layoutParams = new
                LinearLayout.LayoutParams(pxWidth*3/4, ViewGroup.LayoutParams.MATCH_PARENT);

        LinearLayout titleBar = getView().findViewById(R.id.activity_results_dialog_title_bar);
        titleBar.setLayoutParams(layoutParams);

        quantityLinearLayout = getView().findViewById(R.id.quantity_produced_linear_layout);
        productionHoursLinearLayout = getView().findViewById(R.id.production_hours_linear_layout);
        ratingLinearLayout = getView().findViewById(R.id.performance_rating_linear_layout);
        allowancesRelativeLayout = getView().findViewById(R.id.allowances_relative_layout);

        btnCalculate = getView().findViewById(R.id.btn_standard_time);
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnCalculateOnClick();
            }
        });

        tvSTCalculation = getView().findViewById(R.id.tv_standard_time_calculation);
        btnCalculateStandardTime = getView().findViewById(R.id.btn_calculate_standard_time);
        btnCalculateStandardTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnCalculateStandardTimeOnClick();
            }
        });

        if (!activity.getType().equals(ObjectActivity.VA)) {
            btnCalculateStandardTime.setVisibility(View.GONE);
            TextView tvStandardTimeLabel = getView().findViewById(R.id.results_activity_standard_time_label);
            tvStandardTimeLabel.setVisibility(View.GONE);
        }

        rbCalculatedAllowances = getView().findViewById(R.id.rb_allowances_calculated);
        rbOtherAllowances = getView().findViewById(R.id.rb_allowances_other);
        etOtherAllowances = getView().findViewById(R.id.et_other_allowances);

        RadioGroup rgAllowances = getView().findViewById(R.id.rg_allowances);
        rgAllowances.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (rbCalculatedAllowances.isChecked()){
                    etOtherAllowances.setEnabled(false);
                }else {
                    etOtherAllowances.setEnabled(true);
                    etOtherAllowances.requestFocus();
                }
            }
        });

        TextView tvSamplesTaken = getView().findViewById(R.id.results_activity_samples_taken);
        String samplesTaken = "Samples taken: " + activity.getNoOfSamplesTaken();
        tvSamplesTaken.setText(samplesTaken);

        TextView tvProportion = getView().findViewById(R.id.results_activity_proportion);
        String proportion = "Proportion: " + activity.getProportionConfidenceInterval();
        tvProportion.setText(proportion);

        if(projectMode == Project.MODE_RATED_WS && activity.getType().equals(ObjectActivity.VA)) {
            TextView tvAveragePerformanceRating = getView().findViewById(R.id.results_activity_performance_rating);
            tvAveragePerformanceRating.setVisibility(View.VISIBLE);

            TextView tvAveragePerformanceRatingValue = getView().findViewById(R.id.results_activity_performance_rating_value);
            tvAveragePerformanceRatingValue.setText(activity.getStringAverageRating());
            tvAveragePerformanceRatingValue.setVisibility(View.VISIBLE);
        }

        tvStandardTime = getView().findViewById(R.id.results_activity_standard_time);

        etQuantityProduced = getView().findViewById(R.id.et_quantity_produced);
        etProductionHours = getView().findViewById(R.id.et_total_production_hours);
        etPerformanceRating = getView().findViewById(R.id.et_performance_rating);

        if (activity.hasCalculatedST()){
            btnCalculateStandardTimeOnClick();

            String output = activity.getOutput() + "";
            etQuantityProduced.setText(output);

            String prodHours = activity.getProductionHours() + "";
            etProductionHours.setText(prodHours);

            String performanceRating = activity.getPerformanceRating() + "";
            etPerformanceRating.setText(performanceRating);

            if (activity.usedCalculatedAllowances()) rbCalculatedAllowances.setChecked(true);
            else {
                rbOtherAllowances.setChecked(true);
                String allowances = (activity.getAllowances()*100) + "";
                etOtherAllowances.setText(allowances);
            }
            tvStandardTime.setText(activity.getStringStandardTime());
        }
    }

    private void btnCalculateStandardTimeOnClick(){
        tvSTCalculation.setVisibility(View.VISIBLE);
        quantityLinearLayout.setVisibility(View.VISIBLE);
        productionHoursLinearLayout.setVisibility(View.VISIBLE);
        if(projectMode != Project.MODE_RATED_WS) ratingLinearLayout.setVisibility(View.VISIBLE);
        allowancesRelativeLayout.setVisibility(View.VISIBLE);
        btnCalculate.setVisibility(View.VISIBLE);
        btnCalculateStandardTime.setVisibility(View.GONE);
    }

    private void btnCalculateOnClick(){

        if (projectMode == Project.MODE_RATED_WS){
            if (TextUtils.isEmpty(etQuantityProduced.getText().toString())
                    || TextUtils.isEmpty(etProductionHours.getText().toString())){
                Toast.makeText(getContext(), "Please input necessary data to calculate standard time.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }else {
            if (TextUtils.isEmpty(etQuantityProduced.getText().toString())
                    || TextUtils.isEmpty(etProductionHours.getText().toString())
                    || TextUtils.isEmpty(etPerformanceRating.getText().toString())) {
                Toast.makeText(getContext(), "Please input necessary data to calculate standard time.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (rbOtherAllowances.isChecked() && TextUtils.isEmpty(etOtherAllowances.getText().toString())){
            Toast.makeText(getContext(), "Please input necessary data to calculate standard time.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        float output = Float.parseFloat(etQuantityProduced.getText().toString());
        if (output <= 0){
            Toast.makeText(getContext(), "Invalid input: production output must be greater than 0.", Toast.LENGTH_LONG).show();
            return;
        }
        activity.setOutput(output);

        float prodHours = Float.parseFloat(etProductionHours.getText().toString());
        if (prodHours <= 0){
            Toast.makeText(getContext(), "Invalid input: production hours must be greater than 0.", Toast.LENGTH_LONG).show();
            return;
        }
        activity.setProductionHours(prodHours);

        if(projectMode != Project.MODE_RATED_WS) {
            float rating = Float.parseFloat(etPerformanceRating.getText().toString());
            if (rating < 80 || rating > 120){
                Toast.makeText(getContext(), "Invalid input: performance rating must be between 80 and 120.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            activity.setPerformanceRating(rating);
        }

        if (rbCalculatedAllowances.isChecked()) {
            activity.setAllowances(calculatedAllowances);
            activity.setUsedCalculatedAllowances(true);
        }
        if (rbOtherAllowances.isChecked()) {
            float otherAllowances = Float.parseFloat(etOtherAllowances.getText().toString()) / 100f;
            if (otherAllowances > 0.20f){
                Toast.makeText(getContext(), "Invalid input: allowances must be between 0 and 20",
                        Toast.LENGTH_LONG).show();
                return;
            }
            activity.setAllowances(otherAllowances);
            activity.setUsedCalculatedAllowances(false);
        }

        activity.calculateStandardTime(projectMode);
        tvStandardTime.setText(activity.getStringStandardTime());
    }
}
