package com.novaforge.launcher.contextring

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ContextRingEngine {

    companion object {
        const val MAX_NESTING_DEPTH = 3
        const val DEADZONE_DP = 28f
        const val EDGE_MARGIN_DP = 20f
    }

    /**
     * Resolves the active ring based on the strict 4-level context priority:
     * 1. Explicitly assigned app-specific ring
     * 2. Active workspace ring
     * 3. Folder-specific ring
     * 4. Default global ring
     */
    fun resolveRing(
        config: ContextRingConfig,
        targetPackage: String? = null,
        targetWorkspaceId: String? = null,
        targetFolderId: String? = null
    ): ContextRingDefinition {
        // Priority 1: Explicit app assignment
        if (!targetPackage.isNullOrBlank()) {
            val appRingId = config.appAssignments[targetPackage]
            if (appRingId != null) {
                val found = config.rings.find { it.id == appRingId }
                if (found != null) return found
            }
        }

        // Priority 2: Active workspace assignment
        if (!targetWorkspaceId.isNullOrBlank()) {
            val wsRingId = config.workspaceAssignments[targetWorkspaceId]
            if (wsRingId != null) {
                val found = config.rings.find { it.id == wsRingId }
                if (found != null) return found
            }
        }

        // Priority 3: Folder-specific ring
        if (!targetFolderId.isNullOrBlank()) {
            val fldRingId = config.folderAssignments[targetFolderId]
            if (fldRingId != null) {
                val found = config.rings.find { it.id == fldRingId }
                if (found != null) return found
            }
        }

        // Priority 4: Default global ring
        return config.rings.find { it.id == config.defaultGlobalRingId }
            ?: config.rings.firstOrNull()
            ?: ContextRingDefinition(
                id = "fallback",
                title = "NovaForge Ring",
                slotCount = 6,
                radiusDp = 140
            )
    }

    /**
     * Clamps the center of the radial menu so that the ring and its action nodes
     * stay fully visible within the screen bounds regardless of screen size/orientation.
     */
    fun calculateClampedCenter(
        rawX: Float,
        rawY: Float,
        screenWidthPx: Float,
        screenHeightPx: Float,
        radiusPx: Float,
        density: Float
    ): Pair<Float, Float> {
        val totalRadialReachPx = radiusPx + (40f * density)
        val marginPx = EDGE_MARGIN_DP * density

        val minX = totalRadialReachPx + marginPx
        val maxX = screenWidthPx - totalRadialReachPx - marginPx
        val minY = totalRadialReachPx + marginPx
        val maxY = screenHeightPx - totalRadialReachPx - marginPx

        val clampedX = if (minX < maxX) {
            rawX.coerceIn(minX, maxX)
        } else {
            screenWidthPx / 2f
        }

        val clampedY = if (minY < maxY) {
            rawY.coerceIn(minY, maxY)
        } else {
            screenHeightPx / 2f
        }

        return Pair(clampedX, clampedY)
    }

    /**
     * Maps pointer coordinates to distance, angle, and selected action slot.
     */
    fun evaluatePointer(
        pointerX: Float,
        pointerY: Float,
        centerX: Float,
        centerY: Float,
        ring: ContextRingDefinition,
        density: Float
    ): Triple<Int, ContextRingAction?, Float> {
        val dx = pointerX - centerX
        val dy = pointerY - centerY
        val distance = sqrt(dx * dx + dy * dy)
        val deadzonePx = DEADZONE_DP * density

        if (distance < deadzonePx || ring.actions.isEmpty()) {
            return Triple(-1, null, distance)
        }

        // Angle in degrees from top (-90 deg offset) clockwise
        var angleRad = atan2(dy.toDouble(), dx.toDouble())
        var angleDeg = Math.toDegrees(angleRad) + 90.0 // 0 at top (12 o'clock)
        if (angleDeg < 0) angleDeg += 360.0

        val slotCount = ring.actions.size.coerceAtLeast(1)
        val sectorSize = 360.0 / slotCount

        // Shift by half sector so that sector 0 is centered around top
        val shiftedAngle = (angleDeg + (sectorSize / 2.0)) % 360.0
        val selectedIndex = (shiftedAngle / sectorSize).toInt().coerceIn(0, slotCount - 1)
        val action = ring.actions.getOrNull(selectedIndex)

        return Triple(selectedIndex, action, distance)
    }

    /**
     * Resolves slot center coordinate (X, Y) relative to ring center.
     */
    fun getSlotPosition(
        index: Int,
        totalSlots: Int,
        radiusPx: Float
    ): Pair<Float, Float> {
        val angleStep = (2.0 * Math.PI) / totalSlots
        // Start from top (-PI / 2)
        val angle = (index * angleStep) - (Math.PI / 2.0)
        val x = (radiusPx * cos(angle)).toFloat()
        val y = (radiusPx * sin(angle)).toFloat()
        return Pair(x, y)
    }

    /**
     * Evaluates whether a nested ring can be opened, enforcing MAX_NESTING_DEPTH
     * and preventing circular references.
     */
    fun canNavigateToNestedRing(
        targetRingId: String,
        breadcrumbs: List<String>,
        config: ContextRingConfig
    ): Boolean {
        if (breadcrumbs.size >= MAX_NESTING_DEPTH) return false
        if (breadcrumbs.contains(targetRingId)) return false
        return config.rings.any { it.id == targetRingId }
    }
}
