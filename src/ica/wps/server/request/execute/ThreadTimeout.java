package ica.wps.server.request.execute;

/**
 * Thread monitors another thread and interrupts it on timeout.
 * @author	M. Wittensoeldner
 * @date	Created on 11.03.2007
 */
public class ThreadTimeout extends Thread {
	private Thread			_thread;
	private long			_lTimeoutMillis;
	private boolean			_bTimeoutReached = false;

	/**
	 * Constructor.
	 * @param thread					The thread to monitor.
	 * @param lTimeoutMillis			The thread will be interrupted after the timeout.
	 */
	public ThreadTimeout(Thread thread,
								long lTimeoutMillis) {
		_thread = thread;
		_lTimeoutMillis = lTimeoutMillis;
	}

	/**
	 * Runs the thread.
	 * @return void
	 */
    public void run() {
    	_bTimeoutReached = false;
    	long lStartTime = System.currentTimeMillis();
    	_thread.start();
    	while (_thread.isAlive() && !_bTimeoutReached) {
    		try {
    			Thread.sleep(500L);
    		} catch (Exception ex) {
    		}
    		_bTimeoutReached = (System.currentTimeMillis()-lStartTime >= _lTimeoutMillis);
    		if (_bTimeoutReached) {
    			try {
	    			_thread.interrupt();
	    			_thread.stop(new Exception(""));
    			} catch (Error er) {
    			}
    		}
    	}
    	if (_bTimeoutReached && _thread instanceof ThreadExecuteStatus) {
    		((ThreadExecuteStatus)_thread).handleTimeout();
    	}
	}

	/**
	 * Checks whether a timeout has been reached.
	 * @return boolean					True whether the timeout has been reached.
	 */
	public synchronized boolean isTimeout() {
		return _bTimeoutReached;
	}
}
