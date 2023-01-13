package com.example.marasigan.worksampler;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.marasigan.worksampler.entities.Project;
import com.example.marasigan.worksampler.entities.ProjectDebugMode;
import com.example.marasigan.worksampler.entities.Sample;
import com.example.marasigan.worksampler.entities.StudyObject;
import com.example.marasigan.worksampler.receiver.MyBroadcastReceiver;

import java.util.Calendar;

public class SelfWorkSamplingInProgress extends AppCompatActivity {
    private static final String CHANNEL_ID = "Maroon Worksampler channel ID";
    public static Project sProject;

    private Project project;
    private StudyObject self;
    private Fragment selfWSFragment;
    private CountDownTimer countDownToNextSample, countDownForGettingSample;
    private byte getSampleFragmentMode;
    private PendingIntent[] pendingNotificationIntent = new PendingIntent[Project.MAX_NOTIFICATIONS + 1];
    private boolean onEndProject, fromOnPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fromOnPause = false;


        setContentView(R.layout.activity_self_work_sampling_in_progress);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        if (getIntent().getParcelableExtra(Project.TAG) != null) project = getIntent().getParcelableExtra(Project.TAG);
        else project = sProject;
        setTitle(project.getName());

        self = project.getSelfOperator();
        setGetSampleFragment();

        createNotificationChannel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fromOnPause){
//            Intent intent = getIntent();
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
        }

        setGetSampleFragment();

        if (Project.artificialNow().getTimeInMillis() - project.getLastSampleForTheDay().getCalendarDate().getTimeInMillis()
                > (project.getSampleDuration() - 1)*1000){
            if(project.getLastSampleForTheDay().getCalendarDate().get(Calendar.DAY_OF_YEAR)
                    == Project.artificialNow().get(Calendar.DAY_OF_YEAR))
                onDayEnded(Project.SCHED_NEXT_DAY);
            else
                onDayEnded(Project.SCHED_TODAY);
        }

        cancelNotifications();
        onEndProject = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        fromOnPause = true;
        if (!onEndProject) {
            for (int i = 0; i < project.getNextSamples().size() + 1; i++) {
                if (project.getNextSamples().isEmpty()) break;

                if (i == project.getNextSamples().size()) {
//                    setNotification(project.getNextSamples().get(i - 1), 1003 + i, i);
                    createNotification(project.getNextSamples().get(i - 1), 1003 + i, i);
                }
                else {
//                    setNotification(project.getNextSamples().get(i), 1003 + i, i);
                    createNotification(project.getNextSamples().get(i), 1003 + i, i);
                }
            }
        }

        cancelCountdownTimer();
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_project_in_progress, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

