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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.Breakpoint;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Breakpoint that suspends when the thread counter reaches a specific value.
 */
public class LimitBreakpoint extends Breakpoint {
	
	/**
	 * Attribute key for breakpoint threshold/limit
	 */
	public static final String LIMIT = "LIMIT";
	
	/**
	 * Marker type for limit breakpoint
	 */
	public static final String LIMIT_TYPE = "org.eclipse.debug.examples.core.counter.markerType.limit";
		
	/**
	 * Default constructor is required for the breakpoint manager
	 * to re-create persisted breakpoints. After instantiating a breakpoint,
	 * the <code>setMarker(...)</code> method is called to restore
	 * this breakpoint's attributes.
	 */
	public LimitBreakpoint() {
	}
	
	/**
	 * Constructs a limit breakpoint.
	 * 
	 * @throws DebugException if unable to 
	 */
	public LimitBreakpoint(final int limit) throws DebugException {
		final IResource resource = ResourcesPlugin.getWorkspace().getRoot();
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IMarker marker = resource.createMarker(LIMIT_TYPE);
				setMarker(marker);
				marker.setAttribute(IBreakpoint.ENABLED, Boolean.TRUE);
				marker.setAttribute(IBreakpoint.ID, getModelIdentifier());
				marker.setAttribute(LIMIT, limit);
				marker.setAttribute(IMarker.MESSAGE, "Limit Breakpoint: " + limit);
			}
		};
		run(getMarkerRule(resource), runnable);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IBreakpoint#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return CounterDebugElement.COUNTER_MODEL_ID;
	}

	/**
	 * Returns this breakpoint's threshold value.
	 * 
	 * @return limit/threshold value
	 * @throws CoreException if unable to retrieve the value
	 */
	public int getLimit() throws CoreException {
		return ensureMarker().getAttribute(LIMIT, 0);
	}
}
