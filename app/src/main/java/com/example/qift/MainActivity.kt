package com.example.qift

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.qift.navigation.AppNavGraph
import com.example.qift.ui.theme.QiftTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize Firebase App Check (debug builds use debug tokens)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            firebaseAppCheck.getAppCheckToken(false)
                .addOnSuccessListener {
                    Log.d(
                        "AppCheck",
                        "Debug App Check initialized. If Firestore returns PERMISSION_DENIED, " +
                            "register the debug token from Logcat (search for 'Debug App Check token:') " +
                            "in the Firebase Console."
                    )
                }
                .addOnFailureListener { e ->
                    Log.w(
                        "AppCheck",
                        "App Check token fetch failed: ${e.message}. If Firestore is blocked, " +
                            "verify App Check setup in Firebase Console."
                    )
                }
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
        
        enableEdgeToEdge()
        setContent {
            QiftTheme {
                AppNavGraph()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    QiftTheme {
        Greeting("Android")
    }
}