package org.eclipse.ui.externaltools.internal.registry;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Represents the variable for a refresh scope.
 */
public final class RefreshScopeVariable extends ExternalToolVariable {

	/**
	 * Creates a refresh scope variable
	 * 
	 * @param tag the variable tag
	 * @param description a short description of what the variable will expand to
	 * @param element the configuration element
	 */
	/*package*/ RefreshScopeVariable(String tag, String description, IConfigurationElement element) {
		super(tag, description, element);
	}
}
