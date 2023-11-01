package com.example.weatherapp

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices

class UpdateWeatherService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // 이 서비스가 실행됐을떄 호출되는 함수
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // foreground service로 만들어주기 위해서 아래 두 스텝을 거쳐야함.
        // (1) notification channel을 만들어줘야함
        createChannel()
        // (2) foreground service로 바꿔줘야함
        startForeground(1, createNotification())

        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(this)

        // 위치를 가져와서 위젯을 업데이트

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 만약 권한이 없다면
            // 위젯을 권한없음 상태로 표시하고, 클릭했을떄 권한 팝업을 얻을 수 있도록 조정
            val pendingIntent:PendingIntent = Intent(this, SettingActivity::class.java).let {
                PendingIntent.getActivity(this, 2, it, PendingIntent.FLAG_IMMUTABLE)
            }
            RemoteViews(packageName, R.layout.widget_weather).apply {
                setTextViewText(R.id.temperatureTextView,"권한없음")
                setTextViewText(R.id.weatherTextView,"")
                setOnClickPendingIntent(R.id.temperatureTextView, pendingIntent)
            }.also{ remoteViews ->
                val appWidgetName = ComponentName(this, WeatherAppWidgetProvider::class.java)
                appWidgetManager.updateAppWidget(appWidgetName,remoteViews) // 위 remote view 변경요청 사항을 등록
            }

            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }

        // 권한이 있는 상태
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
            WeatherRepository.getVillageForecast(
                longitude = it.longitude,
                latitude = it.latitude,
                successCallback = { forecastList ->
                    // 나중에 실행시킬 Intent 미리 정의
                    val pendingServiceIntent : PendingIntent = Intent(this, UpdateWeatherService::class.java)
                        .let {
                            PendingIntent.getService(this, 1, it, PendingIntent.FLAG_IMMUTABLE)
                        }

                    val currentForecast = forecastList.first()

                    // 위젯 UI 업데이트
                    RemoteViews(packageName, R.layout.widget_weather).apply {
                        setTextViewText(
                            R.id.temperatureTextView,
                            getString(R.string.temperature_text, currentForecast.temperature)
                        )
                        setTextViewText(
                            R.id.weatherTextView,
                            currentForecast.weather
                        )

                        setOnClickPendingIntent(R.id.temperatureTextView, pendingServiceIntent)
                        // 이제 위젯 이 부분 클릭하면 위젯 정보가 업데이트
                    }.also{ remoteViews ->
                        val appWidgetName = ComponentName(this, WeatherAppWidgetProvider::class.java)
                        appWidgetManager.updateAppWidget(appWidgetName,remoteViews) // 실제로 앱 위젯을 업데이트 하라고 명령
                    }

                    stopSelf() // 업데이트 다 완료되었으면 서비스 종료
                },
                failureCallback = {
                    // 위젯을 에러 상태로 표시

                    // 위젯 UI 업데이트
                    val pendingServiceIntent : PendingIntent = Intent(this, UpdateWeatherService::class.java)
                        .let {
                            PendingIntent.getService(this, 1, it, PendingIntent.FLAG_IMMUTABLE)
                        }
                    RemoteViews(packageName, R.layout.widget_weather).apply {
                        setTextViewText(
                            R.id.temperatureTextView,
                            "에러 발생"
                        )
                        setTextViewText(
                            R.id.weatherTextView,
                            ""
                        )
                        setOnClickPendingIntent(R.id.temperatureTextView, pendingServiceIntent)
                        // 이제 위젯 이 부분 클릭하면 위젯 정보가 업데이트
                    }.also{ remoteViews ->
                        val appWidgetName = ComponentName(this, WeatherAppWidgetProvider::class.java)
                        appWidgetManager.updateAppWidget(appWidgetName,remoteViews) // 실제로 앱 위젯을 업데이트 하라고 명령
                    }

                    stopSelf()
                }
            )
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun createChannel(){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL,
            "날씨앱",
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = "위젯을 업데이트하는 채널"

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("날씨앱")
            .setContentText("날씨 업데이트")
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()

        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    companion object {
        const val NOTIFICATION_CHANNEL = "widget_refresh_channel"
    }
}