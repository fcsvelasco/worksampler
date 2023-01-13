package com.example.marasigan.worksampler;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.marasigan.worksampler.entities.ObjectActivity;
import com.example.marasigan.worksampler.entities.Project;
import com.example.marasigan.worksampler.entities.StudyObject;
import com.example.marasigan.worksampler.views.ObjectActivityButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class OperatorDetails extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{
    public static Project sProject;

    private LinearLayout activityListLayout;
    private EditText etOperatorName, etWorkingHoursStart, etWorkingHoursEnd, etBreakTimeStart, etBreakTimeEnd, clickedTimeEditText;
    private CheckBox cbSu, cbM, cbTu, cbW, cbTh, cbF, cbSa;
    private SwitchCompat switchBreakTime;
    private Project project;
    private StudyObject studyObject;
    private String time, type;
    private boolean activityAddedSuccessfully, activityEditedSuccessfully, inEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_details);

        etOperatorName = findViewById(R.id.et_operator_name);
        etWorkingHoursStart = findViewById(R.id.etWorkingHoursStart);
        etWorkingHoursEnd = findViewById(R.id.etWorkingHoursEnd);
        etBreakTimeStart = findViewById(R.id.etBreakTimeStart);
        etBreakTimeEnd = findViewById(R.id.etBreakTImeEnd);

        cbSu = findViewById(R.id.cbxSunday);
        cbM = findViewById(R.id.cbxMonday);
        cbTu = findViewById(R.id.cbxTuesday);
        cbW = findViewById(R.id.cbxWednesday);
        cbTh = findViewById(R.id.cbxThursday);
        cbF = findViewById(R.id.cbxFriday);
        cbSa = findViewById(R.id.cbxSaturday);

        switchBreakTime = findViewById(R.id.switch_break_time);
        switchBreakTimeSetOnCheckedChangeListener();

        activityListLayout = findViewById(R.id.activityListLinearLayout);

        Intent intent = getIntent();
