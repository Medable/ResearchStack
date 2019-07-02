package org.researchstack.backbone.ui.activetasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.widget.ImageViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.researchstack.backbone.R;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.task.FitnessCheckActiveTask;
import org.researchstack.backbone.ui.PinCodeActivity;
import org.researchstack.backbone.utils.ThemeUtils;

public class FitnessCheckActivity extends PinCodeActivity {

    private static final String TAG = "FitnessCheckActivity";
    public static final String EXTRA_TASK = "FitnessCheckActivity.ExtraTask";
    public static final String EXTRA_TASK_RESULT = "FitnessCheckActivity.ExtraTaskResult";
    public static final String EXTRA_COLOR_PRIMARY = "FitnessCheckActivity.ExtraColorPrimary";
    public static final String EXTRA_COLOR_PRIMARY_DARK = "FitnessCheckActivity.ExtraColorPrimaryDark";
    public static final String EXTRA_COLOR_SECONDARY = "FitnessCheckActivity.ExtraColorSecondary";
    public static final String EXTRA_PRINCIPAL_TEXT_COLOR = "FitnessCheckActivity.ExtraPrincipalTextColor";
    public static final String EXTRA_SECONDARY_TEXT_COLOR = "FitnessCheckActivity.ExtraSecondaryTextColor";
    public static final String EXTRA_ACTION_FAILED_COLOR = "FitnessCheckActivity.ExtraActionFailedColor";

    private static final int TIMER_PRECOUNTDOWN_DURATION = 5000;

    private FitnessCheckActiveTask task;
    private TaskResult taskResult;
    private int colorPrimary;
    private int colorPrimaryDark;
    private int colorSecondary;
    private int principalTextColor;
    private int secondaryTextColor;
    private int actionFailedColor;

    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 181;
    private Map<String, String> resultsMap = new HashMap<>();
    private long mStartTime;
    private long mEndTime;
    private ViewFlipper viewFlipper;
    private View fitnessCheckPage1;
    private View fitnessCheckPage2;
    private View fitnessCheckPage3;
    private View fitnessCheckPage4;
    private View fitnessCheckPage5;
    private View fitnessCheckPage6;
    private View fitnessCheckPage7;
    private TextView taskProgressPosition;
    private Button doneButton;
    private int totalNumberOfTaskSteps;
    private CountDownTimer preCountdownTimer;
    private boolean isPrecounterRunning;
    private CountDownTimer fitnessCountdownTimer;
    private boolean isFitnessCounterRunning;
    private CountDownTimer restCountdownTimer;
    private boolean isRestCounterRunning;
    private ImageView animatedIndicator;
    private TextToSpeech textToSpeech;
    private boolean isTtsAvailable;
    private boolean endOfTaskReached;
    private Button nextButton;

    public static Intent newIntent(Context context, FitnessCheckActiveTask task) {
        Intent intent = new Intent(context, FitnessCheckActivity.class);
        intent.putExtra(EXTRA_TASK, task);
        return intent;
    }

