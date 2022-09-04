package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.echo_proto.databinding.FragmentAudioplayerDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AudioPlayerDetailFragment : Fragment() {

    private var _binding: FragmentAudioplayerDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAudioplayerDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("\n\nLAUNCH ------>>>>>    AudioPlayerDetailFragment    <<<<<----------\n\n")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}