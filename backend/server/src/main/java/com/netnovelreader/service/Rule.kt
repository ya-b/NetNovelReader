package com.netnovelreader.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.netnovelreader.AddRule
import com.netnovelreader.DeleteRule
import com.netnovelreader.ImportRule
import com.netnovelreader.QueryRule
import com.netnovelreader.db.SitePreference
import com.netnovelreader.db.SitePreferences
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
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.queryRule() {
    location<QueryRule> {
        handle {
            var list: List<SitePreference>? = null
            transaction {
                list = SitePreference.all().toMutableList()
            }
            list?.map { sitePreference ->
                SitePreferenceJsonBean.SitePreferenceBean(
                    sitePreference.hostname,
                    sitePreference.catalog_selector,
                    sitePreference.chapter_selector,
                    sitePreference.catalog_filter,
                    sitePreference.chapter_filter,
                    sitePreference.search_url,
                    sitePreference.redirect_fileld,
                    sitePreference.redirect_url,
                    sitePreference.no_redirect_url,
                    sitePreference.redirect_name,
                    sitePreference.no_redirect_name,
                    sitePreference.redirect_image,
                    sitePreference.no_redirect_image,
                    sitePreference.charset
                )
            }.also { beans -> call.respond(beans ?: emptyList<SitePreferenceJsonBean.SitePreferenceBean>()) }
        }
    }
}

fun Route.addRule() {
    location<AddRule> {
        authenticate {
            handle {
                if (!checkRoleAuth(this, 100)) {
                    call.respond(HttpStatusCode.Forbidden, "没有权限")
                    return@handle
                }
                val param =
                    if (call.request.httpMethod.equals(HttpMethod.Get))
                        call.parameters
                    else
                        call.receiveParameters()
                val hostname = param["hostname"]
                if (hostname.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, "添加失败，hostname不能为空")
                } else {
                    transaction {
                        SitePreference.new {
                            this.hostname = hostname!!
                            catalog_selector = param.get("catalog_selector") ?: ""
                            chapter_selector = param.get("chapter_selector") ?: ""
                            catalog_filter = param.get("catalog_filter") ?: ""
                            chapter_filter = param.get("chapter_filter") ?: ""
                            search_url = param.get("search_url") ?: ""
                            redirect_fileld = param.get("redirect_fileld") ?: ""
                            redirect_url = param.get("redirect_url") ?: ""
                            no_redirect_url = param.get("no_redirect_url") ?: ""
                            redirect_name = param.get("redirect_name") ?: ""
                            no_redirect_name = param.get("no_redirect_name") ?: ""
                            redirect_image = param.get("redirect_image") ?: ""
                            no_redirect_image = param.get("no_redirect_image") ?: ""
                            charset = param.get("charset") ?: ""
                        }
                    }
                    call.respond("添加成功")
                }
            }
        }
    }
}

fun Route.deleteRule() {
    location<DeleteRule> {
        authenticate {
            handle {
                if (checkRoleAuth(this, 100)) {
                    val params =
                        if (call.request.httpMethod.equals(HttpMethod.Get))
                            call.parameters
                        else
                            call.receiveParameters()
                    val hostname = params["hostname"]
                    when {
                        hostname == "*" -> transaction {
                            SitePreference.all().forEach { item -> item.delete() }
                        }
                        !hostname.isNullOrEmpty() -> transaction {
                            SitePreference.find { SitePreferences.hostname.eq(hostname!!) }
                                .forEach { item -> item.delete() }
                        }
                    }
                    call.respond("删除成功")
                } else {
                    call.respond(HttpStatusCode.Forbidden, "没有权限")
                }
            }
        }
    }
}

fun Route.importRule() {
    location<ImportRule> {
        authenticate {
            handle {
                if (!checkRoleAuth(this, 100)) {
                    call.respond("没有权限")
                    return@handle
                }
                var isSuccess = true
                call.receiveMultipart().forEachPart { part ->
                    if (part !is PartData.FileItem) return@forEachPart
                    val list = part.streamProvider().reader().use { input ->
                        val type = object :
                            TypeToken<ArrayList<SitePreferenceJsonBean.SitePreferenceBean>>() {}.type
                        try {
                            Gson().fromJson<ArrayList<SitePreferenceJsonBean.SitePreferenceBean>>(
                                input.readText(), type
                            )
                        } catch (e: Exception) {
                            isSuccess = false
                            null
                        }
                    }
                    transaction {
                        list?.forEach { bean ->
                            if (bean.hostname.isNullOrEmpty()) return@forEach
                            SitePreference.new {
                                hostname = bean.hostname ?: ""
                                catalog_selector = bean.catalog_selector ?: ""
                                chapter_selector = bean.chapter_selector ?: ""
                                catalog_filter = bean.catalog_filter ?: ""
                                chapter_filter = bean.chapter_filter ?: ""
                                search_url = bean.search_url ?: ""
                                redirect_fileld = bean.redirect_fileld ?: ""
                                redirect_url = bean.redirect_url ?: ""
                                no_redirect_url = bean.no_redirect_url ?: ""
                                redirect_name = bean.redirect_name ?: ""
                                no_redirect_name = bean.no_redirect_name ?: ""
                                redirect_image = bean.redirect_image ?: ""
                                no_redirect_image = bean.no_redirect_image ?: ""
                                charset = bean.charset ?: ""
                            }
                        }
                    }
                    part.dispose()
                }
                call.respond(if (isSuccess) "导入成功" else "导入失败")
            }
        }
    }
}

fun checkRoleAuth(context: PipelineContext<Unit, ApplicationCall>, role: Int): Boolean =
    context.call.authentication.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asInt() == role
