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

public interface IUpdatePolicySetListener {

	/**
	 * Policy set added to the manager.
	 * @param set
	 */
	public void policySetAdded(IUpdatePolicySet set);
	
	/**
	 * Policy set removed from the manager.
	 * @param set
	 */
	public void policySetRemoved(IUpdatePolicySet set);
	
	/**
	 * Policy set changed
	 * @param set
	 */
	public void policySetChanged(IUpdatePolicySet set);
}
