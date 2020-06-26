package org.researchstack.backbone.ui.step.layout;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import org.researchstack.backbone.R;
import org.researchstack.backbone.omron.controller.ScanController;
import org.researchstack.backbone.omron.controller.SessionController;
import org.researchstack.backbone.omron.controller.util.Common;
import org.researchstack.backbone.omron.model.entity.DiscoveredDevice;
import org.researchstack.backbone.omron.model.entity.SessionData;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.FormStep;
import org.researchstack.backbone.step.QuestionStep;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.ui.callbacks.StepCallbacks;
import org.researchstack.backbone.ui.permissions.PermissionListener;
import org.researchstack.backbone.ui.permissions.PermissionMediator;
import org.researchstack.backbone.ui.permissions.PermissionResult;
import org.researchstack.backbone.ui.views.SubmitBar;
import org.researchstack.backbone.utils.ActivityUtils;
import org.researchstack.backbone.utils.BluetoothUtils;
import org.researchstack.backbone.utils.LocalizationUtils;
import org.researchstack.backbone.utils.ResearchStackPrefs;
import org.researchstack.backbone.utils.ThemeUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.ohq.androidcorebluetooth.CBConfig;
import jp.co.ohq.ble.OHQConfig;
import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQDetailedState;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.ble.enumerate.OHQMeasurementRecordKey;
import jp.co.ohq.ble.enumerate.OHQSessionOptionKey;
import jp.co.ohq.utility.Bundler;
import jp.co.ohq.utility.Handler;

