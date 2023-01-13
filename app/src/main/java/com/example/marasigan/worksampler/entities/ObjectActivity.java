package com.example.marasigan.worksampler.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

public class ObjectActivity implements Parcelable, Serializable {
    public final static String VA = "Value Adding";
    public final static String NVAE = "Non Value Adding Essential";
    public final static String NVAN = "Non Value Adding Non Essential";

    public final static String TAG = "Object Activity";

    private String name, type, proportionConfidenceInterval;
    private int index, noOfSamplesRequired, noOfSamplesTaken/*, noOfSamplesLeft*/;
    private float proportion, estimatedProportion, error, standardTime, allowances,
            output, adjustedError, performanceRating, productionHours, averageRating;
    private StudyObject studyObject;
    private boolean hasCalculatedST, usedCalculatedAllowances, forUtilization, forAllowances/*, hasReachedRequiredSamples*/;
//    private ArrayList<Sample> samples;

    public ObjectActivity(){
//        samples = new ArrayList<>();
        this.hasCalculatedST = false;
    }

    protected ObjectActivity(Parcel in) {
//        samples = new ArrayList<>();

        index = in.readInt();
        name = in.readString();
        type = in.readString();
        proportion = in.readFloat();
        estimatedProportion = in.readFloat();
        error = in.readFloat();
        noOfSamplesRequired = in.readInt();
        standardTime = in.readFloat();
        output = in.readFloat();
        noOfSamplesTaken = in.readInt();
        adjustedError = in.readFloat();
        proportionConfidenceInterval = in.readString();
        performanceRating = in.readFloat();
        productionHours = in.readFloat();
        hasCalculatedST = in.readByte() != 0;
        usedCalculatedAllowances = in.readByte() != 0;
        averageRating = in.readFloat();
        forUtilization = in.readByte() != 0;
        forAllowances = in.readByte() != 0;
//        hasReachedRequiredSamples = in.readByte() != 0;
//        noOfSamplesLeft = in.readInt();
    }

