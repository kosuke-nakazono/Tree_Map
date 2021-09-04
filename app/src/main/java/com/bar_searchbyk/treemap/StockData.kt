package com.bar_searchbyk.treemap

// 株価データ
data class StockData(
    // 運用額
    var amount:Double,
    // 銘柄名 Ticker symbol
    var symbolName: String,
    // 騰落率
    var changeRate: Double
)