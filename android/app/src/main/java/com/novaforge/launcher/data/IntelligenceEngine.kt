package com.novaforge.launcher.data

import com.novaforge.launcher.data.model.*
import java.util.Calendar

/**
 * NovaForge Intelligence Engine
 * Handles local workspace recommendations, rule evaluation, and macro action pipelines.
 * All intelligence computations run strictly on-device without external telemetry.
 */
class IntelligenceEngine {

    /**
     * Evaluates active automation rules and context to generate non-intrusive workspace suggestions.
     */
    fun evaluateSuggestions(
        rules: List<AutomationRule>,
        automationsEnabled: Boolean,
        activeWorkspaceId: String,
        batteryInfo: BatteryInfo,
        agendaItems: List<AgendaItem>
    ): WorkspaceSuggestion? {
        if (!automationsEnabled) return null

        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(Calendar.MINUTE)
        val currentTimeStr = String.format("%02d:%02d", currentHour, currentMinute)

        // 1. Evaluate user-configured rules
        for (rule in rules) {
            if (!rule.isEnabled) continue

            when (rule.triggerType) {
                "TIME_OF_DAY" -> {
                    val triggerTime = rule.triggerValue
                    val triggerHour = triggerTime.substringBefore(":").toIntOrNull() ?: 0
                    // Trigger within active hour window
                    if (currentHour == triggerHour && activeWorkspaceId != rule.actionValue) {
                        return WorkspaceSuggestion(
                            spaceId = rule.actionValue,
                            spaceName = rule.actionValue.replaceFirstChar { it.uppercase() },
                            reason = "${rule.title} (Scheduled time $triggerTime)"
                        )
                    }
                }
                "BATTERY_LEVEL" -> {
                    val threshold = rule.triggerValue.toIntOrNull() ?: 20
                    if (batteryInfo.percentage <= threshold && !batteryInfo.isCharging) {
                        return WorkspaceSuggestion(
                            spaceId = "battery",
                            spaceName = "Battery Saver",
                            reason = "Battery level at ${batteryInfo.percentage}% (Below ${threshold}%)"
                        )
                    }
                }
            }
        }

        // 2. Contextual calendar-driven heuristics
        if (agendaItems.isNotEmpty() && activeWorkspaceId != "work" && activeWorkspaceId != "study") {
            val nextEvent = agendaItems.firstOrNull()
            if (nextEvent != null) {
                val titleLower = nextEvent.title.lowercase()
                if (titleLower.contains("study") || titleLower.contains("class") || titleLower.contains("exam")) {
                    return WorkspaceSuggestion(
                        spaceId = "study",
                        spaceName = "Study Space",
                        reason = "Upcoming event: \"${nextEvent.title}\" at ${nextEvent.time}"
                    )
                } else if (titleLower.contains("meeting") || titleLower.contains("work") || titleLower.contains("sync")) {
                    return WorkspaceSuggestion(
                        spaceId = "work",
                        spaceName = "Work Space",
                        reason = "Upcoming event: \"${nextEvent.title}\" at ${nextEvent.time}"
                    )
                }
            }
        }

        // 3. Time-of-day natural routine
        if (currentHour in 8..11 && activeWorkspaceId != "study" && activeWorkspaceId != "work") {
            return WorkspaceSuggestion(
                spaceId = "work",
                spaceName = "Work Space",
                reason = "Morning productivity routine"
            )
        } else if (currentHour in 19..23 && activeWorkspaceId != "gaming" && activeWorkspaceId != "personal") {
            return WorkspaceSuggestion(
                spaceId = "personal",
                spaceName = "Personal Space",
                reason = "Evening relaxation routine"
            )
        }

        return null
    }
}
