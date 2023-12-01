package com.brandon.recorderapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class WaveformView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val ampList = mutableListOf<Float>()
    private val rectsToDraw = mutableListOf<RectF>()
    private val redPaint = Paint().apply {
        color = Color.RED
    }
    private val RECTWIDTH = 10f

    private var tick = 0


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (rectF in rectsToDraw) {
            canvas.drawRect(rectF, redPaint)
        }
    }

    fun addAmplitude(maxAmplitude: Float) {
        val amplitude = (maxAmplitude / Short.MAX_VALUE) * this.height * 0.8f

        // 새 측정값 등록
        ampList.add(amplitude)
        // 그려야 할 그래프 목록 초기화
        rectsToDraw.clear()
        val maxRectsPerScreen = (this.width / RECTWIDTH).toInt()
        // ampList 에 누적된 값 중 가장 최근의 데이터를 저장
        val ampsFromBack = ampList.takeLast(maxRectsPerScreen)

        // 최근 데이터(recentAmps)로 rect 그리기
//        for ((i, amp) in ampsFromBack.withIndex()) {
//            val rectF = RectF()
//            rectF.top = (this.height / 2) - amp / 2 + 5
//            rectF.bottom = rectF.top + amp + 5
//            rectF.left = i * RECTWIDTH
//            rectF.right = rectF.left + (RECTWIDTH - 5f)
//
//            rectsToDraw.add(rectF)
//        }
        for ((i, amp) in ampsFromBack.withIndex()) {
            val rectF = RectF().also {
                it.top =
                    (this.height / 2) - amp / 2 + 5
                it.bottom = it.top + amp + 5
                it.left = i * RECTWIDTH
                it.right = it.left + (RECTWIDTH - 5f)
            }
            rectsToDraw.add(rectF)
        }

        invalidate()
    }
    // 녹화할 때 저장한 값으로 play하는 함수
    fun replayAmplitude() {
        rectsToDraw.clear()
        val maxRectsPerScreen = (this.width / RECTWIDTH).toInt()
        // 녹음 파일의 앞에서 부터 데이터 읽기
        val ampsFromStart = ampList.take(tick).takeLast(maxRectsPerScreen)

//        for ((i, amp) in ampsFromStart.withIndex()) {
//            val rectF = RectF()
//            rectF.top = (this.height / 2) - amp / 2 + 5
//            rectF.bottom = rectF.top + amp + 5
//            rectF.left = i * RECTWIDTH
//            rectF.right = rectF.left + (RECTWIDTH - 5f)
//
//            rectsToDraw.add(rectF)
//        }

        for ((i, amp) in ampsFromStart.withIndex()) {
            val rectF = RectF().also {
                it.top =
                    (this.height / 2) - amp / 2 + 5  // (this.height/2) 는 View의 중간 - amp/2 는 전체 길이의 반절만큼 위로
                it.bottom = it.top + amp + 5
                it.left = i * RECTWIDTH
                it.right = it.left + (RECTWIDTH - 5f)
            }
            rectsToDraw.add(rectF)
        }

        tick++
        invalidate()
    }

    fun clearData() {
        ampList.clear()
    }

    fun clearWave() {
        rectsToDraw.clear()
        tick = 0
        invalidate()
    }
}

