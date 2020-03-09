package no.kristiania.myApp.myfirstmessenger.models
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.latest_messages_row.view.*
import no.kristiania.myApp.myfirstmessenger.R


class LatestMessageRow(val chatMessage: ChatMessage): Item<GroupieViewHolder>(){

    var chatParterUser: User? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.latest_message_latest_message_row.text = chatMessage.text

        val chatParterId: String

        if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
            chatParterId = chatMessage.toId
        } else{
            chatParterId = chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatParterId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                chatParterUser = p0.getValue(User::class.java)
                //user?.username
                viewHolder.itemView.username_latest_message_row.text = chatParterUser?.username

                val targetImage = viewHolder.itemView.imageView_latest_message_row
                Picasso.get().load(chatParterUser?.profileImageUrl).into(targetImage)
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.latest_messages_row
    }

}