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
package org.eclipse.debug.internal.ui.model.elements;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IMemento;

/**
 * Memento provider for the debug target element.
 * 
 * @since 3.9
 */
public class DebugTargetMementoProvider extends
		MemoryViewElementMementoProvider 
{
	protected boolean encodeElement(Object element, IMemento memento,
			IPresentationContext context) throws CoreException 
	{
		String id = context.getId();
		if (id.equals(IDebugUIConstants.ID_MEMORY_VIEW)) {
			return super.encodeElement(element, memento, context);
		} else if ( id.equals(IDebugUIConstants.ID_DEBUG_VIEW) ) {
			String name = getElementName(element);
			memento.putString(DebugElementMementoProvider.ELEMENT_NAME, name);
			return true;
		}
		return false;
	}

    /**
     * Returns the name of the given element to use in a memento in the given context,
     * or <code>null</code> if unsupported.
     * 
     * @param element model element
     * @return element name or <code>null</code> if none
     * @throws CoreException
     */
	protected String getElementName(Object element) throws CoreException {
		if (element instanceof IDebugTarget) {
			return ((IDebugTarget) element).getName();
		}
		return null;
	}
	
	protected boolean isEqual(Object element, IMemento memento,
			IPresentationContext context) throws CoreException {
		String id = context.getId();
		if (id.equals(IDebugUIConstants.ID_MEMORY_VIEW)) {
			return super.isEqual(element, memento, context);
		} else if ( id.equals(IDebugUIConstants.ID_DEBUG_VIEW) ) {
			String mementoName = memento.getString(DebugElementMementoProvider.ELEMENT_NAME);
			if (mementoName != null) {
				String name = getElementName(element);
				if (name != null) {
					return name.equals(mementoName);
				}
			}
			return false;
		}
		return false;
	}
}