//        if (id == R.id.action_project_settings) {
//            return true;
//        }

        if (id == R.id.action_projectInProgress_back_to_home){
            backToHomepage();
            return true;
        }
        if (id == R.id.action_end_project) {
            finishProject();
            return true;
        }
        if (id == R.id.action_exit_project) {
            quitProject();
            return true;
        }
        if (id == R.id.action_projectInProgress_help){
            helpMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void backToHomepage(){
        Intent intent = new Intent(SelfWorkSamplingInProgress.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void finishProject(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Finish Project");
        builder.setMessage("Are you sure you want to end this project and see the results?");
        builder.setPositiveButton("End", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                project.onProjectEnded(SelfWorkSamplingInProgress.this,Project.artificialNow());
                onEndProject = true;

                Results.sProject = project;
                Intent intent = new Intent(SelfWorkSamplingInProgress.this, Results.class);
//                intent.putExtra(Project.TAG, (Parcelable)project);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel",null);
        builder.create().show();
    }

    private void quitProject(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit Project");
        builder.setMessage("Are you sure you want to quit this project? All data will be lost.");
        builder.setPositiveButton("Quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cancelNotifications();
                project.delete(SelfWorkSamplingInProgress.this);
                onEndProject = true;
                Intent intent = new Intent(SelfWorkSamplingInProgress.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("Cancel",null);
        builder.create().show();
    }

    private void helpMenu(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This app is still in testing mode. For comments or inquiries on the use of this app, " +
                "kindly contact:\n\nFranz Christian Velasco\nfsvelasco@up.edu.ph");

        builder.create().show();
    }

    private void setGetSampleFragment(){
        selfWSFragment = new GetSampleDialogFragment();

        long differenceInMillis = Project.artificialNow().getTimeInMillis()
                - self.currentSample().getCalendarDate().getTimeInMillis();
        if (differenceInMillis > 0 && differenceInMillis <= project.getSampleDuration() * 1000
                && self.currentSample().getStatus() == Sample.WAITING) {
            setCountDownForGettingSample();
            getSampleFragmentMode = GetSampleDialogFragment.GET_SAMPLE;
        } else {
            if (self.findNextSample() != null){
                setCountDownToNextSample();
                getSampleFragmentMode = GetSampleDialogFragment.CHECK_STATUS;
            }
        }

        selfWSFragment.setArguments(getBundle());

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.self_WS_frame_layout,selfWSFragment).commit();
    }

    private void setCountDownToNextSample(){

        long millisTillNextSample = self.findNextSample().getCalendarDate().getTimeInMillis()
                - Project.artificialNow().getTimeInMillis();

        if (countDownToNextSample != null) countDownToNextSample.cancel();
        countDownToNextSample = new CountDownTimer(millisTillNextSample, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                getSampleFragmentMode = GetSampleDialogFragment.GET_SAMPLE;

                selfWSFragment.setArguments(getBundle());
                ((GetSampleDialogFragment)selfWSFragment).modeChanged();

                setCountDownForGettingSample();
                playSoundAndVibrate();
            }
        }.start();
    }

    private void setCountDownForGettingSample(){
        long millisToGetSample = self.currentSample().getCalendarDate().getTimeInMillis()
                - Project.artificialNow().getTimeInMillis() + project.getSampleDuration()*1000;

        if (countDownForGettingSample != null) countDownForGettingSample.cancel();
        countDownForGettingSample = new CountDownTimer(millisToGetSample, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
//                if (!(((GetSampleDialogFragment)selfWSFragment).getAddActivityDialog() != null
//                        && ((GetSampleDialogFragment)selfWSFragment).getAddActivityDialog().isShowing()))
                Toast.makeText(SelfWorkSamplingInProgress.this, "Sample missed!", Toast.LENGTH_SHORT).show();

                self.currentSample().setStatus(Sample.MISSED);
                onSampleMissed();
//                project.save(getContext());
                getSampleFragmentMode = GetSampleDialogFragment.CHECK_STATUS;

                selfWSFragment.setArguments(getBundle());
                ((GetSampleDialogFragment)selfWSFragment).modeChanged();

                if(self.findNextSample() != null) setCountDownToNextSample();
            }
        }.start();
    }

    private void playSoundAndVibrate(){
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        Uri ringtoneSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(this,ringtoneSound);

        vibrator.vibrate(500);
        ringtone.play();
    }

    public void onSampleRecorded(){
        getSampleFragmentMode = GetSampleDialogFragment.CHECK_STATUS;
        countDownForGettingSample.cancel();
        if(self.findNextSample() != null) setCountDownToNextSample();

        selfWSFragment.setArguments(getBundle());
        ((GetSampleDialogFragment)selfWSFragment).modeChanged();

        project.setSamplesForTheDay();
        if (samplingDayIsOver()) onDayEnded(Project.SCHED_NEXT_DAY);
        project.save(this);
    }

    private void cancelCountdownTimer(){
        if (countDownForGettingSample != null) countDownForGettingSample.cancel();
        if (countDownToNextSample != null) countDownToNextSample.cancel();
    }

    private Bundle getBundle(){
        Bundle bundle = new Bundle();
        bundle.putParcelable(StudyObject.TAG,self);
        if(self.findNextSample() != null)
            bundle.putParcelable(Sample.TAG, self.findNextSample());
        bundle.putByte("Mode", getSampleFragmentMode);
        bundle.putInt(Project.MODE_TAG,project.getMode());
        bundle.putInt(Project.SAMPLE_DURATION_TAG, project.getSampleDuration());
        bundle.putFloat(Project.Z_VALUE_TAG, project.getzValue());

        return bundle;
    }

    public void onSampleMissed(){
        if (samplingDayIsOver()) onDayEnded(Project.SCHED_NEXT_DAY);
        project.save(this);
    }

    private boolean samplingDayIsOver(){
        for (Sample sample : project.getSamplesForTheDay()){
            if (sample.getStatus() == Sample.WAITING) return false;
        }

        return true;
//        return project.getLastSampleForTheDay().getStatus() != Sample.WAITING;
    }

    private void onDayEnded(byte schedWhen){
        if (todayIsLastDayOfPreSampling()){
            onPreSamplingEnded(schedWhen);
            return;
        }
        if (todayIsLastDayOfActualSampling()){
            onActualSamplingEnded();
            return;
        }
        project.onDayEnded(this, Project.artificialNow(), schedWhen);

        if (project.hasReachedRequiredSamples()){
            onRequiredSamplesReached();
            return;
        }

        if (project.studyObjectHasJustEndedSampling()){
            StringBuilder message = new StringBuilder();
            message.append("The following reached the required number of samples:");
            for (StudyObject object : project.getStudyObjects()){
                if (object.hasReachedRequiredSamples()) message.append("\n\t\t").append(object.getName());
            }
            message.append("\n\nSampling will continue for the other operators/machines.");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(message.toString());
            builder.create().show();

            project.setStudyObjectHasJustEndedSampling(false);
        }
    }

    private boolean todayIsLastDayOfPreSampling() {
        return project.conductsPreSampling()
                && Project.artificialNow().get(Calendar.DAY_OF_YEAR) == project.getLastDayOfPreSampling().get(Calendar.DAY_OF_YEAR)
                && Project.artificialNow().get(Calendar.YEAR) == project.getLastDayOfPreSampling().get(Calendar.YEAR);
    }

    private boolean todayIsLastDayOfActualSampling(){
        return Project.artificialNow().get(Calendar.DAY_OF_YEAR) == project.getLastDayOfActualSampling().get(Calendar.DAY_OF_YEAR)
                && Project.artificialNow().get(Calendar.YEAR) == project.getLastDayOfActualSampling().get(Calendar.YEAR);
    }

    private void onPreSamplingEnded(byte schedWhen){
        project.setPhase(Project.PHASE_ACTUAL);
        project.onDayEnded(this, Project.artificialNow(), schedWhen);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sampling Schedule");
        builder.setMessage("Preliminary sampling has ended." +
                "The number of samples to be taken in the following days " +
                "will be based on the calculated required number of samples.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void onActualSamplingEnded(){
        project.onProjectEnded(SelfWorkSamplingInProgress.this,Project.artificialNow());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sampling has ended");
        builder.setPositiveButton("OK", null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setMessage("Sampling has ended. The results for this study will be displayed.");
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    Results.sProject = project;
                    Intent intent = new Intent(SelfWorkSamplingInProgress.this, Results.class);
//                    intent.putExtra(Project.TAG, (Parcelable)project);
                    startActivity(intent);
                }
            });
        }else {
            builder.setMessage("Sampling has ended. Go to Menu and choose 'End project' to see results.");
        }

        builder.create().show();
    }

    private void onRequiredSamplesReached(){
        project.calculateResults();
        onEndProject = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sampling has ended");
        builder.setPositiveButton("OK", null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setMessage("Sampling has ended. The required number of samples has been reached. The results for this study will be displayed.");
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    Results.sProject = project;
                    Intent intent = new Intent(SelfWorkSamplingInProgress.this, Results.class);
//                    intent.putExtra(Project.TAG, (Parcelable)project);
                    startActivity(intent);
                }
            });
        }else {
            builder.setMessage("Sampling has ended. The required number of samples has been reached. Go to Menu and choose 'End project' to see results.");
        }

        builder.create().show();
    }

    private void createNotification(Sample sample, int notificationID, int notificationIntentCount){
        Calendar notifyWhen = sample.getCalendarDate();
//        if (notificationIntentCount == Project.MAX_NOTIFICATIONS) notifyWhen.set(Calendar.MINUTE, sample.getCalendarDate().get(Calendar.MINUTE) + 1);
//        else notifyWhen.set(Calendar.MINUTE, sample.getCalendarDate().get(Calendar.MINUTE));
//
//        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.worksamplerlogo_small)
////                .setStyle()
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setAutoCancel(true)
//                .setWhen(notifyWhen.getTimeInMillis())
//                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
//                .setSound(alarmSound);
//
//        if (notificationIntentCount == Project.MAX_NOTIFICATIONS){
//            mBuilder.setTicker("You've been missing observations!");
//            mBuilder.setContentTitle("Maroon Worksampler");
//            mBuilder.setContentText("You've been missing observations!");
//        }else {
//            mBuilder.setTicker("Time to get sample!");
//            mBuilder.setContentTitle("Maroon Worksampler");
//            mBuilder.setContentText("Get sample for " + sample.getStudyObjectName());
//        }
//
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.putExtra(Project.IS_FROM_PENDING_INTENT, true);
//        if (project instanceof ProjectDebugMode) intent.putExtra("File name",ProjectDebugMode.FILE_NAME_DEBUG_MODE);
//        else intent.putExtra("File name", Project.FILE_NAME);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,notificationID,intent,PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_ONE_SHOT);
//        mBuilder.setContentIntent(pendingIntent);
//
//        Notification notification = mBuilder.build();
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        Intent notificationIntent = new Intent(this, MyBroadcastReceiver.class);
        notificationIntent.putExtra("Notification ID",notificationID);
//        notificationIntent.putExtra("Notification", notification);
        notificationIntent.putExtra("Notification calendar", notifyWhen);
        notificationIntent.putExtra("Notification intent count", notificationIntentCount);
        if (notificationIntentCount < Project.MAX_NOTIFICATIONS)
            notificationIntent.putExtra("Object name", sample.getStudyObjectName());
        if (project instanceof ProjectDebugMode) notificationIntent.putExtra("File name",ProjectDebugMode.FILE_NAME_DEBUG_MODE);
        else notificationIntent.putExtra("File name", Project.FILE_NAME);
        pendingNotificationIntent[notificationIntentCount] = PendingIntent.getBroadcast(this,
                notificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= 23){
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    notifyWhen.getTimeInMillis(),pendingNotificationIntent[notificationIntentCount]);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notifyWhen.getTimeInMillis(),pendingNotificationIntent[notificationIntentCount]);
        }else alarmManager.set(AlarmManager.RTC_WAKEUP, notifyWhen.getTimeInMillis(), pendingNotificationIntent[notificationIntentCount]);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

