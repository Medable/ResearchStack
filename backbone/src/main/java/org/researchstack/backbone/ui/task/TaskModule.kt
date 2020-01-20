package org.researchstack.backbone.ui.task

import android.content.Intent
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.researchstack.backbone.utils.LocalBroadcaster

val taskActivityModule = module {
    viewModel { (intent : Intent) -> TaskViewModel(get(), intent) }
    factory { (activity: TaskActivity) -> LocalBroadcaster(get(), lifecycleOwner =  activity) }
}