//        project = intent.getParcelableExtra(Project.TAG);
        project = sProject;
        type = intent.getStringExtra("object type");

        if (project.getMode() == Project.MODE_SELF_WS){
            setTitle("Create Project");
            getSupportActionBar().setSubtitle("Page 2 of 3");
            etOperatorName.setHint("Please enter your name");

            Button btnCreate = findViewById(R.id.btn_create_operator);
            btnCreate.setText("Next");

            Button btnCancel = findViewById(R.id.btn_cancel_operator);
            btnCancel.setText("Previous");

        }else {
            if (type.equals(StudyObject.OPERATOR)) setTitle("Add Operator");
            if (type.equals(StudyObject.MACHINE)) {
                setTitle("Add Machine");
                ((TextView) findViewById(R.id.tv_operator_name)).setText("Machine Name:");
            }
        }

        if (intent.getParcelableExtra(StudyObject.TAG) != null) {
            studyObject = intent.getParcelableExtra(StudyObject.TAG);
            inEditMode = true;
            setEditModeLayout();
        } else {
            studyObject = new StudyObject(StudyObject.OPERATOR);
            inEditMode = false;
            setDefaultSchedule();
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
                Intent intent = new Intent(OperatorDetails.this,MainActivity.class);
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

    private void setDefaultSchedule(){
        cbM.setChecked(true);
        cbTu.setChecked(true);
        cbW.setChecked(true);
        cbTh.setChecked(true);
        cbF.setChecked(true);

        setTime(8, 0);
        etWorkingHoursStart.setText(time);
        studyObject.setStartOfWorkHour(8);
        studyObject.setStartOfWorkMinute(0);

        setTime(17, 0);
        etWorkingHoursEnd.setText(time);
        studyObject.setEndOfWorkHour(17);
        studyObject.setEndOfWorkMinute(0);

        setTime(12, 0);
        etBreakTimeStart.setText(time);
        studyObject.setStartOfBreakTimeHour(12);
        studyObject.setStartOfBreakTimeMinute(0);

        setTime(13, 0);
        etBreakTimeEnd.setText(time);
        studyObject.setEndOfBreakTimeHour(13);
        studyObject.setEndOfBreakTimeMinute(0);
    }

    private void setEditModeLayout(){
        if (project.getMode() == Project.MODE_SELF_WS){

        }else {
            if (type.equals(StudyObject.OPERATOR)) setTitle("Edit Operator Details");
            if (type.equals(StudyObject.MACHINE)){
                setTitle("Edit Machine Details");
                ((TextView) findViewById(R.id.tv_operator_name)).setText("Machine Name:");
            }
        }

        ((Button) findViewById(R.id.btn_create_operator)).setText("Save");

        etOperatorName.setText(studyObject.getName());

        if (studyObject.isWorkingOnSunday()) cbSu.setChecked(true);
        if (studyObject.isWorkingOnMonday()) cbM.setChecked(true);
        if (studyObject.isWorkingOnTuesday()) cbTu.setChecked(true);
        if (studyObject.isWorkingOnWednesday()) cbW.setChecked(true);
        if (studyObject.isWorkingOnThursday()) cbTh.setChecked(true);
        if (studyObject.isWorkingOnFriday()) cbF.setChecked(true);
        if (studyObject.isWorkingOnSaturday()) cbSa.setChecked(true);

        setTime(studyObject.getStartOfWorkHour(), studyObject.getStartOfWorkMinute());
        etWorkingHoursStart.setText(time);

        setTime(studyObject.getEndOfWorkHour(), studyObject.getEndOfWorkMinute());
        etWorkingHoursEnd.setText(time);

        if (studyObject.hasBreakTime()) {
            setTime(studyObject.getStartOfBreakTimeHour(), studyObject.getStartOfBreakTimeMinute());
            etBreakTimeStart.setText(time);
            setTime(studyObject.getEndOfBreakTimeHour(), studyObject.getEndOfBreakTimeMinute());
            etBreakTimeEnd.setText(time);
        }else{
            setTime(12, 0);
            etBreakTimeStart.setText(time);

            setTime(13, 0);
            etBreakTimeEnd.setText(time);
        }

        createActivityButtons();
    }

    public void btnCreateOperatorOnClick(View view){
        createOperator();
    }

    public void btnCancelOnClick(View view){
        cancelOperator();
    }

    public void setTimeOnClick(View view){
        android.support.v4.app.DialogFragment timePicker = new TimePickerFragment();
        timePicker.show(getSupportFragmentManager(), "time picker");

        clickedTimeEditText = (EditText) view;
    }

    public void cancelOperator(){

        if(project.getMode() == Project.MODE_SELF_WS){
            CreateProject.sProject = project;
            Intent intent = new Intent(this, CreateProject.class);
//            intent.putExtra(Project.TAG, (Parcelable) project);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else {
            Operators.sProject = project;
            Intent intent = new Intent(this, Operators.class);
//            intent.putExtra(Project.TAG, (Parcelable) project);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public void createOperator(){
        if (TextUtils.isEmpty(etOperatorName.getText().toString())){
            showAlertDialog("Enter " + type + " name", "Please input name of " + type + " before proceeding.");
            etOperatorName.requestFocus();
            return;
        }
        String name = etOperatorName.getText().toString();

        if (name.length() > Project.MAX_LENGTH_NAME){
            showAlertDialog("Invalid input", "Name should not have more than " + Project.MAX_LENGTH_NAME + " characters.");
            etOperatorName.requestFocus();
            return;
        }

        for (int i=0; i<project.getStudyObjects().size(); i++){
            if (inEditMode && i== studyObject.getIndex()) continue;
            if (name.equals(project.getStudyObjects().get(i).getName())) {
                showAlertDialog("Name already taken","The name you entered is already taken." +
                        " Please enter a new name.");
                return;
            }
        }
        studyObject.setName(name);



        if(!cbSu.isChecked() && !cbM.isChecked() && !cbTu.isChecked() && !cbW.isChecked()
                && !cbTh.isChecked() && !cbF.isChecked() && !cbSa.isChecked()){
            showAlertDialog("Enter work schedule", "Please specify " + type + "'s days of work");
            return;
        }

        studyObject.setWorkingOnSunday(cbSu.isChecked());
        studyObject.setWorkingOnMonday(cbM.isChecked());
        studyObject.setWorkingOnTuesday(cbTu.isChecked());
        studyObject.setWorkingOnWednesday(cbW.isChecked());
        studyObject.setWorkingOnThursday(cbTh.isChecked());
        studyObject.setWorkingOnFriday(cbF.isChecked());
        studyObject.setWorkingOnSaturday(cbSa.isChecked());

        studyObject.setDaysOfWork();

        if(TextUtils.isEmpty(etWorkingHoursStart.getText().toString()) ||
                TextUtils.isEmpty(etWorkingHoursEnd.getText().toString())){
            showAlertDialog("Enter work schedule", "Please input " + type + "'s work schedule");
            return;
        }

        if (studyObject.hasBreakTime()){
            if(TextUtils.isEmpty(etBreakTimeStart.getText().toString()) ||
                    TextUtils.isEmpty(etBreakTimeEnd.getText().toString())){
                showAlertDialog("Enter work schedule", "Please input " + type + "'s break time");
                return;
            }
        }

        if (studyObject.getActivities().size() < 2){
            showAlertDialog("Add activities", "Number of activities included must be at least two.");
            return;
        }

        int startOfWorkMinuteOfDay = 60*studyObject.getStartOfWorkHour() + studyObject.getStartOfWorkMinute();
        int endOfWorkMinuteOfDay = 60*studyObject.getEndOfWorkHour() + studyObject.getEndOfWorkMinute();
        int startOfBreakTimeMinuteOfDay = 60*studyObject.getStartOfBreakTimeHour() + studyObject.getStartOfBreakTimeMinute();
        int endOfBreakTimeMinuteOfDay = 60*studyObject.getEndOfBreakTimeHour() + studyObject.getEndOfBreakTimeMinute();

        if (startOfWorkMinuteOfDay == endOfWorkMinuteOfDay){
            showAlertDialog("Invalid work schedule", "The work hours you entered are invalid.");
            return;
        }

        if (startOfWorkMinuteOfDay > endOfWorkMinuteOfDay) {
//            studyObject.setShift(StudyObject.NIGHT_SHIFT);
//            project.setHasNightShift(true);

            if (studyObject.hasBreakTime()) {

                if ((startOfBreakTimeMinuteOfDay > startOfWorkMinuteOfDay || startOfBreakTimeMinuteOfDay < endOfWorkMinuteOfDay) &&
                        (endOfBreakTimeMinuteOfDay > startOfWorkMinuteOfDay || endOfBreakTimeMinuteOfDay < endOfWorkMinuteOfDay)) {
                    if (startOfBreakTimeMinuteOfDay < endOfBreakTimeMinuteOfDay) {
                        if ((startOfBreakTimeMinuteOfDay > startOfWorkMinuteOfDay && endOfBreakTimeMinuteOfDay > startOfWorkMinuteOfDay) ||
                                (endOfBreakTimeMinuteOfDay < endOfWorkMinuteOfDay && startOfBreakTimeMinuteOfDay < endOfWorkMinuteOfDay))
                            showConfirmNighShiftDialog();
                        else
                            showAlertDialog("Invalid work schedule", "The break time you entered is invalid.");
                    } else {
                        if (startOfBreakTimeMinuteOfDay > startOfWorkMinuteOfDay && endOfBreakTimeMinuteOfDay < endOfWorkMinuteOfDay)
                            showConfirmNighShiftDialog();
                        else
                            showAlertDialog("Invalid work schedule", "The break time you entered is invalid.");
                    }

                } else
                    showAlertDialog("Invalid work schedule", "The break time you entered is invalid.");
            }else{
                showConfirmNighShiftDialog();
            }
        }
        else {

            if (studyObject.hasBreakTime()) {
                if (startOfBreakTimeMinuteOfDay <= startOfWorkMinuteOfDay || endOfBreakTimeMinuteOfDay >= endOfWorkMinuteOfDay) {
                    showAlertDialog("Invalid work schedule", "The break time you entered is invalid.");
                    return;
                }
                if (startOfBreakTimeMinuteOfDay > endOfBreakTimeMinuteOfDay) {
                    showAlertDialog("Invalid work schedule", "The break time you entered is invalid.");
                    return;
                }
            }
            studyObject.setShift(StudyObject.MORNING_SHIFT);

            studyObject.setType(type);
            if (inEditMode) {
                project.getStudyObjects().set(studyObject.getIndex(), studyObject);
            } else {
                project.addStudyObject(studyObject);
                studyObject.setIndex(project.getStudyObjects().size() - 1);
//            project.showDetails(this);
            }

            if (project.getMode() == Project.MODE_SELF_WS) {
                ProjectSchedule.sProject = project;
                Intent intent = new Intent(this, ProjectSchedule.class);
//            intent.putExtra(Project.TAG, (Parcelable) project);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                if (inEditMode) Toast.makeText(this, "Changes saved.", Toast.LENGTH_SHORT).show();
                Operators.sProject = project;
                Intent intent = new Intent(this, Operators.class);
//            intent.putExtra(Project.TAG, (Parcelable) project);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }

    private void showConfirmNighShiftDialog(){
        SimpleDateFormat timeFormatStart;
        if (studyObject.getStartOfWorkHour()%12 >= 10 || studyObject.getStartOfWorkHour()%12 == 0)
            timeFormatStart = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        else
            timeFormatStart = new SimpleDateFormat("h:mm a", Locale.getDefault());

        SimpleDateFormat timeFormatEnd;
        if (studyObject.getEndOfWorkHour()%12 >= 10 || studyObject.getEndOfWorkHour()%12 == 0)
            timeFormatEnd = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        else
            timeFormatEnd = new SimpleDateFormat("h:mm a", Locale.getDefault());

        Calendar timeStart = Calendar.getInstance();
        timeStart.set(Calendar.HOUR_OF_DAY, studyObject.getStartOfWorkHour());
        timeStart.set(Calendar.MINUTE, studyObject.getStartOfWorkMinute());
        String stringTimeStart = timeFormatStart.format(timeStart.getTime());

        Calendar timeEnd = Calendar.getInstance();
        timeEnd.set(Calendar.HOUR_OF_DAY, studyObject.getEndOfWorkHour());
        timeEnd.set(Calendar.MINUTE, studyObject.getEndOfWorkMinute());
        String stringTimeEnd = timeFormatEnd.format(timeEnd.getTime());

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("Night Shift");

        StringBuilder message = new StringBuilder();
        if (type.equals(StudyObject.OPERATOR)) message.append("Operator works during the night?");
        else message.append("Machine operates during the night?");
        message.append("\n\tStart: ").append(stringTimeStart);
        message.append("\n\tEnd: ").append(stringTimeEnd);
        builder.setMessage(message);

        builder.setPositiveButton("Confirm", null);
        builder.setNegativeButton("Cancel", null);

        final android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnConfirm = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        studyObject.setShift(StudyObject.NIGHT_SHIFT);

                        studyObject.setType(type);
                        if (inEditMode) {
                            project.getStudyObjects().set(studyObject.getIndex(), studyObject);
                        } else {
                            project.addStudyObject(studyObject);
                            studyObject.setIndex(project.getStudyObjects().size() - 1);
//            project.showDetails(this);
                        }

                        if (project.getMode() == Project.MODE_SELF_WS) {
                            ProjectSchedule.sProject = project;
                            Intent intent = new Intent(OperatorDetails.this, ProjectSchedule.class);
//            intent.putExtra(Project.TAG, (Parcelable) project);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            if (inEditMode) Toast.makeText(OperatorDetails.this, "Changes saved.", Toast.LENGTH_SHORT).show();
                            Operators.sProject = project;
                            Intent intent = new Intent(OperatorDetails.this, Operators.class);
//            intent.putExtra(Project.TAG, (Parcelable) project);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }
                });

                Button btnCancel = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });

        dialog.show();
    }

    private void showAlertDialog(String title, String message){
        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.create().show();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        setTime(i, i1);
        clickedTimeEditText.setText(time);

        if (clickedTimeEditText == etWorkingHoursStart){
            studyObject.setStartOfWorkHour(i);
            studyObject.setStartOfWorkMinute(i1);
        }else if (clickedTimeEditText == etWorkingHoursEnd){
            studyObject.setEndOfWorkHour(i);
            studyObject.setEndOfWorkMinute(i1);
        }else if (clickedTimeEditText == etBreakTimeStart){
            studyObject.setStartOfBreakTimeHour(i);
            studyObject.setStartOfBreakTimeMinute(i1);
        }else if (clickedTimeEditText == etBreakTimeEnd){
            studyObject.setEndOfBreakTimeHour(i);
            studyObject.setEndOfBreakTimeMinute(i1);
        }
    }

    private void setTime(int hour, int minute){
        Calendar calendarTime = Calendar.getInstance();
        calendarTime.set(Calendar.HOUR_OF_DAY, hour);
        calendarTime.set(Calendar.MINUTE, minute);

        SimpleDateFormat timeFormat;
        if (hour%12 >= 10 || hour%12 == 0)
            timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        else
            timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

        time = timeFormat.format(calendarTime.getTime());
    }

    private void switchBreakTimeSetOnCheckedChangeListener(){
        switchBreakTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                studyObject.setHasBreakTime(b);
                etBreakTimeStart.setEnabled(b);
                etBreakTimeEnd.setEnabled(b);
            }
        });
    }

    /******************************* METHODS FOR ADDING AND EDITING ACTIVITIES ****************************/

    public void btnAddActivityOnClick(View view){
        AlertDialog.Builder addActivityDialogBuilder = new AlertDialog.Builder(OperatorDetails.this);
        final View add_activity_dialog = getLayoutInflater().inflate(R.layout.add_activity_dialog, null);
        addActivityDialogBuilder.setView(add_activity_dialog);

        addActivityDialogBuilder.setPositiveButton("Add", null);
        addActivityDialogBuilder.setNegativeButton("Cancel", null);
        final EditText etError = add_activity_dialog.findViewById(R.id.et_error);
        final EditText etEstimatedP = add_activity_dialog.findViewById(R.id.et_estimated_p);
        final RadioButton rbVA = add_activity_dialog.findViewById(R.id.rb_VA);
        final AlertDialog addActivityDialog = addActivityDialogBuilder.create();
        final boolean conductsPresampling = project.conductsPreSampling();

        addActivityDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                if (conductsPresampling) {
                    etError.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    etEstimatedP.setEnabled(false);
                }
                rbVA.setChecked(true);

                Button btnAdd = addActivityDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addActivity(add_activity_dialog);

                        if(activityAddedSuccessfully){
                            addActivityDialog.dismiss();
                        }
                    }
                });
            }
        });
        addActivityDialog.show();

        findViewById(R.id.main_layout).requestFocus();
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
            Toast.makeText(OperatorDetails.this, "Please enter name of activity.", Toast.LENGTH_LONG).show();
            activityAddedSuccessfully = false;
            return;
        }
        String name = etActivityName.getText().toString();
        for (ObjectActivity activity : studyObject.getActivities()){
            if (name.equals(activity.getName())){
                Toast.makeText(OperatorDetails.this, "Name of activity is already taken. Please enter new name.", Toast.LENGTH_LONG).show();
                activityAddedSuccessfully = false;
                return;
            }
        }

        if (name.length() > Project.MAX_LENGTH_NAME){
            Toast.makeText(this,"Invalid input for name: must not be longer than "
                    + Project.MAX_LENGTH_NAME + " characters.", Toast.LENGTH_LONG).show();
            activityAddedSuccessfully = false;
            return;
        }

        if (TextUtils.isEmpty(etError.getText().toString())){
            Toast.makeText(OperatorDetails.this, "Please enter acceptable error for this activity.", Toast.LENGTH_SHORT).show();
            activityAddedSuccessfully = false;
            return;
        }
        float error = Float.parseFloat(etError.getText().toString())/100f;
        if (error <= 0f || error > 0.10f){
            Toast.makeText(OperatorDetails.this, "Invalid input for acceptable error: " +
                    "must be more than 0 and not more than 10.", Toast.LENGTH_LONG).show();
            activityAddedSuccessfully = false;
            return;
        }

        if (etEstimatedP.isEnabled() && TextUtils.isEmpty(etEstimatedP.getText().toString())){
            Toast.makeText(OperatorDetails.this, "Please enter proportion estimate for this activity.", Toast.LENGTH_LONG).show();
            activityAddedSuccessfully = false;
            return;
        }
        float p_estimate=0;
        if(!project.conductsPreSampling()){
            p_estimate = Float.parseFloat(etEstimatedP.getText().toString())/100f;
            if (p_estimate <= 0f || p_estimate >= 1.00f){
                Toast.makeText(OperatorDetails.this, "Invalid input for proportion estimate.", Toast.LENGTH_LONG).show();
                activityAddedSuccessfully = false;
                return;
            }
        }

        ObjectActivity objectActivity = new ObjectActivity();
        objectActivity.setName(name);
        objectActivity.setError(error);
        objectActivity.setEstimatedProportion(p_estimate);

        if (rbVA.isChecked()) objectActivity.setType(ObjectActivity.VA);
        if (rbNVAE.isChecked()) objectActivity.setType(ObjectActivity.NVAE);
        if (rbNVAN.isChecked()) objectActivity.setType(ObjectActivity.NVAN);

        if (switchUtilization.isChecked()) objectActivity.setForUtilization(true);
        if (switchAllowances.isChecked()) objectActivity.setForAllowances(true);

