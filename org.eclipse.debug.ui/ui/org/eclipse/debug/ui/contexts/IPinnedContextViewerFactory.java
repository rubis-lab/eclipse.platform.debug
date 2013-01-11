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

/**
 * Factory class for pinned context viewers.  Pinned view factories are used 
 * to create pinned context viewers that "pin" a debugger view to a particular
 * debug context.  
 * 
 * <p>
 * Pinned context viewer factories are contributed through the 
 * <code>org.eclipse.debug.ui.pinnedContextViewerFactories</code>
 * extension point. Following is an example of a detail pane factory extension:
 * <pre>
 * &lt;extension point="org.eclipse.debug.ui.pinnedContextViewerFactories"&gt;
 *    &lt;pinnedContextViewerFactory
 *            id="com.example.ExamplePinnedContextViewerFactory"
 *            class="com.example.ExamplePinedContextViewerFactory"&gt;
 *    &lt;/pinnedContextViewerFactory&gt;
 * &lt;/extension&gt;
 * </pre>
 * </p>
 * 
 * @see IPinnableDebugContextProvider
 * @since 3.9
 */
public interface IPinnedContextViewerFactory {
	
	/**
	 * Creates a viewer for the given pin-able workbench part.
	 * 
	 * @param part Part that will be pinned using the returned viewer.
	 * @return Pinned context viewer for the given part. 
	 */
	public IPinnedContextViewer createPinnedContextViewer(IPinnablePart part);
}
