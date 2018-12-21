package org.researchstack.backbone.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import java.util.List;
import org.researchstack.backbone.StorageAccess;
import org.researchstack.backbone.storage.file.StorageAccessListener;
import org.researchstack.backbone.utils.LogExt;

public class PinCodeActivity extends AppCompatActivity implements StorageAccessListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        new Handler().post(new Runnable() {
            @Override
            public void run() {
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
    }
}
