package com.cmyk.cheersapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        btnRegister.setOnClickListener(this)
        btnLogin.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnRegister -> {
                val registerIntent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(registerIntent)
            }
            R.id.btnLogin -> {
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()
                if (email.isEmpty() || password.isEmpty()) {
                    var errorMsg = ""
                    when {
                        email.isEmpty() -> {
                            errorMsg = "Email tidak boleh kosong"
                        }
                        password.isEmpty() -> {
                            errorMsg = "Password tidak boleh kosong"
                        }
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                } else {
                    login(email, password)
                }
            }
        }
    }

    private fun login(email: String, password: String) {
        auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@LoginActivity, "Login Sukses!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@LoginActivity, "Login gagal!", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
