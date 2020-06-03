package com.doancnpm.edoctor.ui.main.home.create_order

import androidx.lifecycle.MutableLiveData
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.utils.asLiveData

class CreateOrderVM : BaseVM() {

  val canGoNextLiveDatas = List(4) {
    MutableLiveData<Boolean>().apply { value = true }.asLiveData()
  }

}