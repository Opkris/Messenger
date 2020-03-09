package no.kristiania.myApp.myfirstmessenger.option

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_option.*
import no.kristiania.myApp.myfirstmessenger.Messages.*
import no.kristiania.myApp.myfirstmessenger.Messages.LatestMessagesActivity.Companion.currentUser
import no.kristiania.myApp.myfirstmessenger.R
import no.kristiania.myApp.myfirstmessenger.models.User
import java.util.*

class OptionMenu : AppCompatActivity(){

    companion object{
        val TAG = "OptionActivity"
    }

    var user: User? = null
    private val databaseReference = FirebaseDatabase.getInstance().getReference("/users")
    private val storage = FirebaseStorage.getInstance()

    private val userUID = FirebaseAuth.getInstance().uid
    private val userProfileImg = FirebaseDatabase.getInstance().getReference("/$userUID/profileImageUrl/")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_option)
        supportActionBar?.title = "Option"

        user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        profile_image_button_option_menu.setOnClickListener {

            Log.d(TAG, "Try to show photo select Activity")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        save_settings_option_menu.setOnClickListener {
            Log.d(TAG, "hitting save settings! ")
            deleteImage()
            uploadImageToFirebaseStorage()
        }

        update_log_d.setOnClickListener {
            Log.d(TAG,"*****Update******\nOld Uid: " + selectedPhotoUri + "\nNew Uid: " + newPhotoUri)
            Log.d(TAG,userUID.toString() + " *** " + userProfileImg.toString() + " ******")

        }

        fetchCurrentUser()


    }// end onCreate

    var selectedPhotoUri: Uri? = null
    var newPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            // proceed and check what the selected image was.
            Log.d(TAG,"Photo was selected")

            newPhotoUri = data.data

            val bitmap= MediaStore.Images.Media.getBitmap(contentResolver, newPhotoUri)
            selectphotoimage_user_option.setImageBitmap(bitmap)
            profile_image_button_option_menu.alpha = 0f

        }
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d(TAG, "Current user: ${currentUser?.username}")
                Log.d(TAG, "Current userImage: ${currentUser?.profileImageUrl}")
                Log.d(TAG, "SelectedPhotoUri: $selectedPhotoUri\noldPhotoRefUri: $newPhotoUri")

                username_option_menu.text = currentUser?.username
                val uri = currentUser?.profileImageUrl
                Picasso.get().load(uri).into(selectphotoimage_user_option)


                val test =Uri.parse(currentUser?.profileImageUrl)
                selectedPhotoUri = test
                profile_image_button_option_menu.alpha = 0f

//                val getOldUri = currentUser?.profileImageUrl
//                oldPhotoRef = getOldUri as Nothing?


            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })
    }


    private fun uploadImageToFirebaseStorage() {
        if(newPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(newPhotoUri!!).addOnSuccessListener { it ->
            Log.d(TAG,"Successfully uploaded image: ${it.metadata?.path}")

            ref.downloadUrl.addOnSuccessListener {
                Log.d(TAG, "File location: $it")

             //  saveUserToFirebaseDatabase(it.toString())
            }
        }
            .addOnFailureListener {
                Log.d(TAG, "Failed to upload image")
            }
    }

    private fun deleteImage(){

        // Create a storage reference from our app
        val storageRef = storage.reference

// Create a reference to the file to delete
        val desertRef = storageRef.child("users/" + userUID + "/profileImageUrl")

// Delete the file
        desertRef.delete().addOnSuccessListener {
            // File deleted successfully
            Log.d(TAG,"File deleted successfully")
        }.addOnFailureListener {
            // Uh-oh, an error occurred!
            Log.d(TAG, "Uh-oh, an error occurred!")
        }

    }





















}


