/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.variables;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * A Variable expander provides a value for a context launch variable.
 * An expander is provided for a variable via extension and
 * is automatically instantiated and queried by the platform to compute
 * the variable's value when required.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see org.eclipse.debug.core.variables.IContextLaunchVariable
 * @since 3.0
 */
public interface IVariableExpander {
	/**
	 * Returns the <code>IResource</code> list
	 * for the given variable tag and value.
	 * 
	 * @param varTag the variable tag name
	 * @param varValue the value for the variable
	 * @param context the context the variable should use to expand itself
	 * @return the list of <code>IResource</code> or <code>null</code> if not
	 * @throws CoreException if the given variable could not be expanded
	 */
	public IResource[] getResources(String varTag, String varValue, ExpandVariableContext context) throws CoreException;
	/**
	 * Returns the expanded text for the given variable
	 * tag and value.
	 * 
	 * @param varTag the variable tag name
	 * @param varValue the value for the variable
	 * @param context the context the variable should use to expand itself
	 * @return the text of the expanded variable
	 * @throws CoreException if the given variable could not be expanded
	 */
	public String getText(String varTag, String varValue, ExpandVariableContext context) throws CoreException;
}
