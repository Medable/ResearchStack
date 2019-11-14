package org.researchstack.backbone.ui.task

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.researchstack.backbone.R
import org.researchstack.backbone.task.Task
import org.researchstack.backbone.ui.PinCodeActivity
import org.researchstack.backbone.ui.permissions.PermissionListener
import org.researchstack.backbone.ui.permissions.PermissionMediator
import org.researchstack.backbone.ui.permissions.PermissionResult
import org.researchstack.backbone.ui.step.layout.StepLayout
import org.researchstack.backbone.ui.step.layout.SurveyStepLayout
import org.researchstack.backbone.ui.task.TaskViewModel.Companion.EXTRA_ACTION_FAILED_COLOR
import org.researchstack.backbone.ui.task.TaskViewModel.Companion.EXTRA_COLOR_PRIMARY
import org.researchstack.backbone.ui.task.TaskViewModel.Companion.EXTRA_COLOR_PRIMARY_DARK
import org.researchstack.backbone.ui.task.TaskViewModel.Companion.EXTRA_COLOR_SECONDARY
import org.researchstack.backbone.ui.task.TaskViewModel.Companion.EXTRA_PRINCIPAL_TEXT_COLOR
import org.researchstack.backbone.ui.task.TaskViewModel.Companion.EXTRA_SECONDARY_TEXT_COLOR
import org.researchstack.backbone.ui.task.TaskViewModel.Companion.EXTRA_TASK
import org.researchstack.backbone.utils.ViewUtils

class TaskActivity : PinCodeActivity(), PermissionMediator {

    private val viewModel: TaskViewModel by viewModel { parametersOf(intent) }
    private val navController by lazy { Navigation.findNavController(this, R.id.nav_host_fragment) }
    private var currentStepLayout: StepLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.rsb_activity_task)

        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        observe(viewModel.currentStepEvent) { showStep(it) }
        observe(viewModel.taskCompleted) { close(it) }

        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onPause() {
        hideKeyboard()

        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.rsb_activity_view_task_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.rsb_action_cancel) {
            MaterialDialog.Builder(this)
                .title(R.string.rsb_task_cancel_title)
                .content(R.string.rsb_task_cancel_text)
                .theme(Theme.LIGHT)
                .positiveColor(viewModel.colorPrimary)
                .negativeColor(viewModel.colorPrimary)
                .negativeText(R.string.rsb_cancel)
                .positiveText(R.string.rsb_task_cancel_positive)
                .onPositive { _, _ -> finish() }
                .show()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        viewModel.previousStep()
    }

    override fun onSupportNavigateUp(): Boolean {
        viewModel.previousStep()
        return false
    }

    override fun onDataReady() {
        super.onDataReady()

        viewModel.nextStep()
    }

    override fun onDataFailed() {
        super.onDataFailed()

        Toast.makeText(this, R.string.rsb_error_data_failed, Toast.LENGTH_LONG).show()
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun requestPermissions(vararg permissions: String?) {
        requestPermissions(permissions, STEP_PERMISSION_REQUEST)
    }

    // TODO: this should be handled by each fragment/step/type that needs any sort of permission
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STEP_PERMISSION_REQUEST) {
            // Save the fact that we requested this permission
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            permissions.forEach { preferences.edit().putBoolean(it, true).apply() }

            val result = PermissionResult(permissions, grantResults)
            val permissionListeners = ViewUtils.findViewsOf(findViewById(android.R.id.content), PermissionListener::class.java, true)

            for (listener in permissionListeners) {
                listener.onPermissionGranted(result)
            }

            // This was designed so the step's layout is some form of View/ViewGroup that implements the PermissionListener interface.
            // As it turns out, not all steps are created equal, and not all the implementations follow this structure.
            // RSLocationPermission doesn't extend any View/ViewGroup; it acts more like a custom View that inflates its own layout.
            // For this reason, we cannot simply search the view Hierarchy and obtain the Layout because it will not implement
            // the contract; we have to check if the current Layout reference (saved when created) does.
            if (currentStepLayout !is SurveyStepLayout) {
                return
            }

            val stepBody = (currentStepLayout as SurveyStepLayout).stepBody
            if (stepBody is PermissionListener) {
                (stepBody as PermissionListener).onPermissionGranted(result)
            }
        }
    }

    override fun checkIfShouldShowRequestPermissionRationale(permission: String): Boolean {
        // ShouldShowRequestPermissionRationale() will return false in these cases:
        // * You've never asked for the permission before
        // * The user has checked the 'never again' checkbox
        // * The permission has been disabled by policy (usually enterprise)
        // Therefore a flag must be stored once we requested it.
        // Source: https://stackoverflow.com/questions/33224432/android-m-anyway-to-know-if-a-user-has-chosen-never-to-show-the-grant-permissi?rq=1
        // Note: ianhanniballake is a Google employee working on Android (September 2019)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val wasRequestedInThePast = preferences.getBoolean(permission, false)

        // If the user requested this permission in the past, we can rely on the rationale flag.
        return if (!wasRequestedInThePast) {
            // the user never requested this permission (or we don't have records of it).
            true
        } else Build.VERSION.SDK_INT < Build.VERSION_CODES.M || shouldShowRequestPermissionRationale(permission)
    }

    private fun showStep(navigationEvent: StepNavigationEvent) {
        if (navigationEvent.isMovingForward) {
            navController.navigate(navigationEvent.step.destinationId)
        } else {
            navController.popBackStack()
        }

        supportActionBar?.title = viewModel.task.getTitleForStep(this, navigationEvent.step)
        setActivityTheme(viewModel.colorPrimary, viewModel.colorPrimaryDark)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isActive && imm.isAcceptingText) {
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }
    }

    private fun close(completed: Boolean) {

        if (completed) {
            val result = Intent().apply {
                putExtra(EXTRA_TASK_RESULT, viewModel.taskResult)
            }

            setResult(Activity.RESULT_OK, result)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }

        finish()
    }

    private fun setActivityTheme(primaryColor: Int, primaryColorDark: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (primaryColorDark == Color.BLACK && window.navigationBarColor == Color.BLACK) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            }

            window.statusBarColor = primaryColorDark
        }

        supportActionBar?.setBackgroundDrawable(ColorDrawable(primaryColor))
    }

    private fun <T> observe(liveData: LiveData<T?>, lambda: (T) -> Unit) {
        liveData.observe(this, Observer { if (it != null) lambda(it) })
    }

    companion object {
        const val EXTRA_TASK_RESULT = "TaskActivity.ExtraTaskResult"
        private const val STEP_PERMISSION_REQUEST = 44

        fun newIntent(context: Context, task: Task): Intent {
            return Intent(context, TaskActivity::class.java).apply {
                putExtra(EXTRA_TASK, task)
            }
        }

        fun themeIntent(
            intent: Intent,
            colorPrimary: Int,
            colorPrimaryDark: Int,
            colorSecondary: Int,
            principalTextColor: Int,
            secondaryTextColor: Int,
            actionFailedColor: Int
        ) {
            with(intent) {
                putExtra(EXTRA_COLOR_PRIMARY, colorPrimary)
                putExtra(EXTRA_COLOR_PRIMARY_DARK, colorPrimaryDark)
                putExtra(EXTRA_COLOR_SECONDARY, colorSecondary)
                putExtra(EXTRA_PRINCIPAL_TEXT_COLOR, principalTextColor)
                putExtra(EXTRA_SECONDARY_TEXT_COLOR, secondaryTextColor)
                putExtra(EXTRA_ACTION_FAILED_COLOR, actionFailedColor)
            }
        }
    }

}