package org.researchstack.backbone.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

public class ActivityUtils {
    private ActivityUtils() {
    }

    public static Activity getActivity(Context context) {
        if (context == null) {
            return null;
        } else if (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            } else {
                return getActivity(((ContextWrapper) context).getBaseContext());
            }
        }
        return null;
    }
}
