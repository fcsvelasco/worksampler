package com.example.marasigan.worksampler;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.marasigan.worksampler.entities.Project;
import com.example.marasigan.worksampler.entities.StudyObject;

public class CreateProject extends AppCompatActivity {
    public static Project sProject;

    private Project project;
    private EditText etProjectName, etZValue;
    private RadioButton rbZ1point645, rbZ1point96, rbZ2point58, rbInputZ, rbInputP, rbConductPreSampling;
    private RadioGroup rgZValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);
        setTitle(R.string.create_project_title);
        getSupportActionBar().setSubtitle("Page 1 of 3");


        Intent intent = getIntent();
//        project = intent.getParcelableExtra(Project.TAG);
        project = sProject;

        etProjectName = findViewById(R.id.et_project_name);
        rbZ1point645 = findViewById(R.id.rb_z1_645);
        rbZ1point96 = findViewById(R.id.rb_z1_96);
        rbZ2point58 = findViewById(R.id.rb_z2_58);
        rbInputZ = findViewById(R.id.rb_z_input);
        etZValue = findViewById(R.id.et_z_value);
        rgZValue = findViewById(R.id.rgZValue);
        rbInputP = findViewById(R.id.rb_input_p);
        rbConductPreSampling = findViewById(R.id.rb_pre_sampling);

        etZValue.setEnabled(false);
        rbZ1point645.setChecked(true);
        rbConductPreSampling.setChecked(true);

        rgZValue.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (rbInputZ.isChecked()) {
                    etZValue.setEnabled(true);
                    etZValue.requestFocus();
                }
                else {
                    etZValue.setEnabled(false);
                }
            }
        });

        if (!project.isNew()) setFieldValues();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit");
        builder.setMessage("Are you sure you want to exit? All data will be lost.");
        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                project.delete(CreateProject.this);
                Intent intent = new Intent(CreateProject.this,MainActivity.class);
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

    private void setFieldValues(){
        etProjectName.setText(project.getName());

        if (project.getzValue() == Project.Z_1POINT645) rbZ1point645.setChecked(true);
        else if (project.getzValue() == Project.Z_1POINT96) rbZ1point96.setChecked(true);
        else if (project.getzValue() == Project.Z_2POINT58) rbZ2point58.setChecked(true);
        else{
            rbInputZ.setChecked(true);
            etZValue.setText(project.getzValue() + "");
        }

        if (project.conductsPreSampling()) rbConductPreSampling.setChecked(true);
        else rbInputP.setChecked(true);
    }

    public void btnNextOnClick(View view){
        String name;

        if (TextUtils.isEmpty(etProjectName.getText().toString())){
            showAlertDialog("Enter project name", "Please input project name before proceeding.");
            etProjectName.requestFocus();
            return;
        }else
            name = etProjectName.getText().toString();


        if (name.length() > Project.MAX_LENGTH_NAME){
            showAlertDialog("Invalid input", "Project name must not be longer than " + Project.MAX_LENGTH_NAME + " characters.");
            etProjectName.requestFocus();
            return;
        }

        project.setName(name);

        if (rbInputZ.isChecked()){
            if (TextUtils.isEmpty(etZValue.getText().toString())){
                showAlertDialog("Enter z-value", "Please input preferred z-value.");
                etZValue.requestFocus();
                return;
            }else{
                float zValue = Float.parseFloat(etZValue.getText().toString());
                if (zValue < 0 || zValue > 3.5){
                    showAlertDialog("Invalid input", "Z-value must not be less than 0 or greater than 3.50.");
                    etZValue.requestFocus();
                    return;
                }
            }
        }

        setProjectZValue();

        project.setNew(false);

        setProjectConductPreSampling();

        if (project.getMode() == Project.MODE_SELF_WS){
            OperatorDetails.sProject = project;
            Intent intent = new Intent(this,OperatorDetails.class);
//            intent.putExtra(Project.TAG, (Parcelable) project);
            intent.putExtra("object type", StudyObject.OPERATOR);
            startActivity(intent);
        }else {
            Operators.sProject = project;
            Intent intent = new Intent(this,Operators.class);
//            intent.putExtra(Project.TAG, (Parcelable) project);
            startActivity(intent);
        }
    }

    public void btnCancelOnClick(View view){
        cancelCreateProject();
    }


    public void cancelCreateProject(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

//    private void createProject(){
//        project = new Project(etProjectName.getText().toString());
//    }

    private void setProjectZValue(){
        if (rbZ1point645.isChecked()) project.setzValue(Project.Z_1POINT645);
        else if (rbZ1point96.isChecked()) project.setzValue(Project.Z_1POINT96);
        else if (rbZ2point58.isChecked()) project.setzValue(Project.Z_2POINT58);
        else if (rbInputZ.isChecked()){
            float zValue = Float.parseFloat(etZValue.getText().toString());
            project.setzValue(zValue);
        }
    }

    private void setProjectConductPreSampling(){
        if (rbInputP.isChecked()) {
            if (project.conductsPreSampling()) project.setHasSchedule(false);
            project.setConductPreSampling(false);
            project.setPhase(Project.PHASE_ACTUAL);
        }
        else if (rbConductPreSampling.isChecked()) {
            if (!project.conductsPreSampling()) project.setHasSchedule(false);
            project.setConductPreSampling(true);
            project.setPhase(Project.PHASE_PRESAMPLING);
        }
    }

    private void showAlertDialog(String title, String message){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CreateProject.this);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.create().show();
    }

    public void closeKeyboardOnClick(View view){
        closeKeyboard();
    }

    private void closeKeyboard(){
//        Toast.makeText(this, "closeKeyboard() method accessed", Toast.LENGTH_SHORT).show();

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
    }
}
