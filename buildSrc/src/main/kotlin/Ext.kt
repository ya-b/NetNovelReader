import org.gradle.api.initialization.Settings
import java.io.File

fun Settings.module(dir: String, name: String){
    include(":$name")
    project(":$name").projectDir = File("$dir/$name")
}