package org.howl.stagram.navigation.model

class Pushdto (
    var to : String? = null,
    var notification : Notification = Notification()){
        data class Notification(
            var title : String? = null,
            var body : String? = null
        )
}