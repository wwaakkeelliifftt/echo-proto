package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.echo_proto.databinding.FragmentAudioplayerDescriptionBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AudioPlayerDescriptionFragment : Fragment() {

    private var _binding: FragmentAudioplayerDescriptionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAudioplayerDescriptionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("\n\nLAUNCH ------>>>>>    AudioPlayerDescriptionFragment    <<<<<----------\n\n")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}