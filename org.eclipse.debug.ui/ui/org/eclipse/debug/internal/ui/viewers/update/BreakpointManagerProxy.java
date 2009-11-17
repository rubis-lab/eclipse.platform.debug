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
package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.internal.ui.elements.adapters.AbstractBreakpointManagerInput;
import org.eclipse.debug.internal.ui.model.elements.AbstractBreakpointManagerContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.ViewerAdapterService;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.internal.ui.views.breakpoints.ElementComparator;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.breakpoints.IBreakpointOrganizer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;

public class BreakpointManagerProxy extends AbstractModelProxy {
	protected AbstractBreakpointManagerContentProvider fProvider;
	protected AbstractBreakpointManagerInput fInput;
	protected IBreakpointOrganizer[] fOrganizers;
	protected ElementComparator fComparator;
	protected IStructuredSelection fSelection;

	public BreakpointManagerProxy(Object input, IPresentationContext context) {
		super();
				
		if (input instanceof AbstractBreakpointManagerInput) {
			fInput = (AbstractBreakpointManagerInput) input;
			
			IElementContentProvider provider = ViewerAdapterService.getContentProvider(input);
			if (provider instanceof AbstractBreakpointManagerContentProvider) {
				fProvider = (AbstractBreakpointManagerContentProvider) provider;
				fOrganizers = (IBreakpointOrganizer[]) context.getProperty(IDebugUIConstants.PROP_BREAKPOINTS_ORGANIZERS);
				fSelection = (IStructuredSelection) context.getProperty(IDebugUIConstants.PROP_BREAKPOINTS_FILTER_SELECTION);
				fComparator = (ElementComparator) context.getProperty(IDebugUIConstants.PROP_BREAKPOINTS_ELEMENT_COMPARATOR);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy#installed(org.eclipse.jface.viewers.Viewer)
	 */
	public void installed(Viewer viewer) {
		super.installed(viewer);
		if (fProvider != null) {
			fProvider.registerModelProxy(fInput, this, fOrganizers, fSelection, fComparator);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy#dispose()
	 */
	public synchronized void dispose() {
		if (fProvider != null) {
			fProvider.unregisterModelProxy(fInput);
		}
		super.dispose();		
	}
}
