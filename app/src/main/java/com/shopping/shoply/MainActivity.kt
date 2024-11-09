package com.shopping.shoply

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.lifecycle.lifecycleScope
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
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
                ShoplyAppContent()
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
