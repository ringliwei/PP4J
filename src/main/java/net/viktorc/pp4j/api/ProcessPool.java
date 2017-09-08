package net.viktorc.pp4j.api;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * An interface that outlines an executing mechanism for {@link net.viktorc.pp4j.api.Submission} instances in 
 * separate processes and allows for the tracking of the progress of the execution via 
 * {@link java.util.concurrent.Future} instances. The interface also defines a method for shutting down the 
 * process pool and releasing the associated resources. It defines the same shutdown-related methods as the 
 * {@link java.util.concurrent.ExecutorService} interface to conform to its shutdown behaviour.
 * 
 * @author Viktor Csomor
 *
 */
public interface ProcessPool {

	/**
	 * Returns the {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance responsible for creating instances 
	 * of an implementation of the {@link net.viktorc.pp4j.api.ProcessManager} interface for managing the 
	 * processes of the pool.
	 * 
	 * @return The process manager factory of the process pool.
	 */
	ProcessManagerFactory getProcessManagerFactory();
	/**
	 * Submits the specified submission for execution and returns a {@link java.util.concurrent.Future} instance 
	 * which allows for the cancellation of the submission. It does not block until the processes terminate.
	 * 
	 * @param submission The submission to execute.
	 * @return A {@link java.util.concurrent.Future} instance that allows for the waiting for the completion of 
	 * the execution, the cancellation thereof, or the retrieval of its optional result.
	 */
	<T> Future<T> submit(Submission<T> submission);
	/**
	 * Initiates the orderly shutdown of the process pool. It does not affect the execution of previously 
	 * submitted tasks. See {@link java.util.concurrent.ExecutorService#shutdown()}.
	 */
	void shutdown();
	/**
	 * Kills all the processes, blocks until the pool is shutdown, and returns a list of the submissions that 
	 * have been submitted but never processed. It does not block until the processes terminate. See 
	 * {@link java.util.concurrent.ExecutorService#shutdownNow()}.
	 * 
	 * @return A list of the submissions that were waiting execution.
	 */
	List<Submission<?>> forceShutdown();
	/**
	 * Returns whether the shutdown of the pool has been initiated. See 
	 * {@link java.util.concurrent.ExecutorService#isShutdown()}.
	 * 
	 * @return Whether the shutdown of the pool has been initiated.
	 */
	boolean isShutdown();
	/**
	 * Returns whether the process pool has successfully been shut down with all its processes terminated.
	 * See {@link java.util.concurrent.ExecutorService#isTerminated()}.
	 * 
	 * @return Whether the process pool has successfully been shut down with all its processes terminated.
	 */
	boolean isTerminated();
	/**
	 * It blocks until the {@link #isTerminated()} method returns true or a timeout occurs. See 
	 * {@link java.util.concurrent.ExecutorService#awaitTermination(long, TimeUnit)}.
	 * 
	 * @param timeout The amount of time to wait for the pool's termination.
	 * @param unit The unit of the amount.
	 * @return Whether the pool has successfully terminated or a timeout occurred.
	 * @throws InterruptedException If the thread is interrupted while the method is blocking.
	 */
	boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
	
}
