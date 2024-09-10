package com.yuedong.plugin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import com.live.ss.R
import com.tencent.live2.V2TXLiveDef.V2TXLiveLogConfig
import com.tencent.live2.V2TXLivePremier
import com.yuedong.plugin.beauty.BeautyPreviewActivity
import com.yuedong.plugin.live.play.LebPlayEnterActivity
import com.yuedong.plugin.live.play.LivePlayEnterActivity
import com.yuedong.plugin.live.push.LivePushCameraEnterActivity
import com.yuedong.plugin.util.GenerateTestUserSig

class DemoActivity : ComponentActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        val liveLogConfig = V2TXLiveLogConfig()
        liveLogConfig.enableConsole = true
        V2TXLivePremier.setLogConfig(liveLogConfig)
        V2TXLivePremier.setLicence(
            this,
            GenerateTestUserSig.LICENSEURL,
            GenerateTestUserSig.LICENSEURLKEY
        )

        findViewById<View>(R.id.ll_push_camera).setOnClickListener {
            val intent =
                Intent(this@DemoActivity, LivePushCameraEnterActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.ll_play).setOnClickListener {
            val intent = Intent(this@DemoActivity, LivePlayEnterActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.ll_leb_play).setOnClickListener {
            val intent = Intent(this@DemoActivity, LebPlayEnterActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.ll_leb_beauty).setOnClickListener {
            val intent = Intent(this@DemoActivity, BeautyPreviewActivity::class.java)
            startActivity(intent)
        }
    }
}