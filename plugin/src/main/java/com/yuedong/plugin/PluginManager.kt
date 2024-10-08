package com.yuedong.plugin

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.yuedong.plugin.beauty.ui.BeautyApiUI

object PluginManager {

    fun initBeauty(context: Context) {
        Toast.makeText(context, "开始初始化美颜", Toast.LENGTH_LONG).show()
        val beautyApi = com.yuedong.plugin.beauty.ui.BeautyApiUI(context)
        beautyApi.init("QzZXcURtei92SnluTUlHZE1BMEdDU3FHU0liM0RRRUJBUVVBQTRHTEFEQ0JUSjNod0tCZ1FDV0grK21SV1JIejVJY29uUFV6MDZRQTBmOTYrYTVxCklHZWcwZkFZdE5KYXR1ZzlOQXIvWm85Z1NjM05TWHoxSGg2bGRCaEp4MysxUGRlaW5saW9zbTVnR3c5cWJtVmI2MlZiL3B0NnZQRUhDMEMzbHFkOVMKZFRPZVZPdU9QNEhYaEhxSlE3MXZmWVJBSFNhdnFtenJjNWFIS3Q5TDhKZGllNThaQUoydktic1kwNWtGZEF3MEJDSFdWU3huSWQ2SDBCZG9lU29rOU1ON3pMUGZYVEQ1Y3o5MUpyWmg2VmJ6RDhIQW0vSGtyaisKQkR0RHBnbmpNcEZIUERScHdRSUJBd0VBVHZESFV4dXE0bWJycjhva1B4ODNmQnJmR21taHRJSjJZZFNlemV3RS9pZXR3cXh6eVIvSE5yOGU1Z2dhRkI5VlBoUXNKMjkrbz0=") {
            Toast.makeText(context, "init beautySdk res $it", Toast.LENGTH_LONG).show()
            if (it) {
                beautyApi.initRender()
            }
        }
    }

    fun showToastForAndroid(context: Context) {
        Toast.makeText(context, "Hello UNI，来自Android的Toast", Toast.LENGTH_LONG).show()
    }

    fun startDemoActivity(context: Context) {
        context.startActivity(Intent(context, DemoActivity::class.java))
    }

}