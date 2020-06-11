package com.doancnpm.edoctor.ui.main.home.create_order.inputs.note

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentInputNoteBinding
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderVM
import com.doancnpm.edoctor.utils.hideKeyboard
import com.doancnpm.edoctor.utils.viewBinding
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.koin.androidx.viewmodel.ext.android.getViewModel
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE

class InputNoteFragment : BaseFragment(R.layout.fragment_input_note) {
  private val binding by viewBinding<FragmentInputNoteBinding>()
  private val viewModel by lazy(NONE) { requireParentFragment().getViewModel<CreateOrderVM>() }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.noteLiveData
      .value
      ?.let { binding.editNote.editText!!.setText(it) }

    binding.editNote
      .editText!!
      .textChanges()
      .subscribeBy { note ->
        viewModel.setNote(
          if (note.isBlank()) null
          else note.toString()
        )
      }
      .addTo(compositeDisposable)

    binding.editNote.editText!!.setOnEditorActionListener { v, actionId, event ->
      if (actionId == EditorInfo.IME_ACTION_NEXT) {
        hideKeyboard()
        Timber.d("Hide keyboard")
        true
      } else {
        false
      }
    }
  }
}