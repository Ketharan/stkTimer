package matlogic.stktimer;


import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Debug;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    private long timeCountInMilliSeconds = 3 * 60000;
    private long timeCount = 0;
    private boolean needRunning = true;

    public boolean isNeedRunning() {
        return needRunning;
    }

    public void setNeedRunning(boolean needRunning) {
        this.needRunning = needRunning;
    }




    private enum TimerStatus {
        STARTED,
        STOPPED
    }

    private TimerStatus timerStatus = TimerStatus.STOPPED;

    private ProgressBar progressBarCircle;
    private EditText editTextMinute;
    private TextView textViewTime;


    private ImageView imageViewReset;
    private ImageView imageViewStartStop;
    private CountDownTimer countDownTimer = null;
    private CountDownTimer secondaryTimer = null;


    final FirebaseDatabase database = FirebaseDatabase.getInstance();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SpannableString s = new SpannableString("nrhw;fiz - 2018");
        s.setSpan(new TypefaceSpan(this, "baamini.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setTitle(s);
        actionBar.hide();

        // method call to initialize the views
        initViews();
        // method call to initialize the listeners


        // Attach a listener to read the data at our posts reference
        DatabaseReference ref1 = database.getReference("status");
        ref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("status", dataSnapshot.getValue().toString());
                if (dataSnapshot.getValue().equals("START")){
                    start();
                }else if (dataSnapshot.getValue().equals("PAUSE")){
                    if (countDownTimer != null){
                        stop();
                    }
                }else if (dataSnapshot.getValue().equals("RESET")){
                    reset();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


        DatabaseReference ref2 = database.getReference("time");
        ref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("status", dataSnapshot.getValue().toString());
                timeCountInMilliSeconds = Integer.parseInt(dataSnapshot.getValue().toString())*1000;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


    }

    /**
     * method to initialize the views
     */
    private void initViews() {
        progressBarCircle = (ProgressBar) findViewById(R.id.progressBarCircle);
        editTextMinute = (EditText) findViewById(R.id.editTextMinute);
        textViewTime = (TextView) findViewById(R.id.textViewTime);

        textViewTime.setTypeface(null, Typeface.BOLD);
        imageViewReset = (ImageView) findViewById(R.id.imageViewReset);
        imageViewStartStop = (ImageView) findViewById(R.id.imageViewStartStop);
    }







    /**
     * method to reset count down timer
     */
    private void reset() {
        if (countDownTimer != null){
            stopCountDownTimer();
        }

        textViewTime.setText(hmsTimeFormatter(timeCount));
        setProgressBarValues();
        //textViewTime.setText(hmsTimeFormatter(timeCountInMilliSeconds));
        //setProgressBarValues();
        // hiding the reset icon
        //imageViewReset.setVisibility(View.GONE);
        // changing stop icon to start icon
        //imageViewStartStop.setImageResource(R.drawable.icon_start);
        // making edit text editable
        // editTextMinute.setEnabled(true);
        // changing the timer status to stopped
        textViewTime.setTextColor(Color.parseColor("#16961A"));
        setNeedRunning(false);
    }


    private void start(){
        setProgressBarValues();
        startCountDownTimer();
    }

    private void stop(){
        setNeedRunning(false);
        stopCountDownTimer();
    }


    private long untillmillis;

    public long getUntillmillis() {
        return untillmillis;
    }

    public void setUntillmillis(long untillmillis) {
        this.untillmillis = untillmillis;
    }

    /**0:00


     * method to start count down timer
     */
    private void startCountDownTimer() {

        setNeedRunning(true);
        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                setUntillmillis(millisUntilFinished);
                long passedMillis = timeCountInMilliSeconds - millisUntilFinished + 1000;
                textViewTime.setText(hmsTimeFormatter(passedMillis));
                progressBarCircle.setProgress((int) (passedMillis / 1000));
                if (millisUntilFinished < 60000){
                    textViewTime.setTextColor(Color.parseColor("#FF6F00"));
                    progressBarCircle.setDrawingCacheBackgroundColor(Color.parseColor("#FF6F00"));
                }

            }

            @Override
            public void onFinish() {
                textViewTime.setTextColor(Color.parseColor("#C71D12"));
                progressBarCircle.setProgress((int) (timeCountInMilliSeconds / 1000));
                secondaryTimer = new CountDownTimer(300000,1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (!isNeedRunning()) {
                            this.cancel();
                        } else {
                            long secPassedMills = timeCountInMilliSeconds + 300000 - millisUntilFinished + 1000;
                            textViewTime.setText(hmsTimeFormatter(secPassedMills));
                        }



                    }

                    @Override
                    public void onFinish() {

                    }
                };
                secondaryTimer.start();
//                textViewTime.setText(hmsTimeFormatter(getUntillmillis()-1000));



                // call to initialize the progress bar values
                //setProgressBarValues();
                // hiding the reset icon
                //imageViewReset.setVisibility(View.GONE);
                // changing stop icon to start icon
                //imageViewStartStop.setImageResource(R.drawable.icon_start);
                // making edit text editable
                // editTextMinute.setEnabled(true);
                // changing the timer status to stopped
                //textViewTime.setTextColor(Color.parseColor("#2E7D32"));
                //timerStatus = TimerStatus.STOPPED;
            }

        };
        countDownTimer.start();
    }

    /**
     * method to stop count down timer
     */
    private void stopCountDownTimer() {
        countDownTimer.cancel();
    }

    /**
     * method to set circular progress bar values
     */
    private void setProgressBarValues() {

        progressBarCircle.setMax((int) timeCountInMilliSeconds / 1000);
        progressBarCircle.setProgress(0);
    }


    /**
     * method to convert millisecond to time format
     *
     * @param milliSeconds
     * @return HH:mm:ss time formatted string
     */
    private String hmsTimeFormatter(long milliSeconds) {

        String hms = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

        return hms;


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
