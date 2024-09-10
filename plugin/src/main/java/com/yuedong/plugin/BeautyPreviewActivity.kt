package com.yuedong.plugin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.live.ss.R
import com.live.ss.databinding.ActivityBeautyPreviewBinding
import com.yuedong.plugin.camera.Accelerometer
import com.yuedong.plugin.camera.CameraDisplay
import com.yuedong.plugin.ui.BeautyApiUI
import com.yuedong.plugin.ui.bubble.BubbleWindowManager

class BeautyPreviewActivity : AppCompatActivity() {

    private val cameraRequestCode = 12300
    lateinit var mainBinding: ActivityBeautyPreviewBinding
    private var mBubbleWindowManager: BubbleWindowManager? = null

    //画面采集 未使用rtc采集时，通过cameraManager采集摄像头画面
    private lateinit var cameraManager: CameraDisplay
    private var sensor: Accelerometer? = null

    //美颜api
    private val beautyApi by lazy { BeautyApiUI(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityBeautyPreviewBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        initCamera()

        initView()

        initBeauty()
    }

    override fun onResume() {
        super.onResume()
        cameraManager.onResume();
    }

    override fun onPause() {
        super.onPause()
        cameraManager.onPause()
    }

    private fun initView() {
        mBubbleWindowManager = BubbleWindowManager(this)
        mainBinding.root.setOnClickListener {
            beautyApi.panelFragment.hidePanel()
        }
        mainBinding.icSetting.setOnClickListener {
            mBubbleWindowManager!!.show(mainBinding.icSetting,
                object : BubbleWindowManager.BubbleCallback {

                    override fun onBeautyVersionChanged(id: Int) {
                        when (id) {
                            R.id.rb_beauty1 -> {
                                beautyApi.beautyModule?.setFaceLiftType(com.cosmos.beauty.filter.BeautyType.FACELIFTTYPE.FACELIFT_V1)
                            }

                            R.id.rb_beauty2 -> {
                                beautyApi.beautyModule?.setFaceLiftType(com.cosmos.beauty.filter.BeautyType.FACELIFTTYPE.FACELIFT_V2)
                            }

                            R.id.rb_beauty3 -> {
                                beautyApi.beautyModule?.setFaceLiftType(com.cosmos.beauty.filter.BeautyType.FACELIFTTYPE.FACELIFT_V3)
                            }
                        }
                    }

                    override fun onWhiteVersionChanged(id: Int) {
                        when (id) {
                            R.id.rb_white1 -> {
                                beautyApi.beautyModule?.setWhiteType(com.cosmos.beauty.filter.BeautyType.WHITETYPE.WHITE_T1)

                            }

                            R.id.rb_white2 -> {
                                beautyApi.beautyModule?.setWhiteType(com.cosmos.beauty.filter.BeautyType.WHITETYPE.WHITE_T2)

                            }

                            R.id.rb_white3 -> {
                                beautyApi.beautyModule?.setWhiteType(com.cosmos.beauty.filter.BeautyType.WHITETYPE.WHITE_T3)

                            }
                        }
                    }

                    override fun onBuddyVersionChanged(id: Int) {
                        when (id) {
                            R.id.rb_buddy1 -> {
                                beautyApi.beautyModule?.setRuddyType(com.cosmos.beauty.filter.BeautyType.RUDDYTYPE.RUDDY_T1)
                            }

                            R.id.rb_buddy2 -> {
                                beautyApi.beautyModule?.setRuddyType(com.cosmos.beauty.filter.BeautyType.RUDDYTYPE.RUDDY_T2)
                            }
                        }
                    }
                })
        }
        mainBinding.cameraSwitch.setOnClickListener {
            cameraManager.switchCamera()
        }
    }

    /**
     * 初始化美颜api
     * **/
    private fun initBeauty() {
        Log.i("Beauty", "initBeauty.")
        beautyApi.init("QzZXcURtei92SnluTUlHZE1BMEdDU3FHU0liM0RRRUJBUVVBQTRHTEFEQ0JUSjNod0tCZ1FDV0grK21SV1JIejVJY29uUFV6MDZRQTBmOTYrYTVxCklHZWcwZkFZdE5KYXR1ZzlOQXIvWm85Z1NjM05TWHoxSGg2bGRCaEp4MysxUGRlaW5saW9zbTVnR3c5cWJtVmI2MlZiL3B0NnZQRUhDMEMzbHFkOVMKZFRPZVZPdU9QNEhYaEhxSlE3MXZmWVJBSFNhdnFtenJjNWFIS3Q5TDhKZGllNThaQUoydktic1kwNWtGZEF3MEJDSFdWU3huSWQ2SDBCZG9lU29rOU1ON3pMUGZYVEQ1Y3o5MUpyWmg2VmJ6RDhIQW0vSGtyaisKQkR0RHBnbmpNcEZIUERScHdRSUJBd0VBVHZESFV4dXE0bWJycjhva1B4ODNmQnJmR21taHRJSjJZZFNlemV3RS9pZXR3cXh6eVIvSE5yOGU1Z2dhRkI5VlBoUXNKMjkrbz0=") {
            Log.i("Beauty", "init beautySdk res $it")

            if (it) {
                beautyApi.initRender()
            }

            if (!isFinishing && it) {
                val fragmentManager = supportFragmentManager
                val transaction = fragmentManager.beginTransaction()
                transaction.replace(R.id.beauty_panel_fragment, beautyApi.panelFragment)
                transaction.commit()
            }
        }
    }

    /**
     * 初始化相机，用于画面采集；推荐优先使用rtc采集,使用rtc采集时，不需要此逻辑
     * **/
    private fun initCamera() {
        sensor = Accelerometer(this)
        sensor!!.start()
        cameraManager = CameraDisplay(this, mainBinding.surfaceView)
        cameraManager.frameProcessor = CameraDisplay.IFrameProcessor {
            if (beautyApi.render) {
                beautyApi.renderWithTexture(
                    it,
                    cameraManager.previewWidth,
                    cameraManager.previewHeight,
                    true,
                    cameraManager.currentOrientation
                )
            } else {
                it
            }
        }

//        mainBinding.surfaceView.setCameraProxy(cameraManager.mCameraProxy)

        checkAndRequestPermission(cameraRequestCode)
    }

    private fun checkAndRequestPermission(requestCode: Int): Boolean {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), requestCode)
                return false
            }
        }
        return true
    }

    private fun checkPermissionResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ): Boolean {
        if (requestCode == cameraRequestCode && grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkPermissionResult(requestCode, permissions, grantResults)) {
            cameraManager.onResume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensor!!.stop()
        releaseBeauty()
        cameraManager.onDestroy()

    }

    private fun releaseBeauty() {
        beautyApi.lookupModule?.let {
            beautyApi.renderModuleManager?.unRegisterModule(it)
        }
        beautyApi.makeupModule?.let {
            beautyApi.renderModuleManager?.unRegisterModule(it)
        }
        beautyApi.stickerModule?.let {
            beautyApi.renderModuleManager?.unRegisterModule(it)
        }
        beautyApi.renderModuleManager?.release()
    }
}