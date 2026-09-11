package com.example.data.models

data class FleetMember(
    val id: String,
    val driver: String,
    val vehicle: String,
    val plate: String
)

data class GoodsBayMapping(
    val goods: String,
    val bay: String,
    val unit: String
)

object MasterData {
    // 1. Dedicated Vehicles & Drivers
    val FLEET_ROSTER = listOf(
        FleetMember("v1", "Sam", "Ashok Leyland Dost", "GJ-03-AL-1001"),
        FleetMember("v2", "Ram", "Tata Yodha", "GJ-03-TY-2045"),
        FleetMember("v3", "Manav", "Tata Intra", "GJ-03-TI-3099"),
        FleetMember("v4", "Ajax", "Mahindra Jeeto", "GJ-03-MJ-4112"),
        FleetMember("v5", "Qwerty", "Tata Ace", "GJ-03-TA-5521")
    )

    // 2. Factory Goods & Assigned Loading Bays
    val GOODS_LIST = listOf(
        GoodsBayMapping("Glass Tuff", "Bay No. 1", "Crates"),
        GoodsBayMapping("Glass Bullet Proof", "Bay No. 2", "Crates"),
        GoodsBayMapping("Window Frames", "Bay No. 3", "Sets"),
        GoodsBayMapping("Window Grills", "Bay No. 4", "Pieces"),
        GoodsBayMapping("PVC Doors", "Bay No. 5", "Units")
    )

    // 3. Client Categories
    val CLIENT_CATEGORIES = listOf(
        "Organization",
        "Government Entity",
        "Individual Buyer"
    )

    fun getBayForGoods(goodsName: String): String {
        return GOODS_LIST.find { it.goods.equals(goodsName, ignoreCase = true) }?.bay ?: "Bay No. 1"
    }

    fun getUnitForGoods(goodsName: String): String {
        return GOODS_LIST.find { it.goods.equals(goodsName, ignoreCase = true) }?.unit ?: "Units"
    }
}
