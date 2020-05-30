package com.doancnpm.edoctor.koin

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
import com.doancnpm.edoctor.ui.splash.SplashVM
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
val viewModelModule = module {
  viewModel { SplashVM(get()) }

  //region Auth
  factory<LoginContract.Interactor> { LoginInteractor(get(), get()) }
  viewModel { LoginVM(get(), get()) }

  factory<RegisterContract.Interactor> { RegisterInteractor(get(), get()) }
  viewModel { RegisterVM(get(), get()) }

  factory<ResendCodeContract.Interactor> { ResendCodeInteractor(get(), get()) }
  viewModel { ResendCodeVM(get(), get()) }

  factory<VerifyContract.Interactor> { VerifyInteractor(get(), get()) }
  viewModel { (phone: String) -> VerifyVM(phone, get(), get()) }
  //endregion

  //region Main
  viewModel { MainVM(get()) }

  viewModel { HomeVM(get()) }
  //endregion
}