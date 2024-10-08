package com.live.ss

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import com.yuedong.plugin.live.play.LebPlayEnterActivity
import com.yuedong.plugin.live.play.LivePlayEnterActivity
import com.yuedong.plugin.live.push.LivePushCameraEnterActivity
import com.yuedong.plugin.beauty.BeautyPreviewActivity

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
            val intent = Intent(this@MainActivity, BeautyPreviewActivity::class.java)
            startActivity(intent)
        }
    }
}