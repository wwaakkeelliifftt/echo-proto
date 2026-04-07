package com.example.echo_proto.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import com.example.echo_proto.R
import com.example.echo_proto.data.local.prefs.EpisodeDisplayOptions
import com.example.echo_proto.data.local.prefs.SettingsManager
import com.example.echo_proto.databinding.BottomSheetDisplaySettingsBinding
import com.example.echo_proto.databinding.ItemEpisodeHeaderV2Binding
import com.example.echo_proto.databinding.ItemEpisodeV2Binding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DisplaySettingsBottomSheet : BottomSheetDialogFragment() {

    @Inject
    lateinit var settingsManager: SettingsManager

    private var _binding: BottomSheetDisplaySettingsBinding? = null
    private val binding get() = _binding!!

    private var initialScreenKey: String = "feed"
    private var currentScreenKey: String = "feed"
    private lateinit var currentOptions: EpisodeDisplayOptions

    private val screenOptions = mapOf(
        "Queue Screen" to "queue",
        "Feed Screen" to "feed",
        "Channels Screen" to "channels",
        "Downloads Screen" to "downloads"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialScreenKey = arguments?.getString(ARG_SCREEN_KEY) ?: "feed"
        currentScreenKey = initialScreenKey
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDisplaySettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        currentOptions = settingsManager.loadDisplayOptions(currentScreenKey)
        
        setupScreenSelector()
        setupPreview()
        updateSwitchesFromOptions()
        setupClickListeners()
    }

    private fun setupScreenSelector() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            screenOptions.keys.toList()
        )
        binding.autoCompleteScreenSelector.setAdapter(adapter)
        
        val initialDisplayName = screenOptions.entries.find { it.value == currentScreenKey }?.key
        binding.autoCompleteScreenSelector.setText(initialDisplayName, false)

        binding.autoCompleteScreenSelector.setOnItemClickListener { _, _, position, _ ->
            val selectedName = adapter.getItem(position)
            val newKey = screenOptions[selectedName] ?: "feed"
            if (newKey != currentScreenKey) {
                currentScreenKey = newKey
                currentOptions = settingsManager.loadDisplayOptions(currentScreenKey)
                updateSwitchesFromOptions()
                updatePreviewVisibility(currentOptions)
            }
        }
    }

    private fun setupPreview() {
        // Bind Header Preview
        val headerBinding = ItemEpisodeHeaderV2Binding.bind(binding.previewHeader.root)
        headerBinding.apply {
            tvDateHeader.text = "TODAY"
            tvEpisodeCount.text = "12 EPISODES"
            // Reduce divider weight for preview to keep text visible
            val params = viewDivider.layoutParams as ViewGroup.MarginLayoutParams
            params.width = (40 * resources.displayMetrics.density).toInt()
            viewDivider.layoutParams = params
        }

        // Bind Item Preview
        val previewBinding = ItemEpisodeV2Binding.bind(binding.previewItem.root)
        previewBinding.apply {
            tvEpisodeTitle.text = "Preview: The Architecture of Silence"
            tvEpisodeMetadata.text = "Sonic Landscapes • 42:15"
            ivCover.setImageResource(R.drawable.ic_launcher_background)
        }
        
        updatePreviewVisibility(currentOptions)
    }

    private fun updateSwitchesFromOptions() {
        binding.apply {
            switchCompactMode.isChecked = currentOptions.isCompactMode
            switchImageCover.isChecked = currentOptions.showImageCover
            switchDateHeaders.isChecked = currentOptions.showDateHeaders
            switchMetadata.isChecked = currentOptions.showMetadata
            switchOptimizeSpace.isChecked = currentOptions.isSpaceOptimized
            switchFavorite.isChecked = currentOptions.showFavoriteButton
            switchQueue.isChecked = currentOptions.showQueueButton

            layoutOptimizeSpace.visibility = if (!currentOptions.showMetadata) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        val onToggleListener = View.OnClickListener {
            binding.apply {
                layoutOptimizeSpace.visibility = if (!switchMetadata.isChecked) View.VISIBLE else View.GONE
                
                currentOptions = EpisodeDisplayOptions(
                    isCompactMode = switchCompactMode.isChecked,
                    showImageCover = switchImageCover.isChecked,
                    showDateHeaders = switchDateHeaders.isChecked,
                    showMetadata = switchMetadata.isChecked,
                    isSpaceOptimized = if (!switchMetadata.isChecked) switchOptimizeSpace.isChecked else false,
                    showFavoriteButton = switchFavorite.isChecked,
                    showQueueButton = switchQueue.isChecked
                )
                updatePreviewVisibility(currentOptions)
            }
        }

        binding.apply {
            switchCompactMode.setOnClickListener(onToggleListener)
            switchImageCover.setOnClickListener(onToggleListener)
            switchDateHeaders.setOnClickListener(onToggleListener)
            switchMetadata.setOnClickListener(onToggleListener)
            switchOptimizeSpace.setOnClickListener(onToggleListener)
            switchFavorite.setOnClickListener(onToggleListener)
            switchQueue.setOnClickListener(onToggleListener)

            btnReset.setOnClickListener {
                // Reset to default values (using logic from loadDisplayOptions fallback)
                val defaultOptions = EpisodeDisplayOptions(
                    showDateHeaders = currentScreenKey == "feed" 
                )
                currentOptions = defaultOptions
                updateSwitchesFromOptions()
                updatePreviewVisibility(currentOptions)
            }

            btnClose.setOnClickListener {
                settingsManager.saveDisplayOptions(currentScreenKey, currentOptions)
                dismiss()
            }
        }
    }

    private fun updatePreviewVisibility(options: EpisodeDisplayOptions) {
        // Toggle header visibility in preview
        binding.previewHeader.root.visibility = if (options.showDateHeaders) View.VISIBLE else View.GONE

        val previewBinding = ItemEpisodeV2Binding.bind(binding.previewItem.root)
        val density = resources.displayMetrics.density
        
        previewBinding.apply {
            ivCover.visibility = if (options.showImageCover) View.VISIBLE else View.GONE
            btnFavorite.visibility = if (options.showFavoriteButton) View.VISIBLE else View.GONE
            btnQueue.visibility = if (options.showQueueButton) View.VISIBLE else View.GONE
            
            val coverSize = if (options.isSpaceOptimized && !options.showMetadata) 40 else 52
            ivCover.layoutParams.width = (coverSize * density).toInt()
            ivCover.layoutParams.height = (coverSize * density).toInt()
            ivCover.requestLayout()

            if (options.showMetadata) {
                tvEpisodeMetadata.visibility = View.VISIBLE
            } else {
                tvEpisodeMetadata.visibility = if (options.isSpaceOptimized) View.GONE else View.INVISIBLE
            }
            
            val padding = if (options.isCompactMode) 8 else 16
            val container = root.findViewById<View>(R.id.container)
            container?.setPadding(
                container.paddingLeft,
                (padding * density).toInt(),
                container.paddingRight,
                (padding * density).toInt()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DisplaySettingsBottomSheet"
        private const val ARG_SCREEN_KEY = "arg_screen_key"

        fun newInstance(screenKey: String): DisplaySettingsBottomSheet {
            return DisplaySettingsBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_SCREEN_KEY, screenKey)
                }
            }
        }
    }
}
