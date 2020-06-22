/**
 * Copyright (c) 2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at:
 *
 *     https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.jkube.kit.common.util;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LazyBuilderTest {

  @Test
  public void getShouldInvokeSupplierOnce() {
    // Given
    final AtomicInteger count = new AtomicInteger(0);
    final Supplier<Integer> build = () -> {
      count.incrementAndGet();
      return 1;
    };
    final LazyBuilder<Integer> lazyBuilder = new LazyBuilder<>(build);
    // When
    final int result = IntStream.rangeClosed(1, 10).map(t -> lazyBuilder.get()).sum();
    // Then
    assertThat(result, is(10));
    assertThat(count.get(), is(1));
  }

  @Test
  public void getConcurrentShouldInvokeSupplierTwice() throws Exception {
    // Given
    final AtomicInteger count = new AtomicInteger(0);
    final CountDownLatch cdl = new CountDownLatch(1);
    final Supplier<Integer> build = () -> {
      try {
        if (count.incrementAndGet() == 1) {
          cdl.await(100, TimeUnit.MILLISECONDS);
          return 1337; // This value should be ignored, value set by main thread should be preferred in LazyBuilder
        }
      } catch (InterruptedException ignored) {}
      return 1;
    };
    final LazyBuilder<Integer> lazyBuilder = new LazyBuilder<>(build);
    final ExecutorService es = Executors.newSingleThreadExecutor();
    final Future<Integer> concurrentResult = es.submit(lazyBuilder::get);
    // When
    final int result = IntStream.rangeClosed(1, 10).map(t -> lazyBuilder.get()).sum();
    cdl.countDown();
    // Then
    assertThat(count.get(), is(2));
    assertThat(result, is(10));
    assertThat(concurrentResult.get(100, TimeUnit.MILLISECONDS), is(1));
  }
}
