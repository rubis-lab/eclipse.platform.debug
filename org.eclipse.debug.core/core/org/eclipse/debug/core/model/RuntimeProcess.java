/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.model;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.internal.core.DebugCoreMessages;
import org.eclipse.debug.internal.core.StreamsProxy;


/**
 * Standard implementation of an <code>IProcess</code> that wrappers a system
 * process (<code>java.lang.Process</code>).
 * <p>
 * Clients may subclass this class. Clients that need to replace the implementation
 * of a streams proxy associatd with an <code>IProcess</code> should subclass this
 * class. Generally clients should not instantiate this class directly, but should
 * instead call <code>DebugPlugin.newProcess(...)</code>, which can delegate to an 
 * <code>IProcessFactory</code> if one is referenced by the associated launch configuration.
 * </p>
 * @see org.eclipse.debug.core.model.IProcess
 * @see org.eclipse.debug.core.IProcessFactory
 * @since 3.0
 */
public class RuntimeProcess extends PlatformObject implements IProcess {

	private static final int MAX_WAIT_FOR_DEATH_ATTEMPTS = 10;
	private static final int TIME_TO_WAIT_FOR_THREAD_DEATH = 500; // ms
	
	/**
	 * The launch this process is contained in
	 */
	private ILaunch fLaunch;
	
	/**
	 * The system process represented by this <code>IProcess</code>
	 */
	private Process fProcess;
	
	/**
	 * This process's exit value
	 */
	private int fExitValue;
	
	/**
	 * The monitor which listens for this runtime process' system process
	 * to terminate.
	 */
	private ProcessMonitorJob fMonitor;
	
	/**
	 * The streams proxy for this process
	 */
	private IStreamsProxy fStreamsProxy;

	/**
	 * The name of the process
	 */
	private String fName;

	/**
	 * Whether this process has been terminated
	 */
	private boolean fTerminated;
	
	/**
	 * Table of client defined attributes
	 */
	private Map fAttributes;

	/**
	 * Constructs a RuntimeProcess on the given system process
	 * with the given name, adding this process to the given
	 * launch.
	 * 
	 * @param launch the launch this process will be parented by
	 * @param process underlyig system process
	 * @param name the label used for this process
	 * @param attributes map of attributes used to initialize the attributes
	 *   of this process, or <code>null</code> if none
	 */
	public RuntimeProcess(ILaunch launch, Process process, String name, Map attributes) {
		setLaunch(launch);
		initializeAttributes(attributes);
		fProcess= process;
		fName= name;
		fTerminated= true;
		try {
			process.exitValue();
		} catch (IllegalThreadStateException e) {
			fTerminated= false;
		}
		fStreamsProxy= createStreamsProxy();
		fMonitor = new ProcessMonitorJob(this);
		launch.addProcess(this);
		fireCreationEvent();
	}

