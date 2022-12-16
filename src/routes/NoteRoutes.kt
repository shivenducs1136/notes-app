package com.androiddevs.routes

import com.androiddevs.data.*
import com.androiddevs.data.collections.Note
import com.androiddevs.data.requests.AddOwnerRequest
import com.androiddevs.data.requests.DeleteNoteRequest
import com.androiddevs.data.responses.SimpleResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.engine.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.noteRoutes(){
    route("/getNotes"){
        authenticate {
            get {
                val email = call.principal<UserIdPrincipal>()!!.name
                val notes = getNotesForUser(email)

                call.respond(OK,notes)

            }
        }
    }
        route("/addOwnerToNote"){
        authenticate {
            post {
                val request = try{
                    call.receive<AddOwnerRequest>()
                }
                catch (e:ContentTransformationException){
                    call.respond(BadRequest)
                    return@post
                }
                if(!checkIfUserExists(request.owner)){
                    call.respond(
                        OK,
                        SimpleResponse(false,"No user with this email exists")
                    )
                    return@post
                }
                if(isOwnerOfNote(request.noteId,request.owner)){
                    call.respond(
                        OK,
                        SimpleResponse(false,"This user is already an owner of this note")
                    )
                    return@post
                }
                if(addOwnerToNote(request.noteId,request.owner)){
                    call.respond(
                        OK,
                        SimpleResponse(true,"${request.owner} can now see this note")

                    )

                }
                else{
                    call.respond(Conflict)
                }
            }
        }
    }
    route("/deleteNotes"){
        authenticate {
            post {
                val email = call.principal<UserIdPrincipal>()!!.name
                val request = try{
                    call.receive<DeleteNoteRequest>()
                }catch (e:ContentTransformationException){
                    call.respond(BadRequest)
                    return@post
                }
                if(deleteNoteForUser(email,request.id)){
                    call.respond(OK)
                }
                else{
                    call.respond(Conflict)
                }
            }
        }
    }
    route("/addNotes"){
        authenticate {
            post{
                val note = try{
                    call.receive<Note>()
                }
                catch (e:ContentTransformationException){
                    call.respond(BadRequest)
                    return@post
                }

                if(saveNote(note)){
                    call.respond(OK)
                }
                else{
                    call.respond(Conflict)
                }
            }
        }
    }


}