//        objectActivity.setStudyObject(studyObject);
        studyObject.addActivity(objectActivity);
        objectActivity.setIndex(studyObject.getActivities().size()-1);

        activityAddedSuccessfully = true;

        resetActivityButtons();
        Toast.makeText(this, "Activity successfully added to " + type + ".", Toast.LENGTH_SHORT).show();
    }

    private void createActivityButtons(){
        for (ObjectActivity activity : studyObject.getActivities()){
            ObjectActivityButton btnActivity = (ObjectActivityButton) getLayoutInflater().inflate(R.layout.view_operator_activity_button, null);
            btnActivity.setObjectActivity(activity);

            btnActivity.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            btnActivity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editActivityOnClick(view);
                }
            });

            btnActivity.setText(activity.getName());
            activityListLayout.addView(btnActivity);
        }
    }

    private void resetActivityButtons(){
        activityListLayout.removeAllViews();
        createActivityButtons();
    }

    public void editActivityOnClick(View view){
        final ObjectActivityButton btnActivity = (ObjectActivityButton) view;

        AlertDialog.Builder addActivityDialogBuilder = new AlertDialog.Builder(OperatorDetails.this);
        final View add_activity_dialog = getLayoutInflater().inflate(R.layout.add_activity_dialog, null);
        addActivityDialogBuilder.setView(add_activity_dialog);

        addActivityDialogBuilder.setPositiveButton("Save", null);
        addActivityDialogBuilder.setNegativeButton("Cancel", null);
        addActivityDialogBuilder.setNeutralButton("Delete", null);

        final TextView tvTitle = add_activity_dialog.findViewById(R.id.tv_add_activity);
        final EditText etName = add_activity_dialog.findViewById(R.id.et_activity_name);
        final EditText etEstimatedP = add_activity_dialog.findViewById(R.id.et_estimated_p);
        final EditText etError = add_activity_dialog.findViewById(R.id.et_error);
        final RadioButton rbVA = add_activity_dialog.findViewById(R.id.rb_VA);
        final RadioButton rbNVAE = add_activity_dialog.findViewById(R.id.rb_NVAE);
        final RadioButton rbNVAN = add_activity_dialog.findViewById(R.id.rb_NVAN);
        final AlertDialog addActivityDialog = addActivityDialogBuilder.create();
        final boolean conductsPresampling = project.conductsPreSampling();
        final SwitchCompat switchUtilization = add_activity_dialog.findViewById(R.id.switch_utilization);
        final SwitchCompat switchAllowances = add_activity_dialog.findViewById(R.id.switch_allowances);

        addActivityDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                tvTitle.setText("Edit Activity");
                etName.setText(btnActivity.getObjectActivity().getName());
                switch (btnActivity.getObjectActivity().getType()){
                    case ObjectActivity.VA: rbVA.setChecked(true); break;
                    case ObjectActivity.NVAE: rbNVAE.setChecked(true); break;
                    case ObjectActivity.NVAN: rbNVAN.setChecked(true); break;
                }

                etError.setText(String.format(Locale.US, "%.2f",btnActivity.getObjectActivity().getError()*100));
                if (conductsPresampling) {
                    etError.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    etEstimatedP.setEnabled(false);
                }
                else {
                    etEstimatedP.setEnabled(true);
                    etEstimatedP.setText(String.format(Locale.US, "%.2f",btnActivity.getObjectActivity().getEstimatedProportion()*100));
                }
                switchUtilization.setChecked(btnActivity.getObjectActivity().isForUtilization());
                switchAllowances.setChecked(btnActivity.getObjectActivity().isForAllowances());

                Button btnEdit = addActivityDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editActivity(add_activity_dialog, btnActivity);

                        if(activityEditedSuccessfully){
                            addActivityDialog.dismiss();
                        }
                    }
                });

                Button btnDelete = addActivityDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteActivity(btnActivity, addActivityDialog);
                    }
                });
            }
        });
        addActivityDialog.show();
        findViewById(R.id.main_layout).requestFocus();


