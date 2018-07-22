package com.netnovelreader

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.netnovelreader.db.ReaderDatabase
import com.netnovelreader.service.*
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.content.defaultResource
import io.ktor.content.resources
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.route
import javax.naming.AuthenticationException

@Location("login") class Login
@Location("logout") class Logout
@Location("query") class QueryRule       // rule/query
@Location("add") class AddRule           // rule/add
@Location("delete") class DeleteRule     //   rule/delete
@Location("import") class ImportRule     //   rule/import
@Location("save") class SaveRecord       //   record/save
@Location("restore") class RestoreRecord //   record/restore

fun Application.main() {
    val config = environment.config
    val uploadDir = config.config("reader").property("uploadDir").getString()
    val jwtConfit = config.config("jwt")
    val issuer = jwtConfit.property("issuer").getString()
    val audience = jwtConfit.property("audience").getString()
    val realm = jwtConfit.property("realm").getString()
    val secret = jwtConfit.property("secret").getString()
    val db = ReaderDatabase().apply { init("reader", "reader") }
    install(DefaultHeaders)
    install(CallLogging)
    install(Locations)
    install(ConditionalHeaders)
    install(PartialContent)
    install(AutoHeadResponse)
    install(ContentNegotiation) { gson { setPrettyPrinting() } }
    install(Authentication) { auth(issuer, audience, realm, secret, db) }
    install(StatusPages) { statusPage() }
    install(Routing) { routes(uploadDir, issuer, audience, secret, db) }
}

fun Authentication.Configuration.auth(issuer: String, audience: String, realm: String, secret: String, db: ReaderDatabase){
    val jwtVerifier = JWT.require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()
    jwt {
        this.realm = realm
        verifier(jwtVerifier)
        validate {
            val user = db.userDao().getUser(it.payload.getClaim("username")?.asString())
            if (it.payload.audience.contains(audience) && user != null) {
                JWTPrincipal(it.payload)
            } else {
                null
            }
        }
    }
}

fun StatusPages.Configuration.statusPage(){
    exception<AuthenticationException> { call.respond(HttpStatusCode.Unauthorized) }
    status(HttpStatusCode.NotFound, HttpStatusCode.Unauthorized) {
        call.respond("${it.value}\n${it.description}")
    }
}

fun Routing.routes(uploadDir: String, issuer: String, audience: String, secret: String, db: ReaderDatabase){
    route("reader") {
        defaultResource("index.html", "web")
        resources("web")
        login(issuer, audience, secret, db.userDao())
        logout()
        route("rule") {
            db.sitePreferenceDao().also {
                queryRule(it)
                addRule(it)
                deleteRule(it)
                importRule(it)
            }
        }
        route("record") {
            saveRecord(uploadDir)
            restoreRecord(uploadDir)
        }
    }
}