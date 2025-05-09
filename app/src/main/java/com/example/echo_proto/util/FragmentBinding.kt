package com.example.echo_proto.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class FragmentBinding<out T: ViewBinding> : Fragment() {

    protected abstract val bindingInflater : (LayoutInflater) -> ViewBinding

    private var _binding: ViewBinding? = null
    @Suppress("UNCHECKED_CAST")
    val binding: T
        get() = _binding!! as T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}