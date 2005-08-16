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

import org.eclipse.ui.ISelectionListener;

public abstract class AbstractSelectionEvtPolicyHandler extends AbstractUpdatePolicyHandler implements ISelectionListener{
	
	public void init(IUpdatePolicy policy, IDebugViewExtension view) {
		super.init(policy, view);
		addSelectionListeners();
	}
	
	/**
	 * add selection listeners for this handler to start receiving
	 * selection changed events
	 */
	abstract public void addSelectionListeners();
	
	/**
	 * remove selection listeners for this handler to stop
	 * receiving selection changed events.
	 */
	abstract public void removeSelectionListeners();

	public void dispose() {
		removeSelectionListeners();
		super.dispose();
	}

}
