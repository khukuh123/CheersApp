package com.cmyk.cheersapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cmyk.cheersapp.model.Room
import com.cmyk.cheersapp.model.User
import com.cmyk.cheersapp.ui.LoginActivity
import com.cmyk.cheersapp.ui.RoomChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val REQUEST_LOGIN = 100
        const val TELLER = "teller"
        const val LISTENER = "listener"
        const val STATUS_SEARCHING = "searching"
        const val STATUS_WAITING = "waiting"
        const val STATUS_CHATTING = "chatting"
        const val REQUEST_CHAT = 300
        const val STATUS_IDLE = "idle"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivityForResult(intent, REQUEST_LOGIN)
        } else {
            loadingMain.visibility = View.VISIBLE
            initializedUser()
        }

        btnLogout.setOnClickListener(this)
        btnListener.setOnClickListener(this)
        btnTeller.setOnClickListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == LoginActivity.RESULT_LOGIN_OK) {
                initializedUser()
            } else if (resultCode == LoginActivity.RESULT_EXIT_OK) {
                finish()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnLogout -> {
                auth.signOut()
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivityForResult(intent, REQUEST_LOGIN)
            }
            R.id.btnListener -> {
                currentUser.currentRole = LISTENER
                searchRoom(LISTENER, TELLER)
            }
            R.id.btnTeller -> {
                currentUser.currentRole = TELLER
                searchRoom(TELLER, LISTENER)
            }
        }
    }

    private fun searchRoom(role: String, antiRole: String) {
        val reference = FirebaseDatabase.getInstance().getReference("rooms")
        currentUser.status = STATUS_SEARCHING
        loadingMain.visibility = View.VISIBLE
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                //Apabila ada room, cari
                if (snapshot.hasChildren() && currentUser.status == STATUS_SEARCHING) {
                    val listRoom = iterateRooms(snapshot)
                    //Apabila terdapat kecocokan antar room -> join room
                    for (i: Int in 0 until listRoom.size) {
                        if (!listRoom[i].roomFull) {
                            if (listRoom[i].neededRole == role && currentUser.status == STATUS_SEARCHING) {
                                val hashMap = HashMap<String, Any>()
                                hashMap[role] = currentUser.id
                                hashMap["neededRole"] = ""
                                hashMap["roomFull"] = true
                                currentUser.status = STATUS_CHATTING
                                reference.child("$i").updateChildren(hashMap)
                                Toast.makeText(
                                    this@MainActivity,
                                    "You've got match",
                                    Toast.LENGTH_SHORT
                                ).show()
                                moveToRoomChat()
                                loadingMain.visibility = View.GONE
                                break
                            }
                        }
                    }
                    //Apabila tidak menemukan kecocokan antar room -> buat room
                    if (currentUser.status == STATUS_SEARCHING) {
                        val arrayRoomId = ArrayList<Int>()
                        for (i in listRoom) {
                            arrayRoomId.add(i.roomId)
                        }
                        val arrayComplete =
                            (0 until arrayRoomId.last() + 2).toCollection(ArrayList())
                        currentUser.room.roomId = arrayComplete.minus(arrayRoomId).first()
                        val hashMap = HashMap<String, Any>()
                        hashMap[role] = currentUser.id
                        hashMap[antiRole] = ""
                        hashMap["neededRole"] = antiRole
                        hashMap["roomFull"] = false
                        hashMap["roomFull"] = false
                        hashMap["roomId"] = currentUser.room.roomId
                        currentUser.status = STATUS_WAITING
                        reference.child(currentUser.room.roomId.toString()).setValue(hashMap)
                    }
                }
                //Apabila tidak ada room sama sekali, buat dengan roomId = 0
                else if (!snapshot.hasChildren() && currentUser.status == STATUS_SEARCHING) {
                    val hashMap = HashMap<String, Any>()
                    hashMap[role] = currentUser.id
                    hashMap[antiRole] = ""
                    hashMap["neededRole"] = antiRole
                    hashMap["roomFull"] = false
                    hashMap["roomId"] = 0
                    currentUser.status = STATUS_WAITING
                    currentUser.room.roomId = 0
                    reference.child("0").setValue(hashMap)
                    Toast.makeText(
                        this@MainActivity,
                        "No Room Available, creating new one",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                //Apabila user sedang menunggu -> tunggu
                if (currentUser.status == STATUS_WAITING) {
                    reference.child(currentUser.room.roomId.toString())
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {

                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                //Apabila room user full dan cocok -> chat
                                if (snapshot.child("roomFull").value == true && currentUser.status == STATUS_WAITING) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "You've got Match",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    currentUser.status = STATUS_CHATTING
                                    moveToRoomChat()
                                    loadingMain.visibility = View.GONE
                                }
                            }
                        })
                    /*reference.child(currentUser.room.roomId.toString())
                        .addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {

                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                //Apabila room user full dan cocok -> chat
                                if (snapshot.child("roomFull").value == true) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "You've got Match",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    currentUser.status = STATUS_CHATTING
                                    reference.removeEventListener(this)
                                    moveToRoomChat()
                                    loadingMain.visibility = View.GONE
                                }
                            }
                        })*/
                } else if (currentUser.status == STATUS_CHATTING) {
                    reference.removeEventListener(this)
                }
            }
        })
    }

    fun moveToRoomChat() {
        val intent = Intent(this@MainActivity, RoomChatActivity::class.java)
        intent.putExtra(RoomChatActivity.EXTRA_USER, currentUser)
        startActivityForResult(intent, REQUEST_CHAT)
    }

    fun iterateRooms(snapshot: DataSnapshot): ArrayList<Room> {
        val listOfChildren = ArrayList<Room>()
        for (i in snapshot.children) {
            val children = i.getValue(Room::class.java) as Room
            listOfChildren.add(children)
        }
        return listOfChildren
    }

    private fun initializedUser() {
        val firebaseUser = auth.currentUser
        val reference =
            FirebaseDatabase.getInstance().getReference("users").child(firebaseUser?.uid as String)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java) as User
                val welcoming = "Halo! Selamat datang, Tuan ${currentUser.username}"
                tvWelcoming.text = welcoming
                loadingMain.visibility = View.GONE
            }
        })
    }
}
