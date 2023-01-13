package com.example.marasigan.worksampler;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.marasigan.worksampler.entities.ObjectActivity;
import com.example.marasigan.worksampler.entities.Project;
import com.example.marasigan.worksampler.entities.StudyObject;
import com.example.marasigan.worksampler.views.ObjectActivityButton;

import java.util.ArrayList;

public class Operators extends AppCompatActivity {
    public static Project sProject;

    private LinearLayout operatorListLinearLayout, machineListLinearLayout;
    private Project project;
//    private StudyObject operator;
//    private ArrayList<StudyObject> operators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operators);
        setTitle(R.string.create_project_title);
        getSupportActionBar().setSubtitle("Page 2 of 3");

        operatorListLinearLayout = findViewById(R.id.linear_layout_operators);
        machineListLinearLayout = findViewById(R.id.linear_layout_machines);

        Intent intent = getIntent();
//        project = intent.getParcelableExtra(Project.TAG);
        project = sProject;

//        createOperatorButtons();
//        createMachineButtons();
        createObjectButtons();
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
                Intent intent = new Intent(Operators.this,MainActivity.class);
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

    public void btnAddOperatorOnClick(View view){
        addOperator();
    }

    public void btnAddMachineOnClick(View view){
        addMachine();
    }

    public void btnNextOnClick(View view){
        if (project.getStudyObjects().isEmpty()){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Add operator/machines");
            alertDialog.setMessage("Please add operator/machine before proceeding");
            alertDialog.create().show();
            return;
        }

        for (StudyObject object : project.getStudyObjects()){
            if (object.getShift() == StudyObject.NIGHT_SHIFT){
                project.setHasNightShift(true);
                break;
            }
        }

        if (!project.conductsPreSampling()){
            boolean hasZeroProp = false;
            StringBuilder activitiesWithZeroPropEst = new StringBuilder();
            activitiesWithZeroPropEst.append("The following activities have proportion estimates equal to 0:").append("\n\n");
            for (StudyObject studyObject : project.getStudyObjects()){
                for (ObjectActivity activity : studyObject.getActivities()){
                    if (activity.getEstimatedProportion() == 0){
                        activitiesWithZeroPropEst.append(activity.getName()).append(" (").append(studyObject.getName()).append(")\n");
                        hasZeroProp = true;
                    }
                }
            }
            activitiesWithZeroPropEst.append("\nPlease enter proportion estimates for these activities. " +
                    "If there are no data for proportion estimates, kindly conduct preliminary sampling.");

            if (hasZeroProp){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Proportion estimates");
                builder.setMessage(activitiesWithZeroPropEst.toString());
                builder.create().show();
                return;
            }
        }

        ProjectSchedule.sProject = project;
        Intent intent = new Intent(this, ProjectSchedule.class);
//        intent.putExtra(Project.TAG,(Parcelable)project);
        startActivity(intent);
    }

    public void btnPreviousOnClick(View view){
        CreateProject.sProject = project;
        Intent intent = new Intent(Operators.this, CreateProject.class);
//        intent.putExtra(Project.TAG,(Parcelable)project);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void addOperator(){
        OperatorDetails.sProject = project;
        Intent intent = new Intent(this, OperatorDetails.class);
//        intent.putExtra(Project.TAG,(Parcelable) project);
        intent.putExtra("object type",StudyObject.OPERATOR);
        startActivity(intent);

        finish();
    }

    public void addMachine(){
        OperatorDetails.sProject = project;
        Intent intent = new Intent(this, OperatorDetails.class);
//        intent.putExtra(Project.TAG,(Parcelable) project);
        intent.putExtra("object type",StudyObject.MACHINE);
        startActivity(intent);

        finish();
    }

    private void createObjectButtons(){
        for(StudyObject studyObject : project.getStudyObjects()){
            final ObjectActivityButton btnObject = (ObjectActivityButton) getLayoutInflater().inflate(R.layout.view_operator_activity_button, null);
            btnObject.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            btnObject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editOperatorOnClick(btnObject);
                }
            });

            btnObject.setStudyObject(studyObject);
            btnObject.setText(studyObject.getName());
            if (studyObject.getType().equals(StudyObject.OPERATOR)) operatorListLinearLayout.addView(btnObject);
            if (studyObject.getType().equals(StudyObject.MACHINE)) machineListLinearLayout.addView(btnObject);
        }
    }

    public void editOperatorOnClick(View view){

        final ObjectActivityButton btnStudyObject = (ObjectActivityButton) view;
        final String type = btnStudyObject.getStudyObject().getType();

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Operators.this);
        if (type.equals(StudyObject.OPERATOR)) alertDialogBuilder.setMessage("Operator: " + btnStudyObject.getStudyObject().getName());
        if (type.equals(StudyObject.MACHINE)) alertDialogBuilder.setMessage("Machine: " + btnStudyObject.getStudyObject().getName());

        alertDialogBuilder.setPositiveButton("Edit", null);
        alertDialogBuilder.setNegativeButton("Delete", null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(Operators.this, "Operator/Machine index: " + btnStudyObject.getStudyObject().getIndex(), Toast.LENGTH_SHORT).show();
                        OperatorDetails.sProject = project;
                        Intent intent = new Intent(Operators.this, OperatorDetails.class);
//                        intent.putExtra(Project.TAG,(Parcelable) project);
                        intent.putExtra("object type",type);
                        intent.putExtra(StudyObject.TAG,(Parcelable)btnStudyObject.getStudyObject());
                        startActivity(intent);
                    }
                });

                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

//                        project.getStudyObjects().remove(btnStudyObject.getStudyObject());
//                        if(type.equals(StudyObject.OPERATOR)) operatorListLinearLayout.removeView(btnStudyObject);
//                        if(type.equals(StudyObject.MACHINE)) machineListLinearLayout.removeView(btnStudyObject);
//                        for(StudyObject studyObject: project.getStudyObjects()){
//                            studyObject.setIndex(project.getStudyObjects().indexOf(studyObject));
//                        }

                        deleteObject(btnStudyObject, alertDialog);
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void deleteObject(final ObjectActivityButton btnStudyObject, final AlertDialog dialog){
//        final StudyObject studyObject = btnStudyObject.getStudyObject();
        final String type = btnStudyObject.getStudyObject().getType();

        AlertDialog.Builder confirmDeleteDialogBuilder = new AlertDialog.Builder(this);
        confirmDeleteDialogBuilder.setTitle("Delete operator/machine");
        confirmDeleteDialogBuilder.setMessage("Are you sure you want to delete " + btnStudyObject.getStudyObject().getName() + " from the list of operators/machines?");
        confirmDeleteDialogBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                project.getStudyObjects().remove(btnStudyObject.getStudyObject());
                if(type.equals(StudyObject.OPERATOR)) operatorListLinearLayout.removeView(btnStudyObject);
                if(type.equals(StudyObject.MACHINE)) machineListLinearLayout.removeView(btnStudyObject);
                for(StudyObject studyObject: project.getStudyObjects()){
                    studyObject.setIndex(project.getStudyObjects().indexOf(studyObject));
                }

                dialog.dismiss();
            }
        });
        confirmDeleteDialogBuilder.setNegativeButton("Cancel", null);
        confirmDeleteDialogBuilder.create().show();
    }
}
