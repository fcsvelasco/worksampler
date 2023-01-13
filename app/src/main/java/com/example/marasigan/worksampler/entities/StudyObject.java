package com.example.marasigan.worksampler.entities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.example.marasigan.worksampler.R;
import com.example.marasigan.worksampler.views.SchedData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class StudyObject implements Parcelable, Serializable {
    public final static String MACHINE = "machine";
    public final static String OPERATOR = "operator";

    public final static byte MORNING_SHIFT = 0;
    public final static byte NIGHT_SHIFT = 1;

    public final static String TAG = "Study Object";

    private String type, name;
    private byte shift;
    private Calendar preSamplingStartDate, preSamplingEndDate, actualStartDate, actualEndDate;
    private int startOfWorkHour, startOfWorkMinute, endOfWorkHour, endOfWorkMinute,
            startOfBreakTimeHour, startOfBreakTimeMinute, endOfBreakTimeHour, endOfBreakTimeMinute,
            index, noOfSamplesRequired, noOfSamplesTaken, lastCheckedSample, remarksCount;
    private float utilization, allowances, utilizationError, allowancesError;
    private boolean workingOnSunday, workingOnMonday, workingOnTuesday, workingOnWednesday,
            workingOnThursday, workingOnFriday, workingOnSaturday, hasReachedRequiredSamples, hasBreakTime;
    private ArrayList<Integer> daysOfWork;
    private ArrayList<ObjectActivity> activities;
    private ArrayList<Sample> samples;
    private ArrayList<Calendar> preSamplingDates, actualSamplingDates;

    public StudyObject(String type) {
        this.type = type;
        this.activities = new ArrayList<>();
        this.samples = new ArrayList<>();
        this.daysOfWork = new ArrayList<>();
        this.preSamplingDates = new ArrayList<>();
        this.actualSamplingDates = new ArrayList<>();

        this.actualStartDate = Calendar.getInstance();
        this.actualEndDate = Calendar.getInstance();
        this.preSamplingEndDate = Calendar.getInstance();
        this.preSamplingStartDate = Calendar.getInstance();

//        this.canGetSample = false;
    }

    protected StudyObject(Parcel in) {
        activities = new ArrayList<>();
        samples = new ArrayList<>();
        daysOfWork = new ArrayList<>();
        preSamplingDates = new ArrayList<>();
        actualSamplingDates = new ArrayList<>();
        actualStartDate = Calendar.getInstance();
        actualEndDate = Calendar.getInstance();
        preSamplingEndDate = Calendar.getInstance();
        preSamplingStartDate = Calendar.getInstance();

        index = in.readInt();
        type = in.readString();
        name = in.readString();
        startOfWorkHour = in.readInt();
        startOfWorkMinute = in.readInt();
        endOfWorkHour = in.readInt();
        endOfWorkMinute = in.readInt();
        startOfBreakTimeHour = in.readInt();
        startOfBreakTimeMinute = in.readInt();
        endOfBreakTimeHour = in.readInt();
        endOfBreakTimeMinute = in.readInt();
        workingOnSunday = in.readByte() != 0;
        workingOnMonday = in.readByte() != 0;
        workingOnTuesday = in.readByte() != 0;
        workingOnWednesday = in.readByte() != 0;
        workingOnThursday = in.readByte() != 0;
        workingOnFriday = in.readByte() != 0;
        workingOnSaturday = in.readByte() != 0;
        in.readTypedList(activities,ObjectActivity.CREATOR);
        in.readTypedList(samples, Sample.CREATOR);
        in.readList(daysOfWork, getClass().getClassLoader());
        in.readList(preSamplingDates, Calendar.class.getClassLoader());
        in.readList(actualSamplingDates, Calendar.class.getClassLoader());
        preSamplingStartDate = (Calendar) in.readSerializable();
        preSamplingEndDate = (Calendar) in.readSerializable();
        actualStartDate = (Calendar) in.readSerializable();
        actualEndDate = (Calendar) in.readSerializable();
        noOfSamplesRequired = in.readInt();
        noOfSamplesTaken = in.readInt();
        lastCheckedSample = in.readInt();
        shift = in.readByte();
        utilization = in.readFloat();
        allowances = in.readFloat();
        utilizationError = in.readFloat();
        allowancesError = in.readFloat();
        hasReachedRequiredSamples = in.readByte() != 0;
        remarksCount = in.readInt();
        hasBreakTime = in.readByte() != 0;
    }

    public static final Creator<StudyObject> CREATOR = new Creator<StudyObject>() {
        @Override
        public StudyObject createFromParcel(Parcel in) {
            return new StudyObject(in);
        }

        @Override
        public StudyObject[] newArray(int size) {
            return new StudyObject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(index);
        parcel.writeString(type);
        parcel.writeString(name);
        parcel.writeInt(startOfWorkHour);
        parcel.writeInt(startOfWorkMinute);
        parcel.writeInt(endOfWorkHour);
        parcel.writeInt(endOfWorkMinute);
        parcel.writeInt(startOfBreakTimeHour);
        parcel.writeInt(startOfBreakTimeMinute);
        parcel.writeInt(endOfBreakTimeHour);
        parcel.writeInt(endOfBreakTimeMinute);
        parcel.writeByte((byte) (workingOnSunday ? 1 : 0));
        parcel.writeByte((byte) (workingOnMonday ? 1 : 0));
        parcel.writeByte((byte) (workingOnTuesday ? 1 : 0));
        parcel.writeByte((byte) (workingOnWednesday ? 1 : 0));
        parcel.writeByte((byte) (workingOnThursday ? 1 : 0));
        parcel.writeByte((byte) (workingOnFriday ? 1 : 0));
        parcel.writeByte((byte) (workingOnSaturday ? 1 : 0));
        parcel.writeTypedList(activities);
        parcel.writeTypedList(samples);
        parcel.writeList(daysOfWork);
        parcel.writeList(preSamplingDates);
        parcel.writeList(actualSamplingDates);
        parcel.writeSerializable(preSamplingStartDate);
        parcel.writeSerializable(preSamplingEndDate);
        parcel.writeSerializable(actualStartDate);
        parcel.writeSerializable(actualEndDate);
        parcel.writeInt(noOfSamplesRequired);
        parcel.writeInt(noOfSamplesTaken);
        parcel.writeInt(lastCheckedSample);
        parcel.writeByte(shift);
        parcel.writeFloat(utilization);
        parcel.writeFloat(allowances);
        parcel.writeFloat(utilizationError);
        parcel.writeFloat(allowancesError);
        parcel.writeByte((byte) (hasReachedRequiredSamples ? 1 : 0));
        parcel.writeInt(remarksCount);
        parcel.writeByte((byte) (hasBreakTime ? 1 : 0));
    }

    public void setIndex(int index){
        this.index = index;
    }

    public int getIndex(){
        return this.index;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void addActivity (ObjectActivity objectActivity){
        this.activities.add(objectActivity);
    }

    public ArrayList<ObjectActivity> getActivities() {
        return activities;
    }

    public int getStartOfWorkHour() {
        return startOfWorkHour;
    }

    public void setStartOfWorkHour(int startOfWorkHour) {
        this.startOfWorkHour = startOfWorkHour;
    }

    public int getStartOfWorkMinute() {
        return startOfWorkMinute;
    }

    public void setStartOfWorkMinute(int startOfWorkMinute) {
        this.startOfWorkMinute = startOfWorkMinute;
    }

    public int getEndOfWorkHour() {
        return endOfWorkHour;
    }

    public void setEndOfWorkHour(int endOfWorkHour) {
        this.endOfWorkHour = endOfWorkHour;
    }

    public int getEndOfWorkMinute() {
        return endOfWorkMinute;
    }

    public void setEndOfWorkMinute(int endOfWorkMinute) {
        this.endOfWorkMinute = endOfWorkMinute;
    }

    public int getStartOfBreakTimeHour() {
        return startOfBreakTimeHour;
    }

    public void setStartOfBreakTimeHour(int startOfBreakTimeHour) {
        this.startOfBreakTimeHour = startOfBreakTimeHour;
    }

    public int getStartOfBreakTimeMinute() {
        return startOfBreakTimeMinute;
    }

    public void setStartOfBreakTimeMinute(int startOfBreakTimeMinute) {
        this.startOfBreakTimeMinute = startOfBreakTimeMinute;
    }

    public int getEndOfBreakTimeHour() {
        return endOfBreakTimeHour;
    }

    public void setEndOfBreakTimeHour(int endOfBreakTimeHour) {
        this.endOfBreakTimeHour = endOfBreakTimeHour;
    }

    public int getEndOfBreakTimeMinute() {
        return endOfBreakTimeMinute;
    }

    public void setEndOfBreakTimeMinute(int endOfBreakTimeMinute) {
        this.endOfBreakTimeMinute = endOfBreakTimeMinute;
    }



    public boolean isWorkingOnSunday() {
        return workingOnSunday;
    }

    public void setWorkingOnSunday(boolean workingOnSunday) {
        this.workingOnSunday = workingOnSunday;
    }

    public boolean isWorkingOnMonday() {
        return workingOnMonday;
    }

    public void setWorkingOnMonday(boolean workingOnMonday) {
        this.workingOnMonday = workingOnMonday;
    }

    public boolean isWorkingOnTuesday() {
        return workingOnTuesday;
    }

    public void setWorkingOnTuesday(boolean workingOnTuesday) {
        this.workingOnTuesday = workingOnTuesday;
    }

    public boolean isWorkingOnWednesday() {
        return workingOnWednesday;
    }

    public void setWorkingOnWednesday(boolean workingOnWednesday) {
        this.workingOnWednesday = workingOnWednesday;
    }

    public boolean isWorkingOnThursday() {
        return workingOnThursday;
    }

    public void setWorkingOnThursday(boolean workingOnThursday) {
        this.workingOnThursday = workingOnThursday;
    }

    public boolean isWorkingOnFriday() {
        return workingOnFriday;
    }

    public void setWorkingOnFriday(boolean workingOnFriday) {
        this.workingOnFriday = workingOnFriday;
    }

    public boolean isWorkingOnSaturday() {
        return workingOnSaturday;
    }

    public void setWorkingOnSaturday(boolean workingOnSaturday) {
        this.workingOnSaturday = workingOnSaturday;
    }

    public void setPreSamplingStartDate(Calendar preSamplingStartDate) {
        this.preSamplingStartDate = preSamplingStartDate;
    }

    public Calendar getPreSamplingStartDate() {
        return preSamplingStartDate;
    }

    public void setPreSamplingEndDate(Calendar preSamplingEndDate) {
        this.preSamplingEndDate = preSamplingEndDate;
    }

    public Calendar getPreSamplingEndDate() {
        return preSamplingEndDate;
    }

    public void setActualStartDate(Calendar actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public Calendar getActualStartDate() {
        return actualStartDate;
    }

    public void setActualEndDate(Calendar actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public Calendar getActualEndDate() {
        return actualEndDate;
    }



    public ArrayList<Integer> getDaysOfWork() {
        return daysOfWork;
    }

    public void setDaysOfWork() {
        if (isWorkingOnSunday()) daysOfWork.add(Calendar.SUNDAY);
        if (isWorkingOnMonday()) daysOfWork.add(Calendar.MONDAY);
        if (isWorkingOnTuesday()) daysOfWork.add(Calendar.TUESDAY);
        if (isWorkingOnWednesday()) daysOfWork.add(Calendar.WEDNESDAY);
        if (isWorkingOnThursday()) daysOfWork.add(Calendar.THURSDAY);
        if (isWorkingOnFriday()) daysOfWork.add(Calendar.FRIDAY);
        if (isWorkingOnSaturday()) daysOfWork.add(Calendar.SATURDAY);
    }

    public ArrayList<Sample> getSamples(){
        return samples;
    }

    public void addSample(Sample sample){
        this.samples.add(sample);
    }

    public void setPreSamplingDates(){

        int daysBetween = (preSamplingEndDate.get(Calendar.YEAR)-preSamplingStartDate.get(Calendar.YEAR))
                *preSamplingStartDate.getActualMaximum(Calendar.DAY_OF_YEAR)
                + preSamplingEndDate.get(Calendar.DAY_OF_YEAR) - preSamplingStartDate.get(Calendar.DAY_OF_YEAR) + 1;
        for (int i = 0; i <daysBetween; i++){
            Calendar samplingDay = Calendar.getInstance();
            samplingDay.set(Calendar.YEAR, preSamplingStartDate.get(Calendar.YEAR));
            samplingDay.set(Calendar.DAY_OF_YEAR, preSamplingStartDate.get(Calendar.DAY_OF_YEAR));
            samplingDay.add(Calendar.DAY_OF_YEAR, i);

            if (daysOfWork.contains(samplingDay.get(Calendar.DAY_OF_WEEK))) preSamplingDates.add(samplingDay);
        }
    }

    public ArrayList<Calendar> getPreSamplingDates(){
        return preSamplingDates;
    }

    public int getNoOfSamplesRequired() {
        return noOfSamplesRequired;
    }

    public void computeNoOfSamplesRequired(float zValue) {
        this.noOfSamplesRequired = 0;
        for (ObjectActivity activity : activities){
            activity.computeNoOfSamplesRequired(zValue); /*assuming estimated proportion is set as initial value of proportion*/
            if (this.noOfSamplesRequired < activity.getNoOfSamplesRequired()) {
                this.noOfSamplesRequired = activity.getNoOfSamplesRequired();
            }
        }

        checkIfRequiredSamplesIsReached();
    }

    public int getNoOfSamplesLeft(){
//        int output = 0;
//        for (ObjectActivity activity : activities){
//            output += activity.getNoOfSamplesLeft();
//        }
//        return output;
        return this.noOfSamplesRequired - this.noOfSamplesTaken;
    }

    private void checkIfRequiredSamplesIsReached(){
//        for (ObjectActivity activity : activities){
//            if (!activity.hasReachedRequiredSamples()){
//                hasReachedRequiredSamples = false;
//                return;
//            }
//        }
        hasReachedRequiredSamples = noOfSamplesTaken >= noOfSamplesRequired;
    }

    public ArrayList<Calendar> getActualSamplingDates() {
        return actualSamplingDates;
    }

    public void setActualSamplingDates() {

        int daysBetween = (actualEndDate.get(Calendar.YEAR) - actualStartDate.get(Calendar.YEAR))
                * actualStartDate.getActualMaximum(Calendar.DAY_OF_YEAR)
                + actualEndDate.get(Calendar.DAY_OF_YEAR) - actualStartDate.get(Calendar.DAY_OF_YEAR) + 1;
        for (int i = 0; i <daysBetween; i++){
            Calendar samplingDay = Calendar.getInstance();
            samplingDay.set(Calendar.YEAR, actualEndDate.get(Calendar.YEAR));
            samplingDay.set(Calendar.DAY_OF_YEAR, actualStartDate.get(Calendar.DAY_OF_YEAR));
            samplingDay.add(Calendar.DAY_OF_YEAR, i);

            if (daysOfWork.contains(samplingDay.get(Calendar.DAY_OF_WEEK))) actualSamplingDates.add(samplingDay);
        }
    }

    public Sample findNextSample(){
        for (Sample sample : samples)
            if (sample.getCalendarDate().after(Project.artificialNow())) return sample;

        return null;
    }

    public Sample currentSample(){
        if (samples.size()>0) {
            for (int i = 1; i < samples.size(); i++)
                if (samples.get(i).getCalendarDate().after(Project.artificialNow()))
                    return samples.get(i - 1);

            return samples.get(samples.size() - 1);
        }
        return null;
    }

    public void sortSamples(){
        for (int i = samples.size() - 1; i >= 0; i--){
            for (int j = 1; j <= i; j++){
                if (samples.get(j-1).getCalendarDate()
                        .after(samples.get(j).getCalendarDate())){
                    Sample sampleTemp = samples.get(j-1);
                    samples.set(j-1, samples.get(j));
                    samples.set(j, sampleTemp);
                }
            }
        }
    }

    public int getNoOfSamplesTaken() {
        return noOfSamplesTaken;
    }

    public void computeNoOfSamplesTaken() {
        /*This method is used for debugging purposes only*/
        noOfSamplesTaken = 0;
        for (ObjectActivity activity : activities){
            noOfSamplesTaken += activity.getNoOfSamplesTaken();
        }
    }

    public void addSampleTaken(){
        noOfSamplesTaken += 1;
    }

    public int getLastCheckedSample() {
        return lastCheckedSample;
    }

    public void setLastCheckedSample(int lastCheckedSample) {
        this.lastCheckedSample = lastCheckedSample;
    }

    public void computeActivityProportions(){
        for (ObjectActivity activity : activities) activity.computeProportion((float) noOfSamplesTaken);
    }

    public int getNoOfRemainingSamplingDates(int phase){
        switch (phase){
            case Project.PHASE_PRESAMPLING:

                for (int i=0; i<preSamplingDates.size(); i++){
                    if (Project.artificialNow().get(Calendar.DAY_OF_YEAR) < preSamplingDates.get(i).get(Calendar.DAY_OF_YEAR) +
                            (preSamplingDates.get(i).get(Calendar.YEAR) - Project.artificialNow().get(Calendar.YEAR))
                                    * Project.artificialNow().getActualMaximum(Calendar.DAY_OF_YEAR)){
                        return preSamplingDates.size() - i;
                    }
                }
                break;

            case Project.PHASE_ACTUAL:
                if (Project.artificialNow().get(Calendar.DAY_OF_YEAR) < actualSamplingDates.get(0).get(Calendar.DAY_OF_YEAR) +
                        (actualSamplingDates.get(0).get(Calendar.YEAR) - Project.artificialNow().get(Calendar.YEAR))
                                * Project.artificialNow().getActualMaximum(Calendar.DAY_OF_YEAR))
                    return actualSamplingDates.size();

                for (int i=0; i<actualSamplingDates.size(); i++){
                    if (Project.artificialNow().get(Calendar.DAY_OF_YEAR) < actualSamplingDates.get(i).get(Calendar.DAY_OF_YEAR) +
                            (actualSamplingDates.get(i).get(Calendar.YEAR) - Project.artificialNow().get(Calendar.YEAR))
                                    * Project.artificialNow().getActualMaximum(Calendar.DAY_OF_YEAR)){
                        return actualSamplingDates.size() - i;
                    }
                }
                break;
        }
        return 0;
    }

    public Calendar getNextSamplingDate(int phase){
        switch (phase){
            case Project.PHASE_PRESAMPLING:
                for (int i=0; i<preSamplingDates.size(); i++){
                    if (Project.artificialNow().get(Calendar.DAY_OF_YEAR) < preSamplingDates.get(i).get(Calendar.DAY_OF_YEAR)
                            + (preSamplingDates.get(i).get(Calendar.YEAR) - Project.artificialNow().get(Calendar.YEAR))
                            * Project.artificialNow().getActualMaximum(Calendar.DAY_OF_YEAR)){
                        return preSamplingDates.get(i);
                    }
                }
                break;

            case Project.PHASE_ACTUAL:
                for (int i=0; i<actualSamplingDates.size(); i++){
                    if (Project.artificialNow().get(Calendar.DAY_OF_YEAR) < actualSamplingDates.get(i).get(Calendar.DAY_OF_YEAR)
                            + (actualSamplingDates.get(i).get(Calendar.YEAR) - Project.artificialNow().get(Calendar.YEAR))
                            * Project.artificialNow().getActualMaximum(Calendar.DAY_OF_YEAR)){
                        return actualSamplingDates.get(i);
                    }
                }
                break;
        }
        return null;
    }

    public byte getShift() {
        return shift;
    }

    public void setShift(byte shift) {
        this.shift = shift;
    }

    public boolean samplingDateContains(Calendar date){
        if (preSamplingDates.size()> 0) {
            for (Calendar samplingDate : preSamplingDates){
                if (date.get(Calendar.YEAR) == samplingDate.get(Calendar.YEAR)
                        && date.get(Calendar.DAY_OF_YEAR) == samplingDate.get(Calendar.DAY_OF_YEAR))
                    return true;
            }
        }

        for (Calendar samplingDate : actualSamplingDates){
            if (date.get(Calendar.YEAR) == samplingDate.get(Calendar.YEAR)
                    && date.get(Calendar.DAY_OF_YEAR) == samplingDate.get(Calendar.DAY_OF_YEAR))
                return true;
        }

        return false;
    }

    public void calculateResults(float zValue){
//        float noOfVASamples=0, noOfNVAESamples=0;
        float noOfUtilizationSamples=0, noOfAllowancesSamples=0;
//        for (ObjectActivity activity : activities){
//            if (activity.getType().equals(ObjectActivity.VA)) noOfVASamples += activity.getNoOfSamplesTaken();
//            if (activity.getType().equals(ObjectActivity.NVAE)) noOfNVAESamples += activity.getNoOfSamplesTaken();
//        }
//
        for (ObjectActivity activity : activities){
            if (activity.isForUtilization()) noOfUtilizationSamples += activity.getNoOfSamplesTaken();
            if (activity.isForAllowances()) noOfAllowancesSamples += activity.getNoOfSamplesTaken();
        }

        utilization = noOfUtilizationSamples/noOfSamplesTaken;
        allowances = noOfAllowancesSamples/noOfSamplesTaken;

        utilizationError = zValue*((float)Math.sqrt(utilization*(1-utilization)/noOfSamplesTaken));
        allowancesError = zValue*((float)Math.sqrt(allowances*(1-allowances)/noOfSamplesTaken));

        for (ObjectActivity activity : activities) activity.calculateResults(zValue, noOfSamplesTaken);
    }

    public float getUtilization() {
        return utilization;
    }

    public String getStringUtilization(){
        return String.format(Locale.US, "%.2f",utilization*100) + "% ± " +
                String.format(Locale.US,"%.2f",utilizationError*100) + "%";
    }

    public float getAllowances() {
        return allowances;
    }

    public String getStringAllowances(){
        return String.format(Locale.US, "%.2f",allowances*100) + "% ± " +
                String.format(Locale.US,"%.2f",allowancesError*100) + "%";
    }

    public boolean hasReachedRequiredSamples() {
        return hasReachedRequiredSamples;
    }

    public void setHasReachedRequiredSamples(boolean hasReachedRequiredSamples) {
        this.hasReachedRequiredSamples = hasReachedRequiredSamples;
    }

    public int getRemarksCount() {
        return remarksCount;
    }

    public void addRemarksCount() {
        this.remarksCount ++;
        addSampleTaken();
    }

    public boolean hasBreakTime() {
        return hasBreakTime;
    }

    public void setHasBreakTime(boolean hasBreakTime) {
        this.hasBreakTime = hasBreakTime;
    }

    public int getStartOfWorkInMinutes(){
        return startOfWorkHour*60 + startOfWorkMinute;
    }

    public int getEndOfWorkInMinutes(){
        if (shift == NIGHT_SHIFT){
            return (endOfWorkHour*60 + endOfWorkMinute) + 24*60;
        }else {
            return endOfWorkHour * 60 + endOfWorkMinute;
        }
    }
}
