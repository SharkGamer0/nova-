package com.novaforge.launcher.service

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.view.KeyEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MediaPlaybackInfo(
    val title: String = "No Media Playing",
    val artist: String = "Inactive",
    val isPlaying: Boolean = false,
    val hasActiveSession: Boolean = false
)

/**
 * Android MediaSessionManager integration for controlling playback on Spotify, YouTube Music, etc.
 */
class MediaControlHelper(private val context: Context) {

    private val sessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
    private val _playbackInfo = MutableStateFlow(MediaPlaybackInfo())
    val playbackInfo: StateFlow<MediaPlaybackInfo> = _playbackInfo.asStateFlow()

    private var activeController: MediaController? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateInfo()
        }

        override fun onMetadataChanged(metadata: android.media.MediaMetadata?) {
            updateInfo()
        }
    }

    fun registerNotificationListener() {
        try {
            val listenerComponent = ComponentName(context, NovaForgeNotificationListener::class.java)
            val controllers = sessionManager?.getActiveSessions(listenerComponent)
            if (!controllers.isNullOrEmpty()) {
                activeController?.unregisterCallback(controllerCallback)
                activeController = controllers[0]
                activeController?.registerCallback(controllerCallback)
                updateInfo()
            } else {
                _playbackInfo.value = MediaPlaybackInfo(hasActiveSession = false)
            }
        } catch (e: SecurityException) {
            // Notification access not granted yet
            _playbackInfo.value = MediaPlaybackInfo(
                title = "Media Access Needed",
                artist = "Grant Notification Access in Settings",
                hasActiveSession = false
            )
        }
    }

    private fun updateInfo() {
        val controller = activeController ?: return
        val metadata = controller.metadata
        val state = controller.playbackState

        val title = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown Track"
        val artist = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown Artist"
        val isPlaying = state?.state == PlaybackState.STATE_PLAYING

        _playbackInfo.value = MediaPlaybackInfo(
            title = title,
            artist = artist,
            isPlaying = isPlaying,
            hasActiveSession = true
        )
    }

    fun playPause() {
        val controller = activeController
        if (controller != null) {
            val state = controller.playbackState?.state
            if (state == PlaybackState.STATE_PLAYING) {
                controller.transportControls.pause()
            } else {
                controller.transportControls.play()
            }
        }
    }

    fun skipNext() {
        activeController?.transportControls?.skipToNext()
    }

    fun skipPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }
}
