package com.example.marasigan.worksampler.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Sample implements Parcelable, Serializable {
    public static final int MISSED = -1;
    public static final int WAITING = 0;
    public static final int TAKEN = 1;
    public static final int WITH_REMARKS = 2;

    public static final String TAG = "Sample";

    private Calendar calendarDate;
    private int status;
    private String studyObjectName;
    private String activityName;
    private String remarks;
    private boolean lastSampleForToday;
    private float rating;


    public Sample (Calendar start, Calendar end){
        calendarDate = Calendar.getInstance();
        calendarDate.setTimeInMillis(randomBetween(start.getTimeInMillis(), end.getTimeInMillis()));
        calendarDate.set(Calendar.SECOND,0);
        calendarDate.set(Calendar.MILLISECOND,0);

        status = WAITING;
        lastSampleForToday = false;
    }

    public Sample (Calendar start, Calendar end, Calendar breakTimeStart, Calendar breakTimeEnd){
        do {
            calendarDate = Calendar.getInstance();
            calendarDate.setTimeInMillis(randomBetween(start.getTimeInMillis(), end.getTimeInMillis()));
            calendarDate.set(Calendar.SECOND,0);
            calendarDate.set(Calendar.MILLISECOND,0);

        }while (calendarDate.after(breakTimeStart) && calendarDate.before(breakTimeEnd));

        status = WAITING;
        lastSampleForToday = false;
    }

    public Sample() {

    }

    private long randomBetween(long min, long max){
        return (min + (long) (Math.random()*(max - min)));
    }

    protected Sample(Parcel in) {
        calendarDate = (Calendar) in.readSerializable();
        status = in.readInt();
        studyObjectName = in.readString();
        activityName = in.readString();
        lastSampleForToday = in.readByte() != 0;
        remarks = in.readString();
        rating = in.readFloat();
    }

    public static final Creator<Sample> CREATOR = new Creator<Sample>() {
        @Override
        public Sample createFromParcel(Parcel in) {
            return new Sample(in);
        }

        @Override
        public Sample[] newArray(int size) {
            return new Sample[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(calendarDate);
        parcel.writeInt(status);
        parcel.writeString(studyObjectName);
        parcel.writeString(activityName);
        parcel.writeByte((byte) (lastSampleForToday ? 1 : 0));
        parcel.writeString(remarks);
        parcel.writeFloat(rating);
    }

    public void setCalendarDate(Calendar calendarDate) {
        this.calendarDate = calendarDate;
    }

    public Calendar getCalendarDate() {
        return calendarDate;
    }

    public String getStringTime(){
        SimpleDateFormat simpleDateFormat;
        if (calendarDate.get(Calendar.HOUR) < 10)
            simpleDateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        else
            simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());


        return simpleDateFormat.format(calendarDate.getTime());
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStudyObjectName() {
        return studyObjectName;
    }

    public void setStudyObjectName(String studyObjectName) {
        this.studyObjectName = studyObjectName;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getDateStringLong(){
        return calendarDate.getDisplayName(Calendar.MONTH,Calendar.LONG, Locale.getDefault()) + " " +
                calendarDate.get(Calendar.DAY_OF_MONTH) + ", " + calendarDate.get(Calendar.YEAR);
    }

    public String getDateStringShort(){
        return calendarDate.getDisplayName(Calendar.MONTH,Calendar.SHORT, Locale.getDefault()) + " " +
                calendarDate.get(Calendar.DAY_OF_MONTH) + ", " + calendarDate.get(Calendar.YEAR);
    }

    public boolean isLastSampleForToday() {
        return lastSampleForToday;
    }

    public void setLastSampleForToday(boolean lastSampleForToday) {
        this.lastSampleForToday = lastSampleForToday;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
