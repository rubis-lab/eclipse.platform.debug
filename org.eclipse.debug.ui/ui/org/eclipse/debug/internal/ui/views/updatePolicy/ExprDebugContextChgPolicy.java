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

public class ExprDebugContextChgPolicy implements IUpdatePolicy {
	
	public static final String POLICY_ID = "org.eclipse.debug.ui.ExprDebugContextChgPolicy"; //$NON-NLS-1$

	public String getName() {
		return null;
	}

	public String getDescription() {
		return null;
	}

	public boolean isHidden() {
		return true;
	}

	public IUpdatePolicyHandler createHandler() throws CoreException {
		return new ExprDebugContextChgPolicyHandler();
	}

	public String getId() {
		return POLICY_ID;
	}


}
