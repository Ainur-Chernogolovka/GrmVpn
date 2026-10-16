package com.v2ray.ang.dto

data class GroupMapItem(
    var id: String,
    var remarks: String,
    var trafficUsed: Long = -1,
    var trafficTotal: Long = -1,
    var expireAt: Long = -1,
)
