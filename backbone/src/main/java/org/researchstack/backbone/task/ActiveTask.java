package org.researchstack.backbone.task;

import java.io.Serializable;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.ui.ViewTaskActivity;

/**
 * A task to be carried out by a participant in a research study.
 * <p>
 * To present the ResearchStack framework UI in your app, instantiate an object that extends the
 * Task class (such as {@link OrderedTask}) and provide it to a {@link
 * ViewTaskActivity}.
 * <p>
 * Implement this protocol to enable dynamic selection of the steps for a given task. By default,
 * OrderedTask implements this protocol for simple sequential tasks.
 * <p>
 * Each {@link Step} in a task roughly corresponds to one screen, and represents the primary unit of
 * work in any task. For example, a {@link org.researchstack.backbone.step.QuestionStep} object
 * corresponds to a single question presented on screen, together with controls the participant uses
 * to answer the question. Another example is {@link org.researchstack.backbone.step.FormStep},
 * which corresponds to a single screen that displays multiple questions or items for which
 * participants provide information, such as first name, last name, and birth date.
 */
public abstract class ActiveTask implements Serializable {
    private String identifier;

    /**
     * Class constructor specifying a unique identifier.
     *
     * @param identifier the task identifier, see {@link #getIdentifier()}
     */
    public ActiveTask(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the unique identifier for this task.
     * <p>
     * The identifier should be a short string that identifies the task. The identifier is copied
     * into the {@link TaskResult} objects generated  for this task. You can use a human-readable
     * string for the task identifier or a UUID; the exact string you use depends on your app.
     * <p>
     * In the case of apps whose tasks come from a server, the unique identifier for the task may be
     * in an external database.
     * <p>
     * The task identifier is used when constructing the task result. The identifier can also be
     * used during UI state restoration to identify the task that needs to be restored.
     *
     * @return the task identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Function that can be overridden in order to access the low level changes in the view.
     * The function is called at Activity lifecycle events (creation, pause, resume, stop and whenever
     * the content of the activity is changed, according to the step.
     *
     * @param type        lifecycle event
     * @param activity    current activity
     * @param currentStep the current step being shown
     */
    public void onViewChange(ViewChangeType type, ViewTaskActivity activity, Step currentStep) {

    }

    public static enum ViewChangeType {
        ActivityCreate,
        ActivityPause,
        ActivityResume,
        ActivityStop
    }
}