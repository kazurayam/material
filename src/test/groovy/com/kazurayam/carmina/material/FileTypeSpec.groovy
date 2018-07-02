package com.kazurayam.carmina.material

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.carmina.material.FileType

import spock.lang.Specification

class FileTypeSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(FileTypeSpec.class)

    def testToString() {
        setup:
        logger_.debug("FileType.PNG.toString():\n${FileType.PNG.toString()}")
        expect:
        FileType.PNG.toString() == '{"FileType":{"extension":"png","mimeType":"image/png","description":"Portable Network Graphics"}}'
    }

    def testGetExtension() {
        expect:
        FileType.PNG.getExtension() == 'png'
    }

    def testGetMimeType() {
        expect:
        FileType.PNG.getMimeType() == 'image/png'
    }

    def testGetByExtension() {
        expect:
        FileType.getByExtension('png') == FileType.PNG
    }

    def testGetByMimeType() {
        expect:
        FileType.getByMimeType('image/png') == FileType.PNG
    }

    // -----------------------------------------------------------------------
    def testBMP() {
        expect:
        FileType.BMP.getExtension() == 'bmp'
    }

    def testCSV() {
        expect:
        FileType.CSV.getExtension() == 'csv'
    }

    def testGIF() {
        expect:
        FileType.GIF.getExtension() == 'gif'
    }

    def testJPEG() {
        expect:
        FileType.JPEG.getExtension() == 'jpeg'
    }

    def testJPG() {
        expect:
        FileType.JPG.getExtension() == 'jpg'
    }

    def testJSON() {
        expect:
        FileType.JSON.getExtension() == 'json'
    }

    def testNULL() {
        expect:
        FileType.NULL.getExtension() == ''
    }

    def testPDF() {
        expect:
        FileType.PDF.getExtension() == 'pdf'
    }

    def testPNG() {
        expect:
        FileType.PNG.getExtension() == 'png'
    }

    def testTXT() {
        expect:
        FileType.TXT.getExtension() == 'txt'
    }

    def testXLS() {
        expect:
        FileType.XLS.getExtension() == 'xls'
    }

    def testXLSM() {
        expect:
        FileType.XLSM.getExtension() == 'xlsm'
    }

    def testXLSX() {
        expect:
        FileType.XLSX.getExtension() == 'xlsx'
    }

    def testXML() {
        expect:
        FileType.XML.getExtension() == 'xml'
    }

}