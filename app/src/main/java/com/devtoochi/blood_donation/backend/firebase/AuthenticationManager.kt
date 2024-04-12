package com.devtoochi.blood_donation.backend.firebase

import android.content.Context
import android.util.Log
import com.devtoochi.blood_donation.R
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.checkIfUserExists
import com.devtoochi.blood_donation.backend.firebase.PersonDetailsManager.createNewUser
import com.devtoochi.blood_donation.backend.models.Donor
import com.devtoochi.blood_donation.backend.models.Hospital
import com.devtoochi.blood_donation.backend.models.User
import com.devtoochi.blood_donation.backend.utils.Constants.DONOR
import com.devtoochi.blood_donation.backend.utils.Constants.HOSPITAL
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider

object AuthenticationManager {

    val auth = UserAuth.instance
    val hospitalsCollection = FirestoreDB.instance.collection(HOSPITAL)
    val donorsCollection = FirestoreDB.instance.collection(DONOR)
   // val donationsCollection = FirestoreDB.instance.collection(D)

    fun registerWithEmailAndPassword(user: User, onComplete: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId != null) {
                    when (user) {
                        is Hospital -> user.userId = userId
                        is Donor -> user.userId = userId
                        else -> onComplete(false, "Instance doesn't exist")
                    }
                    handleRegistrationSuccess(user, onComplete)
                } else {
                    onComplete(false, "User ID is null")
                }
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    private fun handleRegistrationSuccess(user: User, onComplete: (Boolean, String?) -> Unit) {
        createNewUser(user, onComplete)
    }

    fun registerWithGoogle(
        account: GoogleSignInAccount,
        userType: String = "",
        onComplete: (Boolean, String?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId != null) {
                    handleGoogleSignInSuccess(userType, userId, account, onComplete)
                } else {
                    onComplete(false, "User ID is null")
                }
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    private fun handleGoogleSignInSuccess(
        userType: String,
        userId: String,
        account: GoogleSignInAccount,
        onComplete: (Boolean, String?) -> Unit
    ) {
        checkIfUserExists(userId) { exists, type ->
            val userType2 = userType.ifEmpty { type }
            if (exists) {
                Log.d("response", "$type")
                onComplete(true, type)
            } else {
                val user = if (userType2 == HOSPITAL) {
                    Log.d("response", "hospital")
                    Hospital(
                        userId = userId,
                        email = account.email.toString(),
                        name = "${account.givenName} ${account.familyName}",
                        imageUrl = account.photoUrl.toString(),
                    )
                } else {
                    Log.d("response", "donor")
                    Donor(
                        userId = userId,
                        email = account.email.toString(),
                        firstname = "${account.givenName}",
                        lastname = "${account.familyName}",
                        imageUrl = account.photoUrl.toString(),
                    )
                }
                createNewUser(user = user) { success, exception ->
                    if (success) {
                        onComplete(true, userType2)
                    } else {
                        onComplete(false, exception)
                    }
                }
            }
        }
    }

    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId != null) {
                    checkIfUserExists(userId = userId) { exists, message ->
                        if (exists) {
                            onComplete(true, message)
                        } else {
                            onComplete(false, message)
                        }
                    }
                } else {
                    onComplete(false, "User ID is null")
                }
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    fun googleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

}