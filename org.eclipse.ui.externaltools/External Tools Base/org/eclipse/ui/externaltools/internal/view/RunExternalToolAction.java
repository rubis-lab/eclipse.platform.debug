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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.externaltools.internal.core.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.core.ToolMessages;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.internal.ui.IHelpContextIds;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action to run the currently selected external tool
 * in the view.
 */
public class RunExternalToolAction extends Action {
	private IWorkbenchPage page;
	private ExternalTool selectedTool;
	
	/**
	 * Create an action to run the selected external
	 * tool in the view.
	 */
	public RunExternalToolAction(IWorkbenchPage page) {
		super();
		this.page = page;
		setText(ToolMessages.getString("RunExternalToolAction.text")); //$NON-NLS-1$
		setToolTipText(ToolMessages.getString("RunExternalToolAction.toolTip")); //$NON-NLS-1$
		setHoverImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/clcl16/run_tool.gif")); //$NON-NLS-1$
		setImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/elcl16/run_tool.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/dlcl16/run_tool.gif")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.RUN_TOOL_ACTION);
	}

	/**
	 * Returns the selected external tool.
	 */
	public ExternalTool getSelectedTool() {
		return selectedTool;
	}
	
	/* (non-Javadoc)
	 * Method declared on Action.
	 */
	public void run() {
		org.eclipse.jface.dialogs.MessageDialog.openInformation(page.getWorkbenchWindow().getShell(), "Action", "This action is not yet implemented");
	}

	/**
	 * Sets the selected external tool.
	 */
	public void setSelectedTool(ExternalTool tool) {
		selectedTool = tool;
		setEnabled(tool != null);
	}
}
