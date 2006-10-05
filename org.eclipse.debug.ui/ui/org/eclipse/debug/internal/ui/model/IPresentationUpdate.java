/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.model;

import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;

/**
 * A context sensitive update request.
 * 
 * @since 3.3
 */
public interface IPresentationUpdate extends IAsynchronousRequestMonitor {

	/**
	 * Returns the presentation context this update was requested in.
	 * 
	 * @return context this update was requested in
	 */
	public IPresentationContext getPresentationContext();
}
