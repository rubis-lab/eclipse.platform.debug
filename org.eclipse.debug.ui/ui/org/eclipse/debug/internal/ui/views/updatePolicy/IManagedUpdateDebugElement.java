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


/**
 * Clients may implement this interface to indicate that its 
 * debug element would manage its own update.  Views would not update
 * based on debug events.
 */
public interface IManagedUpdateDebugElement extends IDebugElement {
	/**
	 * @param viewId
	 * @return true if the element supports update for the
	 * specified view
	 */
	public boolean supportsUpdate(String viewId);
	
	/**
	 * Returns the handler to be used for this view.
	 * The upater will be notified when the view becomes visible / hidden.
	 * @param view - the view requiring an handler
	 * @return the handler to be used for this view
	 */
	public IUpdatePolicyHandler connect(IDebugViewExtension view);
	
	/**
	 * Called when the element is no longer the input to the view.
	 * @param view - the view that has changed input
	 * @param handler - the handler registered to this view
	 */
	public void disconnect(IDebugViewExtension view, IUpdatePolicyHandler handler);
}
