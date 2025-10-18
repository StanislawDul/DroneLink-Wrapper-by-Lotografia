package com.dev.lotografia

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class FakeViewModelStoreOwner(override val viewModelStore: ViewModelStore) : ViewModelStoreOwner {
}