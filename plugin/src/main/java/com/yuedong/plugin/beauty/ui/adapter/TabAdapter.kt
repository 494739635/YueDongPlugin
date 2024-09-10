package com.yuedong.plugin.beauty.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yuedong.plugin.R
import com.yuedong.plugin.beauty.ui.model.TabData
import com.yuedong.plugin.beauty.ui.view.TextViewCanSelect

//import com.yuedong.plugin.beauty.ui.view.TextViewCanSelect

internal class TabAdapter(private val tabDataList: List<TabData>, private val defaultChoice: Int) :
    RecyclerView.Adapter<TabAdapter.ViewHolder>() {
    var currentSelectPosition: Int = 0

    init {
        currentSelectPosition = defaultChoice
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextViewCanSelect = view.findViewById(R.id.type1_name)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        choose(defaultChoice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_text, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != currentSelectPosition) {
                choose(position)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tabData = tabDataList[position]
        holder.name.text = tabData.name
        holder.name.isSelect = position == currentSelectPosition
    }


    /**
     * 选择之后的操作
     */
    var onItemChooseListener: ((Int) -> Unit)? = null
    private fun choose(position: Int) {
        val preSelectPosition = currentSelectPosition
        currentSelectPosition = position

        notifyItemChanged(currentSelectPosition)
        notifyItemChanged(preSelectPosition)

        onItemChooseListener?.invoke(position)
    }

    override fun getItemCount(): Int = tabDataList.size
}
