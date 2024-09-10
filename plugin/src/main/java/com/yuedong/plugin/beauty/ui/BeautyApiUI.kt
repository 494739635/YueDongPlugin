package com.yuedong.plugin.beauty.ui

import android.content.Context
import android.util.ArrayMap
import android.util.Log
import androidx.annotation.Keep
import com.yuedong.plugin.beauty.logic.config.AssetsLoader
import com.cosmos.beauty.filter.BeautyType
import com.cosmos.beauty.module.beauty.AutoBeautyType
import com.cosmos.beauty.module.beauty.MakeupType
import com.yuedong.plugin.beauty.logic.BeautyApi
import com.yuedong.plugin.beauty.logic.type.BeautyBodyStyleLookupTypeMap
import com.yuedong.plugin.beauty.logic.type.BeautyInnerTypeMap
import com.yuedong.plugin.beauty.logic.type.BeautyTypeMap
import com.yuedong.plugin.beauty.logic.type.BeautyTypeReverseMap
import com.yuedong.plugin.beauty.logic.type.MakeupLipTextureMap
import com.yuedong.plugin.beauty.logic.type.MakeupTypeMap
import com.yuedong.plugin.beauty.logic.type.OneKeyBeautyTypeMap
import com.yuedong.plugin.beauty.ui.config.JsonParser
import com.yuedong.plugin.beauty.ui.model.LookupType
import com.yuedong.plugin.beauty.ui.model.OneKeyBeautyType
import com.yuedong.plugin.beauty.ui.model.TabData
import com.yuedong.plugin.beauty.ui.model.clearLookupByClick
import com.yuedong.plugin.beauty.ui.model.clearMakeupStyleByClick
import com.yuedong.plugin.beauty.ui.model.clearMakeupTypeByClick
import com.yuedong.plugin.beauty.ui.model.clearOneKeyBeautyByClick
import com.yuedong.plugin.beauty.ui.model.clearStickerByClick
import com.yuedong.plugin.beauty.ui.model.onClearBeautyBodyClickListener
import com.yuedong.plugin.beauty.ui.model.prepareBeautyByClick
import com.yuedong.plugin.beauty.ui.model.prepareMakeupTypeByClick
import com.yuedong.plugin.beauty.ui.model.renderBeautyTypeByDrag
import com.yuedong.plugin.beauty.ui.model.renderLookupByClick
import com.yuedong.plugin.beauty.ui.model.renderLookupByDrag
import com.yuedong.plugin.beauty.ui.model.renderMakeupStyleByClick
import com.yuedong.plugin.beauty.ui.model.renderMakeupStyleLookupByDrag
import com.yuedong.plugin.beauty.ui.model.renderMakeupStyleMakeupByDrag
import com.yuedong.plugin.beauty.ui.model.renderMakeupTypeByClick
import com.yuedong.plugin.beauty.ui.model.renderMakeupTypeByDrag
import com.yuedong.plugin.beauty.ui.model.renderOneKeyBeautyByClick
import com.yuedong.plugin.beauty.ui.model.renderStickerByClick
import com.yuedong.plugin.beauty.ui.view.BeautyDialogFragment
import java.io.File

@Keep
class BeautyApiUI(context: Context) : BeautyApi(context) {

    lateinit var panelFragment: BeautyDialogFragment

    var render = false
    private lateinit var beautyTabDataList: List<TabData>
    private lateinit var stickerTabDataList: List<TabData>

    private val configLoader: AssetsLoader by lazy { AssetsLoader(context.filesDir.absolutePath) }
    private val jsonParser: JsonParser by lazy { JsonParser(context.filesDir.absolutePath + "/cosmos/config") }

    override fun init(license: String, loadSuccess: (Boolean) -> Unit) {
        super.init(license) {
            if (it) {
                render = true
                beautyTabDataList = jsonParser.initBeautyTabDataList()
                stickerTabDataList = jsonParser.initStickerTabDataList()
                initTypeBehavior()
                initFragment()
            }
            loadSuccess.invoke(it)
        }
    }

