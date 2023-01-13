package com.example.marasigan.worksampler;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marasigan.worksampler.entities.Project;
import com.example.marasigan.worksampler.entities.Sample;
import com.example.marasigan.worksampler.entities.StudyObject;
import com.example.marasigan.worksampler.views.ObjectActivityButton;

import java.util.concurrent.TimeUnit;

public class OperatorsMachinesTabFragment extends Fragment /*implements Serializable */{
    private Project project;
    private byte[] getSampleDialogMode;
    private GetSampleDialogFragment[] getSampleDialogFragment;
    private CountDownTimer[] countDownToNextSample, countDownForGettingSample;
    private ObjectActivityButton clickedBtnObject;
    private LinearLayout operatorsLinearLayout, machinesLinearLayout;
    private boolean layoutIsSet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_operators_machines_tab,container,false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        assert bundle != null;
        project = bundle.getParcelable("Project");
        getSampleDialogFragment = new GetSampleDialogFragment[project.getStudyObjects().size()];
        countDownToNextSample = new CountDownTimer[project.getStudyObjects().size()];
        countDownForGettingSample = new CountDownTimer[project.getStudyObjects().size()];
        getSampleDialogMode = new byte[project.getStudyObjects().size()];

        operatorsLinearLayout = getView().findViewById(R.id.operators_linear_layout_project_in_progress);
        machinesLinearLayout = getView().findViewById(R.id.machines_linear_layout_project_in_progress);
        createObjectButtons();

        TextView tvOperators = getView().findViewById(R.id.tv_operators_project_in_progress);
        if (project.getNoOfOperators() == 0) tvOperators.setVisibility(View.GONE);
        else if (project.getNoOfOperators() == 1) tvOperators.setText("Operator");

        TextView tvMachines = getView().findViewById(R.id.tv_machines_project_in_progress);
        if (project.getNoOfMachines() == 0) tvMachines.setVisibility(View.GONE);
        else if (project.getNoOfMachines() == 1) tvMachines.setText("Machine");

