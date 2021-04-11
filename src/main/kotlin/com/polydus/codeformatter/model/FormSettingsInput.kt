package com.polydus.codeformatter.model

class FormSettingsInput() {

    var content: String? = null
    var setting: String? = null
    var indentation: String? = null

    var encodetype: String? = null
    var decodetype: String? = null

    var minify: String? = null

    var xmlToJson: String? = null
    var xmlRootName: String? = null
    var xmlArrayName: String? = null

    override fun toString(): String {
        return "FormSettingsInput(content=$content, setting=$setting, indentation=$indentation, encodetype=$encodetype, decodetype=$decodetype, minify=$minify, xmlToJson=$xmlToJson, xmlRootName=$xmlRootName, xmlArrayName=$xmlArrayName)"
    }

}