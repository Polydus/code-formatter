package com.polydus.codeformatter.model

class FormSettingsInput {

    var content: String? = null
    var setting: String? = null
    var indentation: String? = null
    var encode: String? = null

    var xmlToJson: String? = null

    override fun toString(): String {
        return "FormSettingsInput(content=$content, setting=$setting, indentation=$indentation, encode=$encode, xmlToJson=$xmlToJson)"
    }
    //var xmlRootName: String? = null


}