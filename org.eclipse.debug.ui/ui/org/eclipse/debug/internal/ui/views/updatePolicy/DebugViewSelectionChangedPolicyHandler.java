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

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

public class DebugViewSelectionChangedPolicyHandler extends
		AbstractSelectionEvtPolicyHandler {


	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		
		if (isDisposed())
			return;
		
		if (part.getSite().getId().equals(getSelectionSrcId()))
		{
			// if this view input is visible, handle this selection changed
			// otherwise, let other selection changed handler handle this event
			if (getView().isVisible(getDebugContext()))
				handleSelectionChanged(part, selection);
				
		}
	}

	public void addSelectionListeners() {
		getView().getSite().getPage().addSelectionListener(getSelectionSrcId(), this);
	}

	public void removeSelectionListeners() {
		getView().getSite().getPage().removeSelectionListener(getSelectionSrcId(), this);
	}
	
	protected String getSelectionSrcId()
	{
		return IDebugUIConstants.ID_DEBUG_VIEW;
	}
	
	protected void handleSelectionChanged(IWorkbenchPart part, ISelection selection)
	{
	}
}
