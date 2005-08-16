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

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

public class DebugEventPolicyHandler extends AbstractDebugEventPolicyHandler {
	
	/**
	 * @see AbstractDebugEventHandler#handleDebugEvents(DebugEvent[])
	 */
	protected void doHandleDebugEvents(DebugEvent[] events, Object data) {
		
		if (isDisposed())
			return;
		
		for (int i = 0; i < events.length; i++) {	
			DebugEvent event = events[i];
			switch (event.getKind()) {
				case DebugEvent.SUSPEND:
						doHandleSuspendEvent(event);
					break;
				case DebugEvent.CHANGE:
						doHandleChangeEvent(event);
					break;
				case DebugEvent.RESUME:
						doHandleResumeEvent(event);
					break;
				case DebugEvent.TERMINATE:
					doHandleTerminateEvent(event);
					break;					
			}
		}
	}

	/**
	 * Clear cached variable expansion state
	 */
	protected void doHandleResumeEvent(DebugEvent event) {
		if (!event.isStepStart() && !event.isEvaluation()) {
			getView().clearCache(event.getSource());
		}
		if (!event.isEvaluation()) {
			getView().cancelPendingRefresh();
		}
	}

	/**
	 * Clear any cached variable expansion state for the
	 * terminated thread/target. Also, remove the part listener if there are
	 * no more active debug targets.
	 */
	protected void doHandleTerminateEvent(DebugEvent event) {
		getView().clearCache(event.getSource());
	}
	
	/**
	 * Process a SUSPEND event
	 */
	protected void doHandleSuspendEvent(DebugEvent event) {
		if (!event.isEvaluation()) {
			// Don't refresh everytime an implicit evaluation finishes
			if (event.getSource() instanceof ISuspendResume) {
				if (!((ISuspendResume)event.getSource()).isSuspended()) {
					// no longer suspended
					return;
				}
			}
			getView().refresh((IDebugElement)event.getSource(), true);
		}		
	}
	
	/**
	 * Process a CHANGE event
	 */
	protected void doHandleChangeEvent(DebugEvent event) {
		if (event.getDetail() == DebugEvent.STATE) {
			// only process variable state changes
			if (event.getSource() instanceof IVariable) {
				getView().refresh((IDebugElement)event.getSource(), true);
			}
		} else {
			if (!(event.getSource() instanceof IExpression)) {
				if (event.getSource() instanceof IVariable) {
					getView().refresh((IDebugElement)event.getSource(), true);
				} else {
					getView().refresh(getDebugContext(), true);
				}
			}
		}	
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.AbstractDebugEventHandler#filterEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	protected DebugEvent[] filterEvents(DebugEvent[] events) {
		ArrayList filtered = null;
		for (int i=0; i<events.length; i++) {
			if (isFiltered(events[i])) {
				if (events.length == 1) {
					return EMPTY_EVENT_SET;
				}
				if (filtered == null) {
					filtered = new ArrayList();
				}
				filtered.add(events[i]);
			}
		}
		if (filtered == null) {
			return events;
		}
		if (filtered.size() == events.length) {
			return EMPTY_EVENT_SET;
		}
		ArrayList all = new ArrayList(events.length);
		for (int i = 0; i < events.length; i++) {
			all.add(events[i]);
		}
		all.removeAll(filtered);
		return (DebugEvent[]) all.toArray(new DebugEvent[all.size()]);
	}
	
	protected boolean isFiltered(DebugEvent event) {
		if (event.getKind() == DebugEvent.CHANGE) {
			Object source = event.getSource();
			switch (event.getDetail()) {
				case DebugEvent.CONTENT:
					if (source instanceof IVariable ||
						source instanceof IStackFrame ||
						source instanceof IThread ||
						source instanceof IDebugTarget) {
						return false;
					}
					return true;
				case DebugEvent.STATE:
					if (source instanceof IVariable) {
						return false;
					}
					return true;
				default: // UNSPECIFIED
					return true;
			}
		}
		return false;
	}

}
