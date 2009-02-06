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
package org.eclipse.debug.examples.ui.counter;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.examples.core.counter.CounterDebugElement;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;

/**
 * Provides an {@link IToggleBreakpointsTarget} adapter for the Counter debug model example 
 */
public class CounterAdapterFactory implements IAdapterFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType.equals(IToggleBreakpointsTarget.class)) {
			if (adaptableObject instanceof CounterDebugElement) {
				return new CounterToggleBreakpointsTarget();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[]{IToggleBreakpointsTarget.class};
	}

}
