package com.example.echo_proto.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.res.ColorStateList
import android.widget.Toast
import androidx.appcompat.R.attr.colorPrimary
import androidx.fragment.app.activityViewModels
import com.example.echo_proto.R
import com.example.echo_proto.databinding.BottomSheetSpeedControlBinding
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.Constants
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.color.MaterialColors
import com.google.android.material.slider.Slider
import timber.log.Timber
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class SpeedControlBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSpeedControlBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<MainViewModel>()
    private val strokeWidth by lazy { resources.getDimension(R.dimen.speed_chip_stroke_width) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            setOnShowListener { dialogInterface ->
                val bottomSheet = (dialogInterface as? BottomSheetDialog)
                    ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.let { sheet ->
                    BottomSheetBehavior.from(sheet).apply {
                        peekHeight = (resources.displayMetrics.heightPixels * 0.45f).toInt()
                        state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetSpeedControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupListeners() = with(binding) {
        chipCurrentSpeed.setOnClickListener {
            val speed = viewModel.currentPlaybackSpeed.value ?: Constants.DEFAULT_PLAYBACK_SPEED
            val added = viewModel.addPlaybackSpeedPreset(speed)
            val message = if (added) {
                R.string.speed_control_preset_saved
            } else {
                R.string.speed_control_preset_exists
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        btnDecreaseSpeed.setOnClickListener {
            Timber.tag("SPEED").d("1) btnDecreaseSpeed clicked -> delta=%.2f", -Constants.PLAYBACK_SPEED_BUTTON_STEP)
            viewModel.adjustPlaybackSpeed(-Constants.PLAYBACK_SPEED_BUTTON_STEP)
        }

        btnIncreaseSpeed.setOnClickListener {
            Timber.tag("SPEED").d("1) btnIncreaseSpeed clicked -> delta=%.2f", Constants.PLAYBACK_SPEED_BUTTON_STEP)
            viewModel.adjustPlaybackSpeed(Constants.PLAYBACK_SPEED_BUTTON_STEP)
        }

        sliderSpeed.apply {
            valueFrom = Constants.PLAYBACK_SPEED_MIN
            valueTo = Constants.PLAYBACK_SPEED_MAX
            stepSize = Constants.PLAYBACK_SPEED_STEP
            addOnChangeListener { slider, value, fromUser ->
                if (fromUser) {
                    updateCurrentSpeedUi(value)
                } else {
                    updateCurrentSpeedUi(value)
                }
            }
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) = Unit
                override fun onStopTrackingTouch(slider: Slider) {
                    Timber.tag("SPEED").d("1) slider stop -> value=%.2f", slider.value)
                    viewModel.setPlaybackSpeed(slider.value)
                }
            })
        }
    }

    private fun observeViewModel() {
        viewModel.currentPlaybackSpeed.observe(viewLifecycleOwner) { speed ->
            updateCurrentSpeedUi(speed)
            if (!binding.sliderSpeed.isPressed) {
                binding.sliderSpeed.value = speed
            }
        }

        viewModel.playbackSpeedPresets.observe(viewLifecycleOwner) { presets ->
            renderPresets(presets)
        }
    }

    private fun updateCurrentSpeedUi(speed: Float) {
        Timber.tag("SPEED").d("updateCurrentSpeedUi=$speed")
        binding.chipCurrentSpeed.text = viewModel.formatSpeed(speed)
        binding.chipCurrentSpeed.chipStrokeWidth = strokeWidth
        binding.chipCurrentSpeed.chipStrokeColor = ColorStateList.valueOf(
            MaterialColors.getColor(binding.chipCurrentSpeed, colorPrimary)
        )
        highlightSelectedPreset(speed)
    }

    private fun renderPresets(presets: List<Float>) {
        val chipGroup = binding.chipGroupPresets
        chipGroup.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        presets.forEach { speed ->
            val chip = createPresetChip(inflater, speed)
            chipGroup.addView(chip)
        }
        val currentSpeed = viewModel.currentPlaybackSpeed.value ?: Constants.DEFAULT_PLAYBACK_SPEED
        highlightSelectedPreset(currentSpeed)
    }

    private fun createPresetChip(inflater: LayoutInflater, speed: Float): Chip {
        val context = requireContext()
        val chipDrawable = ChipDrawable.createFromAttributes(context, null, 0, R.style.FilterChips)
        return Chip(context).apply {
            setChipDrawable(chipDrawable)
            setTextAppearance(R.style.FeedFilterChipText)
            text = viewModel.formatSpeed(speed)
            isCheckable = false
            tag = speed
            setOnClickListener {
                Timber.tag("SPEED").d("1) preset chip clicked -> speed=%.2f", speed)
                viewModel.setPlaybackSpeed(speed)
            }
            setOnLongClickListener {
                viewModel.removePlaybackSpeedPreset(speed)
                Toast.makeText(requireContext(), R.string.speed_control_preset_removed, Toast.LENGTH_SHORT).show()
                true
            }
        }
    }

    private fun highlightSelectedPreset(currentSpeed: Float) {
        val chipGroup = binding.chipGroupPresets
        for (index in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(index) as? Chip ?: continue
            val chipSpeed = (chip.tag as? Float) ?: continue
            val strokeColor = ColorStateList.valueOf(MaterialColors.getColor(chip, colorPrimary))
            if (abs(chipSpeed - currentSpeed) < 0.01f) {
                chip.chipStrokeWidth = strokeWidth
                chip.chipStrokeColor = strokeColor
            } else {
                chip.chipStrokeWidth = 0f
            }
        }
    }

    companion object {
        const val TAG = "SpeedControlBottomSheet"
    }
}
