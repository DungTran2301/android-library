package com.nartgnud.core.ui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

abstract class BaseFragment<V : ViewDataBinding>: Fragment() {
    protected lateinit var binding: V
    private val isBindingInitialized get() = this::binding.isInitialized
    private var mView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (mView != null) {
            mView
        }
        else {
            binding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
            binding.lifecycleOwner = this
            mView = binding.root
            performBeforeInitView()
            initView()
            binding.root
        }
    }

    abstract fun getLayout(): Int
    abstract fun initView()
    abstract fun performBeforeInitView()

    override fun onDestroy() {
        super.onDestroy()
        if (isBindingInitialized)
            binding.unbind()
    }
}