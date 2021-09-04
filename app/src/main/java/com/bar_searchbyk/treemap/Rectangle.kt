package com.bar_searchbyk.treemap

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator

// 各銘柄のセルを作成する
class Rectangle: View {

    // カスタムview作成用コンストラクタ
    constructor(context: Context, coordinate: Coordinate, parentHeight: Float, parentWidth: Float) : super(context) {
        mCoordinate = coordinate
        mParentWidth = parentHeight
        mParentHeight = parentWidth
        mBorder = Border(context,coordinate)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    // 座標データ
    private var mCoordinate: Coordinate? = null
    // TreeMap描画用コンテナ(親view)の高さ
    private var mParentHeight: Float? = null
    // TreeMap描画用コンテナ(親view)の幅
    private var mParentWidth: Float? = null
    // タップ時に枠線を表示させるView
    private var mBorder:Border? = null

    @SuppressLint("RestrictedApi")
    val mArgbEvaluator = ArgbEvaluator()
    @SuppressLint("RestrictedApi")
    private fun evaluate(rate: Float, startValue: Int, endValue: Int): Any = mArgbEvaluator.evaluate(rate, startValue, endValue)

    fun getCoordinate(): Coordinate?{
        return mCoordinate
    }

    fun getBorder(): Border?{
        return mBorder
    }

    override fun onDraw(canvas: Canvas?) {
        drawRectangle(canvas)
        super.onDraw(canvas)
    }

    // セルの描画
    private fun drawRectangle(canvas: Canvas?){
        mCoordinate?:return
        mParentHeight?:return
        mParentWidth?:return

        // 座標データの取得
        val coordinate = mCoordinate!!

        // canvasの状態を保存
        val saveCount = canvas!!.save()

        // 銘柄名
        val symbolName = coordinate.data.symbolName
        // 騰落率
        val changeRate =  coordinate.data.changeRate

        // セル幅
        val cellWidth = coordinate.width
        // セル高さ
        val cellHeight = coordinate.height
        // セル面積
        val cellArea = cellHeight.times(cellWidth)
        // 親view面積
        val parentArea = mParentHeight!!.times(mParentWidth!!)
        // 親viewに対する面積比率
        val areaRate = cellArea.div(parentArea).times(100)
        // テキスト 開始位置 x座標
        val textXCo = coordinate.x + cellWidth.div(2)
        //　親viewの高さに対するセルの高さ
        val heightRate = cellHeight.div(mParentHeight!!).times(100)

        // 面積と文字数からテキストサイズを仮決めする
        var cellTextSize = setTextSize(coordinate,areaRate)

        // 銘柄名 最大描画可能高さ
        val symbolNameMaxHeight =  when {
            areaRate > 50 -> {
                cellHeight.div(4).times(3)
            }
            else -> {
                cellHeight.div(2)
            }
        }

        // 騰落率 最大描画可能高さ
        val changeRateHeight =  when {
            heightRate > 90 -> {
                cellHeight.div(3).times(1)
            }
            else -> {
                cellHeight.div(2)
            }
        }

        // 各セル塗り潰し用Paint
        val fillPaint = Paint().apply {
            color = setColorByChangeRate(changeRate.toFloat())
        }

        // 枠線用Paint
        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = 3f
        }

        // 銘柄名用Paint
        val symbolNamePaint = TextPaint().apply {
            // センタリング
            textAlign = Paint.Align.CENTER
            textSize = cellTextSize
            typeface = Typeface.create("", Typeface.BOLD)
        }

        // 騰落率用Paint
        val changeRatePaint = TextPaint().apply {
            // センタリング
            textAlign = Paint.Align.CENTER
            textSize = cellTextSize.div(1.2f)
            typeface = Typeface.create("", Typeface.BOLD)
        }

        val rect = Rect(
            coordinate.x.toInt(),
            coordinate.y.toInt(),
            (coordinate.x + cellWidth).toInt(),
            (coordinate.y + cellHeight).toInt()
        )
        canvas.drawRect(rect, fillPaint)
        canvas.drawRect(rect, borderPaint)

        // 仮決めしたテキストサイズで全ての文字を描画できるどうかをここで判定
        val isTextSizeFixNeed = isTextSizeFixNeed(
            symbolName,
            cellWidth,
            symbolNameMaxHeight,
            symbolNamePaint.apply { color = Color.TRANSPARENT })?:return // 透過色で描画

        var isSizeFix: Boolean? = isTextSizeFixNeed

        while (isSizeFix == true) {
            // 1/20サイズダウン
            cellTextSize = cellTextSize.times(19).div(20)
            isSizeFix = isTextSizeFixNeed(
                symbolName,
                cellWidth,
                symbolNameMaxHeight,
                symbolNamePaint.apply {
                    color = Color.TRANSPARENT
                    textSize = cellTextSize
                })
        }

        // 騰落率の高さ・幅を計算
        val symbolNameMeasureAlpha = drawMultilineText(
            canvas = canvas,
            text = symbolName,
            x = textXCo,
            y = 0f,
            width = cellWidth,
            height = symbolNameMaxHeight,
            paint =  symbolNamePaint.apply { color = Color.TRANSPARENT } // 透過色で描画
        ) ?: return

        //  騰落率の高さ・幅を計算
        val changeRateMeasureAlpha = drawMultilineText(
            canvas = canvas,
            text = changeRate.toString(),
            x = textXCo,
            y = 0f,
            width = cellWidth,
            height = changeRateHeight,
            paint =  changeRatePaint.apply { color = Color.TRANSPARENT } // 透過色で描画
        )?: return

        // 描画された銘柄名の高さと幅を取得
        val actualHeight = symbolNameMeasureAlpha.height
        val actualWidth = symbolNameMeasureAlpha.width

        // セルの高さに対する銘柄名の高さを取得
        val actualHeightRate = actualHeight.div(cellHeight).times(100)

        // 銘柄名と騰落率の間のスペース
        val space = cellHeight.div(15)

        // 銘柄名 騰落率 スペースを合わせた合計高さ
        val totalHeight = symbolNameMeasureAlpha.height + changeRateMeasureAlpha.height + space

        // 銘柄名の高さがセルの高さの45%以上、セルの面積が全体の面積の50%以上を占める場合、全て描画できない場合があるため、銘柄名と騰落率の間のスペースを削除する
        val fixedTotalSpace = if(areaRate > 50 && actualHeightRate > 45){
            0f
        } else {
            space
        }

        // y座標を再計算 (縦に中央揃え)
        val fixedSymbolNameYCo = cellHeight.minus(totalHeight).div(2) + coordinate.y

        // 銘柄名を描画
        val symbolNameMeasureHeight =  drawMultilineText(
            canvas = canvas,
            text = symbolName,
            x = textXCo,
            y = fixedSymbolNameYCo,
            width = cellWidth,
            height = symbolNameMaxHeight,
            paint =  symbolNamePaint.apply { color = Color.WHITE } // 白色で描画
        )?: return

        // 騰落率のy座標をセット
        val fixedChangeRateYCo = fixedSymbolNameYCo + symbolNameMeasureHeight.height + fixedTotalSpace

        // 騰落率を描画
        drawMultilineText(
            canvas = canvas,
            text = changeRate.toString(),
            x = textXCo,
            y = fixedChangeRateYCo,
            width = cellWidth,
            height = changeRateHeight,
            paint =  changeRatePaint.apply { color = Color.WHITE } // 白色で描画
        )

        // canvasの状態を復元
        canvas.restoreToCount(saveCount)
    }

