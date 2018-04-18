package com.netnovelreader.service

import com.google.gson.Gson
import com.netnovelreader.AddRule
import com.netnovelreader.DeleteRule
import com.netnovelreader.ImportRule
import com.netnovelreader.QueryRule
import com.netnovelreader.db.SitePreferenceDao
import com.netnovelreader.model.RespMessage
import com.netnovelreader.model.SitePreferenceBean
import com.netnovelreader.model.SitePreferenceJsonBean
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.content.PartData
import io.ktor.content.forEachPart
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.location
import io.ktor.pipeline.PipelineContext
import io.ktor.request.httpMethod
import io.ktor.request.receiveMultipart
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Route

fun Route.queryRule(dao: SitePreferenceDao) {
    location<QueryRule> {
        handle {
            val params = if(call.request.httpMethod.equals(HttpMethod.Get)) call.parameters else call.receiveParameters()
            val hostname = params["hostname"]
            if (hostname.isNullOrEmpty()) {
                dao.getAllPreference()?.let { call.respond(RespMessage(6, rules = it.map { it.toShort() })) }
                        ?: call.respond(RespMessage(7))
            } else {
                dao.getPreference(hostname!!)?.let { call.respond(RespMessage(6, rule = it.toShort())) }
                        ?: call.respond(RespMessage(7))
            }
        }
    }
}

fun Route.addRule(dao: SitePreferenceDao) {
    location<AddRule> {
        authenticate {
            handle {
                if (!checkRoleAuth(this, 100)) {
                    call.respond(HttpStatusCode.Forbidden, RespMessage(8, "Insufficient authority"))
                    return@handle
                }
                val param = if(call.request.httpMethod.equals(HttpMethod.Get)) call.parameters else call.receiveParameters()
                val hostname = param["hostname"]
                if (hostname.isNullOrEmpty()) {
                    call.respond(RespMessage(7, "failed : hostname is null or empty"))
                } else {
                    dao.addPreference(SitePreferenceBean().also {
                        it.hostname = hostname
                        it.catalog_selector = param.get("catalog_selector")
                        it.chapter_selector = param.get("chapter_selector")
                        it.catalog_filter = param.get("catalog_filter")
                        it.chapter_filter = param.get("chapter_filter")
                        it.search_url = param.get("search_url")
                        it.redirect_fileld = param.get("redirect_fileld")
                        it.redirect_url = param.get("redirect_url")
                        it.no_redirect_url = param.get("no_redirect_url")
                        it.redirect_name = param.get("redirect_name")
                        it.no_redirect_name = param.get("no_redirect_name")
                        it.redirect_image = param.get("redirect_image")
                        it.no_redirect_image = param.get("no_redirect_image")
                        it.charset = param.get("charset")
                    })
                    call.respond(RespMessage(6, "success"))
                }
            }
        }
    }
}

fun Route.deleteRule(dao: SitePreferenceDao) {
    location<DeleteRule> {
        authenticate {
            handle {
                if (checkRoleAuth(this, 100)) {
                    val params = if(call.request.httpMethod.equals(HttpMethod.Get)) call.parameters else call.receiveParameters()
                    val hostname = params["hostname"]
                    when {
                        hostname == "*" -> dao.deleteAllPreference()
                        !hostname.isNullOrEmpty() -> dao.deletePreference(hostname!!)
                    }
                    call.respond(RespMessage(6))
                } else {
                    call.respond(HttpStatusCode.Forbidden, RespMessage(8, "Insufficient authority"))
                }
            }
        }
    }
}

fun Route.importRule(dao: SitePreferenceDao) {
    location<ImportRule> {
        authenticate {
            handle {
                if (!checkRoleAuth(this, 100)){
                    call.respond(HttpStatusCode.Forbidden, RespMessage(8, "Insufficient authority"))
                    return@handle
                }
                var isSuccess = false
                call.receiveMultipart().forEachPart {
                    if(it !is PartData.FileItem) return@forEachPart
                    try {
                        it.streamProvider().reader().use {
                            it.readText().let { Gson().fromJson(it, SitePreferenceJsonBean::class.java) }
                                ?.arr?.let { dao.addPreference(*it.toTypedArray()); isSuccess = true }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    it.dispose()
                }
                call.respond(RespMessage(if (isSuccess) 6 else 7))
            }
        }
    }
}

fun checkRoleAuth(context: PipelineContext<Unit, ApplicationCall>, role: Int): Boolean =
    context.call.authentication.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asInt() == role
