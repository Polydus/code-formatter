package com.polydus.codeformatter.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.ui.Model
import javax.annotation.PostConstruct
import javax.print.DocFlavor

@Component
class Strings() {

    lateinit var strings: HashMap<String, String>

    @Value("classpath:/string/en/strings_index.json")
    lateinit var resourceFile: Resource// = resourceLoader.getResource("classpath:/string/en/strings_index.json")//? = null

    private val json = Json {  }

    @PostConstruct
    fun init(){
        val input = resourceFile.inputStream
        val string = String(input.readAllBytes())
        strings = json.decodeFromString<HashMap<String, String>>(string)
    }

    fun addAllStringsToModel(model: Model){
        strings.apply {
            for(s in this) addString(model, s.key)
        }
    }

    private fun addString(model: Model, key: String){
        model.addAttribute(key, getString(key))
    }

    fun getString(key : String) : String {
        val strings = strings ?: return key
        return strings[key] ?: key
    }

}