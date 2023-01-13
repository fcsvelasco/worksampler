package com.example.marasigan.worksampler.entities;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Project implements Parcelable, Serializable {
//    public static Calendar artificialNow = Calendar.getInstance
    public static final int MAX_NOTIFICATIONS = 3;

    public final static int PHASE_PRESAMPLING = 0;
    public final static int PHASE_ACTUAL = 1;
    public final static int PHASE_PROJECT_ENDED = 2;

    public final static byte SCHED_TODAY = 0;
    public final static byte SCHED_NEXT_DAY = 1;

    public final static int MODE_DEFAULT = 0;
    public final static int MODE_SELF_WS = 1;
    public final static int MODE_RATED_WS = 2;

    public final static String FILE_NAME = "project.txt";

    public final static float Z_1POINT645 = 1.645f;
    public final static float Z_1POINT96 = 1.96f;
    public final static float Z_2POINT58 = 2.58f;

    public static final String TAG = "Project";
    public static final String MODE_TAG = "Project Mode";
    public static final String SAMPLE_DURATION_TAG = "Get Sample Duration";
    public static final String Z_VALUE_TAG = "Z-value";
    public static final String IS_FROM_PENDING_INTENT = "from pending intent";

    public static final int MAX_LENGTH_NAME = 30;

    private String name;
    private Calendar preSamplingStartDate, preSamplingEndDate, actualStartDate, actualEndDate, samplingDay;
    private ArrayList<StudyObject> operators, machines, studyObjects;
    private float zValue;
    private int noOfPreSamples, lastCheckedSample;
    private boolean conductPreSampling, hasSchedule, isNew, hasNightShift, hasExportedResults, studyObjectHasJustEndedSampling;
    private int phase, mode;
    private Sample lastSampleForTheDay;
    private ArrayList<Sample> samplesForTheDay;
    private ArrayList<String> samplesListFiles;

    public Project(int mode) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0);
        this.actualStartDate = calendar;
        this.actualEndDate = calendar;
        this.preSamplingEndDate = calendar;
        this.preSamplingStartDate = calendar;
        this.samplingDay = Calendar.getInstance();
        this.studyObjects = new ArrayList<>();
        this.samplesForTheDay = new ArrayList<>();
        this.hasSchedule = false;
        this.isNew = true;
        this.hasNightShift = false;
        this.hasExportedResults = false;
        this.samplesListFiles = new ArrayList<>();
        this.mode = mode;
    }

    protected Project(Parcel in) {
        studyObjects = new ArrayList<>();
        samplesForTheDay = new ArrayList<>();
        samplesListFiles = new ArrayList<>();

        name = in.readString();
        in.readTypedList(studyObjects, StudyObject.CREATOR);
        zValue = in.readFloat();
        preSamplingStartDate = (Calendar) in.readSerializable();
        preSamplingEndDate = (Calendar) in.readSerializable();
        actualStartDate = (Calendar) in.readSerializable();
        actualEndDate = (Calendar) in.readSerializable();
        conductPreSampling = in.readByte() != 0;
        hasSchedule = in.readByte() != 0;
        isNew = in.readByte() != 0;
        hasNightShift = in.readByte() != 0;
        hasExportedResults = in.readByte() != 0;
        noOfPreSamples = in.readInt();
        phase = in.readInt();
        in.readTypedList(samplesForTheDay, Sample.CREATOR);
        lastSampleForTheDay = in.readParcelable(Sample.class.getClassLoader());
        samplingDay = (Calendar) in.readSerializable();
        lastCheckedSample = in.readInt();
        in.readStringList(samplesListFiles);
        mode = in.readInt();
        studyObjectHasJustEndedSampling = in.readByte() != 0;
    }

    public static final Creator<Project> CREATOR = new Creator<Project>() {
        @Override
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }

        @Override
        public Project[] newArray(int size) {
            return new Project[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeTypedList(studyObjects);
        parcel.writeFloat(zValue);
        parcel.writeSerializable(preSamplingStartDate);
        parcel.writeSerializable(preSamplingEndDate);
        parcel.writeSerializable(actualStartDate);
        parcel.writeSerializable(actualEndDate);
        parcel.writeByte((byte) (conductPreSampling ? 1:0));
        parcel.writeByte((byte) (hasSchedule ? 1:0));
        parcel.writeByte((byte) (isNew ? 1:0));
        parcel.writeByte((byte) (hasNightShift ? 1:0));
        parcel.writeByte((byte) (hasExportedResults ? 1:0));
        parcel.writeInt(noOfPreSamples);
        parcel.writeInt(phase);
        parcel.writeTypedList(samplesForTheDay);
        parcel.writeParcelable(lastSampleForTheDay,0);
        parcel.writeSerializable(samplingDay);
        parcel.writeInt(lastCheckedSample);
        parcel.writeStringList(samplesListFiles);
        parcel.writeInt(mode);
        parcel.writeByte((byte) (studyObjectHasJustEndedSampling ? 1:0));
    }

    public ArrayList<StudyObject> getStudyObjects() {
        return studyObjects;
    }

    public void addStudyObject(StudyObject studyObject){
        studyObjects.add(studyObject);
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public float getzValue() {
        return zValue;
    }

    public void setzValue(float zValue) {
        this.zValue = zValue;
    }

    public Calendar getPreSamplingStartDate() {
        return preSamplingStartDate;
    }

    public void setPreSamplingStartDate(Calendar preSamplingStartDate) {
        this.preSamplingStartDate = preSamplingStartDate;
        for(StudyObject studyObject: studyObjects) studyObject.setPreSamplingStartDate(preSamplingStartDate);
    }

    public Calendar getPreSamplingEndDate() {
        return preSamplingEndDate;
    }

    public void setPreSamplingEndDate(Calendar preSamplingEndDate) {
        this.preSamplingEndDate = preSamplingEndDate;
        for(StudyObject studyObject: studyObjects) studyObject.setPreSamplingEndDate(preSamplingEndDate);
    }

    public Calendar getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(Calendar actualStartDate) {
        this.actualStartDate = actualStartDate;
        for(StudyObject studyObject: studyObjects) studyObject.setActualStartDate(actualStartDate);
    }

    public Calendar getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(Calendar actualEndDate) {
        this.actualEndDate = actualEndDate;
        for (StudyObject studyObject: studyObjects) studyObject.setActualEndDate(actualEndDate);
    }

    public boolean conductsPreSampling(){
        return this.conductPreSampling;
    }

    public void setConductPreSampling(boolean val){
        this.conductPreSampling = val;
        if (val) phase = PHASE_PRESAMPLING;
        if (!val) phase = PHASE_ACTUAL;
    }

    public int getNoOfOperators(){
        int n=0;
        for (StudyObject studyObject : studyObjects){
            if (studyObject.getType().equals(StudyObject.OPERATOR)) n++;
        }
        return n;
    }

    public int getNoOfMachines(){
        int n=0;
        for (StudyObject studyObject : studyObjects){
            if (studyObject.getType().equals(StudyObject.MACHINE)) n++;
        }
        return n;
    }

    public boolean hasSchedule() {
        return hasSchedule;
    }

    public void setHasSchedule(boolean hasSchedule) {
        this.hasSchedule = hasSchedule;
    }

    public int getNoOfPreSamples() {
        return noOfPreSamples;
    }

    public void setNoOfPreSamples(int noOfPreSamples) {
        this.noOfPreSamples = noOfPreSamples;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public void setPhase(int phase){
        this.phase = phase;
    }

    public int getPhase(){
        return phase;
    }

    public void save(Context context){
        try {
            FileOutputStream fos = context.openFileOutput(Project.FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(this);
            os.close();
            fos.close();
//            Toast.makeText(context, "Project saved!", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delete(Context context){
        File file = context.getFileStreamPath(Project.FILE_NAME);
        boolean deleted = file.delete();
    }

    public static Calendar artificialNow(){
        Calendar artificalNow = Calendar.getInstance();
//        artificalNow.set(Calendar.MONTH, Calendar.OCTOBER);
//        artificalNow.set(Calendar.DAY_OF_MONTH, 10);
//        artificalNow.set(Calendar.HOUR_OF_DAY, 22);
//        artificalNow.set(Calendar.MINUTE,30);
        return artificalNow;
    }

    public ArrayList<Sample> getNextSamples(){
        ArrayList<Sample> samplesList = new ArrayList<>();

        for (int i=0; i<samplesForTheDay.size(); i++){
            if (samplesForTheDay.get(i).getCalendarDate().after(Project.artificialNow())){
                for (int j=0; j<MAX_NOTIFICATIONS; j++) {
                    samplesList.add(samplesForTheDay.get(i+j));
                    if (samplesForTheDay.get(i+j).isLastSampleForToday()) break;
                }

                break;
            }
        }
        return samplesList;
    }

//    public void onPreSamplingFinished(){
////        generateActualSchedForNextDay();
//
//    }

    public void generateSchedule(byte when, int phase){
        samplesForTheDay.clear();

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        Calendar breakTimeStart = Calendar.getInstance();
        Calendar breakTimeEnd = Calendar.getInstance();

        for (StudyObject studyObject: studyObjects){
            if (studyObject.hasReachedRequiredSamples()) continue;

            studyObject.getSamples().clear();
            int noOfSamplingDays;

            if (when == SCHED_TODAY) {
                if (!studyObject.samplingDateContains(artificialNow())) continue;
                else noOfSamplingDays = studyObject.getNoOfRemainingSamplingDates(phase) + 1;
            }else {
                if (!studyObject.samplingDateContains(getNextSamplingDate())) continue;
                else noOfSamplingDays = studyObject.getNoOfRemainingSamplingDates(phase);
            }

            int samplesPerDay = 0;
            if (phase == PHASE_PRESAMPLING) {
                samplesPerDay = noOfPreSamples/studyObject.getPreSamplingDates().size();
                if (noOfPreSamples%noOfSamplingDays != 0) samplesPerDay++;
            }
            if (phase == PHASE_ACTUAL){
                studyObject.computeNoOfSamplesRequired(zValue);
                if (studyObject.hasReachedRequiredSamples()) {
                    studyObjectHasJustEndedSampling = true;
                    continue;
                }
                samplesPerDay = studyObject.getNoOfSamplesLeft()/noOfSamplingDays;
                if (studyObject.getNoOfSamplesLeft()%noOfSamplingDays != 0) samplesPerDay++;
            }

            start.set(Calendar.HOUR_OF_DAY, studyObject.getStartOfWorkHour());
            start.set(Calendar.MINUTE, studyObject.getStartOfWorkMinute());
            end.set(Calendar.HOUR_OF_DAY, studyObject.getEndOfWorkHour());
            end.set(Calendar.MINUTE, studyObject.getEndOfWorkMinute());
            breakTimeStart.set(Calendar.HOUR_OF_DAY, studyObject.getStartOfBreakTimeHour());
            breakTimeStart.set(Calendar.MINUTE, studyObject.getStartOfBreakTimeMinute());
            breakTimeEnd.set(Calendar.HOUR_OF_DAY, studyObject.getEndOfBreakTimeHour());
            breakTimeEnd.set(Calendar.MINUTE, studyObject.getEndOfBreakTimeMinute());

            Calendar samplingDay;
            Calendar samplingDay2;
            if (when == SCHED_TODAY){
                samplingDay = Project.artificialNow();
                samplingDay2 = Project.artificialNow();
            }
            else {
//                samplingDay = studyObject.getNextSamplingDate(phase);
//                samplingDay2 = studyObject.getNextSamplingDate(phase);
                samplingDay = getNextSamplingDate();
                samplingDay2 = getNextSamplingDate();
            }
            start.set(Calendar.YEAR, samplingDay.get(Calendar.YEAR));
            start.set(Calendar.DAY_OF_YEAR, samplingDay.get(Calendar.DAY_OF_YEAR));

            if (studyObject.getShift() == StudyObject.MORNING_SHIFT) {
                end.set(Calendar.YEAR, samplingDay.get(Calendar.YEAR));
                end.set(Calendar.DAY_OF_YEAR, samplingDay.get(Calendar.DAY_OF_YEAR));
                breakTimeStart.set(Calendar.YEAR, samplingDay.get(Calendar.YEAR));
                breakTimeStart.set(Calendar.DAY_OF_YEAR, samplingDay.get(Calendar.DAY_OF_YEAR));
                breakTimeEnd.set(Calendar.YEAR, samplingDay.get(Calendar.YEAR));
                breakTimeEnd.set(Calendar.DAY_OF_YEAR, samplingDay.get(Calendar.DAY_OF_YEAR));
            }else{
                samplingDay2.add(Calendar.DAY_OF_YEAR,1);
                end.set(Calendar.YEAR, samplingDay2.get(Calendar.YEAR));
                end.set(Calendar.DAY_OF_YEAR, samplingDay2.get(Calendar.DAY_OF_YEAR));

                if (60*breakTimeStart.get(Calendar.HOUR_OF_DAY) + breakTimeStart.get(Calendar.MINUTE)
                        < 60*start.get(Calendar.HOUR_OF_DAY) + start.get(Calendar.MINUTE)){
                    breakTimeStart.set(Calendar.YEAR, samplingDay2.get(Calendar.YEAR));
                    breakTimeStart.set(Calendar.DAY_OF_YEAR, samplingDay2.get(Calendar.DAY_OF_YEAR));
                    breakTimeEnd.set(Calendar.YEAR, samplingDay2.get(Calendar.YEAR));
                    breakTimeEnd.set(Calendar.DAY_OF_YEAR, samplingDay2.get(Calendar.DAY_OF_YEAR));
                }else{
                    breakTimeStart.set(Calendar.YEAR, samplingDay.get(Calendar.YEAR));
                    breakTimeStart.set(Calendar.DAY_OF_YEAR, samplingDay.get(Calendar.DAY_OF_YEAR));
                    if (60*breakTimeEnd.get(Calendar.HOUR_OF_DAY) + breakTimeEnd.get(Calendar.MINUTE)
                            < 60*start.get(Calendar.HOUR_OF_DAY) + start.get(Calendar.MINUTE)){
                        breakTimeEnd.set(Calendar.YEAR, samplingDay2.get(Calendar.YEAR));
                        breakTimeEnd.set(Calendar.DAY_OF_YEAR, samplingDay2.get(Calendar.DAY_OF_YEAR));
                    }else {
                        breakTimeEnd.set(Calendar.YEAR, samplingDay.get(Calendar.YEAR));
                        breakTimeEnd.set(Calendar.DAY_OF_YEAR, samplingDay.get(Calendar.DAY_OF_YEAR));
                    }
                }
            }

            for (int i = 0; i < samplesPerDay; i++) {
                Sample sample;
                if(studyObject.hasBreakTime()) {
                    sample = new Sample(start, end, breakTimeStart, breakTimeEnd);
                }else {
                    sample = new Sample(start, end);
                }
                if (studyObject.getSamples().size() > 0){
                    while (sampleHasSameTime(sample, studyObject)){
                        if(studyObject.hasBreakTime()) {
                            sample = new Sample(start, end, breakTimeStart, breakTimeEnd);
                        }else {
                            sample = new Sample(start, end);
                        }
                    }
                }
                sample.setStudyObjectName(studyObject.getName());
                studyObject.addSample(sample);
//                samplesForTheDay.add(sample);
            }
        }

//        for (StudyObject operator : operators) operator.sortSamples();
//        for (StudyObject machine : machines) machine.sortSamples();
        for (StudyObject studyObject : studyObjects) {
            if (studyObject.hasReachedRequiredSamples()) continue;
            studyObject.sortSamples();
            samplesForTheDay.addAll(studyObject.getSamples());
        }

        if (hasReachedRequiredSamples()) return;

        for (int i = samplesForTheDay.size() - 1; i >= 0; i--){
            for (int j = 1; j <= i; j++){
                if (samplesForTheDay.get(j-1).getCalendarDate()
                        .after(samplesForTheDay.get(j).getCalendarDate())){
                    Sample sampleTemp = samplesForTheDay.get(j-1);
                    samplesForTheDay.set(j-1, samplesForTheDay.get(j));
                    samplesForTheDay.set(j, sampleTemp);
                }
            }
        }

        if (samplesForTheDay.size() > 0) {
            samplesForTheDay.get(samplesForTheDay.size() - 1).setLastSampleForToday(true);
            lastSampleForTheDay = samplesForTheDay.get(samplesForTheDay.size() - 1);
            if (when == SCHED_TODAY){
                samplingDay.set(Calendar.YEAR, artificialNow().get(Calendar.YEAR));
                samplingDay.set(Calendar.DAY_OF_YEAR, artificialNow().get(Calendar.DAY_OF_YEAR));
            }else {
                samplingDay.set(Calendar.YEAR, getNextSamplingDate().get(Calendar.YEAR));
                samplingDay.set(Calendar.DAY_OF_YEAR, getNextSamplingDate().get(Calendar.DAY_OF_YEAR));
            }
        }
        hasSchedule = true;
    }

    public boolean hasReachedRequiredSamples(){
        for (StudyObject object : studyObjects){
            if (!object.hasReachedRequiredSamples()) return false;
        }

        return true;
    }

    public ArrayList<Sample> getSamplesForTheDay() {
        return samplesForTheDay;
    }

    public void setSamplesForTheDay(){
        samplesForTheDay = new ArrayList<>();

        for (StudyObject studyObject : studyObjects) samplesForTheDay.addAll(studyObject.getSamples());

        for (int i = samplesForTheDay.size() - 1; i >= 0; i--){
            for (int j = 1; j <= i; j++){
                if (samplesForTheDay.get(j-1).getCalendarDate()
                        .after(samplesForTheDay.get(j).getCalendarDate())){
                    Sample sampleTemp = samplesForTheDay.get(j-1);
                    samplesForTheDay.set(j-1, samplesForTheDay.get(j));
                    samplesForTheDay.set(j, sampleTemp);
                }
            }
        }
    }

    public boolean todayIsASamplingDay(){
        for (StudyObject studyObject: this.getStudyObjects()){
            if (studyObject.getPreSamplingDates().size() > 0){
                for (Calendar samplingDate : studyObject.getPreSamplingDates()){
                    if (Project.artificialNow().get(Calendar.DAY_OF_YEAR) == samplingDate.get(Calendar.DAY_OF_YEAR))
                        return true;
                }
            }

            for (Calendar samplingDate: studyObject.getActualSamplingDates()){
                if (Project.artificialNow().get(Calendar.DAY_OF_YEAR) == samplingDate.get(Calendar.DAY_OF_YEAR))
                    return true;
            }
        }

        return false;
    }

    public boolean sampleHasSameTime(Sample sample, StudyObject studyObject){
        for (Sample existingSample : studyObject.getSamples()){
            if (sample.getCalendarDate().get(Calendar.HOUR_OF_DAY) == existingSample.getCalendarDate().get(Calendar.HOUR_OF_DAY)
                    && sample.getCalendarDate().get(Calendar.MINUTE) == existingSample.getCalendarDate().get(Calendar.MINUTE))
                return true;
        }
        return false;
    }

    public void onDayEnded(Context context, Calendar date, byte schedWhen){
//        computeNoOfSamplesForAll();
        saveSamplesList(context,date);
        generateSchedule(schedWhen,this.phase);
        save(context);
    }

    public void onProjectEnded(Context context, Calendar date){
        saveSamplesList(context,date);
        samplesForTheDay.clear();
        for (StudyObject studyObject : studyObjects) studyObject.getSamples().clear();

        phase = PHASE_PROJECT_ENDED;
        calculateResults();
        save(context);
    }

    private void saveSamplesList(Context context, Calendar date){
        try {
            FileOutputStream fos = context.openFileOutput(samplesListFileName(date), Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(samplesForTheDay);
            os.close();
            fos.close();
            samplesListFiles.add(samplesListFileName(date));
            Toast.makeText(context, "Project saved!", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(context, "Samples list saved!", Toast.LENGTH_SHORT).show();
    }

    public static String samplesListFileName(Calendar date){
        int dayOfYear = date.get(Calendar.DAY_OF_YEAR);
        int year = date.get(Calendar.YEAR);
        return "project_samples_list_" + dayOfYear + "_" + year + ".txt";
    }

    public Calendar getLastDayOfPreSampling(){
        Calendar lastDayOfPresampling = Calendar.getInstance();

        lastDayOfPresampling.set(Calendar.YEAR,
                studyObjects.get(0).getPreSamplingDates().get(studyObjects.get(0).getPreSamplingDates().size()-1).get(Calendar.YEAR));
        lastDayOfPresampling.set(Calendar.DAY_OF_YEAR,
                studyObjects.get(0).getPreSamplingDates().get(studyObjects.get(0).getPreSamplingDates().size()-1).get(Calendar.DAY_OF_YEAR));

        for (int i=1; i<studyObjects.size(); i++){
            Calendar date = studyObjects.get(i).getPreSamplingDates().get(studyObjects.get(i).getPreSamplingDates().size()-1);
            if (lastDayOfPresampling.before(date)) {
                lastDayOfPresampling.set(Calendar.YEAR, date.get(Calendar.YEAR));
                lastDayOfPresampling.set(Calendar.DAY_OF_YEAR, date.get(Calendar.DAY_OF_YEAR));
            }
        }

        return lastDayOfPresampling;
    }

    public Calendar getLastDayOfActualSampling(){
        Calendar lastDayOfActualSampling = Calendar.getInstance();
        lastDayOfActualSampling.set(Calendar.YEAR,
                studyObjects.get(0).getActualSamplingDates().get(studyObjects.get(0).getActualSamplingDates().size()-1).get(Calendar.YEAR));
        lastDayOfActualSampling.set(Calendar.DAY_OF_YEAR,
                studyObjects.get(0).getActualSamplingDates().get(studyObjects.get(0).getActualSamplingDates().size()-1).get(Calendar.DAY_OF_YEAR));

        for (int i=1; i<studyObjects.size(); i++){
            Calendar date = studyObjects.get(i).getActualSamplingDates().get(studyObjects.get(i).getActualSamplingDates().size()-1);
            if (lastDayOfActualSampling.before(date)){
                lastDayOfActualSampling.set(Calendar.YEAR, date.get(Calendar.YEAR));
                lastDayOfActualSampling.set(Calendar.DAY_OF_YEAR, date.get(Calendar.DAY_OF_YEAR));
            }
        }

        return lastDayOfActualSampling;
    }

    public boolean hasNightShift() {
        return hasNightShift;
    }

    public void setHasNightShift(boolean hasNightShift) {
        this.hasNightShift = hasNightShift;
    }



    public Sample getLastSampleForTheDay() {
        return lastSampleForTheDay;
    }

    private Calendar getNextSamplingDate() {
        Calendar nextSamplingDate = Calendar.getInstance();

        nextSamplingDate.set(Calendar.YEAR, studyObjects.get(0).getNextSamplingDate(phase).get(Calendar.YEAR));
        nextSamplingDate.set(Calendar.DAY_OF_YEAR, studyObjects.get(0).getNextSamplingDate(phase).get(Calendar.DAY_OF_YEAR));

        for (int i=1; i<studyObjects.size(); i++){
            if (nextSamplingDate.after(studyObjects.get(i).getNextSamplingDate(phase))) {
                nextSamplingDate.set(Calendar.YEAR, studyObjects.get(i).getNextSamplingDate(phase).get(Calendar.YEAR));
                nextSamplingDate.set(Calendar.DAY_OF_YEAR, studyObjects.get(i).getNextSamplingDate(phase).get(Calendar.DAY_OF_YEAR));
            }
        }
        return nextSamplingDate;
    }

    public Calendar getSamplingDay() {
        return samplingDay;
    }

    public void calculateResults(){
        for (StudyObject object : studyObjects) object.calculateResults(zValue);
    }

    public String getConfidenceLevel(){
        if (zValue == Z_1POINT645) return "90%";
        if (zValue == Z_1POINT96) return "95%";
        if (zValue == Z_2POINT58) return "99%";
        return null;
    }

    public int getLastCheckedSample() {
        return lastCheckedSample;
    }

    public void setLastCheckedSample(int lastCheckedSample) {
        this.lastCheckedSample = lastCheckedSample;
    }

    public static String getStringCalendar(Calendar date) {
        return date.getDisplayName(Calendar.MONTH,Calendar.LONG,Locale.getDefault()) + " "
                + date.get(Calendar.DAY_OF_MONTH) + ", " + date.get(Calendar.YEAR);
    }

    public static String getStringTime(int hour, int minute){
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY,hour);
        time.set(Calendar.MINUTE,minute);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return simpleDateFormat.format(time.getTime());
    }

    public StudyObject getStudyObject(int index, String type){
        int count = 0;

        for (StudyObject object : studyObjects){
            if (object.getType().equals(type)){
                if (count == index) return object;
                count++;
            }
        }

        return null;
    }

    public ArrayList<String> getSamplesListFiles() {
        return samplesListFiles;
    }

    public void addSamplesListFile(String fileName){
        samplesListFiles.add(fileName);
    }

    public boolean hasExportedResults() {
        return hasExportedResults;
    }

    public void setHasExportedResults(boolean hasExportedResults) {
        this.hasExportedResults = hasExportedResults;
    }

    public int getMode() {
        return mode;
    }

    public String getStringMode(){
        if (mode == MODE_DEFAULT) return "Default mode";
        if (mode == MODE_RATED_WS) return  "Rated work sampling";
        if (mode == MODE_SELF_WS) return "Self-reported work sampling";

        return null;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getSampleDuration(){
        if(mode == MODE_RATED_WS) return 45;
        else return 30;
    }

    public StudyObject getSelfOperator() {
        return studyObjects.get(0);
    }

    public void setStudyObjectHasJustEndedSampling(boolean val){
        studyObjectHasJustEndedSampling = val;
    }
    public boolean studyObjectHasJustEndedSampling() {
        return studyObjectHasJustEndedSampling;
    }
}
