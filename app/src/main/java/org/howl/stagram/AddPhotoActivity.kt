package org.howl.stagram

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import org.howl.stagram.databinding.ActivityAddPhotoBinding
import org.howl.stagram.navigation.model.ContentDTO
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity(){
    lateinit var binding : ActivityAddPhotoBinding
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore?  = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_add_photo)

        //Initiate
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //Open the album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        getContent.launch(photoPickerIntent)
        binding.addphotoUploadBtn.setOnClickListener{
            contentUpload()
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if(result.resultCode == RESULT_OK) {
            photoUri = result.data?.data
            binding.addphotoImage.setImageURI(photoUri)
            Log.d("이미지", "성공")
        }
        else{
            Log.d("이미지", "실패")
        }
    }
    fun contentUpload(){
        //Make filename
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE"+timestamp+"_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        // 1. Promise method
        storageRef?.putFile(photoUri!!)?.continueWithTask { task : Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

            //Insert douwnloadUrl of image
            contentDTO.imageUrl = uri.toString()

            //Insert uid of user
            contentDTO.uid = auth?.currentUser?.uid

            //Insert userId
            contentDTO.userId = auth?.currentUser?.email

            //Insert explain of content
            contentDTO.explain = binding.addphotoEditExplain.text.toString()

            //Insert timestamp
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)

            setResult(Activity.RESULT_OK)

            finish()
        }

        // 2. Callback method
//        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
//            storageRef.downloadUrl.addOnSuccessListener { uri->
//                var contentDTO = ContentDTO()
//
//                //Insert douwnloadUrl of image
//                contentDTO.imageUrl = uri.toString()
//
//                //Insert uid of user
//                contentDTO.uid = auth?.currentUser?.uid
//
//                //Insert userId
//                contentDTO.userId = auth?.currentUser?.email
//
//                //Insert explain of content
//                contentDTO.explain = binding.addphotoEditExplain.text.toString()
//
//                //Insert timestamp
//                contentDTO.timestamp = System.currentTimeMillis()
//
//                firestore?.collection("images")?.document()?.set(contentDTO)
//
//                setResult(Activity.RESULT_OK)
//
//                finish()
//            }
//        }
    }
}
