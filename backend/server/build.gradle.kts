import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    id("kotlin")
    id("war")
    id("com.bmuschko.tomcat")
}

dependencies {
    implementation(deps.kotlin.stdlib.jdk)
    implementation(deps.kotlin.ktor.servlet)
    implementation(deps.kotlin.ktor.gson)
    implementation(deps.kotlin.ktor.auth_jwt)
    implementation(deps.kotlin.ktor.location)
    implementation(deps.kotlin.ktor.html_builder)
    implementation(deps.apache.dbutils)
    implementation(deps.mariadb_client)
    implementation(deps.jsoup)
    implementation(deps.slf4j)
    testImplementation(deps.test.junit)
    testImplementation(deps.test.hamcrest)
    testImplementation(deps.test.mockito)
    tomcat(deps.apache.tomcatEmbed.core)
    tomcat(deps.apache.tomcatEmbed.jasper)
}

tomcat {
    contextPath = "/reader"
    httpProtocol = "org.apache.coyote.http11.Http11Nio2Protocol"
    ajpProtocol  = "org.apache.coyote.ajp.AjpNio2Protocol"
}

kotlin.experimental.coroutines = Coroutines.ENABLE

tasks.withType(War::class.java) {
    baseName = "reader"
}