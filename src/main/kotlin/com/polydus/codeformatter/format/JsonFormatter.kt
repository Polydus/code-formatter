package com.polydus.codeformatter.format

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.XmlPrettyPrinter
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter
import org.codehaus.stax2.XMLStreamWriter2
import org.springframework.stereotype.Component
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.StringReader
import java.io.StringWriter
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

@Component
class JsonFormatter() {

    private val jsonPrettyPrinters = Array<DefaultPrettyPrinter>(Indent.values().size){
        val indenter = DefaultIndenter(Indent.values()[it].value, DefaultIndenter.SYS_LF)
        DefaultPrettyPrinter().withSpacesInObjectEntries()
            .withArrayIndenter(indenter)
            .withObjectIndenter(indenter)
    }


    private val xmlPrettyPrinters = Array<DefaultXmlPrettyPrinter>(Indent.values().size){
        val indenter = XmlIndenter(Indent.values()[it])//DefaultIndenter(Indent.values()[it].value, DefaultIndenter.SYS_LF)
        DefaultXmlPrettyPrinter().apply {
            indentArraysWith(indenter)
            indentObjectsWith(indenter)
        }
    }

    private val writers = Array<ObjectWriter>(Indent.values().size){
        ObjectMapper().setDefaultPrettyPrinter(jsonPrettyPrinters[it]).writerWithDefaultPrettyPrinter()
    }

    private val jsonMapper = ObjectMapper()
    private val xmlMapper = XmlMapper()

    init {
        xmlMapper.apply{
            configure(SerializationFeature.INDENT_OUTPUT, true)
            configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false)
            configure(ToXmlGenerator.Feature.WRITE_XML_1_1, false)
        }
    }

    fun prettify(src: String, indent: Int): String?{
        return try {
            val obj = jsonMapper.readValue(src, JsonNode::class.java)
            writers[indent.coerceIn(0, Indent.values().size - 1)].writeValueAsString(obj).toString()
        } catch (e: Exception){
            null
        }
    }

    fun minify(src: String): String?{
        return try {
            val obj = jsonMapper.readValue(src, JsonNode::class.java)
            return obj.toString()
        } catch (e: Exception){
            null
        }
    }

    fun encodeBase64(src: String): String?{
        return try {
            return Base64.getEncoder().encodeToString(src.encodeToByteArray())
        } catch (e: Exception) {
            null
        }
    }

    fun decodeBase64(src: String): String?{
        return try {
            return String(Base64.getDecoder().decode(src))
        } catch (e: Exception){
            null
        }
    }

    fun jsonToXml(src: String, indent: Int, rootName: String?): String?{
        xmlMapper.setDefaultPrettyPrinter(xmlPrettyPrinters[indent.coerceIn(0, Indent.values().size - 1)])

        val stringWriter = StringWriter()

        var res = jsonToXmlAsArray(stringWriter, src, rootName)
        if(res != null) return prettyPrintXml(res, indent, rootName, stringWriter)


        return try {
            val obj = jsonMapper.readTree(src)
            stringWriter.append(xmlMapper.writeValueAsString(obj))

            res = stringWriter.toString()

            prettyPrintXml(res, indent, rootName, stringWriter)
        } catch (e: Exception){
            stringWriter.buffer.setLength(0)
            null
        }
    }

    fun xmlToJson(src: String, indent: Int): String?{
        return try {
            val node = xmlMapper.readTree(src.byteInputStream())
            return writers[indent.coerceIn(0, Indent.values().size - 1)].writeValueAsString(node).toString()
        } catch (e: Exception){
            null
        }
    }

    private fun jsonToXmlAsArray(stringWriter: StringWriter, src: String, rootName: String?): String?{
        return try {
            val obj = jsonMapper.readValue(src, ArrayNode::class.java)
            var index = 0
            obj.forEach {
                xmlMapper.writeValue(stringWriter, it)
                index++
            }
            return stringWriter.toString()
        } catch (e: Exception){
            stringWriter.buffer.setLength(0)
            null
        }
    }

    private fun prettyPrintXml(src: String, indent: Int, rootName: String?, stringWriter: StringWriter): String{
        var res = ""
        if(rootName != null){
            res += "<${rootName}>\n" + src + "</${rootName}>"
        } else {
            res += src
        }

        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(res.byteInputStream()))
        doc.normalize()

        val xPath = XPathFactory.newInstance().newXPath()
        val nodeList = xPath.evaluate("//text()[normalize-space()='']", doc, XPathConstants.NODESET) as NodeList

        for(i in 0 until nodeList.length){
            val node = nodeList.item(i)
            node.parentNode.removeChild(node)
        }

        val transformerFactory = TransformerFactory.newInstance()
        transformerFactory.setAttribute("indent-number", indent);
        val transformer = transformerFactory.newTransformer().apply {
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
            setOutputProperty(OutputKeys.INDENT, "yes")
        }

        stringWriter.buffer.setLength(0)
        transformer.transform(DOMSource(doc), StreamResult(stringWriter))
        return "<?xml version='1.0' encoding='UTF-8'?>\n$stringWriter"
    }

}