package org.eclipse.ui.externaltools.action;

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
 * Action to run an external tool.
 */
public class RunExternalToolAction extends Action {
	private IWorkbenchPage page;
	private ExternalTool tool;
	
	/**
	 * Create an action to run an external tool
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
	 * Returns the workbench page this action
	 * in to be run in.
	 */
	protected final IWorkbenchPage getPage() {
		return page;
	}
	
	/**
	 * Returns the external tool.
	 */
	public final ExternalTool getTool() {
		return tool;
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
	public final void setTool(ExternalTool tool) {
		this.tool = tool;
		setEnabled(tool != null);
	}
}
