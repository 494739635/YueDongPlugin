package com.yuedong.plugin.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.yuedong.plugin.databinding.FragmentPanelBinding
import com.yuedong.plugin.ui.dLog
import com.yuedong.plugin.ui.model.TabData
import com.yuedong.plugin.ui.rootDir

/**
 * 美颜窗口
 * **/
class BeautyDialogFragment : DialogFragment() {
    companion object {
        @JvmStatic
        fun newInstance(rootDirPath: String) = BeautyDialogFragment().apply {
            rootDir = rootDirPath
        }
    }

    private val beautyPanelFragment = BeautyPanelFragment.newInstance()
//    private val stickerPanelFragment = StickerPanelFragment.newInstance()
    private lateinit var currentFragment: Fragment

    var renderCompareOnTouchDownListener: (() -> Unit)? = null
    var renderCompareOnTouchUpListener: (() -> Unit)? = null
    lateinit var beautyTabDataList: (() -> List<TabData>)
    var initBeautyPanelFragment: ((BeautyPanelFragment) -> Unit)? = null
//    var initStickerPanelFragment: ((StickerPanelFragment) -> Unit)? = null
    lateinit var stickerTabDataList: (() -> List<TabData>)
    private var binding: FragmentPanelBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentFragment = beautyPanelFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPanelBinding.inflate(inflater, container, false)
        initBeautyPanelFragment()
//        initStickerPanelFragment()
        addFragment(currentFragment)
        initOnClickListener()
        return binding!!.root
    }

    private fun initOnClickListener() {
        binding?.beauty?.setOnClickListener {
            replaceFragment(beautyPanelFragment)
            showPanel()
        }
//        binding?.sticker?.setOnClickListener {
//            replaceFragment(stickerPanelFragment)
//            showPanel()
//        }
    }

    fun hidePanel() {
        binding?.panel?.animate()?.translationY(binding!!.panel.height.toFloat())?.setDuration(250)
            ?.withEndAction {
                binding?.panel?.visibility = View.GONE
                binding?.beauty?.visibility = View.VISIBLE
//                binding?.sticker?.visibility = View.VISIBLE
            }?.start()
    }

    private fun showPanel() {
        binding?.panel?.translationY = binding!!.panel.height.toFloat() / 2
        binding?.panel?.alpha = 0.4f
        binding?.panel?.visibility = View.VISIBLE
        binding?.beauty?.visibility = View.GONE
//        binding?.sticker?.visibility = View.GONE
        binding?.panel?.animate()?.translationY(0f)?.alpha(1f)?.setDuration(200)?.start()
    }

    private fun initBeautyPanelFragment() {
        beautyPanelFragment.renderCompareOnTouchDownListener = {
            renderCompareOnTouchDownListener?.invoke()
        }
        beautyPanelFragment.renderCompareOnTouchUpListener = {
            renderCompareOnTouchUpListener?.invoke()
        }
        beautyPanelFragment.tabDataList = beautyTabDataList.invoke()
        initBeautyPanelFragment?.invoke(beautyPanelFragment)
    }

//    private fun initStickerPanelFragment() {
//        stickerPanelFragment.tabDataList = stickerTabDataList.invoke()
//        initStickerPanelFragment?.invoke(stickerPanelFragment)
//    }

    private fun addFragment(fragment: Fragment) {
        val fragmentManager = childFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(binding!!.panel.id, fragment)
        transaction.commit()
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = childFragmentManager
        val transaction = fragmentManager.beginTransaction()
        if (fragment.isAdded) {
            transaction.hide(currentFragment)
            transaction.show(fragment)
        } else {
            transaction.hide(currentFragment)
            transaction.add(binding!!.panel.id, fragment)
        }
        currentFragment = fragment
        transaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        dLog("PanelFragment onDestroy")
        binding = null
    }
}