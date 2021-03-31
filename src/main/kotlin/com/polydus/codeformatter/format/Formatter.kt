package com.polydus.codeformatter.format

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter
import org.apache.tomcat.util.buf.HexUtils
import org.springframework.stereotype.Component
import org.thymeleaf.util.StringUtils
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.StringWriter
import java.lang.StringBuilder
import java.math.BigInteger
import java.util.*
import javax.print.attribute.IntegerSyntax
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

@Component
class Formatter() {

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

    private val jsonWriters = Array<ObjectWriter>(Indent.values().size){
        ObjectMapper().setDefaultPrettyPrinter(jsonPrettyPrinters[it]).writerWithDefaultPrettyPrinter()
    }

    private val jsonMapper = ObjectMapper()

    private val xmlMapper = XmlMapper()
    //private val xmlWriter = xmlMapper.writer().withRootName("asdf")
    private val xmlWriterNoRoot = xmlMapper.writer()

    init {
        xmlMapper.apply{
            configure(SerializationFeature.INDENT_OUTPUT, true)
            configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false)
            configure(ToXmlGenerator.Feature.WRITE_XML_1_1, false)
        }
    }

    fun beautify(src: String, indent: Int): String?{
        return try {
            val obj = jsonMapper.readValue(src, JsonNode::class.java)
            jsonWriters[indent.coerceIn(0, Indent.values().size - 1)].writeValueAsString(obj).toString()
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

    fun convert(src: String, from: Encode, to: Encode): String?{
        if(from == to) return src

        return when(from){
            Encode.STRING -> {
                when(to){
                    Encode.BASE64 -> encodeBase64(src, true)
                    Encode.BINARY -> encodeBinary(src, true)
                    Encode.HEX -> encodeHex(src, true)
                    else -> null
                }
            }
            Encode.BASE64 -> {
                val clear = encodeBase64(src, false) ?: return null
                when(to){
                    Encode.STRING -> clear
                    Encode.BINARY -> encodeBinary(clear, true)
                    Encode.HEX -> encodeHex(clear, true)
                    else -> null
                }
            }
            Encode.BINARY -> {
                val clear = encodeBinary(src, false) ?: return null
                when(to){
                    Encode.STRING -> clear
                    Encode.BASE64 -> encodeBase64(clear, true)
                    Encode.HEX -> encodeHex(clear, true)
                    else -> null
                }
            }
            Encode.HEX -> {
                val clear = encodeHex(src, false) ?: return null
                when(to){
                    Encode.STRING -> clear
                    Encode.BASE64 -> encodeBase64(clear, true)
                    Encode.BINARY -> encodeBinary(clear, true)
                    else -> null
                }
            }
        }
    }

    private fun encodeBase64(src: String, encode: Boolean): String?{
        return try {
            if(encode){
                Base64.getEncoder().encodeToString(src.encodeToByteArray())
            } else {
                String(Base64.getDecoder().decode(src))
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun encodeBinary(src: String, encode: Boolean): String?{
        return try {
            val stringBuilder = StringBuilder()
            if(encode){
                val chars = src.toCharArray()
                for(c in chars){
                    val int = Integer.toBinaryString(c.toInt())
                    if(int.length < 8){
                        repeat(8 - int.length){
                            stringBuilder.append("0")
                        }
                    }
                    stringBuilder.append(int)
                }
                stringBuilder.toString()
            } else {
                val string = org.springframework.util.StringUtils.trimAllWhitespace(src)
                for(c in 0 until string.length step 8){
                    stringBuilder.append(
                        Integer.parseInt(string.substring(c, c + 8), 2).toChar()
                    )
                }
                stringBuilder.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun encodeHex(src: String, encode: Boolean): String?{
        return try {
            if(encode){
                HexUtils.toHexString(src.toByteArray())
            } else {
                val string = org.springframework.util.StringUtils.trimAllWhitespace(src)
                return String(HexUtils.fromHexString(string))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun jsonToXml(src: String, indent: Int, rootName: String?, objectsName: String?): String?{
        val thisIndent = Indent.values()[indent.coerceIn(0, Indent.values().size - 1)]
        xmlMapper.setDefaultPrettyPrinter(xmlPrettyPrinters[thisIndent.ordinal])

        val xmlWriter = getXmlWriter(objectsName)
        val stringWriter = StringWriter()

        var res = jsonToXmlAsArray(stringWriter, src, xmlWriter)
        if(res != null) return prettyPrintXml(res, thisIndent.ordinal, rootName, stringWriter)


        return try {
            val obj = jsonMapper.readTree(src)
            //stringWriter.append(xmlMapper.writeValueAsString(obj))
            stringWriter.append(xmlWriter.writeValueAsString(obj))

            res = stringWriter.toString()

            prettyPrintXml(res, thisIndent.ordinal, rootName, stringWriter)
        } catch (e: Exception){
            stringWriter.buffer.setLength(0)
            null
        }
    }

    fun xmlToJson(src: String, indent: Int): String?{
        return try {
            val node = xmlMapper.readTree(src.byteInputStream())
            return jsonWriters[indent.coerceIn(0, Indent.values().size - 1)].writeValueAsString(node).toString()
        } catch (e: Exception){
            null
        }
    }

    private fun jsonToXmlAsArray(stringWriter: StringWriter, src: String, xmlWriter: ObjectWriter): String?{
        return try {
            val obj = jsonMapper.readValue(src, ArrayNode::class.java)
            var index = 0
            obj.forEach {
                //xmlMapper.writeValue(stringWriter, it)
                xmlWriter.writeValue(stringWriter, it)
                index++
            }
            return stringWriter.toString()
        } catch (e: Exception){
            stringWriter.buffer.setLength(0)
            null
        }
    }

    private fun prettyPrintXml(src: String, indent: Int, rootName: String?, stringWriter: StringWriter): String{
        try {
            var res = ""
            if(rootName != null){
                res += "<${rootName}>\n$src</${rootName}>"
            } else {
                res += "<root>\n$src</root>"
            }
            println(res)

            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(res.byteInputStream()))
            doc.normalize()

            val xPath = XPathFactory.newInstance().newXPath()
            val nodeList = xPath.evaluate("//text()[normalize-space()='']", doc, XPathConstants.NODESET) as NodeList

            for(i in 0 until nodeList.length){
                val node = nodeList.item(i)
                node.parentNode.removeChild(node)
            }

            val transformerFactory = TransformerFactory.newInstance()
            //transformerFactory.setFeature(OutputKeys.INDENT, "")
            transformerFactory.setAttribute("indent-number", indent)
            val transformer = transformerFactory.newTransformer().apply {
                setOutputProperty(OutputKeys.ENCODING, "UTF-8")
                setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
                setOutputProperty(OutputKeys.INDENT, "yes")
            }

            stringWriter.buffer.setLength(0)
            transformer.transform(DOMSource(doc), StreamResult(stringWriter))
            res = stringWriter.toString()
            if(indent == Indent.TABS_0.ordinal){
                res = res.replace("     ", "\t")
            } else if(indent == Indent.TABS_1.ordinal){
                res = res.replace("      ", "\t\t")
            }
            return "<?xml version='1.0' encoding='UTF-8'?>\n$res"
        } catch (e: Exception){
            //e.printStackTrace()
            return ""
        }
    }

    private fun getXmlWriter(rootName: String?): ObjectWriter{
        if(rootName == null) return xmlWriterNoRoot
        return xmlMapper.writer().withRootName(rootName)
    }

}