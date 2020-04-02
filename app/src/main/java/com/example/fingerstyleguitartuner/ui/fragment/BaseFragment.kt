package com.example.fingerstyleguitartuner.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.Unbinder


open class BaseFragment : Fragment() {
    private var unbinder: Unbinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDaggerComponent()
    }

    protected fun inflateAndBindView(
        @NonNull inflater: LayoutInflater,
        @LayoutRes layoutRes: Int,
        container: ViewGroup?,
        attachToRoot: Boolean
    ): View {
        val v = inflater.inflate(layoutRes, container, attachToRoot)
        unbinder = ButterKnife.bind(this, v)
        return v
    }

    protected fun setupDaggerComponent() {
        // Default is no operation
    }
}
