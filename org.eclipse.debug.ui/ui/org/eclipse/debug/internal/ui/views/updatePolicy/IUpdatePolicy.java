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

import org.eclipse.core.runtime.CoreException;

/**
 * Represent an update policy for a view with
 * content from a model.  
 * 
 * Clients are not expected to implement this interface.
 *
 * @since 3.2
 */
public interface IUpdatePolicy {

	/**
	 * @return name of this update policy.  May return null
	 * if this policy is hidden.
	 */
	public String getName();
	
	/**
	 * @return description of this update policy.  May return null
	 * if this policy is hidden.
	 */
	public String getDescription();
	
	/**
	 * @return the view udpater for this update policy.  Client 
	 * is to initialize the returned handler.
	 * @throws CoreException if there is an error creating
	 * the handler.
	 */
	public IUpdatePolicyHandler createHandler() throws CoreException;
	
	/**
	 * @return id of this policy
	 */
	public String getId();
}
