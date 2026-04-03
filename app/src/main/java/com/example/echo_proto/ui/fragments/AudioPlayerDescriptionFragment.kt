package com.example.echo_proto.ui.fragments

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ReplacementSpan
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentAudioplayerDescriptionBinding
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.enrichForWebView
import com.example.echo_proto.util.timestampToMillis
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

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
            (parentFragment as? AudioPlayerDetailFragment)?.scrollToInfo()
        }
    }

    private fun setupDescriptionText() {
        binding.tvPlayerDescription.apply {
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.TRANSPARENT
        }
    }

    private fun updateDescription(description: String) {
        val enrichedHtml = description.enrichForWebView()
        
        val spannedText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(enrichedHtml, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(enrichedHtml)
        }
        
        val builder = SpannableStringBuilder(spannedText)
        val spans = builder.getSpans(0, builder.length, URLSpan::class.java)
        
        val goldColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        // Делаем фон чуть менее прозрачным для лучшей видимости границ (0x33 = ~20%)
        val goldBackground = Color.argb(51, Color.red(goldColor), Color.green(goldColor), Color.blue(goldColor))

        for (span in spans) {
            val url = span.url
            if (url.startsWith("seek://")) {
                val start = builder.getSpanStart(span)
                val end = builder.getSpanEnd(span)
                val timestamp = url.removePrefix("seek://").replace("&nbsp;", "").trim()
                
                builder.removeSpan(span)
                
                // 1. Обработка клика
                val clickSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val millis = timestamp.timestampToMillis()
                        Timber.d("🎯 SEEK: Jumping to $timestamp ($millis ms)")
                        mainViewModel.seekTo(millis)
                    }
                    override fun updateDrawState(ds: TextPaint) {
                        ds.isUnderlineText = false
                    }
                }
                builder.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                // 2. Идеальный отрисовщик "тега"
                val tagSpan = TimestampTagSpan(
                    backgroundColor = goldBackground,
                    textColor = goldColor,
                    cornerRadius = 16f,      // Мягкое скругление
                    horizontalPadding = 20f,  // Больше места по бокам
                    verticalPadding = 2f      // Минимальный отступ сверху/снизу, чтобы не сливалось
                )
                builder.setSpan(tagSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        
        binding.tvPlayerDescription.text = builder
    }

    /**
     * Отрисовщик для таймкодов.
     * Гарантирует стабильную ширину и отсутствие "плавания" текста.
     */
    private class TimestampTagSpan(
        private val backgroundColor: Int,
        private val textColor: Int,
        private val cornerRadius: Float,
        private val horizontalPadding: Float,
        private val verticalPadding: Float
    ) : ReplacementSpan() {

        // Общая настройка кисти для точного соответствия в getSize и draw
        private fun applyCustomStyle(paint: Paint) {
            paint.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            // Мы не меняем textSize здесь, чтобы не сломать высоту строки TextView
        }

        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
            applyCustomStyle(paint)
            // Измеряем текст с учетом нашего шрифта
            val width = paint.measureText(text, start, end)
            return (width + 2 * horizontalPadding).toInt()
        }

        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            applyCustomStyle(paint)
            val width = paint.measureText(text, start, end)
            
            // Используем метрики шрифта для расчета идеальной высоты фона
            val metrics = paint.fontMetrics
            
            // Рисуем фон. y - это базовая линия (baseline). 
            // metrics.ascent - это верх букв, metrics.descent - низ.
            val rect = RectF(
                x + 2f, // Небольшой зазор слева для чистоты
                y + metrics.ascent - verticalPadding,
                x + width + 2 * horizontalPadding - 2f,
                y + metrics.descent + verticalPadding
            )
            
            // 1. Отрисовка скругленного фона
            val originalColor = paint.color
            paint.color = backgroundColor
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
            
            // 2. Отрисовка текста
            paint.color = textColor
            // Центрируем текст внутри фона по горизонтали
            canvas.drawText(text!!, start, end, x + horizontalPadding, y.toFloat(), paint)
            
            // Возвращаем цвет кисти (хорошая практика)
            paint.color = originalColor
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
