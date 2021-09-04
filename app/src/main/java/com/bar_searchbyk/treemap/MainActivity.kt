package com.bar_searchbyk.treemap

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var mBorder: Border? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 銘柄データを作成
        val stockList: ArrayList<StockData> = makeStockList()

        // TreeMap描画用コンテナView 親View
        val treeMapLayout = findViewById<FrameLayout>(R.id.treeMapLayout)

        treeMapLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 高さと横幅の取得
                val width = treeMapLayout.width.toFloat()
                val height = treeMapLayout.height.toFloat()

                // 各銘柄の座標データが格納されたリストの取得
                val coordinateList = CalcCoordinate(stockList, width, height).get()

                val rectangleList = mutableListOf<Rectangle>()
                for (data in coordinateList.withIndex()) {

                    val coordinate = data.value

                    val rectangle = Rectangle(
                        this@MainActivity,
                        coordinate,
                        treeMapLayout.width.toFloat(),
                        treeMapLayout.height.toFloat()
                    )
                    rectangleList.add(rectangle)
                    treeMapLayout.addView(rectangle)
                }
                treeMapLayout.setOnTreeMapLayoutTouchListener(rectangleList)
                treeMapLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun makeStockList(): ArrayList<StockData>{
        val list = arrayListOf<StockData>()
        list.add(StockData(amount = 22.0,symbolName = "AAPL",changeRate = 0.2))
        list.add(StockData(amount = 18.0,symbolName = "TSLA",changeRate = 0.8))
        list.add(StockData(amount = 15.0,symbolName = "GOOGLE",changeRate = -3.8))
        list.add(StockData(amount = 10.0,symbolName = "AMZN",changeRate = 0.8))
        list.add(StockData(amount = 8.0,symbolName = "FB",changeRate = 1.2))
        list.add(StockData(amount = 7.0,symbolName = "MSFT",changeRate = 3.2))
        list.add(StockData(amount = 7.0,symbolName = "PG",changeRate = -1.2))
        list.add(StockData(amount = 6.0,symbolName = "WMT",changeRate = -0.8))
        list.add(StockData(amount = 5.0,symbolName = "AVGO",changeRate = 0.0))
        list.add(StockData(amount = 2.0,symbolName = "IBM",changeRate = 0.6))
        return list
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun ViewGroup.setOnTreeMapLayoutTouchListener(rectangleList: List<Rectangle>) {
        setOnTouchListener { _: View, motionEvent: MotionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    val filteredList = rectangleList.filter {
                        it.getCoordinate()?.x!! <= motionEvent.x &&
                                motionEvent.x <= it.getCoordinate()?.x!! + it.getCoordinate()?.width!! &&
                                it.getCoordinate()?.y!! <= motionEvent.y &&
                                motionEvent.y <= it.getCoordinate()?.y!! + it.getCoordinate()?.height!!
                    }
                    if (filteredList.isEmpty()) return@setOnTouchListener false
                    val targetRectangle = filteredList.first()
                    if(mBorder != null) this.removeView(mBorder)
                    mBorder = targetRectangle.getBorder()
                    this.addView(mBorder)
                    return@setOnTouchListener false
                }
                else -> {false}
            }
        }
    }
}