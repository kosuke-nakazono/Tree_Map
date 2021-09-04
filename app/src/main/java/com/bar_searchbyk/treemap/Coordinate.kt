package com.bar_searchbyk.treemap

// 各銘柄の座標とデータを保持する
data class Coordinate(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
    var data: StockData
)