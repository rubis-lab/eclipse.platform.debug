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
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.ui.contexts.IPinnablePart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * If a view is pinned to a debug context, this action clears that pinned context.
 * 
 * @since 3.9
 */
public class ClearPinnedContextAction extends Action implements IUpdate {
	
	private final IPinnablePart fView;
	
	/**
	 * Creates the pin action for the given pinnable part.
	 * @param view
	 */
	public ClearPinnedContextAction(IPinnablePart view) {
		super(ActionMessages.ClearPinAction_label, IAction.AS_RADIO_BUTTON);
		fView = view;
		setId(DebugUIPlugin.getUniqueIdentifier() + ".ClearPinnedContextAction"); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.PIN_TO_CONTEXT_ACTION);
		update();
	}
	
	public void run() {
		fView.clearPin();
	}
	
	public void update() {
		setEnabled(fView.isPinned());
	}
}