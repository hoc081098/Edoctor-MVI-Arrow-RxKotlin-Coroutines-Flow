package com.doancnpm.edoctor.ui.main.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doancnpm.edoctor.core.BaseVM
import com.doancnpm.edoctor.domain.entity.AppError
import com.doancnpm.edoctor.domain.repository.OrderRepository
import com.doancnpm.edoctor.utils.Event
import com.doancnpm.edoctor.utils.asLiveData
import kotlinx.coroutines.launch

class QRCodeVM(
  private val orderId: Long,
  private val orderRepository: OrderRepository
) : BaseVM() {
  private val errorD = MutableLiveData<Event<AppError>>()
  private val imageD = MutableLiveData<String?>().apply { value = null }

  val image get() = imageD.asLiveData()
  val error get() = errorD.asLiveData()

  fun fetchImage() {
    if (imageD.value !== null) {
      return
    }

    viewModelScope.launch {
      orderRepository
        .getQrCode(orderId)
        .fold(
          ifLeft = { errorD.value = Event(it) },
          ifRight = { imageD.value = it }
        )
    }
  }
}