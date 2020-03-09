package no.kristiania.myApp.myfirstmessenger.registerlogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import no.kristiania.myApp.myfirstmessenger.Messages.LatestMessagesActivity
import no.kristiania.myApp.myfirstmessenger.R

class LoginActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.title = "Login"

        setContentView(R.layout.activity_login)
        login_button_login.setOnClickListener {
            val email = email_login.text.toString()
            val password = password_login.text.toString()
            Log.d("LoginActivity", "email is:  $email")

            var auth: FirebaseAuth = FirebaseAuth.getInstance()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Main", "Successfully created user with uid.")
                        Toast.makeText(
                            baseContext, "Successfully Logged in",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this, LatestMessagesActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d("Main", "Failure to create user", task.exception)
                        Toast.makeText(
                            baseContext, "Wrong Password or Email",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        back_to_registration_login.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }
}