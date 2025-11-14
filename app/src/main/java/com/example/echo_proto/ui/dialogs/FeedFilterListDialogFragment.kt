package com.example.echo_proto.ui.dialogs

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.view.inputmethod.InputMethodManager
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.echo_proto.R
import com.example.echo_proto.databinding.DialogChipFilterListBinding
import com.example.echo_proto.ui.viewmodels.FeedViewModel
import com.example.echo_proto.util.Constants
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import timber.log.Timber

// todo: need make rotate-restart survive
class FeedFilterListDialogFragment : DialogFragment() {

    private val viewModel by viewModels<FeedViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private var _binding: DialogChipFilterListBinding? = null
    private val binding get() = _binding!!

    private var isEnableToEdit = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogChipFilterListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            Timber.d("GET_BOOLEAN_NON_EQUAL_TO_NULL=${arguments?.getBoolean(Constants.FEED_FILTER_DIALOG_TAG)}")
            isEnableToEdit = arguments?.getBoolean(Constants.FEED_FILTER_DIALOG_TAG) ?: false
            Timber.d("isEnableToEdit=$isEnableToEdit")
        }

        setupChipGroup()
        setupClickListeners()
    }

    private fun setupChipGroup() {
        viewModel.filterStringsSet.observe(viewLifecycleOwner) { newFilterSet ->
            binding.chipGroupFilter.removeAllViews()
            newFilterSet.forEach {
                val newChip = createNewChip(requireContext(), tagName = it)
                binding.chipGroupFilter.addView(newChip)
            }
            if (isEnableToEdit) {
                chipEditEnable()
            } else {
                chipEditDone()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnChipsEdit.setOnClickListener {
            if (!isEnableToEdit) {
                chipEditEnable()
            } else {
                chipEditDone()
            }
        }
        binding.btnAddNewFilter.setOnClickListener {
            toggleInputVisibility(show = true)
        }

        binding.btnAddFilterConfirm.setOnClickListener {
            val newFilterQuery = binding.inputNewFilter.text?.toString().orEmpty().trim()
            if (newFilterQuery.isNotEmpty()) {
                viewModel.addNewFilterToRssFeedPersonalFilters(newFilter = newFilterQuery)
                binding.inputNewFilter.text?.clear()
                toggleInputVisibility(show = false)
            }
        }

        binding.btnAddFilterCancel.setOnClickListener {
            binding.inputNewFilter.text?.clear()
            toggleInputVisibility(show = false)
        }
    }

    private fun chipEditEnable() {
        isEnableToEdit = true
        binding.btnChipsEdit.setText(R.string.feed_filter_edit_done)
        binding.btnAddNewFilter.isVisible = false
        binding.chipGroupFilter.forEach { child ->
            (child as? Chip)?.apply {
                isCloseIconVisible = true
                setOnCloseIconClickListener { closeIconChip ->
                    (closeIconChip.tag as? String)?.let { filterToRemove ->
                        viewModel.removeFilterFromRssFeedPersonal(filterToRemove)
                    }
                }
                setOnClickListener(null)
            }
        }
    }

    private fun chipEditDone() {
        isEnableToEdit = false
        binding.btnChipsEdit.setText(R.string.feed_filter_edit)
        if (!binding.groupAddFilter.isVisible) {
            binding.btnAddNewFilter.isVisible = true
        }
        binding.chipGroupFilter.forEach { child ->
            (child as? Chip)?.apply {
                isCloseIconVisible = false
                setOnCloseIconClickListener(null)
                setOnClickListener(null)
            }
        }
    }

    private fun createNewChip(context: android.content.Context, tagName: String): Chip = Chip(context).apply {
        val chipDrawable = ChipDrawable.createFromAttributes(
            context, null, 0, R.style.FilterChips
        )
        text = tagName
        tag = tagName
//        setTextColor(ContextCompat.getColor(context, R.color.yellow_500))
        setChipDrawable(chipDrawable)
        setTextAppearance(R.style.FeedFilterChipText)
    }

    private fun setPercentDialogSize(percentage: Int = 80) {
        val percent = percentage.toFloat() / 100
        val displayMetrics = Resources.getSystem().displayMetrics
        val rect = displayMetrics.run {
            Rect(0, 0, widthPixels, heightPixels)
        }
        val percentWidth = rect.width() * percent
        dialog?.window?.let {
            it.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        }

    }

    override fun onResume() {
        super.onResume()
        setPercentDialogSize()
    }

    override fun onPause() {
        super.onPause()
        Timber.d("DIALOG: ON_PAUSE <<------------------------------")
        viewModel.saveRssFeedPersonalFiltersIntoSharedPref()
    }

    override fun onStop() {
        super.onStop()
    }

    private fun toggleInputVisibility(show: Boolean) {
        if (show && isEnableToEdit) {
            chipEditDone()
        }

        binding.groupAddFilter.isVisible = show
        binding.btnAddNewFilter.isVisible = !show
        binding.btnChipsEdit.isVisible = !show

        if (show) {
            binding.inputNewFilter.post {
                binding.inputNewFilter.requestFocus()
                showKeyboard(binding.inputNewFilter)
            }
        } else {
            hideKeyboard()
        }
    }

    private fun showKeyboard(target: View) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("DIALOG: ON_DESTROY <<------------------------------")
        viewModel.refreshRssFeedPersonal()
        _binding = null
    }

}