package org.howl.stagram.navigation.model

data class ContentDTO(
    var explain : String? = null,
    var imageUrl : String? = null,
    var uid : String? = null,
    var userId : String? = null,
    var timestamp : Long? = null,
    var favoriteCount : Int = 0,
    //좋아요 누른 유저 관리
    var favorites : MutableMap<String, Boolean> = HashMap()){
    //댓글 관리
    data class Comment(
        var uid : String? = null,
        var userId : String? = null,
        var comment : String? = null,
        var timestamp : Long? = null)
}