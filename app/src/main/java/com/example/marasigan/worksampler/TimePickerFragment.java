package com.example.marasigan.worksampler;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.Calendar;

public class TimePickerFragment extends android.support.v4.app.DialogFragment {
    private Calendar c = Calendar.getInstance();
    private int hour = c.get(Calendar.HOUR);
    private int minute = c.get(Calendar.MINUTE);

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new TimePickerDialog(getActivity(), (TimePickerDialog.OnTimeSetListener) getActivity(),
                hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
