/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.actions;


import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.contextlaunching.ContextMessages;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchHistory;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.ibm.icu.text.MessageFormat;

/**
 * Opens the launch configuration dialog in the context of a launch group.
 * <p>
 * Clients are not intended to subclass this class; clients may instantiate this
 * class.
 * </p>
 * @since 2.1
 */
public class OpenLaunchDialogAction extends Action implements IActionDelegate2, IWorkbenchWindowActionDelegate {

	/**
	 * Launch group identifier
	 */
	private String fIdentifier;
	
	/**
	 * The underlying <code>IAction</code> for this delegate
	 */
	private IAction fBackingAction = null;
	
	/**
	 * Constructs an action that opens the launch configuration dialog in
	 * the context of the specified launch group.
	 * 
	 * @param identifier unique identifier of a launch group extension
	 */
	public OpenLaunchDialogAction(String identifier) {
		fIdentifier = identifier;
		init(this);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.OPEN_LAUNCH_CONFIGURATION_ACTION);
	}
	
	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		LaunchHistory history = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchHistory(fIdentifier);
		ILaunchConfiguration configuration = history.getRecentLaunch();
		IStructuredSelection selection = null;
		if (configuration == null) {
			selection = new StructuredSelection();
		} else {
			selection = new StructuredSelection(configuration);
		}
		int result = DebugUITools.openLaunchConfigurationDialogOnGroup(DebugUIPlugin.getShell(), selection, fIdentifier);
		notifyResult(result == Window.OK);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		run();
	}
	
	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
		fBackingAction = action;
		if(fBackingAction != null) {
			LaunchGroupExtension extension = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(fIdentifier);
			fBackingAction.setText(MessageFormat.format(ContextMessages.OpenLaunchDialogAction_0, new String[] {extension.getLabel()}));
			fBackingAction.setImageDescriptor(extension.getImageDescriptor());
			fBackingAction.setEnabled(existsConfigTypesForMode());
		}
	}
	
	/**
	 * Return whether there are any registered launch configuration types for
	 * the mode of this action.
	 * 
	 * @return whether there are any registered launch configuration types for
	 * the mode of this action
	 */
	private boolean existsConfigTypesForMode() {
		ILaunchConfigurationType[] configTypes = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationTypes();
		for (int i = 0; i < configTypes.length; i++) {
			ILaunchConfigurationType configType = configTypes[i];
			if (configType.supportsMode(getMode())) {
				return true;
			}
		}		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {}
	
	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {}
	
	/**
	 * Returns the launch mode for this action.
	 * 
	 * @return launch mode
	 */
	private String getMode() {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(fIdentifier).getMode();
	}
}