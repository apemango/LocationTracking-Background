package com.phone.tracker.ui.detail

import androidx.lifecycle.ViewModel
import com.phone.tracker.data.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailViewModel  @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {


}