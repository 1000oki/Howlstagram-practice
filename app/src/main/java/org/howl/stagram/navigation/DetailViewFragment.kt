package org.howl.stagram.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.howl.stagram.CommentActivity
import org.howl.stagram.R
import org.howl.stagram.databinding.FragmentDetailBinding
import org.howl.stagram.databinding.ItemDetailBinding
import org.howl.stagram.navigation.model.AlarmDTO
import org.howl.stagram.navigation.model.ContentDTO
import org.howl.stagram.navigation.util.FcmPush

class DetailViewFragment : Fragment() {
    lateinit var binding: FragmentDetailBinding
    var firestore: FirebaseFirestore? = null
    lateinit var uid : String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().uid!!

        binding.detailviewfragmentRecyclerview.adapter = DetailviewRecyclerviewAdapter()
        binding.detailviewfragmentRecyclerview.layoutManager = LinearLayoutManager(activity)
        return binding.root
    }

    inner class CustomViewHolder(var binding: ItemDetailBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class DetailviewRecyclerviewAdapter() : RecyclerView.Adapter<CustomViewHolder>() {

        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")
                ?.orderBy("timestamp")
                ?.addSnapshotListener { value, error ->
                    contentDTOs.clear()
                    contentUidList.clear()

                    if (value == null) return@addSnapshotListener
                    for (snapshot in value!!.documents) {
                        var item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            var view = ItemDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            var viewHolder = holder.binding
            //UserId
            viewHolder.detailviewitemProfileTextview.text = contentDTOs[position].userId
            //Image
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl)
                .into(viewHolder.detailviewitemImageviewContent)
            //Explain of content
            viewHolder.detailviewitemExplainTextview.text = contentDTOs[position].explain
            //likes
            viewHolder.detailviewitemFavoritecounterTextview.text =
                "Likes " + contentDTOs[position].favoriteCount

            //This code is when the button is clicked
            viewHolder.detailviewitemFavoriteImageview.setOnClickListener {
                favoriteEvent(position)
            }

            viewHolder.detailviewitemProfileImage.setOnClickListener {
                var userFragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("dUid",contentDTOs[position].uid)
                bundle.putString("userId",contentDTOs[position].userId)
                userFragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,userFragment)?.commit()

            }

            //This code is when the page is loaded
            if(contentDTOs[position].favorites.containsKey(uid)){
                //This is like status
                viewHolder.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite)
            }else{
                //This is unlike status
                viewHolder.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite_border)
            }
            //profileImage
             Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl)
               .into(viewHolder.detailviewitemProfileImage)

            viewHolder.detailviewitemCommentImageview.setOnClickListener {
                var i = Intent(activity, CommentActivity::class.java)
                i.putExtra("ContentUid",contentUidList[position])
                i.putExtra("dUid", contentDTOs[position].uid)
                startActivity(i)
            }
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
        fun favoriteEvent(position:Int){
            var docId = contentUidList[position]
            var tsDoc = firestore?.collection("images")?.document(docId)
            firestore?.runTransaction {
                    transition ->
                var contentDTO = transition.get(tsDoc!!).toObject(ContentDTO::class.java)
                if(contentDTO!!.favorites.containsKey(uid)){
                    //좋아요 누른 상태-> 클릭시 좋아요 해제
                    contentDTO.favoriteCount = contentDTO.favoriteCount - 1
                    contentDTO.favorites.remove(uid)

                }else{
                    //좋아요를 누르지 않은 상태 -> 클릭시 좋아요
                    contentDTO.favoriteCount = contentDTO.favoriteCount + 1
                    contentDTO.favorites[uid] = true
                    favoriteAlarm(contentDTOs[position].uid!!)

                }
                transition.set(tsDoc,contentDTO)
            }
        }
        fun favoriteAlarm(destinationUid : String){
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

            var message = FirebaseAuth.getInstance()?.currentUser?.email+getString(R.string.alarm_favorite)
            FcmPush.instance.sendMessage(destinationUid,"Howlstagram", message)
        }
    }
}
