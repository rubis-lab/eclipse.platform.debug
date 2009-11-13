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

import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

public abstract class AbstractBreakpointManagerInput {
	
	protected IPresentationContext fContext;
	
	protected AbstractBreakpointManagerInput(IPresentationContext context) {
		fContext = context;
	}
		
	public IPresentationContext getContext() {
		return fContext;
	}
}
