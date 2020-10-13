package qbo.com.appcursofirebasekotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class FotoFragment : Fragment() {

    /*
    1. Crear el paquete xml
    2. Crear un archivo file_paths.xml y agregar la ubicación
    3. Agregar el provider al manifest, con el nombre del pequete del Py.
     */

    private val CAMERA_REQUEST = 1888
    var mRutaFotoActual = ""
    private lateinit var ivfoto: ImageView
    private lateinit var btntomarfoto: Button
    private lateinit var btnsubirfoto:Button
    private lateinit var storage: FirebaseStorage


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //4. Definir los métodos.
        val vista : View = inflater.inflate(R.layout.fragment_foto, container, false)
        ivfoto = vista.findViewById(R.id.ivfoto)
        btntomarfoto = vista.findViewById(R.id.btntomarfoto)
        btnsubirfoto = vista.findViewById(R.id.btnsubirfoto)
        val storage = FirebaseStorage.getInstance()

        btntomarfoto.setOnClickListener {
            if (PermisoEscrituraAlmacenamiento()) {
                try {
                    IntencionTomarFoto()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else requestStoragePermission()
        }
        btnsubirfoto.setOnClickListener { vista->
            var file = Uri.fromFile(File(mRutaFotoActual))
            val imgRef = storage.reference.child("imagenesqbo/${file.lastPathSegment}")
            var uploadTask = imgRef.putFile(file)
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
                Snackbar.make(vista, "Error", Snackbar.LENGTH_LONG).show()
            }.addOnSuccessListener {
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                // ...
                Snackbar.make(vista, "Imagen en Firebase Storage", Snackbar.LENGTH_LONG).show()
            }
        }
        return vista
    }

    //5. Preguntar el permiso de escritura
    private fun PermisoEscrituraAlmacenamiento(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        var exito = false
        if (result == PackageManager.PERMISSION_GRANTED) exito = true
        return exito
    }
    //6. Solicitar el permiso de escritura.
    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
        )
    }

    //7. Recibir la respuesta del permiso.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode==0){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(context, "Permiso ACEPTADO", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context, "Permiso DENEGADO", Toast.LENGTH_SHORT).show();
        }
    }
    //8. Crear el método donde guardar la imagen
    @Throws(IOException::class)
    private fun crearArchivoImagen(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val image: File = File.createTempFile(imageFileName, ".jpg", storageDir)
        mRutaFotoActual = image.absolutePath
        return image
    }

    //9. Llamamos a la cámara a traves del Intent.
    @Throws(IOException::class)
    private fun IntencionTomarFoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity?.packageManager!!) != null) {
            val photoFile = crearArchivoImagen()
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "qbo.com.appcursofirebasekotlin.provider", photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, CAMERA_REQUEST)
            }
        }
    }
    //10. Llamamos a la cámara a traves del Intent.
    private fun mostrarFoto() {
        val ei = ExifInterface(mRutaFotoActual)
        val orientation: Int = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        if(orientation == ExifInterface.ORIENTATION_ROTATE_90){
            ivfoto.rotation = 90.0F
        }else{
            ivfoto.rotation = 0.0F
        }
        val targetW: Int = ivfoto.width
        val targetH: Int = ivfoto.height
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(mRutaFotoActual, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight
        val scaleFactor = Math.min(photoW / targetW, photoH / targetH)
        bmOptions.inSampleSize = scaleFactor
        bmOptions.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(mRutaFotoActual, bmOptions)
        ivfoto.setImageBitmap(bitmap)

    }
    //11. Grabar Foto en la galeria del dispositivo.
    private fun grabarFotoGaleria() {
        val mediaScanIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val nuevoarchivo = File(mRutaFotoActual)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            val contentUri = FileProvider.getUriForFile(
                requireContext(),
            "qbo.com.appcursofirebasekotlin.provider",nuevoarchivo)
            mediaScanIntent.data = contentUri
        }else{
            val contentUri = Uri.fromFile(nuevoarchivo)
            mediaScanIntent.data = contentUri
        }
        activity?.sendBroadcast(mediaScanIntent)
    }

    //12. Llama el resultado del Intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        grabarFotoGaleria();
        mostrarFoto();
    }

}