/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package decorators;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qubership.atp.integration.configuration.decorators.MdcTaskDecorator;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class MdcTaskDecoratorTest {

    /**
     * ThreadPoolTaskExecutor object for tests.
     */
    private ThreadPoolTaskExecutor executor;

    /**
     * Initialize executor for tests.
     */
    @Before
    public void init() {
        executor = new ThreadPoolTaskExecutor();
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
    }

    /**
     * Test that MDC is not copied to new thread in case executor uses thread pool without MdcDecorator.
     *
     * @throws ExecutionException in case the corresponding exception is thrown during future processing
     * @throws InterruptedException in case the corresponding exception is thrown during future processing.
     */
    @Test
    public void testMdcTaskDecoratorThreadPoolWithoutMdcDecoratorTaskMdcContextWasNotCopiedForNewThread()
            throws ExecutionException, InterruptedException {
        submitAndGetFuture(); // It implicitly relies on the sequence of tests execution.
    }

    /**
     * Test that MDC is copied to new thread in case executor uses thread pool with MdcDecorator.
     *
     * @throws ExecutionException in case the corresponding exception is thrown during future processing
     * @throws InterruptedException in case the corresponding exception is thrown during future processing.
     */
    @Test
    public void testMdcTaskDecoratorThreadPoolWithMdcDecoratorTaskMdcContextWasCopiedForNewThread()
            throws ExecutionException, InterruptedException {
        executor.setTaskDecorator(new MdcTaskDecorator());
        submitAndGetFuture();
    }

    private void submitAndGetFuture() throws ExecutionException, InterruptedException {
        MDC.put("projectId", "123");
        Future<?> future = executor.submit((Callable<Void>) () -> {
            Assert.assertNull(MDC.get("projectId"));
            return null;
        });
        future.get();
    }

}
