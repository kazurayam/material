package com.kazurayam.ksbackyard.screenshotsupport

import groovy.json.JsonBuilder

import java.nio.file.Path

class ScreenshotWrapper {

    private TargetPage parentTargetPage
    private Path screenshotFilePath

    ScreenshotWrapper(TargetPage parent, Path imageFilePath) {
        this.parentTargetPage = parent
        this.screenshotFilePath = imageFilePath
    }

    TargetPage getTargetPage() {
        return parentTargetPage
    }

    Path getScreenshotFilePath() {
        return screenshotFilePath
    }

    // ---------------- overriding Object properties --------------------------
    @Override
    boolean equals(Object obj) {
        if (this == obj) { return true }
        if (!(obj instanceof ScreenshotWrapper)) { return false }
        ScreenshotWrapper other = (ScreenshotWrapper)obj
        if (this.screenshotFilePath == other.getScreenshotFilePath()) {
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        final int prime = 31
        int result = 1
        result = prime * result + this.getTargetPage().hashCode()
        result = prime * result + this.getScreenshotFilePath().hashCode()
        return result
    }

    @Override
    String toString() {
        def json = new JsonBuilder()
        json (
                ["screenshotFilePath" : this.screenshotFilePath.toString()]
        )
        return json.toString()
    }
}
