package com.dev.lotografia.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dev.lotografia.hintrepo.CategoryHints
import com.dev.lotografia.hintrepo.HintRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HintViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = HintRepository(application)
  private val _categoryHints = MutableStateFlow<List<CategoryHints>>(emptyList())
  val categoryHints = _categoryHints.asStateFlow()

  private val _currentIndex = MutableStateFlow(0)
  val currentIndex = _currentIndex.asStateFlow()

  init {
    _categoryHints.value = repository.loadHintsFromAssets()
  }

  fun nextCategory() {
    if (_categoryHints.value.isNotEmpty()) {
      _currentIndex.value = (_currentIndex.value + 1) % _categoryHints.value.size
    }
  }

  fun prevCategory() {
    if (_categoryHints.value.isNotEmpty()) {
      _currentIndex.value =
        if (_currentIndex.value - 1 < 0) _categoryHints.value.size - 1 else _currentIndex.value - 1
    }
  }

  fun currentCategory(): CategoryHints? = _categoryHints.value.getOrNull(_currentIndex.value)
}