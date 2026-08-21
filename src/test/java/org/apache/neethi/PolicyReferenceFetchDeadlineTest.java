/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.neethi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

/**
 * The JDK read timeout applies to each blocking read individually, so a
 * trickle server (one byte per interval, forever) never trips it and never
 * reaches EOF or the byte cap: the resolving thread used to block without
 * bound. readBounded must enforce a total wall-clock deadline per fetch.
 */
public class PolicyReferenceFetchDeadlineTest extends PolicyTestCase {

    @Test
    public void testExpiredDeadlineAbortsBeforeReading() throws IOException {
        InputStream neverEnding = new ByteArrayInputStream(new byte[1024]);
        long expiredDeadline = System.nanoTime() - 1;

        try {
            PolicyReference.readBounded(neverEnding, 1024L * 1024L, expiredDeadline, 0);
            fail("Expected RuntimeException due to total fetch deadline");
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().contains("maximum total fetch time"));
        }
    }

    @Test
    public void testTrickleStreamIsCutOffAtDeadline() throws IOException {
        InputStream trickle = new InputStream() {
            @Override
            public int read() throws IOException {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException(e);
                }
                return 'x';
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                b[off] = (byte) read();
                return 1;
            }
        };
        long deadline = System.nanoTime() + 100L * 1_000_000L;

        try {
            PolicyReference.readBounded(trickle, Long.MAX_VALUE, deadline, 100);
            fail("Expected RuntimeException due to total fetch deadline");
        } catch (RuntimeException ex) {
            assertTrue(ex.getMessage().contains("maximum total fetch time"));
        }
    }

    @Test
    public void testFastStreamWithinDeadlineIsReadFully() throws IOException {
        byte[] payload = "policy-bytes".getBytes("UTF-8");
        long deadline = System.nanoTime() + 10_000L * 1_000_000L;

        byte[] read = PolicyReference.readBounded(
            new ByteArrayInputStream(payload), 1024L, deadline, 10_000);

        assertEquals(payload.length, read.length);
    }
}