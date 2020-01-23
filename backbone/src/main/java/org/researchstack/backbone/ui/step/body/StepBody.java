package org.researchstack.backbone.ui.step.body;

import androidx.lifecycle.MutableLiveData;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.researchstack.backbone.result.StepResult;

public interface StepBody {
    int VIEW_TYPE_DEFAULT = 0;

    int VIEW_TYPE_COMPACT = 1;
    MutableLiveData<Boolean> isStepEmpty = new MutableLiveData<>();

    MutableLiveData<StepResult> modifiedStepResult = new MutableLiveData<>();

    View getBodyView(int viewType, LayoutInflater inflater, ViewGroup parent);

    StepResult getStepResult(boolean skipped);

    BodyAnswer getBodyAnswerState();

}
