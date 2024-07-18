package com.example.uasmobileprograming

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class NewsAdd : AppCompatActivity() {
    private var pickImageRequest: Int = 1

    private var id: String? = null
    private var judul: String? = null
    private var deskripsi: String? = null
    private var image: String? = null

    private lateinit var title: EditText
    private lateinit var desc: EditText
    private lateinit var imageView: ImageView
    private lateinit var saveNews: Button
    private lateinit var chooseImage: Button
    private var imageUrl: Uri? = null

    private lateinit var dbNews: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_news_add)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbNews = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        title = findViewById(R.id.title)
        desc = findViewById(R.id.desc)
        imageView = findViewById(R.id.imageView)
        saveNews = findViewById(R.id.btnAdd)
        chooseImage = findViewById(R.id.btnChooseImage)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading. . .")

        chooseImage.setOnClickListener {
            openFileChooser()
        }

        intent?.let {
            id = it.getStringExtra("id")
            judul = it.getStringExtra("title")
            deskripsi = it.getStringExtra("desc")
            image = it.getStringExtra("imageUrl")

            title.setText(judul)
            desc.setText(deskripsi)
            Glide.with(this).load(image).into(imageView)
        }

        saveNews.setOnClickListener {
            val newsTitle = title.text.toString().trim()
            val newsDesc = desc.text.toString().trim()

            if (newsTitle.isEmpty() || newsDesc.isEmpty()) {
                Toast.makeText(this, "Judul & Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT)
                    .show()
            } else {
                progressDialog.show()
                if (imageUrl != null) {
                    uploadImageToStorage(newsTitle, newsDesc)
                } else {
                    saveData(newsTitle, newsDesc, image ?: "")
                }
            }
        }
    }

    private fun openFileChooser() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(intent, pickImageRequest)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImageRequest && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUrl = data.data
            imageView.setImageURI(imageUrl)
        }
    }

    private fun uploadImageToStorage(newsTitle: String, newsDesc: String) {
        imageUrl?.let {
            val storageRef: StorageReference =
                storage.reference.child("news_images/" + System.currentTimeMillis() + ".jpg")
            storageRef.putFile(it)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        saveData(newsTitle, newsDesc, imageUrl)
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Gagal mengunggah gambar: " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun saveData(newsTitle: String, newsDesc: String, imageUrl: String) {
        val news = mutableMapOf<String, Any>(
            "title" to newsTitle,
            "desc" to newsDesc,
            "imageUrl" to imageUrl
        )

        if (id != null) {
            dbNews.collection("news").document(id ?: "")
                .update(news)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Berhasil Diperbarui", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Memperbarui: " + e.message, Toast.LENGTH_SHORT).show()
                    Log.w("NewsAdd", "Terjadi kesalahan saat memperbarui dokumen", e)
                }
        } else {
            dbNews.collection("news")
                .add(news)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    title.setText("")
                    desc.setText("")
                    imageView.setImageResource(0)
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Terjadi kesalahan saat menambahkan: " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.w("NewsAdd", "Terjadi kesalahan saat menambahkan", e)
                }
        }
    }
}
