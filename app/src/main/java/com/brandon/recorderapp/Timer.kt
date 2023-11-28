package com.brandon.recorderapp

import android.os.Handler
import android.os.Looper

class Timer(listener: OnTimerTickListener) {
    private var duration: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            duration += 100L
            // handler 에게 작업 요청, this 는 현재 Runnable 비익명 객체
            handler.postDelayed(this, 100L)
            listener.onTick(duration)
        }

    }

    fun start() {
        handler.postDelayed(runnable, 100L)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
    }

}

interface OnTimerTickListener {
    fun onTick(duration: Long)
}