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

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * Handle update when the view input is changed
 *
 */
public class DebugContextChangedPolicyHandler extends AbstractUpdatePolicyHandler {
	
	public void setDebugContext(IDebugElement debugContext) {
		super.setDebugContext(debugContext);
		
		if (!getView().isVisible())
			return;
		
		if (debugContext instanceof IStackFrame)
			getView().refresh(debugContext, true);
		else
		{
			// TODO:  hack for making the timer policy hanlder to work.
			// Need flexible hierarchy.
			IStackFrame sf = (IStackFrame)debugContext.getAdapter(IStackFrame.class);
			getView().refresh(sf, true);
		}
	}

}
