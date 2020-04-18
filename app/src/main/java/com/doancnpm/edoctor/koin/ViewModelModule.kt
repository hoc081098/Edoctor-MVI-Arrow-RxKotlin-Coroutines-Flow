package com.doancnpm.edoctor.koin

import com.doancnpm.edoctor.ui.splash.SplashVM
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
  viewModel { SplashVM(get()) }
}