/*****************************************************************
 * Copyright (c) 2009 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Initial API and implementation (Bug 238956)
 *****************************************************************/
package org.eclipse.debug.internal.ui.elements.adapters;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

public class DefaultBreakpointManagerInput extends AbstractBreakpointManagerInput {
	private IDebugTarget fTarget;
		
	/**
	 * Constructor - this object wraps input source and the presentation context.
	 * 
	 * @param source the input source.
	 * @param context the presentation context.
	 */
	public DefaultBreakpointManagerInput(Object source, IPresentationContext context) {
		super(context);
		
		if (source instanceof IDebugElement)
			fTarget =  ((IDebugElement) source).getDebugTarget();
		else if (source instanceof ILaunch)
			fTarget = ((ILaunch) source).getDebugTarget();
		else if (source instanceof IProcess) 
			fTarget = ((IProcess) source).getLaunch().getDebugTarget();
		else if (source instanceof IStackFrame)
			fTarget = ((IStackFrame) source).getDebugTarget();
		else 
			fTarget = null;				
	}
	
	/**
	 * Returns the debug target for this input.
	 * 
	 * @return the debug target.
	 */
	public IDebugTarget getTarget() {
		return fTarget;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (fTarget == null && fContext == null) {
			return 1;
		} else if (fTarget != null && fContext != null) {
			return fTarget.hashCode() + fContext.hashCode();
		} else {
			return super.hashCode();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {
		if ((arg0 != null) &&
			arg0.getClass().equals(this.getClass()) &&
			(arg0.getClass().isAssignableFrom(DefaultBreakpointManagerInput.class))) {
			IDebugTarget target = ((DefaultBreakpointManagerInput) arg0).fTarget;
			IPresentationContext context = ((DefaultBreakpointManagerInput) arg0).fContext;
			if (fTarget != null && fContext != null)
				return fTarget.equals(target) && fContext.equals(context);
			else if (fContext != null)
				return fContext.equals(context);
			else
				return false;
		} else {
			return false;
		}		
	}
}
