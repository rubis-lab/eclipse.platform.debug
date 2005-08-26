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

public interface IUpdatePolicyManager {

	/**
	 * @param viewId
	 * @param modelId
	 * @return the policy sets applicable to the given view id and model id
	 */
	IUpdatePolicySet[] getPolicySets(String viewId, String modelId);
	
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
}
