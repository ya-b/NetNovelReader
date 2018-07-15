package com.netnovelreader.utils

import android.os.Environment
import java.io.File

fun booksDir() = Environment.getExternalStorageDirectory().path + File.separator + "netnovelreader"

fun bookDir(bookname: String) = File(booksDir(), bookname).also { it.mkdirs() }

fun mkBookDir(bookname: String) = File(booksDir(), bookname).takeIf { !it.exists() }?.mkdirs() ?: true