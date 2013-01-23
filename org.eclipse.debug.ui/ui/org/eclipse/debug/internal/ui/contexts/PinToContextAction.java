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

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.IDebugContextManager;
import org.eclipse.debug.ui.contexts.IPinnablePart;
import org.eclipse.debug.ui.contexts.IPinnedContextFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;

/**
 * Pins a workbench part using the given pinned context factory.
 * 
 * @since 3.9
 */
public class PinToContextAction extends Action {
	
	private final IPinnablePart fView;
	private final IPinnedContextFactory fFactory;
	
	/**
	 * Creates the pin action for the given pinnable part.
	 * @param view
	 */
	public PinToContextAction(IPinnablePart view, IPinnedContextFactory factory) {
		super(null, IAction.AS_RADIO_BUTTON);
		fView = view;
		fFactory = factory;
		IDebugContextManager manager = DebugUITools.getDebugContextManager();
		setText(manager.getPinnedContextViewerFactoryName(fFactory.getId()));
		setImageDescriptor(manager.getPinnedContextViewerFactoryImage(fFactory.getId()));
		setToolTipText(manager.getPinnedContextViewerFactoryDescription(fFactory.getId()));
		setId(DebugUIPlugin.getUniqueIdentifier() + ".PinToContextAction"); //$NON-NLS-1$
		setChecked( fFactory.getId().equals(fView.getPinnedFactoryId()) );
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.PIN_TO_CONTEXT_ACTION);
	}
	
	public void run() {
		fView.pin(fFactory);
	}
}