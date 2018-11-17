package com.simplemobiletools.clock.helpers

import android.app.Activity
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioDeviceInfo.TYPE_WIRED_HEADPHONES
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.support.annotation.RequiresApi
import com.simplemobiletools.clock.extensions.config


class SystemSound(val context: Context, val toPlay: Uri) {
    // Sound state
    private val mMediaPlayer: MediaPlayer = MediaPlayer();
    private var mIsKilledOrExpired: Boolean = false;

    // Volume
    private var mVolume: Float = 0.1f;
    private val mVolumeHandler: Handler = Handler();
    private val INCREASE_VOLUME_DELAY = 3000L


    // Core Functions: start and stop

    // This function starts playing the sound if appropriate.
    fun start(): Unit {
        if (!mIsKilledOrExpired){
            initMediaPlayer()
            if (context.config.headphonesOnly){
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
        } else {}
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
        if (!mIsKilledOrExpired){
            mIsKilledOrExpired = true;
            mMediaPlayer.stop()
            mMediaPlayer.release()
            mVolumeHandler.removeCallbacksAndMessages(null)
        } else {} // Already killed. Don't do anything.
    }



    /**
     * These are a collection of helper functions.
     */

    private fun initMediaPlayer(): Unit {
        mMediaPlayer.apply {
            setAudioStreamType(AudioManager.STREAM_ALARM)
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
            val headphones: AudioDeviceInfo = maybeHeadphones;
            mMediaPlayer.setPreferredDevice(headphones)
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