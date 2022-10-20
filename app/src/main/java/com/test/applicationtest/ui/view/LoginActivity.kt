package com.test.applicationtest.ui.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.test.applicationtest.R
import com.test.applicationtest.databinding.ActivityLoginBinding
import com.test.applicationtest.helper.ActivityUtils.fullScreen
import com.test.applicationtest.model.login.LoginResponse
import com.test.applicationtest.ui.viewmodel.LoginViewModel
import com.test.applicationtest.helper.ActivityUtils.hideKeyboard
import com.test.applicationtest.helper.CoroutinesHelper.main
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val TAG = "MAIN"

    /**
     * We validate current form state, changing color and helper text when any input is empty
     */
    private fun validateForm(){

        val notEmpty = usernameTxt.isNotEmpty() && passwordTxt.isNotEmpty()
        val colorBtn = ContextCompat.getColor(this, if(notEmpty) R.color.primary else R.color.medium_gray)
        binding.loginButton.isEnabled = notEmpty
        binding.loginButton.setCardBackgroundColor(colorBtn)
        binding.userName.helperText = if(usernameTxt.isEmpty()) getString(R.string.username_empty) else ""
        binding.password.helperText = if(passwordTxt.isEmpty()) getString(R.string.password_empty) else ""

    }

    private val viewModel:LoginViewModel by viewModels()
    private lateinit var binding:ActivityLoginBinding
    private var usernameTxt = ""
    private var passwordTxt = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        val screenSplash = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullScreen()
        viewModel.onCreate()
        screenSplash.setKeepOnScreenCondition{
            false
        }

        //Config all views
        configViews()

    }

    private fun configViews() {

    binding.loginButton.setOnClickListener {
        hideKeyboard()
            viewModel.login(usernameTxt, passwordTxt){
                handleResult(it)

            }
        }

    viewModel.isLoading.observe(this){
            toggleLoading(it)
        }

    binding.userName.editText?.addTextChangedListener(
        onTextChanged = { s, _, _, _ ->
            usernameTxt = s.toString()
            validateForm()
        }
    )

    binding.password.editText?.addTextChangedListener(
                onTextChanged = { s, _, _, _ ->
                    passwordTxt = s.toString()
                    validateForm()
                }
         )
    //When user press ENTER in keyboard being in password input we must hide keyboard and focus login button
    binding.password.editText?.setOnEditorActionListener{_, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.loginButton.requestFocus()
                hideKeyboard()
               return@setOnEditorActionListener true
            }
            false
        }

    binding.loginButton.isEnabled = false
    }

    //Handle login result
    private fun handleResult(it: LoginResponse) {
        if(it.success){
            //Logged
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }else{
            val msg = it.messageError
            main {
            val toast =  Toasty.error(
                    this@LoginActivity,
                    msg,
                    Toast.LENGTH_SHORT,
                    true
                )
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show()

            }
        }
    }

    // Toggle loading when is needed, hiding login button text
    private fun toggleLoading(visible:Boolean) {
        binding.loadingBtn.isVisible = visible
        binding.loadingBtn.playAnimation()
        binding.txtButtonLogin.isVisible = !visible

        binding.userName.clearFocus()
        binding.password.clearFocus()
    }



}