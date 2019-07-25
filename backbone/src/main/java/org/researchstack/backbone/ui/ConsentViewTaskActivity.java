package org.researchstack.backbone.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.researchstack.backbone.R;
import org.researchstack.backbone.answerformat.BirthDateAnswerFormat;
import org.researchstack.backbone.answerformat.DateAnswerFormat;
import org.researchstack.backbone.answerformat.TextAnswerFormat;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.ConsentDocumentStep;
import org.researchstack.backbone.step.ConsentSignatureStep;
import org.researchstack.backbone.step.FormStep;
import org.researchstack.backbone.step.QuestionStep;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.task.Task;
import org.researchstack.backbone.ui.callbacks.StepCallbacks;
import org.researchstack.backbone.utils.LocaleUtils;
import org.researchstack.backbone.utils.RSHTMLPDFWriter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.researchstack.backbone.ui.step.layout.ConsentSignatureStepLayout.KEY_SIGNATURE;

public class ConsentViewTaskActivity extends ViewTaskActivity implements StepCallbacks {

    private static final String ID_FORM_FIRST_NAME = "user_info_form_first_name";
    private static final String ID_FORM_LAST_NAME = "user_info_form_last_name";
    private static final String ID_FORM_DOB = "user_info_form_dob";
    private static final String ID_FORM = "user_info_form";

    private static final String EXTRA_ASSETS_FOLDER = "extra_assets_folder";

    private String consentHtml;
    private String firstName;
    private String lastName;
    private String signatureBase64;

    public static Intent newIntent(Context context, Task task, String assetsFolder) {
        Intent intent = new Intent(context, ConsentViewTaskActivity.class);
        intent.putExtra(EXTRA_TASK, task);
        intent.putExtra(EXTRA_ASSETS_FOLDER, assetsFolder);
        return intent;
    }

    @Override
    public void onSaveStep(int action, Step step, StepResult result) {

        if(step instanceof FormStep && step.getIdentifier().equalsIgnoreCase(ID_FORM))
        {
            for (QuestionStep question : ((FormStep) step).getFormSteps())
            {
                if(question.getIdentifier().equalsIgnoreCase(ID_FORM_FIRST_NAME))
                {
                    StepResult nameResult = (StepResult) result.getResultForIdentifier(ID_FORM_FIRST_NAME);
                    if(nameResult != null && nameResult.getResult() != null)
                    {
                        firstName = (String) nameResult.getResult();
                    }
                }
                if(question.getIdentifier().equalsIgnoreCase(ID_FORM_LAST_NAME))
                {
                    StepResult nameResult = (StepResult) result.getResultForIdentifier(ID_FORM_LAST_NAME);
                    if(nameResult != null && nameResult.getResult() != null)
                    {
                        lastName = (String) nameResult.getResult();
                    }
                }
            }
        }
        if(step instanceof ConsentSignatureStep)
        {
            signatureBase64 = (String) result.getResultForIdentifier(KEY_SIGNATURE);
        }
        else if(step instanceof ConsentDocumentStep)
        {
            consentHtml = ((ConsentDocumentStep) step).getConsentHTML();
        }

        super.onSaveStep(action, step, result);
    }

    private String getFormalName(String firstName, String lastName)
    {
        String completeName = null;
        if(lastName != null && firstName != null) completeName = lastName+", "+firstName;
        else if (firstName != null) completeName = firstName;
        else completeName = lastName;
        return completeName;
    }

