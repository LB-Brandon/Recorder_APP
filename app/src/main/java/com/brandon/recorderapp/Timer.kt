package com.brandon.recorderapp

import android.os.Handler
import android.os.Looper

// main(UI) 스레드 내 비동기 호출을 handler를 사용하여 구현
// delay 동안 UI를 차단하지 않지만 handler 내에서 시간이 많이 소요되는 작업을 하면 block 된다(UI 스레드사용)
class Timer(listener: OnTimerTickListener) {
    companion object {
        const val DURATION = 40L
    }
    private var duration: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            duration += DURATION
            handler.postDelayed(this, DURATION)
            listener.onTick(duration)
        }
    }

    fun start() {
        handler.postDelayed(runnable, DURATION)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
        duration = 0
    }
}

