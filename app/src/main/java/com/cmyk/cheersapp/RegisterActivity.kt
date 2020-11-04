package com.cmyk.cheersapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        user = User()

        btnRegister2.setOnClickListener(this)
    }

    private fun register(user: User) {
        auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener {
                val firebaseUser = auth.currentUser
                user.id = firebaseUser?.uid as String

                reference = FirebaseDatabase.getInstance().getReference("Users").child(user.id)

                val hashMap = HashMap<String, String>()
                hashMap["id"] = user.id
                hashMap["username"] = user.userName

                reference.setValue(hashMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "You can't make with this email or password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnRegister2 -> {
                var errorMsg = ""
                user.userName = etUserName.text.toString()
                user.email = etEmail2.text.toString()
                user.password = etPassword2.text.toString()
                if (user.userName.isEmpty() && user.email.isEmpty() && user.password.isEmpty()) {
                    when {
                        user.userName.isEmpty() -> {
                            errorMsg = "Username tidak boleh kosong!"
                        }
                        user.email.isEmpty() -> {
                            errorMsg = "Username tidak boleh kosong!"
                        }
                        user.password.isEmpty() -> {
                            errorMsg = "Username tidak boleh kosong!"
                        }
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                } else {
                    register(user)
                }
            }
        }
    }
}
