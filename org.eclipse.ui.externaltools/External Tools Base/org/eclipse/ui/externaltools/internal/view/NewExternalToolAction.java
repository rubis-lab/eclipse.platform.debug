package org.eclipse.ui.externaltools.internal.view;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.NewWizardTypeAction;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action that will launch a wizard to create a new
 * external tool.
 */
public class NewExternalToolAction extends Action {
	private static final String CATEGORY_ID = "org.eclipse.ui.externaltools.newWizards"; //$NON-NLS-1$
	private static final String DLG_SETTING_SECTION = "NewWizardTypeAction"; //$NON-NLS-1$
	
	private IWorkbenchPage page;

	/**
	 * Create an action to launch a new
	 * external tool wizard.
	 */
	public NewExternalToolAction(IWorkbenchPage page) {
		super();
		this.page = page;
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
		IDialogSettings pluginSettings = ExternalToolsPlugin.getDefault().getDialogSettings();
		IDialogSettings wizardSettings = pluginSettings.getSection(DLG_SETTING_SECTION);
		if (wizardSettings == null)
			wizardSettings = pluginSettings.addNewSection(DLG_SETTING_SECTION);
		NewWizardTypeAction action = new NewWizardTypeAction(page.getWorkbenchWindow(), CATEGORY_ID, wizardSettings);
		action.run();
	}
}
