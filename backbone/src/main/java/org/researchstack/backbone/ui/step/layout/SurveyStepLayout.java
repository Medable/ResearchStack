package org.researchstack.backbone.ui.step.layout;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.researchstack.backbone.R;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.QuestionStep;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.ui.callbacks.StepCallbacks;
import org.researchstack.backbone.ui.step.body.BodyAnswer;
import org.researchstack.backbone.ui.step.body.StepBody;
import org.researchstack.backbone.ui.views.FixedSubmitBarLayout;
import org.researchstack.backbone.ui.views.SubmitBar;
import org.researchstack.backbone.utils.LogExt;

import java.lang.reflect.Constructor;

public class SurveyStepLayout extends FixedSubmitBarLayout implements StepLayout {
    public static final String TAG = SurveyStepLayout.class.getSimpleName();

    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // Data used to initializeLayout and return
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private QuestionStep questionStep;
    private StepResult stepResult;

    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // Communicate w/ host
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private StepCallbacks callbacks;

    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // Child Views
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private LinearLayout container;
    private StepBody stepBody;

    private int coloryPrimary;
    private int colorSecondary;
    private int principalTextColor;
    private int secondaryTextColor;

    public SurveyStepLayout(Context context) {
        super(context);
    }

    public SurveyStepLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SurveyStepLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initialize(Step step) {
        initialize(step, null);
    }

    @Override
    public void initialize(Step step, StepResult result) {
        if (!(step instanceof QuestionStep)) {
            throw new RuntimeException("Step being used in SurveyStep is not a QuestionStep");
        }

        this.questionStep = (QuestionStep) step;
        this.stepResult = result;

        initializeStep();
    }

    public void initialize(Step step, StepResult result, int colorPrimary, int colorSecondary, int principalTextColor, int secondaryTextColor) {
        if (!(step instanceof QuestionStep)) {
            throw new RuntimeException("Step being used in SurveyStep is not a QuestionStep");
        }

        this.coloryPrimary = colorPrimary;
        this.colorSecondary = colorSecondary;
        this.principalTextColor = principalTextColor;
        this.secondaryTextColor = secondaryTextColor;

        this.questionStep = (QuestionStep) step;
        this.stepResult = result;

        initializeStep();
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
        callbacks.onSaveStep(StepCallbacks.ACTION_PREV, getStep(), stepBody.getStepResult(false));
        return false;
    }

    @Override
    public void setCallbacks(StepCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public int getContentResourceId() {
        return R.layout.rsb_step_layout;
    }

    public void initializeStep() {
        initStepLayout();
        initStepBody();
    }

    public void initStepLayout() {
        LogExt.i(getClass(), "initStepLayout()");

        container = findViewById(R.id.rsb_survey_content_container);
        final SubmitBar submitBar = findViewById(R.id.rsb_submit_bar);
        submitBar.setNegativeTitleColor(coloryPrimary);
        submitBar.setPositiveTitleColor(colorSecondary);
        submitBar.setPositiveAction(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(onNextClicked())
                {
                    submitBar.clearActions();
                }
            }
        });

        if (questionStep != null) {
            if (questionStep.isOptional()) {
                submitBar.setNegativeTitle(R.string.rsb_step_skip);
                submitBar.setNegativeAction(v -> onSkipClicked());
            } else {
                submitBar.getNegativeActionView().setVisibility(View.GONE);
            }
        }
    }

    public void initStepBody() {
        LogExt.i(getClass(), "initStepBody()");

        LayoutInflater inflater = LayoutInflater.from(getContext());
        stepBody = createStepBody(questionStep, stepResult);
        View body = stepBody.getBodyView(StepBody.VIEW_TYPE_DEFAULT, inflater, this);

        if (body != null) {
            View oldView = container.findViewById(R.id.rsb_survey_step_body);
            int bodyIndex = container.indexOfChild(oldView);
            container.removeView(oldView);
            container.addView(body, bodyIndex);
            body.setId(R.id.rsb_survey_step_body);
        }
    }

    @NonNull
    private StepBody createStepBody(QuestionStep questionStep, StepResult result) {
        try {
            Class cls = questionStep.getStepBodyClass();
            Constructor constructor = cls.getConstructor(Step.class, StepResult.class);
            return (StepBody) constructor.newInstance(questionStep, result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        callbacks.onSaveStep(StepCallbacks.ACTION_NONE, getStep(), stepBody.getStepResult(false));
        return super.onSaveInstanceState();
    }

    protected boolean onNextClicked() {
        BodyAnswer bodyAnswer = stepBody.getBodyAnswerState();

        if (bodyAnswer == null || !bodyAnswer.isValid()) {
            Toast.makeText(getContext(),
                    bodyAnswer == null
                            ? BodyAnswer.INVALID.getString(getContext())
                            : bodyAnswer.getString(getContext()),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            callbacks.onSaveStep(StepCallbacks.ACTION_NEXT,
                    getStep(),
                    stepBody.getStepResult(false));
            return true;
        }
    }

    public void onSkipClicked() {
        if (callbacks != null) {
            // empty step result when skipped
            callbacks.onSaveStep(StepCallbacks.ACTION_NEXT,
                    getStep(),
                    stepBody.getStepResult(true));
        }
    }

    public Step getStep() {
        return questionStep;
    }

    public String getString(@StringRes int stringResId) {
        return getResources().getString(stringResId);
    }
}
