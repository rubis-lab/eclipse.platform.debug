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
package org.eclipse.debug.internal.ui.treeviewer;

/**
 * Children update request. A presentation adapter adds children to
 * this object as they become available.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @since 3.2
 */
public interface IChildrenUpdate extends IPresentationUpdate {

	/**
	 * Adds the given child to this update request.
	 * 
	 * @param child child to add
	 */
    public void addChild(Object child);
    
    /**
     * Adds the given children to this update request.
     * 
     * @param children children to add
     */
    public void addChildren(Object[] children);
}
