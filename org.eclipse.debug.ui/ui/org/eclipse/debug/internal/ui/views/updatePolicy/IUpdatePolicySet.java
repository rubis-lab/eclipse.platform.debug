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

/**
 * Represent a collection of update policies for a view and model.
 *
 */
public interface IUpdatePolicySet {
	/**
	 * @return view id that this policy should be applied to.
	 */
	public String getViewId();
	
	/**
	 * @return model identifier this policy is bound to
	 */
	public String getModelId();
	
	/**
	 * @return a list of update policies
	 */
	public String[] getPolicies();
	
	/**
	 * @return name of the policy set
	 */
	public String getName();
	
	/**
	 * @return description of the policy set
	 */
	public String getDescription();
	
	/**
	 * @return if the policy is hidden
	 */
	public boolean isHidden();

}
