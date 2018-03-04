package net.engin33r.neurotech

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*


class StatisticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        fetchGraphData(findViewById(R.id.statsChart))
        fetchTableEvents()
    }

    private fun fetchGraphData(chart: LineChart) {
        fun callback(data: JsonElement) {
            val entriesHealthy = ArrayList<Entry>()
            val entriesAbnormal = ArrayList<Entry>()
            var i = 0f
            var total = 0
            data.asJsonArray.forEach { x -> run {
                var healthy = x.asJsonObject.get("healthy").asDouble
                var abnormal = x.asJsonObject.get("abnormal").asDouble
                if (healthy == 0.0) healthy = 0.01
                if (abnormal == 0.0) abnormal = 0.01

                val index = i++
                entriesHealthy.add(Entry(index, Math.log(healthy).toFloat(),
                        DataProvider.DataMode.HEALTHY))
                entriesAbnormal.add(Entry(index, Math.log(abnormal).toFloat(),
                        DataProvider.DataMode.ABNORMAL))
                total += abnormal.toInt()
            } }
            Log.d("StatisticsActivity", "total x values: $i")

            val dataSets = ArrayList<ILineDataSet>()
            dataSets.add(LineDataSet(entriesHealthy, "Healthy"))
            dataSets.add(LineDataSet(entriesAbnormal, "Abnormal"))
            val lineData = ChartStyler.styleStatisticsData(dataSets, this.applicationContext, chart)

            chart.data = lineData
            chart.setVisibleYRangeMinimum(1f, chart.axisLeft.axisDependency)
            chart.setVisibleYRangeMinimum(1f, chart.axisRight.axisDependency)
            //chart.setOnChartValueSelectedListener(MonitoringActivity.HighlightListener(DataProvider.DataMode.HEALTHY))

            DataProvider.judge("statistics/graph", total, { obj: JsonObject -> run {
                val statusText = findViewById<TextView>(R.id.statsChartStatus)
                val res = this@StatisticsActivity.resources
                val context = this@StatisticsActivity.applicationContext
                when {
                    obj.get("sick").asBoolean -> {
                        statusText.text = res.getString(R.string.status_sick)
                        statusText.setTextColor(ContextCompat.getColor(
                                context, R.color.statusSick))
                    }
                    obj.get("woozy").asBoolean -> {
                        statusText.text = res.getString(R.string.status_woozy)
                        statusText.setTextColor(ContextCompat.getColor(
                                context, R.color.statusWoozy))
                    }
                    else -> {
                        statusText.text = res.getString(R.string.status_healthy)
                        statusText.setTextColor(ContextCompat.getColor(
                                context, R.color.statusHealthy))
                    }
                }
                chart.invalidate()
            } })
        }

        DataProvider.get(DataProvider.DataMode.BOTH, "1 hour",
                Instant.now().minus(Duration.ofHours(49)).epochSecond,
                Instant.now().minus(Duration.ofHours(1)).epochSecond, ::callback)
    }

    private fun fetchTableEvents() {
        fun callback(data: JsonElement) {
            val table = findViewById<TableLayout>(R.id.statsTable)
            val nodata = findViewById<TableRow>(R.id.statsNodata)
            DataProvider.limits("statistics/table", { woozy, sick -> run {
                data.asJsonArray.reversed().forEach { x -> run {
                    val healthy = x.asJsonObject.get("healthy").asFloat
                    val abnormal = x.asJsonObject.get("abnormal").asFloat
                    val ratio = abnormal / (healthy + abnormal)
                    if (ratio > woozy) {
                        if (nodata.visibility != View.GONE) nodata.visibility = View.GONE
                        val row = layoutInflater.inflate(R.layout.table_entry, null)
                        val color = when {
                            ratio >= sick -> ContextCompat.getColor(this.applicationContext,
                                    R.color.tableSick)
                            else -> ContextCompat.getColor(this.applicationContext,
                                    R.color.tableWoozy)
                        }
                        row.setBackgroundColor(color)
                        row.findViewById<TextView>(R.id.time).text =
                                Instant.parse(x.asJsonObject.get("time").asString)
                                        .atZone(ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ofPattern(resources.getString(
                                                R.string.statistics_table_dateformat)))
                        row.findViewById<TextView>(R.id.details).text =
                                resources.getQuantityString(R.plurals.statistics_table_entry,
                                        abnormal.toInt(), abnormal.toInt(), ratio * 100)
                        table.addView(row)
                    }
                } }
            } })
        }

        DataProvider.get(DataProvider.DataMode.BOTH, "1 hour",
                Instant.now().minus(Duration.ofHours(49)).epochSecond, null, ::callback)
    }
}
