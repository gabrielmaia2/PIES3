package com.newhome.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.newhome.app.R
import com.newhome.app.dto.Animal

class AnimalAdapter(context: Context, var animais: List<Animal> = ArrayList()) :
    ArrayAdapter<Animal>(context, R.layout.fragment_animal_preview, animais) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.fragment_animal_preview, parent, false)

        val animal = animais[position]

        val nomeAnimalText: TextView = v.findViewById(R.id.nomeAnimalPreviewText)
        val detalhesAnimalText: TextView = v.findViewById(R.id.detalhesAnimalPreviewText)
        val animalImagem: ImageView = v.findViewById(R.id.animalImagem)

        nomeAnimalText.text = animal.name
        detalhesAnimalText.text = animal.details
        animalImagem.setImageBitmap(animal.image)

        return v
    }
}
