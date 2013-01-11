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
package org.eclipse.debug.internal.ui.contexts;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.contexts.IPinnablePart;
import org.eclipse.debug.ui.contexts.IPinnedContextViewer;
import org.eclipse.debug.ui.contexts.IPinnedContextViewerFactory;

/**
 * A contributed pinned context viewer factory.
 * 
 * @since 3.9
 */
public class PinnedContextViewerFactory implements IPinnedContextViewerFactory {
	
	private IConfigurationElement fElement;
	private IPinnedContextViewerFactory fDelegate;
	
	// attributes
	public static final String ATTR_ID = "id"; //$NON-NLS-1$
	public static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	
	public PinnedContextViewerFactory(IConfigurationElement element) {
		fElement = element;
	}

	public String getId() {
		return fElement.getAttribute(ATTR_ID);
	}
	
	public IPinnedContextViewer createPinnedContextViewer(IPinnablePart part) {
		return getDelegate().createPinnedContextViewer(part);
	}
	
	/**
	 * Returns this factories's delegate, instantiating it if required.
	 * 
	 * @return this organizer's delegate
	 */
	protected IPinnedContextViewerFactory getDelegate() {
		if (fDelegate == null) {
			try {
				fDelegate = (IPinnedContextViewerFactory) fElement.createExecutableExtension(ATTR_CLASS);
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		return fDelegate;
	}

}
