package org.eclipse.ui.externaltools.internal.registry;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.externaltools.internal.core.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * Registry of all available refresh scope variables.
 */
public class RefreshScopeVariableRegistry extends ExternalToolVariableRegistry {
	/*package*/ static final String TAG_REFRESH_CLASS = "refreshClass"; //$NON-NLS-1$

	/**
	 * Creates the registry and loads the variables.
	 */
	public RefreshScopeVariableRegistry() {
		super(IExternalToolConstants.PL_REFRESH_VARIABLES);
	}

	/**
	 * Returns the refresh scope variable for the given tag
	 * or <code>null</code> if none.
	 */
	public RefreshScopeVariable getRefreshVariable(String tag) {
		return (RefreshScopeVariable) findVariable(tag);
	}
	
	/**
	 * Returns the list of refresh scope variables in the registry.
	 */
	public RefreshScopeVariable[] getRefreshVariables() {
		RefreshScopeVariable[] results = new RefreshScopeVariable[getVariableCount()];
		copyVariables(results);
		return results;
	}
	
	/* (non-Javadoc)
	 * Method declared on ExternalToolVariableRegistry.
	 */
	protected ExternalToolVariable newVariable(String tag, String description, IConfigurationElement element) {
		return new RefreshScopeVariable(tag, description, element);
	}
	
	/* (non-Javadoc)
	 * Method declared on ExternalToolVariableRegistry.
	 */
	protected boolean validateElement(IConfigurationElement element) {
		String className = element.getAttribute(TAG_REFRESH_CLASS);
		if (className == null || className.length() == 0) {
			ExternalToolsPlugin.getDefault().log("Missing refresh class attribute value for variable element.", null); //$NON-NLS-1$
			return false;
		}
		return true;
	}
}
