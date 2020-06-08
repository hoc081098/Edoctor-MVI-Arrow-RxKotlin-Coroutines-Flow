package com.doancnpm.edoctor.koin

import com.doancnpm.edoctor.domain.entity.Category
import com.doancnpm.edoctor.ui.auth.login.LoginContract
import com.doancnpm.edoctor.ui.auth.login.LoginInteractor
import com.doancnpm.edoctor.ui.auth.login.LoginVM
import com.doancnpm.edoctor.ui.auth.register.RegisterContract
import com.doancnpm.edoctor.ui.auth.register.RegisterInteractor
import com.doancnpm.edoctor.ui.auth.register.RegisterVM
import com.doancnpm.edoctor.ui.auth.resendcode.ResendCodeContract
import com.doancnpm.edoctor.ui.auth.resendcode.ResendCodeInteractor
import com.doancnpm.edoctor.ui.auth.resendcode.ResendCodeVM
import com.doancnpm.edoctor.ui.auth.verify.VerifyContract
import com.doancnpm.edoctor.ui.auth.verify.VerifyInteractor
import com.doancnpm.edoctor.ui.auth.verify.VerifyVM
import com.doancnpm.edoctor.ui.main.MainVM
import com.doancnpm.edoctor.ui.main.home.HomeVM
import com.doancnpm.edoctor.ui.main.home.create_order.CreateOrderVM
import com.doancnpm.edoctor.ui.main.home.services.ServicesVM
import com.doancnpm.edoctor.ui.main.profile.ProfileVM
import com.doancnpm.edoctor.ui.splash.SplashVM
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
val viewModelModule = module {
  viewModel { SplashVM(get()) }

  //region Auth
  factory<LoginContract.Interactor> {
    LoginInteractor(
      dispatchers = get(),
      userRepository = get()
    )
  }
  viewModel {
    LoginVM(
      interactor = get(),
      schedulers = get()
    )
  }

  factory<RegisterContract.Interactor> {
    RegisterInteractor(
      dispatchers = get(),
      userRepository = get()
    )
  }
  viewModel {
    RegisterVM(
      interactor = get(),
      schedulers = get()
    )
  }

  factory<ResendCodeContract.Interactor> {
    ResendCodeInteractor(
      dispatchers = get(),
      userRepository = get()
    )
  }
  viewModel {
    ResendCodeVM(
      interactor = get(),
      schedulers = get()
    )
  }

  factory<VerifyContract.Interactor> {
    VerifyInteractor(
      dispatchers = get(),
      userRepository = get()
    )
  }
  viewModel { (phone: String) ->
    VerifyVM(
      phone = phone,
      interactor = get(),
      schedulers = get()
    )
  }
  //endregion

  //region Main
  viewModel { MainVM(userRepository = get()) }

  viewModel { HomeVM(categoryRepository = get()) }

  viewModel { (category: Category) ->
    ServicesVM(
      serviceRepository = get(),
      category = category
    )
  }

  viewModel { CreateOrderVM(locationRepository = get()) }
  //endregion

  //region Profile
  viewModel {
    ProfileVM(
      userRepository = get(),
      dispatchers = get()
    )
  }
  //endregion
}