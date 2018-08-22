package com.kazurayam.material

import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Specification

//@Ignore
class MaterialSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(MaterialSpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static RepositoryRoot repoRoot_
    private static TCaseResult tcr_


    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(MaterialSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        Path materials = workdir_.resolve('Materials')
        RepositoryFileScanner scanner = new RepositoryFileScanner(materials)
        scanner.scan()
        repoRoot_ = scanner.getRepositoryRoot()
    }
    def setup() {
        TSuiteResult tsr = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS1'), new TSuiteTimestamp('20180530_130419'))
        tcr_ = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
    }


    // feature methods
    def testCompareTo_byFileType() {
        when:
        Material mate1 = new Material(Paths.get('.'), new URL('https://www.google.com/'), Suffix.NULL, FileType.JPG).setParent(tcr_)
        Material mate2 = new Material(Paths.get('.'), new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        then:
        mate1.compareTo(mate2) < 0
        mate2.compareTo(mate1) > 0
    }

    def testCompareTo_bySuffix() {
        when:
        Material mate1 = new Material(Paths.get('.'), new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        Material mate2 = new Material(Paths.get('.'), new URL('https://www.google.com/'), new Suffix(1), FileType.PNG).setParent(tcr_)
        then:
        mate1.compareTo(mate2) > 0
        mate2.compareTo(mate1) < 0
    }

    def testCompareTo_byURL() {
        when:
        Material mate1 = new Material(Paths.get('.'), new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        Material mate2 = new Material(Paths.get('.'), new URL('https://www.google.com/abc'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        then:
        mate1.compareTo(mate2) < 0
        when:
        Material mate3 = new Material(Paths.get('.'), new URL('https://aaa.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        then:
        mate1.compareTo(mate3) > 0
    }

    def testCompareTo_equal() {
        when:
        Material mate1 = new Material(Paths.get('.'), new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        Material mate2 = new Material(Paths.get('.'), new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        then:
        mate1.compareTo(mate2) == 0
    }



    def testConstructorWithTCaseResultAndFilePath() {
        setup:
        Path filePath = repoRoot_.getBaseDir().resolve('main.TS1/20180530_130419/main.TC1/foo/bar/fixture.xls')
        when:
        Material mate = new Material(tcr_, filePath)
        then:
        mate.getParent() == tcr_
        mate.getTCaseResult() == tcr_
        mate.getURL() == null
        mate.getSuffix() == Suffix.NULL
        mate.getFileType() == FileType.XLS
        mate.getDirpath() == Paths.get('foo/bar')
        mate.getPath().toString().contains('main.TS1/20180530_130419/main.TC1/foo/bar/fixture.xls'.replace('/', File.separator))
    }


    def testEquals() {
        when:
        Material mate1 = new Material(Paths.get('.'), new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        Material mate2 = new Material(Paths.get('.'), new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        then:
        mate1 != null
        mate2 != null
        mate1 == mate2
    }

    def testEquals_differentURL() {
        Material mate1 = new Material(Paths.get('.'), new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        when:
        Material mate3 = new Material(Paths.get('.'), new URL('https://www.yahoo.com/'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        then:
        mate3 != null
        mate1 != mate3
    }

    def testEquals_differentSuffix() {
        Material mate1 = new Material(Paths.get('.'), new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        when:
        Material mate3 = new Material(Paths.get('.'), new URL('https://www.google.com/'), new Suffix(1), FileType.PNG).setParent(tcr_)
        then:
        mate3 != null
        mate1 != mate3
    }

    def testEquals_differentFileType() {
        Material mate1 = new Material(Paths.get('.'), new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        when:
        Material mate3 = new Material(Paths.get('.'), new URL('https://www.google.com/'), Suffix.NULL, FileType.JPEG).setParent(tcr_)
        then:
        mate3 != null
        mate1 != mate3
    }

    def testEscapeHtml() {
        expect:
        Material.escapeHtml("This is a test") == 'This&nbsp;is&nbsp;a&nbsp;test'
        Material.escapeHtml("&") == '&amp;'
        Material.escapeHtml("<") == '&lt;'
        Material.escapeHtml(">") == '&gt;'
        Material.escapeHtml('"') == '&quot;'
        Material.escapeHtml(" ") == '&nbsp;'
        Material.escapeHtml("©") == '&copy;'
        Material.escapeHtml("<xml>") == '&lt;xml&gt;'
    }
    def testGetDirpath_noSubpath() {
        when:
        Material mate = tcr_.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), new Suffix(1), FileType.PNG)
        then:
        mate != null
        mate.getDirpath() == Paths.get('.')
    }


    def testGetDirpath_withSubpath() {
        when:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS1'), new TSuiteTimestamp('20180718_142832'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC4'))
        Material mate = tcr.getMaterial(Paths.get('foo'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        logger_.debug("#testGetSubpath_withSubpath mate.getSubpath()=${mate.getDirpath()}")
        then:
        mate != null
        mate.getDirpath() == Paths.get('foo')
    }

    def testGetEncodedHrefRelativeToRepositoryRoot() {
        when:
        Material mate = tcr_.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), new Suffix(1), FileType.PNG)
        then:
        mate != null
        when:
        String href = mate.getEncodedHrefRelativeToRepositoryRoot()
        then:
        href != null
        href == 'main.TS1/20180530_130419/main.TC1/http%253A%252F%252Fdemoaut.katalon.com%252F(1).png'
        !href.contains('file:///')
    }

    def testGetHrefRelativeToRepositoryRoot() {
        when:
        Material mate = tcr_.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), new Suffix(1), FileType.PNG)
        then:
        mate != null
        when:
        String href = mate.getHrefRelativeToRepositoryRoot()
        then:
        href != null
        href == 'main.TS1/20180530_130419/main.TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png'
        !href.contains('file:///')
    }



    def testGetIdentifierOfExcelFile() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS4'), new TSuiteTimestamp('20180712_142755'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        Material mate = tcr.getMaterial(Paths.get('smilechart.xls'))
        then:
        mate != null
        when:
        String id = mate.getIdentifier()
        then:
        id == 'smilechart.xls'
    }

    def testGetIdentifier_withoutSuffix() {
        setup:
        Material mate = new Material(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        when:
        String title = mate.getIdentifier()
        then:
        title == 'http://demoaut.katalon.com/ PNG'
    }

    def testGetIdentifier_withSuffix() {
        when:
        Material mate = new Material(Paths.get('.'), new URL('http://demoaut.katalon.com/'), new Suffix(1), FileType.PNG).setParent(tcr_)
        then:
        mate != null
        when:
        String title = mate.getIdentifier()
        then:
        title == 'http://demoaut.katalon.com/ (1) PNG'
    }

    def testGetIdentifier_FileTypeOmmited() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS3'), new TSuiteTimestamp('20180627_140853'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC3'))
        when:
        Material mate = tcr.getMaterial(Paths.get('.'), new URL('http://files.shareholder.com/downloads/AAPL/6323171818x0xS320193-17-70/320193/filing.pdf'),
            Suffix.NULL, FileType.PDF)
        then:
        mate != null
        when:
        String title = mate.getIdentifier()
        logger_.debug("#testGetModalWindowTitle_FileTypeOmmited title=${title}")
        then:
        title == 'http://files.shareholder.com/downloads/AAPL/6323171818x0xS320193-17-70/320193/filing.pdf'
    }


    def testGetPath() {
        when:
        Material mate = tcr_.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        then:
        mate != null
        when:
        Path path = mate.getPath()
        logger_.debug("#testGetPath path=${path.toString()}")
        then:
        path.toString().contains(
            'main.TS1/20180530_130419/main.TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png'.replace('/', File.separator))
        !path.toString().contains('..')   // should be normalized
    }

    def testGetPath_Excel() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS4'), new TSuiteTimestamp('20180712_142755'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        List<Material> materials = tcr.getMaterials()
        for (Material mate : materials) {
            logger_.debug("#testGetPath_Excel mate.getPath()=${mate.getPath()}")
            assert !mate.getPath().contains('..')
        }
        then:
        true
    }

    def testGetPathBySubpath() {
        when:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(
                new TSuiteName('Test Suites/main/TS1'), new TSuiteTimestamp('20180718_142832'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC4'))
        Material png = tcr.getMaterial(Paths.get('foo/http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
        then:
        png != null
        png.getPath().toString().contains(
                'main.TS1/20180718_142832/main.TC4/foo/http%3A%2F%2Fdemoaut.katalon.com%2F.png'.replace('/', File.separator))
    }

    def testGetPathRelativeToTSuiteTimestamp() {
        when:
        Material mate = tcr_.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), new Suffix(1), FileType.PNG)
        Path relative = mate.getPathRelativeToTSuiteTimestamp()
        then:
        relative != null
        relative.toString().replace('\\', '/') == 'main.TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png'
    }


    def testHashCode() {
        when:
        Material mate1 = new Material(Paths.get('.'), new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        Material mate2 = new Material(Paths.get('.'), new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        then:
        mate1.hashCode() == mate2.hashCode()
        when:
        Material mate3 = new Material(Paths.get('.'), new URL('https://www.google.com/'), new Suffix(1), FileType.PNG).setParent(tcr_)
        then:
        mate1.hashCode() != mate3.hashCode()
    }


    def testHashCodeWithAncestors() {
        setup:
        TSuiteResult tsr1 = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS1'), new TSuiteTimestamp('20180530_130419'))
        TSuiteResult tsr2 = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS2'), new TSuiteTimestamp('20180612_111256'))
        TCaseResult tcr1 = tsr1.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        TCaseResult tcr2 = tsr2.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        when:
        Material mate1 = new Material(Paths.get('.'), new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr1)
        Material mate2 = new Material(Paths.get('.'), new URL('https://www.google.com/'), Suffix.NULL, FileType.PNG).setParent(tcr2)
        logger_.debug("#testHashCodeWithAncestors mate1.hashCode()=${mate1.hashCode()}")
        logger_.debug("#testHashCodeWithAncestors mate2.hashCode()=${mate2.hashCode()}")
        then:
        // Path determines the hashCode value.
        mate1.hashCode() != mate2.hashCode()
    }

    def testMarkupInModalWindow_PNG() {
        setup:
        Material mate = new Material(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG).setParent(tcr_)
        when:
        String markup = mate.markupInModalWindow()
        logger_.debug("#testMarkupInModalWindow_png markup=\n${markup}")
        then:
        markup.contains('<img')
        markup.contains('class="img-fluid"')
        markup.contains(FileType.PNG.getExtension())
    }

    def testSetGetLastModified_long() {
        setup:
        Material mate = new Material(Paths.get('.'), new URL('http://demoaut.katalon.com/'), new Suffix(3), FileType.PNG).setParent(tcr_)
        LocalDateTime ldtNow = LocalDateTime.now()
        Instant instantNow = ldtNow.toInstant(ZoneOffset.UTC)
        long longNow = instantNow.toEpochMilli()
        when:
        mate.setLastModified(longNow)
        then:
        mate.getLastModified() == ldtNow
    }

    def testSetParent_GetParent() {
        when:
        Material mate = new Material(Paths.get('.'), new URL('http://demoaut.katalon.com/'), new Suffix(2), FileType.PNG)
        Material modified = mate.setParent(tcr_)
        then:
        modified.getParent() == tcr_
    }

    def testToBootstrapTreeviewData() {
        when:
        Material mate = tcr_.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        def str = mate.toBootstrapTreeviewData()
        //System.out.println("#testToJson:\n${JsonOutput.prettyPrint(str)}")
        then:
        str.startsWith('{"text":"')
        str.contains('http://demoaut.katalon.com/')
        str.endsWith('"}')
    }

    def testToHtmlAsModalWindow_PNG() {
        when:
        Material mate = tcr_.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        String str = mate.toHtmlAsModalWindow()
        logger_.debug("#testToHtmlAsModalWindow str=${str}")
        //Node node = new XmlParser().parseText(str)
        //logger_.debug("#testToHtmlAsModalWindow str parsed as XML =${XmlUtil.serialize(node)}")
        then:
        str.startsWith('<div')
        str.contains('<img')
        str.contains(mate.getEncodedHrefRelativeToRepositoryRoot())
    }


    def testToHtmlAsModalWindow_miscellaneousImages() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS1'), new TSuiteTimestamp('20180530_130604'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        assert tcr != null
         //
        expect:
        tcr.getMaterials().size() == 5
        when:
        Material mate = tcr.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        String str = mate.toHtmlAsModalWindow()
        then:
        str.contains('<img')
        str.contains('.png')
        //
        when:
        mate = tcr.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.BMP)
        str = mate.toHtmlAsModalWindow()
        then:
        str.contains('<img')
        str.contains('.bmp')
        //
        when:
        mate = tcr.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.GIF)
        str = mate.toHtmlAsModalWindow()
        then:
        str.contains('<img')
        str.contains('.gif')
        //
        when:
        mate = tcr.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.JPEG)
        str = mate.toHtmlAsModalWindow()
        then:
        str.contains('<img')
        str.contains('.jpeg')
        //
        when:
        mate = tcr.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.JPG)
        str = mate.toHtmlAsModalWindow()
        then:
        str.contains('<img')
        str.contains('.jpg')
        //
    }

    def testToHtmlAsModalWindow_CSV() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS3'), new TSuiteTimestamp('20180627_140853'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC3'))
        assert tcr != null
        //
        when:
        String url = 'https://fixturedownload.com/download/csv/fifa-world-cup-2018/japan'
        Material mate = tcr.getMaterial(Paths.get('.'), new URL(url), Suffix.NULL, FileType.CSV)
        String str = mate.toHtmlAsModalWindow()
        then:
        str.contains('3,28/06/2018&nbsp;17:00,Volgograd&nbsp;Stadium,Japan,Poland,Group&nbsp;H,')
    }

    def testToHtmlAsModalWindow_PDF() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS3'), new TSuiteTimestamp('20180627_140853'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC3'))
        assert tcr != null
        //
        when:
        String url = 'http://files.shareholder.com/downloads/AAPL/6323171818x0xS320193-17-70/320193/filing.pdf'
        Material mate = tcr.getMaterial(Paths.get('.'), new URL(url), Suffix.NULL, FileType.PDF)
        String str = mate.toHtmlAsModalWindow()
        then:
        str.contains('<object')
        str.contains('type="application/pdf"')
    }

    def testToHtmlAsModalWindow_XLSX() {
        setup:
        TSuiteResult tsr = repoRoot_.getTSuiteResult(new TSuiteName('Test Suites/main/TS3'), new TSuiteTimestamp('20180627_140853'))
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC3'))
        assert tcr != null
        //
        when:
        String url = 'https://fixturedownload.com/download/xlsx/fifa-world-cup-2018/japan'
        Material mate = tcr.getMaterial(Paths.get('.'), new URL(url), Suffix.NULL, FileType.XLSX)
        String str = mate.toHtmlAsModalWindow()
        logger_.debug("#testToHtmlAsModalWindow_XLSX str=${str}")
        then:
        str != null
    }

    def testToJson() {
        when:
        Material mate = tcr_.getMaterial(Paths.get('.'), new URL('http://demoaut.katalon.com/'), Suffix.NULL, FileType.PNG)
        def str = mate.toString()
        //System.out.println("#testToJson:\n${JsonOutput.prettyPrint(str)}")
        then:
        str.startsWith('{"Material":{"url":"')
        str.contains('"suffix":')
        str.contains('"path":')
        str.contains(Helpers.escapeAsJsonText(mate.getPath().toString()))
        str.contains('"fileType":')
        str.endsWith('"}}')
    }

}
