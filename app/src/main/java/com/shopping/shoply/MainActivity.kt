package com.shopping.shoply

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.lifecycleScope
import com.shopping.shoply.common.presentation.ui.theme.ShoplyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        var keepSplashScreenVisible = true
        lifecycleScope.launch {
            delay(1200L)
            keepSplashScreenVisible = false
        }

        installSplashScreen().apply {
            setKeepOnScreenCondition { keepSplashScreenVisible }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                setOnExitAnimationListener { splashScreenView ->
                    setSplashAnimation(splashScreenView)
                }
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ShoplyTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {

                }
            }
        }
    }

    private fun setSplashAnimation(splashScreenView: SplashScreenViewProvider) {
        val scaleX = ObjectAnimator.ofFloat(
            splashScreenView.iconView,
            View.SCALE_X,
            1.0f,
            0.0f
        )
        scaleX.interpolator = AccelerateInterpolator()
        scaleX.duration = 500L

        val scaleY = ObjectAnimator.ofFloat(
            splashScreenView.iconView,
            View.SCALE_Y,
            1.0f,
            0.0f
        )
        scaleY.interpolator = AccelerateInterpolator()
        scaleY.duration = 500L

        scaleX.start()
        scaleY.start()

        scaleX.doOnEnd { splashScreenView.remove() }
        scaleY.doOnEnd { splashScreenView.remove() }
    }

}
