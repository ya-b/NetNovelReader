package com.netnovelreader

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.routing

fun Application.main(){
//    install(DefaultHeaders)
//    install(CallLogging)
//    install(Routing) {
//        static("custom") {
//            staticRootFolder = File("./src/main/webapp")
//            default(File(staticRootFolder!!, "index.jsp"))
//        }
//    }
    routing {
        homeRoute()
//        loginRoute()
    }
}

fun Routing.homeRoute(){
    get("/") {
        call.respondText { "hello" }
//        call.respondHtmlTemplate(BaseTemplate()){
//            content {
//                form(action = "search", encType = FormEncType.applicationXWwwFormUrlEncoded, method = FormMethod.post){
//                    p {
//                        + "BookName:"
//                        textInput(name = "bookname")
//                    }
//                    p {
//                        submitInput { value = "search" }
//                    }
//                }
//            }
//        }
    }
}

fun Routing.loginRoute(){
    get("/login"){

    }
}

fun Routing.search(){
    get("/search"){

    }
}