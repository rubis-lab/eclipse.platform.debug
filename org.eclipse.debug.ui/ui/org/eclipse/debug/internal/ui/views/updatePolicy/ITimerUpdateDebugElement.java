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

public interface ITimerUpdateDebugElement extends IDebugElement{
	
	/**
	 * @param view
	 * @return if the specified view should be updated
	 */
	public boolean updateView(IDebugViewExtension view);
	
	/**
	 * When called, the debug element should prepare for view update.
	 * @param view
	 */
	public void prepareUpdate(IDebugViewExtension view);
	
	/**
	 * Called after view update is completed
	 * @param view
	 */
	public void postUpdate(IDebugViewExtension view);

}
