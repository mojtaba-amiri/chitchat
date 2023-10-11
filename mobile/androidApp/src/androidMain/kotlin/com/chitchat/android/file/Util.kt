package com.chitchat.android.file

import android.content.Context
import java.io.File


fun Context.write(txt:String, fileName: String) {
    try {
//        var fName : File? = null
//        var file: File? = null
//        fileName.split("/").let {
//            if (it.size > 1) {
//                for (t in 0 until (it.size - 1)) {
//                    file = File(filesDir, it[t])
//                    file?.mkdir()
//                }
//                fName = File(file, it[it.size - 1])
//            } else {
//                fName = File(fileName)
//            }
//        }
//        val fis = FileOutputStream(fName) // 2nd line
        openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(txt.toByteArray())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}