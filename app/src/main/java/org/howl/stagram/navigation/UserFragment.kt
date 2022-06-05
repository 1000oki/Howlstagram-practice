package org.howl.stagram.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.howl.stagram.LoginActivity
import org.howl.stagram.MainActivity
import org.howl.stagram.R
import org.howl.stagram.databinding.FragmentUserBinding
import org.howl.stagram.databinding.ItemImageviewBinding
import org.howl.stagram.navigation.model.AlarmDTO
import org.howl.stagram.navigation.model.ContentModel

class UserFragment : Fragment() {
    lateinit var binding: FragmentUserBinding
    lateinit var firestore: FirebaseFirestore
    var dUid: String? = null
    var userId: String? = null
    var currentUid: String? = null
    lateinit var auth: FirebaseAuth
    lateinit var storeage: FirebaseStorage
    var currentUserUid : String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firestore = FirebaseFirestore.getInstance()
        storeage = FirebaseStorage.getInstance()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false)
        currentUid = FirebaseAuth.getInstance().uid // 나의 UID
        auth = FirebaseAuth.getInstance()
        dUid = arguments?.getString("dUid")
        userId = arguments?.getString("userId")
        currentUserUid = auth?.currentUser?.uid

        var mainActivity = activity as? MainActivity

        if(currentUid == dUid){
            //나의페이지
            mainActivity?.binding?.toolbarLogo?.visibility = View.VISIBLE
            mainActivity?.binding?.toolbarUsername?.visibility = View.INVISIBLE
            mainActivity?.binding?.toolbarBtnBack?.visibility = View.INVISIBLE
            binding.accountBtnFollowSignout.text = activity?.getText(R.string.signout)
            binding.accountBtnFollowSignout.setOnClickListener {
                auth.signOut()
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
            }


        }else{
            //상대방 페이지
            mainActivity?.binding?.toolbarLogo?.visibility = View.INVISIBLE
            mainActivity?.binding?.toolbarUsername?.visibility = View.VISIBLE
            mainActivity?.binding?.toolbarBtnBack?.visibility = View.VISIBLE

            mainActivity?.binding?.toolbarUsername?.text = userId
            mainActivity?.binding?.toolbarBtnBack?.setOnClickListener {
                mainActivity?.binding?.bottomNavigation.selectedItemId = R.id.action_home
            }
            binding.accountBtnFollowSignout.text = activity?.getText(R.string.follow)

        }
        binding.accountRecyclerview.adapter = UserFragmentRecyclerviewAdapter()
        binding.accountRecyclerview.layoutManager = GridLayoutManager(activity, 3)


        return binding.root
    }

    fun followerAlarm(dUid : String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = dUid
        alarmDTO.userId = auth.currentUser?.email
        alarmDTO.uid = auth.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()

        firestore.collection("alarms").document().set(alarmDTO)
        var message = auth.currentUser?.email + "이 나를 팔로우 하기 시작했습니다."

    }


        inner class CellImageViewHolder(val binding: ItemImageviewBinding) :
            RecyclerView.ViewHolder(binding.root)

        inner class UserFragmentRecyclerviewAdapter : RecyclerView.Adapter<CellImageViewHolder>() {
            var contentModels: ArrayList<ContentModel> = arrayListOf()

            init {
                firestore.collection("images").whereEqualTo("uid", dUid)
                    .addSnapshotListener { value, error ->

                        for (item in value!!.documentChanges) {
                            if (item.type == DocumentChange.Type.ADDED) {
                                contentModels.add(item.document.toObject(ContentModel::class.java)!!)
                            }
                        }
                        binding.accountPostTextview.text = contentModels.size.toString()

                        notifyDataSetChanged()
                    }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellImageViewHolder {
                var width = resources.displayMetrics.widthPixels / 3

                var view =
                    ItemImageviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                view.cellImageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
                return CellImageViewHolder(view)
            }

            override fun onBindViewHolder(holder: CellImageViewHolder, position: Int) {
                var contentModel = contentModels[position]
                Glide.with(holder.itemView.context).load(contentModel.imageUrl)
                    .into(holder.binding.cellImageview)
            }

            override fun getItemCount(): Int {
                return contentModels.size
            }


    }
}
