package com.example.echo_proto.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.echo_proto.R
import com.example.echo_proto.databinding.DialogChipFilterListBinding
import com.example.echo_proto.ui.viewmodels.FeedViewModel
import com.example.echo_proto.util.Constants
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import timber.log.Timber
import java.util.*

// todo: need make rotate-restart survive
class FeedFilterListDialogFragment(context: Context): DialogFragment() {

    private val viewModel by viewModels<FeedViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private var _binding: DialogChipFilterListBinding? = null
    private val binding get() = _binding!!

    private var isEnableToEdit = false
    private var inputFilterEditText = EditText(context)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Objects.requireNonNull(dialog)?.window?.requestFeature(Window.FEATURE_NO_TITLE)
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
            getNewFilterInputDialog().show()
        }
    }

    private fun chipEditEnable() {
        isEnableToEdit = true
        binding.btnChipsEdit.text = "done"
        binding.chipGroupFilter.forEach { child ->
            (child as? Chip)?.apply {
                isCloseIconVisible = true
                setOnClickListener { this_chip ->
                    Timber.d("CHIP_TAG=${this_chip.tag}")
                    (this_chip.tag as? String)?.let { filterToRemove ->
                        viewModel.removeFilterFromRssFeedPersonal(filterToRemove)
                    }
                    binding.chipGroupFilter.removeView(this_chip)
                }
            }
        }
    }

    private fun chipEditDone() {
        isEnableToEdit = false
        binding.btnChipsEdit.text = "edit"
        binding.chipGroupFilter.forEach { child ->
            (child as? Chip)?.apply {
                isCloseIconVisible = false
                setOnClickListener(null)
            }
        }
    }

    private fun createNewChip(context: Context, tagName: String): Chip = Chip(context).apply {
        val chipDrawable = ChipDrawable.createFromAttributes(
            context, null, 0, R.style.FilterChips
        )
        text = tagName
        tag = tagName
//        setTextColor(ContextCompat.getColor(context, R.color.yellow_500))
        setChipDrawable(chipDrawable)
    }

    private fun getNewFilterInputDialog(): Dialog {
        if (inputFilterEditText.parent != null) {
            (inputFilterEditText.parent as ViewGroup).removeView(inputFilterEditText)
            inputFilterEditText.text.clear()
        }
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Добавьте новый фильтр:")
            .setView(inputFilterEditText)
            .setPositiveButton("Добавить") { _, _ ->
                val newFilterQuery = inputFilterEditText.text.toString()
                if (newFilterQuery.isNotBlank()) {
                    viewModel.addNewFilterToRssFeedPersonalFilters(newFilter = newFilterQuery)
                    Toast.makeText(requireContext(), "add: $newFilterQuery", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .create()
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
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
        dialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("DIALOG: ON_DESTROY <<------------------------------")
        viewModel.refreshRssFeedPersonal()
        _binding = null
    }

}