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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.ui.progress.UIJob;

public class DelayedDebugEvtPolicyHandler extends DebugEventPolicyHandler {

	DelayedUpdateJob fUpdateJob;
	public class DelayedUpdateJob extends UIJob
	{
		private DebugEvent fEvent;
		
		public DelayedUpdateJob()
		{
			super("Delayed Update Job"); //$NON-NLS-1$
		}

		public IStatus runInUIThread(IProgressMonitor monitor) {
			
			if (isDisposed())
				return Status.OK_STATUS;
			
			if (fEvent == null)
				return Status.OK_STATUS;
			
			if (fEvent.getKind() == DebugEvent.SUSPEND)
				doHandleSuspendEvent(fEvent);	
			else if (fEvent.getKind() == DebugEvent.CHANGE)
				doHandleChangeEvent(fEvent);
			return Status.OK_STATUS;
		}
		
		public void setEvent(DebugEvent event)
		{
			fEvent = event;
		}
	}
	
	protected void doHandleDebugEvents(DebugEvent[] events, Object data) {
		
		for (int i=0; i<events.length; i++)
		{
			if (events[i].getKind() == DebugEvent.SUSPEND)
			{
				if (!events[i].isEvaluation())
				{
					fUpdateJob.cancel();
					fUpdateJob.setEvent(events[i]);
					fUpdateJob.schedule(getDelay());
				}
			}
			else if (events[i].getKind() == DebugEvent.CHANGE)
			{
				fUpdateJob.cancel();
				fUpdateJob.setEvent(events[i]);
				fUpdateJob.schedule(getDelay());
			}
			else
			{
				if (!events[i].isEvaluation())
				{
					fUpdateJob.cancel();
					super.doHandleDebugEvents(events, data);
				}
			}
		}
	}


	public void init(IUpdatePolicy policy, IDebugViewExtension view) {
		super.init(policy, view);
		fUpdateJob = new DelayedUpdateJob();
		fUpdateJob.setSystem(true);
	}
	
	public long getDelay()
	{
		return 300;
	}
}