    // 面積と文字数からテキストサイズを仮決めする
    private fun setTextSize(coordinate: Coordinate, areaRate: Float): Float{
        // 係数
        val heightCoefficient = 1
        val widthCoefficient = 1.6

        val totalValue = coordinate.width.times(widthCoefficient) + coordinate.height.times(heightCoefficient)

        // 文字数によって文字の大きさを調整
        val textLength = coordinate.data.symbolName.length
        val dividedNumber = when {
            textLength > 8 -> {
                12
            }
            textLength in 6..8 -> {
                10
            }
            textLength in 3..5 -> {
                when{
                    areaRate > 90 -> {
                        9
                    }
                    areaRate <= 90 && areaRate > 70 -> {
                        10
                    }
                    areaRate <= 70 && areaRate > 50 -> {
                        11
                    }
                    areaRate <= 50 && areaRate > 20 -> {
                        12
                    }
                    else -> {
                        13
                    }
                }
            }
            else -> {
                when{
                    areaRate > 90 -> {
                        6
                    }
                    areaRate <= 90 && areaRate > 70 -> {
                        7
                    }
                    areaRate <= 70 && areaRate > 50 -> {
                        8
                    }
                    areaRate <= 50 && areaRate > 20 -> {
                        10
                    }
                    else -> {
                        11
                    }
                }
            }
        }

        return totalValue.div(dividedNumber).toFloat()
    }

