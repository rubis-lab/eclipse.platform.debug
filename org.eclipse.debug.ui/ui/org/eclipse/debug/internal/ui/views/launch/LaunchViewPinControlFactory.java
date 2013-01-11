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
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.contexts.IPinnablePart;
import org.eclipse.debug.ui.contexts.IPinnedContextViewer;
import org.eclipse.debug.ui.contexts.IPinnedContextViewerFactory;

/**
 * Factory for creating gebug View's pinned context (breadcrumb) viewer. 
 * 
 * @since 3.9
 */
public class LaunchViewPinControlFactory implements IPinnedContextViewerFactory {

	public String getPresentationId() {
		return IDebugUIConstants.ID_DEBUG_VIEW;
	}

	public IPinnedContextViewer createPinnedContextViewer(IPinnablePart part) {
        return new DebugViewContextPinBreadcrumb(part);
	}
}
