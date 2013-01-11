/*******************************************************************************
 * Copyright (c) 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.contexts;

import org.eclipse.ui.IWorkbenchPart;

/**
 * Workbench part that supports pinning the active debug context for the part.
 * 
 * @since 3.9
 * @see IPinnableDebugContextProvider
 */
public interface IPinnablePart extends IWorkbenchPart {
	/**
	 * Pins this part to the given debug context provider.  
	 * @param provider Debug context provider to pin to.
	 */
    public void pinToProvider(IPinnableDebugContextProvider provider);
    
    /**
     * Clears the pinned context from the part.  This should cause the part
     * to start tracking the active window debug context. 
     */
    public void clearPinnedProvider();
    
    /**
     * Returns whether the view is currently pinned.
     * 
     * @return Returns true if pinned.
     */
    public boolean isPinned();
    
    /**
     * Copies the view's settings from the given view to the 
     * that is to be created.  
     * 
     * @param secondaryId Seconary ID of the view to be created.
     */
    public void copyViewSettings(String secondaryId);
}
