package org.howl.stagram

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import org.howl.stagram.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityLoginBinding
    lateinit var googleSigninClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        binding.googleSigninButton.setOnClickListener {
            //First step
            googleLogin()
        }
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("750936145255-i54ejh0mjumsac3gkiijf3n4f6t6k4ei.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSigninClient = GoogleSignIn.getClient(this, gso)
    }

    private fun googleLogin() {
        val signInIntent = googleSigninClient.signInIntent
        googleLoginResult.launch(signInIntent)
    }

    override fun onStart(){
        super.onStart()
        moveMainPage(auth?.currentUser)
    }

    fun firebaseAuthWithGoogle(idToken: String?) {
        var credential = GoogleAuthProvider.getCredential(idToken,null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (auth.currentUser!!.isEmailVerified) {
                    //이메일 인증이 되었을때
                    moveMainPage(task.result?.user)
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var googleLoginResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        // 구글 로그인이 성공했을때 이메일 값이 넘어오는데 -> Token -> Email -> Firebase 서버
        var data = result.data
        var task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = task.getResult(ApiException::class.java)
        firebaseAuthWithGoogle(account.idToken)
    }

    fun moveMainPage(user: FirebaseUser?){
        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}