package com.doancnpm.edoctor.ui.main.history

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.Window
import android.widget.RelativeLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.observe
import com.caverock.androidsvg.SVG
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.databinding.FragmentQrCodeBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.utils.invisible
import com.doancnpm.edoctor.utils.observeEvent
import com.doancnpm.edoctor.utils.toast
import com.doancnpm.edoctor.utils.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class QRCodeFragment : DialogFragment(R.layout.fragment_qr_code) {
  private val viewModel by viewModel<QRCodeVM> {
    val orderId = requireArguments().getLong(ORDER_ID_KEY, -1)
    check(orderId != -1L) { "Required order id" }
    parametersOf(orderId)
  }
  private val binding by viewBinding<FragmentQrCodeBinding>()

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val root = RelativeLayout(activity).apply {
      layoutParams = ViewGroup.LayoutParams(
        MATCH_PARENT,
        MATCH_PARENT
      )
    }

    return Dialog(requireActivity()).apply {
      requestWindowFeature(Window.FEATURE_NO_TITLE)
      setContentView(root)
      window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      window!!.setLayout(MATCH_PARENT, MATCH_PARENT)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.imageView.setOnClickListener { }
    binding.overlay.setOnClickListener { dismiss() }

    viewModel.image.observe(owner = viewLifecycleOwner) {
      val svg = SVG.getFromString(it ?: return@observe)
      val pictureDrawable = PictureDrawable(svg.renderToPicture())

      binding.progressBar.invisible()
      binding.imageView.setImageDrawable(pictureDrawable)
    }
    viewModel.error.observeEvent(owner = viewLifecycleOwner) {
      requireActivity().toast("Get QR code failure: ${it.getMessage()}")
    }
    viewModel.fetchImage()
  }

  companion object Factory {
    private const val ORDER_ID_KEY =
      "com.doancnpm.edoctor.ui.main.history.QRCodeFragment.order_id"

    fun newInstance(orderId: Long): QRCodeFragment {
      return QRCodeFragment()
        .apply { arguments = bundleOf(ORDER_ID_KEY to orderId) }
    }
  }
}