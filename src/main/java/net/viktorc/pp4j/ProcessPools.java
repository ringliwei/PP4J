package net.viktorc.pp4j;

import net.viktorc.pp4j.api.ProcessManagerFactory;
import net.viktorc.pp4j.api.ProcessPool;
import net.viktorc.pp4j.impl.StandardProcessPool;

/**
 * A class for convenience and factory methods for creating instances of implementations of the 
 * {@link net.viktorc.pp4j.api.ProcessPool} interface.
 * 
 * @author Viktor Csomor
 *
 */
public class ProcessPools {

	/**
	 * Only static methods...
	 */
	private ProcessPools() {
		
	}
	/**
	 * Returns a pool of processes. The initial size of the pool is the greater of the minimum pool size and the 
	 * reserve size. This method blocks until the initial number of processes started up. The size of the pool is 
	 * dynamically adjusted based on the pool parameters and the rate of incoming submissions. It is a proxy 
	 * method for the constructor
	 * {@link net.viktorc.pp4j.impl.StandardProcessPool#StandardProcessPool(ProcessManagerFactory, int, int, int, long, boolean)}.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @param minPoolSize The minimum size of the process pool.
	 * @param maxPoolSize The maximum size of the process pool.
	 * @param reserveSize The number of available processes to keep in the pool.
	 * @param keepAliveTime The number of milliseconds after which idle processes are terminated. If it is 
	 * <code>0</code> or less, the life-cycle of the processes will not be limited.
	 * @param verbose Whether the events related to the management of the process pool should be logged. Setting 
	 * this parameter to <code>true</code> does not guarantee that logging will be performed as logging depends on 
	 * the SLF4J binding and the logging configurations, but setting it to <code>false</code> guarantees that no 
	 * logging will be performed by the constructed instance.
	 * @return A pool of process executors each hosting a process.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newCustomProcessPool(ProcessManagerFactory managerFactory, int minPoolSize, 
			int maxPoolSize, int reserveSize, long keepAliveTime, boolean verbose) throws InterruptedException {
		return new StandardProcessPool(managerFactory, minPoolSize, maxPoolSize, reserveSize, keepAliveTime,
				verbose);
	}
	/**
	 * Returns a pool of processes. The initial size of the pool is the greater of the minimum pool size and the 
	 * reserve size. This method blocks until the initial number of processes started up. The size of the pool is 
	 * dynamically adjusted based on the pool parameters and the rate of incoming submissions. It is a convenience 
	 * method for calling {@link #newCustomProcessPool(ProcessManagerFactory, int, int, int, long, boolean)} with 
	 * <code>keepAliveTime</code> set to <code>0</code>.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @param minPoolSize The minimum size of the process pool.
	 * @param maxPoolSize The maximum size of the process pool.
	 * @param reserveSize The number of available processes to keep in the pool.
	 * @param verbose Whether the events related to the management of the process pool should be logged. Setting 
	 * this parameter to <code>true</code> does not guarantee that logging will be performed as logging depends on 
	 * the SLF4J binding and the logging configurations, but setting it to <code>false</code> guarantees that no 
	 * logging will be performed by the constructed instance.
	 * @return A pool of process executors each hosting a process.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newCustomProcessPool(ProcessManagerFactory managerFactory, int minPoolSize,
			int maxPoolSize, int reserveSize, boolean verbose) throws InterruptedException {
		return newCustomProcessPool(managerFactory, minPoolSize, maxPoolSize, reserveSize, 0, verbose);
	}
	/**
	 * Returns a pool of processes. The initial size of the pool is the greater of the minimum pool size and the 
	 * reserve size. This method blocks until the initial number of processes started up. The size of the pool is 
	 * dynamically adjusted based on the pool parameters and the rate of incoming submissions. It is a convenience 
	 * method for calling {@link #newCustomProcessPool(ProcessManagerFactory, int, int, int, long, boolean)} with 
	 * <code>verbose</code> set to <code>false</code>.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @param minPoolSize The minimum size of the process pool.
	 * @param maxPoolSize The maximum size of the process pool.
	 * @param reserveSize The number of available processes to keep in the pool.
	 * @param keepAliveTime The number of milliseconds after which idle processes are terminated. If it is <code>
	 * 0</code> 
	 * or less, the life-cycle of the processes will not be limited.
	 * @return A pool of process executors each hosting a process.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newCustomProcessPool(ProcessManagerFactory managerFactory, int minPoolSize,
			int maxPoolSize, int reserveSize, long keepAliveTime) throws InterruptedException {
		return newCustomProcessPool(managerFactory, minPoolSize, maxPoolSize, reserveSize, keepAliveTime, false);
	}
	/**
	 * Returns a pool of processes. The initial size of the pool is the greater of the minimum pool size and the 
	 * reserve size. This method blocks until the initial number of processes started up. The size of the pool is 
	 * dynamically adjusted based on the pool parameters and the rate of incoming submissions. The processes never 
	 * time out. It is a convenience method for calling the method
	 * {@link #newCustomProcessPool(ProcessManagerFactory, int, int, int, long)} with <code>keepAliveTime</code> 
	 * set to <code>0</code>.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @param minPoolSize The minimum size of the process pool.
	 * @param maxPoolSize The maximum size of the process pool.
	 * @param reserveSize The number of available processes to keep in the pool.
	 * @return A pool of process executors each hosting a process.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newCustomProcessPool(ProcessManagerFactory managerFactory, int minPoolSize,
			int maxPoolSize, int reserveSize) throws InterruptedException {
		return newCustomProcessPool(managerFactory, minPoolSize, maxPoolSize, reserveSize, 0);
	}
	/**
	 * Returns a pool of a fixed number of processes. It is a convenience method for calling the method
	 * {@link #newCustomProcessPool(ProcessManagerFactory, int, int, int, long, boolean)} with <code>minPoolSize
	 * </code> and <code>maxPoolSize</code> equal and a <code>reserveSize</code> of <code>0</code>. The number of 
	 * executors in the pool is always kept at the specified value.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @param keepAliveTime The number of milliseconds after which idle processes are terminated. If it is <code>
	 * 0</code> or less, the life-cycle of the processes will not be limited.
	 * @param verbose Whether the events related to the management of the process pool should be logged. Setting 
	 * this parameter to <code>true</code> does not guarantee that logging will be performed as logging depends 
	 * on the SLF4J binding and the logging configurations, but setting it to <code>false</code> guarantees that 
	 * no logging will be performed by the constructed instance.
	 * @return A fixed size pool of process executors.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newFixedProcessPool(ProcessManagerFactory managerFactory, int size,
			long keepAliveTime, boolean verbose) throws InterruptedException {
		return newCustomProcessPool(managerFactory, size, size, 0, keepAliveTime, verbose);
	}
	/**
	 * Returns a pool of a fixed number of processes. It is a convenience method for calling the method
	 * {@link #newFixedProcessPool(ProcessManagerFactory, int, long, boolean)} with <code>keepAliveTime</code> set 
	 * to <code>0</code>. The number of executors in the pool is always kept at the specified value.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @param verbose Whether the events related to the management of the process pool should be logged. Setting 
	 * this parameter to <code>true</code> does not guarantee that logging will be performed as logging depends on 
	 * the SLF4J binding and the logging configurations, but setting it to <code>false</code> guarantees that no 
	 * logging will be performed by the constructed instance.
	 * @return A fixed size pool of process executors.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newFixedProcessPool(ProcessManagerFactory managerFactory, int size, boolean verbose)
			throws InterruptedException {
		return newFixedProcessPool(managerFactory, size, 0, verbose);
	}
	/**
	 * Returns a pool of a fixed number of processes. It is a convenience method for calling the method
	 * {@link #newFixedProcessPool(ProcessManagerFactory, int, long, boolean)} with <code>verbose</code> set to 
	 * <code>false</code>. The number of executors in the pool is always kept at the specified value.
	 * 
	 * @param managerFactory A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @param keepAliveTime The number of milliseconds after which idle processes are terminated. If it is <code>
	 * 0</code> or less, the life-cycle of the processes will not be limited.
	 * @return A fixed size pool of process executors.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newFixedProcessPool(ProcessManagerFactory managerFactory, int size,
			long keepAliveTime) throws InterruptedException {
		return newFixedProcessPool(managerFactory, size, keepAliveTime, false);
	}
	/**
	 * Returns a pool of a fixed number of processes. The number of executors in the pool is always kept at the 
	 * specified value and the processes never time out. It is a convenience method for calling the method 
	 * {@link #newFixedProcessPool(ProcessManagerFactory, int, long)} with <code>0</code> as the <code>
	 * keepAliveTime</code>.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @return A fixed size pool of process executors.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newFixedProcessPool(ProcessManagerFactory managerFactory, int size)
			throws InterruptedException {
		return newFixedProcessPool(managerFactory, size, 0);
	}
	/**
	 * Returns a pool of processes that grows in size as required. It is a convenience method for calling 
	 * {@link #newCustomProcessPool(ProcessManagerFactory, int, int, int, long, boolean)} with <code>0</code> as 
	 * the <code>minPoolSize</code> and the <code>reserveSize</code>, and <code>Integer.MAX_VALUE</code> as the 
	 * maximum pool size. If <code>keepAliveTime</code> is non-positive, the size of the process pool is only 
	 * decreased if a process is cancelled after the execution of a submission.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @param keepAliveTime The number of milliseconds after which idle processes are terminated. If it is <code>
	 * 0</code> or less, the life-cycle of the processes will not be limited.
	 * @param verbose Whether the events related to the management of the process pool should be logged. Setting 
	 * this parameter to <code>true</code> does not guarantee that logging will be performed as logging depends on 
	 * the SLF4J binding and the logging configurations, but setting it to <code>false</code> guarantees that no 
	 * logging will be performed by the constructed instance.
	 * @return An unbounded pool of process executors.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newCachedProcessPool(ProcessManagerFactory managerFactory, long keepAliveTime,
			boolean verbose) throws InterruptedException {
		return newCustomProcessPool(managerFactory, 0, Integer.MAX_VALUE, 0, keepAliveTime, verbose);
	}
	/**
	 * Returns a pool of processes that grows in size as required. It is a convenience method for calling 
	 * {@link #newCachedProcessPool(ProcessManagerFactory, long, boolean)} with <code>0</code> as the <code>
	 * keepAliveTime</code>. The size of the process pool only ever decreases if a process is terminated after 
	 * the execution of a submission.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @param verbose Whether the events related to the management of the process pool should be logged. Setting 
	 * this parameter to <code>true</code> does not guarantee that logging will be performed as logging depends on 
	 * the SLF4J binding and the logging configurations, but setting it to <code>false</code> guarantees that no 
	 * logging will be performed by the constructed instance.
	 * @return An unbounded pool of process executors.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newCachedProcessPool(ProcessManagerFactory managerFactory, boolean verbose)
			throws InterruptedException {
		return newCachedProcessPool(managerFactory, 0, verbose);
	}
	/**
	 * Returns a pool of processes that grows in size as required. It is a convenience method for calling 
	 * {@link #newCachedProcessPool(ProcessManagerFactory, long, boolean)} with<code>verbose</code> set to <code>
	 * false</code>. If <code>keepAliveTime</code> is non-positive, the size of the process pool only ever 
	 * decreases if a process is terminated after the execution of a submission.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @param keepAliveTime The number of milliseconds after which idle processes are terminated. If it is <code>
	 * 0</code> 
	 * or less, the life-cycle of the processes will not be limited.
	 * @return An unbounded pool of process executors.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newCachedProcessPool(ProcessManagerFactory managerFactory, long keepAliveTime)
			throws InterruptedException {
		return newCachedProcessPool(managerFactory, keepAliveTime, false);
	}
	/**
	 * Returns a pool of processes that grows in size as required. The processes never time out. It is a 
	 * convenience method for calling the method {@link #newCachedProcessPool(ProcessManagerFactory, long)} with 
	 * <code>0</code> as the <code>keepAliveTime</code>. The size of the process pool only ever decreases if a 
	 * process is terminated after the execution of a submission.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @return An unbounded pool of process executors.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newCachedProcessPool(ProcessManagerFactory managerFactory)
			throws InterruptedException {
		return newCachedProcessPool(managerFactory, 0);
	}
	/**
	 * Returns a fixed size pool holding a single process. It is a convenience method for calling the method
	 * {@link #newFixedProcessPool(ProcessManagerFactory, int, long, boolean)} with <code>1</code> as the <code>
	 * size</code>.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @param keepAliveTime The number of milliseconds after which idle processes are terminated. If it is <code>
	 * 0</code> or less, the life-cycle of the processes will not be limited.
	 * @param verbose Whether the events related to the management of the process pool should be logged. Setting 
	 * this parameter to <code>true</code> does not guarantee that logging will be performed as logging depends on 
	 * the SLF4J binding and the logging configurations, but setting it to <code>false</code> guarantees that no 
	 * logging will be performed by the constructed instance.
	 * @return A pool holding a single process executor.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newSingleProcessPool(ProcessManagerFactory managerFactory, long keepAliveTime,
			boolean verbose) throws InterruptedException {
		return newFixedProcessPool(managerFactory, 1, keepAliveTime, verbose);
	}
	/**
	 * Returns a fixed size pool holding a single process. It is a convenience method for calling the method
	 * {@link #newSingleProcessPool(ProcessManagerFactory, long, boolean)} with <code>0</code> as the <code>
	 * keepAliveTime</code>.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @param verbose Whether the events related to the management of the process pool should be logged. Setting 
	 * this parameter to <code>true</code> does not guarantee that logging will be performed as logging depends on 
	 * the SLF4J binding and the logging configurations, but setting it to <code>false</code> guarantees that no 
	 * logging will be performed by the constructed instance.
	 * @return A pool holding a single process executor.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newSingleProcessPool(ProcessManagerFactory managerFactory, boolean verbose)
			throws InterruptedException {
		return newSingleProcessPool(managerFactory, 0, verbose);
	}
	/**
	 * Returns a fixed size pool holding a single process. It is a convenience method for calling the method
	 * {@link #newSingleProcessPool(ProcessManagerFactory, long, boolean)} with <code>verbose</code> set to <code>
	 * false</code>.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @param keepAliveTime The number of milliseconds after which idle processes are terminated. If it is <code>
	 * 0</code> or less, the life-cycle of the processes will not be limited.
	 * @return A pool holding a single process executor.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the processes to start up.
	 */
	public static ProcessPool newSingleProcessPool(ProcessManagerFactory managerFactory, long keepAliveTime)
			throws InterruptedException {
		return newSingleProcessPool(managerFactory, keepAliveTime, false);
	}
	/**
	 * Returns a fixed size pool holding a single process that never times out. It is a convenience method for 
	 * calling the method {@link #newSingleProcessPool(ProcessManagerFactory, long)} with <code>0</code> as the 
	 * <code>keepAliveTime</code>.
	 * 
	 * @param managerFactory  A {@link net.viktorc.pp4j.api.ProcessManagerFactory} instance that is used to build 
	 * {@link net.viktorc.pp4j.api.ProcessManager} instances that manage the processes' life cycle in the pool.
	 * @return A pool holding a single process executor.
	 * @throws InterruptedException If the thread is interrupted while it is waiting for the process to start up.
	 */
	public static ProcessPool newSingleProcessPool(ProcessManagerFactory managerFactory)
			throws InterruptedException {
		return newSingleProcessPool(managerFactory, 0);
	}
	
}