        layoutIsSet = true;
//        Toast.makeText(getContext(), "Number of today's samples: " + project.getSamplesForTheDay().size(), Toast.LENGTH_SHORT).show();
    }

    private void createObjectButtons(){
        int index = 0;

        for (StudyObject studyObject: project.getStudyObjects()){
            ObjectActivityButton btnObject = (ObjectActivityButton) getLayoutInflater().inflate(R.layout.view_operator_activity_button, null);
            btnObject.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            btnObject.setStudyObject(studyObject);
            btnObject.setIndex(index);
            index ++;

            String text = studyObject.getName();
            btnObject.setText(text);

            btnObject.setOnClickListener(new BtnObjectOnClickListener());

            if (studyObject.getSamples().size()>0) {
                long differenceInMillis = Project.artificialNow().getTimeInMillis()
                        - studyObject.currentSample().getCalendarDate().getTimeInMillis();
                if (differenceInMillis > 0 && differenceInMillis <= project.getSampleDuration() * 1000
                        && studyObject.currentSample().getStatus() == Sample.WAITING) {
                    setCountDownForGettingSample(btnObject);
                    getSampleDialogMode[btnObject.getIndex()] = GetSampleDialogFragment.GET_SAMPLE;
                    btnObject.setStyle(ObjectActivityButton.STYLE_GREEN);
                } else {
                    if (studyObject.findNextSample() != null) setCountDownToNextSample(btnObject);
                }
            }

            if (studyObject.getType().equals(StudyObject.OPERATOR)) operatorsLinearLayout.addView(btnObject);
            if (studyObject.getType().equals(StudyObject.MACHINE)) machinesLinearLayout.addView(btnObject);
        }

        for (int i=0; i<index; i++) getSampleDialogFragment[i] = new GetSampleDialogFragment();
    }

    public void resetObjectButtons(){
        if(operatorsLinearLayout == null) return;
        if(machinesLinearLayout == null) return;
        if(operatorsLinearLayout.getChildCount() != 0) operatorsLinearLayout.removeAllViews();
        if(machinesLinearLayout.getChildCount() != 0)machinesLinearLayout.removeAllViews();

        cancelCountdownTimers();
        createObjectButtons();
    }

    public void onDayEnded(){
        resetObjectButtons();
    }

    private void setCountDownToNextSample(View view){
        final ObjectActivityButton btnObject = (ObjectActivityButton) view;
        long millisTillNextSample = btnObject.getStudyObject().findNextSample().getCalendarDate().getTimeInMillis()
                - Project.artificialNow().getTimeInMillis();

        if (countDownToNextSample[btnObject.getIndex()] != null) countDownToNextSample[btnObject.getIndex()].cancel();
        countDownToNextSample[btnObject.getIndex()] = new CountDownTimer(millisTillNextSample, 1000) {
            @Override
            public void onTick(long l) {
                int hours = (int) TimeUnit.MILLISECONDS.toHours(l);
                int minutes = (int) (TimeUnit.MILLISECONDS.toMinutes(l) % TimeUnit.HOURS.toMinutes(1));
                int seconds = (int) (TimeUnit.MILLISECONDS.toSeconds(l) % TimeUnit.MINUTES.toSeconds(1));

                String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                btnObject.setText(btnObject.getStudyObject().getName() + " (" + time + ")");

            }

            @Override
            public void onFinish() {
                getSampleDialogMode[btnObject.getIndex()] = GetSampleDialogFragment.GET_SAMPLE;

                if (getSampleDialogFragment[btnObject.getIndex()].getDialog()!= null
                        && getSampleDialogFragment[btnObject.getIndex()].getDialog().isShowing()){
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(StudyObject.TAG,btnObject.getStudyObject());
                    bundle.putParcelable(Sample.TAG, btnObject.getStudyObject().currentSample());
//                    bundle.putSerializable("Tab", OperatorsMachinesTabFragment.this);
                    bundle.putByte("Mode", getSampleDialogMode[btnObject.getIndex()]);
                    bundle.putInt(Project.MODE_TAG,project.getMode());
                    bundle.putInt(Project.SAMPLE_DURATION_TAG, project.getSampleDuration());
                    bundle.putFloat(Project.Z_VALUE_TAG, project.getzValue());
                    bundle.putInt("Index",btnObject.getIndex());

                    getSampleDialogFragment[btnObject.getIndex()].setArguments(bundle);
                    getSampleDialogFragment[btnObject.getIndex()].modeChanged();
                }
//
                btnObject.setStyle(ObjectActivityButton.STYLE_GREEN);
                setCountDownForGettingSample(btnObject);
//                resetObjectButtons();
                playSoundAndVibrate();
            }
        }.start();
    }

    private void setCountDownForGettingSample(View view){
        final ObjectActivityButton btnObject = (ObjectActivityButton) view;
        long millisToGetSample = btnObject.getStudyObject().currentSample().getCalendarDate().getTimeInMillis()
                - Project.artificialNow().getTimeInMillis() + project.getSampleDuration()*1000;

        if (countDownForGettingSample[btnObject.getIndex()] != null) countDownForGettingSample[btnObject.getIndex()].cancel();
        countDownForGettingSample[btnObject.getIndex()] = new CountDownTimer(millisToGetSample, 1000) {
            @Override
            public void onTick(long l) {
                String text = btnObject.getStudyObject().getName() + " - GET SAMPLE NOW ("
                        + TimeUnit.MILLISECONDS.toSeconds(l) + ")";
                btnObject.setText(text);
            }

            @Override
            public void onFinish() {
                if(getActivity() == null) return;
//                if (!(getSampleDialogFragment[btnObject.getIndex()].getAddActivityDialog() != null
//                        && getSampleDialogFragment[btnObject.getIndex()].getAddActivityDialog().isShowing()))
                Toast.makeText(getContext(), "Sample missed!", Toast.LENGTH_SHORT).show();

                btnObject.getStudyObject().currentSample().setStatus(Sample.MISSED);
                ((ProjectInProgress)getActivity()).onSampleMissed();
//                project.save(getContext());
                getSampleDialogMode[btnObject.getIndex()] = GetSampleDialogFragment.CHECK_STATUS;

                if (getSampleDialogFragment[btnObject.getIndex()].getDialog()!= null
                        && getSampleDialogFragment[btnObject.getIndex()].getDialog().isShowing()){

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(StudyObject.TAG,btnObject.getStudyObject());
                    if(btnObject.getStudyObject().findNextSample() != null)
                        bundle.putParcelable(Sample.TAG, btnObject.getStudyObject().findNextSample());
                    bundle.putByte("Mode", getSampleDialogMode[btnObject.getIndex()]);
                    bundle.putInt(Project.MODE_TAG,project.getMode());
                    bundle.putInt(Project.SAMPLE_DURATION_TAG, project.getSampleDuration());
                    bundle.putFloat(Project.Z_VALUE_TAG, project.getzValue());
                    bundle.putInt("Index",btnObject.getIndex());

                    getSampleDialogFragment[btnObject.getIndex()].setArguments(bundle);
                    getSampleDialogFragment[btnObject.getIndex()].modeChanged();
                }
                btnObject.setStyle(ObjectActivityButton.STYLE_DEFAULT);

                if(btnObject.getStudyObject().findNextSample() != null) setCountDownToNextSample(btnObject);
                else btnObject.setText(btnObject.getStudyObject().getName());
            }
        }.start();
    }

    private void playSoundAndVibrate(){
        if (getContext().getSystemService(getContext().VIBRATOR_SERVICE) == null) return;
        Vibrator vibrator = (Vibrator) getContext().getSystemService(getContext().VIBRATOR_SERVICE);
        Uri ringtoneSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(getContext(),ringtoneSound);

        vibrator.vibrate(500);
        ringtone.play();
    }

    public void onSampleRecorded(){
        getSampleDialogMode[clickedBtnObject.getIndex()] = GetSampleDialogFragment.CHECK_STATUS;
        countDownForGettingSample[clickedBtnObject.getIndex()].cancel();
        if(clickedBtnObject.getStudyObject().findNextSample() != null) setCountDownToNextSample(clickedBtnObject);
        else clickedBtnObject.setText(clickedBtnObject.getStudyObject().getName());

        clickedBtnObject.setStyle(ObjectActivityButton.STYLE_DEFAULT);

        Bundle bundle = new Bundle();
        bundle.putParcelable(StudyObject.TAG,clickedBtnObject.getStudyObject());
        if(clickedBtnObject.getStudyObject().findNextSample() != null)
            bundle.putParcelable(Sample.TAG, clickedBtnObject.getStudyObject().findNextSample());
        bundle.putByte("Mode", getSampleDialogMode[clickedBtnObject.getIndex()]);
        bundle.putInt(Project.MODE_TAG,project.getMode());
        bundle.putInt(Project.SAMPLE_DURATION_TAG, project.getSampleDuration());
        bundle.putFloat(Project.Z_VALUE_TAG, project.getzValue());
        bundle.putInt("Index",clickedBtnObject.getIndex());

        getSampleDialogFragment[clickedBtnObject.getIndex()].setArguments(bundle);
        getSampleDialogFragment[clickedBtnObject.getIndex()].modeChanged();
    }

    public void cancelCountdownTimers(){
        for (CountDownTimer aCountDownToNextSample : countDownToNextSample) {
            if (aCountDownToNextSample != null) aCountDownToNextSample.cancel();
        }

        for (CountDownTimer aCountDownForGettingSample : countDownForGettingSample) {
            if (aCountDownForGettingSample != null) aCountDownForGettingSample.cancel();
        }
    }

    public CountDownTimer getCountDownForGettingSample(int index) {
        return countDownForGettingSample[index];
    }

    public CountDownTimer getCountDownToNextSample(int index) {
        return countDownToNextSample[index];
    }

    public boolean layoutIsSet() {
        return layoutIsSet;
    }

    private class BtnObjectOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            ObjectActivityButton btnObject = (ObjectActivityButton) view;
            clickedBtnObject = btnObject;

            Bundle bundle = new Bundle();
            bundle.putParcelable(StudyObject.TAG,btnObject.getStudyObject());
            if (btnObject.getStudyObject().getSamples().size() > 0) {
                if (getSampleDialogMode[clickedBtnObject.getIndex()] == GetSampleDialogFragment.CHECK_STATUS)
                    bundle.putParcelable(Sample.TAG, btnObject.getStudyObject().findNextSample());
                if (getSampleDialogMode[clickedBtnObject.getIndex()] == GetSampleDialogFragment.GET_SAMPLE)
                    bundle.putParcelable(Sample.TAG, btnObject.getStudyObject().currentSample());
            }
//            bundle.putSerializable("Tab", OperatorsMachinesTabFragment.this);
            bundle.putByte("Mode", getSampleDialogMode[clickedBtnObject.getIndex()]);
            bundle.putInt(Project.MODE_TAG,project.getMode());
            bundle.putInt(Project.SAMPLE_DURATION_TAG, project.getSampleDuration());
            bundle.putFloat(Project.Z_VALUE_TAG, project.getzValue());
            bundle.putInt("Index",btnObject.getIndex());

            getSampleDialogFragment[btnObject.getIndex()].setArguments(bundle);
            getSampleDialogFragment[btnObject.getIndex()].show(getActivity().getSupportFragmentManager(),"Get Sample");
        }
    }
}
