package com.example.marasigan.worksampler;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.content.Context;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.content.res.Resources.Theme;

import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.example.marasigan.worksampler.entities.ObjectActivity;
import com.example.marasigan.worksampler.entities.Project;
import com.example.marasigan.worksampler.entities.ProjectDebugMode;
import com.example.marasigan.worksampler.entities.Sample;
import com.example.marasigan.worksampler.entities.StudyObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import jxl.Workbook;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class Results extends AppCompatActivity {
    public static Project sProject;

    private Project project;
    private Toolbar titleBar, tabBar;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;
    private Spinner spinner;
    private int tabCount;
    private AlertDialog progressDialog;
    private boolean exportSuccess;
    private String xlsFileName, folderName;
    private File root, dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        project = sProject;
//        project = getIntent().getParcelableExtra(Project.TAG);
        tabCount = project.getStudyObjects().size();

        titleBar = (Toolbar) findViewById(R.id.results_title_bar);
        titleBar.setTitle(project.getName());
        setSupportActionBar(titleBar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        tabBar = findViewById(R.id.results_tab_bar);
        if (project.getConfidenceLevel() != null) {
            tabBar.setSubtitle("At " + project.getConfidenceLevel() + " confidence level");
            tabBar.setTitle("Showing results");
        }
        setUpSpinner();
        setUpViewPager();

        Toast.makeText(this, "No of samples files: " + project.getSamplesListFiles().size(), Toast.LENGTH_SHORT).show();

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

    }

    private void setUpSpinner(){
        spinner = (Spinner) findViewById(R.id.spinner);
        if(project.getMode() == Project.MODE_SELF_WS){
            spinner.setVisibility(View.GONE);
            tabBar.setTitle("Results");
        }else {
            String[] spinnerTexts = new String[tabCount];
            for (int i = 0; i < project.getStudyObjects().size(); i++)
                spinnerTexts[i] = project.getStudyObjects().get(i).getName();
            spinner.setAdapter(new MyAdapter(tabBar.getContext(), spinnerTexts));

            spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.textWhite));
                    ((TextView) parent.getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                    viewPager.setCurrentItem(position);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }

    private void setUpViewPager(){
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                spinner.setSelection(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_results, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_results_settings) {
//            return true;
//        }

        if (id == R.id.action_results_back_to_home){
            backToHomepage();
            return true;
        }
        if (id == R.id.action_export_results) {
//            exportResults();
            System.out.println("EXPORT RESULTS");
            new ExportResultsAsyncTask(this).execute();
            return true;
        }
        if (id == R.id.action_exit_results){
            exitProject();
            return true;
        }
        if (id == R.id.action_results_help){
            helpMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void backToHomepage(){
        Intent intent = new Intent(Results.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (progressDialog != null){
            if (progressDialog.isShowing()) progressDialog.dismiss();
        }
    }

    private void exitProject(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Project");
        if(project.hasExportedResults())
            builder.setMessage("Are you sure you want to exit?");
        else
            builder.setMessage("Are you sure you want to exit without exporting results? All data will be lost.");
        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                project.delete(Results.this);
                Intent intent = new Intent(Results.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("Cancel",null);
        builder.create().show();
    }



    private static class MyAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;

        public MyAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
//                view = inflater.inflate(R.layout.view_spinner_item,parent,false);
            } else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));

            return view;
        }

        @Override
        public Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter{

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ResultsFragment.newInstance(project.getStudyObjects().get(position), project.getMode(), project.getzValue());
        }

        @Override
        public int getCount() {
            return tabCount;
        }
    }

    /******************************* BELOW ARE THE METHODS FOR EXPORTING RESULTS *******************************/

    private void exportResults(/*String fileName, File directory*/){
        exportSuccess =false;

            if (externalStorageIsWritable()){
                folderName = "/MaroonWorksampler";
                xlsFileName = "/" + project.getName().toLowerCase().replace(" ","-") + ".xls";
                root = Environment.getExternalStorageDirectory();
                dir = new File(root.getAbsolutePath() + folderName);
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, xlsFileName);

                try {
                    System.out.println("Exporting results");
                    file.delete();
                    file.createNewFile();
                    WritableWorkbook workbook = Workbook.createWorkbook(file);
                    setupWorkbook(workbook);
                    workbook.write();
                    workbook.close();
                    project.setHasExportedResults(true);
                    exportSuccess = true;
                } catch (FileNotFoundException e) {
                    Results.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Results.this, "Error: Go to your device's Settings and " +
                                    "enable " + getString(R.string.app_name) + " to write on your device's storage.", Toast.LENGTH_LONG).show();
                        }
                    });
                    e.printStackTrace();
                } catch (IOException e) {
                    Results.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Results.this, "Error: Go to your device's Settings and " +
                                    "enable " + getString(R.string.app_name) + " to write on your device's storage.", Toast.LENGTH_LONG).show();
                        }
                    });
                    e.printStackTrace();
                } catch (WriteException e) {
                    Results.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Results.this, "Error: Go to your device's Settings and " +
                                    "enable " + getString(R.string.app_name) + " to write on your device's storage.", Toast.LENGTH_LONG).show();
                        }
                    });
                    e.printStackTrace();
                }
            }else{
                Results.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Results.this, "External storage not found.",Toast.LENGTH_SHORT).show();
                    }
                });

            }
    }

    private void setupWorkbook(WritableWorkbook workbook){
        int totalSamplesTakenRow = 10;
        int totalSamplesTakenCol =1;
        int utilizationRow = 11;
        int utilizationCol = 1;
        int utilizationErrorCol = 3;
        int allowancesRow = 12;
        int allowancesCol = 1;
        int allowancesErrorCol = 3;
        int zValueCol = 1;
        int zValueRow = 12+ project.getStudyObjects().size();
        int activityNameCol = 0;
        int activityTypeCol = activityNameCol + 1;
        int forUtilizationCol = activityTypeCol + 1;
        int forAllowancesCol = forUtilizationCol + 1;
        int inputErrorCol = forAllowancesCol + 1;
        int inputProportionCol = inputErrorCol + 1;
        int activitySamplesTakenCol = inputProportionCol + 1;
        int activityProportionCol = activitySamplesTakenCol + 1;
        int activityErrorCol = activityProportionCol + 1;
        int unitsProducedCol = activityErrorCol + 1;
        int prodHoursCol = unitsProducedCol + 1;
        int observedTimeCol = prodHoursCol + 1;
        int performanceRatingCol = observedTimeCol + 1;
        int normalTimeCol = performanceRatingCol + 1;
        int activityAllowacesCol = normalTimeCol + 1;
        int standardTimeCol = activityAllowacesCol + 1;
        int activitiesRowStart = 16;


        try {
            WritableSheet[] worksheet = new WritableSheet[project.getStudyObjects().size() + 1];
            worksheet[0] = workbook.createSheet(project.getName(), 0);

            int noOfLabels = 15 + project.getStudyObjects().size();
            Label[] sheet1Cells = new Label[noOfLabels];
            sheet1Cells[0] = new Label(0,0,"Project name:");
            sheet1Cells[1] = new Label(1,0, project.getName());
            sheet1Cells[2] = new Label(0,3,"Preliminary Sampling");
            sheet1Cells[3] = new Label(1,4,"Start Date:");
            sheet1Cells[4] = new Label(2,4,
                    (project.conductsPreSampling() ? Project.getStringCalendar(project.getPreSamplingStartDate()) : "N/A"));
            sheet1Cells[5] = new Label(1,5, "End Date:");
            sheet1Cells[6]= new Label(2,5,
                    (project.conductsPreSampling() ? Project.getStringCalendar(project.getPreSamplingEndDate()) : "N/A"));
            sheet1Cells[7] = new Label(0,6,"Actual Sampling");
            sheet1Cells[8] = new Label(1,7,"Start Date:");
            sheet1Cells[9] = new Label(2,7,Project.getStringCalendar(project.getActualStartDate()));
            sheet1Cells[10] = new Label(1,8,"End Date:");
            sheet1Cells[11] = new Label(2,8,Project.getStringCalendar(project.getActualEndDate()));

            sheet1Cells[12] = new Label(0, 10, "Operators:");
            for (int i=13; i<13 + project.getNoOfOperators(); i++){
                sheet1Cells[i] = new Label(1, 10+i-13,
                        project.getStudyObject(i-13, StudyObject.OPERATOR).getName());
            }

            sheet1Cells[13+project.getNoOfOperators()] = new Label(0,11+project.getNoOfOperators(),"Machines:");
            for (int i=14+project.getNoOfOperators(); i<14+project.getStudyObjects().size(); i++){
                sheet1Cells[i] = new Label(1, 11+i-14,
                        project.getStudyObject(i-14-project.getNoOfOperators(), StudyObject.MACHINE).getName());
            }

            sheet1Cells[14+project.getStudyObjects().size()] = new Label(0, zValueRow, "Z-value");

            for (int i=0; i<noOfLabels; i++) worksheet[0].addCell(sheet1Cells[i]);

            Number zValue = new Number(zValueCol,zValueRow,project.getzValue());
            worksheet[0].addCell(zValue);

            Label labelMode = new Label(0,1,"Mode:");
            Label mode = new Label(1,1, project.getStringMode());
            worksheet[0].addCell(labelMode);
            worksheet[0].addCell(mode);

            for (int i=0; i<project.getStudyObjects().size();i++){
                StudyObject object = project.getStudyObjects().get(i);
                worksheet[i+1] = workbook.createSheet(object.getName(),i+1);

                int n1 = object.getDaysOfWork().size();
                int n2 = object.getActivities().size();
                int labelCount = (project.conductsPreSampling()? 45+n1+n2*4 : 45+n1+n2*3);
                int numberCount = (project.conductsPreSampling()? n2 : n2*2);

                Label[] sheetLabels = new Label[labelCount];
                Number[] sheetNumbers = new Number[numberCount];

                sheetLabels[0] = new Label(0,0,
                        (object.getType().equals(StudyObject.OPERATOR) ? "Operator name:" : "Machine name:"));
                sheetLabels[1] = new Label(1,0, object.getName());
                sheetLabels[2] = new Label(0,2,"Working hours");
                sheetLabels[3] = new Label(1,3,"Start:");
                sheetLabels[4] = new Label(2,3,
                        Project.getStringTime(object.getStartOfWorkHour(), object.getStartOfWorkMinute()));
                sheetLabels[5] = new Label(1,4,"End:");
                sheetLabels[6] = new Label(2,4,
                        Project.getStringTime(object.getEndOfWorkHour(),object.getEndOfWorkMinute()));
                sheetLabels[7] = new Label(0,5,"Break time");
                sheetLabels[8] = new Label(1,6,"Start:");
                sheetLabels[9] = new Label(2,6,
                        Project.getStringTime(object.getStartOfBreakTimeHour(), object.getEndOfBreakTimeMinute()));
                sheetLabels[10] = new Label(1,7,"End");
                sheetLabels[11] = new Label(2,7,
                        Project.getStringTime(object.getEndOfBreakTimeHour(), object.getEndOfBreakTimeMinute()));
                sheetLabels[12] = new Label(0,8,"Working days:");

                for (int j=13;j<13+n1;j++){
                    Calendar dayOfWork = Calendar.getInstance();
                    dayOfWork.set(Calendar.DAY_OF_WEEK,object.getDaysOfWork().get(j-13));
                    sheetLabels[j] = new Label(j-13+1,8,
                            dayOfWork.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.LONG, Locale.getDefault()));
                }

                sheetLabels[13+n1] = new Label(0,totalSamplesTakenRow,"Total number of samples:");
                sheetLabels[14+n1] = new Label(0,utilizationRow, "Utilization:");
                sheetLabels[15+n1] = new Label(2,utilizationRow, "±");
                sheetLabels[16+n1] = new Label(0,allowancesRow,"Allowances:");
                sheetLabels[17+n1] = new Label(2,allowancesRow, "±");


                sheetLabels[18+n1] = new Label(0,activitiesRowStart - 2,"Activities");
                sheetLabels[19+n1] = new Label(activityNameCol,activitiesRowStart - 1,"Name");
                sheetLabels[20+n1] = new Label(activityTypeCol,activitiesRowStart - 1,"Type");
                sheetLabels[21+n1] = new Label(forUtilizationCol, activitiesRowStart - 1, "For utilizaiton");
                sheetLabels[22+n1] = new Label(forAllowancesCol, activitiesRowStart - 1, "For allowances");
                sheetLabels[23+n1] = new Label(inputErrorCol,activitiesRowStart - 1,"Acceptable error (user input)");
                sheetLabels[24+n1] = new Label(inputProportionCol,activitiesRowStart - 1,"Proportion estimate (user input)");
                sheetLabels[25+n1] = new Label(activitySamplesTakenCol,activitiesRowStart - 1,"Samples taken");
                sheetLabels[26+n1] = new Label(activityProportionCol,activitiesRowStart - 1,"Calculated Proportion (%)");
                sheetLabels[27+n1] = new Label(activityErrorCol,activitiesRowStart - 1,"Calculated Error (%)");
                sheetLabels[28+n1] = new Label(unitsProducedCol,activitiesRowStart - 1,"Units produced");
                sheetLabels[29+n1] = new Label(prodHoursCol,activitiesRowStart - 1,"Total number of hours");
                sheetLabels[30+n1] = new Label(observedTimeCol,activitiesRowStart - 1,"Observed time (min/piece)");
                sheetLabels[31+n1] = new Label(performanceRatingCol,activitiesRowStart - 1,"Performance rating");
                sheetLabels[32+n1] = new Label(normalTimeCol,activitiesRowStart - 1,"Normal time (min/piece)");
                sheetLabels[33+n1] = new Label(activityAllowacesCol,activitiesRowStart - 1,"Allowances");
                sheetLabels[34+n1] = new Label(standardTimeCol,activitiesRowStart - 1,"Standard time (min/piece)");

//Writes the name of each activity
                for(int j=0;j<n2;j++)
                    sheetLabels[35+n1+j] = new Label(activityNameCol,activitiesRowStart+j,object.getActivities().get(j).getName());

//Writes the type of each activity
                for(int j=0;j<n2;j++)
                    sheetLabels[35+n1+n2+j] = new Label(activityTypeCol,activitiesRowStart+j,object.getActivities().get(j).getType());

//writes the enterred error for each activity
                for (int j=0;j<n2;j++)
                    sheetNumbers[j] = new Number(inputErrorCol,activitiesRowStart+j,object.getActivities().get(j).getError()*100);

                sheetLabels[35+n1+n2*2] = new Label(0, activitiesRowStart+1+n2,"VA activities proportion(%)");
                sheetLabels[36+n1+n2*2] = new Label(2,activitiesRowStart+1+n2,"±");
                sheetLabels[37+n1+n2*2] = new Label(0, activitiesRowStart+2+n2,"NVAE activities proportion(%)");
                sheetLabels[38+n1+n2*2] = new Label(2,activitiesRowStart+2+n2,"±");
                sheetLabels[39+n1+n2*2] = new Label(0, activitiesRowStart+3+n2,"NVAN activities proportion(%)");
                sheetLabels[40+n1+n2*2] = new Label(2,activitiesRowStart+3+n2,"±");

                sheetLabels[41+n1+n2*2] = new Label(0, activitiesRowStart+5+n2, "Samples");
                sheetLabels[42+n1+n2*2] = new Label(0, activitiesRowStart+6+n2,"Date");
                sheetLabels[43+n1+n2*2] = new Label(1,activitiesRowStart+6+n2,"Time");

                for (int j=0; j<n2+1; j++){
                    if (j==n2){
                        sheetLabels[44+n1+n2*2+j] = new Label(2+j,activitiesRowStart+6+n2,"Others");
                        break;
                    }
                    sheetLabels[44+n1+n2*2+j] = new Label(2+j,activitiesRowStart+6+n2,object.getActivities().get(j).getName());
                }

                for (int j=0;j<n2;j++){
                    if (project.conductsPreSampling())
                        sheetLabels[45+n1+n2*3+j] = new Label(inputProportionCol,activitiesRowStart+j,"N/A");
                    else
                        sheetNumbers[j + n2] = new Number(inputProportionCol,activitiesRowStart+j,object.getActivities().get(j).getEstimatedProportion()*100);
                }

                for (int j=0; j<labelCount; j++) worksheet[i+1].addCell(sheetLabels[j]);
                for (int j=0; j<numberCount; j++) worksheet[i+1].addCell(sheetNumbers[j]);

                for (int j=0; j<object.getActivities().size(); j++){
                    ObjectActivity activity = object.getActivities().get(j);

                    if (activity.isForUtilization())
                        worksheet[i+1].addCell(new Number(forUtilizationCol,activitiesRowStart+j,1));

                    if (activity.isForAllowances())
                        worksheet[i+1].addCell(new Number(forAllowancesCol,activitiesRowStart+j,1));
                }
            }

            int[] totalObjectSampleCount = new int[project.getStudyObjects().size()];

//opens the files of samples list and export the data to the excel sheets:
            for (int file=0; file<project.getSamplesListFiles().size(); file++){
                ArrayList<Sample> samplesList =loadSamplesListFiles(project.getSamplesListFiles().get(file));
                int sampleCount = samplesList.size();

                for (int i=0;i<project.getStudyObjects().size();i++){
                    StudyObject object = project.getStudyObjects().get(i);
                    object.getSamples().clear();

                    for(int j=0; j<sampleCount; j++){
                        if (samplesList.get(j).getStudyObjectName().equals(object.getName()))
                            object.addSample(samplesList.get(j));
                    }

                    int objectSampleCount = object.getSamples().size();
                    int activityCount = object.getActivities().size();
                    int samplesRowStart = activitiesRowStart + 7 + activityCount;

//enters the date of each sample:
                    Label[] sampleDate = new Label[objectSampleCount];
                    for(int j=0; j<objectSampleCount; j++){
                        sampleDate[j] = new Label(0,samplesRowStart+j+totalObjectSampleCount[i],object.getSamples().get(j).getDateStringShort());
                        worksheet[i+1].addCell(sampleDate[j]);
                    }

//enters the time of each sample:
                    for(int j=0; j<objectSampleCount; j++){
                        Label time =  new Label(1,samplesRowStart+j+totalObjectSampleCount[i],object.getSamples().get(j).getStringTime());
                        worksheet[i+1].addCell(time);
                    }

//enters which activity was selected for each sample:
                    for(int j=0; j<objectSampleCount; j++){
                        for(int k=0; k<object.getActivities().size()+1;k++){
                            if (k == object.getActivities().size()){
                                Label label = new Label(k+2,samplesRowStart+j+totalObjectSampleCount[i],object.getSamples().get(j).getRemarks());
                                worksheet[i+1].addCell(label);
                                break;
                            }
//                            if (object.getSamples().get(j) == null) continue;
                            if (object.getSamples().get(j).getActivityName().equals(object.getActivities().get(k).getName())){
                                if (project.getMode() == Project.MODE_RATED_WS &&
                                        (object.getActivities().get(k).getType().equals(ObjectActivity.VA)
                                                || object.getActivities().get(k).getType().equals(ObjectActivity.VA))){
                                    Number number = new Number(k + 2, samplesRowStart + j + totalObjectSampleCount[i],
                                            object.getSamples().get(j).getRating());
                                    worksheet[i + 1].addCell(number);
                                    break;
                                }else {
                                    Number number = new Number(k + 2, samplesRowStart + j + totalObjectSampleCount[i], 1);
                                    worksheet[i + 1].addCell(number);
                                    break;
                                }
                            }
                        }
                    }
                    totalObjectSampleCount[i] += objectSampleCount;
                }
            }

//Adds formulas
            for (int j=0; j<project.getStudyObjects().size(); j++){
                StudyObject object = project.getStudyObjects().get(j);
                int activityCount = object.getActivities().size();
                int samplesRowStart = activitiesRowStart + 7 + activityCount;
                int activitiesRowEnd = activitiesRowStart + activityCount - 1;
                int objectSampleCount = object.getSamples().size();
                int va_nvae_nvan_ProportionCol = 1;
                int va_nvae_nvan_ErrorCol = 3;
                int vaProportionRow = activitiesRowStart + 1 + activityCount;
                int nvaeProportionRow = activitiesRowStart + 2 + activityCount;
                int nvanProportionRow = activitiesRowStart + 3 + activityCount;

//Gets the number samples for each activity
                for (int k=0; k<activityCount; k++){
                    int rowStart = samplesRowStart + 1;
                    int rowEnd = rowStart + totalObjectSampleCount[j];

                    if (project.getMode() == Project.MODE_RATED_WS) {
                        Formula formula1 = new Formula(activitySamplesTakenCol, activitiesRowStart + k,
                                "COUNTA(" + columnAddress(2 + k) + rowStart + ":"
                                        + columnAddress(2 + k) + rowEnd + ")");
                        worksheet[j + 1].addCell(formula1);
                    }else {
                        Formula formula = new Formula(activitySamplesTakenCol, activitiesRowStart + k,
                                "SUM(" + columnAddress(2 + k) + rowStart + ":"
                                        + columnAddress(2 + k) + rowEnd + ")");
                        worksheet[j + 1].addCell(formula);
                    }
                }

//Gets the total number of samples for the operator/machine
                Formula formulaSamplesTaken = new Formula(totalSamplesTakenCol, totalSamplesTakenRow,
                        "SUM("+columnAddress(activitySamplesTakenCol) + (activitiesRowStart+1) + ":"
                                + columnAddress(activitySamplesTakenCol) + (activitiesRowStart+activityCount) + ")");
                worksheet[j+1].addCell(formulaSamplesTaken);

//Gets proportion for each activity
                for (int k=0; k<activityCount; k++){
                    Formula formula = new Formula(activityProportionCol,activitiesRowStart+k,
                            "100*" + columnAddress(activitySamplesTakenCol) + (activitiesRowStart+k+1) + "/"
                                    + columnAddress(totalSamplesTakenCol) + (totalSamplesTakenRow+1));
                    worksheet[j+1].addCell(formula);
                }

//Gets total utilization
                Formula formulaUtilization = new Formula(utilizationCol, utilizationRow,
                        "100*SUMPRODUCT(" + columnAddress(forUtilizationCol) + (activitiesRowStart + 1) + ":"
                                + columnAddress(forUtilizationCol) + (activitiesRowEnd + 1) + "," + columnAddress(activitySamplesTakenCol)
                                + (activitiesRowStart + 1) + ":" + columnAddress(activitySamplesTakenCol) + (activitiesRowEnd + 1) + ")/"
                                + columnAddress(totalSamplesTakenCol) + (totalSamplesTakenRow + 1));
                worksheet[j+1].addCell(formulaUtilization);

//Gets error for utilization
                Formula formulaUtilizationError = new Formula(utilizationErrorCol, utilizationRow,
                        "100*'" + project.getName() + "'!" + columnAddress(zValueCol) + (zValueRow+1) +
                                "*SQRT((" + columnAddress(utilizationCol) + (utilizationRow+1) +"/100)*(1-" +
                                columnAddress(utilizationCol) + (utilizationRow + 1) + "/100)/" +
                                columnAddress(totalSamplesTakenCol) + (totalSamplesTakenRow+1) +")");
                worksheet[j+1].addCell(formulaUtilizationError);

//Gets total allowances
                Formula formulaAllowances = new Formula(allowancesCol, allowancesRow,
                        "100*SUMPRODUCT(" + columnAddress(forAllowancesCol) + (activitiesRowStart + 1) + ":"
                                + columnAddress(forAllowancesCol) + (activitiesRowEnd + 1) + "," + columnAddress(activitySamplesTakenCol)
                                + (activitiesRowStart + 1) + ":" + columnAddress(activitySamplesTakenCol) + (activitiesRowEnd + 1) + ")/"
                                + columnAddress(totalSamplesTakenCol) + (totalSamplesTakenRow + 1));
                worksheet[j+1].addCell(formulaAllowances);

//Gets error for utilization
                Formula formulaAllowancesError = new Formula(allowancesErrorCol, allowancesRow,
                        "100*'" + project.getName() + "'!" + columnAddress(zValueCol) + (zValueRow+1) +
                                "*SQRT((" + columnAddress(allowancesCol) + (allowancesRow+1) +"/100)*(1-" +
                                columnAddress(allowancesCol) + (allowancesRow + 1) + "/100)/" +
                                columnAddress(totalSamplesTakenCol) + (totalSamplesTakenRow+1) +")");
                worksheet[j+1].addCell(formulaAllowancesError);

//Gets error for each activity
                for(int k=0; k<activityCount; k++){
                    Formula formula = new Formula(activityErrorCol, activitiesRowStart+k,
                            "100*'" + project.getName() + "'!" + columnAddress(zValueCol) + (zValueRow+1) +
                                    "*SQRT((" + columnAddress(activityProportionCol) + (activitiesRowStart+k+1) +"/100)*(1-" +
                                    columnAddress(activityProportionCol) + (activitiesRowStart+k+1) + "/100)/" +
                                    columnAddress(totalSamplesTakenCol) + (totalSamplesTakenRow+1) +")");
                    worksheet[j+1].addCell(formula);
                }

//Gets number of units produced for activities
                for(int k=0; k<activityCount; k++){
                    ObjectActivity activity = object.getActivities().get(k);
                    if (activity.getType().equals(ObjectActivity.VA)
                            && activity.hasCalculatedST()) {

                        Number number = new Number(unitsProducedCol, activitiesRowStart + k,
                                object.getActivities().get(k).getOutput());
                        worksheet[j + 1].addCell(number);
                    }
                }

//Gets total number of hours for activity
                for(int k=0; k<activityCount; k++){
                    ObjectActivity activity = object.getActivities().get(k);
                    if (activity.getType().equals(ObjectActivity.VA)
                            && activity.hasCalculatedST()) {

                        Number number = new Number(prodHoursCol, activitiesRowStart + k,
                                object.getActivities().get(k).getProductionHours());
                        worksheet[j + 1].addCell(number);
                    }
                }

//Calculates observed time
                for(int k=0; k<activityCount; k++) {
                    ObjectActivity activity = object.getActivities().get(k);
                    if (activity.getType().equals(ObjectActivity.VA)
                            && activity.hasCalculatedST()) {

                        Formula formula = new Formula(observedTimeCol, activitiesRowStart + k,
                                "(" + columnAddress(prodHoursCol) + (activitiesRowStart + k + 1) + "*60/"
                                        + columnAddress(unitsProducedCol) + (activitiesRowStart + k + 1) + ")*("
                                        + columnAddress(activityProportionCol) + (activitiesRowStart + k + 1) + ")/100");
                        worksheet[j + 1].addCell(formula);
                    }
                }

//Gets performance rating
                for(int k=0; k<activityCount; k++){
                    ObjectActivity activity = object.getActivities().get(k);
                    if (activity.getType().equals(ObjectActivity.VA)) {

                        if (project.getMode() == Project.MODE_RATED_WS){
                            int rowStart = samplesRowStart + 1;
                            int rowEnd = rowStart + totalObjectSampleCount[j];

                            Formula formula = new Formula(performanceRatingCol, activitiesRowStart + k,
                                    "AVERAGE(" + columnAddress(2+k) + rowStart + ":"
                                            + columnAddress(2+k) + rowEnd + ")" );

                            worksheet[j + 1].addCell(formula);
                        }else if(activity.hasCalculatedST()) {
                            Number number = new Number(performanceRatingCol, activitiesRowStart + k,
                                    object.getActivities().get(k).getPerformanceRating());
                            worksheet[j + 1].addCell(number);
                        }
                    }
                }

//Calculates normal time
                for(int k=0; k<activityCount; k++) {
                    ObjectActivity activity = object.getActivities().get(k);
                    if (activity.getType().equals(ObjectActivity.VA)
                            && activity.hasCalculatedST()) {

                        Formula formula = new Formula(normalTimeCol, activitiesRowStart + k,
                                columnAddress(observedTimeCol) + (activitiesRowStart+k+1) + "*("
                                        + columnAddress(performanceRatingCol) + (activitiesRowStart+k+1) + "/100)");
                        worksheet[j + 1].addCell(formula);
                    }
                }

//Gets allowances for each activity
                for(int k=0; k<activityCount; k++) {
                    ObjectActivity activity = object.getActivities().get(k);
                    if (activity.getType().equals(ObjectActivity.VA)
                            && activity.hasCalculatedST()) {

                        if(activity.usedCalculatedAllowances()) {
                            Formula formula = new Formula(activityAllowacesCol, activitiesRowStart + k,
                                    columnAddress(allowancesCol) + (allowancesRow + 1));
                            worksheet[j + 1].addCell(formula);
                        }else{
                            Number number = new Number(activityAllowacesCol, activitiesRowStart + k, activity.getAllowances()*100);
                            worksheet[j + 1].addCell(number);
                        }
                    }
                }

//Calculates standard time
                for(int k=0; k<activityCount; k++) {
                    ObjectActivity activity = object.getActivities().get(k);
                    if (activity.getType().equals(ObjectActivity.VA)
                            && activity.hasCalculatedST()) {

                        Formula formula = new Formula(standardTimeCol, activitiesRowStart + k,
                                columnAddress(normalTimeCol) + (activitiesRowStart+k+1) + "/(1-"
                                        + columnAddress(activityAllowacesCol) + (activitiesRowStart+k+1) + "/100)");
                        worksheet[j + 1].addCell(formula);
                    }
                }

//Calculates VA/NVAE/NVAN proportion
                Formula formulaVAProportion = new Formula(va_nvae_nvan_ProportionCol, vaProportionRow,
                        "100*SUM(" + cellAddresses(object,activitySamplesTakenCol,ObjectActivity.VA) + ")/"
                                + columnAddress(totalSamplesTakenCol) + (totalSamplesTakenRow + 1));
                worksheet[j+1].addCell(formulaVAProportion);

                Formula formulaNVAEProportion = new Formula(va_nvae_nvan_ProportionCol, nvaeProportionRow,
                        "100*SUM(" + cellAddresses(object,activitySamplesTakenCol,ObjectActivity.NVAE) + ")/"
                                + columnAddress(totalSamplesTakenCol) + (totalSamplesTakenRow + 1));
                worksheet[j+1].addCell(formulaNVAEProportion);

                Formula formulaNVANProportion = new Formula(va_nvae_nvan_ProportionCol, nvanProportionRow,
                        "100*SUM(" + cellAddresses(object,activitySamplesTakenCol,ObjectActivity.NVAN) + ")/"
                                + columnAddress(totalSamplesTakenCol) + (totalSamplesTakenRow + 1));
                worksheet[j+1].addCell(formulaNVANProportion);

//Calculates VA/NVAE/NVAN error
                Formula formulaVAError = new Formula(va_nvae_nvan_ErrorCol, vaProportionRow,
                        "100*'" + project.getName() + "'!" + columnAddress(zValueCol) + (zValueRow+1) +
                                "*SQRT((" + columnAddress(va_nvae_nvan_ProportionCol) + (vaProportionRow+1) +"/100)*(1-" +
                                columnAddress(va_nvae_nvan_ProportionCol) + (vaProportionRow+1) + "/100)/" +
                                columnAddress(totalSamplesTakenCol) + (totalSamplesTakenRow+1) +")");
                worksheet[j+1].addCell(formulaVAError);

                Formula formulaNVAEError = new Formula(va_nvae_nvan_ErrorCol, nvaeProportionRow,
                        "100*'" + project.getName() + "'!" + columnAddress(zValueCol) + (zValueRow+1) +
                                "*SQRT((" + columnAddress(va_nvae_nvan_ProportionCol) + (nvaeProportionRow+1) +"/100)*(1-" +
                                columnAddress(va_nvae_nvan_ProportionCol) + (nvaeProportionRow+1) + "/100)/" +
                                columnAddress(totalSamplesTakenCol) + (totalSamplesTakenRow+1) +")");
                worksheet[j+1].addCell(formulaNVAEError);

                Formula formulaNVANError = new Formula(va_nvae_nvan_ErrorCol, nvanProportionRow,
                        "100*'" + project.getName() + "'!" + columnAddress(zValueCol) + (zValueRow+1) +
                                "*SQRT((" + columnAddress(va_nvae_nvan_ProportionCol) + (nvanProportionRow+1) +"/100)*(1-" +
                                columnAddress(va_nvae_nvan_ProportionCol) + (nvanProportionRow+1) + "/100)/" +
                                columnAddress(totalSamplesTakenCol) + (totalSamplesTakenRow+1) +")");
                worksheet[j+1].addCell(formulaNVANError);
            }


        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    private String columnAddress(int columnIndex){
        String[] letter = "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z".split(",");
        StringBuilder address = new StringBuilder();
        int base = 26;
        int index = columnIndex;

        do {
            int remainder = index%base;
            address.insert(0,letter[remainder]);
            index = index/base - 1;
        }while(index>=0);

        return address.toString();
    }

    private String cellAddresses(StudyObject object, int columnIndex, String category){
        int activitiesRowStart = project.getMode() == Project.MODE_RATED_WS ? 17:16;

        StringBuilder builder = new StringBuilder();
        for (int i=0; i<object.getActivities().size(); i++){
            ObjectActivity activity = object.getActivities().get(i);
            if (activity.getType().equals(category)){
                String rowNumber = (activitiesRowStart+i+1) + "";
                builder.append(columnAddress(columnIndex)).append(rowNumber).append(",");
            }
        }

        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }

    private boolean externalStorageIsWritable(){
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private ArrayList<Sample> loadSamplesListFiles(String fileName){
        try {
            FileInputStream fileInputStream = this.openFileInput(fileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            ArrayList<Sample> samplesList = (ArrayList<Sample>) objectInputStream.readObject();
            fileInputStream.close();
            objectInputStream.close();
//            Toast.makeText(this, "Samples list loaded!", Toast.LENGTH_SHORT).show();
            return samplesList;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
    private void testColumnAddress(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        builder.setView(editText);
        builder.setPositiveButton("Column Address", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String address = columnAddress(Integer.parseInt(editText.getText().toString()));
                Toast.makeText(Results.this, "Columnt address: " + address, Toast.LENGTH_SHORT).show();
            }
        });

        builder.create().show();
    }

//    private class ExportResultsAsyncTask extends AsyncTask<Void,Void,Void> {
//
//        String xlsFileName = "/" + project.getName().toLowerCase().replace(" ","-") + ".xls";
////        String xlsFileName = "sample.xls";
//
//        File root = Environment.getExternalStorageDirectory();
//        File dir = new File(root.getAbsolutePath() + "/MaroonWorksampler");
////        String dir = root.getAbsolutePath() + "/WorkSampler";
////        File dir = new File(Environment.getDataDirectory()+"/WorkSampler/");
//
//        @Override
//        protected void onPreExecute() {
//            showProgressBar();
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            exportResults(xlsFileName, dir);
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            progressDialog.dismiss();
//
//            if(exportSuccess)
//                Toast.makeText(Results.this, "Results exported to: " + dir+xlsFileName, Toast.LENGTH_LONG).show();
//            else
//                Toast.makeText(Results.this, "Failed to export results", Toast.LENGTH_LONG).show();
//        }
//
//        private void showProgressBar(){
//            System.out.println("LOADING RESULTS");
//            AlertDialog.Builder progressDialogBuilder = new AlertDialog.Builder(Results.this);
//            progressDialogBuilder.setView(getLayoutInflater().inflate(R.layout.dialog_progress_bar, null));
//            progressDialog = progressDialogBuilder.create();
//
//            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//            progressDialog.setCancelable(false);
//            progressDialog.setCanceledOnTouchOutside(false);
//            progressDialog.show();
//        }
//    }

    private static class ExportResultsAsyncTask extends AsyncTask<Void,Void,Void> {

        private WeakReference<Results> ref;
        Project project;

        ExportResultsAsyncTask(Results context){
            ref = new WeakReference<>(context);
            project = ref.get().project;
        }

        @Override
        protected void onPreExecute() {
            showProgressBar();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ref.get().exportResults();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Results activity = ref.get();
            activity.progressDialog.dismiss();

            if(activity.exportSuccess)
                Toast.makeText(ref.get(), "Results exported to: " + ref.get().folderName+ref.get().xlsFileName, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(ref.get(), "Failed to export results", Toast.LENGTH_LONG).show();
        }

        private void showProgressBar(){
            System.out.println("LOADING RESULTS");
            AlertDialog.Builder progressDialogBuilder = new AlertDialog.Builder(ref.get());
            progressDialogBuilder.setView(ref.get().getLayoutInflater().inflate(R.layout.dialog_progress_bar, null));
            ref.get().progressDialog = progressDialogBuilder.create();

            ref.get().progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            ref.get().progressDialog.setCancelable(false);
            ref.get().progressDialog.setCanceledOnTouchOutside(false);
            ref.get().progressDialog.show();
        }
    }
}
