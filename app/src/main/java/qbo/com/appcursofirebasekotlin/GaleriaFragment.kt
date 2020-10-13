package qbo.com.appcursofirebasekotlin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import qbo.com.appcursofirebasekotlin.adapter.ImagenAdapter
import qbo.com.appcursofirebasekotlin.model.Imagen


class GaleriaFragment : Fragment() {

    lateinit var rvimagenes : RecyclerView


    //Cambiar la regla de acceso a Storage
    //allow read, write: if request.auth != null;
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_galeria, container, false)
            rvimagenes = view.findViewById(R.id.rvimagenes)
        ListarImagenes(view)
        return view
    }

    fun ListarImagenes(view: View){
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("/imagenesqbo/")
        val lstimagenes : ArrayList<Imagen> = ArrayList()
        val listAllTask : Task<ListResult> = storageRef.listAll()
        listAllTask.addOnCompleteListener { result ->
            val items: List<StorageReference> = result.result!!.items
            //add cycle for add image url to list
            items.forEachIndexed { index, item ->
                item.downloadUrl.addOnSuccessListener {
                    lstimagenes.add(Imagen( it.toString()))
                }.addOnCompleteListener {
                    rvimagenes.adapter = ImagenAdapter(lstimagenes, view.context)
                    rvimagenes.layoutManager = GridLayoutManager(view.context, 3)
                }
            }
        }
    }

}