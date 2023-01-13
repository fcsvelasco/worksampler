package com.example.marasigan.worksampler;

//import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.marasigan.worksampler.entities.ObjectActivity;
import com.example.marasigan.worksampler.entities.Project;
import com.example.marasigan.worksampler.entities.ProjectDebugMode;
import com.example.marasigan.worksampler.entities.Sample;
import com.example.marasigan.worksampler.entities.StudyObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private final String CREATE_PROJECT = "Create project";
    private final String LOAD_PROJECT = "Load project";

    private Project project;
    private ProjectDebugMode projectDebugMode;
    private String mode;
    private Button btnCreate;
    private boolean progressBarShown;
    private android.support.v7.app.AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        if(getIntent().getBooleanExtra(Project.IS_FROM_PENDING_INTENT,false)){
            loadProject(getIntent().getStringExtra("File name"));
        }else{
            btnCreate = findViewById(R.id.btn_create_proj);
//        showProgressBar();
            File file = this.getFileStreamPath(Project.FILE_NAME);

            if (file.exists()) {
                mode = LOAD_PROJECT;
                btnCreate.setText("Load Project");
            }
            else mode = CREATE_PROJECT;
        }
//        btnCreate.setText("Load Project");
    } // endof onCreate()
    private void showProgressBar(){
        android.support.v7.app.AlertDialog.Builder progressDialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
        progressDialogBuilder.setView(getLayoutInflater().inflate(R.layout.dialog_progress_bar, null));

        progressDialog = progressDialogBuilder.create();

        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
//        progressBarShown = true;
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    public void btnCreateOnClick(View view){
        if (mode.equals(CREATE_PROJECT)) createProject();
        if (mode.equals(LOAD_PROJECT)) loadProject(Project.FILE_NAME);
//        createProject();
    }

    public void btnDemoCreateOnClick(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Dialog));
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_choose_mode,null);
        builder.setView(dialogView);
        builder.setPositiveButton("Create", null);
        builder.setNegativeButton("Cancel",null);
//        final RadioButton rbDefault = dialogView.findViewById(R.id.rb_mode_default);
        final AppCompatCheckBox cbxDifferentMode = dialogView.findViewById(R.id.cbxDifferentMode);
        final RadioButton rbSelfWS = dialogView.findViewById(R.id.rb_mode_self_WS);
        final RadioButton rbRatedWS = dialogView.findViewById(R.id.rb_mode_rated_WS);

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                cbxDifferentMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        rbSelfWS.setEnabled(b);
                        rbRatedWS.setEnabled(b);
                    }
                });
                rbSelfWS.setChecked(true);

                Button btnCreate = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                btnCreate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!cbxDifferentMode.isChecked()) projectDebugMode = new ProjectDebugMode(Project.MODE_DEFAULT);
                        else {
                            if (rbSelfWS.isChecked())
                                projectDebugMode = new ProjectDebugMode(Project.MODE_SELF_WS);
                            if (rbRatedWS.isChecked())
                                projectDebugMode = new ProjectDebugMode(Project.MODE_RATED_WS);
                        }

                        CreateProject.sProject = projectDebugMode;
                        Intent intent = new Intent(MainActivity.this, CreateProject.class);
//                        intent.putExtra(Project.TAG, (Parcelable)projectDebugMode);
                        startActivity(intent);
                    }
                });
            }
        });
        dialog.show();
    }

    public void btnSampleProjectOnClick(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar));
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_choose_mode,null);
        builder.setView(dialogView);
        builder.setPositiveButton("Create", null);
        builder.setNegativeButton("Cancel",null);
//        final RadioButton rbDefault = dialogView.findViewById(R.id.rb_mode_default);
        final AppCompatCheckBox cbxDifferentMode = dialogView.findViewById(R.id.cbxDifferentMode);
        final RadioButton rbSelfWS = dialogView.findViewById(R.id.rb_mode_self_WS);
        final RadioButton rbRatedWS = dialogView.findViewById(R.id.rb_mode_rated_WS);

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                cbxDifferentMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        rbSelfWS.setEnabled(b);
                        rbRatedWS.setEnabled(b);
                    }
                });

                rbSelfWS.setChecked(true);

                Button btnCreate = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                btnCreate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!cbxDifferentMode.isChecked()) {
                            createSampleProject(Project.MODE_DEFAULT);
                            ProjectInProgress.sProject = projectDebugMode;
                            Intent intent = new Intent(MainActivity.this, ProjectInProgress.class);
//                            intent.putExtra(Project.TAG, (Parcelable) projectDebugMode);
                            startActivity(intent);
                        }else {
                            if (rbSelfWS.isChecked()) {
                                createSampleSelfWSProject();
                                SelfWorkSamplingInProgress.sProject = projectDebugMode;
                                Intent intent = new Intent(MainActivity.this, SelfWorkSamplingInProgress.class);
//                            intent.putExtra(Project.TAG, (Parcelable) projectDebugMode);
                                startActivity(intent);

                            }
                            if (rbRatedWS.isChecked()) {
                                createSampleProject(Project.MODE_RATED_WS);
                                ProjectInProgress.sProject = projectDebugMode;
                                Intent intent = new Intent(MainActivity.this, ProjectInProgress.class);
//                            intent.putExtra(Project.TAG, (Parcelable) projectDebugMode);
                                startActivity(intent);
                            }
                        }
                    }
                });
            }
        });
        dialog.show();

    }

    public void btnLoadProjectOnClick(View view){
        loadProject(Project.FILE_NAME);
    }

    public void btnResultsOnClick(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar));
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_choose_mode,null);
        builder.setView(dialogView);
        builder.setPositiveButton("Create", null);
        builder.setNegativeButton("Cancel",null);
