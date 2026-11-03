package com.phonesimulator.game

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import kotlin.math.sin

/** Short synthesized single-voice, mono PCM ringtone. Plays only on an explicit tap. */
class RingtonePreview {
    private var audio: AudioTrack?=null
    private val handler=Handler(Looper.getMainLooper())
    private val release=Runnable { stop() }
    fun play() {
        stop()
        runCatching {
            val rate=22050
            val notes=listOf(659.25,587.33,369.99,415.30,554.37,493.88,293.66,329.63)
            val samples=ShortArray(rate*2)
            samples.indices.forEach { i ->
                val note=i/(rate/4);val position=i%(rate/4)
                val envelope=if(position<100)position/100.0 else if(position>rate/4-300)(rate/4-position)/300.0 else 1.0
                samples[i]=(sin(2*Math.PI*notes[note.coerceAtMost(notes.lastIndex)]*i/rate)*7000*envelope).toInt().toShort()
            }
            val track=AudioTrack.Builder().setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(AudioFormat.Builder().setSampleRate(rate).setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setTransferMode(AudioTrack.MODE_STATIC).setBufferSizeInBytes(samples.size*2).build()
            audio=track;track.write(samples,0,samples.size);track.setVolume(0.35f);track.play();handler.postDelayed(release,2300L)
        }.onFailure { stop() }
    }
    fun stop(){handler.removeCallbacks(release);audio?.let { runCatching { it.stop() };it.release() };audio=null}
}
