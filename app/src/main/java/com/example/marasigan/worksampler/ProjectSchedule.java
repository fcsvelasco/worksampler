package com.example.marasigan.worksampler;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marasigan.worksampler.entities.ObjectActivity;
import com.example.marasigan.worksampler.entities.Project;
import com.example.marasigan.worksampler.entities.StudyObject;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Calendar;

public class ProjectSchedule extends AppCompatActivity {
    public static Project sProject;

    private Project project;
    private EditText etPresamplingStartDate, etPresamplingEndDate, etActualStartDate, etActualEndDate, etPresamplingSamples;
    private Button btnPresamplingGenerateSched, btnPresamplingSchedDetails, btnActualGenerateSched, btnActualSchedDetails;
    private DatePickerDialog datePickerDialog;
    private android.support.v7.app.AlertDialog progressDialog;
//    private Calendar presamplingStartDate, presamplingEndDate, actualStartDate, actualEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_schedule);
        setTitle("Create Project");
        getSupportActionBar().setSubtitle("Page 3 of 3");

        etPresamplingStartDate = findViewById(R.id.etPresamplingStartDate);
        etPresamplingEndDate = findViewById(R.id.etPresamplingEndDate);
        etActualStartDate = findViewById(R.id.etActualStartDate);
        etActualEndDate = findViewById(R.id.etActualEndDate);
        etPresamplingSamples = findViewById(R.id.etPresamplingSamples);

        btnPresamplingGenerateSched = findViewById(R.id.btnPresamplingGenerateSched);
        btnPresamplingSchedDetails = findViewById(R.id.btnPresamplingSchedDetails);
        btnActualGenerateSched = findViewById(R.id.btnActualGenerateSched);
        btnActualSchedDetails = findViewById(R.id.btnActualSchedDetails);

        Intent intent = getIntent();
//        project = intent.getParcelableExtra("Project");
        project = sProject;

        if(project.getMode() == Project.MODE_SELF_WS){
            TextView tvNoOfPresamples = findViewById(R.id.tv_number_of_presamples);
            tvNoOfPresamples.setText("Total number of samples:");
        }

        if (project.conductsPreSampling()){
            btnActualGenerateSched.setEnabled(false);
            btnActualSchedDetails.setEnabled(false);

            btnActualGenerateSched.setVisibility(View.GONE);
            btnActualSchedDetails.setVisibility(View.GONE);
        }else{
            etPresamplingStartDate.setEnabled(false);
            etPresamplingEndDate.setEnabled(false);
            etPresamplingSamples.setEnabled(false);
            btnPresamplingGenerateSched.setEnabled(false);
            btnPresamplingSchedDetails.setEnabled(false);

//            etPresamplingStartDate.setVisibility(View.GONE);
//            etPresamplingEndDate.setVisibility(View.GONE);
//            etPresamplingSamples.setVisibility(View.GONE);
//            btnPresamplingGenerateSched.setVisibility(View.GONE);
//            btnPresamplingSchedDetails.setVisibility(View.GONE);
            LinearLayout preSamplingLinearLayout = findViewById(R.id.preliminary_sampling_linear_layout);
            preSamplingLinearLayout.setVisibility(View.GONE);
        }

