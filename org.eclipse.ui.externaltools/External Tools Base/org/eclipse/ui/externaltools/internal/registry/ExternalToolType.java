package org.eclipse.ui.externaltools.internal.registry;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.externaltools.internal.core.ExternalToolsPlugin;

/**
 * This class represents the definition of an external
 * tool type.
 */
public final class ExternalToolType {
	private String id;
	private String name;
	private IConfigurationElement element;
	private ImageDescriptor imageDescriptor;
	
	/**
	 * Create a new external tool type.
	 * 
	 * @param id the unique identifier of this type
	 * @param name a user readable label for this type
	 * @param element the configuration element for the extension
	 */
	/*package*/ ExternalToolType(String id, String name, IConfigurationElement element) {
		super();
		this.id = id;
		this.name = name;
		this.element = element;
	}

	/**
	 * Returns a short description of this type.
	 */
	public String getDescription() {
		String description = element.getAttribute(ExternalToolTypeRegistry.TAG_DESCRIPTION);
		if (description == null)
			description = ""; //$NON-NLS-1$
		return description;
	}
	
	/**
	 * Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the image descriptor for this tool type icon.
	 */
	public ImageDescriptor getImageDescriptor() {
		if (imageDescriptor == null) {
			IExtension extension = element.getDeclaringExtension();
			IPluginDescriptor pluginDescriptor = extension.getDeclaringPluginDescriptor();
			String location = element.getAttribute(ExternalToolTypeRegistry.TAG_ICON);
			if (location != null && location.length() > 0) {
				URL fullPath = pluginDescriptor.find(new Path(location));
				if (fullPath != null) {
					imageDescriptor = ImageDescriptor.createFromURL(fullPath);
				} else {
					try {
						URL installURL = pluginDescriptor.getInstallURL();
						fullPath = new URL(installURL, location);
						imageDescriptor = ImageDescriptor.createFromURL(fullPath);
					} catch (MalformedURLException e) {
					}
				}
			}
			if (imageDescriptor == null)
				imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
		}
		return imageDescriptor;
	}
}
