package com.doancnpm.edoctor.ui.main.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationsViewModel : ViewModel() {

  private val _text = MutableLiveData<String>().apply {
    value = "This is notifications Fragment"
  }
  val text: LiveData<String> = _text
}