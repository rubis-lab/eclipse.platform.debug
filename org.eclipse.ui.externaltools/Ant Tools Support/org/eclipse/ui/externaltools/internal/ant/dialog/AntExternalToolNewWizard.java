package org.eclipse.ui.externaltools.internal.ant.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard that will create a new external tool of type Ant build.
 */
public class AntExternalToolNewWizard extends Wizard implements INewWizard {

	/**
	 * Creates the wizard for a new external tool
	 */
	public AntExternalToolNewWizard() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWizard.
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		return false;
	}
}
