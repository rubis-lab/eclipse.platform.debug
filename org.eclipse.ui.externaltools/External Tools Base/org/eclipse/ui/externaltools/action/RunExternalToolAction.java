package org.eclipse.ui.externaltools.action;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.ui.externaltools.internal.core.*;
import org.eclipse.ui.externaltools.internal.menu.FavoritesManager;
import org.eclipse.ui.externaltools.internal.ui.IHelpContextIds;
import org.eclipse.ui.externaltools.model.*;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action to run an external tool.
 */
public class RunExternalToolAction extends Action {
	private IWorkbenchWindow window;
	private ExternalTool tool;
	
	/**
	 * Create an action to run an external tool
	 */
	public RunExternalToolAction(IWorkbenchWindow window) {
		super();
		this.window = window;
		setText(ToolMessages.getString("RunExternalToolAction.text")); //$NON-NLS-1$
		setToolTipText(ToolMessages.getString("RunExternalToolAction.toolTip")); //$NON-NLS-1$
		setHoverImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/clcl16/run_tool.gif")); //$NON-NLS-1$
		setImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/elcl16/run_tool.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/dlcl16/run_tool.gif")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.RUN_TOOL_ACTION);
	}

	/**
	 * Returns the last external tool to be run.
	 */
	public final static ExternalTool getLastTool() {
		return FavoritesManager.getInstance().getLastTool();	
	}

	/**
	 * Returns the workbench window this action
	 * is to be run in.
	 */
	protected final IWorkbenchWindow getWindow() {
		return window;
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
		org.eclipse.jface.dialogs.MessageDialog.openInformation(getWindow().getShell(), "Action", "This action is not yet implemented");
		
		// Keep track of the most recently run tool.
		FavoritesManager.getInstance().setLastTool(tool);
	}

	/**
	 * Sets the selected external tool.
	 */
	public final void setTool(ExternalTool tool) {
		this.tool = tool;
		setEnabled(tool != null);
	}
}
