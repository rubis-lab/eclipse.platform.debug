package org.eclipse.ui.externaltools.variable;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * Expands the <code>resource_loc</code> variable into the desired
 * result format.
 */
public class ResourceLocExpander implements IVariableLocationExpander, IVariableResourceExpander {

	/**
	 * Create an instance
	 */
	public ResourceLocExpander() {
		super();
	}

	/**
	 * Expands the variable to a resource.
	 */
	private IResource expand(String varValue, ExpandVariableContext context) {
		if (varValue != null && varValue.length() > 0)
			return ResourcesPlugin.getWorkspace().getRoot().findMember(varValue);
		else
			return context.getSelectedResource();
	}
	
	/* (non-Javadoc)
	 * Method declared on IVariableLocationExpander.
	 */
	public IPath getPath(String varTag, String varValue, ExpandVariableContext context) {
		IResource resource = expand(varValue, context);
		if (resource != null)
			return resource.getLocation();
		else
			return null;
	}

	/* (non-Javadoc)
	 * Method declared on IVariableResourceExpander.
	 */
	public IResource[] getResources(String varTag, String varValue, ExpandVariableContext context) {
		IResource resource = expand(varValue, context);
		if (resource != null)
			return new IResource[] {resource};
		else
			return null;
	}
}
