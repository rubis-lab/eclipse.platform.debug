/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.core.counter;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

/**
 * Thread for the counting debug model example.
 */
public class CounterThread extends CounterDebugElement implements IThread {
	
	// Thread state
	private boolean fTerminated = false;
	private boolean fSuspended = false;
	private boolean fStepping = false;
	
	// Counter
	int fCount = 0;
	
	// simulates an executing thread
	class Execution implements Runnable {
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}
				fCount++;
			}
		}
		
	}
	
	// the execution and its thread
	private Execution fExecution;
	private Thread fExecThread;
	
	// simulates a step over
	class StepOver implements Runnable {

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			fireResumeEvent(DebugEvent.STEP_OVER);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				synchronized (CounterThread.this) {
					fStepping = false;
					fSuspended = true;					
				}
				fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
				return;
			}
			fCount++;
			synchronized (CounterThread.this) {
				fStepping = false;
				fSuspended = true;
			}
			fireSuspendEvent(DebugEvent.STEP_END);
		}
		
	}

	/**
	 * Constructs a thread in the given target.
	 * 
	 * @param target debug target
	 */
	public CounterThread(IDebugTarget target) {
		super(target);
		fExecution = new Execution();
		fExecThread = new Thread(fExecution);
		fExecThread.start();
		fireCreationEvent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getBreakpoints()
	 */
	public IBreakpoint[] getBreakpoints() {
		// TODO:
		return new IBreakpoint[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getName()
	 */
	public String getName() throws DebugException {
		return "Counting Thread";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getPriority()
	 */
	public int getPriority() throws DebugException {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getStackFrames()
	 */
	public IStackFrame[] getStackFrames() throws DebugException {
		synchronized (this) {
			if (isSuspended()) {
				return new IStackFrame[]{new CounterStackFrame(this)};
			}
		}
		return new IStackFrame[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#getTopStackFrame()
	 */
	public IStackFrame getTopStackFrame() throws DebugException {
		synchronized (this) {
			if (isSuspended()) {
				return new CounterStackFrame(this);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IThread#hasStackFrames()
	 */
	public boolean hasStackFrames() throws DebugException {
		return isSuspended() && !isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public synchronized boolean canResume() {
		return isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public synchronized boolean canSuspend() {
		return !isTerminated() && !isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public synchronized boolean isSuspended() {
		return fSuspended && !isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		synchronized (this) {
			fExecThread = new Thread(fExecution);
			fExecThread.start();
			fSuspended = false;
		}		
		fireResumeEvent(DebugEvent.CLIENT_REQUEST);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		synchronized (this) {
			fExecThread.interrupt();
			fSuspended = true;	
		}
		fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepInto()
	 */
	public boolean canStepInto() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepOver()
	 */
	public boolean canStepOver() {
		return isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#canStepReturn()
	 */
	public boolean canStepReturn() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#isStepping()
	 */
	public synchronized boolean isStepping() {
		return fStepping;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepInto()
	 */
	public void stepInto() throws DebugException {
		notSupported("Step into not supported", null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepOver()
	 */
	public void stepOver() throws DebugException {
		synchronized (this) {
			StepOver step = new StepOver();
			fExecThread = new Thread(step);
			fStepping = true;
			fSuspended = false;
			fExecThread.start();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IStep#stepReturn()
	 */
	public void stepReturn() throws DebugException {
		notSupported("Step return not supported", null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return !isTerminated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public synchronized boolean isTerminated() {
		return fTerminated;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		synchronized (this) {
			fExecThread.interrupt();
			fTerminated = true;
		}
		fireTerminateEvent();
	}

}
