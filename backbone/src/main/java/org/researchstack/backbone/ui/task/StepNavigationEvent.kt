package org.researchstack.backbone.ui.task

import org.researchstack.backbone.step.Step

class StepNavigationEvent(val popUpToStep: Step? = null, val step: Step, val isMovingForward: Boolean = true)