    // 暴騰率によってセルのカラーを変更
    private fun setColorByChangeRate(changeRate: Float): Int{
        return when{
            changeRate <= -3 -> {
                Color.rgb(48,204,94)
            }

            changeRate <= -1 &&  changeRate > -3  -> {
                var value = changeRate + 1
                if(value < 0) {
                    value = value.times(-1)
                }

                val rate = value.div(2)

                return evaluate(
                    rate,
                    Color.rgb(41,158,79),
                    Color.rgb(48,204,94)
                ) as Int
            }

            changeRate < 0 &&  changeRate > -1  -> {
                val value = changeRate.times(-1)

                return evaluate(
                    value,
                    Color.rgb(65,69,85),
                    Color.rgb(41,158,79)
                ) as Int
            }

            changeRate == 0f -> {
                Color.rgb(65,69,85)
            }

            changeRate <= 1 &&  changeRate > 0  -> {
                return evaluate(
                    changeRate,
                    Color.rgb(139,68,78),
                    Color.rgb(65,69,85)
                ) as Int
            }

            changeRate <= 3 &&  changeRate > 1  -> {
                val value = changeRate - 1

                val rate = value.div(2)
                return evaluate(
                    rate,
                    Color.rgb(191,64,69),
                    Color.rgb(139,68,78)
                ) as Int
            }
            // 3以上
            else -> {
                Color.rgb(191,64,69)
            }
        }
    }

    /**
     * 複数行のテキストを描画する
     * @param canvas canvas
     * @param text 描画する文字列
     * @param x x座標left
     * @param y y座標top
     * @param width 描画できる最大幅
     * @param height 描画できる最大高さ
     * @param paint Paint
     */
    private fun drawMultilineText(
        canvas: Canvas, text: String, x: Float, y: Float,
        width: Float, height: Float, paint: Paint
    ):MeasureText?{

        // 文字が描画された行数を管理する
        var drawLineCount = 0
        val lineHeightWidth = calcLineHeight(paint)
        val len = text.length
        var sumHeight = lineHeightWidth
        var longestWidth = lineHeightWidth
        var index = 0

        while (index < len && sumHeight <= height) {
            // 1行で描画できる文字数
            val count = paint.breakText(text, index, len, true, width, null)

            if (count == 0) return null

            if(isDrawDots(count,index,len,sumHeight,lineHeightWidth,height)){
                val fixedText = text.substring(index,index + count -1) + ".."
                canvas.drawText(fixedText, 0, fixedText.length , x, y + sumHeight, paint)
            } else {
                canvas.drawText(text, index, index + count, x, y + sumHeight, paint)
            }

            drawLineCount += 1

            index += count
            sumHeight += lineHeightWidth
            longestWidth = if(longestWidth < count.times(lineHeightWidth)) count.times(lineHeightWidth) else longestWidth
        }

        return MeasureText(height = lineHeightWidth.times(drawLineCount),width = longestWidth)
    }

    // 文字を..に変換するかどうかの判定
    private fun isDrawDots(count: Int,index: Int,len: Int,sumHeight: Float,lineHeight: Float,height: Float): Boolean{
        return sumHeight + lineHeight > height && count + index < len
    }

    private fun calcLineHeight(paint: Paint): Float {
        return paint.textSize
    }

    // セル内に全て文字を描画できるどうかの判定
    private fun isTextSizeFixNeed(text: String,width: Float,height: Float,paint: Paint): Boolean?{
        var drawLineCount = 0
        val lineHeightWidth = calcLineHeight(paint)
        val len = text.length
        var sumHeight = lineHeightWidth
        var longestWidth = lineHeightWidth
        var index = 0

        while (index < len && sumHeight <= height){
            // 1行で描画できる文字数
            val count = paint.breakText(text, index, len, true, width, null)

            if (count == 0) return null

            if(isDrawDots(count,index,len,sumHeight,lineHeightWidth,height)) return true

            drawLineCount += 1

            index += count
            sumHeight += lineHeightWidth
            longestWidth = if(longestWidth < count.times(lineHeightWidth)) count.times(lineHeightWidth) else longestWidth
        }

        return false
    }

    // 描画されたテキストの高さと幅を保持するクラス
    inner class MeasureText(val height: Float,val width: Float)
}