package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.ant.core.TargetInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.NewWizardAction;
import org.eclipse.ui.externaltools.internal.ant.dialog.AntExternalToolNewWizard;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.model.DefaultRunnerContext;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.ExternalToolStorage;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.model.IStorageListener;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action to run an ant build file.
 */
public class AntAction extends Action {
	private IFile file;
	private IWorkbenchWindow window;

	/**
	 * Creates an initialize action to run an
	 * Ant build file
	 * 
	 * @param file the ant build file to run
	 */
	public AntAction(IFile file, IWorkbenchWindow window) {
		super();
		this.file = file;
		this.window = window;
		setText(file.getName());
		setToolTipText(file.getFullPath().toOSString());
		WorkbenchHelp.setHelp(this, IHelpContextIds.ANT_ACTION);
	}

	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		if (file == null) {
			return;
		}
		
		new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), new AntExternalToolNewWizard(file)).open();
		ExternalToolStorage.addStorageListener(new IStorageListener() {
			public void toolDeleted(ExternalTool tool) {
			}

			public void toolCreated(ExternalTool tool) {
				if (tool.getLocation().equals(file.getLocation().toString())) {
					MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, "", null);
					new DefaultRunnerContext(tool, file).run(new NullProgressMonitor(), status);
					Shell shell= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					if (!status.isOK()) {
						StringBuffer message= new StringBuffer("An exception occurred while running ant: ");
						IStatus[] errors= status.getChildren();
						IStatus error;
						for (int i= 0, numErrors= errors.length; i < numErrors; i++) {
							error= errors[i];
							if (error.getSeverity() == IStatus.ERROR) {
								Throwable exception= error.getException();
								message.append('\n');
								if (exception != null) {
									message.append(exception.getClass().getName()).append(' ');
								}
								message.append(error.getMessage());
							}
						}
						MessageDialog.openError(shell, "Ant Error", message.toString());
					}
					ExternalToolStorage.deleteTool(tool, shell);
				}
				ExternalToolStorage.removeStorageListener(this);
			}

			public void toolModified(ExternalTool tool) {
			}

			public void toolsRefreshed() {
			}
		});
		
//		TargetInfo[] targetList = null;
//		try {
//			targetList = AntUtil.getTargets(file.getFullPath().makeAbsolute().toString()); // getTargetList(file.getLocation());
//		} catch (CoreException e) {
//			ErrorDialog.openError(
//				window.getShell(),
//				ToolMessages.getString("AntAction.runErrorTitle"), //$NON-NLS-1$
//				ToolMessages.format("AntAction.errorReadAntFile", new Object[] {file.getFullPath().toString()}), //$NON-NLS-1$;
//				e.getStatus());
//			return;
//		}
//		
//		if (targetList == null) {
//			MessageDialog.openError(
//				window.getShell(),
//				ToolMessages.getString("AntAction.runErrorTitle"), //$NON-NLS-1$;
//				ToolMessages.format("AntAction.noAntTargets", new Object[] {file.getFullPath().toString()})); //$NON-NLS-1$;
//			return;
//		}
//
//		AntLaunchWizard wizard = new AntLaunchWizard(targetList, file, window);
//		wizard.setNeedsProgressMonitor(true);
//		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
//		dialog.create();
//		WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.ANT_LAUNCH_WIZARD);		
//		dialog.open();
	}
}