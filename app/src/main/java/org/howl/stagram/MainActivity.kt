package org.howl.stagram

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import org.howl.stagram.databinding.ActivityMainBinding
import org.howl.stagram.navigation.AlarmFragment
import org.howl.stagram.navigation.DetailViewFragment
import org.howl.stagram.navigation.GridFragment
import org.howl.stagram.navigation.UserFragment

class MainActivity : AppCompatActivity(){
    lateinit var binding: ActivityMainBinding
    lateinit var message : FirebaseMessaging
    lateinit var firestore : FirebaseFirestore
    lateinit var auth : FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        message = FirebaseMessaging.getInstance()
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initNavigationBar()
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
        binding.bottomNavigation.findViewById<View>(R.id.action_photo).setOnLongClickListener {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                var i = Intent(this, AddPhotoActivity::class.java)
                i.putExtra("isBeauty", true)
                startActivity(i)
            }
            return@setOnLongClickListener true
        }

        //Set default screen
        binding.bottomNavigation.selectedItemId = R.id.action_home

        registerPushToken()
    }

    fun initNavigationBar() {
        binding.bottomNavigation.run {
            setOnItemSelectedListener { item ->
                setToolbarDefault()
                when (item.itemId) {
                    R.id.action_home -> {
                        var detailViewFragment = DetailViewFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, detailViewFragment).commit()
                    }
                    R.id.action_search -> {
                        var gridFragment = GridFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, gridFragment).commit()
                    }
                    R.id.action_photo -> {
                        if(ContextCompat.checkSelfPermission(applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                            //사진 읽기(READ_EXTERNAL_STORAGE) 있을때
                            startActivity(Intent(applicationContext,AddPhotoActivity::class.java))
                        }
                    }
                    R.id.action_favorite_alarm -> {
                        var alarmFragment = AlarmFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, alarmFragment).commit()
                    }
                    R.id.action_account -> {
                        var userFragment = UserFragment()
                        var bundle = Bundle()
                        var uid = FirebaseAuth.getInstance().currentUser?.uid
                        bundle.putString("dUid", uid)
                        userFragment.arguments = bundle
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, userFragment).commit()
                    }
                }
                true
            }
            selectedItemId = R.id.action_home
        }
    }
    fun setToolbarDefault(){
        binding.toolbarBtnBack.visibility = View.GONE
        binding.toolbarUsername.visibility = View.GONE
        binding.toolbarLogo.visibility = View.VISIBLE
    }

    fun registerPushToken(){
        message.token.addOnCompleteListener { task ->
            if(task.isSuccessful){

                var token = task.result
                var map = mutableMapOf<String,Any>()
                map["token"] = token

                firestore.collection("pushtokens").document(auth.uid!!).set(map)

            }

        }
    }
}