//        final RadioButton rbDefault = dialogView.findViewById(R.id.rb_mode_default);
        final AppCompatCheckBox cbxDifferentMode = dialogView.findViewById(R.id.cbxDifferentMode);
        final RadioButton rbSelfWS = dialogView.findViewById(R.id.rb_mode_self_WS);
        final RadioButton rbRatedWS = dialogView.findViewById(R.id.rb_mode_rated_WS);

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                cbxDifferentMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        rbSelfWS.setEnabled(b);
                        rbRatedWS.setEnabled(b);
                    }
                });

                rbSelfWS.setChecked(true);

                Button btnCreate = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                btnCreate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        showProgressBar();
//                        SystemClock.sleep(2000);
//
//                        if (rbDefault.isChecked()) createSampleProject(Project.MODE_DEFAULT);
//                        if (rbSelfWS.isChecked()) createSampleSelfWSProject();
//                        if (rbRatedWS.isChecked()) createSampleProject(Project.MODE_RATED_WS);
//
//                        createSampleResults();
//
//                        Intent intent = new Intent(MainActivity.this, Results.class);
//                        intent.putExtra(Project.TAG, (Parcelable) project);
//                        startActivity(intent);

//                        if (rbDefault.isChecked()) new LoadResultsAsyncTask(Project.MODE_DEFAULT).execute();
                        if (!cbxDifferentMode.isChecked()) new LoadResultsAsyncTask(Project.MODE_DEFAULT).execute();
                        else {
                            if (rbSelfWS.isChecked())
                                new LoadResultsAsyncTask(Project.MODE_SELF_WS).execute();
                            if (rbRatedWS.isChecked())
                                new LoadResultsAsyncTask(Project.MODE_RATED_WS).execute();
                        }
                        dialog.dismiss();

                    }
                });
            }
        });
        dialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(progressDialog != null) {
            if(progressDialog.isShowing())progressDialog.dismiss();
        }
    }

    private class LoadResultsAsyncTask extends AsyncTask<Void,Void,Void>{
        private int mode;

        private LoadResultsAsyncTask(int mode){
            this.mode = mode;
        }

        @Override
        protected void onPreExecute() {
            showProgressBar();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(mode == Project.MODE_SELF_WS) createSampleSelfWSProject();
            else createSampleProject(mode);
            createSampleResults();

            Results.sProject = projectDebugMode;
            Intent intent = new Intent(MainActivity.this, Results.class);
//            intent.putExtra(Project.TAG, (Parcelable) projectDebugMode);
            startActivity(intent);
            return null;
        }
    }

    public void createProject(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert));
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_choose_mode,null);

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View dialogView = getLayoutInflater().inflate(R.layout.add_activity_dialog,null);
//
        builder.setView(dialogView);
//        builder.create().show();

        builder.setPositiveButton("Create", null);
        builder.setNegativeButton("Cancel",null);
//        final RadioButton rbDefault = dialogView.findViewById(R.id.rb_mode_default);
        final AppCompatCheckBox cbxDifferentMode = dialogView.findViewById(R.id.cbxDifferentMode);
        final RadioButton rbSelfWS = dialogView.findViewById(R.id.rb_mode_self_WS);
        final RadioButton rbRatedWS = dialogView.findViewById(R.id.rb_mode_rated_WS);

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                cbxDifferentMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        rbSelfWS.setEnabled(b);
                        rbRatedWS.setEnabled(b);
                    }
                });


                rbSelfWS.setChecked(true);

                Button btnCreate = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                btnCreate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        if (rbDefault.isChecked()) project = new Project(Project.MODE_DEFAULT);
                        if (!cbxDifferentMode.isChecked()) project = new Project(Project.MODE_DEFAULT);
                        else {
                            if (rbSelfWS.isChecked()) project = new Project(Project.MODE_SELF_WS);
                            if (rbRatedWS.isChecked()) project = new Project(Project.MODE_RATED_WS);
                        }
                        CreateProject.sProject = project;
                        Intent intent = new Intent(MainActivity.this, CreateProject.class);
//                        intent.putExtra(Project.TAG, (Parcelable)project);
                        startActivity(intent);
                    }
                });
            }
        });
        dialog.show();

