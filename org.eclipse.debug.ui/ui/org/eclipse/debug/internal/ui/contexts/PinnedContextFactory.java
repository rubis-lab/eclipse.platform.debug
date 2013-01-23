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

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.contexts.IPinnablePart;
import org.eclipse.debug.ui.contexts.IPinnedContextViewer;
import org.eclipse.debug.ui.contexts.IPinnedContextFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.Bundle;

/**
 * A contributed pinned context viewer factory.
 * 
 * @since 3.9
 */
public class PinnedContextFactory implements IPinnedContextFactory {
	
	private IConfigurationElement fElement;
	private IPinnedContextFactory fDelegate;
	private ImageDescriptor fImage;
	
	// attributes
	public static final String ATTR_ID = "id"; //$NON-NLS-1$
	public static final String ATTR_LABEL = "label"; //$NON-NLS-1$
	public static final String ATTR_DESCRIPTION = "description"; //$NON-NLS-1$
	public static final String ATTR_IMAGE = "image"; //$NON-NLS-1$
	public static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	
	public PinnedContextFactory(IConfigurationElement element) {
		fElement = element;
	}

	private Bundle getBundle() {
		String namespace= fElement.getDeclaringExtension().getContributor().getName();
		Bundle bundle= Platform.getBundle(namespace);
		return bundle;
	}
	
	public String getId() {
		return fElement.getAttribute(ATTR_ID);
	}
	
	public IPinnedContextViewer createPinnedContextViewer(IPinnablePart part) {
		return getDelegate().createPinnedContextViewer(part);
	}
	
	public String getName() {
		return fElement.getAttribute(ATTR_LABEL);
	}
	
	public String getDescription() {
		return fElement.getAttribute(ATTR_DESCRIPTION);
	}

	public ImageDescriptor getImage() {
		if (fImage == null) { 
			String attr = fElement.getAttribute(ATTR_IMAGE);
			if (attr != null) {
				Bundle bundle= getBundle();
				if (bundle != null) {
					Path path= new Path(attr);
					URL url= FileLocator.find(bundle, path, null);
					fImage = ImageDescriptor.createFromURL(url);
				}
			}
		}
		return fImage;
	}
	
	
	/**
	 * Returns this factories's delegate, instantiating it if required.
	 * 
	 * @return this organizer's delegate
	 */
	protected IPinnedContextFactory getDelegate() {
		if (fDelegate == null) {
			try {
				fDelegate = (IPinnedContextFactory) fElement.createExecutableExtension(ATTR_CLASS);
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		return fDelegate;
	}

}
