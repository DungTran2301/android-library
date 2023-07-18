package com.nartgnud.core.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri

interface Mp3PlayerUtils {
    fun addOnCompletionListener(listener: (() -> Unit))
    fun addOnErrorListener(listener: ((Exception) -> Unit))
    fun play(url: String)
    fun pause()
    fun resume()
    fun stop()

    companion object {
        fun build(context: Context): Mp3PlayerUtils {
            return Mp3PlayerUtilsImpl(context)
        }
    }
}

internal class Mp3PlayerUtilsImpl(val context: Context) : Mp3PlayerUtils {

    // A media player instance
    private var mediaPlayer: MediaPlayer? = null

    // A flag to indicate if the player is prepared
    private var isPrepared = false
    // A flag to indicate if the player is currently playing
    private var isPlaying = false

    // A listener to handle completion events
    private var onCompletionListener: (() -> Unit)? = null

    private var onErrorListener: ((Exception) -> Unit)? = null

    override fun addOnCompletionListener(listener: () -> Unit) {
        this.onCompletionListener = listener
    }

    override fun addOnErrorListener(listener: (Exception) -> Unit) {
        this.onErrorListener = listener
    }

    override fun play(url: String) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
                mediaPlayer?.setOnCompletionListener {
                    isPlaying = false
                    isPrepared = false
                    onCompletionListener?.invoke()
                }
                mediaPlayer?.setOnErrorListener { _, what, extra ->
                    isPlaying = false
                    isPrepared = false
                    val exception =
                        PlayMp3Exception.NetworkFailureException("Media player error: what=$what, extra=$extra")
                    onErrorListener?.invoke(exception)
                    true
                }
            } else {
                mediaPlayer?.reset()
                isPrepared = false
            }

            // Set the data source for the media player
            mediaPlayer?.setDataSource(context, Uri.parse(url))
            // Prepare the media player asynchronously
            mediaPlayer?.prepareAsync()
            // Set the prepared listener for the media player
            mediaPlayer?.setOnPreparedListener {
                // Set the prepared flag to true
                isPrepared = true
                // Start playing the mp3 url
                mediaPlayer?.start()
                // Set the playing flag to true
                isPlaying = true
            }
        } catch (e: Exception) {
            // Handle any exceptions
            e.printStackTrace()
            val exception = PlayMp3Exception.NetworkFailureException("Media player error: ${e.message}")
            onErrorListener?.invoke(exception)
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // A function to pause the playback
    override fun pause() {
        // Check if the media player is playing
        if (isPlaying) {
            // Pause the media player
            mediaPlayer?.pause()
            // Set the playing flag to false
            isPlaying = false
        }
    }

    // A function to resume the playback
    override fun resume() {
        // Check if the media player is prepared and not playing
        if (isPrepared && !isPlaying) {
            // Resume the media player
            mediaPlayer?.start()
            // Set the playing flag to true
            isPlaying = true
        }
    }

    // A function to stop the playback and release the resources
    override fun stop() {
        // Check if the media player is playing
        if (isPlaying) {
            // Stop the media player
            mediaPlayer?.stop()
            // Set the playing flag to false
            isPlaying = false
        }
        // Check if the media player is prepared
        if (isPrepared) {
            // Reset the media player
            mediaPlayer?.reset()
            // Set the prepared flag to false
            isPrepared = false
        }
        // Release the media player resources
        mediaPlayer?.release()
        mediaPlayer = null
    }

}