package org.eclipse.ui.externaltools.internal.registry;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.externaltools.variable.IVariableLocation;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

/**
 * Represents the variable for the path location
 */
public final class PathLocationVariable extends ExternalToolVariable {
	private static final DefaultVariableLocation defaultLocation = new DefaultVariableLocation();
	
	private IVariableLocation location = null;

	/**
	 * Creates a path location variable
	 * 
	 * @param tag the variable tag
	 * @param description a short description of what the variable will expand to
	 * @param element the configuration element
	 */
	/*package*/ PathLocationVariable(String tag, String description, IConfigurationElement element) {
		super(tag, description, element);
	}

	/**
	 * Returns the object that can expand the variable
	 * into a path location.
	 */
	public IVariableLocation getLocation() {
		if (location == null) {
			location = (IVariableLocation) createObject(PathLocationVariableRegistry.TAG_LOCATION_CLASS);
			if (location == null)
				location = defaultLocation;
		}
		return location;
	}


	/**
	 * Default variable location implementation which does does
	 * not expand variables, but just returns <code>null</code>.
	 */	
	private static final class DefaultVariableLocation implements IVariableLocation {
		/* (non-Javadoc)
		 * Method declared on IVariableLocation.
		 */
		public IPath getPath(String varTag, String varValue, ExpandVariableContext context) {
			return null;
		}
	}
}
