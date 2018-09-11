package com.kazurayam.materials

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Ignore
import spock.lang.Specification

//@Ignore
class MaterialRepositoryImplSpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(MaterialRepositoryImplSpec.class)

    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static Path materials_ = fixture_.resolve('Materials')
    private static String classShortName_ = Helpers.getClassShortName(MaterialRepositoryImplSpec.class)

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${classShortName_}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testGetBaseDir() {
        setup:
        Path casedir = workdir_.resolve('testGetBaseDir')
        Helpers.copyDirectory(materials_, casedir)
        when:
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(casedir)
        then:
        mri.getBaseDir() == casedir
    }


    def testResolveMaterialPath() {
        setup:
        def methodName ='testResolveMaterialPath'
        Path casedir = workdir_.resolve(methodName)
        Helpers.copyDirectory(materials_, casedir)
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(casedir)
        mri.putCurrentTestSuite('TS1', '20180530_130604')
        when:
        String materialFileName = MaterialFileName.format(
            new URL('http://demoaut.katalon.com/'),
            Suffix.NULL,
            FileType.PNG)
        Path p = mri.resolveMaterialPath('TC1', materialFileName)
        then:
        p != null
        p.toString().replace('\\', '/') ==
            "build/tmp/${classShortName_}/${methodName}/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
    }

    def testResolveMaterialPath_withSuffix() {
        setup:
        def methodName = 'testResolveMaterialPath_withSuffix'
        Path casedir = workdir_.resolve(methodName)
        Helpers.copyDirectory(materials_, casedir)
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(casedir)
        mri.putCurrentTestSuite('TS1', '20180530_130604')
        when:
        String materialFileName = MaterialFileName.format(
            new URL('http://demoaut.katalon.com/'),
            new Suffix(1),
            FileType.PNG)
        Path p = mri.resolveMaterialPath('TC1', materialFileName)
        then:
        p != null
        p.toString().replace('\\', '/') ==
            "build/tmp/${classShortName_}/${methodName}/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png"
    }

    def testResolveMaterialPath_new() {
        setup:
        def methodName = 'testResolveMaterialPath_new'
        Path casedir = workdir_.resolve(methodName)
        Helpers.copyDirectory(materials_, casedir)
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(casedir)
        mri.putCurrentTestSuite('TS3', '20180614_152000')
        when:
        String materialFileName = MaterialFileName.format(new URL('http://demoaut.katalon.com/'),
            Suffix.NULL,
            FileType.PNG)
        Path p = mri.resolveMaterialPath('TC1', materialFileName)
        then:
        p != null
        p.toString().replace('\\', '/') ==
            "build/tmp/${classShortName_}/${methodName}/TS3/20180614_152000/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
        Files.exists(p.getParent())
    }

    def testResolveMaterialPath_withSuffix_new() {
        setup:
        def methodName = 'testResolveMaterialPath_withSuffix_new'
        Path casedir = workdir_.resolve(methodName)
        Helpers.copyDirectory(materials_, casedir)
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(casedir)
        mri.putCurrentTestSuite('TS3', '20180614_152000')
        when:
        String materialFileName = MaterialFileName.format(new URL('http://demoaut.katalon.com/'),
            new Suffix(1),
            FileType.PNG)
        Path p = mri.resolveMaterialPath('TC1', materialFileName)
        then:
        p != null
        p.toString().replace('\\', '/') ==
            "build/tmp/${classShortName_}/${methodName}/TS3/20180614_152000/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png"
        Files.exists(p.getParent())
    }

    def testResolveMaterial_png_SuitelessTimeless() {
        setup:
        def methodName = 'testResolveMaterial_png_SuitelessTimeless'
        Path casedir = workdir_.resolve(methodName)
        Helpers.copyDirectory(materials_, casedir)
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(casedir)
        mri.putCurrentTestSuite(TSuiteName.SUITELESS, TSuiteTimestamp.TIMELESS)
        when:
        String materialFileName = MaterialFileName.format(new URL('http://demoaut.katalon.com/'), new Suffix(1), FileType.PNG)
        Path p = mri.resolveMaterialPath('TC1', materialFileName)
        then:
        p != null
        p.toString().replace('\\', '/') == "build/tmp/${classShortName_}/${methodName}/_/_/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png"
    }

    def testToJson() {
        setup:
        Path casedir = workdir_.resolve('testToJson')
        Helpers.copyDirectory(materials_, casedir)
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(casedir)
        mri.putCurrentTestSuite('TS1')
        when:
        def str = mri.toJson()
        then:
        str != null
        str.contains('{"MaterialRepositoryImpl":{')
        str.contains(Helpers.escapeAsJsonText(casedir.toString()))
        str.contains('}}')
    }


    def testGetRecentMaterialPairs() {
        setup:
        def methodName = "testGetRecentMaterialPairs"
        Path casedir = workdir_.resolve(methodName)
        Helpers.copyDirectory(fixture_, casedir)
        Path materials = casedir.resolve('Materials')
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(materials)
        when:
        List<MaterialPair> list = mri.getRecentMaterialPairs(
            'product', 'demo', 'TS1')
        then:
        list.size() == 1
        when:
        MaterialPair mp = list.get(0)
        Material expected = mp.getExpected()
        Material actual = mp.getActual()
        then:
        expected.getPathRelativeToTSuiteTimestamp() == Paths.get('TC1/CURA_Healthcare_Service.png')
        actual.getPathRelativeToTSuiteTimestamp()   == Paths.get('TC1/CURA_Healthcare_Service.png')
    }


    /**
     * 2018/08/29 Tried to reproduce a problem in another project.
     *
     * @return
     */
    @Ignore
    def test_getRecentMaterialsPairs_reproducingProblem() {
        setup:
        def methodName = "test_getRecentMaterialsPairs_reproducingProblem"
        Path problematicFixture = Paths.get("C:\\Users\\qcq0264\\katalon-workspace\\Q-FNHP-ImageDiff")
        Path casedir = workdir_.resolve(methodName)
        Path materials = casedir.resolve("Materials")
        Path reports = casedir.resolve("Reports")
        Files.createDirectories(materials)
        Files.createDirectories(reports)
        Helpers.copyDirectory(problematicFixture.resolve('Materials'), materials)
        Helpers.copyDirectory(problematicFixture.resolve('Reports'), reports)
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(materials)
        when:
        List<MaterialPair> list = mri.getRecentMaterialPairs(
            'product', 'develop', 'AllCorps')
        then:
        list.size() > 0
    }


}