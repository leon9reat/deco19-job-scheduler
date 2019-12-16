package com.medialink.deco19jobscheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.lang.Exception
import java.text.DecimalFormat

class GetCurrentWeatherJobService : JobService() {
    companion object {
        private const val TAG = "debug"
        internal const val CITY = "Pontianak"
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "on stop job")
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "on start job")
        getCurrentWeather(params)
        return true
    }

    private fun getCurrentWeather(job: JobParameters?) {
        Log.d(TAG, "get current weather: mulai...")
        val client = AsyncHttpClient()
        val url = "http://api.openweathermap.org/data/2.5/weather?q=$CITY&appid=${BuildConfig.API_KEY}"

        Log.d(TAG, "get current weather: $url")
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?
            ) {
                val result = String(responseBody ?: byteArrayOf())
                Log.d(TAG, result)
                try {
                    val responseObject = JSONObject(result)

                    val currentWeather = responseObject.getJSONArray("weather").getJSONObject(0).getString("main")
                    val description = responseObject.getJSONArray("weather").getJSONObject(0).getString("description")
                    val tempInKelvin = responseObject.getJSONObject("main").getDouble("temp")
                    val iconId = responseObject.getJSONArray("weather").getJSONObject(0).getString("icon")

                    val icon: Bitmap = Glide.with(applicationContext)
                        .asBitmap()
                        .load("${BuildConfig.PATH_IMAGE}${iconId}.png")
                        .submit()
                        .get()

                    val tempInCelcius = tempInKelvin - 273
                    val temperature = DecimalFormat("##.##").format(tempInCelcius)

                    val title = "Current Weather"
                    val message = "$currentWeather, $description with $temperature celcius"
                    val notifId = 100

                    showNotification(applicationContext, title, message, notifId, icon)

                    Log.d(TAG, "on success")
                    jobFinished(job, false)
                } catch (e: Exception) {
                    Log.d(TAG, "exception")
                    jobFinished(job, false)
                    e.printStackTrace()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {
                Log.d(TAG, "on failure")
                jobFinished(job, true)
            }

        })
    }

    private fun showNotification(context: Context, title: String, message: String, notifId: Int, icon: Bitmap) {
        val CHANNEL_ID = "Channel_1"
        val CHANNEL_NAME = "Job scheduler channel"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://openweathermap.org/find?q=${CITY}"))
        val pendingIntent = PendingIntent.getActivity(context, notifId, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_reply_black_24dp)
            .setLargeIcon(icon)
            .setContentText(message)
            .setColor(ContextCompat.getColor(context, android.R.color.black))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setSound(alarmSound)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            builder.setChannelId(CHANNEL_ID)
            notifManager.createNotificationChannel(channel)
        }

        val notification = builder.build()
        notifManager.notify(notifId, notification)
    }
}