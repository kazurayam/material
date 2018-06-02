package com.kazurayam.ksbackyard.screenshotsupport

import groovy.json.JsonBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAccessor

/**
 * Timestamp of a Test Suite
 *
 * @author kazurayam
 *
 */
class TestSuiteTimestamp {

    private LocalDateTime timestamp

    static String DATE_TIME_PATTERN = 'yyyyMMdd_HHmmss'

    /**
     * create a Timestamp object based on the LocalDateTime of now
     */
    TestSuiteTimestamp() {
        this(LocalDateTime.now())
    }

    /**
     * instanciate a Timestamp object while ignoring milliseconds
     *
     * @param ts
     */
    TestSuiteTimestamp(LocalDateTime ts) {
        this.timestamp = LocalDateTime.of(ts.getYear(), ts.getMonth(), ts.getDayOfMonth(),
            ts.getHour(), ts.getMinute(), ts.getSecond())  // ignore milliseconds
    }

    LocalDateTime getValue() {
        return this.timestamp
    }

    String format() {
        return DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).format(timestamp)
    }

    static LocalDateTime parse(String str) {
        try {
            TemporalAccessor parsed = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).parse(str)
            return LocalDateTime.from(parsed)
        } catch (DateTimeParseException ex) {
            System.err.println("unable to parse '${str}' as LocalDateTime")
            return null
        }
    }

    // ---------------- overriding Object properties --------------------------
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true
        if (!(obj instanceof TestSuiteTimestamp))
            return false
        TestSuiteTimestamp other = (TestSuiteTimestamp)obj
        return this.getValue() == other.getValue()
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode()
    }

    @Override
    String toString() {
        def json = new JsonBuilder()
        json (
               ["timestamp": this.format() ]
        )
        return json.toString()
    }

}
