package org.eclipse.ui.externaltools.group;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.io.File;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.core.ToolMessages;
import org.eclipse.ui.externaltools.model.ExternalTool;

/**
 * Group of components applicable to most external tools. This group
 * will collect from the user the options:
 * <ul>
 * <li>log tool messages to console</li>
 * <li>run tool in background</li>
 * <li>open perspective on run</li>
 * <li>tool arguments</li>
 * <li>prompt for arguments on run</li>
 * </ul>
 * <p>
 * This group can be used or extended by clients.
 * </p>
 */
public class ExternalToolOptionGroup extends ExternalToolGroup {
	private boolean initialShowMessage = true;
	private boolean initialRunBackground = true;
	private String initialOpenPersp = null;
	private String initialArgument = ""; //$NON-NLS-1$
	private boolean initialPromptArg = false;
	
	protected Button showMessageField;
	protected Button runBackgroundField;
	protected Button openPerspField;
	protected Combo openPerspNameField;
	protected Text argumentField;
	protected Button promptArgField;
	
	private IPerspectiveDescriptor[] perspectives;
	
	/**
	 * Creates the group
	 */
	public ExternalToolOptionGroup() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on ExternalToolGroup.
	 */
	protected Control createGroupContents(Composite parent, ExternalTool tool) {
		// main composite
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		
		createShowMessageComponent(mainComposite);
		createRunBackgroundComponent(mainComposite);
		createOpenPerspComponent(mainComposite);
		createArgumentComponent(mainComposite);
		
		if (showMessageField != null) {
			showMessageField.setSelection(isEditMode() ? tool.getLogMessages() : initialShowMessage);
		}
		
		if (runBackgroundField != null) {
			runBackgroundField.setSelection(isEditMode() ? tool.getRunInBackground() : initialRunBackground);
		}
		
		if (openPerspField != null) {
			String perspId = isEditMode() ? tool.getOpenPerspective() : initialOpenPersp;
			openPerspField.setSelection(perspId != null);
		}
		
		if (openPerspNameField != null) {
			int index = -1;
			if (isEditMode())
				index = getPerspectiveIndex(tool.getOpenPerspective());
			else
				index = getPerspectiveIndex(initialOpenPersp);
			if (index != -1)
				openPerspNameField.select(index);
			updateOpenPerspNameField();
		}
		
		if (argumentField != null) {
			argumentField.setText(isEditMode() ? tool.getArguments() : initialArgument);
		}
		
		if (promptArgField != null) {
			promptArgField.setSelection(isEditMode() ? tool.getPromptForArguments() : initialPromptArg);
		}

		validate();

		return mainComposite;
	}
	
	/**
	 * Creates the controls needed to edit the argument and
	 * prompt for argument attributes of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createArgumentComponent(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayout(layout);
		comp.setLayoutData(data);

		Label label = new Label(comp, SWT.NONE);
		label.setText(ToolMessages.getString("ExternalToolOptionGroup.argumentLabel")); //$NON-NLS-1$
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		argumentField = new Text(comp, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		argumentField.setLayoutData(data);

		Button button = new Button(comp, SWT.PUSH);
		button.setText(ToolMessages.getString("ExternalToolOptionGroup.argumentVariableLabel")); //$NON-NLS-1$
		getPage().setButtonGridData(button);

		promptArgField = new Button(parent, SWT.CHECK);
		promptArgField.setText(ToolMessages.getString("ExternalToolOptionGroup.promptArgLabel")); //$NON-NLS-1$
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		promptArgField.setLayoutData(data);
		
		createSpacer(parent);
	}

	/**
	 * Creates the controls needed to edit the open perspective
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createOpenPerspComponent(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayout(layout);
		comp.setLayoutData(data);

		openPerspField = new Button(comp, SWT.CHECK);
		openPerspField.setText(ToolMessages.getString("ExternalToolOptionGroup.openPerspLabel")); //$NON-NLS-1$
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		openPerspField.setLayoutData(data);
		openPerspField.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateOpenPerspNameField();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		openPerspNameField = new Combo(comp, (SWT.DROP_DOWN | SWT.READ_ONLY));
		openPerspNameField.setItems(getOpenPerspectiveNames());
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		openPerspNameField.setLayoutData(data);
		
		createSpacer(parent);
	}
	
	/**
	 * Creates the controls needed to edit the run in background
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createRunBackgroundComponent(Composite parent) {
		runBackgroundField = new Button(parent, SWT.CHECK);
		runBackgroundField.setText(ToolMessages.getString("ExternalToolOptionGroup.runBackgroundLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		runBackgroundField.setLayoutData(data);
	}

	/**
	 * Creates the controls needed to edit the log messages
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createShowMessageComponent(Composite parent) {
		showMessageField = new Button(parent, SWT.CHECK);
		showMessageField.setText(ToolMessages.getString("ExternalToolOptionGroup.showMessageLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		showMessageField.setLayoutData(data);
	}

	/**
	 * Creates a vertical space between controls.
	 */
	protected final void createSpacer(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 1;
		label.setLayoutData(data);
	}

	/**
	 * Returns the proposed initial argument for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial argument when editing new tool.
	 */
	public final String getInitialArgument() {
		return initialArgument;
	}

	/**
	 * Returns the proposed initial open perspective id for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial open perspective id when editing new tool.
	 */
	public final String getInitialOpenPerspective() {
		return initialOpenPersp;
	}

