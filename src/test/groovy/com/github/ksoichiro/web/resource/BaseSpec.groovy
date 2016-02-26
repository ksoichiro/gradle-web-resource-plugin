package com.github.ksoichiro.web.resource

import spock.lang.Specification

class BaseSpec extends Specification {
    public static final String PLUGIN_ID = 'com.github.ksoichiro.web.resource'

    def setupSpec() {
        printRuntimeInfo "test spec started "
    }

    def cleanupSpec() {
        printRuntimeInfo "test spec finished"
    }

    def setup() {
        printRuntimeInfo "test case started "
    }

    def cleanup() {
        printRuntimeInfo "test case finished"
    }

    def printRuntimeInfo(def tag) {
        if (System.getenv().CI) {
            def ap = Runtime.runtime.availableProcessors()
            def fm = (int) (Runtime.runtime.freeMemory()/1024/1024)
            def mm = (int) (Runtime.runtime.maxMemory()/1024/1024)
            def tm = (int) (Runtime.runtime.totalMemory()/1024/1024)
            println "${tag}: availableProcessors: ${ap}, freeMemory: ${fm}MB, maxMemory: ${mm}MB, totalMemory: ${tm}MB"
        }
    }
}