//        Toast.makeText(this, "Activity index: " + btnActivity.getObjectActivity().getIndex(), Toast.LENGTH_SHORT).show();
    }

    private void editActivity(View dialog, ObjectActivityButton btnActivity){
        EditText etActivityName = dialog.findViewById(R.id.et_activity_name);
        EditText etError = dialog.findViewById(R.id.et_error);
        EditText etEstimatedP = dialog.findViewById(R.id.et_estimated_p);
        RadioButton rbVA = dialog.findViewById(R.id.rb_VA);
        RadioButton rbNVAE = dialog.findViewById(R.id.rb_NVAE);
        RadioButton rbNVAN = dialog.findViewById(R.id.rb_NVAN);
        SwitchCompat switchUtilization = dialog.findViewById(R.id.switch_utilization);
        SwitchCompat switchAllowances = dialog.findViewById(R.id.switch_allowances);

        ObjectActivity objectActivity = btnActivity.getObjectActivity();


        if (TextUtils.isEmpty(etActivityName.getText().toString())){
            Toast.makeText(OperatorDetails.this, "Please enter name of activity.", Toast.LENGTH_SHORT).show();
            activityEditedSuccessfully = false;
            return;
        }
        String name = etActivityName.getText().toString();

        for (int i=0; i<studyObject.getActivities().size(); i++){
            if (i == objectActivity.getIndex()) continue;
            if (name.equals(studyObject.getActivities().get(i).getName())){
                Toast.makeText(OperatorDetails.this, "Name of activity is already taken. Please enter new name.", Toast.LENGTH_LONG).show();
                activityEditedSuccessfully = false;
                return;
            }
        }

        if (name.length() > Project.MAX_LENGTH_NAME){
            Toast.makeText(this,"Invalid input for name: must not be longer than "
                    + Project.MAX_LENGTH_NAME + " characters.", Toast.LENGTH_LONG).show();
            activityEditedSuccessfully = false;
            return;
        }

        if (TextUtils.isEmpty(etError.getText().toString())){
            Toast.makeText(OperatorDetails.this, "Please enter acceptable error for this activity.", Toast.LENGTH_SHORT).show();
            activityEditedSuccessfully = false;
            return;
        }
        float error = Float.parseFloat(etError.getText().toString())/100f;
        if (error <= 0f || error > 0.10f){
            Toast.makeText(OperatorDetails.this, "Invalid input for acceptable error: " +
                    "must be more than 0 and not more than 10.", Toast.LENGTH_LONG).show();
            activityEditedSuccessfully = false;
            return;
        }


        if (etEstimatedP.isEnabled() && TextUtils.isEmpty(etEstimatedP.getText().toString())){
            Toast.makeText(OperatorDetails.this, "Please enter proportion estimate for this activity.", Toast.LENGTH_SHORT).show();
            activityEditedSuccessfully = false;
            return;
        }
        float p_estimate=0;
        if(!project.conductsPreSampling()){
            p_estimate = Float.parseFloat(etEstimatedP.getText().toString())/100f;
            if (p_estimate <= 0f || p_estimate >= 1.00f){
                Toast.makeText(OperatorDetails.this, "Invalid input for proportion estimate.", Toast.LENGTH_LONG).show();
                activityEditedSuccessfully = false;
                return;
            }
        }


        objectActivity.setName(name);
        objectActivity.setError(error);
        objectActivity.setEstimatedProportion(p_estimate);

        if (rbVA.isChecked()) objectActivity.setType(ObjectActivity.VA);
        if (rbNVAE.isChecked()) objectActivity.setType(ObjectActivity.NVAE);
        if (rbNVAN.isChecked()) objectActivity.setType(ObjectActivity.NVAN);

        if (switchUtilization.isChecked()) objectActivity.setForUtilization(true);
        if (switchAllowances.isChecked()) objectActivity.setForAllowances(true);

        objectActivity.setStudyObject(studyObject);
        studyObject.getActivities().set(objectActivity.getIndex(), objectActivity);

        activityEditedSuccessfully = true;

        btnActivity.setText(objectActivity.getName());

        Toast.makeText(this, "Activity successfully edited.", Toast.LENGTH_SHORT).show();
    }

    private void deleteActivity(final ObjectActivityButton btnActivity, final AlertDialog dialog){
        android.support.v7.app.AlertDialog.Builder confirmDeleteDialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
        confirmDeleteDialogBuilder.setTitle("Delete activity");
        confirmDeleteDialogBuilder.setMessage("Are you sure you want to delete " + btnActivity.getObjectActivity().getName()
                + " from the list of activities?");
        confirmDeleteDialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                studyObject.getActivities().remove(btnActivity.getObjectActivity());
                resetActivityButtons();
//                for (int j=0; i<studyObject.getActivities().size(); j++){
//                    studyObject.getActivities().get(j).setIndex(j);
//                }
                for (ObjectActivity activity : studyObject.getActivities()){
                    activity.setIndex(studyObject.getActivities().indexOf(activity));
                }
                dialog.dismiss();
            }
        });

        confirmDeleteDialogBuilder.setNegativeButton("Cancel", null);

        confirmDeleteDialogBuilder.create().show();
    }
}
