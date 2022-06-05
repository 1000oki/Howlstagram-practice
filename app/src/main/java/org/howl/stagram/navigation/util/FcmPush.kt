package org.howl.stagram.navigation.util

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.howl.stagram.navigation.model.Pushdto

class FcmPush {
    var JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
    var url = "https://fcm.googleapis.com/fcm/send"
    var serverKey = "key=AAAArtdIPWc:APA91bEi3lKr_JCKw6fYbE4CYwZ9UgN0mjIthWf9g1RNgx_gj3o7w7v2FaOesI7oglJqFj_RQYcfFFv3JJJrSzWZYSdpmZR6yMN9l-iJDQGlRoTAKHKa9lEguzpkFF5RPIzazsjovVit"

    var okHttpClient : OkHttpClient? = null
    var gson : Gson? = null
    lateinit var firestore : FirebaseFirestore
    companion object{
        var instance = FcmPush()

    }

    init {
        firestore = FirebaseFirestore.getInstance()
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

    fun sendMessage(dUid : String, title : String, message : String){

        firestore.collection("pushtokens").document(dUid).get().addOnCompleteListener {
                result ->

            var token = result.result["token"].toString()

            var pushModel = Pushdto()
            pushModel.to = token
            pushModel.notification.title = title
            pushModel.notification.body = message

            var body = gson?.toJson(pushModel)!!.toRequestBody(JSON)


            var request = Request.Builder()
                .addHeader("Content-Type","application/json")
                .addHeader("Authorization",serverKey)
                .url(url)
                .post(body)
                .build()


            okHttpClient?.newCall(request)?.enqueue(object : Callback{

                override fun onFailure(call: Call, e: okio.IOException) {
                    TODO("Not yet implemented")
                }

                override fun onResponse(call: Call, response: Response) {
                    print(response.body.string())
                }
            })
        }


    }
}