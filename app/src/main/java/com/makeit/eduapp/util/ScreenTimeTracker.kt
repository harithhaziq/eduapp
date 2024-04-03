package com.makeit.eduapp.util

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

object ScreenTimeTracker {

    private const val TAG = "ScreenTimeTracker"

    fun getTotalScreenTime(context: Context): Long {
        var totalScreenTime: Long = 0

        // Get the UsageStatsManager
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return totalScreenTime

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0) // Set the hour to 0 (midnight)
        cal.set(Calendar.MINUTE, 0) // Set the minute to 0
        cal.set(Calendar.SECOND, 0) // Set the second to 0
        cal.set(Calendar.MILLISECOND, 0) // Set the millisecond to 0 (start of the day)

        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()

        // Get the usage stats for the desired time period
        val usageStatsList =
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

        // Calculate total screen time
        usageStatsList?.let {
            for (usageStats in it) {
                totalScreenTime += usageStats.totalTimeInForeground
            }
        }

        return ((totalScreenTime / 60000) - 300)
    }
}