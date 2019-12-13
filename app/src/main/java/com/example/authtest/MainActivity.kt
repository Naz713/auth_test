package com.example.app

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        FirebaseAuth.getInstance().setLanguageCode("es")

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null){
            Log.i(TAG,"Current User already logged in: $user")
        }

        phoneButton.setOnClickListener {
            sendVerificationCode()
        }

        signButton.setOnClickListener {
            if (inputCode.text.isNotBlank() && inputCode.text.isNotEmpty()){
                signInWithPhoneAuthCode(inputCode.text.toString())
            }
        }
    }

    fun sendVerificationCode(){
        val context :Context = this

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "AutoVerificationSuccessful: $credential")
                signInWithPhoneAuthCredential(credential)

                phoneLayout.setVisibility(View.VISIBLE)
                codeLayout.setVisibility(View.VISIBLE)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    showPopop(context, "Número Invalido")

                } else if (e is FirebaseTooManyRequestsException) {
                    showPopop(context, "Máximo número de códigos enviados exedido. Reintente en unos minutos")
                }

                phoneLayout.setVisibility(View.VISIBLE)
                codeLayout.setVisibility(View.GONE)
            }

            override fun onCodeSent(
                newVerificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent: $newVerificationId")

                verificationId = newVerificationId
                storedToken = token

                phoneLayout.setVisibility(View.GONE)
                codeLayout.setVisibility(View.VISIBLE)
            }

            override fun onCodeAutoRetrievalTimeOut(newVerificationId: String?) {
                Log.d(TAG, "timeOut Reached")
                verificationId = newVerificationId

                phoneLayout.setVisibility(View.VISIBLE)
                codeLayout.setVisibility(View.GONE)
            }
        }

        if (inputPhone.text.isNotBlank() && inputPhone.text.isNotEmpty()){
            val number :String = "+52"+inputPhone.text.toString()

            Log.i(TAG,"Telefono: $number")

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number, // Phone number to verify
                2, // Timeout duration
                TimeUnit.MINUTES, // Unit of timeout
                this, // Activity (for callback binding)
                callbacks) // OnVerificationStateChangedCallbacks
        }
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null){
            Log.i(TAG,"Current User already logged in: $user")
        } else {
            sendVerificationCode()
        }
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential){
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user.toString()
                    Log.i(TAG,"User: $user")
                    showPopop(this,"Autenticado Correctamente")
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        showPopop(this,"Código Incorrecto")
                    }
                }
            }
    }

    fun signInWithPhoneAuthCode(code :String){
        Log.d(TAG,"onGetCredential")
        if (verificationId != null){
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            Log.i(TAG,"credential successful")
            signInWithPhoneAuthCredential(credential)
        } else {
            Log.e(TAG,"verificationId null")
        }
    }

    fun showPopop(context : Context, msg :String){
        AlertDialog.Builder(context)
            .setMessage(msg)
            .create()
            .show()
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
