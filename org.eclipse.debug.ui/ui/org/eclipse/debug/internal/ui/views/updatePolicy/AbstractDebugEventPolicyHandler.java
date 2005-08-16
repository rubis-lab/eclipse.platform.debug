/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.updatePolicy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.ui.progress.UIJob;

public abstract class AbstractDebugEventPolicyHandler extends AbstractUpdatePolicyHandler implements IDebugEventSetListener{
	
	/**
	 * Queued debug event sets (arrays of events) to process.
	 */
	private List fEventSetQueue = new ArrayList();
	
	/**
	 * Queued data associated with event sets. Entries may be <code>null</code>.
	 */
	private List fDataQueue = new ArrayList();
	
	/**
	 * Lock to add to/remove from data and event queues.
	 */
	private Object LOCK = new Object();
	
	/**
	 * Update job 
	 */
	private EventProcessingJob fUpdateJob = new EventProcessingJob();
	
	/**
	 * Empty event set constant
	 */
	protected static final DebugEvent[] EMPTY_EVENT_SET = new DebugEvent[0];
	
	private Object NULL = new Object();
	
	private boolean fHandleDebugEvents;
	
	/**
	 * Job to dispatch debug event sets
	 */
	private class EventProcessingJob extends UIJob {

        private static final int TIMEOUT = 200;
        
	    public EventProcessingJob() {
	        super(DebugUIViewsMessages.AbstractDebugEventHandler_0); //$NON-NLS-1$
	        setSystem(true);
	        setPriority(Job.INTERACTIVE);
	    }
	    
        /* (non-Javadoc)
         * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
         */
        public IStatus runInUIThread(IProgressMonitor monitor) {
            boolean more = true;
            long start = System.currentTimeMillis();
            // to avoid blocking the UI thread, process a max of 50 event sets at once
            while (more) {
                DebugEvent[] eventSet = null;
                Object data = null;
			    synchronized (LOCK) {
			        if (fEventSetQueue.isEmpty()) {
			            return Status.OK_STATUS;
			        }
			        eventSet = (DebugEvent[]) fEventSetQueue.remove(0);
			        more = !fEventSetQueue.isEmpty();
			        data = fDataQueue.remove(0);
			        if (data == NULL) {
			            data = null;
			        }
			    }
			    
			    doHandleDebugEvents(eventSet, data);
                
                if (more) {
                    long current = System.currentTimeMillis();
                    if (current - start > TIMEOUT) {
                        break;
                    }
                }
            }
            if (more) {
                // re-schedule with a delay if there are still events to process 
                schedule(50);
            }
            return Status.OK_STATUS;
        }
	    
	}

	/**
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		
		if (!fHandleDebugEvents)
			return;

		// filter events
		events = filterEvents(events);
		if (events.length == 0) {
		    return;
		}
		synchronized (LOCK) {
			events = doPreprocessEvents(events);
			if (events.length == 0) {
			    return;
			}
			// add the event set to the queue and schedule update
		    fEventSetQueue.add(events);
	        if (fDataQueue.size() < fEventSetQueue.size()) {
	            fDataQueue.add(NULL);
	        }
		}
		fUpdateJob.schedule();
	}
	
	protected void queueData(Object data) {
	    synchronized (LOCK) {
	        fDataQueue.add(data);
        }
	}
	
	protected DebugEvent[] doPreprocessEvents(DebugEvent[] events) {
	    return events;
	}
	
	/**
	 * Filters the given events before processing.
	 *  
	 * @param events event set received for processing
	 * @return events to be processed
	 */
	protected DebugEvent[] filterEvents(DebugEvent[] events) {
	    return events;
	}
	
	
	/**
	 * Implementation specific handling of debug events.
	 * Subclasses should override.
	 */
	protected abstract void doHandleDebugEvents(DebugEvent[] events, Object data);	
		
	
	/**
	 * De-registers this event handler from the debug model.
	 */
	public void dispose() {
		DebugPlugin plugin= DebugPlugin.getDefault();
		plugin.removeDebugEventListener(this);
		synchronized (LOCK) {
			fEventSetQueue.clear();
			fDataQueue.clear();
		}
		super.dispose();
	}

	public void init(IUpdatePolicy policy, IDebugViewExtension view) {
		super.init(policy, view);
		
		fHandleDebugEvents = view.isVisible();

		// add debug event listener
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	public void becomesHidden(IDebugViewExtension view) {

		fHandleDebugEvents = false;
		super.becomesHidden(view);
	}

	public void becomesVisible(IDebugViewExtension view) {
		
		fHandleDebugEvents = true;
		super.becomesVisible(view);
	}
}