    public static void themeIntent(Intent intent,
                                   int colorPrimary,
                                   int colorPrimaryDark,
                                   int colorSecondary,
                                   int principalTextColor,
                                   int secondaryTextColor,
                                   int actionFailedColor) {
        intent.putExtra(EXTRA_COLOR_PRIMARY, colorPrimary);
        intent.putExtra(EXTRA_COLOR_PRIMARY_DARK, colorPrimaryDark);
        intent.putExtra(EXTRA_COLOR_SECONDARY, colorSecondary);
        intent.putExtra(EXTRA_PRINCIPAL_TEXT_COLOR, principalTextColor);
        intent.putExtra(EXTRA_SECONDARY_TEXT_COLOR, secondaryTextColor);
        intent.putExtra(EXTRA_ACTION_FAILED_COLOR, actionFailedColor);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setResult(RESULT_CANCELED);
        super.setContentView(R.layout.rsb_activity_fitness_check);

        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (savedInstanceState == null) {
            task = (FitnessCheckActiveTask) getIntent().getSerializableExtra(EXTRA_TASK);
            colorPrimary = getIntent().getIntExtra(EXTRA_COLOR_PRIMARY, R.color.rsb_colorPrimary);
            colorPrimaryDark = getIntent().getIntExtra(EXTRA_COLOR_PRIMARY_DARK, R.color.rsb_colorPrimaryDark);
            colorSecondary = getIntent().getIntExtra(EXTRA_COLOR_SECONDARY, R.color.rsb_colorAccent);
            principalTextColor = getIntent().getIntExtra(EXTRA_PRINCIPAL_TEXT_COLOR, R.color.rsb_cell_header_grey);
            secondaryTextColor = getIntent().getIntExtra(EXTRA_SECONDARY_TEXT_COLOR, R.color.rsb_item_text_grey);
            actionFailedColor = getIntent().getIntExtra(EXTRA_ACTION_FAILED_COLOR, R.color.rsb_error);
            taskResult = (TaskResult) getIntent().getExtras().get(EXTRA_TASK_RESULT);
            if (taskResult == null) {
                taskResult = new TaskResult(task.getIdentifier());
            }
            taskResult.setStartDate(new Date());
        } else {
            task = (FitnessCheckActiveTask) savedInstanceState.getSerializable(EXTRA_TASK);
            taskResult = (TaskResult) savedInstanceState.getSerializable(EXTRA_TASK_RESULT);
        }

        taskProgressPosition = findViewById(R.id.rsb_fitness_check_position);

        doneButton = findViewById(R.id.rsb_fitness_check_done_button);
        doneButton.setBackground(ThemeUtils.getPrincipalColorButtonDrawable(this, colorPrimary));
        doneButton.setTextColor(ThemeUtils.getWhiteToSecondaryColorStateList(this, secondaryTextColor));
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskResult.setEndDate(new Date());
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_TASK_RESULT, taskResult);
                resultIntent.putExtra("TEST_FOR_REST", "wtflolbbq");
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        nextButton = findViewById(R.id.rsb_fitness_check_next_button);
        nextButton.setBackground(ThemeUtils.getPrincipalColorButtonDrawable(this, colorPrimary));
        nextButton.setTextColor(ThemeUtils.getWhiteToSecondaryColorStateList(this, secondaryTextColor));
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewFlipper.setInAnimation(FitnessCheckActivity.this, R.anim.rsb_slide_in_right);
                viewFlipper.setOutAnimation(FitnessCheckActivity.this, R.anim.rsb_slide_out_left);
                viewFlipper.showNext();
                updateTaskProgressIndicator();
            }
        });

        final ImageView backButton = findViewById(R.id.rsb_fitness_check_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewFlipper.setInAnimation(FitnessCheckActivity.this, R.anim.rsb_slide_in_left);
                viewFlipper.setOutAnimation(FitnessCheckActivity.this, R.anim.rsb_slide_out_right);
                viewFlipper.showPrevious();
                updateTaskProgressIndicator();
            }
        });

        Button cancelButton = findViewById(R.id.rsb_fitness_check_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        int walkingMinutes = task.getWalkDuration() / 60;

        final TextView fitnessTitle = findViewById(R.id.rsb_fitness_check_title);

        TextView step1Text = findViewById(R.id.rsb_fitness_check_step1_text);
        if (task.getIntendedUse() != null && !task.getIntendedUse().isEmpty()) {
            step1Text.setText(task.getIntendedUse());
        }

        ImageViewCompat.setImageTintList(((ImageView) findViewById(R.id.rsb_fitness_check_step1_heart)),
                ColorStateList.valueOf(colorPrimary));
        ImageViewCompat.setImageTintList(((ImageView) findViewById(R.id.rsb_fitness_check_step2_pocket_phone)),
                ColorStateList.valueOf(colorPrimary));
        ImageViewCompat.setImageTintList(((ImageView) findViewById(R.id.rsb_fitness_check_step3_walker)),
                ColorStateList.valueOf(colorPrimary));
        ImageViewCompat.setImageTintList(((ImageView) findViewById(R.id.rsb_fitness_check_step5_walker)),
                ColorStateList.valueOf(colorPrimary));
        ImageViewCompat.setImageTintList(((ImageView) findViewById(R.id.rsb_fitness_check_step6_sitter)),
                ColorStateList.valueOf(colorPrimary));

        TextView step3Text = findViewById(R.id.rsb_fitness_check_step3_text);
        step3Text.setText(getString(R.string.rsb_fitness_check_step3_text, walkingMinutes));

        final TextView step4CounterText = findViewById(R.id.rsb_fitness_check_step4_counter);
        final ProgressBar step4CounterProgress = findViewById(R.id.rsb_fitness_check_step4_progressbar);

        TextView step5Text = findViewById(R.id.rsb_fitness_check_step5_text);
        step5Text.setText(getString(R.string.rsb_fitness_check_step5_text, walkingMinutes));
        final TextView step5CountdownText = findViewById(R.id.rsb_fitness_check_step5_countdown_text);

        TextView step6Text = findViewById(R.id.rsb_fitness_check_step6_text);
        step6Text.setText(getString(R.string.rsb_fitness_check_step6_text, task.getRestDuration()));
        final TextView step6CountdownText = findViewById(R.id.rsb_fitness_check_step6_countdown_text);

        animatedIndicator = findViewById(R.id.checkmark_success);

        fitnessCheckPage1 = findViewById(R.id.rsb_viewflipper_fitness_step1);
        fitnessCheckPage2 = findViewById(R.id.rsb_viewflipper_fitness_step2);
        fitnessCheckPage3 = findViewById(R.id.rsb_viewflipper_fitness_step3);
        fitnessCheckPage4 = findViewById(R.id.rsb_viewflipper_fitness_step4);
        fitnessCheckPage5 = findViewById(R.id.rsb_viewflipper_fitness_step5);
        fitnessCheckPage6 = findViewById(R.id.rsb_viewflipper_fitness_step6);
        fitnessCheckPage7 = findViewById(R.id.rsb_viewflipper_fitness_step7);

        preCountdownTimer = new CountDownTimer(TIMER_PRECOUNTDOWN_DURATION, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
                isPrecounterRunning = true;
                long seconds = millisUntilFinished / 1000;
                double progressValue = ((double) millisUntilFinished / TIMER_PRECOUNTDOWN_DURATION) * 100;
                step4CounterProgress.setProgress((int) progressValue);
                step4CounterText.setText(String.valueOf(seconds + 1));
            }

            @Override
            public void onFinish() {
                viewFlipper.showNext();
                updateTaskProgressIndicator();
                isPrecounterRunning = false;
            }
        };

        fitnessCountdownTimer = new CountDownTimer(task.getWalkDuration() * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                isFitnessCounterRunning = true;
                long min = (millisUntilFinished / 60000) % 60;
                long sec = (millisUntilFinished / 1000) % 60;
                step5CountdownText.setText(getString(R.string.rsb_fitness_check_countdown_timer_text, min,
                        String.format(Locale.US, "%02d", sec)));
            }

            @Override
            public void onFinish() {
                if (task.getRestDuration() > 0) {
                    viewFlipper.showNext();
                } else {
                    viewFlipper.setDisplayedChild(6); // the 'rest' screen
                }
                updateTaskProgressIndicator();
                isFitnessCounterRunning = false;
            }
        };

        restCountdownTimer = new CountDownTimer(task.getRestDuration() * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                isRestCounterRunning = true;
                long min = (millisUntilFinished / 60000) % 60;
                long sec = (millisUntilFinished / 1000) % 60;
                if (task.getRestDuration() <= 60) {
                    step6CountdownText.setText(String.valueOf(sec));
                } else {
                    step6CountdownText.setText(getString(R.string.rsb_fitness_check_countdown_timer_text, min,
                            String.format(Locale.US, "%02d", sec)));
                }
            }

            @Override
            public void onFinish() {
                viewFlipper.showNext();
                updateTaskProgressIndicator();
                isRestCounterRunning = false;
            }
        };

        viewFlipper = findViewById(R.id.rsb_viewflipper_fitness_check);
        viewFlipper.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int leftPrevious,
                                       int topPrevious, int rightPrevious, int bottomPrevious) {

                nextButton.setText(viewFlipper.getCurrentView() == fitnessCheckPage2 ? getString(R.string.rsb_next) : getString(R.string.rsb_get_started));


                if (viewFlipper.getCurrentView() == fitnessCheckPage4) {
                    if (!isPrecounterRunning) {
                        preCountdownTimer.start();
                    }
                } else if (viewFlipper.getCurrentView() == fitnessCheckPage5) {
                    // start realdeal countdown
                    if (!isFitnessCounterRunning) {
                        if (isTtsAvailable) {
                            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                                textToSpeech.speak(getString(R.string.rsb_fitness_check_tts_directive_walk,
                                        getTimeFromSeconds(task.getWalkDuration())),
                                        TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }
                        fitnessCountdownTimer.start();
                    }
                } else if (viewFlipper.getCurrentView() == fitnessCheckPage6) {
                    // start rest countdown
                    if (!isRestCounterRunning) {
                        if (isTtsAvailable) {
                            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                                textToSpeech.speak(getString(R.string.rsb_fitness_check_tts_directive_rest,
                                        getTimeFromSeconds(task.getRestDuration())),
                                        TextToSpeech.QUEUE_FLUSH, null);
                            }
                        }
                        restCountdownTimer.start();
                    }
                } else if (viewFlipper.getCurrentView() == fitnessCheckPage7) {
                    if (!endOfTaskReached) {
                        endOfTaskReached = true;
                        initTimeRange(task.getWalkDuration());
                        readFitnessDataFromHistory();
                        fitnessTitle.setText(getString(R.string.rsb_fitness_check_task_complete));
                        doneButton.setVisibility(View.VISIBLE);
                        animatedIndicator.setVisibility(View.VISIBLE);
                        ((Animatable) animatedIndicator.getDrawable()).start();
                    }
                }

                if (viewFlipper.getCurrentView() == fitnessCheckPage2
                        || viewFlipper.getCurrentView() == fitnessCheckPage3) {
                    backButton.setVisibility(View.VISIBLE);
                } else {
                    backButton.setVisibility(View.INVISIBLE);
                }

                if (viewFlipper.getCurrentView() == fitnessCheckPage1
                        || viewFlipper.getCurrentView() == fitnessCheckPage2
                        || viewFlipper.getCurrentView() == fitnessCheckPage3) {
                    nextButton.setVisibility(View.VISIBLE);
                } else {
                    nextButton.setVisibility(View.GONE);
                }
            }
        });

        ImageViewCompat.setImageTintList(((ImageView) findViewById(R.id.checkmark_background)),
                ColorStateList.valueOf(colorSecondary));

        totalNumberOfTaskSteps = task.getRestDuration() > 0 ? viewFlipper.getChildCount() : viewFlipper.getChildCount
                () - 1;
        taskProgressPosition.setText(getString(R.string.rsb_fitness_check_position_indicator_text, 1,
                totalNumberOfTaskSteps));

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                isTtsAvailable = status != TextToSpeech.ERROR;
            }
        });

        buildFitnessClient();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    private void updateTaskProgressIndicator() {
        int currentFlipperChild = viewFlipper.getDisplayedChild() + 1;
        if (currentFlipperChild > totalNumberOfTaskSteps) {
            currentFlipperChild = totalNumberOfTaskSteps;
        }
        taskProgressPosition.setText(getString(R.string.rsb_fitness_check_position_indicator_text,
                currentFlipperChild, totalNumberOfTaskSteps));
    }

    private String getTimeFromSeconds(int seconds) {
        Resources res = getResources();
        long min = TimeUnit.SECONDS.toMinutes(seconds);
        long sec = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);

        if (min == 0) { // only return seconds
            return seconds + res.getQuantityString(R.plurals.rsb_seconds, seconds, seconds);
        } else {
            if (sec == 0) { // return only minutes
                return min + res.getQuantityString(R.plurals.rsb_minutes, (int) min, (int) min);
            } else { // return everything
                return min
                        + res.getQuantityString(R.plurals.rsb_minutes, (int) min, (int) min)
                        + getString(R.string.rsb_and)
                        + sec
                        + res.getQuantityString(R.plurals.rsb_seconds, (int) sec, (int) sec);
            }
        }
    }

    /**
     * Init history time range
     */
    private void initTimeRange(int durationSeconds) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        mEndTime = calendar.getTimeInMillis();
        calendar.add(Calendar.MINUTE, -(durationSeconds / 60));
        mStartTime = calendar.getTimeInMillis();

