package com.zerodev.subscriptionmanager.presentation.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object UpcomingWidgetHelper {

    /**
     * Triggers an immediate asynchronous update of all active Upcoming Payments widgets
     * by bumping the revision timestamp in Glance state and requesting an instant redraw.
     */
    fun updateWidget(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(UpcomingWidget::class.java)
                glanceIds.forEach { glanceId ->
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                        prefs.toMutablePreferences().apply {
                            this[UpcomingWidget.KeyLastUpdated] = System.currentTimeMillis()
                        }
                    }
                    UpcomingWidget().update(context, glanceId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
