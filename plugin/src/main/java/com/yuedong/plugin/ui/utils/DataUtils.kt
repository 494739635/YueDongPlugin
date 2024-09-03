package com.yuedong.plugin.ui.utils

import android.os.Build
import androidx.annotation.RequiresApi

/**
 * 标准进度为 [0-100]
 * 这个函数可以把 数据中的进度值 进行转换
 * 返回一个 标准的进度值
 * @param max 数据最大值
 * @param min 数据最小值
 * @param dataProgress 数据进度值
 */
fun dataProgressToStandardProgress(max: Float, min: Float, dataProgress: Float): Int {
    checkDataProgress(dataProgress, min, max)
    return ((dataProgress - min) / (max - min) * 100).toInt()
}

/**
 * 标准进度为 0~100
 * 这个函数可以把 标准的进度值 进行转换
 * 返回一个 数据中的进度值
 * @param max 数据最大值
 * @param min 数据最小值
 * @param standardProgress 标准进度值 [0-100]
 */
fun standardProgressToDataProgress(max: Float, min: Float, standardProgress: Int): Float {
    checkStandardProgress(standardProgress)
    return min + (standardProgress * (max - min) / 100f)
}

private fun checkDataProgress(progress: Float, min: Float, max: Float) {
    if (progress < min) {
        throw IllegalProgressException("progress < min is not allow")
    }
    if (max < min) {
        throw IllegalProgressException("max < min is not allow")
    }
    if (max == min) {
        throw IllegalProgressException("max = min is not allow")
    }
}

private fun checkStandardProgress(progress: Int) {
    if (progress < 0) {
        throw IllegalProgressException("StandardProgress < 0 is not allow")
    }
    if (progress > 100) {
        throw IllegalProgressException("StandardProgress > 100 is not allow")
    }
}

data class Range(val max: Float, val min: Float)

/**
 * 定义了特殊的类型对应的range
 * 3002 对应 削脸 详情见 #com.cosmos.config_loader.bean.type
 */
private val Beauty_Range_Map = mapOf(
    Pair(3002, Range(1f, -1f)),
    Pair(3004, Range(1f, -1f)),
    Pair(3005, Range(1f, -1f)),
    Pair(3008, Range(1f, -1f)),
    Pair(3009, Range(1f, -1f)),
    Pair(3010, Range(1f, -1f)),
    Pair(3011, Range(1f, -1f)),
    Pair(3012, Range(1f, -1f)),
    Pair(3013, Range(1f, -1f)),
    Pair(3014, Range(1f, -1f)),
    Pair(3016, Range(1f, -1f)),
    Pair(3017, Range(1f, -1f)),
    Pair(8000, Range(1f, 0f)),
    Pair(8001, Range(1f, 0f))
)

fun getDataRange(typeId: Int): Range {
    var range = Beauty_Range_Map[typeId]
    if (range == null) {
        range = Range(1f, 0f)
    }
    return range
}

class IllegalProgressException : Exception {
    constructor() : this("")
    constructor(message: String?) : this(message, null)
    constructor(cause: Throwable?) : this("", cause)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    @RequiresApi(Build.VERSION_CODES.N)
    constructor(
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)
}