//        project = new Project();

//        Intent intent = new Intent(this, CreateProject.class);
//        intent.putExtra("Project", (Parcelable)project);
//        startActivity(intent);
    } // endof openCreateProject

    private void loadProject(String fileName){
        try {
            FileInputStream fileInputStream = this.openFileInput(fileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            project = (Project) objectInputStream.readObject();
            fileInputStream.close();
            objectInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(project.getPhase() == Project.PHASE_PROJECT_ENDED){
            Results.sProject = project;
            Intent intent = new Intent(this, Results.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.putExtra(Project.TAG, (Parcelable) project);
            startActivity(intent);
        }else {
            if (project.getMode() == Project.MODE_SELF_WS){
                SelfWorkSamplingInProgress.sProject = project;
                Intent intent = new Intent(this, SelfWorkSamplingInProgress.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.putExtra("Project", (Parcelable) project);
                startActivity(intent);
            }else {
                ProjectInProgress.sProject = project;
                Intent intent = new Intent(this, ProjectInProgress.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.putExtra("Project", (Parcelable) project);
                startActivity(intent);
            }
        }
    }

    private void createSampleSelfWSProject(){
        projectDebugMode = new ProjectDebugMode(Project.MODE_SELF_WS);
        projectDebugMode.setName("Sample Self Work Sampling");
        projectDebugMode.setzValue(Project.Z_1POINT96);
        projectDebugMode.setConductPreSampling(true);
        projectDebugMode.setHasNightShift(false);

        Calendar startOfWork = Project.artificialNow();
        Calendar endOfWork = Project.artificialNow();

//        startOfWork.add(Calendar.HOUR_OF_DAY, -2);
//        endOfWork.add(Calendar.MINUTE, 3);
        endOfWork.add(Calendar.HOUR_OF_DAY, 3);
        endOfWork.set(Calendar.DAY_OF_YEAR, startOfWork.get(Calendar.DAY_OF_YEAR));
        if (startOfWork.get(Calendar.HOUR_OF_DAY) > endOfWork.get(Calendar.HOUR_OF_DAY))
            projectDebugMode.setHasNightShift(true);

        StudyObject self = new StudyObject(StudyObject.OPERATOR);
        self.setName("Franz Velasco");
        self.setIndex(0);
        self.setShift(projectDebugMode.hasNightShift() ? StudyObject.NIGHT_SHIFT : StudyObject.MORNING_SHIFT);

        self.setStartOfWorkHour(startOfWork.get(Calendar.HOUR_OF_DAY));
        self.setStartOfWorkMinute(startOfWork.get(Calendar.MINUTE));
        self.setEndOfWorkHour(endOfWork.get(Calendar.HOUR_OF_DAY));
        self.setEndOfWorkMinute(endOfWork.get(Calendar.MINUTE));
        self.setStartOfBreakTimeHour(0);
        self.setStartOfBreakTimeMinute(0);
        self.setEndOfBreakTimeHour(0);
        self.setEndOfBreakTimeMinute(1);

        self.setWorkingOnSunday(true);
        self.setWorkingOnMonday(true);
        self.setWorkingOnTuesday(true);
        self.setWorkingOnWednesday(true);
        self.setWorkingOnThursday(true);
        self.setWorkingOnFriday(true);
        self.setWorkingOnSaturday(true);
        self.setDaysOfWork();

        for (int j=0; j<10; j++){
            ObjectActivity activity = new ObjectActivity();
            activity.setName("Activity " + j);
            activity.setIndex(j);
            if (j<4) {
                activity.setType(ObjectActivity.VA);
                activity.setEstimatedProportion(0.15f);
            }
            if (j>=4 && j<7){
                activity.setType(ObjectActivity.NVAE);
                activity.setEstimatedProportion(0.05f);
            }
            if (j>=7) {
                activity.setType(ObjectActivity.NVAN);
                activity.setEstimatedProportion(0.05f);
            }
            activity.setError(.05f);

            self.addActivity(activity);
        }
        projectDebugMode.addStudyObject(self);

        Calendar preSamplingStart = Calendar.getInstance();
        preSamplingStart.set(Calendar.MONTH, Project.artificialNow().get(Calendar.MONTH));
        preSamplingStart.set(Calendar.DAY_OF_MONTH, Project.artificialNow().get(Calendar.DAY_OF_MONTH));
        preSamplingStart.set(Calendar.YEAR, Project.artificialNow().get(Calendar.YEAR));
        projectDebugMode.setPreSamplingStartDate(preSamplingStart);

//        preSamplingStart.add(Calendar.DAY_OF_YEAR, 7);
        Calendar preSamplingEnd = Calendar.getInstance();
        preSamplingEnd.set(Calendar.MONTH, preSamplingStart.get(Calendar.MONTH));
        preSamplingEnd.set(Calendar.DAY_OF_MONTH, preSamplingStart.get(Calendar.DAY_OF_MONTH));
        preSamplingEnd.set(Calendar.YEAR, preSamplingStart.get(Calendar.YEAR));
        preSamplingEnd.add(Calendar.DAY_OF_YEAR, 7);
        projectDebugMode.setPreSamplingEndDate(preSamplingEnd);

//        preSamplingEnd.add(Calendar.DAY_OF_YEAR,1);
        Calendar actualStart = Calendar.getInstance();
        actualStart.set(Calendar.MONTH, preSamplingEnd.get(Calendar.MONTH));
        actualStart.set(Calendar.DAY_OF_MONTH, preSamplingEnd.get(Calendar.DAY_OF_MONTH));
        actualStart.set(Calendar.YEAR, preSamplingEnd.get(Calendar.YEAR));
        actualStart.add(Calendar.DAY_OF_YEAR,1);
        projectDebugMode.setActualStartDate(actualStart);

//        actualStart.add(Calendar.DAY_OF_YEAR, 14);
        Calendar actualEnd = Calendar.getInstance();
        actualEnd.set(Calendar.MONTH, actualStart.get(Calendar.MONTH));
        actualEnd.set(Calendar.DAY_OF_MONTH, actualStart.get(Calendar.DAY_OF_MONTH));
        actualEnd.set(Calendar.YEAR, actualStart.get(Calendar.YEAR));
        actualEnd.add(Calendar.DAY_OF_YEAR, 14);
        projectDebugMode.setActualEndDate(actualEnd);

        self.setPreSamplingDates();
        self.setActualSamplingDates();

        projectDebugMode.setNoOfPreSamples(1000);
        if(projectDebugMode.todayIsASamplingDay()) projectDebugMode.generateSchedule(Project.SCHED_TODAY,Project.PHASE_PRESAMPLING);
        else projectDebugMode.generateSchedule(Project.SCHED_NEXT_DAY, Project.PHASE_PRESAMPLING);

        projectDebugMode.save(this);

    }

    private void createSampleProject(int projectMode) {
        // this method creates a sample project for debugging purposes
        projectDebugMode = new ProjectDebugMode(projectMode);
        projectDebugMode.setName("Demo Project");
        projectDebugMode.setzValue(Project.Z_1POINT96);
        projectDebugMode.setConductPreSampling(true);
        projectDebugMode.setHasNightShift(false);

        Calendar startOfWork = Project.artificialNow();
        Calendar endOfWork = Project.artificialNow();

//        startOfWork.add(Calendar.HOUR_OF_DAY, -2);
//        endOfWork.add(Calendar.MINUTE, 3);
        endOfWork.add(Calendar.HOUR_OF_DAY, 3);
        endOfWork.set(Calendar.DAY_OF_YEAR, startOfWork.get(Calendar.DAY_OF_YEAR));
        if (startOfWork.get(Calendar.HOUR_OF_DAY) > endOfWork.get(Calendar.HOUR_OF_DAY))
            projectDebugMode.setHasNightShift(true);

        for (int i = 0; i<2; i++){
            // create operators for the project

            StudyObject operator = new StudyObject(StudyObject.OPERATOR);
            operator.setName("Operator " + i);
            operator.setIndex(i);
            operator.setShift(projectDebugMode.hasNightShift() ? StudyObject.NIGHT_SHIFT : StudyObject.MORNING_SHIFT);

            operator.setStartOfWorkHour(startOfWork.get(Calendar.HOUR_OF_DAY));
            operator.setStartOfWorkMinute(startOfWork.get(Calendar.MINUTE));
            operator.setEndOfWorkHour(endOfWork.get(Calendar.HOUR_OF_DAY));
            operator.setEndOfWorkMinute(endOfWork.get(Calendar.MINUTE));
            operator.setStartOfBreakTimeHour(0);
            operator.setStartOfBreakTimeMinute(0);
            operator.setEndOfBreakTimeHour(0);
            operator.setEndOfBreakTimeMinute(1);

            operator.setWorkingOnSunday(true);
            operator.setWorkingOnMonday(true);
            operator.setWorkingOnTuesday(true);
            operator.setWorkingOnWednesday(true);
            operator.setWorkingOnThursday(true);
            operator.setWorkingOnFriday(true);
            operator.setWorkingOnSaturday(true);
            operator.setDaysOfWork();

            for (int j=0; j<10; j++){
                ObjectActivity activity = new ObjectActivity();
                activity.setName("Activity " + j);
                activity.setIndex(j);
                if (j<4) {
                    activity.setType(ObjectActivity.VA);
                    activity.setEstimatedProportion(0.15f);
                    activity.setForUtilization(true);
                }
                if (j>=4 && j<7){
                    activity.setType(ObjectActivity.NVAE);
                    activity.setEstimatedProportion(0.05f);
                    activity.setForUtilization(true);
                    if (j>4) activity.setForAllowances(true);
                }
                if (j>=7) {
                    activity.setType(ObjectActivity.NVAN);
                    activity.setEstimatedProportion(0.05f);
                    if (j==7) activity.setForUtilization(true);
                }
                activity.setError(.05f);
                operator.addActivity(activity);
            }

//            project.addOperators(operator);
            projectDebugMode.addStudyObject(operator);
        }

        for (int i = 0; i<2; i++){
            // create machines for the project

            StudyObject machine = new StudyObject(StudyObject.MACHINE);
            machine.setName("Machine " + i);
            machine.setIndex(i);
            machine.setShift(projectDebugMode.hasNightShift() ? StudyObject.NIGHT_SHIFT : StudyObject.MORNING_SHIFT);

            machine.setStartOfWorkHour(startOfWork.get(Calendar.HOUR_OF_DAY));
            machine.setStartOfWorkMinute(startOfWork.get(Calendar.MINUTE));
            machine.setEndOfWorkHour(endOfWork.get(Calendar.HOUR_OF_DAY));
            machine.setEndOfWorkMinute(endOfWork.get(Calendar.MINUTE));
            machine.setStartOfBreakTimeHour(0);
            machine.setStartOfBreakTimeMinute(0);
            machine.setEndOfBreakTimeHour(0);
            machine.setEndOfBreakTimeMinute(1);

            machine.setWorkingOnSunday(true);
            machine.setWorkingOnMonday(true);
            machine.setWorkingOnTuesday(true);
            machine.setWorkingOnWednesday(true);
            machine.setWorkingOnThursday(true);
            machine.setWorkingOnFriday(true);
            machine.setWorkingOnSaturday(true);
            machine.setDaysOfWork();

            for (int j=0; j<10; j++){
                ObjectActivity activity = new ObjectActivity();
                activity.setName("Activity " + j);
                activity.setIndex(j);
                if (j<4) {
                    activity.setType(ObjectActivity.VA);
                    activity.setEstimatedProportion(0.15f);
                    activity.setForUtilization(true);
                }
                if (j>=4 && j<7){
                    activity.setType(ObjectActivity.NVAE);
                    activity.setEstimatedProportion(0.05f);
                    activity.setForUtilization(true);
                    if (j>4) activity.setForAllowances(true);
                }
                if (j>=7) {
                    activity.setType(ObjectActivity.NVAN);
                    activity.setEstimatedProportion(0.05f);
                    if (j==7) activity.setForUtilization(true);
                }
                activity.setError(.05f);
                machine.addActivity(activity);
            }

//            project.addMachines(machine);
            projectDebugMode.addStudyObject(machine);
        }
//        project.setStudyObjects();

//        addPreSamples();

        Calendar preSamplingStart = Calendar.getInstance();
        preSamplingStart.set(Calendar.MONTH, Project.artificialNow().get(Calendar.MONTH));
        preSamplingStart.set(Calendar.DAY_OF_MONTH, Project.artificialNow().get(Calendar.DAY_OF_MONTH));
        preSamplingStart.set(Calendar.YEAR, Project.artificialNow().get(Calendar.YEAR));
        projectDebugMode.setPreSamplingStartDate(preSamplingStart);

//        preSamplingStart.add(Calendar.DAY_OF_YEAR, 7);
        Calendar preSamplingEnd = Calendar.getInstance();
        preSamplingEnd.set(Calendar.MONTH, preSamplingStart.get(Calendar.MONTH));
        preSamplingEnd.set(Calendar.DAY_OF_MONTH, preSamplingStart.get(Calendar.DAY_OF_MONTH));
        preSamplingEnd.set(Calendar.YEAR, preSamplingStart.get(Calendar.YEAR));
        preSamplingEnd.add(Calendar.DAY_OF_YEAR, 7);
        projectDebugMode.setPreSamplingEndDate(preSamplingEnd);

//        preSamplingEnd.add(Calendar.DAY_OF_YEAR,1);
        Calendar actualStart = Calendar.getInstance();
        actualStart.set(Calendar.MONTH, preSamplingEnd.get(Calendar.MONTH));
        actualStart.set(Calendar.DAY_OF_MONTH, preSamplingEnd.get(Calendar.DAY_OF_MONTH));
        actualStart.set(Calendar.YEAR, preSamplingEnd.get(Calendar.YEAR));
        actualStart.add(Calendar.DAY_OF_YEAR,1);
        projectDebugMode.setActualStartDate(actualStart);

//        actualStart.add(Calendar.DAY_OF_YEAR, 14);
        Calendar actualEnd = Calendar.getInstance();
        actualEnd.set(Calendar.MONTH, actualStart.get(Calendar.MONTH));
        actualEnd.set(Calendar.DAY_OF_MONTH, actualStart.get(Calendar.DAY_OF_MONTH));
        actualEnd.set(Calendar.YEAR, actualStart.get(Calendar.YEAR));
        actualEnd.add(Calendar.DAY_OF_YEAR, 14);
        projectDebugMode.setActualEndDate(actualEnd);

        for (StudyObject studyObject : projectDebugMode.getStudyObjects()){
            studyObject.setPreSamplingDates();
            studyObject.setActualSamplingDates();
        }

//        createSampleResults();
        projectDebugMode.setNoOfPreSamples(315);
        if(projectDebugMode.todayIsASamplingDay()) projectDebugMode.generateSchedule(Project.SCHED_TODAY,Project.PHASE_PRESAMPLING);
        else projectDebugMode.generateSchedule(Project.SCHED_NEXT_DAY, Project.PHASE_PRESAMPLING);
        projectDebugMode.save(this);
//        projectDebugMode.setPhase(Project.PHASE_ACTUAL);
//        createSampleResults();
//        if(projectDebugMode.todayIsASamplingDay()) projectDebugMode.generateSchedule(Project.SCHED_TODAY,Project.PHASE_ACTUAL);
//        else projectDebugMode.generateSchedule(Project.SCHED_NEXT_DAY, Project.PHASE_ACTUAL);
    }

    private void addPreSamples(){
        int minNoOfSamplesVA = 800;
        int maxNoOfSamplesVA = 900;
        int minNoOfSamplesNVAE = 320;
        int maxNoOfSamplesNVAE = 340;
        int minNoOfSamplesNVAN = 320;
        int maxNoOfSamplesNVAN = 330;

        for (StudyObject studyObject : projectDebugMode.getStudyObjects()){
            for (ObjectActivity activity : studyObject.getActivities()){
                if (activity.getType().equals(ObjectActivity.VA))
                    activity.setNoOfSamplesTaken((int) (minNoOfSamplesVA + Math.random()*(maxNoOfSamplesVA - minNoOfSamplesVA)));
                if (activity.getType().equals(ObjectActivity.NVAE))
                    activity.setNoOfSamplesTaken((int) (minNoOfSamplesNVAE + Math.random()*(maxNoOfSamplesNVAE - minNoOfSamplesNVAE)));
                if (activity.getType().equals(ObjectActivity.NVAN))
                    activity.setNoOfSamplesTaken((int) (minNoOfSamplesNVAN + Math.random()*(maxNoOfSamplesNVAN - minNoOfSamplesNVAN)));
            }
            studyObject.computeNoOfSamplesTaken();
            studyObject.computeActivityProportions();
        }
    }

    private void createSampleResults(){
        int minNoOfSamplesVA = 200;
        int maxNoOfSamplesVA = 400;
        int minNoOfSamplesNVAE = 100;
        int maxNoOfSamplesNVAE = 150;
        int minNoOfSamplesNVAN = 50;
        int maxNoOfSamplesNVAN = 100;

//        for (StudyObject studyObject : project.getStudyObjects()){
//            for (ObjectActivity activity : studyObject.getActivities()){
//                if (activity.getType().equals(ObjectActivity.VA))
//                    activity.setNoOfSamplesTaken((int) (minNoOfSamplesVA + Math.random()*(maxNoOfSamplesVA - minNoOfSamplesVA)));
//                if (activity.getType().equals(ObjectActivity.NVAE))
//                    activity.setNoOfSamplesTaken((int) (minNoOfSamplesNVAE + Math.random()*(maxNoOfSamplesNVAE - minNoOfSamplesNVAE)));
//                if (activity.getType().equals(ObjectActivity.NVAN))
//                    activity.setNoOfSamplesTaken((int) (minNoOfSamplesNVAN + Math.random()*(maxNoOfSamplesNVAN - minNoOfSamplesNVAN)));
//            }
//            studyObject.computeNoOfSamplesTaken();
//            studyObject.computeActivityProportions();
//        }

        createSamplesListFiles();
        projectDebugMode.calculateResults();
    }

    private void createSamplesListFiles(){
        int dayOfYear = 267;

        for (int k=0; k<30; k++) {
            for(StudyObject object : projectDebugMode.getStudyObjects()) object.getSamples().clear();
            ArrayList<Sample> samplesList = new ArrayList<>();

            Calendar samplingDay = Calendar.getInstance();
            Calendar samplingDay2 = Calendar.getInstance();
            samplingDay.set(Calendar.DAY_OF_YEAR, dayOfYear);
            samplingDay2.set(Calendar.DAY_OF_YEAR, dayOfYear);

            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            Calendar breakTimeStart = Calendar.getInstance();
            Calendar breakTimeEnd = Calendar.getInstance();

            int samplesPerDay = 58;

            for (StudyObject studyObject : projectDebugMode.getStudyObjects()) {

                start.set(Calendar.HOUR_OF_DAY, studyObject.getStartOfWorkHour());
                start.set(Calendar.MINUTE, studyObject.getStartOfWorkMinute());
                end.set(Calendar.HOUR_OF_DAY, studyObject.getEndOfWorkHour());
                end.set(Calendar.MINUTE, studyObject.getEndOfWorkMinute());
                breakTimeStart.set(Calendar.HOUR_OF_DAY, studyObject.getStartOfBreakTimeHour());
                breakTimeStart.set(Calendar.MINUTE, studyObject.getStartOfBreakTimeMinute());
                breakTimeEnd.set(Calendar.HOUR_OF_DAY, studyObject.getEndOfBreakTimeHour());
                breakTimeEnd.set(Calendar.MINUTE, studyObject.getEndOfBreakTimeMinute());

                start.set(Calendar.YEAR, samplingDay.get(Calendar.YEAR));
                start.set(Calendar.DAY_OF_YEAR, samplingDay.get(Calendar.DAY_OF_YEAR));

                if (studyObject.getShift() == StudyObject.MORNING_SHIFT) {
                    end.set(Calendar.YEAR, samplingDay.get(Calendar.YEAR));
                    end.set(Calendar.DAY_OF_YEAR, samplingDay.get(Calendar.DAY_OF_YEAR));
                    breakTimeStart.set(Calendar.YEAR, samplingDay.get(Calendar.YEAR));
                    breakTimeStart.set(Calendar.DAY_OF_YEAR, samplingDay.get(Calendar.DAY_OF_YEAR));
                    breakTimeEnd.set(Calendar.YEAR, samplingDay.get(Calendar.YEAR));
                    breakTimeEnd.set(Calendar.DAY_OF_YEAR, samplingDay.get(Calendar.DAY_OF_YEAR));
                } else {
                    samplingDay2.add(Calendar.DAY_OF_YEAR, 1);
                    end.set(Calendar.YEAR, samplingDay2.get(Calendar.YEAR));
                    end.set(Calendar.DAY_OF_YEAR, samplingDay2.get(Calendar.DAY_OF_YEAR));

                    if (60 * breakTimeStart.get(Calendar.HOUR_OF_DAY) + breakTimeStart.get(Calendar.MINUTE)
                            < 60 * start.get(Calendar.HOUR_OF_DAY) + start.get(Calendar.MINUTE)) {
                        breakTimeStart.set(Calendar.YEAR, samplingDay2.get(Calendar.YEAR));
                        breakTimeStart.set(Calendar.DAY_OF_YEAR, samplingDay2.get(Calendar.DAY_OF_YEAR));
                        breakTimeEnd.set(Calendar.YEAR, samplingDay2.get(Calendar.YEAR));
                        breakTimeEnd.set(Calendar.DAY_OF_YEAR, samplingDay2.get(Calendar.DAY_OF_YEAR));
                    } else {
                        breakTimeStart.set(Calendar.YEAR, samplingDay.get(Calendar.YEAR));
                        breakTimeStart.set(Calendar.DAY_OF_YEAR, samplingDay.get(Calendar.DAY_OF_YEAR));
                        if (60 * breakTimeEnd.get(Calendar.HOUR_OF_DAY) + breakTimeEnd.get(Calendar.MINUTE)
                                < 60 * start.get(Calendar.HOUR_OF_DAY) + start.get(Calendar.MINUTE)) {
                            breakTimeEnd.set(Calendar.YEAR, samplingDay2.get(Calendar.YEAR));
                            breakTimeEnd.set(Calendar.DAY_OF_YEAR, samplingDay2.get(Calendar.DAY_OF_YEAR));
                        } else {
                            breakTimeEnd.set(Calendar.YEAR, samplingDay.get(Calendar.YEAR));
                            breakTimeEnd.set(Calendar.DAY_OF_YEAR, samplingDay.get(Calendar.DAY_OF_YEAR));
                        }
                    }
                }

                for (int i = 0; i < samplesPerDay; i++) {
                    Sample sample = new Sample(start, end, breakTimeStart, breakTimeEnd);
                    if (studyObject.getSamples().size() > 0) {
                        while (projectDebugMode.sampleHasSameTime(sample, studyObject)) {
                            sample = new Sample(start, end, breakTimeStart, breakTimeEnd);
                        }
                    }

                    if (i < 10) {
                        ObjectActivity activity = studyObject.getActivities().get(0);
                        activity.addSampleTaken();
                        sample.setActivityName(activity.getName());
                        sample.setStatus(Sample.TAKEN);
                        studyObject.addSampleTaken();
                        studyObject.computeActivityProportions();

                        if(projectDebugMode.getMode() == Project.MODE_RATED_WS) {
                            float rating = (float) (90 + Math.random() * (110 - 90));
                            sample.setRating(rating);
                            activity.addPerformanceRating(rating);
                        }
                    }
                    if (i >= 10 && i < 20) {
                        ObjectActivity activity = studyObject.getActivities().get(1);
                        activity.addSampleTaken();
                        sample.setActivityName(activity.getName());
                        sample.setStatus(Sample.TAKEN);
                        studyObject.addSampleTaken();
                        studyObject.computeActivityProportions();

                        if(projectDebugMode.getMode() == Project.MODE_RATED_WS) {
                            float rating = (float) (90 + Math.random() * (110 - 90));
                            sample.setRating(rating);
                            activity.addPerformanceRating(rating);
                        }
                    }
                    if (i >= 20 && i < 30) {
                        ObjectActivity activity = studyObject.getActivities().get(2);
                        activity.addSampleTaken();
                        sample.setActivityName(activity.getName());
                        sample.setStatus(Sample.TAKEN);
                        studyObject.addSampleTaken();
                        studyObject.computeActivityProportions();

                        if(projectDebugMode.getMode() == Project.MODE_RATED_WS) {
                            float rating = (float) (90 + Math.random() * (110 - 90));
                            sample.setRating(rating);
                            activity.addPerformanceRating(rating);
                        }
                    }
                    if (i >= 30 && i < 40) {
                        ObjectActivity activity = studyObject.getActivities().get(3);
                        activity.addSampleTaken();
                        sample.setActivityName(activity.getName());
                        sample.setStatus(Sample.TAKEN);
                        studyObject.addSampleTaken();
                        studyObject.computeActivityProportions();

                        if(projectDebugMode.getMode() == Project.MODE_RATED_WS) {
                            float rating = (float) (90 + Math.random() * (110 - 90));
                            sample.setRating(rating);
                            activity.addPerformanceRating(rating);
                        }
                    }
                    if (i >= 40 && i < 44) {
                        ObjectActivity activity = studyObject.getActivities().get(4);
                        activity.addSampleTaken();
                        sample.setActivityName(activity.getName());
                        sample.setStatus(Sample.TAKEN);
                        studyObject.addSampleTaken();
                        studyObject.computeActivityProportions();
                    }
                    if (i >= 44 && i < 48) {
                        ObjectActivity activity = studyObject.getActivities().get(5);
                        activity.addSampleTaken();
                        sample.setActivityName(activity.getName());
                        sample.setStatus(Sample.TAKEN);
                        studyObject.addSampleTaken();
                        studyObject.computeActivityProportions();
                    }
                    if (i >= 48 && i < 52){
                        ObjectActivity activity = studyObject.getActivities().get(6);
                        activity.addSampleTaken();
                        sample.setActivityName(activity.getName());
                        sample.setStatus(Sample.TAKEN);
                        studyObject.addSampleTaken();
                        studyObject.computeActivityProportions();
                    }
                    if (i >= 52 && i < 54){
                        ObjectActivity activity = studyObject.getActivities().get(7);
                        activity.addSampleTaken();
                        sample.setActivityName(activity.getName());
                        sample.setStatus(Sample.TAKEN);
                        studyObject.addSampleTaken();
                        studyObject.computeActivityProportions();
                    }
                    if (i >= 54 && i < 56){
                        ObjectActivity activity = studyObject.getActivities().get(8);
                        activity.addSampleTaken();
                        sample.setActivityName(activity.getName());
                        sample.setStatus(Sample.TAKEN);
                        studyObject.addSampleTaken();
                        studyObject.computeActivityProportions();
                    }
                    if (i >= 56){
                        ObjectActivity activity = studyObject.getActivities().get(9);
                        activity.addSampleTaken();
                        sample.setActivityName(activity.getName());
                        sample.setStatus(Sample.TAKEN);
                        studyObject.addSampleTaken();
                        studyObject.computeActivityProportions();
                    }

                    sample.setStudyObjectName(studyObject.getName());
                    studyObject.addSample(sample);
                }
            }

            for (StudyObject studyObject : projectDebugMode.getStudyObjects()) {
                studyObject.sortSamples();
                samplesList.addAll(studyObject.getSamples());
            }

            for (int i = samplesList.size() - 1; i >= 0; i--) {
                for (int j = 1; j <= i; j++) {
                    if (samplesList.get(j - 1).getCalendarDate()
                            .after(samplesList.get(j).getCalendarDate())) {
                        Sample sampleTemp = samplesList.get(j - 1);
                        samplesList.set(j - 1, samplesList.get(j));
                        samplesList.set(j, sampleTemp);
                    }
                }
            }

            saveSamplesList(this, samplingDay, samplesList);
            dayOfYear++;
        }

//        Toast.makeText(this, "Samples lists saved!", Toast.LENGTH_SHORT).show();
    }

    private void saveSamplesList(Context context, Calendar date, ArrayList<Sample> samplesForTheDay){
        try {
            FileOutputStream fos = context.openFileOutput(samplesListFileName(date), Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(samplesForTheDay);
            os.close();
            fos.close();
            projectDebugMode.addSamplesListFile(samplesListFileName(date));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String samplesListFileName(Calendar date){
        int dayOfYear = date.get(Calendar.DAY_OF_YEAR);
        int year = date.get(Calendar.YEAR);
        return "project_samples_list_" + dayOfYear + "_" + year + ".txt";
    }

    public void btnAboutOnClick(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About");
        builder.setMessage(R.string.about);

        builder.create().show();
    }
}
