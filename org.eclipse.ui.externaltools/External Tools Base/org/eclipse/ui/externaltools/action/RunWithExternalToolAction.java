package org.eclipse.ui.externaltools.action;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.externaltools.internal.core.*;
import org.eclipse.ui.externaltools.internal.ui.IHelpContextIds;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action to run an external tool by prompting
 * for arguments beforehand.
 */
public class RunWithExternalToolAction extends RunExternalToolAction {
	
	/**
	 * Create an action to run an external tool.
	 */
	public RunWithExternalToolAction(IWorkbenchWindow window) {
		super(window);
		setText(ToolMessages.getString("RunWithExternalToolAction.text")); //$NON-NLS-1$
		setToolTipText(ToolMessages.getString("RunWithExternalToolAction.toolTip")); //$NON-NLS-1$
		setHoverImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/clcl16/runwith_tool.gif")); //$NON-NLS-1$
		setImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/elcl16/runwith_tool.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/dlcl16/runwith_tool.gif")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.RUN_WITH_TOOL_ACTION);
	}

	/* (non-Javadoc)
	 * Method declared on Action.
	 */
	public void run() {
		super.run(); // for now do same as RunExternalToolAction
	}
}
