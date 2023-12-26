/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.test;

/* ====================================================================
   Taken from Apache POI, with a big thanks.

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple utility class that can verify that objects have been successfully garbage collected.
 *
 * Usage is something like
 *
 *  private final MemoryLeakVerifier verifier = new MemoryLeakVerifier();

 {@literal}After
 void tearDown() {
 verifier.assertGarbageCollected();
 }

 {@literal}Test
 void someTest() {
 ...
 verifier.addObject(object);
 }

 *
 * This will verify at the end of the test if the object is actually removed by the
 * garbage collector or if it lingers in memory for some reason.
 *
 * Idea taken from http://stackoverflow.com/a/7410460/411846
 */
public class MemoryLeakVerifier {
    private static final int MAX_GC_ITERATIONS = 50;
    private static final int GC_SLEEP_TIME     = 100;

    private final List<WeakReference<Object>> references = new ArrayList<>();

    public void addObject(Object object) {
        references.add(new WeakReference<>(object));
    }

    /**
     * Attempts to perform a full garbage collection so that all weak references will be removed. Usually only
     * a single GC is required, but there have been situations where some unused memory is not cleared up on the
     * first pass. This method performs a full garbage collection and then validates that the weak reference
     * now has been cleared. If it hasn't then the thread will sleep for 100 milliseconds and then retry up to
     * 50 more times. If after this the object still has not been collected then the assertion will fail.
     *
     * Based upon the method described in: http://www.javaworld.com/javaworld/javatips/jw-javatip130.html
     */
    public void assertGarbageCollected() {
        assertGarbageCollected(MAX_GC_ITERATIONS);
    }

    /**
     * Used only for testing the class itself where we would like to fail faster than 5 seconds
     * @param maxIterations The number of times a GC will be invoked until a possible memory leak is reported
     */
    void assertGarbageCollected(int maxIterations) {
        try {
            for (WeakReference<Object> ref : references) {
                assertGarbageCollected(ref, maxIterations);
            }
        } catch (InterruptedException e) {
            // just ensure that we quickly return when the thread is interrupted
        }
    }

    private static void assertGarbageCollected(WeakReference<Object> ref, int maxIterations) throws InterruptedException {
        Runtime runtime = Runtime.getRuntime();
        for (int i = 0; i < maxIterations; i++) {
            runtime.runFinalization();
            runtime.gc();
            if (ref == null || ref.get() == null) {
                break;
            }

            // Pause for a while and then go back around the loop to try again...
            //EventQueue.invokeAndWait(Procedure.NoOp); // Wait for the AWT event queue to have completed processing
            Thread.sleep(GC_SLEEP_TIME);
        }

        assertNull(ref.get(), "Object should not exist after " + MAX_GC_ITERATIONS + " collections, but still had: " + ref.get());
    }
}

