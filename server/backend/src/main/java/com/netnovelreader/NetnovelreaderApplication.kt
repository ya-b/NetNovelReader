package com.netnovelreader

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration

@SpringBootApplication
class NetnovelreaderApplication

fun main(args: Array<String>) {
	runApplication<NetnovelreaderApplication>(*args)
}