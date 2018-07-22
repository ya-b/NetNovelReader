package com.netnovelreader.service

import com.netnovelreader.RestoreRecord
import com.netnovelreader.SaveRecord
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
                call.respond(if (isSuccess) "保存成功" else "保存失败")
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
                try {
                    File(uploadDir, username).readText()
                } catch (e: IOException) {
                    null
                }.also {
                    call.respond(it ?: "")
                }
            }
        }
    }
}