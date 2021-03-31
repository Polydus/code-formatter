package com.polydus.codeformatter.controller

import com.polydus.codeformatter.format.Encode
import com.polydus.codeformatter.format.Formatter
import com.polydus.codeformatter.model.FormSettingsInput
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Controller
import org.springframework.ui.Model

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping


@Controller
class IndexController {

    @Value("classpath:/string/en/strings_index.json")
    lateinit var resourceFile: Resource// = resourceLoader.getResource("classpath:/string/en/strings_index.json")//? = null

    @Autowired
    lateinit var formatter: Formatter//? = null

    private var strings: HashMap<String, String>? = null

    private var output: String? = null
   // private var exception = false
    private var formSettingsInput = FormSettingsInput()

    private val json = Json {  }

    @GetMapping("/")
    fun main(model: Model): String {
        if(strings == null){
            val input = resourceFile.inputStream
            val string = String(input.readAllBytes())
            strings = json.decodeFromString<HashMap<String, String>>(string)
        }
        addAllStrings(model)

        if(formSettingsInput.setting == null){
            formSettingsInput.setting = getString("formatter")
            formSettingsInput.decodetype = getString("base64")
            formSettingsInput.encodetype = getString("string")
            formSettingsInput.xmlToJson = getString("json_to_xml")

            formSettingsInput.minify = getString("minify")
            formSettingsInput.indentation = "2"

            formSettingsInput.xmlRootName = getString("")
            formSettingsInput.xmlArrayName = getString("")
        }

        model.addAttribute("FormSettingsInput", formSettingsInput)
        //model.addAttribute("output", "")
        output?.apply {
            model.addAttribute("output", output)
        }

        //println("get index")
        return "index" //view
    }

    @PostMapping("/")
    fun onFormSubmit(
        @ModelAttribute("FormSettingsInput")
        formSettingsInput: FormSettingsInput,
        model: Model
    ): String {
        val src = formSettingsInput.content ?: ""

        //println(formSettingsInput)
        val res = when(formSettingsInput.setting){
            getString("formatter") -> {
                when(formSettingsInput.minify){
                    getString("minify") -> {
                        val res = formatter.minify(src)
                        if(res == null) model.addAttribute("exception_message", getString("exception_message_invalid_json"))
                        res
                    }
                    getString("beautify") -> {
                        val indent = formSettingsInput.indentation?.toInt() ?: -1
                        val res = formatter.beautify(src, indent)
                        if(res == null) model.addAttribute("exception_message", getString("exception_message_invalid_json"))
                        res
                    }
                    else -> {
                        null
                    }
                }
            }
            getString("xml") -> {
                val indent = formSettingsInput.indentation?.toInt() ?: -1

                when(formSettingsInput.xmlToJson){
                    getString("xml_to_json") -> {
                        val res = formatter.xmlToJson(src, indent)
                        if(res == null) model.addAttribute("exception_message", getString("exception_message_invalid_xml"))
                        res
                    }
                    getString("json_to_xml") -> {
                        var xmlRootName = formSettingsInput.xmlRootName
                        if(xmlRootName.isNullOrEmpty()) xmlRootName = null
                        var xmlArrayName = formSettingsInput.xmlArrayName
                        if(xmlArrayName.isNullOrEmpty()) xmlArrayName = null
                        val res = formatter.jsonToXml(src, indent, xmlRootName, xmlArrayName)
                        if(res == null) model.addAttribute("exception_message", getString("exception_message_invalid_json"))
                        res
                    }
                    else -> null
                }
            }
            getString("encode_decode") -> {
                val from = getType(formSettingsInput.encodetype)
                val to = getType(formSettingsInput.decodetype)
                if(from == to){
                    src
                } else if(from == null || to == null){
                    null
                } else {
                    formatter.convert(src, from, to)
                }
            }
            else -> ""
        }
        this.formSettingsInput = formSettingsInput

        if(res == null){
            output = ""
            model.addAttribute("exception", true)
            addAllStrings(model)
            return "index"
        } else {
            output = res
        }

        //println("post index | $formSettingsInput")
        return "redirect:/"//"redirect:/"
    }

    private fun getType(type: String?): Encode?{
        return when(type) {
            getString("string") -> Encode.STRING
            getString("base64") -> Encode.BASE64
            getString("binary") -> Encode.BINARY
            getString("hex") -> Encode.HEX
            else -> null
        }
    }

    private fun addAllStrings(model: Model){
        strings?.apply {
            for(s in this){
                addString(model, s.key)
            }
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