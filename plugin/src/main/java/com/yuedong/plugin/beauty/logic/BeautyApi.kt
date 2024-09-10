package com.yuedong.plugin.beauty.logic

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.yuedong.plugin.beauty.logic.config.AssetsLoader
import com.cosmos.beauty.module.IMMRenderModuleManager
import com.cosmos.beauty.module.beauty.IBeautyBodyModule
import com.cosmos.beauty.module.beauty.IBeautyModule
import com.cosmos.beauty.module.lookup.ILookupModule
import com.cosmos.beauty.module.makeup.IMakeupBeautyModule
import com.cosmos.beauty.module.sticker.IStickerModule
import java.io.File
import kotlin.concurrent.thread

/**
 *
 * **/
open class BeautyApi(var context: Context) {
    val sdkApi: SdkApi = SdkApi()
    val lookupModule: ILookupModule?
        get() = sdkApi.lookupModule
    val makeupModule: IMakeupBeautyModule?
        get() = sdkApi.makeupModule
    val beautyModule: IBeautyModule?
        get() = sdkApi.beautyModule
    val stickerModule: IStickerModule?
        get() = sdkApi.stickerModule
    val bodyModule: IBeautyBodyModule?
        get() = sdkApi.beautyBodyModule
    open val renderModuleManager: IMMRenderModuleManager?
        get() = sdkApi.renderModuleManager

    private val configLoader: AssetsLoader by lazy { AssetsLoader(context.filesDir.absolutePath) }

    @Volatile
    open var resourceReady = false

    fun renderWithOESTexture(
        texture: Int, texWidth: Int, texHeight: Int, mFrontCamera: Boolean, cameraRotation: Int
    ): Int {
        return if (resourceReady && renderModuleManager != null) {
            renderModuleManager!!.renderOESFrame(
                texture, texWidth, texHeight, cameraRotation, mFrontCamera, true
            )
        } else texture
    }

    fun renderWithTexture(
        texture: Int, texWidth: Int, texHeight: Int, mFrontCamera: Boolean, cameraRotation: Int
    ): Int {
        //revertOutTexture 是否转正纹理 true时 不管传入rotation为什么，输出正图像；false时保持原纹理图像rotation
        return if (resourceReady && renderModuleManager != null) {
            renderModuleManager!!.renderFrame(
                texture, texWidth, texHeight, cameraRotation, mFrontCamera, true
            )
        } else texture
    }

    /**
     * 解压资源、验证授权
     * **/
    open fun init(license: String, loadSuccess: (Boolean) -> Unit) {
        val assetsPath = File(context.filesDir, "model-all")
        thread {
            configLoader.loadCosmosZip(context) { copySuccess, unZipSuccess ->
                Log.i("Beauty", "loadCosmosZip  $unZipSuccess $copySuccess.")
                if (copySuccess && unZipSuccess) {
                    Handler(Looper.getMainLooper()).post {
                        sdkApi.init(context, assetsPath.absolutePath, license) {
                            Log.i("Beauty", "beauty init res $it.")
                            resourceReady = it
                            Handler(Looper.getMainLooper()).post {
                                loadSuccess(it)
                            }
                        }
                    }

                } else {
                    Handler(Looper.getMainLooper()).post {
                        loadSuccess(false)
                    }
                }
            }
        }
    }

    fun initRender() {
        sdkApi.initRenderManager();
    }

    open fun textureDestroyed() {
        if (renderModuleManager != null) {
            if (makeupModule != null) {
                renderModuleManager!!.unRegisterModule(makeupModule!!)
            }
            if (lookupModule != null) {
                renderModuleManager!!.unRegisterModule(lookupModule!!)
            }
            if (beautyModule != null) {
                renderModuleManager!!.unRegisterModule(beautyModule!!)
            }
            if (stickerModule != null) {
                renderModuleManager!!.unRegisterModule(stickerModule!!)
            }
            if (bodyModule != null) {
                renderModuleManager!!.unRegisterModule(bodyModule!!)
            }
            renderModuleManager!!.destroyModuleChain()
            renderModuleManager!!.release()
        }
    }
}