package no.kristiania.myApp.myfirstmessenger.Messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import no.kristiania.myApp.myfirstmessenger.R
import no.kristiania.myApp.myfirstmessenger.models.User
import no.kristiania.myApp.myfirstmessenger.models.ChatMessage

class ChatLogActivity : AppCompatActivity() {

    companion object{
        val TAG = "Chatlog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recycelerView_chat_log.adapter = adapter
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = toUser?.username


        listenForMessages()

        send_button_chat_log.setOnClickListener {
            Log.d(TAG, "Attempt to send message ...")
            performSendMessage()
        }
    }

    private fun listenForMessages() {

        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            val chatMessage = p0.getValue(ChatMessage::class.java)

                if(chatMessage != null){
                    Log.d(TAG, chatMessage.text)

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    }else{
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }
                recycelerView_chat_log.scrollToPosition(adapter.itemCount -1)
            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }


    private fun performSendMessage() {
        //how do we actually send a message to firebase
        val text = edit_chat_message_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        if( fromId == null) return

//        val referance = FirebaseDatabase.getInstance().getReference("/messages").push()
        val referance = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toReferance = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()



        val chatMessage = ChatMessage(referance.key!!, text, fromId, toId, System.currentTimeMillis() / 1000 )
        referance.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG,"Saved our chat message: ${referance.key}")
                edit_chat_message_chat_log.text.clear()
                recycelerView_chat_log.scrollToPosition(adapter.itemCount -1)
            }
        toReferance.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }


}

class ChatFromItem(val text: String,val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
    viewHolder.itemView.text_to_row_chat.text = text

    val uri = user.profileImageUrl
    val targeImageView = viewHolder.itemView.imageView_chat_to_row
    Picasso.get().load(uri).into(targeImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}

class ChatToItem(val text: String, val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.text_from_row_chat.text = text

        val uri = user.profileImageUrl
        val targeImageView = viewHolder.itemView.imageView_chat_from_row
        Picasso.get().load(uri).into(targeImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}