//    private void setNotification(Sample sample, int notificationID, int notificationIntentCount){
//        Calendar notifyWhen = sample.getCalendarDate();
//        if (notificationIntentCount == Project.MAX_NOTIFICATIONS) notifyWhen.set(Calendar.MINUTE, sample.getCalendarDate().get(Calendar.MINUTE) + 1);
//        else notifyWhen.set(Calendar.MINUTE, sample.getCalendarDate().get(Calendar.MINUTE));
//
//        Notification.Builder notificationBuilder = new Notification.Builder(this);
//
//        notificationBuilder.setAutoCancel(true);
//        notificationBuilder.setSmallIcon(R.drawable.worksamplerlogo_small);
//        notificationBuilder.setWhen(notifyWhen.getTimeInMillis());
//        notificationBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
//        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        notificationBuilder.setSound(alarmSound);
//
//
//        if (notificationIntentCount == Project.MAX_NOTIFICATIONS){
//            notificationBuilder.setTicker("You've been missing observations!");
//            notificationBuilder.setContentTitle("Maroon Worksampler");
//            notificationBuilder.setContentText("You've been missing observations!");
//        }else {
//            notificationBuilder.setTicker("Time to get sample!");
//            notificationBuilder.setContentTitle("Maroon Worksampler");
//            notificationBuilder.setContentText("Get sample now");
//        }
//
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.putExtra(Project.IS_FROM_PENDING_INTENT, true);
//        if (project instanceof ProjectDebugMode) intent.putExtra("File name",ProjectDebugMode.FILE_NAME_DEBUG_MODE);
//        else intent.putExtra("File name", Project.FILE_NAME);
////        intent.putExtra("Project", (Parcelable) project);
////        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,notificationID,intent,PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_ONE_SHOT);
//        notificationBuilder.setContentIntent(pendingIntent);
//
//        Notification notification = build(notificationBuilder);
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//
//        Intent notificationIntent = new Intent(this, MyBroadcastReceiver.class);
//        notificationIntent.putExtra("Notification ID",notificationID);
//        notificationIntent.putExtra("Notification", notification);
////        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        pendingNotificationIntent[notificationIntentCount] = PendingIntent.getBroadcast(this,
//                notificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notifyWhen.getTimeInMillis(),pendingNotificationIntent[notificationIntentCount]);
//        }else alarmManager.set(AlarmManager.RTC_WAKEUP, notifyWhen.getTimeInMillis(), pendingNotificationIntent[notificationIntentCount]);
//    }
//
//    private Notification build(Notification.Builder builder){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            return builder.build();
//        }else return builder.getNotification();
//    }

    private void cancelNotifications(){
        for (int i=0; i<Project.MAX_NOTIFICATIONS + 1; i++){
            Intent intent = new Intent(this, MyBroadcastReceiver.class);
            pendingNotificationIntent[i] = PendingIntent.getBroadcast(this,
                    1003+i,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            pendingNotificationIntent[i].cancel();
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingNotificationIntent[i]);
        }
    }

    public Project getProject() {
        return project;
    }

    public CountDownTimer getCountDownForGettingSample() {
        return countDownForGettingSample;
    }

    public CountDownTimer getCountDownToNextSample() {
        return countDownToNextSample;
    }
}
