package com.example.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.authtest.R
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.concurrent.TimeUnit

const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private var verificationId :String? = null
    private var storedToken :PhoneAuthProvider.ForceResendingToken? = null
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted:$credential")

            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                // ...
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // ...
            }
        }

        override fun onCodeSent(
            newVerificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d(TAG, "onCodeSent:$newVerificationId")

            verificationId = newVerificationId
            storedToken = token
        }

        override fun onCodeAutoRetrievalTimeOut(newVerificationId: String?) {
            Log.d(TAG, "timeOut Reached")
            verificationId = newVerificationId
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        FirebaseAuth.getInstance().setLanguageCode("es")

        phoneButton.setOnClickListener {
            if (inputPhone.text.isNotBlank() && inputPhone.text.isNotEmpty()){
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    inputPhone.text.toString(), // Phone number to verify
                    5, // Timeout duration
                    TimeUnit.MINUTES, // Unit of timeout
                    this, // Activity (for callback binding)
                    callbacks) // OnVerificationStateChangedCallbacks
                phoneLayout.setVisibility(View.GONE)
                codeLayout.setVisibility(View.VISIBLE)
            }
        }

        signButton.setOnClickListener {
            if (inputCode.text.isNotBlank() && inputCode.text.isNotEmpty()){
                signInWithPhoneAuthCode(inputCode.text.toString())
                phoneLayout.setVisibility(View.VISIBLE)
                codeLayout.setVisibility(View.VISIBLE)
            }
        }
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential){
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    // ...
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            }
    }

    fun signInWithPhoneAuthCode(code :String){
        Log.d(TAG,"onGetCredential")
        if (verificationId != null){
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            Log.d(TAG,"credential successful")
            signInWithPhoneAuthCredential(credential)
        } else {
            Log.d(TAG,"verificationId null")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
