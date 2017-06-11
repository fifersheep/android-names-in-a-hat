package uk.lobsterdoodle.namepicker.application

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

import java.lang.reflect.Field

import javax.inject.Inject
import javax.inject.Singleton

import uk.lobsterdoodle.namepicker.Log
import uk.lobsterdoodle.namepicker.analytics.GroupAnalytics
import uk.lobsterdoodle.namepicker.analytics.ScreenAnalytics
import uk.lobsterdoodle.namepicker.analytics.SelectionAnalytics
import uk.lobsterdoodle.namepicker.events.EventBus
import uk.lobsterdoodle.namepicker.selection.SelectionGridUseCase
import uk.lobsterdoodle.namepicker.selection.SelectionNamesUseCase
import uk.lobsterdoodle.namepicker.selection.SelectionUseCase
import uk.lobsterdoodle.namepicker.storage.ActiveGroupUseCase
import uk.lobsterdoodle.namepicker.storage.StorageUseCase

class AppService : Service() {

    @Inject lateinit var bus: EventBus

    @Inject lateinit var storageUseCase: StorageUseCase

    @Inject lateinit var selectionUseCase: SelectionUseCase

    @Inject lateinit var selectionGridUseCase: SelectionGridUseCase

    @Inject lateinit var selectionNamesUseCase: SelectionNamesUseCase

    @Inject lateinit var activeGroupUseCase: ActiveGroupUseCase

    @Inject lateinit var groupAnalytics: GroupAnalytics

    @Inject lateinit var screenAnalytics: ScreenAnalytics

    @Inject lateinit var selectionAnalytics: SelectionAnalytics

    private val binder: IBinder? = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        App[this].component().inject(this)
    }

    override fun onBind(intent: Intent) = binder

    private inner class LocalBinder : Binder()
}
