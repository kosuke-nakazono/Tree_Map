package com.bar_searchbyk.treemap

import kotlin.math.pow

// 座標を計算するクラス
class CalcCoordinate(stockList: List<StockData>, parentWidth: Float, parentHeight: Float) {
    // 計算未完了エリア
    private var mUnCalcArea: UnCalcArea = UnCalcArea(parentWidth, parentHeight)
    // 銘柄リスト
    private var mStockList: List<StockData> = stockList
    // 計算未完了エリアが縦長であれば、横方向に描画。逆も然り
    // 描画方向の描画幅
    private val mDirectionWidth: Vertical
        get() = if (mUnCalcArea.height.pow(2.0f) > mUnCalcArea.width.pow(2.0f)) {
            Vertical(mUnCalcArea.width, false)
        } else Vertical(mUnCalcArea.height, true)

    init {
        if(!mStockList.isNullOrEmpty()) {
            // 運用額の合計
            var totalValue = 0.0
            mStockList.forEach {
                totalValue += it.amount
            }

            // 各銘柄の全体に対する占有面積を格納したリスト
            val valueRateList = floatArrayOf().toCollection(ArrayList())
            mStockList.forEach {
                valueRateList.add(it.amount.times(parentHeight).times(parentWidth).div(totalValue).toFloat())
            }

            // 座標データの作成
            square(valueRateList.toFloatArray(), floatArrayOf(), mDirectionWidth.value)
        }
    }

    /**
     * @param unCalcStockList 計算未完了銘柄リスト
     * @param rawStocksList  1行で描画する(計算する)銘柄リスト
     * @param directionWidth 描画方向の描画幅　
     * */
    private fun square(unCalcStockList: FloatArray, rawStocksList: FloatArray, directionWidth: Float) {
        // 最後の1銘柄の場合の処理
        if (unCalcStockList.size == 1) {
            calcLastRow(rawStocksList, unCalcStockList, directionWidth)
            return
        }

        // drawStocksListに計算未完了銘柄リストの先頭を追加したリストを作成
        val additionalStocksList = FloatArray(rawStocksList.size + 1)
        System.arraycopy(rawStocksList, 0, additionalStocksList, 0, rawStocksList.size)
        additionalStocksList[rawStocksList.size] = unCalcStockList[0]

        // additionalStocksListから、もう1銘柄描画できるかどうかを判定
        if (rawStocksList.isEmpty() || worstRatio(rawStocksList, directionWidth) >= worstRatio(additionalStocksList, directionWidth)) {
            square(unCalcStockList.copyOfRange(1, unCalcStockList.size), additionalStocksList, directionWidth)
            return
        }

        // 1行分の銘柄の座標データを作成
        calcRow(rawStocksList, directionWidth, mDirectionWidth.isVertical)

        // 残りの銘柄を計算
        square(unCalcStockList, floatArrayOf(), mDirectionWidth.value)
    }

    // 面積比と描画方向の描画幅から描画可否の判定に使用する値を算出
    private fun worstRatio(row: FloatArray, directionWidth: Float): Float {
        val sum = row.sum()
        val rowMax = row.maxOrNull() ?:0f
        val rowMin = row.minOrNull() ?:0f
        return (directionWidth.pow(2.0f) * rowMax / sum.pow(2.0f)).coerceAtLeast(
            sum.pow(2.0f) / (directionWidth.pow(2.0f) * rowMin)
        )
    }

    // 1行分の銘柄の座標データを作成
    private fun calcRow(valueRateList: FloatArray, directionWidth: Float, vertical: Boolean) {
        val rowHeight = valueRateList.sum().div(directionWidth)

        valueRateList.forEach {
            val rowWidth = it / rowHeight
            val xBeginning = mUnCalcArea.xBeginning
            val yBeginning = mUnCalcArea.yBeginning
            val data: Coordinate
            if (vertical) {
                data = Coordinate(
                    xBeginning, yBeginning, rowHeight, rowWidth,
                    mStockList[mUnCalcArea.data.size]
                )
                mUnCalcArea.yBeginning += rowWidth
            } else {
                data = Coordinate(
                    xBeginning, yBeginning, rowWidth, rowHeight,
                    mStockList[mUnCalcArea.data.size]
                )
                mUnCalcArea.xBeginning += rowWidth
            }

            // 座標データをここで追加
            mUnCalcArea.data.add(data);
        }

        // 描画した方向を元に次の銘柄の描画開始位置を設定
        if (vertical) {
            mUnCalcArea.xBeginning += rowHeight
            mUnCalcArea.yBeginning -= directionWidth
            mUnCalcArea.width -= rowHeight
        } else {
            mUnCalcArea.xBeginning -= directionWidth
            mUnCalcArea.yBeginning += rowHeight
            mUnCalcArea.height -= rowHeight
        }
    }

    // 最後の1銘柄を計算
    private fun calcLastRow(rawStocksList: FloatArray, unDrawnStockList: FloatArray, width: Float) {
        val vertical = mDirectionWidth.isVertical
       if(mStockList.size != 1) calcRow(rawStocksList, width, vertical)
        calcRow(unDrawnStockList, width, vertical)
    }

    // 座標データの取得
    fun get(): List<Coordinate> {
        return mUnCalcArea.data
    }

    // 計算未完了エリア
    private inner class UnCalcArea(var width: Float, var height: Float) {
        var data: MutableList<Coordinate> = ArrayList()
        var xBeginning: Float = 0.0f
        var yBeginning: Float = 0.0f
    }

    /**
     * @param value 描画未完了エリアの最小辺長
     * @param isVertical 描画する方向
     * */
    private inner class Vertical(var value: Float, var isVertical: Boolean)
}