    public static final Creator<ObjectActivity> CREATOR = new Creator<ObjectActivity>() {
        @Override
        public ObjectActivity createFromParcel(Parcel in) {
            return new ObjectActivity(in);
        }

        @Override
        public ObjectActivity[] newArray(int size) {
            return new ObjectActivity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(index);
        parcel.writeString(name);
        parcel.writeString(type);
        parcel.writeFloat(proportion);
        parcel.writeFloat(estimatedProportion);
        parcel.writeFloat(error);
        parcel.writeInt(noOfSamplesRequired);
        parcel.writeFloat(standardTime);
        parcel.writeFloat(output);
        parcel.writeInt(noOfSamplesTaken);
        parcel.writeFloat(adjustedError);
        parcel.writeString(proportionConfidenceInterval);
        parcel.writeFloat(performanceRating);
        parcel.writeFloat(productionHours);
        parcel.writeByte((byte) (hasCalculatedST ? 1 : 0));
        parcel.writeByte((byte) (usedCalculatedAllowances ? 1 : 0));
        parcel.writeFloat(averageRating);
        parcel.writeByte((byte) (forUtilization ? 1 : 0));
        parcel.writeByte((byte) (forAllowances ? 1 : 0));
//        parcel.writeByte((byte) (hasReachedRequiredSamples ? 1 : 0));
//        parcel.writeInt(noOfSamplesLeft);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getProportion() {
        return proportion;
    }

//    public void setProportion(float proportion) {
//        this.proportion = proportion;
//    }

    public float getEstimatedProportion() {
        return estimatedProportion;
    }

    public void setEstimatedProportion(float estimatedProportion) {
        this.estimatedProportion = estimatedProportion;
        proportion = estimatedProportion; /*proportion is initially set equal to estimated proportion*/
    }

    public float getError() {
        return error;
    }

    public void setError(float error) {
        this.error = error;
    }

    public StudyObject getStudyObject() {
        return studyObject;
    }

    public void setStudyObject(StudyObject studyObject) {
        this.studyObject = studyObject;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }



    public int getNoOfSamplesRequired() {
        return noOfSamplesRequired;
    }

//    public void computeNoOfSamplesRequiredUsingEstimatedP(float zValue) {
//        this.noOfSamplesRequired = (int)((zValue*zValue)*estimatedProportion*(1-estimatedProportion)/(error*error)) + 1;
//    }
//
//    public void computeNoOfSamplesRequiredUsingCalculatedP(float zValue){
//        this.noOfSamplesRequired = (int)((zValue*zValue)*proportion*(1-proportion)/(error*error)) + 1;
//    }

    public void computeNoOfSamplesRequired(float zValue){
        /*use this if estimated proportion is the initial value of proportion*/
        if (proportion == 0)
            this.noOfSamplesRequired = (int)((zValue*zValue)*estimatedProportion*(1-estimatedProportion)/(error*error)) + 1;
        else
            this.noOfSamplesRequired = (int)((zValue*zValue)*proportion*(1-proportion)/(error*error)) + 1;

//        int difference = noOfSamplesRequired - noOfSamplesTaken;
//        noOfSamplesLeft = difference <= 0 ? 0:difference;
//        if (noOfSamplesLeft  <= 0) hasReachedRequiredSamples = true;
    }

//    public ArrayList<Sample> getSamples() {
//        return samples;
//    }
//
//    public void addSample(Sample sample) {
//        samples.add(sample);
//    }

    public void computeProportion(float totalSampleCount){
        proportion = (float)noOfSamplesTaken/totalSampleCount;
    }

    public int getNoOfSamplesTaken() {
        return noOfSamplesTaken;
    }

    public void setNoOfSamplesTaken(int noOfSamplesTaken) {
        /*This method if used for debugging purposes only. Shall be deleted once app is finished*/
        this.noOfSamplesTaken = noOfSamplesTaken;
    }

    public void addSampleTaken(){
        noOfSamplesTaken++;
    }

    public void calculateResults(float zValue, float totalSampleCount){
        adjustedError = zValue*((float)Math.sqrt(proportion*(1-proportion)/totalSampleCount));
        proportionConfidenceInterval = String.format(Locale.US,"%.2f",proportion*100) + "% ± " +
                String.format(Locale.US,"%.2f",adjustedError*100) + "%";
    }

    public float getAdjustedError() {
        return adjustedError;
    }

    public String getProportionConfidenceInterval() {
        return proportionConfidenceInterval;
    }

    public float getPerformanceRating() {
        return performanceRating;
    }

    public void setPerformanceRating(float performanceRating) {
        this.performanceRating = performanceRating;
    }

    public float getProductionHours() {
        return productionHours;
    }

    public void setProductionHours(float productionHours) {
        this.productionHours = productionHours;
    }

    public float getOutput() {
        return output;
    }

    public void setOutput(float output) {
        this.output = output;
    }

    public boolean hasCalculatedST() {
        return hasCalculatedST;
    }

    public void setHasCalculatedST(boolean hasCalculatedST) {
        this.hasCalculatedST = hasCalculatedST;
    }

    public void calculateStandardTime(int mode){
        float observedTime = (productionHours*60/output)*proportion;

        standardTime = observedTime*((mode == Project.MODE_RATED_WS ? averageRating : performanceRating)/100)/(1-allowances);

        hasCalculatedST = true;
    }

    public void setAllowances(float allowances) {
        this.allowances = allowances;
    }

    public float getAllowances(){
        return allowances;
    }

    public float getStandardTime(){
        return standardTime;
    }

    public String getStringStandardTime(){
        return String.format(Locale.US,"%.2f",standardTime) + " min per piece";
    }

    public boolean usedCalculatedAllowances() {
        return usedCalculatedAllowances;
    }

    public void setUsedCalculatedAllowances(boolean usedCalculatedAllowances) {
        this.usedCalculatedAllowances = usedCalculatedAllowances;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public String getStringAverageRating(){
        return String.format(Locale.US, "%.2f",averageRating) + "%";
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public void addPerformanceRating(float rating){
        averageRating = (averageRating*(noOfSamplesTaken-1) + rating)/noOfSamplesTaken;
    }

    public boolean isForUtilization() {
        return forUtilization;
    }

    public void setForUtilization(boolean forUtilization) {
        this.forUtilization = forUtilization;
    }

    public boolean isForAllowances() {
        return forAllowances;
    }

    public void setForAllowances(boolean forAllowances) {
        this.forAllowances = forAllowances;
    }


//    public int getNoOfSamplesLeft() {
//        return noOfSamplesLeft;
//    }

//    public boolean hasReachedRequiredSamples() {
//        return hasReachedRequiredSamples;
//    }
}