public class OmronBPStepLayout extends RelativeLayout implements StepLayout, SessionController.Listener,
        PermissionListener {
    private static final String TAG = "OmronBPLayout";

    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // Data used to initializeLayout and return
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private FormStep step;
    private StepResult<StepResult> result;
    private Context context;

    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // Communicate w/ host
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private StepCallbacks callbacks;
    private SubmitBar submitBar;

    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    // Omron
    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*
    private static final long CONNECTION_WAIT_TIME = 60000;
    private ScanController scanController;
    private SessionController sessionController;
    private ImageView loadingAnimation;
    private View resultContainer;
    private TextView step1;
    private View stepDivider12;
    private TextView step2;
    private View stepDivider23;
    private TextView step3;
    private TextView status1;
    private TextView status2;
    private TextView status3;
    private TextView resultSystolic;
    private TextView resultDiastolic;
    private TextView resultPulseRate;
    private TextView stepInstructions;
    private String resultValueSystolic;
    private String resultValueDiastolic;
    private String resultValuePulseRate;
    private ConstraintLayout pairedDeviceContainer;
    private LinearLayout unpairedDeviceContainer;
    private ResearchStackPrefs rsbPrefs;
    private boolean firstRun;
    private boolean tryAgain = true;
    private int completedScans;
    private RelativeLayout locationPermissionErrorContainer;
    private TextView locationPermissionErrorText;
    private Button requestLocationPermissionButton;
    private enum DeviceConnectionStatus {
        Pairing,
        Connecting,
        Error,
        Reading,
        Done
    }

    private MediatorLiveData<Boolean> mediator = new MediatorLiveData<>();

    public OmronBPStepLayout(Context context) {
        super(context);
        this.context = context;
        inflateViews();
    }

    public OmronBPStepLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        inflateViews();
    }

    public OmronBPStepLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        inflateViews();
    }

    private void inflateViews() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.rsb_step_layout_omron_bp, this, true);
    }

    public void initialize(Step step) {
        initialize(step, null);
    }

    @Override
    public void initialize(Step step, StepResult result) {
        if (!(step instanceof QuestionStep)) {
            throw new RuntimeException("Step being used in SurveyStep is not a QuestionStep");
        }

        this.step = (FormStep) step;
        this.result = result == null ? new StepResult<>(step) : result;

        initStepLayout();
    }

    @Override
    public View getLayout() {
        return this;
    }

    /**
     * Method allowing a step to consume a back event.
     *
     * @return
     */
    @Override
    public boolean isBackEventConsumed() {
        return true;
    }

    @Override
    public void taskCancelled() {
        killOmronSession(false);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // onDestroy() called
        killOmronSession(false);
    }

    @Override
    public void enablePositiveAction() {
        submitBar.setPositiveActionEnabled(step.getColorSecondary());
    }

    @Override
    public void disablePositiveAction() {
        submitBar.setPositiveActionDisabled();
    }

    @Override
    public void setCallbacks(StepCallbacks callbacks) {
        this.callbacks = callbacks;

        callbacks.setActionbarVisible(false);
    }

    @Override
    public void setCancelEditMode(boolean isCancelEdit) {

    }

    @Override
    public void setRemoveFromBackStack(boolean removeFromBackStack) {

    }

    @Override
    public void isEditView(boolean isEditView) {

    }

    public void initStepLayout() {
        rsbPrefs = new ResearchStackPrefs(context);

        locationPermissionErrorContainer = findViewById(R.id.location_perms_error_container);
        locationPermissionErrorText = findViewById(R.id.location_perms_error_text);
        requestLocationPermissionButton = findViewById(R.id.button_open_app_settings);

        requestLocationPermissionButton.setOnClickListener(view -> {
            if (context instanceof PermissionMediator
                    && ((PermissionMediator)context).checkIfShouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ((PermissionMediator) context).requestPermissions(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
        });

        if (context instanceof PermissionMediator
                && ((PermissionMediator)context).checkIfShouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ((PermissionMediator) context).requestPermissions(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        updateLayoutBasedUponLocationPermissionAndPresence();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        callbacks.onSaveStep(StepCallbacks.ACTION_NONE, getStep(), getStepResult());
        return super.onSaveInstanceState();
    }

    private void updateLayoutBasedUponLocationPermissionAndPresence() {
        if (needsToRequestPermissions()) {
            handleMissingLocationPermission();
        } else if (!BluetoothUtils.isBluetoothEnabled(context)) {
            showErrorDialog();
        } else {
            locationPermissionErrorContainer.setVisibility(GONE);

            TextView title = findViewById(R.id.title);
            submitBar = findViewById(R.id.submit_bar);
            submitBar.setNegativeTitleColor(step.getPrincipalTextColor());
            submitBar.setPositiveTitleColor(step.getSecondaryTextColor());
            submitBar.setPositiveAction(view -> {
                if (firstRun) {
                    if (scanController != null) {
                        ActivityUtils.getActivity(context).runOnUiThread(() -> submitBar.setPositiveActionDisabled());
                        setDeviceConnectionStep(DeviceConnectionStatus.Connecting);
                        scanController.startScan();
                    }
                } else {
                    killOmronSession(false);
                    callbacks.onSaveStep(StepCallbacks.ACTION_NEXT, step, getStepResult());
                    submitBar.clearActions();
                }
            });

            title.setText(step.getTitle());

            submitBar.getNegativeActionView().setVisibility(View.GONE);

            loadingAnimation = findViewById(R.id.loading_animation);
            loadingAnimation.setBackgroundResource(R.drawable.rsb_loading_animation);

            step1 = findViewById(R.id.step1);
            status1 = findViewById(R.id.measure_status1);
            stepDivider12 = findViewById(R.id.divider12);
            step2 = findViewById(R.id.step2);
            status2 = findViewById(R.id.measure_status2);
            stepDivider23 = findViewById(R.id.divider23);
            step3 = findViewById(R.id.step3);
            status3 = findViewById(R.id.measure_status3);
            resultSystolic = findViewById(R.id.result_systolic);
            resultDiastolic = findViewById(R.id.result_diastolic);
            resultPulseRate = findViewById(R.id.result_pulserate);
            stepInstructions = findViewById(R.id.step_instructions);
            resultContainer = findViewById(R.id.measure_result_container);
            pairedDeviceContainer = findViewById(R.id.paired_device_container);
            unpairedDeviceContainer = findViewById(R.id.unpaired_device_container);

            status1.setText(LocalizationUtils.getLocalizedString(context, R.string.rsb_omron_step_state_connecting));
            status2.setText(LocalizationUtils.getLocalizedString(context, R.string.rsb_omron_step_state_reading));
            status3.setText(LocalizationUtils.getLocalizedString(context, R.string.rsb_omron_step_state_done));
            stepInstructions.setText(LocalizationUtils.getLocalizedString(context, R.string.rsb_omron_step_stage_3_final_intructions));

            if (rsbPrefs.getOmronDeviceId() != null && !rsbPrefs.getOmronDeviceId().isEmpty()
                    && BluetoothUtils.isDevicePaired(rsbPrefs.getOmronDeviceId())) {
                submitBar.setPositiveActionDisabled();
                startSession(rsbPrefs.getOmronDeviceId());

            } else {
                setDeviceConnectionStep(OmronBPStepLayout.DeviceConnectionStatus.Pairing);
                submitBar.setPositiveActionEnabled(step.getColorSecondary());
                firstRun = true;

                scanController = new ScanController(new ScanController.Listener() {
                    @Override
                    public void onScan(@NonNull List<DiscoveredDevice> discoveredDevices) {
                        if (discoveredDevices.size() > 0) {
                            //Log.d(TAG, "onScan() found " + discoveredDevices.size() + " devices...");
                            scanController.stopScan();

                            // choose the strongest signal
                            Collections.sort(discoveredDevices, (d1, d2) -> d2.getRssi() - (d1.getRssi()));
                            String address = discoveredDevices.get(0).getAddress();
                            rsbPrefs.updateOmronDeviceId(address);

                            startSession(rsbPrefs.getOmronDeviceId());

                        } else if (completedScans >= 30) {
                            scanController.stopScan();
                            setLoadingVisible(false);
                            AlertDialog.Builder noDevicesFoundDialog = new AlertDialog.Builder(context);
                            noDevicesFoundDialog.setTitle(R.string.rsb_omron_no_devices_found_title);
                            noDevicesFoundDialog.setMessage(R.string.rsb_omron_no_devices_found_text);
                            final AlertDialog dialog = noDevicesFoundDialog.setPositiveButton(LocalizationUtils.getLocalizedString(context,
                                    R.string.rsb_omron_pairing_retry), (dialog1, id) -> {
                                dialog1.cancel();
                                setLoadingVisible(true);
                                completedScans = 0;
                                scanController.startScan();
                            }).create();

                            if (!((Activity) context).isFinishing()) {
                                dialog.show();
                            }
                        }
                        completedScans++;
                    }

                    @Override
                    public void onScanCompletion(@NonNull OHQCompletionReason reason) {

                    }
                });
                scanController.setFilteringDeviceCategory(OHQDeviceCategory.BloodPressureMonitor);
            }
        }
    }

    private void handleMissingLocationPermission() {
        locationPermissionErrorText.setText(LocalizationUtils.getLocalizedString(context,
                R.string.rsb_location_step_no_permission_error_text));
        locationPermissionErrorContainer.setVisibility(VISIBLE);
        requestLocationPermissionButton.setBackgroundColor(step.getPrimaryColor());
        requestLocationPermissionButton.setTextColor(ThemeUtils.getContrastColor(step.getPrimaryColor()));
        requestLocationPermissionButton.setVisibility(VISIBLE);
    }

    @Override
    public void onPermissionGranted(@NonNull final PermissionResult permissionResult) {
        updateLayoutBasedUponLocationPermissionAndPresence();
    }

    private boolean needsToRequestPermissions() {
        return (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    public Step getStep() {
        return step;
    }

    public String getString(@StringRes int stringResId) {
        return getResources().getString(stringResId);
    }

    public LiveData<Boolean> isStepEmpty() {
        return mediator;
    }

    public StepResult getStepResult() {
        for (QuestionStep questionStep : step.getFormSteps()) {
            StepResult stepResult = new StepResult(questionStep);
            switch (questionStep.getKey()) {
                case "4272559e-17f7-4268-96b4-04be6092ec86": // systolic keys
                case "e1fdb4f7-6fac-4633-9a1d-27aa1a2cc342":
                    stepResult.setResult(resultValueSystolic);
                    result.setResultForIdentifier(questionStep.getIdentifier(), stepResult);
                    break;
                case "220eef2c-9798-49f9-98cf-b6d9b2be4f3a": // diastolic keys
                case "19bac45d-686d-4e76-a473-3666547d540d":
                    stepResult.setResult(resultValueDiastolic);
                    result.setResultForIdentifier(questionStep.getIdentifier(), stepResult);
                    break;
                case "aac5de4f-3677-41e5-9662-43a2311ee890": // pulse keys
                case "19bdaa84-77c5-418b-8294-ad1ee836493f":
                    stepResult.setResult(resultValuePulseRate);
                    result.setResultForIdentifier(questionStep.getIdentifier(), stepResult);
                    break;
            }
        }
        return result;
    }

    private void setLoadingVisible(boolean visible) {
        AnimationDrawable anim = (AnimationDrawable) loadingAnimation.getBackground();
        if (visible) {
            anim.start();
        } else {
            anim.stop();
        }
        loadingAnimation.setVisibility(visible ? View.VISIBLE: View.GONE);
    }

    private void startSession(String address) {
        if (sessionController != null && sessionController.isInSession()) {
            //Log.d(TAG, "Session exists");
            return;
        }

        sessionController = new SessionController(this, null);

        final Handler handler = new Handler();
        handler.post(() -> {
            sessionController.setConfig(getConfig());
            Map<OHQSessionOptionKey, Object> options = new HashMap<>();
            options.put(OHQSessionOptionKey.ConnectionWaitTimeKey, CONNECTION_WAIT_TIME);
            options.put(OHQSessionOptionKey.ReadMeasurementRecordsKey, true);
            sessionController.startSession(address, options);
        });
    }

    @Override
    public void onSessionComplete(@NonNull SessionData sessionData) {
        if (OHQCompletionReason.Canceled == sessionData.getCompletionReason()) {
            return;
        }

        List<Map<OHQMeasurementRecordKey, Object>> measurementRecords = sessionData.getMeasurementRecords();

        if (measurementRecords != null && measurementRecords.size() > 0) {

            Map<OHQMeasurementRecordKey, Object> latestMeasurement = measurementRecords.get(0);

            resultValueSystolic = Common.getDecimalString((BigDecimal) latestMeasurement.get(OHQMeasurementRecordKey.SystolicKey), 0);
            resultValueDiastolic = Common.getDecimalString((BigDecimal) latestMeasurement.get(OHQMeasurementRecordKey.DiastolicKey), 0);
            resultValuePulseRate = Common.getDecimalString((BigDecimal) latestMeasurement.get(OHQMeasurementRecordKey.PulseRateKey), 0);

            resultSystolic.setText(LocalizationUtils.getLocalizedString(context, R.string.rsb_omron_result_systolic, resultValueSystolic));
            resultDiastolic.setText(LocalizationUtils.getLocalizedString(context, R.string.rsb_omron_result_diastolic, resultValueDiastolic));
            resultPulseRate.setText(LocalizationUtils.getLocalizedString(context, R.string.rsb_omron_result_pulserate, resultValuePulseRate));

            submitBar.setPositiveActionEnabled(step.getColorSecondary());
            setDeviceConnectionStep(OmronBPStepLayout.DeviceConnectionStatus.Done);
        } else {
            if (tryAgain) {
                killOmronSession(true);
                tryAgain = false;
            } else {
                setDeviceConnectionStep(OmronBPStepLayout.DeviceConnectionStatus.Error);
                showErrorDialog();
            }
        }
        ActivityUtils.getActivity(context).runOnUiThread(() -> loadingAnimation.setVisibility(View.GONE));
    }

    private void killOmronSession(boolean restartSession) {
        final Handler handler = new Handler();
        handler.post(() -> {
            if (sessionController != null && sessionController.isInSession()) {
                sessionController.cancel();
            }
            if (restartSession) {
                startSession(rsbPrefs.getOmronDeviceId());
            }
        });
    }

    @Override
    public void onDetailedStateChanged(@NonNull OHQDetailedState newState) {
        switch (newState) {
            case ConnectStarting:
                //Log.d(TAG, "onDetailedStateChanged (ConnectStarting)");
                ActivityUtils.getActivity(context).runOnUiThread(() -> setDeviceConnectionStep(OmronBPStepLayout.DeviceConnectionStatus.Connecting));
                break;
            case DescValueReading:
                if (!firstRun) {
                    ActivityUtils.getActivity(context).runOnUiThread(() -> setDeviceConnectionStep(OmronBPStepLayout.DeviceConnectionStatus.Reading));
                }
                break;
            case Disconnected:
                if (firstRun) {
//                    //Log.d(TAG, "Disconnected (first-run), starting session again... >>>>>>>>>>>>>>>>>>>>>>>>");
//                    killOmronSession(true);
                    firstRun = false;
                }
                break;
        }
    }

    private void setDeviceConnectionStep(OmronBPStepLayout.DeviceConnectionStatus status) {
        switch (status) {
            case Connecting:
                step1.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                step1.setBackground(ContextCompat.getDrawable(context, R.drawable.rsb_bg_omron_device_step_completed));
                break;
            case Reading:
                stepDivider12.setBackgroundColor(ContextCompat.getColor(context, R.color.rsb_omron_color_accent));
                step2.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                step2.setBackground(ContextCompat.getDrawable(context, R.drawable.rsb_bg_omron_device_step_completed));
                break;
            case Done:
                stepDivider23.setBackgroundColor(ContextCompat.getColor(context, R.color.rsb_omron_color_accent));
                step3.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                step3.setBackground(ContextCompat.getDrawable(context, R.drawable.rsb_bg_omron_device_step_completed));
                resultContainer.setVisibility(View.VISIBLE);
                break;
            case Error:
                resultContainer.setVisibility(View.INVISIBLE);
                break;
        }

        status1.setVisibility(status == DeviceConnectionStatus.Connecting ? View.VISIBLE : View.INVISIBLE);
        status2.setVisibility(status == DeviceConnectionStatus.Reading ? View.VISIBLE : View.INVISIBLE);
        status3.setVisibility(status == DeviceConnectionStatus.Done ? View.VISIBLE : View.INVISIBLE);
        pairedDeviceContainer.setVisibility(status != OmronBPStepLayout.DeviceConnectionStatus.Pairing ? View.VISIBLE : View.INVISIBLE);
        unpairedDeviceContainer.setVisibility(status == OmronBPStepLayout.DeviceConnectionStatus.Pairing ? View.VISIBLE : View.INVISIBLE);
        setLoadingVisible(status == OmronBPStepLayout.DeviceConnectionStatus.Connecting || status == OmronBPStepLayout.DeviceConnectionStatus.Reading);
    }

    @NonNull
    private Bundle getConfig() {
        return Bundler.bundle(
                OHQConfig.Key.CreateBondOption.name(), CBConfig.CreateBondOption.UsedBeforeGattConnection,
                OHQConfig.Key.RemoveBondOption.name(), CBConfig.RemoveBondOption.NotUse,
                OHQConfig.Key.AssistPairingDialogEnabled.name(), true,
                OHQConfig.Key.AutoPairingEnabled.name(), true,
                OHQConfig.Key.UseRefreshWhenDisconnect.name(), true
        );
    }

    private void showErrorDialog() {
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(context);
        errorDialog.setCancelable(false);
        errorDialog.setTitle(R.string.rsb_omron_error_title);
        errorDialog.setMessage(R.string.rsb_omron_error_text);
        final AlertDialog dialog = errorDialog.setPositiveButton(LocalizationUtils.getLocalizedString(context,
                R.string.rsb_ok), (dialog1, id) -> {
            dialog1.cancel();
            ActivityUtils.getActivity(context).finish();
        }).create();
        if (!((Activity) context).isFinishing()) {
            dialog.show();
        }
    }
}