    private fun initFragment() {
        panelFragment = BeautyDialogFragment.newInstance(configLoader.rootDir)
        panelFragment.renderCompareOnTouchDownListener = { render = false }
        panelFragment.renderCompareOnTouchUpListener = { render = true }
        panelFragment.beautyTabDataList = { beautyTabDataList }
        panelFragment.stickerTabDataList = { stickerTabDataList }
        panelFragment.initBeautyPanelFragment = { beautyPanelFragment ->
            beautyPanelFragment.changeBeautyAndMicroByRenderOneKeyBeauty = { oneKeyBeautyType ->
                val resultMap = ArrayMap<Int, Float>()
                OneKeyBeautyTypeMap[oneKeyBeautyType.id]?.let { typeId ->
                    beautyModule?.getAutoValues(typeId)?.forEach { entry ->
                        resultMap[BeautyTypeReverseMap[entry.key]] = entry.value
                    }
                }
                resultMap
            }
            beautyPanelFragment.initEffect = { oneKeyBeautyType, lookupType ->
                renderOneKeyBeauty(oneKeyBeautyType)
                renderLookupDirectly(lookupType)
            }
            beautyPanelFragment.prepareInLevel1 = { it.prepareInLevel1() }
            beautyPanelFragment.renderInLevel1 = { it.renderInLevel1() }
            beautyPanelFragment.clearInLevel1 = { it.clearInLevel1() }
            beautyPanelFragment.removeLookupByMakeupStyle = { lookupModule?.clear() }
            beautyPanelFragment.clearMakeupByMakeupStyle = { makeupModule?.clear() }
            beautyPanelFragment.prepareInLevel2 = { it.prepareInLevel2() }
            beautyPanelFragment.renderInLevel2 = { it.renderInLevel2() }
            beautyPanelFragment.clearInLevel2 = { it.clearInLevel2() }
            beautyPanelFragment.removeMakeupStyleByMakeup = {
                makeupModule?.clear()
            }
            beautyPanelFragment.beautySeekBarRender = {
                it.renderForParam1ByDrag()
            }
            beautyPanelFragment.filterSeekBarRender = { it.renderForParam2ByDrag() }
            beautyPanelFragment.resetOneKeyBeauty = {
                OneKeyBeautyTypeMap[it.id]?.let { type ->
                    beautyModule?.setAutoBeauty(type)
                }
            }
            beautyPanelFragment.resetBeauty = {
                BeautyInnerTypeMap[it.innerType]?.let { beautyType ->
                    when (beautyType) {
                        is BeautyType.WHITETYPE -> beautyModule?.setWhiteType(beautyType)
                        is BeautyType.RUDDYTYPE -> beautyModule?.setRuddyType(beautyType)
                        else -> {
                        }
                    }
                }
                MakeupTypeMap[it.id]?.let { simpleBeautyType ->
                    makeupModule?.setValue(simpleBeautyType, it.value)
                }
            }
            beautyPanelFragment.resetMakeupStyle = {
                makeupModule?.clear()
            }
            beautyPanelFragment.resetMakeupInner = {
                makeupModule?.clear()
            }
            beautyPanelFragment.resetLookup = {
                lookupModule?.clear()
                lookupModule?.setEffect(it.path.toAbPath())
                lookupModule?.setIntensity(it.value)
            }
            beautyPanelFragment.lipTextTureToChange = {
                MakeupLipTextureMap[it]?.let { lipTexture ->
                    makeupModule?.changeLipTextureType(lipTexture)
                }
            }
        }
//        panelFragment.initStickerPanelFragment = {
//            it.renderInLevel1 = { renderType ->
//                renderType.renderInLevel1()
//            }
//            it.clearInLevel1 = { renderType ->
//                renderType.clearInLevel1()
//            }
//            it.resetSticker = {
//                stickerModule?.clear()
//            }
//        }
    }

