package com.polydus.codeformatter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Main{

	companion object{
		@JvmStatic
		fun main(args: Array<String>) {
			runApplication<Main>(*args)
		}

	}
}

