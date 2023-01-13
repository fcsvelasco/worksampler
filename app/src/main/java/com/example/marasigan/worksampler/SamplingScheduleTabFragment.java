package com.example.marasigan.worksampler;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.marasigan.worksampler.entities.ObjectActivity;
import com.example.marasigan.worksampler.entities.Project;
import com.example.marasigan.worksampler.entities.Sample;
import com.example.marasigan.worksampler.entities.StudyObject;
import com.example.marasigan.worksampler.views.ObjectActivityButton;
import com.example.marasigan.worksampler.views.SchedData;
import com.example.marasigan.worksampler.views.SchedRecyclerViewAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class SamplingScheduleTabFragment extends Fragment {
    private Project project;
    private RecyclerView schedRecyclerView;
    private TextView schedToolbarTitle;
    private Button btnBack, btnNext;
    private Calendar dateOnDisplay;
    private LinearLayoutManager layoutManager;
    private boolean layoutIsSet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_sampling_schedule_tab,container,false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        assert bundle != null;
        project = bundle.getParcelable(Project.TAG);

        dateOnDisplay = Project.artificialNow();
        dateOnDisplay.set(Calendar.YEAR, project.getSamplingDay().get(Calendar.YEAR));
        dateOnDisplay.set(Calendar.DAY_OF_YEAR, project.getSamplingDay().get(Calendar.DAY_OF_YEAR));

//        if (project.conductsPreSampling()) {
//            if (Project.artificialNow().get(Calendar.DAY_OF_YEAR) - (project.getPreSamplingStartDate().get(Calendar.DAY_OF_YEAR)
//                    + (project.getPreSamplingStartDate().get(Calendar.YEAR) - Project.artificialNow().get(Calendar.YEAR))
//                    * Project.artificialNow().getActualMaximum(Calendar.DAY_OF_YEAR))
//                    < 0) {
//                dateOnDisplay.set(Calendar.YEAR, project.getPreSamplingStartDate().get(Calendar.YEAR));
//                dateOnDisplay.set(Calendar.DAY_OF_YEAR, project.getPreSamplingStartDate().get(Calendar.DAY_OF_YEAR));
//            }
//        }else {
//            if (Project.artificialNow().get(Calendar.DAY_OF_YEAR) - (project.getActualStartDate().get(Calendar.DAY_OF_YEAR)
//                    + (project.getActualStartDate().get(Calendar.YEAR) - Project.artificialNow().get(Calendar.YEAR))
//                    * Project.artificialNow().getActualMaximum(Calendar.DAY_OF_YEAR))
//                    < 0){
//                dateOnDisplay.set(Calendar.YEAR, project.getActualStartDate().get(Calendar.YEAR));
//                dateOnDisplay.set(Calendar.DAY_OF_YEAR, project.getActualStartDate().get(Calendar.DAY_OF_YEAR));
//            }
//        }

        schedToolbarTitle = getView().findViewById(R.id.sched_toolbar_title);
//        btnBack = getView().findViewById(R.id.btn_back_sched);
//        btnNext = getView().findViewById(R.id.btn_next_sched);

//        btnBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                btnBackOnClick(view);
//            }
//        });

//        btnNext.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                btnNextOnClick(view);
//            }
//        });

        schedRecyclerView = getView().findViewById(R.id.schedRecyclerView);
        schedRecyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(schedRecyclerView.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        schedRecyclerView.setLayoutManager(layoutManager);

        checkMissedSamples();

        setSchedTitle(dateOnDisplay);
        SchedRecyclerViewAdapter schedRecyclerViewAdapter = new SchedRecyclerViewAdapter(project.getSamplesForTheDay());
        schedRecyclerView.setAdapter(schedRecyclerViewAdapter);
        scrollToLatestSample();

        layoutIsSet = true;
    }

    private void setSamplesList(){
//        samplesList = new ArrayList<>();
//        samplesList = project.getSamplesForTheDay(dateOnDisplay);
    }


    private void setSchedTitle (Calendar now){
        String stringNow;

        if (project.hasNightShift()){
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));
            tomorrow.set(Calendar.YEAR, now.get(Calendar.YEAR));
            tomorrow.add(Calendar.DAY_OF_YEAR, 1);

            if (now.get(Calendar.DAY_OF_YEAR) == now.getActualMaximum(Calendar.DAY_OF_YEAR)){
                stringNow = now.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " "
                        + now.get(Calendar.DAY_OF_MONTH) + ", " + now.get(Calendar.YEAR)
                        + " (" + now.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) +") - "
                        + tomorrow.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " "
                        + tomorrow.get(Calendar.DAY_OF_MONTH) + ", " + tomorrow.get(Calendar.YEAR)
                        + " (" + tomorrow.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) +")";
            }else{
                if (now.get(Calendar.DAY_OF_MONTH) == now.getActualMaximum((Calendar.DAY_OF_MONTH))){
                    stringNow = now.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " "
                            + now.get(Calendar.DAY_OF_MONTH)
                            + " - " + tomorrow.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " "
                            + tomorrow.get(Calendar.DAY_OF_MONTH) + ", " + now.get(Calendar.YEAR) + " ("
                            + now.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) + "-"
                            + tomorrow.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) + ")";
                }else {
                    stringNow = now.getDisplayName(Calendar.MONTH,Calendar.LONG, Locale.getDefault()) + " "
                            + now.get(Calendar.DAY_OF_MONTH) + "-" + tomorrow.get(Calendar.DAY_OF_MONTH) + ", " + now.get(Calendar.YEAR)
                            + " (" + now.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.SHORT,Locale.getDefault()) + "-"
                            + tomorrow.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.SHORT,Locale.getDefault())+")";
                }
            }

        }else {
            stringNow = now.getDisplayName(Calendar.MONTH,Calendar.LONG, Locale.getDefault()) + " "
                    + now.get(Calendar.DAY_OF_MONTH) + ", " + now.get(Calendar.YEAR)
                    + " (" + now.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.SHORT,Locale.getDefault()) + ")";
        }

        schedToolbarTitle.setText(stringNow);
    }

    public void btnBackOnClick(View view) {
//        dateOnDisplay.add(Calendar.DAY_OF_YEAR, -1);
//        setSamplesList();
//        setSchedTitle(dateOnDisplay);
//        SchedRecyclerViewAdapter newAdapter = new SchedRecyclerViewAdapter(samplesList);
//        schedRecyclerView.swapAdapter(newAdapter,true);
    }

    public void btnNextOnClick(View view) {
//        dateOnDisplay.add(Calendar.DAY_OF_YEAR, 1);
//        setSamplesList();
//        setSchedTitle(dateOnDisplay);
//        SchedRecyclerViewAdapter newAdapter = new SchedRecyclerViewAdapter(samplesList);
//        schedRecyclerView.swapAdapter(newAdapter,true);
    }

    public void onSampleRecordedOrMissed(){
        SchedRecyclerViewAdapter newAdapter = new SchedRecyclerViewAdapter(project.getSamplesForTheDay());
        schedRecyclerView.swapAdapter(newAdapter,true);
        scrollToLatestSample();
    }

    public void checkMissedSamples(){
        if (project == null) return;
        for (StudyObject studyObject : project.getStudyObjects()){
            for (Sample sample : studyObject.getSamples()){
                if (sample.getCalendarDate().before(Project.artificialNow()) && sample.getStatus() == Sample.WAITING)
                    sample.setStatus(Sample.MISSED);
            }
        }
        project.setSamplesForTheDay();
    }

    public void onDayEnded(){
        if (dateOnDisplay == null) return;
        dateOnDisplay.set(Calendar.YEAR, project.getSamplingDay().get(Calendar.YEAR));
        dateOnDisplay.set(Calendar.DAY_OF_YEAR, project.getSamplingDay().get(Calendar.DAY_OF_YEAR));
        setSchedTitle(dateOnDisplay);
        onSampleRecordedOrMissed();
    }

    private void scrollToLatestSample(){
        if (project.getSamplesForTheDay().isEmpty()) return;

        for (int i=project.getSamplesForTheDay().size()-1; i>=0; i--){
            if (project.getSamplesForTheDay().get(i).getStatus() != Sample.WAITING) {
                layoutManager.scrollToPositionWithOffset(i, 150);
                return;
            }
        }

        layoutManager.scrollToPosition(0);
    }

    public boolean layoutIsSet() {
        return layoutIsSet;
    }
}
