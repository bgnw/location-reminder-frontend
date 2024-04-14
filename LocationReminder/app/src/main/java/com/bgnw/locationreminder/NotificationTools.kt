package com.bgnw.locationreminder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationTools {

    companion object {
        private var notifID = 1
        fun showNotification(
            context: Context,
            title: String,
            body: String,
            longText: Boolean = false
        ) {
            val builder = NotificationCompat.Builder(context, R.string.channel_id.toString())
                .setSmallIcon(R.drawable.baseline_info_24)
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            if (longText) {
                builder.setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(body)
                )
            } else {
                builder.setContentText(body)
            }


            with(NotificationManagerCompat.from(context)) {
                if (ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                if (notifID + 1 < Int.MAX_VALUE) {
                    ++notifID
                } else {
                    notifID = 1
                }
                notify(notifID, builder.build())
            }
        }
    }

}