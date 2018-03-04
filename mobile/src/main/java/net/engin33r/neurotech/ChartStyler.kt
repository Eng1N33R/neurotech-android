package net.engin33r.neurotech

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.Log
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import kotlin.math.roundToInt

object ChartStyler {
    fun styleMonitorData(dataSet: LineDataSet, color: Int, chart: LineChart) : LineData {
        dataSet.color = color
        dataSet.fillColor = color
        dataSet.fillAlpha = 0xC0
        dataSet.setDrawFilled(true)
        dataSet.setDrawCircles(false)
        dataSet.isHighlightEnabled = true
        dataSet.setDrawHighlightIndicators(true)
        dataSet.valueTextSize = 14f

        val lineData = LineData(dataSet)
        lineData.setValueFormatter {
            value, entry, _, _ -> run {
                val hl = MonitoringActivity.HighlightListener.getFor(entry.data
                        as DataProvider.DataMode)
                if (entry.x == hl) {
                    value.toInt().toString()
                } else {
                    ""
                }
            }
        }

        chart.description.text = ""
        chart.legend.isEnabled = false

        chart.xAxis.setValueFormatter { value, _ -> run {
            val startInstant = Instant.ofEpochSecond(System.currentTimeMillis() / 1000 - 3600)
            startInstant.plus(Duration.ofSeconds((value * 60).toLong()))
                    .atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))
        } }

        return lineData
    }

    fun styleStatisticsData(dataSets: ArrayList<ILineDataSet>, context: Context, chart: LineChart)
            : LineData {
        val dataSetHealthy = dataSets[0]
        val dataSetAbnormal = dataSets[1]

        fun commonStyling(dataSet: LineDataSet, color: Int) {
            dataSet.color = color
            dataSet.fillColor = color
            dataSet.fillAlpha = 0xC0
            dataSet.setDrawFilled(true)
            dataSet.setDrawCircles(false)
            dataSet.isHighlightEnabled = true
            dataSet.setDrawHighlightIndicators(true)
            dataSet.valueTextSize = 14f
            dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        commonStyling(dataSetHealthy as LineDataSet,
                ContextCompat.getColor(context, R.color.statusHealthy))
        commonStyling(dataSetAbnormal as LineDataSet,
                ContextCompat.getColor(context, R.color.statusSick))

        val lineData = LineData(dataSets)
        lineData.setValueFormatter {
            value, entry, _, _ -> run {
            val hl = MonitoringActivity.HighlightListener.getFor(entry.data
                    as DataProvider.DataMode)
            if (entry.x == hl) {
                value.toInt().toString()
            } else {
                ""
            }
        }
        }

        chart.description.text = ""
        chart.legend.isEnabled = false

        chart.xAxis.setValueFormatter { value, _ -> run {
            val startInstant = Instant.now().minus(Duration.ofHours(49))
            startInstant.plus(Duration.ofMinutes((value * 60).toLong()))
                    .atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))
        } }
        val yFormatter = IAxisValueFormatter { value, _ ->
            "%d".format(Math.pow(Math.E, value.toDouble()).roundToInt())
        }
        chart.axisLeft.valueFormatter = yFormatter
        chart.axisRight.valueFormatter = yFormatter

        return lineData
    }
}