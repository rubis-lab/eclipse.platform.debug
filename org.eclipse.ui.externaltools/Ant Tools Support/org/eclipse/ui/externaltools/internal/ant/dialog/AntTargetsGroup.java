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

	private String fileLocation = null;
	private TargetInfo defaultTarget = null;
	private Map mapTargetNamesToTargetInfos = new HashMap();
	private ArrayList subTargets = new ArrayList();

	private Button runDefaultTarget;
	private Button showSubTargets;
	private List availableTargets;
	private List activeTargets;
	private Button add;
	private Button remove;
	private Button addAll;
	private Button removeAll;
	private Button up;
	private Button down;
	private Text description;
	private Label descriptionLabel;
	private Label availableLabel;
	private Label activeLabel;

	public AntTargetsGroup() {
		super();
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
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		upperComposite.setLayout(layout);
		upperComposite.setLayoutData(gridData);		
		
		createRunDefaultTargetButton(upperComposite);
										
		Composite middleComposite = new Composite(mainComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 4;
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		middleComposite.setLayout(layout);
		middleComposite.setLayoutData(gridData);
		
		createAvailableTargetsList(middleComposite);
		createAddRemoveComposite(middleComposite);
		createActiveTargetsList(middleComposite);
		createUpDownComposite(middleComposite);
		
		Composite lowerComposite = new Composite(mainComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		lowerComposite.setLayout(layout);
		lowerComposite.setLayoutData(gridData);		
		
		createDescriptionField(lowerComposite);
		createShowSubTargetsButton(lowerComposite);
		
		if (tool != null) 
			restoreValues(tool);
		allowSelectTargets(! runDefaultTarget.getSelection());
		
		return mainComposite;
	}
	
	private void createSpacer(Composite parent) {
		Label spacer = new Label(parent, SWT.NONE);	
	}
	
	/*
	 * Creates the checkbox button for the
	 * "Run default target" preference.
	 */
	private void createRunDefaultTargetButton(Composite parent) {
		runDefaultTarget = new Button(parent, SWT.CHECK);
		// The label that is applied if the default target is unknown
		runDefaultTarget.setText(ToolMessages.getString("AntTargetsGroup.runDefaultTargetUnknownLabel")); //$NON-NLS-1$
		runDefaultTarget.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				runDefaultTargetSelected();
			}			
		});
		runDefaultTarget.setSelection(true);
	}
	
	/*
	 * Creates the checkbox button for the
	 * "Show sub-targets" preference.
	 */
	private void createShowSubTargetsButton(Composite parent) {
		showSubTargets = new Button(parent, SWT.CHECK);
		showSubTargets.setText(ToolMessages.getString("AntTargetsGroup.showSubTargetsLabel")); //$NON-NLS-1$
		showSubTargets.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (showSubTargets.getSelection()) {
					showSubTargets();
				} else {
					hideSubTargets();
				}
			}			
		});
		showSubTargets.setSelection(false);			
	}
	
	/*
	 * Creates the list of targets provided by the ant build tool.
	 */
	private void createAvailableTargetsList(Composite parent) {
		Composite listComposite = new Composite(parent, SWT.NONE);
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		listComposite.setLayoutData(gridData);
		
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		listComposite.setLayout(layout);
				
		availableLabel = new Label(listComposite, SWT.LEFT);
		availableLabel.setText(ToolMessages.getString("AntTargetsGroup.availableTargetsLabel"));
		
		availableTargets = new List(listComposite, SWT.BORDER | SWT.MULTI);	
		gridData = new GridData(GridData.FILL_BOTH);
		availableTargets.setLayoutData(gridData);
		
		availableTargets.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = availableTargets.getSelectionIndex();
				if (index >= 0)
					targetsSelected(availableTargets.getItem(index), availableTargets);
				else
					deselectAll();
			}
		});	
	}
	
	/*
	 * Creates the list of targets that will be used when the tool is run.
	 */
	private void createActiveTargetsList(Composite parent) {
		Composite listComposite = new Composite(parent, SWT.NONE);
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		listComposite.setLayoutData(gridData);
		
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		listComposite.setLayout(layout);
				
		activeLabel = new Label(listComposite, SWT.LEFT);
		activeLabel.setText(ToolMessages.getString("AntTargetsGroup.activeTargetsLabel"));
		
		activeTargets = new List(listComposite, SWT.BORDER | SWT.MULTI);	
		gridData = new GridData(GridData.FILL_BOTH);
		activeTargets.setLayoutData(gridData);
		
		activeTargets.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = activeTargets.getSelectionIndex();
				if (index >= 0)
					targetsSelected(activeTargets.getItem(index), activeTargets);
				else
					deselectAll();
			}
		});		
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
					addTargets();
				}
			},
			false);
		
		remove = createButton(
			addRemoveComposite, 
			ToolMessages.getString("AntTargetsGroup.removeLabel"), //$NON-NLS-1$	
			new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					removeTargets();
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
		descriptionLabel = new Label(parent, SWT.NONE);
		descriptionLabel.setText(ToolMessages.getString("AntTargetsGroup.descriptionLabel")); //$NON-NLS-1$
		
		description = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = getPage().convertHeightHint(DESCRIPTION_FIELD_HEIGHT);
		description.setLayoutData(data);
	}

	/**
	 * @see org.eclipse.ui.externaltools.group.IExternalToolGroup#restoreValues(ExternalTool)
	 */
	public void restoreValues(ExternalTool tool) {
		if (activeTargets != null) {
			activeTargets.setItems(toArray(tool.getExtraAttribute(AntUtil.RUN_TARGETS_ATTRIBUTE)));

			if (activeTargets.getItemCount() == 0) {
				runDefaultTarget.setSelection(true);
				runDefaultTargetSelected();
			}
		}
	}

	/**
	 * @see org.eclipse.ui.externaltools.group.IExternalToolGroup#updateTool(ExternalTool)
	 */
	public void updateTool(ExternalTool tool) {
		if (runDefaultTarget == null)
			return;
		if (runDefaultTarget.getSelection()) {
			tool.setExtraAttribute(AntUtil.RUN_TARGETS_ATTRIBUTE, null);
		} else {
			if (activeTargets != null)
				tool.setExtraAttribute(AntUtil.RUN_TARGETS_ATTRIBUTE, toString(activeTargets.getItems()));
		}
	}

	/**
	 * @see org.eclipse.ui.externaltools.group.IExternalToolGroup#validate()
	 */
	public void validate() {
	}
	
	/**
	 * Informs the group of the current external tool
	 * file location.
	 */
	public void setFileLocation(String newLocation) {
		if (newLocation == null) {
			if (fileLocation != null) {
				fileLocation = newLocation;
				updateAvailableTargets();
			}
		} else if (!newLocation.equals(fileLocation)) {
			fileLocation = newLocation;
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
	 * The "run default target" preference has been selected.
	 */
	private void runDefaultTargetSelected() {
		allowSelectTargets(! runDefaultTarget.getSelection());
	}
	
	/*
	 * Adds the current selection in the available list
	 * to the active list.
	 */
	private void addTargets() {
		String[] targets = availableTargets.getSelection();
		for (int i=0; i < targets.length; i++) {
			activeTargets.add(targets[i]);
		}
		updateButtonEnablement();
	}
	
	/*
	 * Removes the current selection in the active list.
	 */
	private void removeTargets() {
		String[] targets = activeTargets.getSelection();
		for (int i=0; i < targets.length; i++) {
			activeTargets.remove(targets[i]);
		}
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
		updateButtonEnablement();
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
	 * for this tool.
	 */
	private void updateAvailableTargets() {
		// Clear the map of target names to target infos.
		mapTargetNamesToTargetInfos.clear();
		subTargets.clear();
		availableTargets.removeAll();
		activeTargets.removeAll();
		
		if (fileLocation == null)
			return;
			
		try {
			MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
			String expandedLocation = ToolUtil.expandFileLocation(fileLocation, ExpandVariableContext.EMPTY_CONTEXT, status);
			if (expandedLocation != null && status.isOK()) {
				TargetInfo[] targets = AntUtil.getTargets(expandedLocation);
				ArrayList targetNameList = new ArrayList();
				for (int i=0; i < targets.length; i++) {
					if (! AntUtil.isInternalTarget(targets[i])) {
						// Add the target to the map of target names to target infos.
						mapTargetNamesToTargetInfos.put(targets[i].getName(), targets[i]);

						if (targets[i].isDefault()) {
							defaultTarget = targets[i];
							runDefaultTarget.setText(ToolMessages.format("AntTargetsGroup.runDefaultTargetLabel", new Object[] {targets[i].getName()})); //NON-NLS-1$
						}
						
						if (AntUtil.isSubTarget(targets[i])) {
							subTargets.add(targets[i].getName());
						} else {
							targetNameList.add(targets[i].getName());
						}
					}
				}
				if (showSubTargets.getSelection())
					targetNameList.addAll(subTargets);
					
				String[] targetNames = (String[]) targetNameList.toArray(new String[targetNameList.size()]);
				availableTargets.setItems(targetNames);
			} else {
				displayErrorStatus(status);
			}
		} catch (CoreException e) {
			displayErrorStatus(e.getStatus());
		}
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
	private void targetsSelected(String targetName, List list) {
		updateButtonEnablement();

		if (targetName == null)	
			return;		
		if (list == availableTargets) {
			activeTargets.deselectAll();
		} else {
			availableTargets.deselectAll();
		}
		
		showDescription(targetName);
	}
	
	/*
	 * Updates the enablement of all the buttons in the group.
	 */
	private void updateButtonEnablement() {
		updateUpDownButtonEnablement();
		updateAddRemoveButtonEnablement();
	}
	
	/*
	 * Updates the enabled state of the up and down buttons based
	 * on the current list selection.
	 */
	private void updateUpDownButtonEnablement() {
		if (activeTargets.getEnabled() == false) {
			disableUpDownButtons();
			return;
		}
		// Disable up and down buttons if there is not one
		// target selected in the active list.
		if (activeTargets.getSelectionCount() != 1) {
			disableUpDownButtons();
			return;	
		}
			
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
	 * Updates the enabled state of the add, remove, addAll, and
	 * removeAll buttons based on the current list selection.
	 */
	private void updateAddRemoveButtonEnablement() {
		if (runDefaultTarget.getSelection()) {
			disableAddRemoveButtons();
			return;
		}
		int availableIndex = availableTargets.getSelectionIndex();
		int activeIndex = activeTargets.getSelectionIndex();
		add.setEnabled(availableIndex >= 0);
		remove.setEnabled(activeIndex >= 0);
		addAll.setEnabled(availableTargets.getItemCount() > 0);
		removeAll.setEnabled(activeTargets.getItemCount() > 0);
	}
	
	/*
	 * Deselects all targets in both lists.
	 */
	private void deselectAll() {
		availableTargets.deselectAll();
		activeTargets.deselectAll();
		updateButtonEnablement();
		clearDescriptionField();
	}

	/*
	 * Enables all the appropriate controls.
	 */
	private void allowSelectTargets(boolean enabled) {
		if (! enabled) {
			deselectAll();
			if (defaultTarget != null && defaultTarget.getDescription() != null)
				description.setText(defaultTarget.getDescription());	
		} else {
			description.setText(""); //$NON-NLS-1$
		}
		
		availableLabel.setEnabled(enabled);
	 	availableTargets.setEnabled(enabled);
	 	activeLabel.setEnabled(enabled);
	 	activeTargets.setEnabled(enabled);
	 	descriptionLabel.setEnabled(enabled);
	 	description.setEnabled(enabled);
	 	showSubTargets.setEnabled(enabled);
	 	updateButtonEnablement();	
	}

	/*
	 * Disables all buttons in the group.
	 */
	private void disableAllButtons() {
		disableAddRemoveButtons();
		disableUpDownButtons();
	}
	
	/*
	 * Disables the add, remove, addAll, and
	 * removeAll buttons.
	 */
	private void disableAddRemoveButtons() {
		add.setEnabled(false);
		remove.setEnabled(false);
		addAll.setEnabled(false);
		removeAll.setEnabled(false);		
	}
	
	/*
	 * Disables the up and down buttons.
	 */
	private void disableUpDownButtons() {
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
	 
	 /*
	  * Shows sub-targets in the available targets list.
	  */
	 private void showSubTargets() {
	 	Iterator i = subTargets.iterator();
	 	while (i.hasNext()) {
	 		String target = (String) i.next();
	 		availableTargets.add(target);
	 	}
	 }
	 
	 /*
	  * Hides sub-targets in the available targets list.
	  */
	 private void hideSubTargets() {
	 	int startOfSubTargets = availableTargets.getItemCount() - subTargets.size();
	 	int endOfSubTargets = availableTargets.getItemCount() - 1;
	 	availableTargets.remove(startOfSubTargets, endOfSubTargets);
	 }
}
