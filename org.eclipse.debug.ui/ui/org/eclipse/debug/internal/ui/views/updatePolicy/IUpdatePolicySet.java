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
	 * @param ids set new policies in the policy set
	 *
	 */
	public void setPolicies(String[] ids);
	
	/**
	 * @return name of the policy set
	 */
	public String getName();
	
	/**
	 * Sets the name of this policy set
	 * @param name
	 */
	public void setName(String name);
	
	/**
	 * @return description of the policy set
	 */
	public String getDescription();

	/**
	 * Sets the description of this policy set
	 */
	public void setDescription(String description);
	
	/**
	 * @return if the policy is hidden
	 */
	public boolean isHidden();
	
	/**
	 * @return if policy set is primary
	 */
	public boolean isPrimary();
	
	/**
	 * @return the id of this policy set
	 */
	public String getId();
	
	/**
	 * @return if the policy set is editable.  A policy set is editable
	 * if it is added by an user.
	 */
	public boolean canEdit();
	
	/**
	 * @return if the policy set is removable.  A policy set is removable
	 * if it is added by an user.
	 */
	public boolean canRemove();

}
