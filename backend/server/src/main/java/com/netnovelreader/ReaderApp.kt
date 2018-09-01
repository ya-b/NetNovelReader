package com.netnovelreader

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.netnovelreader.db.SitePreferences
import com.netnovelreader.db.User
import com.netnovelreader.db.Users
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
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.vendors.DatabaseDialect
import javax.naming.AuthenticationException
import kotlin.reflect.full.createInstance

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
    val dbConfig = config.config("db")
    val dbUrl = dbConfig.property("url").getString()
    val dbDrivername = dbConfig.property("drivername").getString()
    val dbUsername = dbConfig.property("username").getString()
    val dbPassword = dbConfig.property("password").getString()
    connectDB(dbUrl, dbDrivername, dbUsername, dbPassword)
    install(DefaultHeaders)
    install(CallLogging)
    install(Locations)
    install(ConditionalHeaders)
    install(PartialContent)
    install(AutoHeadResponse)
    install(ContentNegotiation) { gson { setPrettyPrinting() } }
    install(Authentication) { auth(issuer, audience, realm, secret) }
    install(StatusPages) { statusPage() }
    install(Routing) { routes(uploadDir, issuer, audience, secret) }
}

fun Authentication.Configuration.auth(issuer: String, audience: String, realm: String, secret: String){
    val jwtVerifier = JWT.require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()
    jwt {
        this.realm = realm
        verifier(jwtVerifier)
        validate {
            val username = it.payload.getClaim("username")?.asString() ?: ""
            var isEmpty = true
            if(username.isNotEmpty()) {
                transaction {
                    isEmpty = User.find { Users.username.eq(username) }.empty()
                }
            }
            if (it.payload.audience.contains(audience) && !isEmpty) {
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

fun Routing.routes(uploadDir: String, issuer: String, audience: String, secret: String){
    route("reader") {
        defaultResource("index.html", "web")
        resources("web")
        login(issuer, audience, secret)
        logout()
        route("rule") {
            queryRule()
            addRule()
            deleteRule()
            importRule()
        }
        route("record") {
            saveRecord(uploadDir)
            restoreRecord(uploadDir)
        }
    }
}

fun connectDB(url: String, driver: String, username: String, password: String) {
    Database.registerDialect("mariadb") {
        @Suppress("UNCHECKED_CAST")
        val type = (Class.forName("org.jetbrains.exposed.sql.vendors.MysqlDialect") as Class<DatabaseDialect>).kotlin
        type.createInstance()
    }
    Database.connect(url, driver, username, password)
    transaction {
        SchemaUtils.create (SitePreferences, Users)
    }
}