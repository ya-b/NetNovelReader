package com.netnovelreader.utils

import android.os.Environment
import com.netnovelreader.ReaderApp
import java.io.File

fun booksDirOld() =
    Environment.getExternalStorageDirectory().path + File.separator + "netnovelreader"

fun booksDir() =
    Environment.getExternalStorageDirectory().path + File.separator + ReaderApp::class.java.`package`.name

fun bookDir(bookname: String) = File(booksDir(), bookname).also { it.mkdirs() }

fun mkBookDir(bookname: String) =
    File(booksDir(), bookname).takeIf { !it.exists() }?.mkdirs() ?: true