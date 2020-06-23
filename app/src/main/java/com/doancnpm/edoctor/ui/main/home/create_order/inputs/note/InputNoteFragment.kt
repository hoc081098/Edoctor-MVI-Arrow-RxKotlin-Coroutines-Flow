package com.doancnpm.edoctor.ui.main.home.create_order.inputs.note

import android.os.Bundle
import android.view.View
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentInputNoteBinding
import com.doancnpm.edoctor.ui.main.MainActivity
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderVM
import com.doancnpm.edoctor.utils.viewBinding
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import org.koin.androidx.viewmodel.ext.android.getViewModel
import kotlin.LazyThreadSafetyMode.NONE

class InputNoteFragment : BaseFragment(R.layout.fragment_input_note) {
  private val binding by viewBinding<FragmentInputNoteBinding>()
  private val viewModel by lazy(NONE) { requireParentFragment().getViewModel<CreateOrderVM>() }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    KeyboardVisibilityEvent.setEventListener(
      requireActivity(),
      viewLifecycleOwner,
      object : KeyboardVisibilityEventListener {
        override fun onVisibilityChanged(isOpen: Boolean) {
          val mainActivity = requireActivity() as MainActivity
          if (isOpen) {
            mainActivity.hideBottomNav()
          } else {
            mainActivity.showBottomNav()
          }
        }
      }
    )

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
  }
}