package net.engin33r.neurotech

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.jakewharton.threetenabp.AndroidThreeTen

@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AndroidThreeTen.init(this.application)
    }

    fun openMonitoring(view: View) {
        val intent = Intent(this, MonitoringActivity::class.java)
        startActivity(intent)
    }

    fun openStatistics(view: View) {
        val intent = Intent(this, StatisticsActivity::class.java)
        startActivity(intent)
    }
}
