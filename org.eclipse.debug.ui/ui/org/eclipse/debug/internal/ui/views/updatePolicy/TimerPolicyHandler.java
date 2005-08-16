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

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.ui.progress.UIJob;

/**
 * Update view at a fixed time interval when the debug program is runnning
 *
 */
public class TimerPolicyHandler extends AbstractDebugEventPolicyHandler {

	private long fTimeInterval;
	private Timer fTimer;
	private UpdateTask fUpdateTask;
	
	class ViewUpdateJob extends UIJob
	{
		public ViewUpdateJob(String name) {
			super(name);
		}

		public IStatus runInUIThread(IProgressMonitor monitor) {
			
			if (!isDisposed())
			{
//				System.out.println("update job " + System.currentTimeMillis());
				getView().refresh(getDebugContext(), true);
			}
			return Status.OK_STATUS;
		}
	}
	
	class UpdateTask extends TimerTask
	{
		public void run() {
			
			final ITimerUpdateDebugElement elm = (ITimerUpdateDebugElement)getDebugContext().getAdapter(ITimerUpdateDebugElement.class);
			if (elm != null && elm.updateView(getView()))
			{
				final IDebugViewExtension view = getView();
				getView().addUpdateListener(new IDebugViewUpdateListener() {
					public void viewUpdated() {
						elm.postUpdate(view);		
						view.removeUpdateListener(this);
					}});
				

				elm.prepareUpdate(view);
				ViewUpdateJob viewJob = new ViewUpdateJob("Update Job"); //$NON-NLS-1$
				viewJob.setSystem(true);
				viewJob.schedule();
			}
		}
	}
	 
	public TimerPolicyHandler()
	{
		fTimeInterval =  getTimeInterval();
	}
	
	public void setDebugContext(IDebugElement input) {
		super.setDebugContext(input);
		
		if (getDebugContext() != null)
		{
			if (!getDebugContext().getDebugTarget().isSuspended() && 
				!getDebugContext().getDebugTarget().isTerminated() &&
				!getDebugContext().getDebugTarget().isDisconnected())
				startTimer();
		}
	}

	public void setTimeInterval (long timeInterval)
	{
		fTimeInterval = timeInterval;
	}
	
	protected void doHandleDebugEvents(DebugEvent[] events, Object data) {
		
		if (getDebugContext() == null)
			return;
		
		for (int i = 0; i < events.length; i++) {	
			DebugEvent event = events[i];
			if (event.getSource() instanceof IDebugElement)
			{
				IDebugElement src = (IDebugElement)event.getSource();
				if (src.getModelIdentifier().equals(getDebugContext().getModelIdentifier())
						&& !event.isEvaluation())
				{
					switch (event.getKind()) {
						case DebugEvent.SUSPEND:
							stopTimer();
							break;
						case DebugEvent.RESUME:
							startTimer();
							break;
						case DebugEvent.TERMINATE:
							stopTimer();
							break;
					}
				}
			}
		}
	}
	
	protected void startTimer()
	{
		if (fTimer == null)
		{
			fTimer = new Timer();
			fUpdateTask = new UpdateTask();
			fTimer.schedule(fUpdateTask, 300, fTimeInterval);
		}
	}
	
	protected void stopTimer()
	{
		if (fTimer != null)
		{
			fTimer.cancel();
			fTimer = null;
			fUpdateTask = null;
		}
	}
	
	public void becomesHidden(IDebugViewExtension view) {
		stopTimer();
		super.becomesHidden(view);
	}

	public void becomesVisible(IDebugViewExtension view) {
		IDebugTarget target = getDebugContext().getDebugTarget();
		if (!target.isSuspended() && !target.isDisconnected() && !target.isTerminated())			
			startTimer();
		
		super.becomesVisible(view);
	}

	protected long getTimeInterval()
	{
		// TODO:  make it user-configurable
		return 500;
	}
	
	public void dispose() {
		stopTimer();
		super.dispose();
	}
}
