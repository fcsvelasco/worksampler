package com.example.marasigan.worksampler.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.support.v7.app.AlertDialog;

import com.example.marasigan.worksampler.R;
import com.example.marasigan.worksampler.entities.ObjectActivity;
import com.example.marasigan.worksampler.entities.Sample;
import com.example.marasigan.worksampler.entities.StudyObject;

public class ObjectActivityButton extends android.support.v7.widget.AppCompatButton{
    public static final int STYLE_DEFAULT = 0;
    public static final int STYLE_GREEN = 1;
    public static final int STYLE_YELLOW = 2;
    public static final int STYLE_RED = 3;
    public static final int STYLE_VA = 4;
    public static final int STYLE_NVAE = 5;
    public static final int STYLE_NVAN = 6;
    public static final int STYLE_OTHERS = 7;

    private ObjectActivity objectActivity;
    private StudyObject studyObject;
    private SchedData schedData;
    private Sample sample;
    private int index;

    public ObjectActivityButton(Context context) {
        super(context);
    }

    public ObjectActivityButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObjectActivityButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ObjectActivity getObjectActivity() {
        return objectActivity;
    }

    public void setObjectActivity(ObjectActivity objectActivity) {
        this.objectActivity = objectActivity;
    }

    public StudyObject getStudyObject() {
        return studyObject;
    }

    public void setStudyObject(StudyObject studyObject) {
        this.studyObject = studyObject;
    }

    public void setSchedData(SchedData schedData){
        this.schedData = schedData;
        String text = schedData.getSample().getStringTime() + " - " + schedData.getStudyObject().getName();
        setText(text);

        if (schedData.getSample().getStatus() == Sample.MISSED) setStyle(STYLE_RED);
        if (schedData.getSample().getStatus() == Sample.TAKEN) setStyle(STYLE_GREEN);
        if (schedData.getSample().getStatus() == Sample.WAITING) setStyle(STYLE_DEFAULT);
    }

    public SchedData getSchedData() {
        return schedData;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setStyle(int style){
        switch (style){
            case STYLE_DEFAULT:
                setBackgroundDrawable(((ObjectActivityButton) inflate(getContext(),R.layout.view_operator_activity_button,null)).getBackground());
                setTextColor(Color.BLACK);
                break;
            case STYLE_GREEN:
                setBackgroundDrawable(getResources().getDrawable(R.drawable.button_light_green));
                setTextColor(getResources().getColor(R.color.textWhite));
                break;
            case STYLE_YELLOW:
                setBackgroundDrawable(getResources().getDrawable(R.drawable.button_yellow));
                setTextColor(Color.BLACK);
                break;
            case STYLE_RED:
                setBackgroundDrawable(getResources().getDrawable(R.drawable.button_red));
                setTextColor(getResources().getColor(R.color.textWhite));
                break;
            case STYLE_VA:
                setBackgroundDrawable(getResources().getDrawable(R.drawable.button_va));
                setTextColor(getResources().getColor(R.color.textBlack));
                break;
            case STYLE_NVAE:
                setBackgroundDrawable(getResources().getDrawable(R.drawable.button_nvae));
                setTextColor(getResources().getColor(R.color.textBlack));
                break;
            case STYLE_NVAN:
                setBackgroundDrawable(getResources().getDrawable(R.drawable.button_nvan));
                setTextColor(getResources().getColor(R.color.textBlack));
                break;
            case STYLE_OTHERS:
                setBackgroundDrawable(getResources().getDrawable(R.drawable.button_others));
                setTextColor(getResources().getColor(R.color.textBlack));
                break;
        }
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(final Sample sample) {
        this.sample = sample;

        String text = sample.getStringTime() + " - " + sample.getStudyObjectName();
        setText(text);

        if (sample.getStatus() == Sample.MISSED) setStyle(STYLE_RED);
        if (sample.getStatus() == Sample.TAKEN) setStyle(STYLE_GREEN);
        if (sample.getStatus() == Sample.WAITING) setStyle(STYLE_DEFAULT);
        if (sample.getStatus() == Sample.WITH_REMARKS) setStyle(STYLE_YELLOW);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder message = new StringBuilder();
                message.append("Date: ").append(sample.getDateStringLong());
                message.append("\nTime: ").append(sample.getStringTime());
                message.append("\nOperator/Machine: ").append(sample.getStudyObjectName());

                if (sample.getStatus() == Sample.TAKEN) {
                    message.append("\nActivity: ").append(sample.getActivityName());
                    message.append("\nStatus: Taken");
                }else {
                    message.append("\nActivity: N/A");
                    if (sample.getStatus() == Sample.WAITING) message.append("\nStatus: Waiting");
                    if (sample.getStatus() == Sample.MISSED) message.append("\nStatus: Missed");
                    if (sample.getStatus() == Sample.WITH_REMARKS){
                        message.append("\nStatus: With remarks");
                        message.append("\nRemarks: ").append(sample.getRemarks());
                    }
                }
//                message.append("\nLast sample for today: ").append(sample.isLastSampleForToday());

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(message.toString());
                builder.setTitle("Sample Data");
                builder.create().show();
            }
        });
    }
}
