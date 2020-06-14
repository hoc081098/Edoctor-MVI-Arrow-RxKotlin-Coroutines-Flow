package com.doancnpm.edoctor.utils

inline val Any?.unit get() = Unit

inline val <T> T.exhaustive get() = this