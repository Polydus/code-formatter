package com.polydus.codeformatter.controller

import com.polydus.codeformatter.format.Encode
import com.polydus.codeformatter.format.Formatter
import com.polydus.codeformatter.model.FormSettingsInput
import com.polydus.codeformatter.model.Strings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.Errors

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import javax.validation.Valid

@Controller
class IndexController {

    @Autowired
    lateinit var formatter: Formatter

    @Autowired
    private lateinit var strings: Strings

    //private var output: String? = null
   // private var exception = false
    //private var formSettingsInput = FormSettingsInput()

    @GetMapping("/")
    fun main(model: Model): String {
        strings.addAllStringsToModel(model)

        val formSettingsInput = FormSettingsInput()
        if(formSettingsInput.setting == null){
            formSettingsInput.setting = strings.getString("formatter")
            formSettingsInput.decodetype = strings.getString("base64")
            formSettingsInput.encodetype = strings.getString("string")
            formSettingsInput.xmlToJson = strings.getString("json_to_xml")

            formSettingsInput.minify = strings.getString("minify")
            formSettingsInput.indentation = "2"

            formSettingsInput.xmlRootName = strings.getString("")
            formSettingsInput.xmlArrayName = strings.getString("")
        }

        model.addAttribute("FormSettingsInput", formSettingsInput)
        //model.addAttribute("output", "")
        /*output?.apply {
            model.addAttribute("output", output)
        }*/

        //println("get index")
        return "index" //view
    }

    @PostMapping("/")
    fun onFormSubmit(
        @ModelAttribute("FormSettingsInput")
        @Valid
        formSettingsInput: FormSettingsInput,
        model: Model, errors: Errors
    ): String {
        if(errors.hasErrors()){
            return "redirect:/"
        }

        val src = formSettingsInput.content ?: ""

        //println(formSettingsInput)
        val res = when(formSettingsInput.setting){
            strings.getString("formatter") -> {
                when(formSettingsInput.minify){
                    strings.getString("minify") -> {
                        val res = formatter.minify(src)
                        if(res == null) model.addAttribute("exception_message", strings.getString("exception_message_invalid_json"))
                        res
                    }
                    strings.getString("beautify") -> {
                        val indent = formSettingsInput.indentation?.toInt() ?: -1
                        val res = formatter.beautify(src, indent)
                        if(res == null) model.addAttribute("exception_message", strings.getString("exception_message_invalid_json"))
                        res
                    }
                    else -> {
                        null
                    }
                }
            }
            strings. getString("xml") -> {
                val indent = formSettingsInput.indentation?.toInt() ?: -1

                when(formSettingsInput.xmlToJson){
                    strings.getString("xml_to_json") -> {
                        val res = formatter.xmlToJson(src, indent)
                        if(res == null) model.addAttribute("exception_message", strings.getString("exception_message_invalid_xml"))
                        res
                    }
                    strings.getString("json_to_xml") -> {
                        var xmlRootName = formSettingsInput.xmlRootName
                        if(xmlRootName.isNullOrEmpty()) xmlRootName = null
                        var xmlArrayName = formSettingsInput.xmlArrayName
                        if(xmlArrayName.isNullOrEmpty()) xmlArrayName = null
                        val res = formatter.jsonToXml(src, indent, xmlRootName, xmlArrayName)
                        if(res == null) model.addAttribute("exception_message", strings.getString("exception_message_invalid_json"))
                        res
                    }
                    else -> null
                }
            }
            strings.getString("encode_decode") -> {
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

        //this.formSettingsInput = formSettingsInput
        strings.addAllStringsToModel(model)

        if(res == null){
            model.addAttribute("exception", true)
            model.addAttribute("output", "")
        } else {
            model.addAttribute("exception", false)
            model.addAttribute("output", res)
        }
        return "index"

        //println("post index | $formSettingsInput")
        //return "redirect:/"//"redirect:/"
    }

    private fun getType(type: String?): Encode?{
        return when(type) {
            strings.getString("string") -> Encode.STRING
            strings.getString("base64") -> Encode.BASE64
            strings.getString("binary") -> Encode.BINARY
            strings.getString("hex") -> Encode.HEX
            else -> null
        }
    }

}