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

    override fun editGroupDetails(groupId: Long, groupName: String) {
        val groupsRef = usersRef.child(id).child("groups")
        groupsRef.child(groupId.toString()).child("label").setValue(groupName)
    }

    override fun removeGroup(groupId: Long) {
        usersRef.child(id).child("groups").child(groupId.toString()).removeValue()
    }

    override fun addName(groupId: Long, nameId: Long, name: String) {
        usersRef.child(id).child("groups").child(groupId.toString())
                .child("names").child(nameId.toString()).setValue(name)
    }

    override fun deleteName(groupId: Long, nameId: Long) {
        usersRef.child(id).child("groups").child(groupId.toString())
                .child("names").child(nameId.toString()).removeValue()
    }
}
