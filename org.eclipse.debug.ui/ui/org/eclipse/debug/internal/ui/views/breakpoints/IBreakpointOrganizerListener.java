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
package org.eclipse.debug.internal.ui.views.breakpoints;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.breakpoints.IBreakpointOrganizer;

public interface IBreakpointOrganizerListener {
	
	/**
	 * Set the breakpoint organizers for the given input.
	 * 
	 * @param input the view input.
	 * @param context the presentation context.
	 * @param organizers the new organizers, can be <code>null</code>.
	 */
	void setOrganizers(Object input, IPresentationContext context, IBreakpointOrganizer[] organizers);	
}
