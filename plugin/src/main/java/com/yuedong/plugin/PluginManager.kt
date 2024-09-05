package com.yuedong.plugin

import android.content.Context
import android.widget.Toast

object PluginManager {

    fun showToastForAndroid(context: Context) {
        Toast.makeText(context, "Hello UNI", Toast.LENGTH_LONG).show()
    }
}