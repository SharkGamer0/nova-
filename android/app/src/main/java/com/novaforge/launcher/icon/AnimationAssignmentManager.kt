package com.novaforge.launcher.icon

import com.novaforge.launcher.data.model.AppItem
import kotlinx.coroutines.flow.StateFlow

class AnimationAssignmentManager(
    private val repository: AnimationRepository
) {
    val assignments: StateFlow<Map<String, IconAssignment>> = repository.assignments

    fun getAssignmentForPackage(packageName: String): IconAssignment? {
        return assignments.value[packageName]
    }

    fun isAppAssigned(packageName: String): Boolean {
        return assignments.value.containsKey(packageName)
    }

    fun filterAssignedApps(apps: List<AppItem>): List<AppItem> {
        val current = assignments.value
        return apps.filter { current.containsKey(it.packageName) }
    }

    fun filterUnassignedApps(apps: List<AppItem>): List<AppItem> {
        val current = assignments.value
        return apps.filter { !current.containsKey(it.packageName) }
    }

    suspend fun assign(
        packageName: String,
        animationId: String,
        playbackSpeed: Float = 1.0f,
        loopMode: String = "LOOP"
    ) {
        val assignment = IconAssignment(
            packageName = packageName,
            animationId = animationId,
            playbackSpeed = playbackSpeed,
            loopMode = loopMode,
            animateOnAppear = true,
            animateOnLaunch = false,
            staticFirstFrameFallback = true,
            isEnabled = true
        )
        repository.assignAnimationToApp(assignment)
    }

    suspend fun assignBatch(
        packageNames: List<String>,
        animationId: String,
        playbackSpeed: Float = 1.0f,
        loopMode: String = "LOOP"
    ) {
        for (pkg in packageNames) {
            assign(pkg, animationId, playbackSpeed, loopMode)
        }
    }

    suspend fun remove(packageName: String) {
        repository.removeAssignmentForApp(packageName)
    }

    suspend fun removeAll() {
        for (pkg in assignments.value.keys) {
            repository.removeAssignmentForApp(pkg)
        }
    }
}
