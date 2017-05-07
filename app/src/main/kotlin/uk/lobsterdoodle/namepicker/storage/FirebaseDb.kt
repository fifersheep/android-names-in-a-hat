package uk.lobsterdoodle.namepicker.storage

import com.google.firebase.database.*


class FirebaseDb: RemoteDb {
    val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")

    lateinit var id: String

    override fun createUser(userId: String) {
        id = userId
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(err: DatabaseError?) { }
            override fun onDataChange(snapshot: DataSnapshot?) {
                if (snapshot != null && !snapshot.hasChild(id))
                    usersRef.child(id).setValue(true)
            }
        })
    }

    override fun editGroupDetails(groupId: Long, groupName: String, nameCount: Int) {
        val groupRef = usersRef.child(id).child("groups").child(groupId.toString())
        groupRef.child("label").setValue(groupName)
        groupRef.child("names").setValue(nameCount)
    }
}
