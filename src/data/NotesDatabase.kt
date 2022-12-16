package com.androiddevs.data

import com.androiddevs.data.collections.Note
import com.androiddevs.data.collections.User
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("NoteDatabase")
private val users=database.getCollection<User>()
private val notes = database.getCollection<Note>()


suspend fun registerUser( user: User):Boolean{
    return users.insertOne(user).wasAcknowledged()
}

suspend fun checkIfUserExists(email:String):Boolean{
    return users.findOne(User::email eq email)!=null
}
suspend fun checkPasswordForEmail(email:String,passwordToCheck:String):Boolean{
    val actualPassword = users.findOne(User::email eq email)?.password ?:return false
    return actualPassword == passwordToCheck
}
suspend fun getNotesForUser(email:String):List<Note>{
    return notes.find(Note::owners contains email).toList()
}

suspend fun saveNote(notess:Note):Boolean{
    val noteExists = notes.findOneById(notess.id) !=null
    return if(noteExists){
        notes.updateOneById(notess.id,notess).wasAcknowledged()
    }
    else{
        notes.insertOne(notess).wasAcknowledged()
    }
}

suspend fun deleteNoteForUser(email: String,noteId:String):Boolean{
    val note = notes.findOne(Note::id eq noteId, Note::owners contains email)
    note?.let {note->
        if(note.owners.size > 1){
            // the note has multiple owners, so we just delete the email from the owners list
            val newOwners = note.owners - email
            val updateResult = notes.updateOne(Note::id eq note.id, setValue(Note::owners,newOwners))
            return updateResult.wasAcknowledged()
        }
        return notes.deleteOneById(note.id).wasAcknowledged()
    }
        ?: return false

}

suspend fun addOwnerToNote(noteId: String,owner:String):Boolean{
    val owners = notes.findOneById(noteId)?.owners?:return false
    return notes.updateOneById(noteId, setValue(Note::owners,owners+owner)).wasAcknowledged()
}
suspend fun isOwnerOfNote(noteId: String,owner:String):Boolean{
    val note = notes.findOneById(noteId)?:return false
    return owner in note.owners
}
