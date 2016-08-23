package com.dreamburst.sed;

import com.dreamburst.sed.tests.BatchDispatcherTests;
import com.dreamburst.sed.tests.DirectDispatcherTests;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public final class TestRunner {

    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(DirectDispatcherTests.class, BatchDispatcherTests.class);
        result.getFailures().forEach(System.out::println);
    }

}
