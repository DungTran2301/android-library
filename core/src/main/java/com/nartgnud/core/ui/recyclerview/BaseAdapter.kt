package com.nartgnud.core.ui.recyclerview

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import java.util.*

abstract class BaseAdapter<T : Any>(
    private val layout: Int
) : RecyclerView.Adapter<BaseViewHolder>() {

    private lateinit var inflater: LayoutInflater

    private var list = mutableListOf<T>()
    var listener: BaseListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        if (!::inflater.isInitialized) {
            inflater = LayoutInflater.from(parent.context)
        }
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            inflater,
            layout,
            parent,
            false
        )
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.binding.apply {
            bindView(list[position], position, listener)
//            val context = root.context as LifecycleOwner
//            lifecycleOwner = context
            executePendingBindings()
        }
    }

    override fun getItemCount(): Int = list.size


    @SuppressLint("NotifyDataSetChanged")
    fun submit(newData : List<T>?){
        val new = newData?.toMutableList()
        this.list = new!!
        notifyDataSetChanged()
    }

    abstract fun bindView(item: T, position: Int, listener: BaseListener?)
}
