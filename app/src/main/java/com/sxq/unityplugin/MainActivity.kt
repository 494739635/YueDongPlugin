package com.sxq.unityplugin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import com.live.ss.R
import com.sxq.unityplugin.ui.play.LebPlayEnterActivity
import com.sxq.unityplugin.ui.play.LivePlayEnterActivity
import com.sxq.unityplugin.ui.push.LivePushCameraEnterActivity
import com.yuedong.plugin.LivePreviewActivity

class MainActivity : ComponentActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.ll_push_camera).setOnClickListener {
            val intent =
                Intent(this@MainActivity, LivePushCameraEnterActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.ll_play).setOnClickListener {
            val intent = Intent(this@MainActivity, LivePlayEnterActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.ll_leb_play).setOnClickListener {
            val intent = Intent(this@MainActivity, LebPlayEnterActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.ll_leb_beauty).setOnClickListener {
            val intent = Intent(this@MainActivity, LivePreviewActivity::class.java)
            startActivity(intent)
        }
    }
}