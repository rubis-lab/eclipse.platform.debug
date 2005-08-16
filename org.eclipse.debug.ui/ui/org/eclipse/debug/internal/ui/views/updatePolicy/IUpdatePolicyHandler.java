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

/*
  View handlers are required to implement this interface.  Once registered with
  a view, the handler will start to receive becoming visible/hidden events from the view.
*/

public interface IUpdatePolicyHandler {
	/**
	 * Initialize the handler with the view and the element
	 * that is the input to the view.
	 * @param view
	 * @param viewInput
	 */
	public void init(IUpdatePolicy policy, IDebugViewExtension view);
	
	/**
	 * Sets the debug context for the view updater
	 * 
	 * @param debugContext
	 */
	public void setDebugContext(IDebugElement debugContext);
	
	/**
	 * @return the debug context this handler is responsible for
	 */
	public IDebugElement getDebugContext();
	
	/**
	 * Called when the specified view becomes visible
	 * @param view
	 */
	public void becomesVisible(IDebugViewExtension view);
	
	/**
	 * Called when the specified view becomes hidden
	 * @param view
	 */
	public void becomesHidden(IDebugViewExtension view);
	
	/**
	 * Called when the specified objects become visible within the view.
	 * TODO:  not sure if this is possible as it may be expensive to keep
	 * track of visible objects
	 * @param obj
	 */
	public void becomesVisible(Object[] obj);
	
	/**
	 * Called when the specified objects become hidden within the view.
	 * TODO:  not sure if this is possible as it may be expensive to keep
	 * track of visible objects
	 * @param obj
	 */
	public void becomesHidden(Object[] obj);
	
	/**
	 * @return true if the view is currently visible, false otherwise.
	 */
	public boolean isVisible();
	
	/**
	 * @param obj
	 * @return true if the object is visible in the view, false otherwise.
	 */
	public boolean isVisible(Object obj);
	
	/**
	 * Called when the view is disposed or when the handler
	 * should no longer be used by the debug view.
	 */
	public void dispose();
	
	/**
	 * @return the view that this handler will update
	 */
	public IDebugViewExtension getView();
	
	/**
	 * @return the view id of the view that this handler is registered to
	 */
	public String getViewId();
	
	/**
	 * Call to update the view that this handler is responsible for
	 * @param elm - the element to update or null if the entire view is to be updated.
	 * @param getContent - set to true if the content of the debug element and its children
	 * needs to be retreived.  Set to false if onnly the labels of the debug elements are to
	 * be updated.
	 */
	public void updateView(IDebugElement elm, boolean getContent);
	
	/**
	 * @return the policy this handler is handling.
	 */
	public IUpdatePolicy getPolicy();
}
