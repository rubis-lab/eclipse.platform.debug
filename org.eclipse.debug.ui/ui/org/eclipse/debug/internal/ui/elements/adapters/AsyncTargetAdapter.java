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
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.treeviewer.IPresentationContext;

public class AsyncTargetAdapter extends AbstractAsyncPresentationAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.AbstractAsyncPresentationAdapter#getChildren(java.lang.Object, org.eclipse.debug.internal.ui.treeviewer.IPresentationContext)
	 */
	protected Object[] getChildren(Object parent, IPresentationContext context) throws CoreException {
		return ((IDebugTarget) parent).getThreads();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.elements.adapters.AbstractAsyncPresentationAdapter#hasChildren(java.lang.Object, org.eclipse.debug.internal.ui.treeviewer.IPresentationContext)
	 */
	protected boolean hasChildren(Object element, IPresentationContext context) throws CoreException {
		return ((IDebugTarget)element).hasThreads();
	}

}
