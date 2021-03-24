package com.polydus.codeformatter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Springtest3Application{

	companion object{
		@JvmStatic
		fun main(args: Array<String>) {
			runApplication<Springtest3Application>(*args)
		}

	}
}