	/**
	 * Returns the proposed initial prompt for argument for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial prompt for argument when editing new tool.
	 */
	public final boolean getInitialPromptForArgument() {
		return initialPromptArg;
	}

	/**
	 * Returns the proposed initial run in background for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial run in background when editing new tool.
	 */
	public final boolean getInitialRunInBackground() {
		return initialRunBackground;
	}

	/**
	 * Returns the proposed initial log message for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial log message when editing new tool.
	 */
	public final boolean getInitialShowMessage() {
		return initialShowMessage;
	}

	/**
	 * Returns the list of perspective names to place in
	 * the open perspective combo box. This list contains
	 * all the available perspectives in the workbench.
	 */
	protected final String[] getOpenPerspectiveNames() {
		String[] names = new String[getPerspectives().length];

		for (int i = 0; i < getPerspectives().length; i++) {
			names[i] = getPerspectives()[i].getLabel();
		}
		
		return names;
	}

	/**
	 * Returns the ID for the perspective in the combo box
	 * at the specified index, or <code>null</code> if
	 * none.
	 */
	protected final String getPerspectiveId(int index) {
		if (index < 0 || index > getPerspectives().length)
			return null;
		return getPerspectives()[index].getId();
	}
		
	/**
	 * Returns the index in the perspective combo that
	 * matches the specified perspective ID, or -1 if
	 * none found.
	 */
	protected final int getPerspectiveIndex(String perspId) {
		if (perspId == null)
			return -1;

		for (int i = 0; i < getPerspectives().length; i++) {
			if (perspId.equals(getPerspectives()[i].getId()))
				return i;
		}
		
		return -1;
	}
	
	/**
	 * Returns the list of perspectives known to the workbench.
	 * The list is also sorted by name in alphabetical order.
	 */
	protected final IPerspectiveDescriptor[] getPerspectives() {
		if (perspectives == null) {
			perspectives = PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives();
			Arrays.sort(perspectives, new Comparator() {
				private Collator collator = Collator.getInstance();
			
				public int compare(Object o1, Object o2) {
					String name1 = ((IPerspectiveDescriptor)o1).getLabel();
					String name2 = ((IPerspectiveDescriptor)o2).getLabel();
					return collator.compare(name1, name2);
				}
			});
		}
		return perspectives;
	}
	
	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void restoreValues(ExternalTool tool) {
		if (showMessageField != null)
			showMessageField.setSelection(tool.getLogMessages());
		if (runBackgroundField != null)
			runBackgroundField.setSelection(tool.getRunInBackground());
		if (openPerspField != null)
			openPerspField.setSelection(tool.getOpenPerspective() != null);
		if (openPerspNameField != null) {
			int index = getPerspectiveIndex(tool.getOpenPerspective());
			if (index != -1)
				openPerspNameField.select(index);
		}
		updateOpenPerspNameField();
		if (argumentField != null)
			argumentField.setText(tool.getArguments());
		if (promptArgField != null)
			promptArgField.setSelection(tool.getPromptForArguments());
	}
	
	/**
	 * Sets the proposed initial argument for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialArgument the proposed initial argument when editing new tool.
	 */
	public final void setInitialArgument(String initialArgument) {
		if (initialArgument != null)
			this.initialArgument = initialArgument;
	}

	/**
	 * Sets the proposed initial open perspective id for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialOpenPersp the proposed initial open perspective id when editing new tool.
	 */
	public final void setInitialOpenPerspective(String initialOpenPersp) {
		this.initialOpenPersp = initialOpenPersp;
	}

	/**
	 * Sets the proposed initial prompt for argument for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialPromptArg the proposed initial prompt for argument when editing new tool.
	 */
	public final void setInitialPromptForArgument(boolean initialPromptArg) {
		initialPromptArg = initialPromptArg;
	}

	/**
	 * Sets the proposed initial run in background for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialRunBackground the proposed initial run in background when editing new tool.
	 */
	public final void setInitialRunInBackground(boolean initialRunBackground) {
		this.initialRunBackground = initialRunBackground;
	}

	/**
	 * Sets the proposed initial log message for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialShowMessage the proposed initial log message when editing new tool.
	 */
	public final void setInitialShowMessage(boolean initialShowMessage) {
		this.initialShowMessage = initialShowMessage;
	}

	/**
	 * Updates the enablement state of the open perspective
	 * combo box if required.
	 */
	protected void updateOpenPerspNameField() {
		if (openPerspNameField != null && openPerspField != null)
			openPerspNameField.setEnabled(openPerspField.getSelection());
	}
	
	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void updateTool(ExternalTool tool) {
		if (showMessageField != null)
			tool.setLogMessages(showMessageField.getSelection());
		if (runBackgroundField != null)
			tool.setRunInBackground(runBackgroundField.getSelection());
		if (openPerspField != null && openPerspNameField != null) {
			if (openPerspField.getSelection())
				tool.setOpenPerspective(getPerspectiveId(openPerspNameField.getSelectionIndex()));
			else
				tool.setOpenPerspective(null);
		}
		if (argumentField != null)
			tool.setArguments(argumentField.getText().trim());
		if (promptArgField != null)
			tool.setPromptForArguments(promptArgField.getSelection());
	}

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void validate() {
		// do nothing
	}
}
