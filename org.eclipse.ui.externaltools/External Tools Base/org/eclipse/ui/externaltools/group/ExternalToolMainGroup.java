package org.eclipse.ui.externaltools.group;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.ToolUtil;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

/**
 * Group of components applicable to most external tools. This group
 * will collect from the user the location, working directory,
 * name, and description for the tool.
 * <p>
 * This group can be used or extended by clients.
 * </p>
 */
public class ExternalToolMainGroup extends ExternalToolGroup {
	private String initialLocation = ""; //$NON-NLS-1$
	private String initialName = ""; //$NON-NLS-1$
	private String initialWorkDirectory = ""; //$NON-NLS-1$
	protected Text locationField;
	protected Text workDirectoryField;
	protected Text nameField;
	protected Text descriptionField;
	
	private ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			validate();
		}
	};
	
	/**
	 * Creates the group
	 */
	public ExternalToolMainGroup() {
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
		layout.numColumns = 2;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		
		createLocationComponent(mainComposite);
		createWorkDirectoryComponent(mainComposite);
		createNameComponent(mainComposite);
		createDescriptionComponent(mainComposite);

		if (locationField != null) {
			locationField.setText(isEditMode() ? tool.getLocation() : initialLocation);
			locationField.addModifyListener(modifyListener);
		}
		
		if (workDirectoryField != null) {
			workDirectoryField.setText(isEditMode() ? tool.getWorkingDirectory() : initialWorkDirectory);
			workDirectoryField.addModifyListener(modifyListener);
		}
		
		if (nameField != null) {
			nameField.setText(isEditMode() ? tool.getName() : initialName);
			if (isEditMode())
				nameField.setEditable(false);
			else
				nameField.addModifyListener(modifyListener);
		}
		
		validate();

		return mainComposite;
	}

	/**
	 * Creates the controls needed to edit the description
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createDescriptionComponent(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(ToolMessages.getString("ExternalToolMainGroup.descriptionLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		descriptionField = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		descriptionField.setLayoutData(data);
		
		createSpacer(parent);
	}
	
	/**
	 * Creates the controls needed to edit the location
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createLocationComponent(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(ToolMessages.getString("ExternalToolMainGroup.locationLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		locationField = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		locationField.setLayoutData(data);
		
		Button button = new Button(parent, SWT.PUSH );
		button.setText(ToolMessages.getString("ExternalToolMainGroup.locationBrowseLabel")); //$NON-NLS-1$
		getPage().setButtonGridData(button);

		createSpacer(parent);
	}
	
	/**
	 * Creates the controls needed to edit the name
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createNameComponent(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(ToolMessages.getString("ExternalToolMainGroup.nameLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		nameField = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		nameField.setLayoutData(data);
		
		createSpacer(parent);
	}
	
	/**
	 * Creates a vertical space between controls.
	 */
	protected void createSpacer(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
	}
	
	/**
	 * Creates the controls needed to edit the working directory
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createWorkDirectoryComponent(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(ToolMessages.getString("ExternalToolMainGroup.workDirLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		workDirectoryField = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		workDirectoryField.setLayoutData(data);
		
		Button button = new Button(parent, SWT.PUSH );
		button.setText(ToolMessages.getString("ExternalToolMainGroup.workDirBrowseLabel")); //$NON-NLS-1$
		getPage().setButtonGridData(button);

		createSpacer(parent);
	}

	/**
	 * Returns the proposed initial location for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial location when editing new tool.
	 */
	public final String getInitialLocation() {
		return initialLocation;
	}

	/**
	 * Returns the proposed initial name for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial name when editing new tool.
	 */
	public final String getInitialName() {
		return initialName;
	}

	/**
	 * Returns the proposed initial working directory for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial working directory when editing new tool.
	 */
	public final String getInitialWorkDirectory() {
		return initialWorkDirectory;
	}

	/**
	 * Returns the name given to the external tool as
	 * found in the text field, or <code>null</code> if
	 * field does not exist
	 */
	public String getNameFieldValue() {
		if (nameField != null)
			return nameField.getText().trim();
		else
			return null;
	}
	
	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void restoreValues(ExternalTool tool) {
		if (locationField != null)
			locationField.setText(tool.getLocation());
		if (workDirectoryField != null)
			workDirectoryField.setText(tool.getWorkingDirectory());
		if (nameField != null)
			nameField.setText(tool.getName());
		if (descriptionField != null)
			descriptionField.setText(tool.getDescription());
	}
	
	/**
	 * Sets the proposed initial location for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialLocation the proposed initial location when editing new tool.
	 */
	public final void setInitialLocation(String initialLocation) {
		if (initialLocation != null)
			this.initialLocation = initialLocation;
	}

	/**
	 * Sets the proposed initial name for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialName the proposed initial name when editing new tool.
	 */
	public final void setInitialName(String initialName) {
		if (initialName != null)
			this.initialName = initialName;
	}

	/**
	 * Sets the proposed initial working directory for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialWorkDirectory the proposed initial working directory when editing new tool.
	 */
	public final void setInitialWorkDirectory(String initialWorkDirectory) {
		if (initialWorkDirectory != null)
			this.initialWorkDirectory = initialWorkDirectory;
	}

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void updateTool(ExternalTool tool) {
		if (locationField != null)
			tool.setLocation(locationField.getText().trim());
		if (workDirectoryField != null)
			tool.setWorkingDirectory(workDirectoryField.getText().trim());
		if (descriptionField != null)
			tool.setDescription(descriptionField.getText().trim());
	}
	
	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void validate() {
		if (!validateLocation())
			return;
		if (!validateWorkDirectory())
			return;
		if (!validateName())
			return;
		getPage().setMessage(null, getPage().NONE);
		setIsValid(true);
	}

	/**
	 * Validates the content of the location field, and
	 * sends any message to the reporter.
	 * 
	 * @return <code>true</code> to continue validating other
	 * 	fields, <code>false</code> to stop.
	 */
	protected boolean validateLocation() {
		if (locationField == null)
			return true;

		String value = locationField.getText().trim();
		if (value.length() < 1) {
			getPage().setMessage(ToolMessages.getString("ExternalToolMainGroup.locationRequired"), getPage().NONE); //$NON-NLS-1$
			setIsValid(false);
			return false;
		}

		// Translate field contents to the actual file location so we
		// can check to ensure the file actually exists.
		try {
			value = ToolUtil.expandFileLocation(value, ExpandVariableContext.EMPTY_CONTEXT);
		} catch (CoreException e) {
			getPage().setMessage(e.getStatus().getMessage(), getPage().WARNING);
			setIsValid(false);
			return false;			
		}
		
		if (value == null) { // The resource could not be found.
			getPage().setMessage(ToolMessages.getString("ExternalToolMainGroup.invalidLocation"), getPage().INFORMATION); //$NON-NLS-1$
			return true;			
		}
		
		File file = new File(value);
		if (!file.exists()) { // The file does not exist.
			getPage().setMessage(ToolMessages.getString("ExternalToolMainGroup.invalidLocation"), getPage().INFORMATION); //$NON-NLS-1$
			return true;
		}
		
		return true;
	}
	
	/**
	 * Validates the content of the name field and
	 * sends any message to the reporter.
	 * 
	 * @return <code>true</code> to continue validating other
	 * 	fields, <code>false</code> to stop.
	 */
	protected boolean validateName() {
		if (isEditMode() || nameField == null)
			return true;
			
		String value = nameField.getText().trim();
		if (value.length() < 1) {
			getPage().setMessage(ToolMessages.getString("ExternalToolMainGroup.nameRequired"), getPage().WARNING); //$NON-NLS-1$
			setIsValid(false);
			return false;
		}
		
		String errorText = ExternalTool.validateToolName(value);
		if (errorText != null) {
			getPage().setMessage(errorText, getPage().WARNING); //$NON-NLS-1$
			setIsValid(false);
			return false;
		}
		
		boolean exists = ExternalToolsPlugin.getDefault().getToolRegistry(nameField.getShell()).hasToolNamed(value);
		if (exists) {
			getPage().setMessage(ToolMessages.getString("ExternalToolMainGroup.nameAlreadyExist"), getPage().WARNING); //$NON-NLS-1$
			setIsValid(false);
			return false;
		}

		return true;
	}
	
	/**
	 * Validates the content of the working directory field and
	 * sends any message to the reporter.
	 * 
	 * @return <code>true</code> to continue validating other
	 * 	fields, <code>false</code> to stop.
	 */
	protected boolean validateWorkDirectory() {
		if (workDirectoryField == null)
			return true;
			
		String value = workDirectoryField.getText().trim();
		if (value.length() > 0) {
			// Translate field contents to the actual directory location so we
			// can check to ensure the directory actually exists.
			try {
				value = ToolUtil.expandDirectoryLocation(value, ExpandVariableContext.EMPTY_CONTEXT);
			} catch (CoreException e) {
				getPage().setMessage(e.getStatus().getMessage(), getPage().WARNING);
				setIsValid(false);
				return false;			
			}
			
			if (value == null) { // The resource could not be found.
				getPage().setMessage(ToolMessages.getString("ExternalToolMainGroup.invalidWorkDir"), getPage().INFORMATION); //$NON-NLS-1$
				return true;			
			}			
			File file = new File(value);
			if (!file.exists()) { // The directory does not exist.
				getPage().setMessage(ToolMessages.getString("ExternalToolMainGroup.invalidWorkDir"), getPage().INFORMATION); //$NON-NLS-1$
				return true;
			}
		}
		
		return true;
	}
}
