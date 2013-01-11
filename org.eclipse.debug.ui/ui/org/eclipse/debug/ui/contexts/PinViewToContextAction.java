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

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Pins a workbench part to the active debug context.
 * 
 * @since 3.9
 */
class PinViewToContextAction extends Action implements IUpdate {
	
	private IPinnablePart fView;
	
	/**
	 * Creates the pin action for the given pinnable part.
	 * @param view
	 */
	public PinViewToContextAction(IPinnablePart view) {
		super(null, IAction.AS_CHECK_BOX);
		fView = view;
		setToolTipText(ActionMessages.PinViewToContextAction_label);   
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_PIN));
		setId(DebugUIPlugin.getUniqueIdentifier() + ".PinViewToContextAction"); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.PIN_VIEW_TO_CONTEXT_ACTION);
	}
	
	public void run() {
	    if (isChecked()) {
	        IPinnableDebugContextProvider provider = getContextProvider();
	        if (provider != null) {
	            fView.pinToProvider(provider);
	        }
	    } else {
	        fView.clearPinnedProvider();
	    }
	}
	
	private IPinnableDebugContextProvider getContextProvider() {
	    IDebugContextProvider provider = getContextService().getActiveProvider();
	    if (provider instanceof IPinnableDebugContextProvider) {
	        return (IPinnableDebugContextProvider)provider;
	    }
	    return null;
	}
	
	private IDebugContextService getContextService() {
	    return DebugUITools.getDebugContextManager().getContextService(fView.getSite().getWorkbenchWindow());
	}
	
	public void update() {
		setEnabled(getContextProvider() != null);
		setChecked(fView.isPinned());
	}
}