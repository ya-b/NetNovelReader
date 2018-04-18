package com.netnovelreader.service

import com.google.gson.Gson
import com.netnovelreader.RestoreRecord
import com.netnovelreader.SaveRecord
import com.netnovelreader.model.RespMessage
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.content.PartData
import io.ktor.content.forEachPart
import io.ktor.locations.location
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Route
import java.io.File
import java.io.IOException

fun Route.saveRecord(uploadDir: String) {
    location<SaveRecord> {
        authenticate {
            handle {
                var isSuccess = false
                val username = call.authentication.principal<JWTPrincipal>()?.payload
                    ?.getClaim("username")?.asString() ?: "any"
                call.receiveMultipart().forEachPart { part ->
                    if (part !is PartData.FileItem) return@forEachPart
                    try {
                        part.streamProvider().use { `in` ->
                            File(uploadDir, username).outputStream().buffered().use { `in`.copyTo(it) }
                            isSuccess = true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    part.dispose()
                }
                call.respond(RespMessage(if (isSuccess) 6 else 7))
            }
        }
    }
}

fun Route.restoreRecord(uploadDir: String) {
    location<RestoreRecord> {
        authenticate {
            handle {
                val username = call.authentication.principal<JWTPrincipal>()?.payload
                        ?.getClaim("username")?.asString() ?: "any"
                val respMessage = RespMessage(6)
                try {
                    File(uploadDir, username).readText()
                } catch (e: IOException) {
                    respMessage.ret = 7
                    null
                }?.also { fileText ->
                    try {
                        respMessage.books = Gson().fromJson(fileText, RespMessage::class.java).books
                    } catch (e: Exception) {
                        respMessage.ret = 7
                    }
                }
                call.respond(respMessage)
            }
        }
    }
}