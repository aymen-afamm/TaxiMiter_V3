package com.taximeter.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.taximeter.app.models.User
import com.taximeter.app.repository.TaxiRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaxiRepository(application)

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val user = repository.login(username, password)
            if (user != null) {
                repository.saveCurrentUser(user)
                _loginResult.value = LoginResult.Success(user)
            } else {
                _loginResult.value = LoginResult.Error("Nom d'utilisateur ou mot de passe incorrect")
            }
        }
    }

    fun getCurrentUser(): User? {
        return repository.getCurrentUser()
    }

    sealed class LoginResult {
        data class Success(val user: User) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }
}