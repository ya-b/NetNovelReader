import org.jetbrains.kotlin.gradle.dsl.Coroutines

plugins {
    id("kotlin")
    id("war")
    id("com.bmuschko.tomcat")
}

dependencies {
    implementation(deps.kotlin.stdlib.jdk)
    implementation(deps.kotlin.ktor.servlet)
    implementation(deps.javaee_api)
    implementation(deps.commons_dbutils)
    implementation(deps.commons_fileupload)
    implementation(deps.mariadb_client)
    implementation(deps.jsoup)
    implementation(deps.slf4j)
    implementation(deps.gson)
    testImplementation(deps.test.junit)
    testImplementation(deps.test.hamcrest)
    testImplementation(deps.test.mockito)
    tomcat(deps.tomcatEmbed.core)
    tomcat(deps.tomcatEmbed.jasper)
}

tomcat {
    contextPath = "/"
    httpProtocol = "org.apache.coyote.http11.Http11Nio2Protocol"
    ajpProtocol  = "org.apache.coyote.ajp.AjpNio2Protocol"
}

war.webAppDirName = "webapp"
kotlin.experimental.coroutines = Coroutines.ENABLE

task("run"){
    dependsOn("tomcatRun")
}