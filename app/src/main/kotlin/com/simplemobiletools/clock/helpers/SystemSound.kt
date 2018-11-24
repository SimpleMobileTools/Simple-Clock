package com.simplemobiletools.clock.helpers

import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioDeviceInfo.TYPE_WIRED_HEADPHONES
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.support.annotation.RequiresApi
import com.simplemobiletools.clock.extensions.config
import java.io.Serializable


class SystemSound(val context: Context, val toPlay: Uri) {
    // Sound state
    private val mMediaPlayer: MediaPlayer = MediaPlayer()
    private val PLAYER_DEAD = "player_dead"
    private val PLAYER_PAUSED = "player_paused"
    private val PLAYER_INIT = "player_init"
    private var mPlayerState: String = PLAYER_INIT


    // Volume
    private var mVolume: Float = 0.1f;
    private val mVolumeHandler: Handler = Handler();
    private val INCREASE_VOLUME_DELAY = 3000L


    // Core Functions: start and stop

    // This function starts playing the sound if appropriate.
    fun start(): Unit {
        when (mPlayerState) {
            PLAYER_INIT -> {
                initMediaPlayer()
                playNoise()
            }
            PLAYER_PAUSED -> playNoise()
            PLAYER_DEAD -> {}
            else -> {}
        }
    }

    private fun playNoise(): Unit {
        if (context.config.headphonesOnly) {
            headphoneStartMediaPlayer()
        } else {
            mMediaPlayer.start();
        }
        if (context.config.increaseVolumeGradually) {
            scheduleVolumeIncrease()
        } else {
            mVolume = 1f
            mMediaPlayer.setVolume(mVolume, mVolume)
        }
    }

    /** This function kills the SystemSound that may have been playing and destroys any
     * used resources.
     *
     * Note: calling .stop() on the media player
     * whether in prepared mode or started mode still results in stopped mode
     * https://developer.android.com/images/mediaplayer_state_diagram.gif
     * Hence, whenever this is called in between the check for the headphones_only setting
     * and the actual starting, this function behaves properly.
     */
    fun kill(): Unit {
        if (mPlayerState != PLAYER_DEAD){
            mPlayerState = PLAYER_DEAD
            mMediaPlayer.stop()
            mMediaPlayer.release()
            mVolumeHandler.removeCallbacksAndMessages(null)
        } else {} // Already killed. Don't do anything.
    }


    fun pause(): Unit {
        mPlayerState = PLAYER_PAUSED
        mMediaPlayer.pause()
    }

    /**
     * These are a collection of helper functions.
     */

    private fun initMediaPlayer(): Unit {
        var settings: AudioAttributes = AudioAttributes.Builder().apply {
            // Note: media defaults to playing on headphones when possible
            setUsage(AudioAttributes.USAGE_MEDIA)
            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        }.build();
        mMediaPlayer.apply {
            setAudioAttributes(settings)
            setDataSource(context, toPlay);
            setVolume(mVolume, mVolume)
            isLooping = true
            prepare()
        }
    }

    // Start the media player with headphones (if possible)
    private fun headphoneStartMediaPlayer(): Unit {
        // Set up the headset reciever FIRST

        val maybeHeadphones: AudioDeviceInfo? = areHeadphonesIn();
        if (maybeHeadphones != null){
            mMediaPlayer.start()
        } else {} // Can't play b/c no headphones and in only-headphones mode.
    }

    private fun scheduleVolumeIncrease(){
        mVolumeHandler.postDelayed({
            mVolume = Math.min(mVolume + 0.1f, 1f)
            mMediaPlayer.setVolume(mVolume, mVolume)
            scheduleVolumeIncrease()
        }, INCREASE_VOLUME_DELAY)

    }

    private fun areHeadphonesIn(): AudioDeviceInfo? {
        val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager;
        audioManager.getDevices(AudioManager.GET_DEVICES_ALL).forEach {
            if (isAudioDeviceHeadphone(it.type)){
                return it;
            }
        }
        return null;
    }

    private fun isAudioDeviceHeadphone(devTy: Int): Boolean {
        return (devTy == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                devTy == AudioDeviceInfo.TYPE_WIRED_HEADSET)
    }

}


class CloseSound(private val systemSound: SystemSound) : Serializable {
    fun run() : Unit {
        systemSound.kill()
    }
}

