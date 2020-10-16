package qbo.com.appcursofirebasekotlin

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.remoteMessage

class MiServicioMensajeFirebase : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        //3.Notificaciones en primer plano.
        Looper.prepare()
        Handler().post{
            Toast.makeText(baseContext,
            p0.notification?.title+" - "+p0.notification?.body,
            Toast.LENGTH_LONG).show()
        }
        Looper.loop()
    }
}