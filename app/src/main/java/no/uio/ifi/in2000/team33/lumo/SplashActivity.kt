package no.uio.ifi.in2000.team33.lumo

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Start the rotation animation using AnimatedVectorDrawable
        val sunImage = findViewById<ImageView>(R.id.sun_image)
        val drawable = sunImage.drawable
        if (drawable is AnimatedVectorDrawable) {
            drawable.start()
        }

        // Navigate to main activity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000) // 3 seconds delay
    }
}

