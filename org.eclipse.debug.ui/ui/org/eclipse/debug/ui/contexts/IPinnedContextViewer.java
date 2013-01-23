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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;

/**
 * Wrapper class for the viewer of the pinned context in a view.  The viewer is 
 * created using the pinned context factory.  
 * 
 * @see IPinnedContextFactory
 * 
 * @since 3.9
 */
public interface IPinnedContextViewer {
	
	/**
	 * Returns the ID of the pinned context viewer factory.
	 * 
	 * @return Returns the factory Id.
	 */
	public String getFactoryId();
	
	/**
	 * Creates the pinned control viewer.
	 * @param parent Control parent.
	 * @return Pinned context viewer's control.
	 */
    public Control createControl(Composite parent);
    
    /**
     * Saves the viewer's state to the memento.
     * @param memento Memento to save state.
     */
    public void savePinnedContext(IMemento memento);
    
    /**
     * Restore the viewer state from given memento.
     * @param memento Memento to restore from.
     */
    public void restorePinnedContext(IMemento memento);
    
    /**
     * Dispose the viewer and its control.
     */
    public void dispose();
}
