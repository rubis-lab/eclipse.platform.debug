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

import org.eclipse.debug.core.DebugException;

public interface IUpdatePolicyManager {

	/**
	 * @param viewId
	 * @param modelId
	 * @return the policy sets applicable to the given view id and model id
	 */
	IUpdatePolicySet[] getPolicySets(String viewId, String modelId);
	
	/**
	 * @return all policy sets
	 */
	IUpdatePolicySet[] getAllPolicySets();
	
	/**
	 * @param viewId
	 * @param modelId
	 * @return the primary policy set applicable to the given view id and model id
	 * Return null if none is found.
	 */
	IUpdatePolicySet getPrimaryPolicySet(String viewId, String modelId);
	
	/**
	 * @param id
	 * @return the policy set matching the given id.  Return null if none is found.
	 */
	IUpdatePolicySet getPolicySet(String id);
	
	/**
	 * @param policyId
	 * @return the policy with the given policy id
	 */
	IUpdatePolicy getPolicy(String policyId);
	
	/**
	 * @return all update policies
	 */
	IUpdatePolicy[] getAllPolicies();
	
	/**
	 * @param policySet
	 * @throws DebugException if error has occurred adding the policy set
	 */
	public void addPolicySet(IUpdatePolicySet policySet) throws DebugException;
	
	/**
	 * @param policySet
	 * @throws DebugException if error has occurred removing the policy set
	 */
	public void removePolicySet(IUpdatePolicySet policySet) throws DebugException;
	
	/**
	 * Notify UpdatePolicyManager that the policy set has changed.
	 * This will cause the policy set manager to fire a change event
	 * @param policySet the policy set changed
	 */
	public void policySetChanged(IUpdatePolicySet policySet);
	
	/**
	 * @param listener listener to be notfied when user-defined policy set are added/removed from the manager
	 */
	public void addPolicySetListener(IUpdatePolicySetListener listener);
	
	/**
	 * @param listener listener to remove
	 */
	public void removePolicySetListener(IUpdatePolicySetListener listener);
}
