package kkhouse.com.utils

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

//fun getLocateMainPath(clazz: Class<*>): String {
//    val classPath = clazz.getResource("").path
//    val projectRoot = File(classPath).parentFile.parentFile.parentFile.parentFile
//    return ""
//}


fun findModuleRootDirectoryPath(startingClass: Class<*>): String {
    var path = Paths.get(startingClass.getResource("").toURI())

    while (path != null) {
        if (path.fileName.toString() == "build") {
            return Paths.get(path.parent.toUri().path, "src", "main").toString()
        }
        path = path.parent
    }

    throw IllegalStateException("Unable to locate 'main' directory.")
}