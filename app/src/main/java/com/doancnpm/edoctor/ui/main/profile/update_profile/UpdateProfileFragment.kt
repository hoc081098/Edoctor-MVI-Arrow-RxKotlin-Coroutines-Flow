package com.doancnpm.edoctor.ui.main.profile.update_profile

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.doancnpm.edoctor.GlideApp
import com.doancnpm.edoctor.R
import com.doancnpm.edoctor.core.BaseFragment
import com.doancnpm.edoctor.databinding.FragmentUpdateProfileBinding
import com.doancnpm.edoctor.domain.entity.getMessage
import com.doancnpm.edoctor.ui.main.MainActivity
import com.doancnpm.edoctor.ui.main.profile.update_profile.UpdateProfileContract.SingleEvent
import com.doancnpm.edoctor.ui.main.profile.update_profile.UpdateProfileContract.ViewIntent
import com.doancnpm.edoctor.utils.*
import com.jakewharton.rxbinding4.view.clicks
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.MainThreadDisposable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.addTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

@ExperimentalCoroutinesApi
class UpdateProfileFragment : BaseFragment(R.layout.fragment_update_profile) {
  private val binding by viewBinding<FragmentUpdateProfileBinding>()
  private val navArgs by navArgs<UpdateProfileFragmentArgs>()
  private val viewModel by viewModel<UpdateProfileVM> { parametersOf(navArgs.user) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupViews()
    bindVM()

    requireActivity()
      .onBackPressedDispatcher
      .addCallback(owner = viewLifecycleOwner) { showExitAlert() }

    (requireActivity() as MainActivity).onSupportNavigateUp = {
      showExitAlert()
      false
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    (requireActivity() as MainActivity).onSupportNavigateUp = null
  }

  private fun showExitAlert() {
    val (fullName, birthday, avatar) = viewModel.stateLiveData.value!!
    val user = navArgs.user

    val canGoBack = user.fullName == fullName
      && user.birthday == birthday
      && avatar === null

    if (canGoBack) {
      findNavController().popBackStack()
    } else {
      requireActivity().showAlertDialog {
        title("Exit user profile update")
        message("All information will be not saved")
        cancelable(false)
        positiveAction("OK") { _, _ -> findNavController().popBackStack() }
        negativeAction("Cancel") { _, _ -> }
      }
    }
  }

  private fun bindVM() {
    val glide = GlideApp.with(this)
    val user = navArgs.user

    viewModel
      .stateLiveData
      .observe(owner = viewLifecycleOwner) { viewState ->
        binding.run {
          if (viewState.tooShortFullName) {
            "Too short full name"
          } else {
            null
          }.let { if (editFullName.error != it) editFullName.error = it }

          editBirthday.editText!!.setText(viewState.birthday?.toString_yyyyMMdd())

          if (viewState.isLoading) {
            progressBar.visible()
            buttonUpdate.invisible()
          } else {
            progressBar.invisible()
            buttonUpdate.visible()
          }

          if (viewState.avatar === null) {
            user.avatar
              ?.let {
                glide
                  .load(it)
                  .placeholder(R.drawable.icons8_person_96)
                  .error(R.drawable.icons8_person_96)
                  .dontAnimate()
                  .into(imageAvatar)
              }
              ?: when (val firstLetter = user.fullName.firstOrNull()) {
                null -> ColorDrawable(Color.parseColor("#fafafa"))
                else -> {
                  val size = requireContext()
                    .dpToPx(96)
                    .also { Timber.d("96dp = ${it}px") }
                  TextDrawable
                    .builder()
                    .beginConfig()
                    .width(size)
                    .height(size)
                    .endConfig()
                    .buildRect(
                      firstLetter.toUpperCase().toString(),
                      ColorGenerator.MATERIAL.getColor(user.phone),
                    )
                }
              }.let(imageAvatar::setImageDrawable)
          } else {
            glide
              .load(viewState.avatar)
              .centerCrop()
              .dontAnimate()
              .into(imageAvatar)
          }
        }
      }

    viewModel.eventLiveData.observeEvent(owner = viewLifecycleOwner) {
      when (it) {
        SingleEvent.Success -> {
          view?.snack("Update profile successfully")
          findNavController().popBackStack()
        }
        is SingleEvent.Failure -> {
          Timber.d("Update profile failure: ${it.error}")
          view?.snack("Update profile failure: ${it.error.getMessage()}")
        }
      }
    }

    viewModel.processIntents(
      Observable.mergeArray(
        binding.editFullName
          .editText!!
          .textChanges()
          .map { ViewIntent.FullNameChanged(it.toString()) },
        binding
          .editBirthday
          .editText!!
          .clicks()
          .exhaustMap {
            val date = viewModel.stateLiveData.value!!.birthday
            requireActivity()
              .pickDateObservable(date)
              .toObservable()
          }
          .map { ViewIntent.BirthdayChanged(it) }
          .startWithItem(ViewIntent.BirthdayChanged(user.birthday)),
        binding
          .buttonUpdate
          .clicks()
          .map { ViewIntent.Submit },
        binding
          .imageAvatar
          .clicks()
          .exhaustMap {
            Observable.create<ViewIntent.AvatarChanged> { emitter ->
              val launcher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                uri?.let { emitter.onNext(ViewIntent.AvatarChanged(it)) }
                emitter.onComplete()
              }
              launcher.launch(arrayOf("image/*"))
              emitter.setDisposable(object : MainThreadDisposable() {
                override fun onDispose() = launcher.unregister()
              })
            }
          }
          .startWithItem(ViewIntent.AvatarChanged(null))
      )
    ).addTo(compositeDisposable)
  }

  private fun setupViews() {
    binding.editBirthday.editText!!.inputType = InputType.TYPE_NULL
    binding.editBirthday.editText!!.onFocusChangeListener = null

    val (fullName, birthday) = viewModel.stateLiveData.value!!
    binding.run {
      editFullName.editText!!.setText(fullName)
      editBirthday.editText!!.setText(birthday?.toString_yyyyMMdd())
    }
  }
}