package com.test.applicationtest.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.test.applicationtest.model.login.LoginResponse
import com.test.applicationtest.helper.CoroutinesHelper.ioSafe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() :ViewModel() {

    val isLoading = MutableLiveData<Boolean>()
    private var currentLogin: Job? = null

    fun onCreate(){
        isLoading.postValue(false)
    }

    /**
     * Simple login validation
     */
    fun login(username:String, password:String, response: suspend ((LoginResponse) -> Unit)){
        isLoading.postValue(true)
        currentLogin?.cancel()

        if(username != "user" || password != "12345"){
            isLoading.postValue(false)
            //Use jobs and ioSafe to start thread out of Main
           currentLogin = ioSafe {
               //Ensure current coroutine is active
               if (!this.isActive) return@ioSafe
               response(LoginResponse(false, "Incorrect username or password"))
           }
        return
        }
        //Login success, delay 1.3 seconds and return success response
        currentLogin = ioSafe {
            delay(1300)
            if (!this.isActive) return@ioSafe
            response(LoginResponse())
        }
    }

}