package com.cmyk.cheersapp.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cmyk.cheersapp.MainActivity
import com.cmyk.cheersapp.R
import com.cmyk.cheersapp.model.Message
import com.cmyk.cheersapp.model.User
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_room_chat.*

class RoomChatActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val EXTRA_USER = "extra_user"
        const val RESULT_CHAT_OK = 301
    }

    private var sender = User()
    private var receiver = User()
    private lateinit var database: FirebaseDatabase
    var message = Message()
    private lateinit var messageEventListener: ChildEventListener
    private lateinit var idMessageEventListener: ChildEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_chat)
        database = FirebaseDatabase.getInstance()
        if (intent != null) {
            val user = intent.getParcelableExtra<User>(EXTRA_USER) as User
            when (user.currentRole) {
                MainActivity.TELLER -> initializedUser(user, MainActivity.LISTENER)
                MainActivity.LISTENER -> initializedUser(user, MainActivity.TELLER)
            }
        }
        btnSend.setOnClickListener(this)
    }

    private fun initializedUser(user: User, antiRole: String) {
        sender = user
        FirebaseDatabase.getInstance().getReference("rooms").child(sender.room.roomId.toString())
            .child(antiRole).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    receiver.id = snapshot.value as String
                    FirebaseDatabase.getInstance().getReference("users").child(receiver.id)
                        .child("username")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {

                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                receiver.username = snapshot.value as String
                                tvReceiver.text = receiver.username
                            }

                        })
                }

            })
        val reference = database.getReference("messages")
        messageEventListener = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java) as Message
                tvTestingMessage.text = message.text
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

        }
        reference.child(user.room.roomId.toString())
            .addChildEventListener(messageEventListener)
        idMessageEventListener = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                if (snapshot.key == sender.room.roomId.toString()) {
                    Toast.makeText(
                        this@RoomChatActivity,
                        "Interlocutor has leave",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

        }
        reference.addChildEventListener(idMessageEventListener)
    }

    override fun onDestroy() {
        val roomRef = database.getReference("rooms")
        val messagesRef = database.getReference("messages")
        roomRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val id = sender.room.roomId
                if (snapshot.hasChild(id.toString())) {
                    roomRef.child(id.toString()).removeValue()
                }
            }

        })
        messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val id = sender.room.roomId
                if (snapshot.hasChild(id.toString())) {
                    messagesRef.child(id.toString()).removeValue()
                }
            }

        })
        messagesRef.child(sender.room.roomId.toString()).removeEventListener(messageEventListener)
        messagesRef.removeEventListener(idMessageEventListener)
        super.onDestroy()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnSend -> {
                message.text = etTestingMessageBox.text.toString()
                Log.d("MessageText", etTestingMessageBox.text.toString())
                message.receiver = receiver.id
                message.sender = sender.id
                val messageId =
                    database.getReference("messages").child(sender.room.roomId.toString())
                messageId.child(messageId.push().key.toString()).setValue(message.toMap())
            }
        }
    }
}
