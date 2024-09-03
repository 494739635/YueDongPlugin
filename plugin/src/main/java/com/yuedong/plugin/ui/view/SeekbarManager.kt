package com.yuedong.plugin.ui.view

import android.view.View
import android.widget.TextView
import com.yuedong.plugin.ui.model.RenderType
import com.yuedong.plugin.ui.utils.standardProgressToDataProgress

class SeekbarManager(
    private val beautySeekBar: ProgressBar,
    private val beautySeekBarTitle: TextView,
    private val filterSeekBar: ProgressBar,
    private val filterSeekBarTitle: TextView
) {

    private var max = 100
    private var min = 0

    private lateinit var renderType: RenderType

    var beautySeekBarOnDragToRender: ((renderType: RenderType) -> Unit)? = null
    var filterSeekBarOnDragToRender: ((renderType: RenderType) -> Unit)? = null

    var changeValueInListByDragBeautySeekbar: ((Int) -> Unit)? = null
    var changeValueInListByDragFilterSeekbar: ((Int) -> Unit)? = null

    var updateRecyclerRateByDragBeautySeekbar: (() -> Unit)? = null

    fun initSeekBar() {
        beautySeekBar.setOnProgressChangedListener(object : ProgressBar.OnProgressChangedListener {

            override fun onProgressChanged(
                progressBar: ProgressBar?, progress: Float, isFormUser: Boolean
            ) {
                if (isFormUser) {
                    linkedEffectValueByDrag?.invoke(renderType)
                    //修改List里面的数值，做到SeekBar和List里面同步
                    changeValueInListByDragBeautySeekbar?.invoke((progress * 100).toInt())
                    //修改RecyclerView下方的数字
                    updateRecyclerRateByDragBeautySeekbar?.invoke()
                    //对外的回调
                    beautySeekBarOnDragToRender?.invoke(renderType)
                }
            }

            override fun onProgressEnd(
                progressBar: ProgressBar?, progress: Float, isFormUser: Boolean
            ) {
            }
        })
        filterSeekBar.setOnProgressChangedListener(object : ProgressBar.OnProgressChangedListener {
            override fun onProgressChanged(
                progressBar: ProgressBar?, progress: Float, isFormUser: Boolean
            ) {

                if (isFormUser) {
                    //修改List里面的数值，做到SeekBar和List里面同步
                    changeValueInListByDragFilterSeekbar?.invoke((progress * 100).toInt())
                    //SeekBar的对外回调
                    filterSeekBarOnDragToRender?.invoke(renderType)
                }
            }

            override fun onProgressEnd(
                progressBar: ProgressBar?, progress: Float, isFormUser: Boolean
            ) {

            }


        })
    }

    private fun TextView.updateProgressText(progress: Int) {
        val pText = standardProgressToDataProgress(max.toFloat(), min.toFloat(), progress)
        this.text = pText.toInt().toString()
    }

    fun changeRenderType(renderType: RenderType) {
        this.renderType = renderType
    }

    fun changeMaxAndMin(max: Int, min: Int) {
        this.max = max
        this.min = min
    }

    fun changeProgress(progressArray: IntArray) {
        when (progressArray.size) {
            1 -> {
                beautySeekBar.progress = progressArray[0] / 100.0f
            }

            2 -> {
                beautySeekBar.progress = progressArray[0] / 100.0f
                filterSeekBar.progress = progressArray[1] / 100.0f
            }
        }
    }

    fun showSeekBar(count: Int) {
        when (count) {
            0 -> {
                beautySeekBar.visibility = View.GONE
                beautySeekBarTitle.visibility = View.GONE

                filterSeekBar.visibility = View.GONE
                filterSeekBarTitle.visibility = View.GONE
            }

            1 -> {
                beautySeekBar.visibility = View.VISIBLE
                beautySeekBarTitle.visibility = View.GONE

                filterSeekBar.visibility = View.GONE
                filterSeekBarTitle.visibility = View.GONE
            }

            2 -> {
                beautySeekBar.visibility = View.VISIBLE
                beautySeekBarTitle.visibility = View.VISIBLE

                filterSeekBar.visibility = View.VISIBLE
                filterSeekBarTitle.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 由于点击 导致的渲染 联动反应
     */
    var linkedEffectValueByDrag: ((renderType: RenderType) -> Unit)? = null

    internal fun resetSeekbar(progress: IntArray) {
        when (progress.size) {
            1 -> beautySeekBar.progress = progress[0] / 100.0f
            2 -> {
                beautySeekBar.progress = progress[0] / 100.0f
                filterSeekBar.progress = progress[1] / 100.0f
            }
        }
    }
}