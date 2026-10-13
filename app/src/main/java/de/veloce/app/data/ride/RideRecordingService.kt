package de.veloce.app.data.ride

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import de.veloce.app.MainActivity
import de.veloce.app.R

/**
 * Foreground-Service, der während einer laufenden Fahrt den Prozess am Leben
 * hält, damit die Aufzeichnung auch bei ausgeschaltetem Bildschirm bzw. im
 * Hintergrund weiterläuft.
 *
 * Die eigentliche Aufzeichnung liegt im [RideRecordingController]; dieser
 * Service hält lediglich die Benachrichtigung offen und einen Partial-WakeLock,
 * damit die CPU nicht einschlafen kann.
 */
class RideRecordingService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        // NOT_STICKY: Nach einem Prozess-Neustart gibt es keinen gültigen
        // rideId-Kontext mehr, deshalb kein Zombie-Service ohne Aufzeichnung.
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Veloce:RideRecording")
            .apply {
                // Safety-Limit, damit der Lock nicht für immer hängen bleibt.
                acquire(MAX_WAKE_LOCK_MS)
            }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fahrt-Aufzeichnung",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Hält die Fahrt-Aufzeichnung im Hintergrund aktiv."
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_veloce)
            .setContentTitle("Fahrt wird aufgezeichnet")
            .setContentText("Messwerte werden live an den Server übertragen.")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private companion object {
        const val CHANNEL_ID = "veloce_recording"
        const val NOTIFICATION_ID = 4711
        const val MAX_WAKE_LOCK_MS = 10 * 60 * 60 * 1000L // 10 h Sicherheitslimit
    }
}
