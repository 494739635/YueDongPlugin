package com.yuedong.plugin.beauty.ui.utils

import android.content.res.Resources
import android.util.TypedValue

fun Number.toPX(): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(),
        Resources.getSystem().displayMetrics
    )
}
