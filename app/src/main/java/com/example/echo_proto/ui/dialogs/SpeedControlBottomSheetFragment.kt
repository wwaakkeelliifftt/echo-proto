package com.example.echo_proto.ui.dialogs

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.echo_proto.R
import com.example.echo_proto.databinding.BottomSheetSpeedControlV2Binding
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.Constants
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import kotlin.math.abs

@AndroidEntryPoint
class SpeedControlBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSpeedControlV2Binding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            setOnShowListener { dialogInterface ->
                val bottomSheet = (dialogInterface as? BottomSheetDialog)
                    ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.let { sheet ->
                    sheet.setBackgroundColor(Color.TRANSPARENT)
                    val behavior = BottomSheetBehavior.from(sheet)
                    behavior.isFitToContents = true
                    behavior.isHideable = true
                    behavior.skipCollapsed = true
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            // remove "sticking" state
                            when (newState) {
                                BottomSheetBehavior.STATE_HALF_EXPANDED -> behavior.state = BottomSheetBehavior.STATE_EXPANDED
                            }
                        }
                        override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
                    })
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetSpeedControlV2Binding.inflate(inflater, container, false)
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
        btnDecreaseSpeed.setOnClickListener {
            viewModel.adjustPlaybackSpeed(-Constants.PLAYBACK_SPEED_BUTTON_STEP)
        }

        btnIncreaseSpeed.setOnClickListener {
            viewModel.adjustPlaybackSpeed(Constants.PLAYBACK_SPEED_BUTTON_STEP)
        }

        btnAddNewSpeed.setOnClickListener {
            val speed = viewModel.currentPlaybackSpeed.value ?: Constants.DEFAULT_PLAYBACK_SPEED
            val added = viewModel.addPlaybackSpeedPreset(speed)
            val message = if (added) {
                R.string.speed_control_preset_saved
            } else {
                R.string.speed_control_preset_exists
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        sliderSpeed.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                updateCurrentSpeedUi(value)
            }
        }
        sliderSpeed.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) = Unit
            override fun onStopTrackingTouch(slider: Slider) {
                viewModel.setPlaybackSpeed(slider.value)
            }
        })
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
        binding.chipCurrentSpeed.text = viewModel.formatSpeed(speed).replace("x", "").replace("X", "")
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
        val chipDrawable = ChipDrawable.createFromAttributes(context, null, 0, R.style.SpeedControlChipV2)
        return Chip(context).apply {
            setChipDrawable(chipDrawable)
            text = viewModel.formatSpeed(speed)
            isCheckable = false
            tag = speed
            setTextColor(ContextCompat.getColor(context, R.color.colorInactiveChipText))
            chipIcon = null
            isChipIconVisible = false
            setOnClickListener {
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
        val context = requireContext()
        for (index in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(index) as? Chip ?: continue
            val chipSpeed = (chip.tag as? Float) ?: continue
            
            val shouldBeActive = abs(chipSpeed - currentSpeed) < 0.01f
            if (shouldBeActive) {
                val activeDrawable = ChipDrawable.createFromAttributes(
                    context, null, 0, R.style.SpeedControlChipV2_Active
                )
                chip.setChipDrawable(activeDrawable)
                chip.setTextColor(ContextCompat.getColor(context, R.color.colorChipActiveText))
                chip.chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_check_circle)
                chip.chipIconTint = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.colorChipActiveIcon)
                )
                chip.isChipIconVisible = true
            } else {
                val normalDrawable = ChipDrawable.createFromAttributes(
                    context, null, 0, R.style.SpeedControlChipV2
                )
                chip.setChipDrawable(normalDrawable)
                chip.setTextColor(ContextCompat.getColor(context, R.color.colorInactiveChipText))
                chip.chipIcon = null
                chip.isChipIconVisible = false
            }
        }
    }

    companion object {
        const val TAG = "SpeedControlBottomSheet"
    }
}
