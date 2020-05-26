package com.doancnpm.edoctor.koin

import com.doancnpm.edoctor.ui.auth.login.LoginContract
import com.doancnpm.edoctor.ui.auth.login.LoginInteractor
import com.doancnpm.edoctor.ui.auth.login.LoginVM
import com.doancnpm.edoctor.ui.auth.register.RegisterContract
import com.doancnpm.edoctor.ui.auth.register.RegisterInteractor
import com.doancnpm.edoctor.ui.auth.register.RegisterVM
import com.doancnpm.edoctor.ui.splash.SplashVM
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@ExperimentalCoroutinesApi
val viewModelModule = module {
  viewModel { SplashVM(get()) }

  factory<LoginContract.Interactor> { LoginInteractor(get(), get()) }
  viewModel { LoginVM(get(), get()) }

  factory<RegisterContract.Interactor> { RegisterInteractor(get(), get()) }
  viewModel { RegisterVM(get(), get()) }
}