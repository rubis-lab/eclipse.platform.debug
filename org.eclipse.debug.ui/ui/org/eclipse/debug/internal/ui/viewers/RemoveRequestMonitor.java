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
package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.swt.widgets.Widget;

/**
 * Request to remove an item from a tree.
 * 
 * @since 3.2
 */
class RemoveRequestMonitor extends AsynchronousRequestMonitor {

	/**
	 * Removes the given widget from the given tree.
	 * 
	 * @param widget
	 * @param viewer
	 */
	RemoveRequestMonitor(Widget widget, AsynchronousTreeViewer viewer) {
		super(widget, viewer);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousRequestMonitor#performUpdate()
	 */
	protected void performUpdate() {
		Widget widget = getWidget();
		Object element = widget.getData(); 
		getViewer().unmap(element, widget);
		widget.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AsynchronousRequestMonitor#contains(org.eclipse.debug.internal.ui.viewers.AsynchronousRequestMonitor)
	 */
	protected boolean contains(AsynchronousRequestMonitor update) {
		return update instanceof AddRequestMonitor && update.contains(getWidget());
	}

}
