package com.bar_searchbyk.treemap

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class Border: View {
    // カスタムview作成用コンストラクタ
    constructor(context: Context, coordinate: Coordinate) : super(context){
        mCoordinate = coordinate
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    // 座標データ
    private var mCoordinate: Coordinate? = null

    // 枠線用Paint
    private val mPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.YELLOW
        strokeWidth = 4f
    }

    override fun onDraw(canvas: Canvas?) {
        drawFrameBorder(canvas)
        super.onDraw(canvas)
    }

    // 枠線の描画
    private fun drawFrameBorder(canvas: Canvas?) {
        mCoordinate?:return
        // canvasの状態を保存
        val saveCount = canvas!!.save()
        canvas.drawRect(
            mCoordinate!!.x,
            mCoordinate!!.y,
            mCoordinate!!.x + mCoordinate!!.width,
            mCoordinate!!.y + mCoordinate!!.height,
            mPaint
        )
        // canvasの状態を復元
        canvas.restoreToCount(saveCount)
    }
}