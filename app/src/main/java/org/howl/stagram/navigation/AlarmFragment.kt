package org.howl.stagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.howl.stagram.databinding.FragmentAlarmBinding
import org.howl.stagram.databinding.ItemCommentBinding
import org.howl.stagram.navigation.model.AlarmDTO

class AlarmFragment : Fragment() {
    lateinit var binding : FragmentAlarmBinding
    lateinit var auth : FirebaseAuth
    lateinit var firestore : FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlarmBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        binding.alarmRecyclerview.adapter = AlarmAdapter()
        binding.alarmRecyclerview.layoutManager = LinearLayoutManager(activity)
        return binding.root

    }
    inner class ItemPersonViewHolder(var binding : ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)
    inner class AlarmAdapter : RecyclerView.Adapter<ItemPersonViewHolder>(){
        var alarmList = arrayListOf<AlarmDTO>()

        init {
            var uid = auth.uid
            firestore.collection("alarms").whereEqualTo("destinationUid",uid).addSnapshotListener { value, error ->
                alarmList.clear()
                for (item in value!!.documents){
                    alarmList.add(item.toObject(AlarmDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }

        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemPersonViewHolder {
            var view = ItemCommentBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            return ItemPersonViewHolder(view)
        }

        override fun onBindViewHolder(holder: ItemPersonViewHolder, position: Int) {
            var view = holder.binding
            var alarmModel = alarmList[position]
            view.messageTextview.visibility = View.INVISIBLE
            when(alarmModel.kind){
                0 ->{
                    var m = alarmModel.userId + "가 좋아요를 눌렀습니다."
                    view.profileTextview.text = m
                }
                1 ->{
                    var m_1 = alarmModel.userId +"가" + alarmModel.message + "라는 메세지를 남겼습니다."
                    view.profileTextview.text = m_1
                }
                2 ->{
                    var m_2 = alarmModel.userId +"가 나를 팔로우 하기 시작했습니다."
                    view.profileTextview.text = m_2
                }
            }
        }

        override fun getItemCount(): Int {
            return alarmList.size
        }

    }
}