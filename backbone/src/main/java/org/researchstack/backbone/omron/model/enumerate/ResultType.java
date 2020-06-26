package org.researchstack.backbone.omron.model.enumerate;

import androidx.annotation.StringRes;

import org.researchstack.backbone.R;

public enum ResultType {
    Success(R.string.rsb_omron_success),
    Failure(R.string.rsb_omron_failure);
    @StringRes
    int id;

    ResultType(@StringRes int id) {
        this.id = id;
    }

    @StringRes
    public int stringResId() {
        return this.id;
    }
}