package com.example.weatherapp

import android.app.ForegroundServiceStartNotAllowedException
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.content.ContextCompat

class WeatherAppWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        appWidgetIds.forEach { appWidgetId ->

            // 나중에 실행되는 Intent
            val pendingIntent: PendingIntent = Intent(context, UpdateWeatherService::class.java)
                .let { intent ->
                    PendingIntent.getForegroundService(
                        context,
                        1,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                }

            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.widget_weather
            ).apply {
                setOnClickPendingIntent(R.id.temperatureTextView, pendingIntent)
                // testTextView 를 클릭할떄 pendingIntent가 실행이 되도록
            }
            appWidgetManager.updateAppWidget(appWidgetId, views) // 위젯에다가 위 내용을 등록하게됨
        }

        // foreground service start
        val serviceIntent = Intent(context, UpdateWeatherService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S /*31*/){ // sdk 31버전부터 foreground service를 실행하는데 제약이 걸림
            try{
                ContextCompat.startForegroundService(context, serviceIntent)
            } catch(e:ForegroundServiceStartNotAllowedException){
                e.printStackTrace()
            }
        } else {
            ContextCompat.startForegroundService(context, serviceIntent)
        }


    }
}