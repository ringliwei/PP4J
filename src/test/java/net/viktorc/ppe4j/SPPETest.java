package net.viktorc.ppe4j;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import net.viktorc.ppe4j.Command;
import net.viktorc.ppe4j.ProcessShell;
import net.viktorc.ppe4j.SimpleCommand;
import net.viktorc.ppe4j.SimpleProcessManager;
import net.viktorc.ppe4j.SimpleSubmission;
import net.viktorc.ppe4j.StandardProcessPoolExecutor;
import net.viktorc.ppe4j.Submission;

import java.io.File;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A test class for the standard process pool.
 * 
 * @author Viktor
 *
 */
public class SPPETest {
	
	@Rule
	public final ExpectedException exceptionRule = ExpectedException.none();
	
	private final String programLocation;
	
	/**
	 * Resolves the path to the test program and ensures that it is executable.
	 * 
	 * @throws URISyntaxException If the path to the test program cannot be resolved.
	 */
	public SPPETest() throws URISyntaxException {
		// Support testing on Linux and Windows.
		boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
		programLocation = new File(getClass().getResource(windows ? "win/test.exe" : "linux/test")
				.toURI().getPath()).getAbsolutePath();
		File file = new File(programLocation);
		file.setExecutable(true);
	}
	/**
	 * Performs some basic checks on the pool concerning its size and other parameters.
	 * 
	 * @param pool The pool to check.
	 * @param minPoolSize The minimum pool size.
	 * @param maxPoolSize The maximum pool size.
	 * @param reserveSize The process reserve size.
	 * @param keepAliveTime The time after which idled processes are killed.
	 * @param verifyStartup Whether the startup should be verified.
	 * @param manuallyTerminate Whether the process should be terminated in an orderly way or forcibly.
	 * @param verbose Whether the events relating to the management of the pool should be logged to the console.
	 */
	private void checkPool(StandardProcessPoolExecutor pool, int minPoolSize, int maxPoolSize, int reserveSize, long keepAliveTime,
			boolean verifyStartup, boolean manuallyTerminate, boolean verbose) {
		// Basic pool statistics checks.
		assert minPoolSize == pool.getMinSize() : "Different min pool sizes: " + minPoolSize + " and " +
				pool.getMinSize() + ".";
		assert maxPoolSize == pool.getMaxSize() : "Different max pool sizes: " + maxPoolSize + " and " +
				pool.getMaxSize() + ".";
		assert reserveSize == pool.getReserveSize() : "Different reserve sizes: " + reserveSize + " and " +
				pool.getReserveSize() + ".";
		assert Math.max(0, keepAliveTime) == pool.getKeepAliveTime() : "Different keep alive times: " +
				Math.max(0, keepAliveTime) + " and " + pool.getKeepAliveTime() + ".";
		assert verbose == pool.isVerbose() : "Different verbosity: " + verbose + " and " + pool.isVerbose() + ".";
		assert pool.getNumOfQueuedSubmissions() == 0 : "Non-zero number of queued submissions on startup: " +
				pool.getNumOfQueuedSubmissions() + ".";
		assert pool.getNumOfExecutingSubmissions() == 0 : "Non-zero number of executing submissions on startup: " +
				pool.getNumOfExecutingSubmissions() + ".";
		assert pool.getNumOfProcesses() == Math.max(minPoolSize, reserveSize) : "Unexpected number of total " +
				"processes: " + pool.getNumOfProcesses() + " instead of " + Math.max(minPoolSize, reserveSize) + ".";
	}
	/**
	 * Creates a custom test process pool according to the specified parameters.
	 * 
	 * @param minPoolSize The minimum pool size.
	 * @param maxPoolSize The maximum pool size.
	 * @param reserveSize The process reserve size.
	 * @param keepAliveTime The time after which idled processes are killed.
	 * @param verifyStartup Whether the startup should be verified.
	 * @param manuallyTerminate Whether the process should be terminated in an orderly way or forcibly.
	 * @param verbose Whether the events relating to the management of the pool should be logged to the console.
	 * @return The process pool created according to the specified parameters.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the core threads to start up.
	 */
	public StandardProcessPoolExecutor getCustomPool(int minPoolSize, int maxPoolSize, int reserveSize, long keepAliveTime,
			boolean verifyStartup, boolean manuallyTerminate, boolean verbose) throws InterruptedException {
		StandardProcessPoolExecutor pool = new StandardProcessPoolExecutor(new TestProcessManagerFactory(verifyStartup,
				manuallyTerminate), minPoolSize, maxPoolSize, reserveSize, keepAliveTime, verbose);
		checkPool(pool, minPoolSize, maxPoolSize, reserveSize, keepAliveTime, verifyStartup, manuallyTerminate, verbose);
		return pool;
	}
	/**
	 * Submits the specified number of commands with the specified frequency to a the test process pool 
	 * corresponding to the specified parameters and returns a list of the total execution times of the 
	 * commands.
	 * 
	 * @param processPool The process pool executor to test.
	 * @param reuse Whether a process can execute multiple commands.
	 * @param procTimes The times for which the test processes should "execute" commands. Each element 
	 * stands for a command. If there are multiple elements, the commands will be chained.
	 * @param requests The number of commands to submit.
	 * @param timeSpan The number of milliseconds in which the specified number of requests should be sent. 
	 * If it is 0 or less, they are all sent at once.
	 * @param cancelTime The number of milliseconds after which the futures should be cancelled. If it 
	 * is 0 or less, the futures are not cancelled.
	 * @param forcedCancel If the command should be interrupted if it is already being processed. If 
	 * <code>cancelTime</code> is not greater than 0, it has no effect.
	 * @param earlyClose Whether the pool should be closed right after the submission of the commands.
	 * @param waitTimeout The number of milliseconds for which the submissions are waited on.
	 * @return A list of the total execution times of the commands.
	 * @throws Exception
	 */
	private List<Long> testBase(StandardProcessPoolExecutor processPool, boolean reuse, int[] procTimes,
			int requests, long timeSpan, long cancelTime, boolean forcedCancel, boolean earlyClose, long waitTimeout)
					throws Exception {
		long frequency = requests > 0 ? timeSpan/requests : 0;
		List<Future<Long>> futures = new ArrayList<>(requests);
		List<Entry<Semaphore,Long>> cancelTimes = cancelTime > 0 ? new ArrayList<>(requests) : null;
		for (int i = 0; i < requests; i++) {
			if (i != 0 && frequency > 0) {
				try {
					Thread.sleep(frequency);
				} catch (InterruptedException e) {
					return null;
				}
			}
			List<Command> commands;
			if (procTimes == null)
				commands = null;
			else {
				commands = new ArrayList<>();
				for (int procTime : procTimes)
					commands.add(new SimpleCommand("process " + procTime, (c, o) -> {
								if ("ready".equals(o)) {
									// Output line caching check.
									assert c.getStandardOutLines().size() == procTime && c.getErrorOutLines().size() == 0 :
											"Unexpected numbers of output lines: " + c.getStandardOutLines().size() + 
											" instead of " + procTime + " and " + c.getErrorOutLines().size() + 
											" instead of " + 0 + ".";
									if (processPool.isVerbose())
										System.out.println(("Std: " + c.getJointStandardOutLines() + "; Err: " +
												c.getJointErrorOutLines()).replaceAll("\n", " "));
									c.reset();
									return true;
								}
								return false;
							}, (c, o) -> true));
			}
			Submission submission;
			if (cancelTime > 0) {
				Semaphore semaphore = new Semaphore(0);
				submission = new SimpleSubmission(commands, !reuse) {
					
					@Override
					public void onFinishedProcessing() {
						semaphore.release();
					}
					
				};
				long startTime = System.nanoTime();
				cancelTimes.add(new SimpleEntry<>(semaphore, startTime));
			} else
				submission = new SimpleSubmission(commands, !reuse);
			futures.add(processPool.submit(submission));
		}
		if (cancelTime > 0) {
			Thread.sleep(cancelTime);
			for (Future<Long> future : futures)
				future.cancel(forcedCancel);
		} else if (earlyClose)
			processPool.shutdown();
		List<Long> times = new ArrayList<>();
		for (int i = 0; i < futures.size(); i++) {
			Future<Long> future = futures.get(i);
			try {
				long time = waitTimeout > 0 ? future.get(waitTimeout, TimeUnit.MILLISECONDS) : future.get();
				times.add(time);
			} catch (CancellationException e) {
				if (cancelTime > 0) {
					Entry<Semaphore,Long> cancelEntry = cancelTimes.get(i);
					cancelEntry.getKey().acquire();
					times.add((long) Math.round(((double) (System.nanoTime() - cancelEntry.getValue()))/1000000));
				} else
					times.add((long) 0);
			}
		}
		if (!earlyClose)
			processPool.shutdown();
		return times;
	}
	/**
	 * Submits the specified number of commands with the specified frequency to a the test process pool 
	 * corresponding to the specified parameters and determines whether it performs well enough based on 
	 * the number of processed requests and the times it took to process them.
	 * 
	 * @param testName The name of the test.
	 * @param processPool The process pool executor to test.
	 * @param reuse Whether a process can execute multiple commands.
	 * @param procTimes The times for which the test processes should "execute" commands. Each element 
	 * stands for a command. If there are multiple elements, the commands will be chained.
	 * @param requests The number of commands to submit.
	 * @param requestPerSe The number of commands to submit per second
	 * @param cancelTime The number of milliseconds after which the futures should be cancelled. If it 
	 * is 0 or less, the futures are not cancelled.
	 * @param forcedCancel If the command should be interrupted if it is already being processed. If 
	 * <code>cancelTime</code> is not greater than 0, it has no effect.
	 * @param earlyClose Whether the pool should be closed right after the submission of the commands.
	 * @param waitTimeout The number of milliseconds for which the submissions are waited on.
	 * @param lowerBound The minimum acceptable submission execution time.
	 * @param upperBound The maximum acceptable submission execution time.
	 * @return Whether the test passes.
	 * @throws Exception If the process pool cannot be created.
	 */
	private boolean perfTest(String testName, StandardProcessPoolExecutor processPool, boolean reuse, int[] procTimes,
			int requests, long timeSpan, long cancelTime, boolean forcedCancel, boolean earlyClose, long waitTimeout,
			long lowerBound, long upperBound) throws Exception {
		List<Long> times = testBase(processPool, reuse, procTimes, requests, timeSpan, cancelTime, forcedCancel,
				earlyClose, waitTimeout);
		System.out.println("\n" + testName);
		System.out.println("-------------------------------------------------------------------------------------" +
				"---------------");
		System.out.printf("minPoolSize: %d; maxPoolSize: %d; reserveSize: %d; keepAliveTime: %d;%n" +
				"verbose: %s; verifyStartup: %s; manuallyTerminate: %s; reuse: %s;%n" +
				"procTimes: %s; requests: %d; timeSpan: %d; cancelTime: %d; forcedCancel: %s;%n" +
				"earlyClose: %s; waitTimeout: %.3f; lowerBound: %.3f; upperBound: %.3f;%n",
				processPool.getMinSize(), processPool.getMaxSize(), processPool.getReserveSize(),
				processPool.getKeepAliveTime(), Boolean.toString(processPool.isVerbose()),
				Boolean.toString(((TestProcessManagerFactory) processPool.getProcessManagerFactory()).verifyStartup),
				Boolean.toString(((TestProcessManagerFactory) processPool.getProcessManagerFactory()).manuallyTerminate),
				Boolean.toString(reuse), Arrays.toString(procTimes), requests, timeSpan, cancelTime,
				Boolean.toString(forcedCancel), Boolean.toString(earlyClose), (float) (((double) waitTimeout)/1000),
				(float) (((double) lowerBound)/1000), (float) (((double) upperBound)/1000));
		System.out.println("-------------------------------------------------------------------------------------" +
				"---------------");
		if (times.size() == requests) {
			boolean pass = true;
			for (Long time : times) {
				boolean fail = time == null || time > upperBound || time < lowerBound;
				if (fail)
					pass = false;
				System.out.printf("Time: %.3f %s%n", (float) (((double) time)/1000), fail ? "FAIL" : "");
			}
			return pass;
		} else {
			System.out.printf("Some requests were not processed %d/%d%n", times.size(), requests);
			return false;
		}
	}
	// Exception testing.
	@Test
	public void test01() throws Exception {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("The minimum pool size has to be greater than 0.");
		StandardProcessPoolExecutor pool = getCustomPool(-1, 5, 0, 0, false, false, false);
		perfTest("Test 1", pool, false, new int[] { 5 }, 100, 10000, 0, false, false, 0, 4995, 6200);
	}
	@Test
	public void test02() throws Exception {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("The maximum pool size has to be at least 1 and at least as great as the " +
				"minimum pool size.");
		getCustomPool(0, 0, 0, 0, false, false, false);
	}
	@Test
	public void test03() throws Exception {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("The maximum pool size has to be at least 1 and at least as great as the " +
				"minimum pool size.");
		getCustomPool(10, 5, 0, 0, false, false, false);
	}
	@Test
	public void test04() throws Exception {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("The reserve has to be greater than 0 and less than the maximum pool size.");
		getCustomPool(10, 12, -1, 0, false, false, false);
	}
	@Test
	public void test05() throws Exception {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("The reserve has to be greater than 0 and less than the maximum pool size.");
		getCustomPool(10, 12, 15, 0, false, false, false);
	}
	@Test
	public void test06() throws Exception {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("The commands cannot be null.");
		StandardProcessPoolExecutor pool = getCustomPool(10, 20, 0, 0, false, false, false);
		perfTest("Test 6", pool, false, null, 100, 10000, 0, false, false, 0, 4995, 6200);
	}
	@Test
	public void test07() throws Exception {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("The commands cannot be empty.");
		StandardProcessPoolExecutor pool = getCustomPool( 10, 20, 0, 0, false, false, false);
		perfTest("Test 7", pool, false, new int[0], 100, 10000, 0, false, false, 0, 4995, 6200);
	}
	// Performance testing.
	@Test
	public void test08() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(0, 100, 0, 0, true, false, false);
		Assert.assertTrue(perfTest("Test 8", pool, true, new int[] { 5 }, 100, 10000, 0, false, false, 0, 4995, 6250));
	}
	@Test
	public void test09() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(50, 150, 20, 0, false, false, false);
		Assert.assertTrue(perfTest("Test 9", pool, true, new int[] { 5 }, 100, 5000, 0, false, false, 0, 4995, 5100));
	}
	@Test
	public void test10() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(10, 25, 5, 15000, true, false, false);
		Assert.assertTrue(perfTest("Test 10", pool, true, new int[] { 5 }, 20, 10000, 0, false, false, 0, 4995, 5100));
	}
	@Test
	public void test11() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(50, 150, 20, 0, false, true, false);
		Assert.assertTrue(perfTest("Test 11", pool, true, new int[] { 5 }, 100, 5000, 0, false, false, 0, 4995, 5100));
	}
	@Test
	public void test12() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(10, 50, 5, 15000, true, false, false);
		Assert.assertTrue(perfTest("Test 12", pool, true, new int[] { 5, 3, 2 }, 50, 10000, 0, false, false, 0, 9995,
				10340));
	}
	@Test
	public void test13() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(100, 250, 20, 0, true, true, false);
		Assert.assertTrue(perfTest("Test 13", pool, true, new int[] { 5 }, 800, 20000, 0, false, false, 0, 4995, 6000));
	}
	@Test
	public void test14() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(0, 100, 0, 0, false, false, false);
		Assert.assertTrue(perfTest("Test 14", pool, false, new int[] { 5 }, 100, 10000, 0, false, false, 0, 4995, 7250));
	}
	@Test
	public void test15() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(50, 150, 10, 0, true, false, false);
		Assert.assertTrue(perfTest("Test 15", pool, false, new int[] { 5 }, 100, 5000, 0, false, false, 0, 4995, 5620));
	}
	@Test
	public void test16() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(10, 25, 5, 15000, false, true, false);
		Assert.assertTrue(perfTest("Test 16", pool, false, new int[] { 5 }, 20, 10000, 0, false, false, 0, 4995, 5100));
	}
	@Test
	public void test17() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(50, 150, 10, 0, true, true, false);
		Assert.assertTrue(perfTest("Test 17", pool, false, new int[] { 5 }, 100, 5000, 0, false, false, 0, 4995, 5600));
	}
	@Test
	public void test18() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(10, 50, 5, 15000, false, false, false);
		Assert.assertTrue(perfTest("Test 18", pool, false, new int[] { 5, 3, 2 }, 50, 10000, 0, false, false, 0, 9995,
				10350));
	}
	@Test
	public void test19() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(50, 250, 20, 0, true, true, false);
		Assert.assertTrue(perfTest("Test 19", pool, false, new int[] { 5 }, 800, 20000, 0, false, false, 0, 4995, 6000));
	}
	// Keep alive timer and logging test.
	@Test
	public void test20() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(20, 40, 4, 250, true, true, true);
		Assert.assertTrue(perfTest("Test 20", pool, false, new int[] { 5 }, 50, 5000, 0, false, false, 0, 4995, 8200));
	}
	// Cancellation testing.
	@Test
	public void test21() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(10, 30, 5, 0, true, true, false);
		Assert.assertTrue(perfTest("Test 21", pool, false, new int[] { 5 }, 20, 0, 2500, true, false, 0, 2495, 2520));
	}
	@Test
	public void test22() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(20, 20, 0, 0, false, false, false);
		Assert.assertTrue(perfTest("Test 22", pool, false, new int[] { 5 }, 20, 0, 2500, false, false, 0, 4995, 5120));
	}
	@Test
	public void test23() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(10, 30, 5, 0, true, true, false);
		Assert.assertTrue(perfTest("Test 23", pool, false, new int[] { 5, 5, 3 }, 20, 0, 2500, true, false, 0, 2495,
				2520));
	}
	@Test
	public void test24() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(20, 20, 0, 0, true, true, false);
		Assert.assertTrue(perfTest("Test 24", pool, false, new int[] { 5, 5, 3 }, 20, 0, 3000, false, false, 0, 4995,
				5120));
	}
	// Early shutdown testing.
	@Test
	public void test25() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(100, 100, 0, 5000, true, false, false);
		Assert.assertTrue(perfTest("Test 25", pool, false, new int[] { 5 }, 100, 0, 0, false, true, 0, 0, 0));
	}
	// Interrupted construction testing.
	@Test
	public void test26() throws Exception {
		Thread t = new Thread(() -> {
			StandardProcessPoolExecutor pool;
			try {
				pool = getCustomPool(20, 30, 0, 0, false, false, true);
				exceptionRule.expect(InterruptedException.class);
				pool.shutdown();
			} catch (InterruptedException e) {
				// Expected.
			}
		});
		t.start();
		Thread.sleep(500);
		t.interrupt();
	}
	// Single process pool performance testing.
	@Test
	public void test27() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(1, 1, 0, 20000, true, true, false);
		Assert.assertTrue(perfTest("Test 27", pool, false, new int[] { 5 }, 5, 30000, 0, false, false, 0, 4995, 5100));
	}
	@Test
	public void test28() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(1, 1, 0, 0, true, false, false);
		Assert.assertTrue(perfTest("Test 28", pool, false, new int[] { 5 }, 5, 20000, 0, false, false, 0, 4995, 13200));
	}
	// Fixed size process pool performance testing.
	@Test
	public void test29() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(20, 20, 0, 0, true, false, false);
		Assert.assertTrue(perfTest("Test 29", pool, false, new int[] { 5 }, 20, 5000, 0, false, false, 0, 4995, 5200));
	}
	@Test
	public void test30() throws Exception {
		StandardProcessPoolExecutor pool = getCustomPool(20, 20, 0, 0, true, false, false);
		Assert.assertTrue(perfTest("Test 30", pool, false, new int[] { 5 }, 40, 10000, 0, false, false, 0, 4995, 6200));
	}
	// Wait with timeout testing.
	@Test
	public void test31() throws Exception {
		exceptionRule.expect(TimeoutException.class);
		StandardProcessPoolExecutor pool = getCustomPool(20, 50, 10, 0, true, true, false);
		Assert.assertTrue(perfTest("Test 31", pool, false, new int[] { 5 }, 40, 0, 0, false, false, 3000, 3000,
				3000));
	}
	@Test
	public void test32() throws Exception {
		exceptionRule.expect(TimeoutException.class);
		StandardProcessPoolExecutor pool = getCustomPool(20, 50, 0, 0, true, true, false);
		Assert.assertTrue(perfTest("Test 32", pool, false, new int[] { 5, 5 }, 40, 0, 0, false, false, 5000, 5000,
				5000));
	}
	
	/**
	 * An implementation of the {@link net.viktorc.ppe4j.ProcessManagerFactory} interface for testing purposes.
	 * 
	 * @author Viktor
	 *
	 */
	private class TestProcessManagerFactory implements ProcessManagerFactory {
		
		final boolean verifyStartup;
		final boolean manuallyTerminate;
		
		/**
		 * Constructs an instance according to the specified parameters.
		 * 
		 * @param verifyStartup Whether the startup should be verified.
		 * @param manuallyTerminate Whether the process should be terminated in an orderly way or forcibly.
		 */
		TestProcessManagerFactory(boolean verifyStartup, boolean manuallyTerminate) {
			this.verifyStartup = verifyStartup;
			this.manuallyTerminate = manuallyTerminate;
		}
		@Override
		public ProcessManager newProcessManager() {
			return new SimpleProcessManager(new ProcessBuilder(programLocation),
					s -> {
						try {
							s.execute(new SimpleSubmission(new SimpleCommand("start",
									(c, o) -> "ok".equals(o), (c, o) -> true), false));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}) {
				
				@Override
				public boolean startsUpInstantly() {
					return !verifyStartup;
				}
				@Override
				public boolean isStartedUp(String output, boolean standard) {
					return standard && "hi".equals(output);
				}
				@Override
				public boolean terminate(ProcessShell shell) {
					if (manuallyTerminate) {
						try {
							AtomicBoolean success = new AtomicBoolean(true);
							if (shell.execute(new SimpleSubmission(new SimpleCommand("stop", (c, o) -> "bye".equals(o),
									(c, o) -> {
										success.set(false);
										return true;
									}), false)))
								return success.get();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return false;
				}
			};
		}
		
	}
	
}