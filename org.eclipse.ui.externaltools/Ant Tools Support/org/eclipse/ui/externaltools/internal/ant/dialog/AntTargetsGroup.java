package org.eclipse.ui.externaltools.internal.ant.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.util.*;

import org.eclipse.ant.core.TargetInfo;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.externaltools.group.*;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.*;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

import org.eclipse.swt.widgets.List;

/**
 * Group for selecting the targets of an ant build tool.
 */
public class AntTargetsGroup extends ExternalToolGroup {
	private static final int DESCRIPTION_FIELD_HEIGHT = 3;

	private ExternalToolMainGroup mainGroup;
	private String oldLocation;
	
	private Map mapTargetNamesToTargetInfos = new HashMap();

	private List availableTargets;
	private List activeTargets;
	private Button add;
	private Button remove;
	private Button addAll;
	private Button removeAll;
	private Button up;
	private Button down;
	private Text description;

	public AntTargetsGroup(ExternalToolMainGroup mainGroup) {
		this.mainGroup = mainGroup;
		if (mainGroup != null)
			oldLocation = mainGroup.getLocationFieldValue();
	}

	/**
	 * @see org.eclipse.ui.externaltools.group.ExternalToolGroup#createGroupContents(Composite, ExternalTool)
	 */
	protected Control createGroupContents(Composite parent, ExternalTool tool) {
		// main composite
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		mainComposite.setLayout(layout);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayoutData(gridData);
										
		Composite upperComposite = new Composite(mainComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 4;
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		upperComposite.setLayout(layout);
		upperComposite.setLayoutData(gridData);
		
		createAvailableTargetsList(upperComposite);
		createAddRemoveComposite(upperComposite);
		createActiveTargetsList(upperComposite);
		createUpDownComposite(upperComposite);
		
		Composite lowerComposite = new Composite(mainComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		lowerComposite.setLayout(layout);
		lowerComposite.setLayoutData(gridData);		
		
		createDescriptionField(lowerComposite);
		if (tool != null)
			restoreValues(tool);
		
		deselectAll();
		
		return mainComposite;
	}
	
	private void createSpacer(Composite parent) {
		Label spacer = new Label(parent, SWT.NONE);	
	}
	
	/*
	 * Creates the list of targets provided by the ant build tool.
	 */
	private void createAvailableTargetsList(Composite parent) {
		availableTargets = createList(parent, ToolMessages.getString("AntTargetsGroup.availableTargetsLabel")); //$NON-NLS-1$
	}
	
	/*
	 * Creates the list of targets that will be used when the tool is run.
	 */
	private void createActiveTargetsList(Composite parent) {
		activeTargets = createList(parent, ToolMessages.getString("AntTargetsGroup.activeTargetsLabel")); //$NON-NLS-1$
	}
	
	/*
	 * Creates the bank of buttons that allow the user to
	 * add and remove targets from the active list.
	 */
	private void createAddRemoveComposite(Composite parent) {
		Composite addRemoveComposite = new Composite(parent, SWT.NONE);

		GridData gridData = new GridData();
		addRemoveComposite.setLayoutData(gridData);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		addRemoveComposite.setLayout(layout);

		createSpacer(addRemoveComposite);
		
		add = createButton(
			addRemoveComposite, 
			ToolMessages.getString("AntTargetsGroup.addLabel"), //$NON-NLS-1$	
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					addTarget();
				}
			},
			false);
		
		remove = createButton(
			addRemoveComposite, 
			ToolMessages.getString("AntTargetsGroup.removeLabel"), //$NON-NLS-1$	
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					removeTarget();
				}
			},
			false);
		
		createSpacer(addRemoveComposite);
		
		addAll = createButton(
			addRemoveComposite, 
			ToolMessages.getString("AntTargetsGroup.addAllLabel"), //$NON-NLS-1$	
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					addAllTargets();
				}
			},
			false);
		
		removeAll = createButton(
			addRemoveComposite, 
			ToolMessages.getString("AntTargetsGroup.removeAllLabel"), //$NON-NLS-1$	
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					removeAllTargets();
				}
			},
			false);
	}
	
	/*
	 * Creates a button bank containing the buttons for moving
	 * targets in the active list up and down.
	 */
	private void createUpDownComposite(Composite parent) {
		Composite upDownComposite = new Composite(parent, SWT.NONE);

		GridData gridData = new GridData();
		upDownComposite.setLayoutData(gridData);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		upDownComposite.setLayout(layout);

		Label spacer = new Label(upDownComposite, SWT.NONE);
				
		up = createButton(
			upDownComposite, 
			ToolMessages.getString("AntTargetsGroup.upLabel"), //$NON-NLS-1$	
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					moveTargetUp();
				}
			},
			true);
				
		down = createButton(
			upDownComposite, 
			ToolMessages.getString("AntTargetsGroup.downLabel"), //$NON-NLS-1$	
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					moveTargetDown();
				}
			},
			true);
	}
	
	/*
	 * Creates a button in the given composite with the given label and selection adapter.
	 * minWidth specifies whether the button should be at minimum IDialogConstants.BUTTON_WIDTH
	 * wide.
	 */
	 private Button createButton(Composite parent, String label, SelectionAdapter adapter, boolean minWidth) {
		Button button = new Button(parent, SWT.PUSH);		
		button.setText(label);
		GridData data = getPage().setButtonGridData(button);
		if (!minWidth)
			data.widthHint = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
		button.addSelectionListener(adapter);
		
		return button;
	 }
	
	/*
	 * Creates the text field which displays the description of the selected target.
	 */
	private void createDescriptionField(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(ToolMessages.getString("AntTargetsGroup.descriptionLabel")); //$NON-NLS-1$
		
		description = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = getPage().convertHeightHint(DESCRIPTION_FIELD_HEIGHT);
		description.setLayoutData(data);
	}
	
	/*
	 * Creates a list in the given composite with the given label.
	 */
	private List createList(Composite parent, String label) {
		Composite listComposite = new Composite(parent, SWT.NONE);
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		listComposite.setLayoutData(gridData);
		
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		listComposite.setLayout(layout);
				
		Label listLabel = new Label(listComposite, SWT.LEFT);
		listLabel.setText(label);
		
		final List list = new List(listComposite, SWT.BORDER | SWT.SINGLE);
		gridData = new GridData(GridData.FILL_BOTH);
		list.setLayoutData(gridData);
		
		list.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				targetSelected(list.getItem(list.getSelectionIndex()), list);
			}
		});
		
		return list;
	}

	/**
	 * @see org.eclipse.ui.externaltools.group.IExternalToolGroup#restoreValues(ExternalTool)
	 */
	public void restoreValues(ExternalTool tool) {
		activeTargets.setItems(toArray(tool.getExtraAttribute(AntUtil.RUN_TARGETS_ATTRIBUTE)));
	}

	/**
	 * @see org.eclipse.ui.externaltools.group.IExternalToolGroup#updateTool(ExternalTool)
	 */
	public void updateTool(ExternalTool tool) {
		tool.setExtraAttribute(AntUtil.RUN_TARGETS_ATTRIBUTE, toString(activeTargets.getItems()));
	}

	/**
	 * @see org.eclipse.ui.externaltools.group.IExternalToolGroup#validate()
	 */
	public void validate() {
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			updateAvailableTargets();
		}
	}

	/*
	 * Translates an array of target names into a 
	 * single string for storage.
	 */
	private String toString(String[] targetArray) {
		return AntUtil.combineRunTargets(targetArray);
	}
	
	/*
	 * Translates a single string of target names into
	 * an array of target names.
	 */
	private String[] toArray(String targetString) {
		return AntUtil.parseRunTargets(targetString);
	}
	
	/*
	 * Adds the current selection in the available list
	 * to the active list.
	 */
	private void addTarget() {
		int index = availableTargets.getSelectionIndex();
		if (index < 0)
			return;
		String target = availableTargets.getItem(index);
		activeTargets.add(target);
	}
	
	/*
	 * Removes the current selection in the active list.
	 */
	private void removeTarget() {
		int index = activeTargets.getSelectionIndex();
		if (index < 0)
			return;
		activeTargets.remove(index);
		deselectAll();
	}
	
	/*
	 * Adds all the available targets to the active list.
	 */
	private void addAllTargets() {
		String[] targets = availableTargets.getItems();
		for (int i=0; i < targets.length; i++) {
			activeTargets.add(targets[i]);	
		}
	}
	
	/*
	 * Removes all the active targets.
	 */
	private void removeAllTargets() {
		activeTargets.removeAll();
		deselectAll();
	}
	
	/*
	 * Moves the current selection in the active list up.
	 */
	private void moveTargetUp() {
		int index = activeTargets.getSelectionIndex();
		// Action only works if selected element is not first element.
		if (index > 0) {
			String target = activeTargets.getItem(index);
			activeTargets.remove(index);
			activeTargets.add(target, index - 1);
			activeTargets.setSelection(index - 1);
		}
		
		updateUpDownButtonEnablement();
	}
	
	/*
	 * Moves the current selection in the active list down.
	 */
	private void moveTargetDown() {
		int index = activeTargets.getSelectionIndex();
		if (index < 0)
			return;
		// Action only works if selected element is not last element.
		if (index < activeTargets.getItemCount() - 1) {
			String target = activeTargets.getItem(index);
			activeTargets.remove(index);
			activeTargets.add(target, index + 1);
			activeTargets.setSelection(index + 1);
		}
		
		updateUpDownButtonEnablement();
	}
	
	/*
	 * Updates the available targets list based on the tool location
	 * provided in the mainGroup for this tool.
	 */
	private void updateAvailableTargets() {
		if (mainGroup == null)
			return;
		String location = mainGroup.getLocationFieldValue();
		if (location != null && !location.equals(oldLocation)) {
			try {
				// Clear the map of target names to target infos.
				mapTargetNamesToTargetInfos.clear();
				availableTargets.removeAll();
				activeTargets.removeAll();
				
				MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
				String expandedLocation = ToolUtil.expandFileLocation(location, ExpandVariableContext.EMPTY_CONTEXT, status);
				if (expandedLocation != null && status.isOK()) {
					TargetInfo[] targets = AntUtil.getTargets(expandedLocation);
					String[] targetNames = new String[targets.length];
					for (int i=0; i < targetNames.length; i++) {
						// Add the target to the map of target names to target infos.
						mapTargetNamesToTargetInfos.put(targets[i].getName(), targets[i]);
						targetNames[i] = targets[i].getName();
					}
					availableTargets.setItems(targetNames);
				} else {
					displayErrorStatus(status);
				}
			} catch (CoreException e) {
				displayErrorStatus(e.getStatus());
			}
		}
		oldLocation = location;			
	}
	
	/*
	 * Displays an error dialog with the given status.
	 */
	private void displayErrorStatus(IStatus status) {
		ErrorDialog.openError(
			null, 
			ToolMessages.getString("AntTargetsGroup.getTargetsTitle"), //$NON-NLS-1$;
			ToolMessages.getString("AntTargetsGroup.getTargetsProblem"), //$NON-NLS-1$;
			status);
	}
	
	/*
	 * A target was selected in one of the lists.
	 */
	private void targetSelected(String targetName, List list) {
		if (targetName == null) {
			enableAvailableListButtons(false);
			enableActiveListButtons(false);
			return;		
		}
			
		if (list == availableTargets) {
			enableAvailableListButtons(true);
			enableActiveListButtons(false);
			activeTargets.deselectAll();
		} else {
			enableActiveListButtons(true);
			enableAvailableListButtons(false);
			availableTargets.deselectAll();
		}
		
		showDescription(targetName);
	}

	/*
	 * Enables the buttons which apply to the current 
	 * selection in the list of active targets.
	 */
	private void enableActiveListButtons(boolean enabled) {
		remove.setEnabled(enabled);
		removeAll.setEnabled(enabled);
		updateUpDownButtonEnablement();
	}
	
	private void updateUpDownButtonEnablement() {
		int index = activeTargets.getSelectionIndex();
		if (index > 0)
			up.setEnabled(true);
		else
			up.setEnabled(false);
	
		if (index >= 0 && index < activeTargets.getItemCount() - 1)
			down.setEnabled(true);		
		else
			down.setEnabled(false);		
	}
	
	/*
	 * Enables the buttons which apply to the current 
	 * selection in the list of available targets.
	 */
	private void enableAvailableListButtons(boolean enabled) {
		add.setEnabled(enabled);
		addAll.setEnabled(enabled);	
	}
	
	/*
	 * Deselects all targets in both lists.
	 */
	private void deselectAll() {
		availableTargets.deselectAll();
		activeTargets.deselectAll();
		disableAllButtons();
		clearDescriptionField();
	}
	
	/*
	 * Disables all buttons in the group.
	 */
	private void disableAllButtons() {
		add.setEnabled(false);
		remove.setEnabled(false);
		addAll.setEnabled(false);
		removeAll.setEnabled(false);
		up.setEnabled(false);
		down.setEnabled(false);
	}
	
	/*
	 * Shows the description of the given target in the
	 * description field.
	 */
	private void showDescription(String targetName) {
		clearDescriptionField();
		if (targetName == null)
			return;
		TargetInfo targetInfo = (TargetInfo) mapTargetNamesToTargetInfos.get(targetName);
		if (targetInfo != null && targetInfo.getDescription() != null)
			description.setText(targetInfo.getDescription());
	}
	
	/*
	 * Clears the description field.
	 */
	 private void clearDescriptionField() {
	 	description.setText(""); //$NON-NLS-1$
	 }
}