    @Override
    protected void saveAndFinish() {
        // you can also set title / message
        final AlertDialog dialog = new ProgressDialog.Builder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.rsb_saving_consent))
                .setMessage(getString(R.string.rsb_please_wait))
                .create();

        dialog.show();

        String consentAssetsFolder = getIntent().getStringExtra(EXTRA_ASSETS_FOLDER);
        String role = getString(R.string.rsb_consent_role);
        String dateFormat = getString(R.string.rsb_consent_doc_line_date_format);
        consentHtml += getSignatureHtmlContent(getFormalName(firstName, lastName),
                role,
                signatureBase64,
                new SimpleDateFormat(dateFormat).format(new Date())
        );

        new PDFWriteExposer().printPdfFile(this, getCurrentTaskId(), consentHtml, consentAssetsFolder, () -> {
                    dialog.dismiss();
                    ConsentViewTaskActivity.super.saveAndFinish();
                }
        );
    }


    /**
     * Returns the personal form steps related with the consent view task.
     *
     * This method doesn't support multilanguage. If used, it will just print the filed names in English.
     * Please use the method with the new signature
     * {@link #getConsentPersonalInfoFormStep(Context context, boolean requiresName, boolean requiresBirthDate) getConsentPersonalInfoFormStep}
     */
    @Deprecated
    public static @Nullable FormStep getConsentPersonalInfoFormStep(boolean requiresName, boolean requiresBirthDate)
    {
        return getConsentPersonalInfoFormStep(null, requiresName, requiresBirthDate);
    }

    /**
     * Returns the personal form steps related with the consent view task.
     *
     * This new version is prepared to work with multilanguage.
     * Please use this method instead of
     * {@link #getConsentPersonalInfoFormStep(boolean requiresName, boolean requiresBirthDate) getConsentPersonalInfoFormStep}
     */
    public static @Nullable FormStep getConsentPersonalInfoFormStep(Context context, boolean requiresName,
                                                                    boolean requiresBirthDate)
    {
        if (requiresName || requiresBirthDate)
        {
            List<QuestionStep> formSteps = new ArrayList<>();
            if (requiresName)
            {
                String firstName = (context != null) ? LocaleUtils.getLocalizedString(context, R.string.rsb_name_first) : "First Name";
                formSteps.add(new QuestionStep(ID_FORM_FIRST_NAME, firstName, new TextAnswerFormat()));

                String lastName = (context != null) ? LocaleUtils.getLocalizedString(context,R.string.rsb_name_last) : "Last Name";
                formSteps.add(new QuestionStep(ID_FORM_LAST_NAME, lastName, new TextAnswerFormat()));
            }

            if (requiresBirthDate)
            {
                Calendar maxDate = Calendar.getInstance();
                maxDate.add(Calendar.YEAR, -18);

                DateAnswerFormat dobFormat = new BirthDateAnswerFormat(null, 18, 0);
                String dobText = (context != null) ? LocaleUtils.getLocalizedString(context, R.string.rsb_consent_dob_full) : "Date of birth";
                formSteps.add(new QuestionStep(ID_FORM_DOB, dobText, dobFormat));
            }

            String formTitle = (context != null) ?  LocaleUtils.getLocalizedString(context, R.string.rsb_consent) : "Consent";
            FormStep formStep = new FormStep(ID_FORM, formTitle, "");
            formStep.setOptional(false);
            formStep.setFormSteps(formSteps);

            return formStep;
        }

        return null;
    }

    private String getSignatureHtmlContent(@Nullable String completeName, @NonNull String role, @Nullable String signatureB64,
                                           @Nullable String signatureDate) {
        StringBuffer body = new StringBuffer();

        String hr = "<hr align='left' width='100%' style='height:1px; border:none; color:#000; background-color:#000; margin-top: 5px; margin-bottom: 0px;'/>";

        String signatureElementWrapper = "<div class='sigbox'><div class='inbox'>%s</div></div>%s%s";
        String imageTag;

        List<String> signatureElements = new ArrayList<>();

        if (role == null) {
            throw new RuntimeException("Consent role cannot be empty");
        }

        // Signature
        if (completeName != null) {
            String base = getString(R.string.rsb_consent_doc_line_printed_name, role);
            String nameElement = String.format(signatureElementWrapper, completeName, hr, base);
            signatureElements.add(nameElement);
        }

        if (signatureB64 != null) {
            imageTag = "<img width='100%' alt='star' style='max-height:100px;max-width:200px;height:auto;width:auto;' " +
                    "src='data:image/png;base64," + signatureB64 + "'/>";
            String base = getString(R.string.rsb_consent_doc_line_signature, role);
            String signatureElement = String.format(signatureElementWrapper, imageTag, hr, base);
            signatureElements.add(signatureElement);
        }

        if (signatureElements.size() > 0) {
            String base = getString(R.string.rsb_consent_doc_line_date);
            String signatureElement = String.format(signatureElementWrapper, signatureDate, hr, base);
            signatureElements.add(signatureElement);
        }

        int numElements = signatureElements.size();

        if (numElements > 1) {
            body
                    .append("<table cellpadding='20px' width='100%'>")
                    .append("<tr>");

            for (String element : signatureElements) {
                body.append("<td style='vertical-align: bottom;width:33%;'>").append(String.format("<div>%s</div>", element))
                        .append("</td>");
            }

            body
                    .append("</tr>")
                    .append("</table>");

        } else if (numElements == 1) {
            body.append(String.format("<div width='200'>%@</div>", signatureElements.get(0)));
        }

        return body.toString();
    }

    class PDFWriteExposer extends RSHTMLPDFWriter {
        protected void printPdfFile(Activity context, final String taskId, String htmlConsentDocument, String assetsFolder,
                PDFFileReadyCallback callback) {
            super.printPdfFile(context, taskId, htmlConsentDocument, assetsFolder, callback);
        }
    }
}
