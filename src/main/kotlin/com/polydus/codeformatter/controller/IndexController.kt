package com.polydus.codeformatter.controller

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultIndenter.SYS_LF
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.polydus.codeformatter.format.JsonFormatter
import com.polydus.codeformatter.model.FormSettingsInput
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Controller
import org.springframework.ui.Model

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import java.io.File
import java.nio.charset.StandardCharsets


@Controller
class IndexController {

    //@Autowired
    //lateinit var resourceLoader: ResourceLoader//? = null

    @Value("classpath:/string/en/strings_index.json")
    lateinit var resourceFile: Resource// = resourceLoader.getResource("classpath:/string/en/strings_index.json")//? = null

    @Autowired
    lateinit var jsonFormatter: JsonFormatter//? = null

    var strings: HashMap<String, String>? = null

    private var output: String? = null
    private var exception = false
    private var formSettingsInput = FormSettingsInput()

    init {
        //resourceFile = resourceLoader.getResource("classpath:/string/en/strings_index.json")//? = null
    }

    @GetMapping("/")
    fun main(model: Model): String {
        if(strings == null){
            val input = resourceFile?.inputStream//?.readT
           // val a = input.readAllBytes()
            val string = String(input.readAllBytes())
            val json = Json {  }
            strings = json.decodeFromString<HashMap<String, String>>(string)
            println()
        }

        strings?.apply {
            for(s in this){
                addString(model, s.key)
            }
        }

        if(formSettingsInput.setting == null){
            formSettingsInput.setting = getString("minify")
            formSettingsInput.encode = getString("encode")
            formSettingsInput.xmlToJson = getString("json_to_xml")

        }

        model.addAttribute("FormSettingsInput", formSettingsInput)
        //model.addAttribute("output", "")
        output?.apply {
            model.addAttribute("output", output)
        }

        if(exception){
            model.addAttribute("exception", true)
            exception = false
        }

        println("get index")
        return "index" //view
    }

    @PostMapping("/minify")
    fun minify(
        @ModelAttribute("FormSettingsInput")
        formSettingsInput: FormSettingsInput,
        model: Model
    ): String {
        val src = formSettingsInput.content ?: ""

        val res = when(formSettingsInput.setting){
            getString("minify") -> {
                jsonFormatter?.minify(src)
            }
            getString("beautify") -> {
                val indent = formSettingsInput.indentation?.toInt() ?: -1
                //println(indent)
                jsonFormatter?.prettify(src, indent)
            }
            getString("xml") -> {
                val indent = formSettingsInput.indentation?.toInt() ?: -1

                when(formSettingsInput.xmlToJson){
                    getString("xml_to_json") -> {
                        jsonFormatter?.xmlToJson(src, indent)
                    }
                    getString("json_to_xml") -> {
                        jsonFormatter?.jsonToXml(src, indent, "a")
                    }
                    else -> {
                        null
                    }
                }
            }
            getString("base64") -> {
                when (formSettingsInput.encode) {
                    getString("encode") -> {
                        jsonFormatter?.encodeBase64(src)
                    }
                    getString("decode") -> {
                        jsonFormatter?.decodeBase64(src)
                    }
                    else -> {
                        null
                    }
                }
            }
            else -> ""
        }
        if(res == null){
            output = ""
            exception = true
        } else {
            output = res
        }
        this.formSettingsInput = formSettingsInput

        //println("post index | $formSettingsInput")
        return "redirect:/"//"redirect:/"
    }

    private fun addString(model: Model, key: String){
        model.addAttribute(key, getString(key))
    }

    fun getString(key : String) : String {
        val strings = strings ?: return key
        return strings[key] ?: key
    }

}