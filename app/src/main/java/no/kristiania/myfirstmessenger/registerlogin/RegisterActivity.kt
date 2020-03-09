package no.kristiania.myApp.myfirstmessenger.registerlogin


import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import no.kristiania.myApp.myfirstmessenger.Messages.LatestMessagesActivity
import no.kristiania.myApp.myfirstmessenger.R
import no.kristiania.myApp.myfirstmessenger.models.User
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        supportActionBar?.title = "Create new Accountkristoff"
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button_register.setOnClickListener {
        performRegister()
        }

        already_have_an_account_register.setOnClickListener {
            Log.d("RegisterActivity", "Try to show log activity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        profile_image_register.setOnClickListener {
            Log.d("RegisterActivity", "Try to show photo select Activity")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            // proceed and check what the selected image was.
            Log.d("RegisterActivity","Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selectphotoimage_register.setImageBitmap(bitmap)

            profile_image_register.alpha =0f

        }
    }

    private fun performRegister() {
        val email = email_register.text.toString()
        val password = password_register.text.toString()

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Pleas enter text in email/pw",Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity","Email is: $email")
        Log.d("RegisterActivity","Password: $password")

        // Initialize Firebase Auth
        var auth: FirebaseAuth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("RegisterActivity", "Successfully created user with uid.")
                    Toast.makeText(baseContext,"Account Successfully Created",
                        Toast.LENGTH_SHORT).show()
                    uploadImageToFirebaseStorage()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.d("RegisterActivity", "Failure to create user", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener { it ->
            Log.d("RegisterActivity","Successfully uploaded image: ${it.metadata?.path}")

            ref.downloadUrl.addOnSuccessListener {
                Log.d("RegisterActivity", "File location: $it")

                saveUserToFirebaseDatabase(it.toString())
            }
        }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Failed to upload image")
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?:"" // ?:"" = Elvis operator insted of check if the string os empty or not
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(
            uid,
            username_register.text.toString(),
            profileImageUrl
        )

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Successfully saved the user to Firebase database")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Failed to save the user to FireBase Database ")
            }
    }
}

