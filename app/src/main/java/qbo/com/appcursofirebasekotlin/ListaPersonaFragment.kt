package qbo.com.appcursofirebasekotlin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import qbo.com.appcursofirebasekotlin.adapter.PersonaAdapter
import qbo.com.appcursofirebasekotlin.model.Persona


class ListaPersonaFragment : Fragment() {

    lateinit var rvfirestore : RecyclerView
    lateinit var firestoredb: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_lista_persona, container, false)
        val lstpersonas : ArrayList<Persona> = ArrayList()
        rvfirestore = view.findViewById(R.id.rvfirestore)
        firestoredb = FirebaseFirestore.getInstance()
        firestoredb.collection("Persona")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()
                }
                for (dc in snapshots!!.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        lstpersonas.add(Persona(dc.document.data["nombre"].toString(),
                            dc.document.data["apellido"].toString(),
                            dc.document.data["edad"].toString().toInt()))
                        //Log.d("FIRE", "New city: ${dc.document.data["apellido"]}")
                    }

                }
                rvfirestore.adapter = PersonaAdapter(lstpersonas)
                rvfirestore.layoutManager = LinearLayoutManager(view.context)
            }
        return view
    }


}