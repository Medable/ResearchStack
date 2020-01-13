package org.researchstack.backbone.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.ContextThemeWrapper;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;

import org.researchstack.backbone.BuildConfig;
import org.researchstack.backbone.R;
import org.researchstack.backbone.StorageAccess;
import org.researchstack.backbone.storage.file.PinCodeConfig;
import org.researchstack.backbone.storage.file.StorageAccessListener;
import org.researchstack.backbone.ui.views.PinCodeLayout;
import org.researchstack.backbone.utils.LogExt;
import org.researchstack.backbone.utils.ObservableUtils;
import org.researchstack.backbone.utils.ThemeUtils;

import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.functions.Action1;

import static org.researchstack.backbone.utils.LocaleUtils.getLocaleFromString;
import static org.researchstack.backbone.utils.LocaleUtils.getPreferredLocale;
import static org.researchstack.backbone.utils.LocaleUtils.wrapLocaleContext;

public class PinCodeActivity extends BaseActivity implements StorageAccessListener {

    private PinCodeLayout pinCodeLayout;
    private Action1<Boolean> toggleKeyboardAction;

    @Override
    protected void attachBaseContext(Context newBase) {
        String preferredLocale = getPreferredLocale(newBase);
        if (preferredLocale != null) {
            Locale locale = getLocaleFromString(preferredLocale);
            super.attachBaseContext(wrapLocaleContext(newBase, locale));
        } else {
            super.attachBaseContext(newBase);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BuildConfig.USE_SECURE_FLAG) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        LogExt.i(getClass(), "logAccessTime()");
        StorageAccess.getInstance().logAccessTime();
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestStorageAccess();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        storageAccessUnregister();
        if (pinCodeLayout != null) {
            getWindowManager().removeView(pinCodeLayout);
        }
    }

    protected void requestStorageAccess() {
        LogExt.i(getClass(), "requestStorageAccess()");
        StorageAccess storageAccess = StorageAccess.getInstance();
        storageAccessRegister();
        storageAccess.requestStorageAccess(this);
    }

    protected void storageAccessRegister() {
        LogExt.i(getClass(), "storageAccessRegister()");
        StorageAccess storageAccess = StorageAccess.getInstance();
        storageAccess.register(this);
    }

    protected void storageAccessUnregister() {
        LogExt.i(getClass(), "storageAccessUnregister()");
        StorageAccess storageAccess = StorageAccess.getInstance();
        storageAccess.unregister(this);
    }

    @Override
    public void onDataReady() {
        LogExt.i(getClass(), "onDataReady()");

        storageAccessUnregister();

        // this fixes the race condition where fragments from the viewpager weren't created yet
        // need a more permanent solution for notifying fragments of onDataReady() after creation
        new Handler().post(() -> {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if (fragments != null) {
                LogExt.i(getClass(),
                        "Fragments found on stack. Checking for StorageAccessListener.");

                for (Fragment fragment : fragments) {
                    if (fragment instanceof StorageAccessListener) {
                        LogExt.i(getClass(), "Notifying " + fragment.getClass().getSimpleName() +
                                " of onDataReady");

                        ((StorageAccessListener) fragment).onDataReady();
                    }
                }
            }
        });
    }

    @Override
    public void onDataFailed() {
        LogExt.e(getClass(), "onDataFailed()");

        storageAccessUnregister();

        List<Fragment> fragments = getSupportFragmentManager().getFragments();

        if (fragments != null) {
            LogExt.i(getClass(), "Fragments found on stack. Checking for StorageAccessListener.");

            for (Fragment fragment : fragments) {
                if (fragment instanceof StorageAccessListener) {
                    LogExt.i(getClass(), "Notifying " + fragment.getClass().getSimpleName() +
                            " of onDataFailed");

                    ((StorageAccessListener) fragment).onDataFailed();
                }
            }
        }
    }

    @Override
    public void onDataAuth() {
        LogExt.e(getClass(), "onDataAuth()");
        storageAccessUnregister();

        // Show pincode layout
        PinCodeConfig config = StorageAccess.getInstance().getPinCodeConfig();

        int theme = ThemeUtils.getPassCodeTheme(this);
        pinCodeLayout = new PinCodeLayout(new ContextThemeWrapper(this, theme));
        pinCodeLayout.setBackgroundColor(Color.WHITE);

        int errorColor = getResources().getColor(R.color.rsb_error);

        TextView summary = pinCodeLayout.findViewById(R.id.text);
        EditText pincode = pinCodeLayout.findViewById(R.id.pincode);

        toggleKeyboardAction = enable -> {
            pincode.setEnabled(enable);
            pincode.setText("");
            pincode.requestFocus();
            if (enable) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(pincode, InputMethodManager.SHOW_FORCED);
            }
        };

        RxTextView.textChanges(pincode).map(CharSequence::toString).doOnNext(pin -> {
            if (summary.getCurrentTextColor() == errorColor) {
                summary.setTextColor(ThemeUtils.getTextColorPrimary(PinCodeActivity.this));
                pinCodeLayout.resetSummaryText();
            }
        }).filter(pin -> pin != null && pin.length() == config.getPinLength()).doOnNext(pin -> {
            pincode.setEnabled(false);
            pinCodeLayout.showProgress(true);
        }).flatMap(pin -> Observable.fromCallable(() -> {
            StorageAccess.getInstance().authenticate(PinCodeActivity.this, pin);
            return true;
        }).compose(ObservableUtils.applyDefault()).doOnError(throwable -> {
            toggleKeyboardAction.call(true);
            throwable.printStackTrace();
            summary.setText(R.string.rsb_pincode_enter_error);
            summary.setTextColor(errorColor);
            pinCodeLayout.showProgress(false);
        }).onErrorResumeNext(throwable1 -> {
            return Observable.empty();
        })).subscribe(success -> {
            if (!success) {
                toggleKeyboardAction.call(true);
            } else {
                getWindowManager().removeView(pinCodeLayout);
                pinCodeLayout = null;
                // authenticate() no longer calls notifyReady(), call this after auth
                requestStorageAccess();
            }
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        getWindowManager().addView(pinCodeLayout, params);

        // Show keyboard, needs to be delayed, not sure why
        pinCodeLayout.postDelayed(() -> toggleKeyboardAction.call(true), 300);
    }
}
