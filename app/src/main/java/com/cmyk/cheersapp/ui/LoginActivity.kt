package com.cmyk.cheersapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cmyk.cheersapp.R
import com.cmyk.cheersapp.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val RESULT_LOGIN_OK = 101
        const val RESULT_EXIT_OK = 102
        const val REQUEST_REGISTER = 200
        const val EXTRA_USER = "extra_user"
    }

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        btnRegister.setOnClickListener(this)
        btnLogin.setOnClickListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_REGISTER) {
            if (resultCode == RegisterActivity.RESULT_REGISTER_OK) {
                val user = data?.getParcelableExtra<User>(EXTRA_USER) as User
                etEmail.setText(user.email, TextView.BufferType.EDITABLE)
                etPassword.setText(user.password, TextView.BufferType.EDITABLE)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnRegister -> {
                val registerIntent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivityForResult(
                    registerIntent,
                    REQUEST_REGISTER
                )
            }
            R.id.btnLogin -> {
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()
                if (email.isEmpty() || password.isEmpty()) {
                    var errorMsg = ""
                    when {
                        email.isEmpty() -> {
                            errorMsg = "Email must not be empty!"
                        }
                        password.isEmpty() -> {
                            errorMsg = "Password must not be empty!"
                        }
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                } else {
                    loadingLogin.visibility = View.VISIBLE
                    login(email, password)
                    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                        currentFocus?.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_EXIT_OK)
        finish()
        super.onBackPressed()
    }

    private fun login(email: String, password: String) {
        auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                setResult(RESULT_LOGIN_OK)
                finish()
            } else {
                Toast.makeText(this@LoginActivity, "Login failed!", Toast.LENGTH_SHORT).show()
            }
            loadingLogin.visibility = View.GONE
        }
    }


}
