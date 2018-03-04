package net.engin33r.neurotech

import android.animation.ObjectAnimator
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isTaskRoot) {
            Log.d("SplashActivity", "brought to front")
            finish()
            return
        }

        setContentView(R.layout.activity_splash)

        findViewById<TextView>(R.id.progressText).setText("0%") // property access results in linting error

        val progressBar = findViewById<ProgressBar>(R.id.progress)
        progressBar.progress = 0

        val t = object : Thread() {
            private var running = true

            private fun end() {
                running = false
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                overridePendingTransition(0, 0)
            }

            override fun run() {
                Thread.sleep(500)
                runOnUiThread {
                    val animation = ObjectAnimator.ofInt(progressBar, "progress", 100)
                    animation.duration = 1000
                    animation.interpolator = DecelerateInterpolator()
                    animation.start()
                }
                while (running) {
                    try {
                        Thread.sleep(50)
                        runOnUiThread {
                            val bar = findViewById<ProgressBar>(R.id.progress)
                            val progress = findViewById<TextView>(R.id.progressText)
                            progress.text = resources.getString(R.string.splash_percent)
                                    .format(Math.round(bar.progress.toFloat()))
                            if (bar.progress == 100) {
                                end()
                            }
                        }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                }
            }
        }
        t.start()
    }
}
