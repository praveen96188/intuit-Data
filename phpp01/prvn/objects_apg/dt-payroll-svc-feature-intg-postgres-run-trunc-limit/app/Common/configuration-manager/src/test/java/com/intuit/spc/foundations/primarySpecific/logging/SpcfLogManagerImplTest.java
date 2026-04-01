package com.intuit.spc.foundations.primarySpecific.logging;

import com.intuit.spc.foundations.portability.threading.SpcfThread;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SpcfLogManagerImpl.class, SpcfLogManager.class, SpcfThread.class})
public class SpcfLogManagerImplTest {


    static final long DELAY = 4_000;
    static final long EVENT_COUNT = 2_000;

    @Before
    public void runBeforeEachTest() {
        System.out.println("System Logger: START");

        // by default log4j2-test.xml is loaded, if log4j2.xml is not present in classpath
        // org.apache.logging.log4j.core.config.Configurator.initialize(null, "log4j2-test.xml");
    }

    @After
    public void runAfterEachTest() {
        System.out.println("System Logger: END");
    }

    @Test
    public void testNOPLogger() throws Exception {

        // setup mock
        mockStatic(SpcfThread.class);
        SpcfLogManagerImpl spcfLogManagerImpl = new SpcfLogManagerImpl();
        PowerMockito.whenNew(SpcfLogManagerImpl.class).withNoArguments().thenReturn(spcfLogManagerImpl);
        doThrow(new RuntimeException()).when(
                SpcfThread.class, "initializeUncaughtExceptionHandler", spcfLogManagerImpl);

        // override System.out
        String file = "/tmp/" + UUID.randomUUID();
        PrintStream original = System.out;
        try (OutputStream output = new FileOutputStream(file);
             PrintStream printOut = new PrintStream(output)) {

            System.setOut(printOut);

            // init no-op logger
            SpcfLogger nopLogger = SpcfLogManager.getLogger(SpcfLogManagerImplTest.class);
            Assert.assertNotNull(nopLogger);
            String doNotLogMe = "doNotLogMe";
            nopLogger.info(doNotLogMe);

            // read from printStream
            String content = headFile(file);

            // assert no-op logger is invoked
            Assert.assertTrue(content.contains(SpcfLogManager.NOP_LOGGER_INVOKE_MESSAGE));
            Assert.assertFalse(content.contains(doNotLogMe));

        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void testLogConfigInit() {
        SpcfLogger logger = SpcfLogManager.getLogger("test-log-02");
        Assert.assertNotNull(logger);
        Assert.assertTrue(logger instanceof SpcfLoggerImpl);
    }

    @Ignore
    public void testLogLevel() {
        SpcfLogger logger1 = SpcfLogManager.getLogger(SpcfLogManagerImplTest.class);
        SpcfLogger logger2 = SpcfLogManager.getLogger("test-log-02");

        logger1.trace("Test logger1 - trace");
        logger1.debug("Test logger1 - debug");
        logger1.info("Test logger1 - info");
        logger1.warn("Test logger1 - warn");
        logger1.error("Test logger1 - error");
        logger1.fatal("Test logger1 - fatal");

        logger2.trace("Test logger2 - warn");
        logger2.debug("Test logger2 - debug");
        logger2.info("Test logger2 - info");
        logger2.warn("Test logger2 - warn");
        logger2.error("Test logger2 - error");
        logger2.fatal("Test logger2 - fatal");

        // assert log levels
        Assert.assertFalse(logger1.isDebugEnabled());
        Assert.assertTrue(logger2.isDebugEnabled());
    }

    /*@Test
    public void testConsoleLogs() throws IOException {
        org.apache.logging.log4j.core.config.Configurator.initialize(null, "log4j2-console.xml");
        String file = "/tmp/" + UUID.randomUUID();
        String logLine = "Test logger - info";
        PrintStream original = System.out;

        try (OutputStream output = new FileOutputStream(file);
             PrintStream printOut = new PrintStream(output)) {

            // 1. override system output stream
            System.setOut(printOut);

            // 2. log to console
            SpcfLogger logger = SpcfLogManager.getLogger(SpcfLogManagerImplTest.class);
            logger.info(logLine);

        } finally {
            System.setOut(original);
        }

        // 3. read logged content & validate
        String content = IOUtils.toString(new FileReader(file));
        System.out.println(file);
        Assert.assertTrue(content.contains(logLine));

        // 4. tear down
        System.out.println("Temp file delete status=" + new File(file).delete());

        System.out.println("please report if this is not printed to console!");
    }*/

    @Test
    public void testSystemClock() {
        long start = 0, end = 0;
        start = System.nanoTime();
        long millis = 1_000;

        sleep(millis);
        end = System.nanoTime();
        long timeTaken = end - start;

        // validate System nano clock precision with 1% variance
        long millisToNanos = millis * 1_000_000;
        BigDecimal lowRange = new BigDecimal(millisToNanos).subtract(new BigDecimal(millisToNanos / 100));
        BigDecimal highRange = new BigDecimal(millisToNanos).add(new BigDecimal(millisToNanos / 100));

        BigDecimal timeTakenBig = new BigDecimal(timeTaken);
        Assert.assertEquals(1, timeTakenBig.compareTo(lowRange));
        Assert.assertEquals(-1, timeTakenBig.compareTo(highRange));

        System.out.println(timeTakenBig);
        System.out.println(lowRange);
        System.out.println(highRange);
    }

    @Ignore
    public void testSyncLoggerPerfSingleThread() throws IOException {
        loggerPerfSingleThread(false);
    }

    @Ignore
    public void testAsyncLoggerPerfSingleThread() throws IOException {
        loggerPerfSingleThread(true);
    }

    @Ignore
    public void testSyncLoggerPerfMultiThread() throws IOException {
        perfHarness(false);
    }

    @Ignore
    public void testAsyncLoggerPerfMultiThread() throws IOException {
        perfHarness(true);
    }

    private void perfHarness(boolean async) throws IOException {
        int[] threadCounts = new int[]{1, 4, 10, 40};
        System.out.println("async,threadCount,eventCount,time (ms)");
        for (int threadCount : threadCounts) {
            loggerPerfMultiThread(async, threadCount);
        }
    }

    private void loggerPerfSingleThread(boolean async) throws IOException {

        if (async) {
            System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
            System.setProperty("log4j2.asyncLoggerThreadNameStrategy", "CACHED");
            System.setProperty("log4j2.asyncLoggerConfigRingBufferSize", "" + (1024 * 256));
        }

        String logLine = "Test logger - SingleThread, async=" + async;
        long start, end;
        try {
            SpcfLogger logger = SpcfLogManager.getLogger(SpcfLogManagerImplTest.class);

            // warm-up
            for (int i = 0; i < EVENT_COUNT; i++) {
                logger.info(logLine);
            }
            sleep(DELAY);

            // 2. log to console 'count' number of times & note latency
            start = System.nanoTime();
            for (int i = 0; i < EVENT_COUNT; i++) {
                logger.info(logLine);
            }
            end = System.nanoTime();

            sleep(DELAY);

        } finally {
            // no-op
        }

        // 3. read logged content & validate
        String file = System.getProperty("user.dir") + "/logs/log4j2-test.log";
        Assert.assertTrue(new File(file).exists());
        String content = headFile(file);
        Assert.assertTrue(content.contains(logLine));

        // 4. tear down
        if (async) {
            System.setProperty("log4j2.contextSelector", "");
            System.setProperty("log4j2.asyncLoggerThreadNameStrategy", "");
            System.setProperty("log4j2.asyncLoggerConfigRingBufferSize", "");
        }

        System.out.printf("eventCount=%d, time=%dms", EVENT_COUNT, (end - start) / 1_000_000);
        System.out.println();
    }

    private void loggerPerfMultiThread(boolean async, int threadCount) throws IOException {
        if (async) {
            System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
            System.setProperty("log4j2.asyncLoggerThreadNameStrategy", "CACHED");
            System.setProperty("log4j2.asyncLoggerConfigRingBufferSize", "" + (1024 * 256));
        }
        long start, end;
        String logLine = "Test logger - Perf, async=" + async;

        try {
            SpcfLogger logger = SpcfLogManager.getLogger(SpcfLogManagerImplTest.class);

            // warm-up
            l(logger, EVENT_COUNT, logLine);
            sleep(DELAY);

            // 2. log to console 'count' number of times & note latency
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<Loggable> futureList = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                Loggable loggable = new Loggable(logger, EVENT_COUNT, logLine);
                futureList.add(loggable);
            }

            start = System.nanoTime();
            try {
                executor.invokeAll(futureList);
            } catch (Exception err) {
                System.err.println(err.getMessage());
            }
            end = System.nanoTime();
            executor.shutdown();

            sleep(DELAY);
        } finally {
            // no-op
        }

        // 3. read logged content & validate
        String file = System.getProperty("user.dir") + "/logs/log4j2-test.log";
        Assert.assertTrue(new File(file).exists());
        String content = headFile(file);
        Assert.assertTrue(content.contains(logLine));

        // 4. tear down
        if (async) {
            System.setProperty("log4j2.contextSelector", "");
            System.setProperty("log4j2.asyncLoggerThreadNameStrategy", "");
            System.setProperty("log4j2.asyncLoggerConfigRingBufferSize", "");
        }

        // output results
        System.out.printf("%s,%d,%d,%d", async, threadCount, EVENT_COUNT, (end - start) / 1_000_000);
        System.out.println();
    }

    class Loggable implements Callable<Object> {
        SpcfLogger logger;
        long eventCount;
        String logLine;

        public Loggable(SpcfLogger logger, long eventCount, String logLine) {
            this.logger = logger;
            this.eventCount = eventCount;
            this.logLine = logLine;
        }

        public Object call() {
            l(logger, eventCount, logLine);
            return null;
        }
    }

    private void l(SpcfLogger logger, long count, String logLine) {
        for (int i = 0; i < count; i++) {
            logger.info(logLine);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            System.err.println("Exception" + e.getMessage());
        }
    }

    private String headFile(String filePath) {
        // IOUtils.toString(new FileReader(file));

        long position = 0;
        long count = 10;
        try (RandomAccessFile readWriteFileAccess = new RandomAccessFile(filePath, "r")) {
            ;
            readWriteFileAccess.seek(position);
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < count; i++) {
                line.append(readWriteFileAccess.readLine()).append("\n");
            }
            // position = readWriteFileAccess.getFilePointer();
            return line.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}