package net.engin33r.neurotech

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.TextView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.google.gson.JsonObject
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.gson.JsonElement
import io.engi.android.views.StatusTimeline
import org.threeten.bp.Duration
import org.threeten.bp.Instant

class MonitoringActivity : AppCompatActivity() {

    class HighlightListener(mode: DataProvider.DataMode) : OnChartValueSelectedListener {
        private var mMode : DataProvider.DataMode = mode

        companion object {
            val mChartHighlight = Array(2, { _ -> -1f })

            fun getFor(mode: DataProvider.DataMode) : Float {
                return mChartHighlight[getI(mode)]
            }

            private fun getI(mode: DataProvider.DataMode) : Int {
                return when (mode) {
                    DataProvider.DataMode.ALL -> 1
                    else -> 0
                }
            }
        }

        override fun onNothingSelected() {
            mChartHighlight[getI(mMode)] = -1f
        }

        override fun onValueSelected(e: Entry?, h: Highlight?) {
            mChartHighlight[getI(mMode)] = h!!.x
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monitoring)
        val chartDanger = findViewById<LineChart>(R.id.chartDanger)
        val chartTotal = findViewById<LineChart>(R.id.chartTotal)
        fetchChartData(DataProvider.DataMode.ABNORMAL, chartDanger)
        fetchChartData(DataProvider.DataMode.ALL, chartTotal)

        val timeline = findViewById<StatusTimeline>(R.id.timeline)
        fetchTimelineData(timeline)
    }

    private fun fetchChartData(mode: DataProvider.DataMode, chart: LineChart) {
        fun callback(data: JsonElement) {
            val entries = ArrayList<Entry>()
            var i = 0f
            var total = 0
            data.asJsonArray.forEach { x -> run {
                val packets = x.asJsonObject.get("packets").asFloat
                entries.add(Entry(i++, packets, mode))
                total += packets.toInt()
            } }

            val res = this@MonitoringActivity.resources
            val context = this@MonitoringActivity.applicationContext

            chart.setOnChartValueSelectedListener(MonitoringActivity.HighlightListener(mode))

            if (mode == DataProvider.DataMode.ABNORMAL) {
                DataProvider.judge("monitor/graph", total, { obj: JsonObject -> run {
                    val statusText = findViewById<TextView>(R.id.chartDangerStatus)
                    val color = when {
                        obj.get("sick").asBoolean -> {
                            val c = ContextCompat.getColor(context, R.color.statusSick)
                            statusText.text = res.getString(R.string.status_sick)
                            statusText.setTextColor(c)
                            c
                        }
                        obj.get("woozy").asBoolean -> {
                            val c = ContextCompat.getColor(context, R.color.statusWoozy)
                            statusText.text = res.getString(R.string.status_woozy)
                            statusText.setTextColor(c)
                            c
                        }
                        else -> {
                            val c = ContextCompat.getColor(context, R.color.statusHealthy)
                            statusText.text = res.getString(R.string.status_healthy)
                            statusText.setTextColor(c)
                            c
                        }
                    }

                    val dataSet = LineDataSet(entries, "Abnormal")
                    val lineData = ChartStyler.styleMonitorData(dataSet, color, chart)
                    chart.data = lineData
                    chart.invalidate()
                } })
            } else {
                val dataSet = LineDataSet(entries, "Total")
                val lineData = ChartStyler.styleMonitorData(dataSet,
                        ContextCompat.getColor(context, R.color.statusHealthy), chart)
                chart.data = lineData
                chart.invalidate()
            }
        }

        DataProvider.get(mode, "1 minute",
                Instant.now().minus(Duration.ofHours(1)).epochSecond, null, ::callback)
    }

    private fun fetchTimelineData(timeline: StatusTimeline) {
        fun callback(data: JsonElement) {
            val colorSick = ContextCompat.getColor(this.applicationContext, R.color.statusSick)
            val colorWoozy = ContextCompat.getColor(this.applicationContext, R.color.statusWoozy)
            val colorHealthy = ContextCompat.getColor(this.applicationContext, R.color.statusHealthy)

            DataProvider.limits("monitor/timeline", { woozy, sick -> run {
                var i = 0
                data.asJsonArray.forEach { x -> run {
                    if (i < 7) {
                        val pktHealthy = x.asJsonObject.get("healthy").asFloat
                        val pktAbnormal = x.asJsonObject.get("abnormal").asFloat
                        val ratio: Float
                        ratio = if (pktAbnormal == 0f) {
                            0f
                        } else {
                            pktAbnormal / (pktHealthy + pktAbnormal)
                        }

                        val index = i++
                        when {
                            ratio > sick -> timeline.setUnitColor(index, colorSick)
                            ratio > woozy -> timeline.setUnitColor(index, colorWoozy)
                            else -> timeline.setUnitColor(index, colorHealthy)
                        }
                    }
                } }
            } })
        }

        DataProvider.get(DataProvider.DataMode.BOTH, "1 day",
                Instant.now().minus(Duration.ofDays(7)).epochSecond,
                Instant.now().minus(Duration.ofDays(1)).epochSecond, ::callback)
    }
}