//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Log.e(TAG, "Range Start: " + dateFormat.format(mStartTime));
//        Log.e(TAG, "Range End: " + dateFormat.format(mEndTime));

    }

    /**
     * Authenticate the user and allow the application to connect to Fitness APIs.
     * The scopes included should match the scopes your app needs
     */
    private void buildFitnessClient() {
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .build();
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(this, GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this), fitnessOptions);
        } else {
            setupFitnessActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                setupFitnessActivity();
            }
        }
    }

    private void setupFitnessActivity() {
        subscribeFitnessRecording();
        viewFlipper.setVisibility(View.VISIBLE);
    }

    /**
     * Subscribe to fitness recorders
     */
    private void subscribeFitnessRecording() {

        // Subscribe to fit recorders
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                        Log.e(TAG, "Recording onSuccess()");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
//                        Log.e(TAG, "Recording onFailure()", e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> t) {
//                        Log.e(TAG, "Recording onComplete()");
                    }
                });

    }

    /**
     * Read fitness history
     */
    private void readFitnessDataFromHistory() {

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.HOURS)
//                .read(DataType.TYPE_STEP_COUNT_DELTA)
//                .read(DataType.TYPE_DISTANCE_DELTA)
                .setTimeRange(mStartTime, mEndTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        Log.e(TAG, "History onSuccess()");
                        parseData(dataReadResponse);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "History onFailure()", e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<DataReadResponse> task) {
                        Log.d(TAG, "History onComplete()");
                    }
                });

    }

    public void parseData(DataReadResponse dataReadResult) {
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned as buckets
        // containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.d(TAG, "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.d(TAG, "Number of returned DataSets is: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        } else {
            // TODO: no steps/distance were measured...pop a dialog?
            Toast.makeText(this, "No step data was collected!", Toast.LENGTH_SHORT).show();
        }

        StepResult result = new StepResult(new Step(task.getIdentifier()));
        result.setResult(resultsMap);
//        taskResult.setResults(resultsMap);
        taskResult.setStepResultForStepIdentifier(task.getIdentifier(), result);
    }

    /**
     * Get steps count from data points
     *
     * @param dataSet
     */
    private void dumpDataSet(DataSet dataSet) {

        Log.d(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (DataPoint dp : dataSet.getDataPoints()) {
//            int stepCount = 0;
            Log.d(TAG, "Data point:");
            Log.d(TAG, "\tType: " + dp.getDataType().getName());
            Log.d(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.d(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {

//                stepCount = dp.getValue(field).asInt();

                Log.d(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                resultsMap.put(field.getName(), dp.getValue(field).toString());
            }
        }
    }
}