    private fun initTypeBehavior() {
        /**
         * 一键美颜
         */
        renderOneKeyBeautyByClick = {
            OneKeyBeautyTypeMap[it]?.let { type ->
                Log.i("Beauty", "beautyModule setAutoBeauty $type")
                beautyModule?.setAutoBeauty(type)
            }
        }
        clearOneKeyBeautyByClick = {
            Log.i("Beauty", "beautyModule setAutoBeauty null")
            beautyModule?.setAutoBeauty(AutoBeautyType.AUTOBEAUTY_NULL)
        }
        /**
         * 美颜
         */
        prepareBeautyByClick = { innerType ->
            BeautyInnerTypeMap[innerType]?.let { beautyType ->

                Log.i("Beauty", "beautyModule $beautyType")

                when (beautyType) {
                    is BeautyType.WHITETYPE -> beautyModule?.setWhiteType(beautyType)
                    is BeautyType.RUDDYTYPE -> beautyModule?.setRuddyType(beautyType)
                }
            }
        }
        renderBeautyTypeByDrag = { type, value ->
            MakeupTypeMap[type]?.let {

                Log.i("Beauty", "makeupModule setValue $it $value")
                makeupModule?.setValue(it, value)
            }
            BeautyTypeMap[type]?.let {

                Log.i("Beauty", "beautyModule setValue $it $value")
                beautyModule?.setValue(it, value * 2)
            }
        }
        /**
         * 风格妆
         */
        renderMakeupStyleByClick = { styleMakeupType, styleLookupType ->

            Log.i("Beauty", "makeupModule addMakeup ${styleMakeupType.path}")

            makeupModule?.clear()
            makeupModule?.addMakeup(styleMakeupType.path.toAbPath())
            makeupModule?.setValue(MakeupType.MAKEUP_STYLE, styleMakeupType.value)
            makeupModule?.setValue(MakeupType.MAKEUP_LUT, styleLookupType.value)
        }
        clearMakeupStyleByClick = {
            Log.i("Beauty", "makeupModule clear")
            makeupModule?.clear()
        }
        renderMakeupStyleMakeupByDrag = { makeupType ->

            Log.i("Beauty", "makeupModule setValue ${MakeupType.MAKEUP_STYLE} ${makeupType.value}")

            makeupModule?.setValue(MakeupType.MAKEUP_STYLE, makeupType.value)
        }
        renderMakeupStyleLookupByDrag = { lookupType ->

            Log.i("Beauty", "makeupModule setValue ${MakeupType.MAKEUP_LUT} ${lookupType.value}")
            makeupModule?.setValue(MakeupType.MAKEUP_LUT, lookupType.value)
        }

        /**
         * 美妆
         */
        prepareMakeupTypeByClick = { type, path ->
            MakeupTypeMap[type]?.let { simpleBeautyType ->
                Log.i("Beauty", "makeupModule addMakeup ${simpleBeautyType.name}")
                makeupModule?.addMakeup(path.toAbPath())
            }
        }
        renderMakeupTypeByClick = { type, value ->
            MakeupTypeMap[type]?.let { simpleBeautyType ->
                Log.i("Beauty", "makeupModule setValue ${simpleBeautyType.name} $value")
                makeupModule?.setValue(simpleBeautyType, value)
            }
        }
        clearMakeupTypeByClick = { type ->
            MakeupTypeMap[type]?.let { simpleBeautyType ->
                Log.i("Beauty", "makeupModule removeMakeup ${simpleBeautyType.name} ")

                makeupModule?.removeMakeup(simpleBeautyType)
            }
        }
        renderMakeupTypeByDrag = { typeId, value ->
            MakeupTypeMap[typeId]?.let { simpleBeautyType ->
                Log.i("Beauty", "makeupModule setValue ${simpleBeautyType.name} $value ")
                makeupModule?.setValue(simpleBeautyType, value)
            }
        }
        /**
         * 滤镜
         */
        renderLookupByClick = { path, value ->

            Log.i("Beauty", "lookupModule setEffect $path $value ")

            lookupModule?.clear()
            lookupModule?.setEffect(path.toAbPath())
            lookupModule?.setIntensity(value)
        }
        clearLookupByClick = {

            Log.i("Beauty", "lookupModule clear")

            lookupModule?.clear()
        }
        renderLookupByDrag = { value ->

            Log.i("Beauty", "lookupModule setIntensity $value")
            lookupModule?.setIntensity(value)
        }
        /**
         * 贴纸
         */
        renderStickerByClick = { path ->
            Log.i("Beauty", "stickerModule addMaskModel $path ")

            stickerModule?.clear()
            stickerModule?.addMaskModel(File(path.toAbPath())) {
                com.yuedong.plugin.beauty.ui.dLog("${it == null}")
            }
        }
        clearStickerByClick = {
            Log.i("Beauty", "stickerModule clear ")
            stickerModule?.clear()
        }

        /**
         * 美体
         */
        onClearBeautyBodyClickListener = { innerType, value ->

            Log.i("Beauty", "bodyModule setValue ${BeautyBodyStyleLookupTypeMap[innerType]} $value")

            bodyModule?.setValue(BeautyBodyStyleLookupTypeMap[innerType]!!, value)
        }
    }

    private fun renderOneKeyBeauty(oneKeyBeautyType: OneKeyBeautyType) {
        OneKeyBeautyTypeMap[oneKeyBeautyType.id]?.let {
            beautyModule?.setAutoBeauty(it)
        }
    }

    private fun renderLookupDirectly(lookupType: LookupType) {
        lookupModule?.setEffect(lookupType.path.toAbPath())
        lookupModule?.setIntensity(lookupType.value)
    }

    private fun String.toAbPath() = "${configLoader.rootDir}/$this"
}