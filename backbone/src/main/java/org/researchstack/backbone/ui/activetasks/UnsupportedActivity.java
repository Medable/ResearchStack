package org.researchstack.backbone.ui.activetasks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.researchstack.backbone.R;
import org.researchstack.backbone.ui.PinCodeActivity;

public class UnsupportedActivity extends PinCodeActivity {

    private static final String TAG = "UnsupportedActivity";
    public static final String EXTRA_TASK_TYPE = "FitnessCheckActivity.ExtraTaskType";
    public static final String EXTRA_COLOR_PRIMARY = "FitnessCheckActivity.ExtraColorPrimary";
    public static final String EXTRA_COLOR_PRIMARY_DARK = "FitnessCheckActivity.ExtraColorPrimaryDark";
    public static final String EXTRA_COLOR_SECONDARY = "FitnessCheckActivity.ExtraColorSecondary";
    public static final String EXTRA_PRINCIPAL_TEXT_COLOR = "FitnessCheckActivity.ExtraPrincipalTextColor";
    public static final String EXTRA_SECONDARY_TEXT_COLOR = "FitnessCheckActivity.ExtraSecondaryTextColor";
    public static final String EXTRA_ACTION_FAILED_COLOR = "FitnessCheckActivity.ExtraActionFailedColor";

    private String taskType;
    private int colorPrimary;
    private int colorPrimaryDark;
    private int colorSecondary;
    private int principalTextColor;
    private int secondaryTextColor;
    private int actionFailedColor;

    public static Intent newIntent(Context context, String taskType) {
        Intent intent = new Intent(context, UnsupportedActivity.class);
        intent.putExtra(EXTRA_TASK_TYPE, taskType);
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
        super.setContentView(R.layout.rsb_activity_unsupported_task);

        if (savedInstanceState == null) {
            taskType = getIntent().getSerializableExtra(EXTRA_TASK_TYPE).toString();
            colorPrimary = getIntent().getIntExtra(EXTRA_COLOR_PRIMARY, R.color.rsb_colorPrimary);
            colorPrimaryDark = getIntent().getIntExtra(EXTRA_COLOR_PRIMARY_DARK, R.color.rsb_colorPrimaryDark);
            colorSecondary = getIntent().getIntExtra(EXTRA_COLOR_SECONDARY, R.color.rsb_colorAccent);
            principalTextColor = getIntent().getIntExtra(EXTRA_PRINCIPAL_TEXT_COLOR, R.color.rsb_cell_header_grey);
            secondaryTextColor = getIntent().getIntExtra(EXTRA_SECONDARY_TEXT_COLOR, R.color.rsb_item_text_grey);
            actionFailedColor = getIntent().getIntExtra(EXTRA_ACTION_FAILED_COLOR, R.color.rsb_error);
        } else {
            taskType = savedInstanceState.getSerializable(EXTRA_TASK_TYPE).toString();

        }

        TextView unsupportedText = findViewById(R.id.rsb_unsupported_text);
        unsupportedText.setText(getString(R.string.rsb_unsupported_task_text,
                convertToTitleCase(taskType.replace('_',' '))));

        Button unsupportedButton = findViewById(R.id.rsb_unsupported_button);
        unsupportedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private String convertToTitleCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder converted = new StringBuilder();

        boolean convertNext = true;
        for (char ch : text.toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            converted.append(ch);
        }

        return converted.toString();
    }
}