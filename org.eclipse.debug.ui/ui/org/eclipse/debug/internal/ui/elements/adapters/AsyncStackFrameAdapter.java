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

package org.eclipse.debug.internal.ui.elements.adapters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.treeviewer.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;

public class AsyncStackFrameAdapter extends AbstractAsyncPresentationAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.AbstractAsyncPresentationAdapter#getChildren(java.lang.Object, org.eclipse.debug.internal.ui.treeviewer.IPresentationContext)
	 */
	protected Object[] getChildren(Object parent, IPresentationContext context) throws CoreException {
        String id = context.getPart().getSite().getId();
        IStackFrame frame = (IStackFrame) parent;
        if (id.equals(IDebugUIConstants.ID_VARIABLE_VIEW)) {
            return frame.getVariables();
        } else if (id.equals(IDebugUIConstants.ID_REGISTER_VIEW)) {
            return frame.getRegisterGroups();
        }
        return EMPTY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.AbstractAsyncPresentationAdapter#hasChildren(java.lang.Object, org.eclipse.debug.internal.ui.treeviewer.IPresentationContext)
	 */
	protected boolean hasChildren(Object element, IPresentationContext context) throws CoreException {
        String id = context.getPart().getSite().getId();
        IStackFrame frame = (IStackFrame) element;
        if (id.equals(IDebugUIConstants.ID_VARIABLE_VIEW)) {
            return frame.hasVariables();
        } else if (id.equals(IDebugUIConstants.ID_REGISTER_VIEW)) {
            return frame.hasRegisterGroups();
        }
        return false;
	}
    
    

}
