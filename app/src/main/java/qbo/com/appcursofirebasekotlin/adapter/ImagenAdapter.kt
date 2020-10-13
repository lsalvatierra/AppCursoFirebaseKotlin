package qbo.com.appcursofirebasekotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import qbo.com.appcursofirebasekotlin.R
import qbo.com.appcursofirebasekotlin.adapter.ImagenAdapter.*
import qbo.com.appcursofirebasekotlin.model.Imagen

class ImagenAdapter(private var lstimagenes:List<Imagen>, private val context: Context)
    : RecyclerView.Adapter<ViewHolder>() {

    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivimagen : ImageView = itemView.findViewById(R.id.ivimagen)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater
            .from(parent.context)
        return ViewHolder(layoutInflater.inflate(
            R.layout.image_item,
            parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lstimagenes[position]
        Glide.with(context).load(item.urlimagen).into(holder.ivimagen)
    }

    override fun getItemCount(): Int {
        return lstimagenes.size
    }
}