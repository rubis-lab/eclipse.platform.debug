package org.eclipse.ui.externaltools.internal.menu;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.externaltools.internal.core.*;
import org.eclipse.ui.externaltools.internal.view.ExternalToolView;
import org.eclipse.ui.externaltools.model.*;

/**
 * This action delegate is responsible for producing the
 * Run > External Tools sub menu contents, which includes
 * an items to run last tool, favorite tools, and show the
 * external tools view. Default action is to run the last tool
 * if one exist.
 */
public class ExternalToolMenuDelegate extends ActionDelegate implements IWorkbenchWindowPulldownDelegate2, IMenuCreator {
	private IWorkbenchWindow window;
	private IAction realAction;
	private ExternalTool lastTool;
	
	/**
	 * Creates the action delegate
	 */
	public ExternalToolMenuDelegate() {
		super();
	}
	
	/* (non-Javadoc)
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		if (action.isEnabled())
			runLastTool();
	}

	/* (non-Javadoc)
	 * Method declared on IActionDelegate.
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (realAction == null) {
			realAction = action;
			realAction.setMenuCreator(this);
		}
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWindowPulldownDelegate.
	 */
	public Menu getMenu(Control parent) {
		Menu menu= new Menu(parent);
		return createMenu(menu, false);
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWindowPulldownDelegate2.
	 */
	public Menu getMenu(Menu parent) {
		Menu menu= new Menu(parent);
		return createMenu(menu, true);
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWindowActionDelegate.
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWindowActionDelegate.
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	
	/**
	 * Creates the menu for the action
	 */
	private Menu createMenu(Menu menu, final boolean wantFastAccess) {
		// Add listener to repopulate the menu each time
		// it is shown because of dynamic history list
		menu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				Menu m = (Menu)e.widget;
				MenuItem[] items = m.getItems();
				for (int i=0; i < items.length; i++)
					items[i].dispose();
				populateMenu(m, wantFastAccess);
			}
		});
		
		return menu;
	}

	/**
	 * Populates the menu with its items
	 */
	private void populateMenu(Menu menu, boolean wantFastAccess) {
		// Add a menu item to run the most recent tool.
		MenuItem runRecent = new MenuItem(menu, SWT.NONE);
		runRecent.setText(ToolMessages.getString("ExternalToolMenuDelegate.runRecent")); //$NON-NLS-1$
		runRecent.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				runLastTool();
			}
		});
		// Disable option if no tool has been run yet.
		runRecent.setEnabled(lastTool != null);
		
		// Add a separator.
		new MenuItem(menu, SWT.SEPARATOR);
				
		// Add a menu item for each tool in the favorites list.
		ExternalTool[] tools = FavoritesManager.getInstance().getFavorites();
		if (tools.length > 0) {
			for (int i = 0; i < tools.length; i++) {
				ExternalTool tool = tools[i];
				StringBuffer label = new StringBuffer();
				if (i < 9 && wantFastAccess) {
					//add the numerical accelerator
					label.append('&');
					label.append(i+1);
					label.append(' ');
				}
				label.append(tool.getName());
				MenuItem item = new MenuItem(menu, SWT.NONE);
				item.setText(label.toString());
				item.setData(tool);
				item.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						runTool((ExternalTool)e.widget.getData());
					}
				});
			}
			
			// Add a separator.
			new MenuItem(menu, SWT.SEPARATOR);
		}

		// Add a menu item to show the external tools view.
		MenuItem showView = new MenuItem(menu, SWT.NONE);
		showView.setText(ToolMessages.getString("ExternalToolMenuDelegate.showView")); //$NON-NLS-1$
		showView.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showView();
			}
		});
	}

	/**
	 * Runs the specified tool
	 */
	private void runTool(final ExternalTool tool) {
		if (tool == null)
			return;
			
		ToolUtil.saveDirtyEditors(window);
			
		// Selection is assigned BEFORE Log Console is given focus.
		// Otherwise incorrect selection is used. Selection is assigned outside
		// runnable.run(IProgressMonitor) to avoid invalid thread access.
		final ISelection sel = window.getSelectionService().getSelection();
		final IWorkbenchPart activePart = window.getPartService().getActivePart();
									
		if (tool.getLogMessages()) {
//			ToolUtil.showLogConsole(window);
//			ToolUtil.clearLogDocument();
		}
		
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				IResource resource = null;

				// Get the focused resource.
				if (sel instanceof IStructuredSelection) {
					Object result = ((IStructuredSelection)sel).getFirstElement();
					if (result instanceof IResource) {
						resource = (IResource) result;
					} else if (result instanceof IAdaptable) {
						resource = (IResource)((IAdaptable) result).getAdapter(IResource.class);
					}
				}
				
				if (resource == null) {
					// If the active part is an editor, get the file resource used as input.
					if (activePart instanceof IEditorPart) {
						IEditorPart editorPart = (IEditorPart) activePart;
						IEditorInput input = editorPart.getEditorInput();
						resource = (IResource) input.getAdapter(IResource.class);
					} 
				}

				DefaultRunnerContext context;
				if (resource != null)
					context = new DefaultRunnerContext(tool, resource.getProject(), resource, window.getWorkbench().getWorkingSetManager());
				else
					context = new DefaultRunnerContext(tool, null, window.getWorkbench().getWorkingSetManager());
				context.run(monitor, window.getShell());
			};
		};
		
		// Keep track of the most recently run tool.
		lastTool = tool;
		
		try {
			new ProgressMonitorDialog(window.getShell()).run(true, true, runnable);		
		} catch (InterruptedException e) {
			return;
		} catch (InvocationTargetException e) {
			IStatus status = null;
			if (e.getTargetException() instanceof CoreException)
				status = ((CoreException)e.getTargetException()).getStatus();
			else
				status = new Status(IStatus.ERROR, IExternalToolConstants.PLUGIN_ID, 0, ToolMessages.getString("ExternalToolsAction.internalError"), e.getTargetException()); //$NON-NLS-1$;
			ErrorDialog.openError(
				window.getShell(), 
				ToolMessages.getString("ExternalToolMenuDelegate.runErrorTitle"), //$NON-NLS-1$;
				ToolMessages.getString("ExternalToolMenuDelegate.runProblem"), //$NON-NLS-1$;
				status);
			return;
		}
	}
	
	/**
	 * Shows the external tool view.
	 */
	private void showView() {
		try {
			IWorkbenchPage page = window.getActivePage();
			if (page != null)
				page.showView(IExternalToolConstants.VIEW_ID);
		} catch (PartInitException e) {
			ExternalToolsPlugin.getDefault().log("Unable to display the External Tools view.", e); //$NON-NLS-1$
		}
	}
	
	/**
	 * Run the most recently run external tool.
	 */
	private void runLastTool() {
		if (lastTool == null)
			return;
		runTool(lastTool);	
	}
	
	
}
