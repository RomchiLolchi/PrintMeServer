package site.romchilolchi

import ServiceAttributeSet
import org.apache.pdfbox.printing.PDFPageable
import java.io.File
import javax.print.DocFlavor
import javax.print.PrintService
import javax.print.PrintServiceLookup
import javax.print.SimpleDoc
import javax.print.attribute.HashPrintRequestAttributeSet
import javax.print.attribute.HashPrintServiceAttributeSet
import javax.print.attribute.standard.Chromaticity
import javax.print.attribute.standard.Copies
import javax.print.attribute.standard.OrientationRequested

object PrintingTools {
    var selectedAttributes: ServiceAttributeSet? = null
        private set
    var selectedPrinter: PrintService? = null
        private set
    var uploadedFile: File? = null

    fun getPrintersNames(attributes: ServiceAttributeSet): List<String> {
        selectedAttributes = attributes
        return PrintServiceLookup.lookupPrintServices(null, HashPrintServiceAttributeSet()).map { it.name }
    }

    fun setSelectedPrinter(name: String) {
        for (i in PrintServiceLookup.lookupPrintServices(null, null)) {
            if (name == i.name) {
                i?.let {
                    selectedPrinter = i
                }
            }
        }
    }

    fun print() {
        try {
            uploadedFile?.let { file ->
                selectedPrinter?.let { printer ->
                    val flavor = file.resolveDocFlavor()
                    printer.createPrintJob()
                        .print(
                            SimpleDoc(
                                if (flavor != DocFlavor.SERVICE_FORMATTED.PAGEABLE) file.inputStream() else PDFPageable(
                                    org.apache.pdfbox.Loader.loadPDF(file)
                                ), flavor, null
                            ),
                            selectedAttributes?.let {
                                HashPrintRequestAttributeSet().apply {
                                    add(Copies(selectedAttributes!!.copies))
                                    add(if (selectedAttributes!!.orientation == "portrait") OrientationRequested.PORTRAIT else OrientationRequested.LANDSCAPE)
                                    add(if (selectedAttributes!!.colorPrint) Chromaticity.COLOR else Chromaticity.MONOCHROME)
                                }
                            }
                        )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun File.resolveDocFlavor(): DocFlavor {
        return when (extension.lowercase()) {
            "txt" -> DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST
            "html", "htm" -> DocFlavor.INPUT_STREAM.TEXT_HTML_HOST
            "gif" -> DocFlavor.INPUT_STREAM.GIF
            "jpeg", "jpg" -> DocFlavor.INPUT_STREAM.JPEG
            "png" -> DocFlavor.INPUT_STREAM.PNG
            "pdf" -> DocFlavor.SERVICE_FORMATTED.PAGEABLE
            else -> DocFlavor.INPUT_STREAM.AUTOSENSE
        }
    }
}