	/**
	 * Initialize the attributes of this process to those in the given map.
	 * 
	 * @param attributes attribute map or <code>null</code> if none
	 */
	private void initializeAttributes(Map attributes) {
		if (attributes != null) {
			Iterator keys = attributes.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String)keys.next();
				setAttribute(key, (String)attributes.get(key));
			}	
		}
	}

	/**
	 * @see ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return !fTerminated;
	}

	/**
	 * @see IProcess#getLabel()
	 */
	public String getLabel() {
		return fName;
	}
	
	/**
	 * Sets the launch this process is contained in
	 * 
	 * @param launch the launch this process is contained in
	 */
	protected void setLaunch(ILaunch launch) {
		fLaunch = launch;
	}

	/**
	 * @see IProcess#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fLaunch;
	}

	/**
	 * Returns the underlying system process associated with this process.
	 * 
	 * @return system process
	 */
	protected Process getSystemProcess() {
		return fProcess;
	}

	/**
	 * @see ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return fTerminated;
	}

	/**
	 * @see ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		if (!isTerminated()) {
			fProcess.destroy();
			if (fStreamsProxy instanceof StreamsProxy) {
				((StreamsProxy)fStreamsProxy).kill();
			}
			int attempts = 0;
			while (attempts < MAX_WAIT_FOR_DEATH_ATTEMPTS) {
				try {
					if (fProcess != null) {
						fExitValue = fProcess.exitValue(); // throws exception if process not exited
					}
					return;
				} catch (IllegalThreadStateException ie) {
				}
				try {
					Thread.sleep(TIME_TO_WAIT_FOR_THREAD_DEATH);
				} catch (InterruptedException e) {
				}
				attempts++;
			}
			// clean-up
			fMonitor.killJob();
			IStatus status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugException.TARGET_REQUEST_FAILED, DebugCoreMessages.getString("RuntimeProcess.terminate_failed"), null);		 //$NON-NLS-1$
			throw new DebugException(status);
		}
	}

	/**
	 * Notification that the system process associated with this process
	 * has terminated.
	 */
	protected void terminated() {
		if (fStreamsProxy instanceof StreamsProxy) {
			((StreamsProxy)fStreamsProxy).close();
		}
		fTerminated= true;
		try {
			fExitValue = fProcess.exitValue();
		} catch (IllegalThreadStateException ie) {
		}
		fProcess= null;
		fireTerminateEvent();
	}
		
	/**
	 * @see IProcess#getStreamsProxy()
	 */
	public IStreamsProxy getStreamsProxy() {
		return fStreamsProxy;
	}
	
	/**
	 * Returns the streams proxy associated with this process.
	 * 
	 * @return streams proxy
	 */
	protected IStreamsProxy createStreamsProxy() {
		return new StreamsProxy(getSystemProcess());
	}
	
	/**
	 * Fires a creation event.
	 */
	protected void fireCreationEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CREATE));
	}

	/**
	 * Fires the given debug event.
	 * 
	 * @param event debug event to fire
	 */
	protected void fireEvent(DebugEvent event) {
		DebugPlugin manager= DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[]{event});
		}
	}

	/**
	 * Fires a terminate event.
	 */
	protected void fireTerminateEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
	}

	/**
	 * @see IProcess#setAttribute(String, String)
	 */
	public void setAttribute(String key, String value) {
		if (fAttributes == null) {
			fAttributes = new HashMap(5);
		}
		fAttributes.put(key, value);
	}
	
	/**
	 * @see IProcess#getAttribute(String)
	 */
	public String getAttribute(String key) {
		if (fAttributes == null) {
			return null;
		}
		return (String)fAttributes.get(key);
	}
	
	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IProcess.class)) {
			return this;
		}
		if (adapter.equals(IDebugTarget.class)) {
			ILaunch launch = getLaunch();
			IDebugTarget[] targets = launch.getDebugTargets();
			for (int i = 0; i < targets.length; i++) {
				if (this.equals(targets[i].getProcess())) {
					return targets[i];
				}
			}
			return null;
		}
		return super.getAdapter(adapter);
	}
	/**
	 * @see IProcess#getExitValue()
	 */
	public int getExitValue() throws DebugException {
		if (isTerminated()) {
			return fExitValue;
		} else {
			throw new DebugException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugException.TARGET_REQUEST_FAILED, DebugCoreMessages.getString("RuntimeProcess.Exit_value_not_available_until_process_terminates._1"), null)); //$NON-NLS-1$
		}
	}
	
	/**
	 * Monitors a system process, waiting for it to terminate, and
	 * then notifies the associated runtime process.
	 */
	class ProcessMonitorJob extends Job {
		/**
		 * The underlying <code>java.lang.Process</code> being monitored.
		 */
		protected Process fOSProcess;	
		/**
		 * The <code>IProcess</code> which will be informed when this
		 * monitor detects that the underlying process has terminated.
		 */
		protected RuntimeProcess fProcess;

		/**
		 * The <code>Thread</code> which is monitoring the underlying process.
		 */
		protected Thread fThread;

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus run(IProgressMonitor monitor) {
			fThread = Thread.currentThread();
			while (fOSProcess != null) {
				try {
					fOSProcess.waitFor();
				} catch (InterruptedException ie) {
					// clear interrupted state
					Thread.interrupted();
				} finally {
					fOSProcess = null;
					fProcess.terminated();
				}
			}
			fThread = null;
			return Status.OK_STATUS;
		}

		/**
		 * Creates a new process monitor and starts monitoring the process
		 * for termination.
		 */
		public ProcessMonitorJob(RuntimeProcess process) {
			super(DebugCoreMessages.getString("ProcessMonitorJob.0")); //$NON-NLS-1$
			setPriority(Job.INTERACTIVE);
			setSystem(true);
			fProcess= process;
			fOSProcess= process.getSystemProcess();
			schedule();
		}

		/**
		 * Kills the monitoring thread.
		 * 
		 * This method is to be useful for dealing with the error
		 * case of an underlying process which has not informed this
		 * monitor of its termination.
		 */
		protected void killJob() {
			if (fThread == null) {
				cancel();
			} else {
				fThread.interrupt();
			}
		}
	}	
}