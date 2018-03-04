package net.engin33r.neurotech

import android.util.Log
import com.google.gson.*
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import java.nio.charset.Charset

object DataProvider {
    enum class DataMode {
        ALL, HEALTHY, ABNORMAL, BOTH;

        override fun toString(): String {
            return when (this.ordinal) {
                1 -> "healthy"
                2 -> "abnormal"
                3 -> "both"
                else -> "all"
            }
        }
    }

    private const val BASE_URL = "http://nt.engi.io/api/"

    private val client = AsyncHttpClient()

    fun get(mode: DataMode, resolution: String, from: Long?, to: Long?,
            callback: (JsonElement) -> Unit) {
        val params = RequestParams()
        params.put("resolution", resolution)
        if (from != null) params.put("from", from)
        if (to != null) params.put("to", to)

        client.get(BASE_URL + "stats/" + mode.toString(), params,
                object: AsyncHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?,
                                           responseBody: ByteArray?) {
                        if (responseBody != null) {
                            val values = JsonParser().parse(responseBody
                                    .toString(Charset.defaultCharset()))
                            callback(values)
                        }
                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?,
                                           responseBody: ByteArray?, error: Throwable?) {
                        Log.w("DataProvider/get", "Request failed: " + statusCode)
                    }
                })
    }

    fun judge(mode: String, n: Int, callback: (JsonObject) -> Unit) {
        client.get(BASE_URL + "judge/" + mode + "/" + n, null,
                object: AsyncHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?,
                                           responseBody: ByteArray?) {
                        if (responseBody != null) {
                            callback(JsonParser().parse(responseBody
                                    .toString(Charset.defaultCharset())).asJsonObject)
                        }
                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?,
                                           responseBody: ByteArray?, error: Throwable?) {
                        Log.w("DataProvider/judge", "Request failed: " + statusCode)
                    }
                })
    }

    fun limits(mode: String, callback: (Float, Float) -> Unit) {
        client.get(BASE_URL + "limits/" + mode, null,
                object: AsyncHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Array<out Header>?,
                                           responseBody: ByteArray?) {
                        if (responseBody != null) {
                            val limits = JsonParser().parse(responseBody
                                    .toString(Charset.defaultCharset())).asJsonObject
                            callback(limits.get("woozy").asFloat, limits.get("sick").asFloat)
                        }
                    }

                    override fun onFailure(statusCode: Int, headers: Array<out Header>?,
                                           responseBody: ByteArray?, error: Throwable?) {
                        Log.w("DataProvider/judge", "Request failed: " + statusCode)
                    }
                })
    }
}
