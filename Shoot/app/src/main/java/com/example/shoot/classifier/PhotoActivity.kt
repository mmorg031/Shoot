package com.example.shoot.classifier

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.shoot.BuildConfig
import com.example.shoot.R

private const val REQUEST_PERMISSIONS = 1
private const val REQUEST_TAKE_PICTURE = 2


class PhotoActivity : AppCompatActivity() {

    private var classifier =  ClassifierMove(this)
    private var photoFilePath=""
    private var predictedTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)
        predictedTextView = findViewById(R.id.textViewResult)
        confirmPermissions()
        classifier.initialize()
    }

    private fun confirmPermissions() {
        var permissionAlreadyGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if(permissionAlreadyGranted){
            takePhoto()
        }
        else{
            requestPermissions()
        }
    }

    private fun requestPermissions(){
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        REQUEST_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        var permissionGranted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        if(requestCode== REQUEST_PERMISSIONS && permissionGranted){
            takePhoto()
        }
        else{
            requestPermissions()
        }

    }

    private fun takePhoto(){
        photoFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/${System.currentTimeMillis()}.jpg"
        val currentPhotoUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+ ".provider", File(photoFilePath) )
        //val currentPhotoUri = Uri.fromFile( File(photoFilePath))

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
        takePictureIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val file = File(photoFilePath)
        if (requestCode == REQUEST_TAKE_PICTURE && file.exists()) {
            classifyMove(file)
        }
    }


    private fun classifyMove(file: File){
        //get photo and classify here
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)

        if ((bitmap != null) && (classifier.isInitialized)) {
            val result = classifier.classifyAsync(bitmap)
            predictedTextView?.text = result

                /*.addOnSuccessListener { resultText -> predictedTextView?.text = resultText }
                .addOnFailureListener { e ->
                    predictedTextView?.text = "ffuuuckkkk"
                    Log.e(TAG, "Error classifying drawing.", e)
                }*/
        }
    }

    companion object {
        private const val TAG = "PhotoActivity"
    }
}