        if (project.hasSchedule()){
            if (project.conductsPreSampling()){
                Calendar start = project.getPreSamplingStartDate();
                Calendar end = project.getPreSamplingEndDate();

                etPresamplingStartDate.setText((start.get(Calendar.MONTH)+1) + "/"
                        + start.get(Calendar.DAY_OF_MONTH) + "/" + start.get(Calendar.YEAR));

                etPresamplingEndDate.setText((end.get(Calendar.MONTH)+1) + "/"
                        + end.get(Calendar.DAY_OF_MONTH) + "/" + end.get(Calendar.YEAR));

                etPresamplingSamples.setText(project.getNoOfPreSamples() + "");

                btnPresamplingSchedDetails.setEnabled(true);
                btnPresamplingSchedDetails.setVisibility(View.VISIBLE);
            }else {
                Calendar start = project.getActualStartDate();
                Calendar end = project.getActualEndDate();

                etActualStartDate.setText((start.get(Calendar.MONTH)+1) + "/"
                        + start.get(Calendar.DAY_OF_MONTH) + "/" + start.get(Calendar.YEAR));

                etActualEndDate.setText((end.get(Calendar.MONTH)+1) + "/"
                        + end.get(Calendar.DAY_OF_MONTH) + "/" + end.get(Calendar.YEAR));

                btnActualSchedDetails.setEnabled(true);
                btnActualSchedDetails.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_project, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

//        if (id == R.id.action_project_settings) {
//            return true;
//        }

        if (id == R.id.action_exit_create) {
            exitCreateProject();
            return true;
        }
        if (id == R.id.action_help_create){
            helpMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void exitCreateProject(){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("Exit");
        builder.setMessage("Are you sure you want to exit? All data will be lost.");
        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                project.delete(CreateProject.this);
                Intent intent = new Intent(ProjectSchedule.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("Cancel",null);
        builder.create().show();
    }

    private void helpMenu(){
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("This app is still in testing mode. For comments or inquiries on the use of this app, " +
//                "kindly contact:\n\nFranz Christian Velasco\nfsvelasco@up.edu.ph");
//
//        builder.create().show();

        Intent helpIntent = new Intent(this, Help.class);
        helpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(helpIntent);
    }

    public void etDateOnClick(View view){
        final EditText etDate = (EditText) view;
        final Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(ProjectSchedule.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                        etDate.setText((month+1) + "/" + day + "/" + year);
                        calendar.set(year, month, day);
                        if (etDate == etPresamplingStartDate) {
                            if (!(project.getPreSamplingStartDate().get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                                    project.getPreSamplingStartDate().get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR))){
                                if (project.conductsPreSampling()) {
                                    project.setHasSchedule(false);
                                    btnPresamplingSchedDetails.setVisibility(View.INVISIBLE);
                                }
                            }

                            project.setPreSamplingStartDate(calendar);
                        }
                        if (etDate == etPresamplingEndDate) {
                            if (!(project.getPreSamplingEndDate().get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                                    project.getPreSamplingEndDate().get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR))){
                                if (project.conductsPreSampling()) {
                                    project.setHasSchedule(false);
                                    btnPresamplingSchedDetails.setVisibility(View.INVISIBLE);
                                }
                            }
                            project.setPreSamplingEndDate(calendar);
                        }
                        if (etDate == etActualStartDate) {
                            if (!(project.getActualStartDate().get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                                    project.getActualStartDate().get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR))){
                                if (!project.conductsPreSampling()) {
                                    project.setHasSchedule(false);
                                    btnActualSchedDetails.setVisibility(View.INVISIBLE);
                                }
                            }
                            project.setActualStartDate(calendar);
                        }
                        if (etDate == etActualEndDate) {
                            if (!(project.getActualEndDate().get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                                    project.getActualEndDate().get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR))){
                                if (!project.conductsPreSampling()) {
                                    project.setHasSchedule(false);
                                    btnActualSchedDetails.setVisibility(View.INVISIBLE);
                                }
                            }
                            project.setActualEndDate(calendar);
                        }
//                        project.showDetails(ProjectSchedule.this);
                    }
                }, year, month, dayOfMonth);
        datePickerDialog.show();
    }

    public void btnGenerateSchedOnClick(View view){
//        resetNoOfSamples();
//        resetSamplingDates();

        if(view == btnPresamplingGenerateSched){

            if (TextUtils.isEmpty(etPresamplingStartDate.getText().toString())
                    || TextUtils.isEmpty(etPresamplingStartDate.getText().toString())){
                Toast.makeText(this, "Please enter start and end dates for preliminary sampling.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(etPresamplingSamples.getText().toString())){
                Toast.makeText(this, "Please enter number of samples", Toast.LENGTH_SHORT).show();
                return;
            }
            int n = Integer.parseInt(etPresamplingSamples.getText().toString());
            if (n <= 0){
                Toast.makeText(this, "Invalid input for number of samples", Toast.LENGTH_SHORT).show();
                return;
            }
            project.setNoOfPreSamples(n);

            if (project.getPreSamplingStartDate().after(project.getPreSamplingEndDate())){
                Toast.makeText(this, "Invalid start and end dates for preliminary sampling.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (project.getPreSamplingStartDate().get(Calendar.DAY_OF_YEAR) == project.getPreSamplingEndDate().get(Calendar.DAY_OF_YEAR) &&
                    project.getPreSamplingStartDate().get(Calendar.YEAR) == project.getPreSamplingEndDate().get(Calendar.YEAR)){
                Toast.makeText(this, "Invalid start and end dates for preliminary sampling.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (project.getPreSamplingStartDate().get(Calendar.DAY_OF_YEAR) <
                    (Project.artificialNow().get(Calendar.YEAR) - project.getPreSamplingStartDate().get(Calendar.YEAR))*
                            project.getPreSamplingStartDate().getActualMaximum(Calendar.DAY_OF_YEAR)
                            + Project.artificialNow().get(Calendar.DAY_OF_YEAR)){
                Toast.makeText(this, "Invalid start date for preliminary sampling.", Toast.LENGTH_SHORT).show();
                return;
            }

            resetPreSamplingDates();
            for (StudyObject studyObject : project.getStudyObjects()) studyObject.setPreSamplingDates();

            resetNoOfSamples();

            if(project.todayIsASamplingDay()) {
                project.generateSchedule(Project.SCHED_TODAY,Project.PHASE_PRESAMPLING);
                Toast.makeText(this, "Schedule for preliminary sampling has been successfully generated!", Toast.LENGTH_LONG).show();
                btnPresamplingSchedDetails.setVisibility(View.VISIBLE);
                btnPresamplingSchedDetails.setEnabled(true);
            }
            else {
                project.generateSchedule(Project.SCHED_NEXT_DAY, Project.PHASE_PRESAMPLING);
                Toast.makeText(this, "Schedule for preliminary sampling has been successfully generated!", Toast.LENGTH_LONG).show();
                btnPresamplingSchedDetails.setVisibility(View.VISIBLE);
                btnPresamplingSchedDetails.setEnabled(true);
            }
        }
        if(view == btnActualGenerateSched){
            if (project.conductsPreSampling()) {
                Toast.makeText(this, "Schedule for actual sampling will only be generated once preliminary sampling is over.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(etActualStartDate.getText().toString())
                    || TextUtils.isEmpty(etActualEndDate.getText().toString())){
                Toast.makeText(this, "Please enter start and end dates of sampling.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (project.getActualStartDate().after(project.getActualEndDate())){
                Toast.makeText(this, "Invalid start and end dates for actual sampling.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (project.getActualStartDate().get(Calendar.DAY_OF_YEAR) == project.getActualEndDate().get(Calendar.DAY_OF_YEAR) &&
                    project.getActualStartDate().get(Calendar.YEAR) == project.getActualEndDate().get(Calendar.YEAR)){
                Toast.makeText(this, "Invalid start and end dates for preliminary sampling.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (project.getActualStartDate().get(Calendar.DAY_OF_YEAR) <
                    (Project.artificialNow().get(Calendar.YEAR) - project.getActualStartDate().get(Calendar.YEAR))*
                            project.getActualStartDate().getActualMaximum(Calendar.DAY_OF_YEAR)
                            + Project.artificialNow().get(Calendar.DAY_OF_YEAR)){
                Toast.makeText(this, "Invalid start date for actual sampling.", Toast.LENGTH_SHORT).show();
                return;
            }

//            if ((project.getActualStartDate().get(Calendar.YEAR) ))
            resetActualSamplingDates();
            for (StudyObject studyObject : project.getStudyObjects()) studyObject.setActualSamplingDates();

            resetNoOfSamples();

            if(project.todayIsASamplingDay()) {
                project.generateSchedule(Project.SCHED_TODAY,Project.PHASE_ACTUAL);
                Toast.makeText(this, "Schedule for actual sampling has been successfully generated!", Toast.LENGTH_LONG).show();
                btnActualSchedDetails.setVisibility(View.VISIBLE);
                btnActualSchedDetails.setEnabled(true);
            }
            else {
                project.generateSchedule(Project.SCHED_NEXT_DAY, Project.PHASE_ACTUAL);
                Toast.makeText(this, "Schedule for actual sampling has been successfully generated!", Toast.LENGTH_LONG).show();
                btnActualSchedDetails.setVisibility(View.VISIBLE);
                btnActualSchedDetails.setEnabled(true);
            }
        }
    }

    private void resetNoOfSamples(){
        project.getSamplesForTheDay().clear();
        for (StudyObject studyObject : project.getStudyObjects()) studyObject.getSamples().clear();
    }

    private void resetPreSamplingDates(){
        for (StudyObject studyObject : project.getStudyObjects()) {
            studyObject.getPreSamplingDates().clear();
        }
    }

    private void resetActualSamplingDates(){
        for (StudyObject studyObject : project.getStudyObjects()) {
            studyObject.getActualSamplingDates().clear();
        }
    }

    public void btnSchedDetailsOnClick (View view){
        final String PRESAMPLING = "Pre sampling";
        final String ACTUAL = "Actual sampling";

        String mode = "";
        if (view == btnPresamplingSchedDetails) mode = PRESAMPLING;
        if (view == btnActualSchedDetails) mode = ACTUAL;

        StringBuilder stringBuilder = new StringBuilder();

        int samples = 0;
        int totalSamplesPerDay = 0;

        if (mode.equals(PRESAMPLING)) samples = project.getNoOfPreSamples();

        for (StudyObject studyObject : project.getStudyObjects()){

            if (mode.equals(ACTUAL)) samples = studyObject.getNoOfSamplesRequired();
            int noOfSamplingDays = 0;

            if (mode.equals(PRESAMPLING)) noOfSamplingDays = studyObject.getPreSamplingDates().size();
            if (mode.equals(ACTUAL))noOfSamplingDays = studyObject.getActualSamplingDates().size();

            int samplesPerDay = samples/noOfSamplingDays;
            if (samples%noOfSamplingDays != 0) samplesPerDay++;

            stringBuilder.append("\n"+studyObject.getName());
            if (mode.equals(PRESAMPLING)) stringBuilder.append("\n\t\tSamples: " + samples);
            if (mode.equals(ACTUAL)){
                stringBuilder.append("\n\t\tActivities: ");
                for (ObjectActivity activity : studyObject.getActivities())
                    stringBuilder.append("\n\t\t\t\t"+activity.getName()+": "+activity.getNoOfSamplesRequired()+" samples");
//                stringBuilder.append("\n\t\tTotal number of samples: " + studyObject.getNoOfSamplesRequired());
                stringBuilder.append("\n\t\tSamples: " + studyObject.getNoOfSamplesRequired());
            }
            stringBuilder.append("\n\t\tNumber of sampling days: " + noOfSamplingDays);
            stringBuilder.append("\n\t\tSamples per day: " + samplesPerDay);
//            stringBuilder.append("\n\t\tAverage interval: ").append((getLatestEndOfWork()-getEarliestStartOfWork())/samplesPerDay)
//                    .append(" minutes");

            totalSamplesPerDay += samplesPerDay;
            stringBuilder.append("\n");
        }

        stringBuilder.append("\nTotal samples per day: ").append(totalSamplesPerDay);
        stringBuilder.append("\nAverage interval: ").append((getLatestEndOfWork()-getEarliestStartOfWork())/totalSamplesPerDay)
                .append(" minutes");
        stringBuilder.append("\n");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ProjectSchedule.this);
        alertDialogBuilder.setMessage(stringBuilder.toString());
        alertDialogBuilder.create().show();
    }

    private int getEarliestStartOfWork(){
        int value = project.getStudyObjects().get(0).getStartOfWorkInMinutes();
        for (int i=1; i<project.getStudyObjects().size(); i++){
            if (value > project.getStudyObjects().get(i).getStartOfWorkInMinutes()){
                value = project.getStudyObjects().get(i).getStartOfWorkInMinutes();
            }
        }
        return value;
    }

    private int getLatestEndOfWork(){
        int value = project.getStudyObjects().get(0).getEndOfWorkInMinutes();
        for (int i=1; i<project.getStudyObjects().size(); i++){
            if (value < project.getStudyObjects().get(i).getEndOfWorkInMinutes()){
                value = project.getStudyObjects().get(i).getEndOfWorkInMinutes();
            }
        }
        return value;
    }

    public void btnPreviousOnClick (View view){
        if (project.getMode() == Project.MODE_SELF_WS){
            OperatorDetails.sProject = project;
            Intent intent = new Intent(ProjectSchedule.this,OperatorDetails.class);
//            intent.putExtra(Project.TAG,(Parcelable) project);
            intent.putExtra("object type",StudyObject.OPERATOR);
            intent.putExtra(StudyObject.TAG, (Parcelable)project.getSelfOperator());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else{
            Operators.sProject = project;
            Intent intent = new Intent(ProjectSchedule.this,Operators.class);
//            intent.putExtra(Project.TAG,(Parcelable) project);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public void btnFinishOnClick(View view){
        if (!project.hasSchedule()){
            if(project.conductsPreSampling())
                Toast.makeText(this, "Generate schedule for preliminary sampling before proceeding.", Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, "Generate schedule for actual sampling before proceeding.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(etActualStartDate.getText().toString())
                || TextUtils.isEmpty(etActualEndDate.getText().toString())){
            Toast.makeText(this, "Enter start and end dates of actual sampling before proceeding.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (project.conductsPreSampling()){
            if (project.getActualStartDate().after(project.getActualEndDate())){
                Toast.makeText(this, "Invalid start and end dates for actual sampling.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (project.getPreSamplingEndDate().after(project.getActualStartDate()) ||
                    (project.getPreSamplingEndDate().get(Calendar.YEAR) == project.getActualStartDate().get(Calendar.YEAR) &&
                    project.getPreSamplingEndDate().get(Calendar.DAY_OF_YEAR) == project.getActualStartDate().get(Calendar.DAY_OF_YEAR))){
                Toast.makeText(this, "Invalid sampling dates: preliminary and actual sampling dates are overlapping.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            for (StudyObject studyObject : project.getStudyObjects()) studyObject.setActualSamplingDates();
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ProjectSchedule.this);

        alertDialogBuilder.setPositiveButton("Finalize", null);
        alertDialogBuilder.setNegativeButton("Cancel", null );

        alertDialogBuilder.setTitle("Finalize Project Details");
        alertDialogBuilder.setMessage("Please make sure that the details entered are correct. " +
                "Project details cannot be edited once finalized." +
                "\n\nFinalize project?");

        final AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnFinalize = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                btnFinalize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        project.save(ProjectSchedule.this);
                        if (project.getMode() == Project.MODE_SELF_WS){
                            SelfWorkSamplingInProgress.sProject = project;
                            Intent intent = new Intent(ProjectSchedule.this, SelfWorkSamplingInProgress.class);
//                            intent.putExtra(Project.TAG, (Parcelable) project);
                            startActivity(intent);
                        }else {
                            ProjectInProgress.sProject = project;
                            Intent intent = new Intent(ProjectSchedule.this, ProjectInProgress.class);
//                            intent.putExtra(Project.TAG, (Parcelable) project);
                            startActivity(intent);
                        }
                    }
                });
            }
        });
        alertDialog.show();
    }

//    private void showProgressBar(){
//        android.support.v7.app.AlertDialog.Builder progressDialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
//        progressDialogBuilder.setView(getLayoutInflater().inflate(R.layout.dialog_progress_bar, null));
//
//        progressDialog = progressDialogBuilder.create();
//
//        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//        progressDialog.setCancelable(false);
//        progressDialog.setCanceledOnTouchOutside(false);
//        progressDialog.show();
////        progressBarShown = true;
////        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
////                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//    }

//    private class GenerateScheduleAsyncTask extends AsyncTask<Void,Void,Void> {
//        private byte when;
//        private int phase;
//
//        private GenerateScheduleAsyncTask(byte when,int phase){
//            this.when = when;
//            this.phase = phase;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            showProgressBar();
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            project.generateSchedule(when,phase);
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            progressDialog.dismiss();
//        }
//    }
}
