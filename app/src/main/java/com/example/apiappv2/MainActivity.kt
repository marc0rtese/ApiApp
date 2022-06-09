package com.example.apiappv2

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class MainActivity : AppCompatActivity() {
    //Инициализация и привязка элементов Activity
    private lateinit var imgQr: ImageView
    private lateinit var ButGenerate: Button
    private lateinit var href: String
    private lateinit var editText: EditText
    private lateinit var ButSave: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArrayAdapter<String>
    val urls: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Initialize()
        ButGenerate.setOnClickListener(ButGenerateListener) //Объявление слушателя кнопки
        ButSave.setOnClickListener(ButSaveListener)



        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = RecyclerAdapter(urls)


    }

    private var ButGenerateListener: View.OnClickListener = View.OnClickListener { //Обработка нажатия на кнопку
        href = editText.text.toString()
        try {
            urls.add(editText.text.toString())
            recyclerView.adapter!!.notifyDataSetChanged();
        } catch (e: Exception){

        }

        getQr()
    }



    private var ButSaveListener: View.OnClickListener = View.OnClickListener {
        var fOut: OutputStream? = null
        var bitmap: Bitmap = imgQr.drawable.toBitmap()
        var context = applicationContext
        val filename = "${System.currentTimeMillis()}.jpg"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context?.contentResolver?.also { resolver ->

                val contentValues = ContentValues().apply {

                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                fOut = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fOut = FileOutputStream(image)
        }

        fOut?.use {
            //Запись файла
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(applicationContext, "Saved", Toast.LENGTH_SHORT).show()
        }
    }


    private fun Initialize(){
        imgQr = findViewById(R.id.imageQr)
        ButGenerate = findViewById(R.id.buttonGen)
        editText = findViewById(R.id.editText)
        ButSave = findViewById(R.id.buttonSave)
        recyclerView = findViewById(R.id.recyclerView)
    }

    private fun getQr(){
        val thread = Thread{
            try {
                //инициализация клиента для коннекта к апи
                val client = OkHttpClient()
                //инициализация объекта для запроса к апи
                val request = Request.Builder()
                    .url("https://qrcodeutils.p.rapidapi.com/qrcodefree?text=${href}&validate=true&size=150&type=png&level=M")
                    .get()
                    .addHeader("X-RapidAPI-Host", "qrcodeutils.p.rapidapi.com")
                    .addHeader("X-RapidAPI-Key", "0be0467752mshed4b342163600c1p12e523jsn5d5f21b71643")
                    .build()


                val response = client.newCall(request).execute()
                val parse = response.body()?.byteStream()
                val value = BitmapFactory.decodeStream(parse)
                imgQr.setImageBitmap(value)
            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
        thread.start()
    }
}