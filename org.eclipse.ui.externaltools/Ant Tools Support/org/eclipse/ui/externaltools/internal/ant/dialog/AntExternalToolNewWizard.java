package org.eclipse.ui.externaltools.internal.ant.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.externaltools.dialog.ExternalToolGroupWizardPage;
import org.eclipse.ui.externaltools.dialog.ExternalToolNewWizard;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * Wizard that will create a new external tool of type Ant build.
 */
public class AntExternalToolNewWizard extends ExternalToolNewWizard {
	private AntTargetsGroup antTargetsGroup;

	/**
	 * Creates the wizard for a new external tool
	 */
	public AntExternalToolNewWizard() {
		super(IExternalToolConstants.TOOL_TYPE_ANT_BUILD);
	}

	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void addPages() {
		addMainPage();
		addAntTargetsPage();
		addOptionPage();
		addRefreshPage();
		
		optionGroup.setPromptForArgumentLabel(ToolMessages.getString("AntExternalToolNewWizard.promptForArgumentLabel")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * Method declared on ExternalToolNewWizard.
	 */
	protected ImageDescriptor getDefaultImageDescriptor() {
		return ExternalToolsPlugin.getDefault().getImageDescriptor("icons/full/wizban/ant_wiz.gif"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * Method declared on ExternalToolNewWizard.
	 */
	protected boolean updateTool(ExternalTool tool) {
		if (super.updateTool(tool))
			return updateToolFromGroup(tool, antTargetsGroup);
		return false;
	}

	/**
	 * Creates a wizard page to contain the ant build tool
	 * targets component and adds it to the wizard page
	 * list.
	 */
	private void addAntTargetsPage() {
		createAntTargetsGroup();
		if (antTargetsGroup == null)
			return;
		ExternalToolGroupWizardPage page;
		page = new AntTargetsGroupWizardPage("antGroupPage", antTargetsGroup, mainGroup, IHelpContextIds.ANT_TARGETS_WIZARD_PAGE); //$NON-NLS-1$
		page.setTitle(ToolMessages.getString("AntExternalToolNewWizard.antTargetsPageTitle")); //$NON-NLS-1$
		page.setDescription(ToolMessages.getString("AntExternalToolNewWizard.antTargetsPageDescription")); //$NON-NLS-1$
		addPage(page);
	}

	/**
	 * Creates and initializes the group for selecting
	 * which Ant targets to run.
	 */
	private void createAntTargetsGroup() {
		if (antTargetsGroup != null)
			return;
		antTargetsGroup = new AntTargetsGroup();
	}
}
