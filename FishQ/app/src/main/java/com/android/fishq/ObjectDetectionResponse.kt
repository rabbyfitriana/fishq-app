package com.android.fishq

data class ObjectDetectionResponse(
    var total: Int,
    var fresh: Int,
    var no_fresh: Int,
    var image_data: String
)
