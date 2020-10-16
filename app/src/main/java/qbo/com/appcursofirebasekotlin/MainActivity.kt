package qbo.com.appcursofirebasekotlin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import java.math.BigInteger
import java.security.SecureRandom


class MainActivity : AppCompatActivity() {

    //3. Autenticación con Facebook
    private val callbackManager = CallbackManager.Factory.create()
    //4. Autenticación con GitHub
    private val random: SecureRandom = SecureRandom()
    //4. Colocar este valor en el Callback de GIT y agregar en el filtro del activity en el Manifest.xml
    private val URL_CALLBACK = "qbodev://git.oauth2token"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //1. Validar preferencias de la aplicación
        validarPreferencia()
        //1. Autenticación con email y password con Firebase
        pbautenticacion.visibility = View.GONE
        btnlogin.setOnClickListener { vista ->
            if (etemail.text?.isNotEmpty()!! && etpassword.text?.isNotEmpty()!!){
                pbautenticacion.visibility = View.VISIBLE
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    etemail.text.toString(), etpassword.text.toString()
                ).addOnCompleteListener {
                    if(it.isSuccessful){
                        guardarPreferenciaIrAlApp(
                            it.result?.user?.email ?: "",
                            TipoAutenticacion.FIREBASE.name,
                            "", ""
                        )
                    }else{
                        enviarMensaje(vista, "Error en la autenticación Firebase")
                    }
                }
            }
        }
        //2. Autenticación con Gmail
        btnlogingoogle.setOnClickListener {
            pbautenticacion.visibility = View.VISIBLE
            val configlogin = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val cliente : GoogleSignInClient = GoogleSignIn.getClient(
                this, configlogin
            )
            startActivityForResult(cliente.signInIntent, 777)
        }
        //3. Autenticación con Facebook
        btnloginfacebook.setOnClickListener {
            pbautenticacion.visibility = View.VISIBLE
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("email")
            )
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult?) {
                        result?.let {
                            val token = it.accessToken
                            val credencial: AuthCredential = FacebookAuthProvider
                                .getCredential(token.token)
                            FirebaseAuth.getInstance().signInWithCredential(
                                credencial
                            ).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    guardarPreferenciaIrAlApp(
                                        it.result?.user?.email.toString(),
                                        TipoAutenticacion.FACEBOOK.name,
                                        it.result?.user?.displayName.toString(),
                                        it.result?.user?.photoUrl.toString()
                                    )
                                } else {
                                    enviarMensaje(
                                        obtenerVista(),
                                        "Error en la autenticación FACEBOOK."
                                    )
                                }
                            }
                        }
                    }
                    override fun onCancel() {
                        enviarMensaje(obtenerVista(), "Canceló en la autenticación FACEBOOK.")
                    }

                    override fun onError(error: FacebookException?) {
                        enviarMensaje(obtenerVista(), "Error en la autenticación FACEBOOK.")
                    }
                })
        }

        //4. Autenticación con Github
        btnlogingithub.setOnClickListener {
            pbautenticacion.visibility = View.VISIBLE
            val httpUrl = HttpUrl.Builder()
                .scheme("https")
                .host("github.com")
                .addPathSegment("login")
                .addPathSegment("oauth")
                .addPathSegment("authorize")
                .addQueryParameter("client_id", "f02b77a967fe014c75fb")
                .addQueryParameter("redirect_uri", URL_CALLBACK)
                .addQueryParameter("state", getRandomString())
                .addQueryParameter("scope", "user:email")
                .build()
            //Approach 1
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(httpUrl.toString()))
            startActivity(intent)
        }
        //4. Autenticación con Github
        val uri = intent.data
        if (uri != null && uri.toString().startsWith(URL_CALLBACK)) {
            val code = uri.getQueryParameter("code")
            val state = uri.getQueryParameter("state")
            if (code != null && state != null)
                obtenerCredencial(code, state)
        }
        obtenerTokenDispositivo()
    }

    private fun obtenerTokenDispositivo(){
        //1.Notificaciones por Token
        FirebaseInstanceId.getInstance()
            .instanceId.addOnCompleteListener {
                it.result?.token?.let { token->
                    Log.i("TOKENFIRE", "El token del disposito es $token")
                }
            }
        //2.Notificaciones por Tema
        FirebaseMessaging.getInstance().subscribeToTopic("qboinstitute")
   
    }

    //4. Autenticación con Github
    private fun obtenerCredencial(code: String, state: String) {
        //POST https://github.com/login/oauth/access_token
        val okHttpClient = OkHttpClient()
        val form = FormBody.Builder()
            .add("client_id", "f02b77a967fe014c75fb")
            .add("client_secret", "b932942d3601382da14ec68508a099b8e292fce7")
            .add("code", code)
            .add("redirect_uri", URL_CALLBACK)
            .add("state", state)
            .build()
        val request = Request.Builder()
            .url("https://github.com/login/oauth/access_token")
            .post(form)
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                enviarMensaje(obtenerVista(), "Error en la autenticación GITHUB.")
            }
            override fun onResponse(call: Call, response: Response) {
                val responseBody: String = response.body!!.string()
                Log.i("TAGRPTA1", responseBody)
                val splitted = responseBody.split("=|&".toRegex()).toTypedArray()
                if (splitted[0].equals("access_token", ignoreCase = true)) {
                    loginGitHubConToken(splitted[1])
                } else {
                    enviarMensaje(obtenerVista(), "Canceló en la autenticación GITHUB.")
                }
            }

        })
    }
    //4. Autenticación con Github
    fun loginGitHubConToken(token: String) {
        //Habilitar la autenticación con una misma cuenta para varios proveedores en Firebase
        val credential = GithubAuthProvider.getCredential(token)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    guardarPreferenciaIrAlApp(
                        it.result?.additionalUserInfo?.username.toString(),
                        TipoAutenticacion.GITHUB.name,
                        it.result?.user?.displayName.toString(),
                        it.result?.user?.photoUrl.toString()
                    )
                }
            }
    }

    //4. Autenticación con Github
    private fun getRandomString(): String {
        return BigInteger(130, random).toString(32)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //3.
        callbackManager.onActivityResult(requestCode, resultCode, data)
        //2.
        if(requestCode == 777){
            val task : Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val cuenta : GoogleSignInAccount? = task.getResult(ApiException::class.java)
                if(cuenta != null){
                    val credencial : AuthCredential = GoogleAuthProvider
                        .getCredential(cuenta.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(
                        credencial
                    ).addOnCompleteListener {
                        if(it.isSuccessful){
                            guardarPreferenciaIrAlApp(
                                cuenta.email.toString(),
                                TipoAutenticacion.GOOGLE.name,
                                cuenta.displayName.toString(),
                                cuenta.photoUrl.toString()
                            )
                        }else{
                            enviarMensaje(obtenerVista(), "Error en la autenticación GMAIL.")
                        }
                    }
                }
            }catch (e: ApiException){
                enviarMensaje(obtenerVista(), "Error en la autenticación GMAIL.")
            }
        }
    }

    private fun obtenerVista(): View{
        return findViewById(android.R.id.content)
    }

    //1. Enviar mensajes.
    private fun enviarMensaje(vista: View, mensaje: String){
        pbautenticacion.visibility = View.GONE
        Snackbar.make(vista, mensaje, Snackbar.LENGTH_LONG).show()
    }
    //1. Validar ingreso a la aplicación.
    private fun guardarPreferenciaIrAlApp(
        email: String, tipo: String,
        nombre: String, urlimagen: String
    ){
        val preferencia : SharedPreferences.Editor = getSharedPreferences(
            "appFireabaseQBO",
            Context.MODE_PRIVATE
        ).edit()
        preferencia.putString("email", email)
        preferencia.putString("tipo", tipo)
        preferencia.putString("nombre", nombre)
        preferencia.putString("urlimg", urlimagen)
        preferencia.apply()
        pbautenticacion.visibility = View.GONE
        startActivity(Intent(this, HomeActivity::class.java))
    }
    //1. Validar si existe preferencias guardadas.
    private fun validarPreferencia(){
        val preferencia : SharedPreferences = getSharedPreferences(
            "appFireabaseQBO",
            Context.MODE_PRIVATE
        )
        val email: String? = preferencia.getString("email", null)
        val tipo: String? = preferencia.getString("tipo", null)
        val nombre: String? = preferencia.getString("nombre", null)
        val urlimagen: String? = preferencia.getString("urlimg", null)
        if(email != null && tipo != null && nombre != null && urlimagen != null) {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}