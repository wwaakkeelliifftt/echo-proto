package com.example.echo_proto.ui.fragments

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.echo_proto.databinding.FragmentAudioplayerDescriptionBinding
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.enrichForWebView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AudioPlayerDescriptionFragment : Fragment() {

    private var _binding: FragmentAudioplayerDescriptionBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAudioplayerDescriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupDescriptionText()
        
        mainViewModel.currentEpisodeFromDb.observe(viewLifecycleOwner) { episode ->
            updateDescription(episode.description)
        }

        binding.dtnToPlayer.setOnClickListener {
            // Возврат к основному экрану плеера во вьюпейджере
            (parentFragment as? AudioPlayerDetailFragment)?.scrollToInfo()
        }
    }

    private fun setupDescriptionText() {
        binding.tvPlayerDescription.apply {
            // Разрешаем кликать по ссылкам
            movementMethod = LinkMovementMethod.getInstance()
            // Устанавливаем цвет выделения ссылок при нажатии
            highlightColor = android.graphics.Color.TRANSPARENT
        }
    }

    private fun updateDescription(description: String) {
        // Используем вашу утилиту для подготовки HTML (таймкоды, ссылки)
        val enrichedHtml = description.enrichForWebView()
        
        // Превращаем HTML строку в Spanned текст для TextView
        val spannedText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(enrichedHtml, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(enrichedHtml)
        }
        
        binding.tvPlayerDescription.text = spannedText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
