package com.legoueix.objectifcalories

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.legoueix.objectifcalories.analysis.MistralAnalyzer
import com.legoueix.objectifcalories.ui.theme.ObjectifCaloriesTheme

class MainActivity : ComponentActivity() {

    private val analyzer = MistralAnalyzer(BuildConfig.WORKER_URL, BuildConfig.APP_SHARED_SECRET)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ObjectifCaloriesTheme {
                ObjectifCaloriesApp(analyzer = analyzer)
            }
        }
    }
}
