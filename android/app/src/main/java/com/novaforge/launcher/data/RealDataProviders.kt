package com.novaforge.launcher.data

import android.Manifest
import android.app.usage.UsageStatsManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.novaforge.launcher.data.model.AgendaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

data class RealWeatherInfo(
    val temperature: String = "26°",
    val description: String = "Clear Sky",
    val city: String = "Current Location",
    val tempRange: String = "28° / 16°",
    val isAvailable: Boolean = true,
    val isDemoData: Boolean = false
)

data class ScreenTimeSummary(
    val totalTimeFormatted: String = "0h 0m",
    val totalTimeMinutes: Long = 0,
    val topAppUsage: List<Pair<String, String>> = emptyList(),
    val hasPermission: Boolean = false,
    val isDemoData: Boolean = false
)

/**
 * Open-Meteo Integration with configurable coordinates and location name.
 */
class WeatherRepository {
    suspend fun fetchWeather(
        latitude: Double = 28.6139,
        longitude: Double = 77.2090,
        cityName: String = "New Delhi"
    ): RealWeatherInfo = withContext(Dispatchers.IO) {
        try {
            val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min&timezone=auto"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            val stream = conn.inputStream
            val text = stream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val json = JSONObject(text)
            val current = json.getJSONObject("current")
            val temp = current.getDouble("temperature_2m").toInt()
            val code = current.getInt("weather_code")

            val daily = json.getJSONObject("daily")
            val maxTemp = daily.getJSONArray("temperature_2m_max").getDouble(0).toInt()
            val minTemp = daily.getJSONArray("temperature_2m_min").getDouble(0).toInt()

            val desc = when (code) {
                0 -> "Clear Sky"
                1, 2, 3 -> "Partly Cloudy"
                45, 48 -> "Foggy"
                51, 53, 55 -> "Drizzle"
                61, 63, 65 -> "Rain"
                71, 73, 75 -> "Snow"
                80, 81, 82 -> "Rain Showers"
                95 -> "Thunderstorm"
                else -> "Clear"
            }

            RealWeatherInfo(
                temperature = "$temp°",
                description = desc,
                city = cityName,
                tempRange = "$maxTemp° / $minTemp°",
                isAvailable = true,
                isDemoData = false
            )
        } catch (e: Exception) {
            RealWeatherInfo(
                temperature = "26°",
                description = "Offline (Demo Display)",
                city = cityName,
                tempRange = "28° / 16°",
                isAvailable = false,
                isDemoData = true
            )
        }
    }
}

/**
 * Android Calendar Provider reader with runtime permission validation.
 */
class CalendarRepository(private val context: Context) {

    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getUpcomingEvents(): List<AgendaItem> = withContext(Dispatchers.IO) {
        if (!hasCalendarPermission()) {
            return@withContext emptyList()
        }

        val events = mutableListOf<AgendaItem>()
        val contentResolver: ContentResolver = context.contentResolver

        val now = System.currentTimeMillis()
        val endOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART
        )

        val selection = "(${CalendarContract.Events.DTSTART} >= ?) AND (${CalendarContract.Events.DTSTART} <= ?)"
        val selectionArgs = arrayOf(now.toString(), endOfDay.toString())

        try {
            val cursor: Cursor? = contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Events.DTSTART} ASC"
            )

            cursor?.use {
                val idCol = it.getColumnIndex(CalendarContract.Events._ID)
                val titleCol = it.getColumnIndex(CalendarContract.Events.TITLE)
                val startCol = it.getColumnIndex(CalendarContract.Events.DTSTART)

                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

                while (it.moveToNext()) {
                    val id = it.getLong(idCol).toString()
                    val title = it.getString(titleCol) ?: "Untitled Event"
                    val startTime = it.getLong(startCol)
                    val timeStr = timeFormat.format(Date(startTime))

                    events.add(AgendaItem(id = id, title = title, time = timeStr, isDemoData = false))
                }
            }
        } catch (e: SecurityException) {
            // Handled
        }

        events
    }
}

/**
 * UsageStatsManager integration for real screen time metrics.
 */
class ScreenTimeRepository(private val context: Context) {

    fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    fun getUsagePermissionIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }

    fun getDailyUsageStats(): ScreenTimeSummary {
        val hasPerm = hasUsagePermission()
        if (!hasPerm) {
            // Return demo data with isDemoData = true
            return ScreenTimeSummary(
                totalTimeFormatted = "4h 32m",
                totalTimeMinutes = 272,
                topAppUsage = listOf("YouTube" to "1h 12m", "Chrome" to "52m", "WhatsApp" to "28m"),
                hasPermission = false,
                isDemoData = true
            )
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return ScreenTimeSummary(hasPermission = false, isDemoData = true)

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (stats.isNullOrEmpty()) {
            return ScreenTimeSummary(hasPermission = true, isDemoData = false)
        }

        var totalMillis = 0L
        val appUsageList = mutableListOf<Pair<String, Long>>()

        for (usage in stats) {
            if (usage.totalTimeInForeground > 0) {
                totalMillis += usage.totalTimeInForeground
                val pkgName = usage.packageName
                val cleanName = pkgName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
                appUsageList.add(cleanName to usage.totalTimeInForeground)
            }
        }

        appUsageList.sortByDescending { it.second }

        val totalMinutes = totalMillis / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        val formattedTotal = "${hours}h ${minutes}m"

        val topApps = appUsageList.take(3).map { (name, millis) ->
            val m = millis / (1000 * 60)
            val h = m / 60
            val min = m % 60
            val timeText = if (h > 0) "${h}h ${min}m" else "${min}m"
            name to timeText
        }

        return ScreenTimeSummary(
            totalTimeFormatted = formattedTotal,
            totalTimeMinutes = totalMinutes,
            topAppUsage = topApps,
            hasPermission = true,
            isDemoData = false
        )
    }
}
