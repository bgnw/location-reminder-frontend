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
        ) {
            val builder = NotificationCompat.Builder(context, R.string.channel_id.toString())
                .setSmallIcon(R.drawable.baseline_info_24).setContentTitle(title)
                .setContentText(body).setPriority(NotificationCompat.PRIORITY_HIGH)

            with(NotificationManagerCompat.from(context)) {
                if (ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions

                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                if (notifID + 1 < Int.MAX_VALUE) { ++notifID }
                else { notifID = 1 }
                notify(notifID, builder.build())
            }
        }
    }

}