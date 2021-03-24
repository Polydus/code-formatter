package com.polydus.codeformatter.format

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter
import org.codehaus.stax2.XMLStreamWriter2
import java.io.Serializable

class XmlIndenter(val indent: Indent): DefaultXmlPrettyPrinter.Indenter, Serializable {

    private val serialVersionUID = 1L

    constructor(): this(Indent.values().first())

    init {

    }

    override fun writeIndentation(g: JsonGenerator?, level: Int) {
        g?.writeRaw("\n${indent.value}")
    }

    override fun writeIndentation(sw: XMLStreamWriter2?, level: Int) {
        sw?.writeRaw("\n${indent.value}")
    }

    override fun isInline(): Boolean {
        return false
    }


}
