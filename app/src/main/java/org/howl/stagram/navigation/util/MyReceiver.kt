package org.howl.stagram.navigation.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.howl.stagram.R

class MyReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "1000"
        const val NOTIFICATION_ID = 100
    }

    override fun onReceive(context: Context, intent: Intent) {

        // 채널 생성
        createNotificationChannel(context)
        // 알림
        notifyNotification(context)

        var str = intent.getStringExtra("send").toString()
        FcmPush.instance.sendMessage("wv4nOuLFCse8Gmoqpvt59ZJPcvo1","Howlstagram",str)

    }

    private fun createNotificationChannel(context: Context) {
        // context : 실행하고 있는 앱의 상태나 맥락을 담고 있음
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "기상 알람",
                NotificationManager.IMPORTANCE_HIGH
            )

            NotificationManagerCompat.from(context)
                .createNotificationChannel(notificationChannel)
        }
        Log.e("알림","성공")
    }

    private fun notifyNotification(context: Context) {
        with(NotificationManagerCompat.from(context)) {
            val build = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("공동구매")
                .setContentText("다음 공동구매가 3일 남았습니다. 참여자들에게 연락을 해주세요.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground)

            notify(NOTIFICATION_ID, build.build())
        }
    }
}