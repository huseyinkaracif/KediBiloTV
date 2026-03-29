package com.kedibilotv.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer

object KediBiloPlayer {

    fun create(
        context: Context,
        bufferMinMs: Int = 10_000,
        bufferMaxMs: Int = 30_000
    ): ExoPlayer {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs              */ bufferMinMs,
                /* maxBufferMs              */ bufferMaxMs,
                /* bufferForPlaybackMs      */ 1_500,
                /* bufferForPlaybackAfterRebufferMs */ 5_000
            )
            .build()

        return ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .build()
    }

    fun play(player: ExoPlayer, url: String, startPositionMs: Long = 0) {
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        if (startPositionMs > 0) {
            player.seekTo(startPositionMs)
        }
        player.play()
    }
}
