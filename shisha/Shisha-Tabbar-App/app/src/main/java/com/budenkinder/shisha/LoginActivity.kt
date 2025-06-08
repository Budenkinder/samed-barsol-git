package com.budenkinder.shisha

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit
import com.budenkinder.shisha.databinding.ActivityLoginMainBinding // Adjust package if different

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private lateinit var binding: ActivityLoginMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginMainBinding.inflate(layoutInflater);
        setContentView(binding.root)

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }

        // Assign click listeners
        binding.buttonStartVerification.setOnClickListener(this)
        binding.buttonVerifyPhone.setOnClickListener(this)
        binding.buttonResend.setOnClickListener(this)
        binding.signOutButton.setOnClickListener(this)

        binding.goToList.setOnClickListener {
            if (!TextUtils.isEmpty(FirebaseAuth.getInstance().currentUser?.uid)) {
                startActivity(Intent(MainActivity@ this, MainActivity::class.java))
            }
        }

        auth = FirebaseAuth.getInstance()
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
                verificationInProgress = false
                updateUI(STATE_VERIFY_SUCCESS, credential)
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)
                verificationInProgress = false
                if (e is FirebaseAuthInvalidCredentialsException) {
                    binding.fieldPhoneNumber.error = "Invalid phone number."
                } else if (e is FirebaseTooManyRequestsException) {
                    Snackbar.make(
                        findViewById(android.R.id.content), "Quota exceeded.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

                updateUI(STATE_VERIFY_FAILED)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent:$verificationId")

                storedVerificationId = verificationId
                resendToken = token

                updateUI(STATE_CODE_SENT)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
        if (verificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(binding.fieldPhoneNumber.text.toString())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks
        )

        verificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks, // OnVerificationStateChangedCallbacks
            token
        )
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    updateUI(STATE_SIGNIN_SUCCESS, user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        binding.fieldVerificationCode.error = "Invalid code."
                    }
                    updateUI(STATE_SIGNIN_FAILED)
                }
            }
    }

    private fun signOut() {
        auth.signOut()
        updateUI(STATE_INITIALIZED)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user)
        } else {
            updateUI(STATE_INITIALIZED)
        }
    }

    private fun updateUI(uiState: Int, cred: PhoneAuthCredential) {
        updateUI(uiState, null, cred)
    }

    private fun updateUI(
        uiState: Int,
        user: FirebaseUser? = auth.currentUser,
        cred: PhoneAuthCredential? = null
    ) {
        when (uiState) {
            STATE_INITIALIZED -> {
                enableViews(binding.buttonStartVerification, binding.fieldPhoneNumber)
                disableViews(binding.buttonVerifyPhone, binding.buttonResend, binding.fieldVerificationCode)
                binding.detail.text = null
            }
            STATE_CODE_SENT -> {
                enableViews(
                    binding.buttonVerifyPhone,
                    binding.buttonResend,
                    binding.fieldPhoneNumber,
                    binding.fieldVerificationCode
                )
                disableViews(binding.buttonStartVerification)
                binding.detail.setText(R.string.status_code_sent)
            }
            STATE_VERIFY_FAILED -> {
                enableViews(
                    binding.buttonStartVerification, binding.buttonVerifyPhone, binding.buttonResend, binding.fieldPhoneNumber,
                    binding.fieldVerificationCode
                )
                binding.detail.setText(R.string.status_verification_failed)
            }
            STATE_VERIFY_SUCCESS -> {
                disableViews(
                    binding.buttonStartVerification, binding.buttonVerifyPhone, binding.buttonResend, binding.fieldPhoneNumber,
                    binding.fieldVerificationCode
                )
                binding.detail.setText(R.string.status_verification_succeeded)

                if (cred != null) {
                    if (cred.smsCode != null) {
                        binding.fieldVerificationCode.setText(cred.smsCode)
                    } else {
                        binding.fieldVerificationCode.setText(R.string.instant_validation)
                    }
                }
            }
            STATE_SIGNIN_FAILED ->
                binding.detail.setText(R.string.status_sign_in_failed)
            STATE_SIGNIN_SUCCESS -> {
            }
        }

        if (user == null) {
            binding.phoneAuthFields.visibility = View.VISIBLE
            binding.signedInButtons.visibility = View.GONE

            binding.status.setText(R.string.signed_out)
        } else {
            binding.phoneAuthFields.visibility = View.GONE
            binding.signedInButtons.visibility = View.VISIBLE

            enableViews(binding.fieldPhoneNumber, binding.fieldVerificationCode)
            binding.fieldPhoneNumber.text = null
            binding.fieldVerificationCode.text = null

            binding.status.setText(R.string.signed_in)
            binding.detail.text = getString(R.string.firebase_status_fmt, user.uid)
        }
    }

    private fun validatePhoneNumber(): Boolean {
        val phoneNumber = binding.fieldPhoneNumber.text.toString()
        if (TextUtils.isEmpty(phoneNumber)) {
            binding.fieldPhoneNumber.error = "Invalid phone number."
            return false
        }

        return true
    }

    private fun enableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = true
        }
    }

    private fun disableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = false
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonStartVerification -> {
                if (!validatePhoneNumber()) {
                    return
                }

                startPhoneNumberVerification(binding.fieldPhoneNumber.text.toString())
            }
            R.id.buttonVerifyPhone -> {
                val code = binding.fieldVerificationCode.text.toString()
                if (TextUtils.isEmpty(code)) {
                    binding.fieldVerificationCode.error = "Cannot be empty."
                    return
                }

                verifyPhoneNumberWithCode(storedVerificationId, code)
            }
            R.id.buttonResend -> resendVerificationCode(
                binding.fieldPhoneNumber.text.toString(),
                resendToken
            )
            R.id.signOutButton -> signOut()
        }
    }

    companion object {
        private const val TAG = "PhoneAuthActivity"
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_INITIALIZED = 1
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_CODE_SENT = 2
        private const val STATE_SIGNIN_FAILED = 5
        private const val STATE_SIGNIN_SUCCESS = 6
    }
}