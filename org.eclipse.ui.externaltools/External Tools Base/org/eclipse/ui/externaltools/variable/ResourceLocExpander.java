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
 * Expands the <code>resource_loc</code> variable location into
 * an <code>IPath</code> to the resource.
 */
public class ResourceLocExpander implements IVariableLocationExpander {

	/**
	 * Create an instance
	 */
	public ResourceLocExpander() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on IVariableLocationExpander.
	 */
	public IPath getPath(String varTag, String varValue, ExpandVariableContext context) {
		IPath path = null;
		if (varValue != null && varValue.length() > 0) {
			IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varValue);
			if (member != null)
				path = member.getLocation();
		} else {
			if (context.getSelectedResource() != null)
				path = context.getSelectedResource().getLocation();
		}
		return path;
	}
}
