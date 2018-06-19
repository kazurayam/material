package com.kazurayam.carmina

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class TestMaterialsRepositoryImpl implements TestMaterialsRepository {

    static Logger logger_ = LoggerFactory.getLogger(TestMaterialsRepositoryImpl.class)

    private Path baseDir_
    private TSuiteName currentTSuiteName_
    private TSuiteTimestamp currentTSuiteTimestamp_
    private List<TSuiteResult> tSuiteResults_

    static final String IMAGE_FILE_EXTENSION = '.png'

    // ---------------------- constructors & initializer ----------------------

    /**
     *
     * @param baseDir required
     * @param tsName required
     * @param tsTimestamp required
     */
    TestMaterialsRepositoryImpl(Path baseDir) {
        //
        if (!baseDir.toFile().exists()) {
            throw new IllegalArgumentException("${baseDir} does not exist")
        }
        baseDir_ = baseDir
        Helpers.ensureDirs(baseDir_)

        // load data from the local disk
        RepositoryScanner scanner = new RepositoryScanner(baseDir_)
        scanner.scan()
        tSuiteResults_ = scanner.getTSuiteResults()

        // set default Material path to the "./${baseDir name}/_/_" directory
        this.setCurrentTestSuite(TSuiteName.SUITELESS, TSuiteTimestamp.TIMELESS)
    }

    /**
     * The current time now 'now' is assumed
     *
     * @param testSuiteId
     */
    @Override
    void setCurrentTestSuite(String testSuiteId) {
        this.setCurrentTestSuite(
                testSuiteId,
                Helpers.now())
    }

    @Override
    void setCurrentTestSuite(String testSuiteId, String testSuiteTimestampString) {
        this.setCurrentTSuiteResult(
                new TSuiteName(testSuiteId),
                new TSuiteTimestamp(testSuiteTimestampString))
    }

    void setCurrentTestSuite(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        this.setCurrentTSuiteResult(
                tSuiteName,
                tSuiteTimestamp)
    }

    void setCurrentTSuiteResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        // memorize the specified TestSuite
        currentTSuiteName_ = tSuiteName
        currentTSuiteTimestamp_ = tSuiteTimestamp

        // add the specified TestSuite
        TSuiteResult tsr = this.getTSuiteResult(currentTSuiteName_, currentTSuiteTimestamp_)
        if (tsr == null) {
            tsr = new TSuiteResult(tSuiteName, tSuiteTimestamp).setParent(baseDir_)
        }
        this.addTSuiteResult(tsr)
    }

    @Override
    Path getCurrentTestSuiteDirectory() {
        TSuiteResult tsr = this.getTSuiteResult(currentTSuiteName_, currentTSuiteTimestamp_)
        if (tsr != null) {
            return tsr.getTSuiteTimestampDirectory()
        }
        return null
    }

    // -------------------------- attribute getters & setters ------------------------
    @Override
    Path getBaseDir() {
        return baseDir_
    }

    TSuiteName getCurrentTSuiteName() {
        return currentTSuiteName_
    }

    TSuiteTimestamp getCurrentTSuiteTimestamp() {
        return currentTSuiteTimestamp_
    }

    // --------------------- create/add/get child nodes -----------------------

    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    void addTSuiteResult(TSuiteResult tSuiteResult) {
        boolean found = false
        for (TSuiteResult tsr : tSuiteResults_) {
            if (tsr == tSuiteResult) {
                found = true
            }
        }
        if (!found) {
            tSuiteResults_.add(tSuiteResult)
        }
    }

    /**
     *
     * @param testSuiteId
     * @param timestamp
     * @return
     */
    TSuiteResult getTSuiteResult(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
        for (TSuiteResult tsr : tSuiteResults_) {
            if (tsr.getTSuiteName() == tSuiteName && tsr.getTSuiteTimestamp() == tSuiteTimestamp) {
                return tsr
            }
        }
        return null
    }

    // -------------------------- do the business -----------------------------
    @Override
    Path resolveMaterial(String testCaseName, String url, FileType fileType) {
        return this.resolveMaterial(
                new TCaseName(testCaseName),
                new URL(url),
                Suffix.NULL,
                fileType)
    }

    @Override
    Path resolveMaterial(String testCaseName, String url, String suffix, FileType fileType) {
        return this.resolveMaterial(
                new TCaseName(testCaseName),
                new URL(url),
                new Suffix(suffix),
                fileType)
    }

    /**
     * This is the core value of the Carmina package.
     *
     * @param tCaseName
     * @param url
     * @param postFix
     * @return
     */
    Path resolveMaterial(TCaseName tCaseName, URL url, Suffix suffix, FileType fileType) {
        TSuiteResult tSuiteResult = getCurrentTSuiteResult()
        if (tSuiteResult == null) {
            logger_.error("tSuiteResult is null")
        }
        TCaseResult tCaseResult = tSuiteResult.getTCaseResult(tCaseName)
        if (tCaseResult == null) {
            tCaseResult = new TCaseResult(tCaseName).setParent(tSuiteResult)
            tSuiteResult.addTCaseResult(tCaseResult)
        }
        TargetURL targetURL = tCaseResult.getTargetURL(url)
        if (targetURL == null) {
            targetURL = new TargetURL(url).setParent(tCaseResult)
            tCaseResult.getTargetURLs().add(targetURL)
        }
        Material material = targetURL.getMaterial(suffix, fileType)
        if (material == null) {
            String fileName = Material.resolveMaterialFileName(url, suffix, fileType)
            Path materialPath = tCaseResult.getTCaseDirectory().resolve(fileName)
            material = new Material(materialPath, fileType).setParent(targetURL)

            // Here we create the parent directory for the material
            Helpers.ensureDirs(materialPath.getParent())
        }
        return material.getMaterialFilePath()
    }

    /**
     * create a Result.html file under the directory ${baseDir}/${Test Suite name}/${Test Suite timestamp}/
     * The Result.html file is an index to the Material files created by the TestResultsImpl at this time of execution
     *
     * @returns Path of the Results.html file
     */
    @Override
    Path makeIndex() throws IOException {
        // reload the latest Test Results Repository from the local disk
        RepositoryScanner scanner = new RepositoryScanner(baseDir_)
        scanner.scan()
        tSuiteResults_ = scanner.getTSuiteResults()
        //
        if (currentTSuiteName_ != null && currentTSuiteTimestamp_ != null) {
            List<TSuiteResult> tsrList =
                tSuiteResults_.stream()
                    .filter({tsr -> tsr.getTSuiteName() == currentTSuiteName && tsr.getTSuiteTimestamp() == currentTSuiteTimestamp })
                    .collect(Collectors.toList())
            if (tsrList.size() > 0) {
                TSuiteResult tsr = tsrList[0]
                Path html = tsr.getTSuiteTimestampDirectory().resolve("Result.html")
                Helpers.ensureDirs(tsr.getTSuiteTimestampDirectory())
                //
                Indexer.makeIndex(tsr, Files.newOutputStream(html))
                return html
            }
            return null
        }
        return null
    }


    // ----------------------------- helpers ----------------------------------

    TSuiteResult getCurrentTSuiteResult() {
        if (currentTSuiteName_ != null) {
            if (currentTSuiteTimestamp_ != null) {
                TSuiteResult tsr = getTSuiteResult(currentTSuiteName_, currentTSuiteTimestamp_)
                return tsr
            } else {
                throw new IllegalStateException('currentTSuiteTimestamp is not set')
            }
        } else {
            throw new IllegalStateException('currentTSuiteName is not set')
        }
    }

    TCaseResult getTCaseResult(String testCaseId) {
        return this.getTCaseResult(new TCaseName(testCaseId))
    }

    TCaseResult getTCaseResult(TCaseName tCaseName) {
        if (tCaseName != null) {
            TSuiteResult tsr = this.getCurrentTSuiteResult()
            return tsr.getTCaseResult(tCaseName)
        }
        else {
            throw new IllegalStateException("currentTcName is null")
        }
    }

    @Override
    Path getTestCaseDirectory(String testCaseId) {
        return this.getTCaseResult(testCaseId).getTCaseDirectory()
    }

    // ---------------------- overriding Object properties --------------------
    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"TestResultsImpl":{')
        sb.append('"baseDir":"' +
            Helpers.escapeAsJsonText(baseDir_.toString()) + '",')
        sb.append('"currentTsName":"' +
            Helpers.escapeAsJsonText(currentTSuiteName_.toString()) + '",')
        sb.append('"currentTsTimestamp":"' +
            Helpers.escapeAsJsonText(currentTSuiteTimestamp_.toString()) + '",')
        sb.append('"tsResults":[')
        def counter = 0
        for (TSuiteResult tsr : tSuiteResults_) {
            if (counter > 0) { sb.append(',') }
            sb.append(tsr.toJson())
            counter += 1
        }
        sb.append(']')
        sb.append('}}')
        return sb.toString()
    }
}