package com.example.myfblogin

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.Exception
import java.util.*

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : BaseActivity(){
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAuthTask: UserLoginTask? = null
    private var mAuth: FirebaseAuth?=null
    private var callbackManager: CallbackManager? = null
    private var loginCallback: FacebookCallback<LoginResult>? = null
    private var accessToken: AccessToken? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        title = "Login Page"

        mAuth = FirebaseAuth.getInstance()
        callbackManager = com.facebook.CallbackManager.Factory.create()
        loginCallback = object : FacebookCallback<LoginResult>{
            override fun onCancel() {
                Log.d("AAA", "onCancel")
            }

            override fun onSuccess(loginResult: LoginResult?) {
                val parameters = Bundle()
                parameters.putString("fields", "name,last_name,email,picture")
                val request = GraphRequest.newMeRequest(loginResult!!.accessToken) { jObj, _ ->
                    if (jObj != null) {
                        Log.d("AAA", "Json Object: $jObj")
                    }
                }
                request.parameters = parameters
                request.executeAsync()
                accessToken = loginResult.accessToken
                firebaseAuthWithFacebook()
            }

            override fun onError(error: FacebookException?) {
                val msg = error!!.message
                Log.d("AAA", "Exception : $msg")
            }
        }
        val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this@LoginActivity) {
                    Log.d("AAA", "onConnectionFailed")
                }.addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()

        btnLoginFacebook.setOnClickListener{
            doLoginFacebook()
        }

        btnLoginGoogle.setOnClickListener{
            doLoginGoogle()
        }
    }

    override fun onStart() {
        super.onStart()
        LoginManager.getInstance().retrieveLoginStatus(this@LoginActivity, object : LoginStatusCallback{
            override fun onFailure() {
                Log.d("AAA", "retrieveLoginStatus:onFailure")
                updateUIFacebook()
            }

            override fun onError(exception: Exception?) {
                Log.d("AAA", "retrieveLoginStatus:onError : " + exception!!.message)
                updateUIFacebook()
            }

            override fun onCompleted(accessToken: AccessToken?) {
                Log.d("AAA", "retrieveLoginStatus:onCompleted : " + accessToken!!.token)
                this@LoginActivity.accessToken = accessToken
                updateUIFacebook()
            }
        })
        updateUIGoogle()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val result: GoogleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            Log.d("AAA", "handleSignInResult:" + result.isSuccess)
            if (result.isSuccess) {
                // Signed in successfully, show authenticated UI.
                val acct: GoogleSignInAccount? = result.signInAccount
                Log.d("AAA", "account:" + acct!!.account)
                Log.d("AAA", "displayName:" + acct.displayName)
                firebaseAuthWithGoogle(acct)
            } else {
                // Signed out, show unauthenticated UI.
                updateUIGoogle()
            }
        } else {
            if (resultCode == Activity.RESULT_OK) {
                callbackManager?.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onCreateHeaderAction() {}

    fun doLoginFacebook() {
        LoginManager.getInstance().registerCallback(callbackManager, loginCallback)
        LoginManager.getInstance().logInWithReadPermissions(this@LoginActivity, Arrays.asList("email", "public_profile"))
    }

    fun doLoginGoogle() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun doLogout() {
        LoginManager.getInstance().logOut()
        mAuth!!.signOut()
        updateUIFacebook()
        updateUIGoogle()
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    inner class UserLoginTask internal constructor() : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                return false
            }

            return true
        }

        override fun onPostExecute(success: Boolean?) {
            mAuthTask = null

            if (success!!) {
                finish()
            } else {

            }
        }

        override fun onCancelled() {
            mAuthTask = null
        }
    }

    companion object {
        private val REQUEST_READ_CONTACTS = 0
        private val DUMMY_CREDENTIALS = arrayOf("foo@example.com:hello", "bar@example.com:world")
    }

    fun firebaseAuthWithFacebook() {
        Log.d("AAA", "firebaseAuthWithFacebook:" + accessToken!!.token)

        val credential:AuthCredential = FacebookAuthProvider.getCredential(accessToken!!.token)
        if (mAuth!!.currentUser == null) {
            mAuth!!.signInWithCredential(credential)
                    .addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
                        override fun onComplete(task: Task<AuthResult>) {
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("AAA", "signInWithCredential:success")
                                val user:FirebaseUser? = mAuth!!.currentUser
                                val uid = user!!.uid
                                val name = user.displayName
                                val email = user.email

                                Log.d("AAA", "user id: $uid")
                                Log.d("AAA", "name: $name")
                                Log.d("AAA", "email: $email")
                                updateUIFacebook()
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("AAA", "signInWithCredential:failure", task.exception)
                                Toast.makeText(this@LoginActivity, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show()
                                updateUIFacebook()
                            }
                        }
                    })
        } else {
            mAuth!!.currentUser!!.linkWithCredential(credential)
                    .addOnCompleteListener(this@LoginActivity, { task ->
                        if (task.isSuccessful) {
                            Log.d("AAA", "linkWithCredential:success")
                            updateUIFacebook()
                        } else {
                            Log.w("AAA", "linkWithCredential:failure", task.exception)
                            Toast.makeText(this@LoginActivity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                            updateUIFacebook()
                        }
                    })
        }
    }

    fun firebaseAuthWithGoogle(acct:GoogleSignInAccount) {
        Log.d("AAA", "firebaseAuthWithGoogle:" + acct.id)

        val credential:AuthCredential  = GoogleAuthProvider.getCredential(acct.idToken, null)
        if (mAuth!!.currentUser == null) {
            mAuth!!.signInWithCredential(credential)
                    .addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
                        override fun onComplete(task: Task<AuthResult>) {
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("AAA", "signInWithCredential:success")
                                val user:FirebaseUser? = mAuth!!.currentUser
                                val uid = user!!.uid
                                val name = user.displayName
                                val email = user.email

                                Log.d("AAA", "user id: $uid")
                                Log.d("AAA", "name: $name")
                                Log.d("AAA", "email: $email")
                                updateUIGoogle()
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("AAA", "signInWithCredential:failure", task.exception)
                                Toast.makeText(this@LoginActivity, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show()
                                updateUIGoogle()
                            }
                        }
                    })
        } else {
            mAuth!!.currentUser!!.linkWithCredential(credential)
                    .addOnCompleteListener(this@LoginActivity, { taskOnComplete ->
                        if (taskOnComplete.isSuccessful) {
                            Log.d("AAA", "linkWithCredential:success")
                            updateUIGoogle()
                        } else {
                            Log.w("AAA", "linkWithCredential:failure", taskOnComplete.exception)
                            Toast.makeText(this@LoginActivity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                            updateUIGoogle()
                        }
                    })
        }
    }

    fun updateUIFacebook(){
        val currentUser:FirebaseUser? = mAuth!!.currentUser
        if (currentUser != null) {
            btnLoginFacebook.text = getString(R.string.action_sign_out)
            btnLoginFacebook.setOnClickListener{
                doLogout()
            }
        } else {
            btnLoginFacebook.text = getString(R.string.action_sign_in_with_facebook)
            btnLoginFacebook.setOnClickListener{
                doLoginFacebook()
            }
        }
    }

    fun updateUIGoogle(){
        val currentUser:FirebaseUser? = mAuth!!.currentUser
        if (currentUser != null) {
            btnLoginGoogle.text = getString(R.string.action_sign_out)
            btnLoginGoogle.setOnClickListener{
                doLogout()
            }
        } else {
            btnLoginGoogle.text = getString(R.string.action_sign_in_with_google)
            btnLoginGoogle.setOnClickListener{
                doLoginGoogle()
            }
        }
    }
}
