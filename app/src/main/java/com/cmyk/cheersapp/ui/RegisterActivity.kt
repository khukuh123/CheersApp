package com.cmyk.cheersapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cmyk.cheersapp.R
import com.cmyk.cheersapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val RESULT_REGISTER_OK = 201
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        user = User()

        btnRegister2.setOnClickListener(this)
    }

    private fun register(user: User) {
        auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener {
                val firebaseUser = auth.currentUser
                user.id = firebaseUser?.uid as String

                reference = FirebaseDatabase.getInstance().getReference("users").child(user.id)

                val hashMap = HashMap<String, String>()
                hashMap["id"] = user.id
                hashMap["username"] = user.username

                reference.setValue(hashMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.putExtra(LoginActivity.EXTRA_USER, user)
                        setResult(RESULT_REGISTER_OK, intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "You can't make with this email or password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                loadingRegister.visibility = View.GONE
            }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnRegister2 -> {
                var errorMsg = ""
                user.username = etUserName.text.toString()
                user.email = etEmail2.text.toString()
                user.password = etPassword2.text.toString()
                if (user.username.isEmpty() || user.email.isEmpty() || user.password.isEmpty() || user.password.length < 8) {
                    when {
                        user.username.isEmpty() -> {
                            errorMsg = "Username tidak boleh kosong!"
                        }
                        user.email.isEmpty() -> {
                            errorMsg = "Email tidak boleh kosong!"
                        }
                        user.password.isEmpty() -> {
                            errorMsg = "Password tidak boleh kosong!"
                        }
                        user.password.length < 8 -> {
                            errorMsg = "Password tidak boleh kurang dari 8!"
                        }
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                } else {
                    loadingRegister.visibility = View.VISIBLE
                    register(user)
                    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                        currentFocus?.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                }
            }
        }
    }
}
