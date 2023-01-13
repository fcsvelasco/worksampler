package com.example.marasigan.worksampler.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static com.example.marasigan.worksampler.R.color.colorPrimary;

public class SchedCustomView extends View {

    private Rect mHeader;
    private Paint mPaintHeader, mPaintDate, mPaintDay, mPaintDateNotInMonth;
    private int mHeaderColor;

    private String mHeaderMonth, mHeaderYear;
    private int[] mDay;
    private String[] mDayOfWeek;
    private int[] mDaysInMonth, mDaysNotInMonth;

    private SchedData data;

    public SchedCustomView(Context context) {
        super(context);
        init(null);
    }

    public SchedCustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SchedCustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public SchedCustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init (@Nullable AttributeSet set){
        mHeader = new Rect();
        mHeader.left = 0;
        mHeader.top = 0;
        mHeader.right = 1000;
        mHeader.bottom = 100;

        mPaintHeader = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHeaderColor = getResources().getColor(colorPrimary);
        mPaintHeader.setColor(mHeaderColor);

        mPaintDate = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintDate.setColor(Color.LTGRAY);
        mPaintDate.setTextSize(35f);

        mPaintDateNotInMonth = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintDateNotInMonth.setColor(Color.DKGRAY);
        mPaintDateNotInMonth.setTextSize(35f);

        mPaintDay = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintDay.setColor(Color.WHITE);
        mPaintDay.setTextSize(35f);

        mDayOfWeek = new String[7];
        mDayOfWeek[0] = "Su";
        mDayOfWeek[1] = "M";
        mDayOfWeek[2] = "Tu";
        mDayOfWeek[3] = "W";
        mDayOfWeek[4] = "Th";
        mDayOfWeek[5] = "F";
        mDayOfWeek[6] = "Sa";

        data = new SchedData();
        mDay = new int[7];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.LTGRAY);
        canvas.drawRect(mHeader, mPaintHeader);

        if (this.data != null){
            canvas.drawText(mHeaderMonth,30f,40f,mPaintDate);
            canvas.drawText(mHeaderYear, 30f,90f, mPaintDate);
            for(int i = 0; i<7; i++){
                if(mDaysInMonth[i] != 0){
                    canvas.drawText(mDaysInMonth[i]+"", 160f+(i*235f/3f), 40f, mPaintDate);
                    canvas.drawText(mDayOfWeek[i],160f+(i*235f/3f), 90f, mPaintDate );
                }
                if(mDaysNotInMonth[i] != 0){
                    canvas.drawText(mDaysNotInMonth[i]+"", 160f+(i*235f/3f), 40f, mPaintDateNotInMonth);
                    canvas.drawText(mDayOfWeek[i],160f+(i*235f/3f), 90f, mPaintDateNotInMonth );
                }
            }
        }
    }

    public void setHeaderDate(Calendar calendar){

        mDaysInMonth = new int[7];
        mDaysNotInMonth = new int[7];

        Calendar previousMonth = Calendar.getInstance();
        previousMonth.set(Calendar.DAY_OF_MONTH, 1);
        previousMonth.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        previousMonth.set(Calendar.MONTH,calendar.get(Calendar.MONTH) - 1);

        mHeaderMonth = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
        mHeaderYear = calendar.get(Calendar.YEAR)+"";

        mDay[0] = calendar.get(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_WEEK) + 1;
        for(int i = 1; i<7; i++) mDay[i] = mDay[i-1]+1;

        for(int i=0; i<7; i++){
            if(mDay[i]<=0) {
                mDay[i] = previousMonth.getActualMaximum(Calendar.DAY_OF_MONTH) + mDay[i];
                mDaysNotInMonth[i] = mDay[i];
            }else if(mDay[i] > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                mDay[i] = mDay[i] - calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                mDaysNotInMonth[i] = mDay[i];
            }else{
                mDaysInMonth[i] = mDay[i];
            }
        }
        postInvalidate();
    }

    public void setData(SchedData data){
        this.data = data;
//        setHeaderDate(this.data.getDate());
    }
}
