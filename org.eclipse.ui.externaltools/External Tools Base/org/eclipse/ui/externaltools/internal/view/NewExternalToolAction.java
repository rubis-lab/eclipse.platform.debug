package org.eclipse.ui.externaltools.internal.view;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action that will launch a wizard to create a new
 * external tool.
 */
public class NewExternalToolAction extends Action {
	private ExternalToolView view;

	/**
	 * Create an action to launch a new
	 * external tool wizard.
	 */
	public NewExternalToolAction(ExternalToolView view) {
		super();
		this.view = view;
		setText(ToolMessages.getString("NewExternalToolAction.text")); //$NON-NLS-1$
		setToolTipText(ToolMessages.getString("NewExternalToolAction.toolTip")); //$NON-NLS-1$
		setHoverImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/clcl16/new_tool.gif")); //$NON-NLS-1$
		setImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/elcl16/new_tool.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/dlcl16/new_tool.gif")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.NEW_TOOL_ACTION);
	}

	/* (non-Javadoc)
	 * Method declared on Action.
	 */
	public void run() {
		org.eclipse.jface.dialogs.MessageDialog.openInformation(view.getSite().getShell(), "Action", "This action is not yet implemented");
	}
}
