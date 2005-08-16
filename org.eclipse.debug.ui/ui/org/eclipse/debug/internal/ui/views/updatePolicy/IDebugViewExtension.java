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
import org.eclipse.debug.ui.IDebugView;

/**
 *
 */
public interface IDebugViewExtension extends IDebugView{
	
	/**
	 * Called when the view needs to be refreshed.  The sepcified element and its children
	 * would be updated.  
	 * @param elm - debug element to update, null if the entire view is to be updated
	 * @param getContent - set to true if the view needs to get content
	 * from the model.  Set to false if only the labels of the content needs to be updated
	 */
	public void refresh(IDebugElement elm, boolean getContent);
	
	/**
	 * Cancel all pending refresh that the view may have scheduled.
	 * Called when the target has stepped away.
	 */
	public void cancelPendingRefresh();
	
	/**
	 * Add listener to be notified when the view has finished update
	 * @param listener
	 */
	public void addUpdateListener(IDebugViewUpdateListener listener);
	
	/**
	 * Remove listener from view update notification
	 * @param listener
	 */
	public void removeUpdateListener(IDebugViewUpdateListener listener);
	
	/**
	 * @return a list of view update listeners
	 */
	public IDebugViewUpdateListener[] getUpdateListeners();
	
	/**
	 * Clear any cached information that the view may have when called.
	 * e.g. tree expansion from the view
	 */
	public void clearCache(Object obj);
	
	/**
	 * @return if the view is visible
	 */
	public boolean isVisible();
	
	/**
	 * @param obj
	 * @return if the selected objects are visible in the view
	 */
	public boolean isVisible(Object obj);
	
	/**
	 * @return a list of update policies to use when a model has not define
	 * any update policy for a view.
	 */
	public IUpdatePolicy[] getDefaultUpdatePolicies();
	
	/**
	 * @return a list of update policies that should be active
	 * all the time.
	 */
	public IUpdatePolicy[] getMandatoryUpdatePolicies();
}
