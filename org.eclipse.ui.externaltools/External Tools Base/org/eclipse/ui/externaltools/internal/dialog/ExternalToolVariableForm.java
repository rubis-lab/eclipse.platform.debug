package org.eclipse.ui.externaltools.internal.dialog;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.externaltools.group.IGroupDialogPage;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolVariable;
import org.eclipse.ui.externaltools.model.ToolUtil;
import org.eclipse.ui.externaltools.variable.IVariableComponent;

/**
 * Visual grouping of controls that allows the user to
 * select a variable and configure it with extra
 * information.
 */
public class ExternalToolVariableForm {
	private static final int VISIBLE_ITEM_COUNT = 6;
	
	private String variableListLabelText;
	private ExternalToolVariable[] variables;
	private IVariableComponent[] components;
	private IGroupDialogPage page;
	
	private Label variableListLabel;
	private List variableList;
	private Composite variableComposite;
	private int activeComponentIndex = -1;
	
	/**
	 * Creates the visual grouping
	 * 
	 * @param variableListLabelText the label text to use for identifying the list of variables
	 * @param variables the collection of variables to display to the user
	 */
	public ExternalToolVariableForm(String variableListLabelText, ExternalToolVariable[] variables) {
		super();
		this.variableListLabelText = variableListLabelText;
		this.variables = variables;
		this.components = new IVariableComponent[variables.length];
	}

	public Composite createContents(Composite parent, IGroupDialogPage page) {
		this.page = page;
		
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		GridData data = new GridData(GridData.FILL_BOTH);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(data);

		variableListLabel = new Label(mainComposite, SWT.NONE);
		variableListLabel.setText(variableListLabelText);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 1;
		variableListLabel.setLayoutData(data);

		variableList = new List(mainComposite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = variableList.getItemHeight() * VISIBLE_ITEM_COUNT;
		variableList.setLayoutData(data);

		variableComposite = new Composite(mainComposite, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		data = new GridData(GridData.FILL_BOTH);
		variableComposite.setLayout(layout);
		variableComposite.setLayoutData(data);
		
		populateVariableList();
		
		variableList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateVariableComposite(null);
			}
		});
		
		return mainComposite;
	}
	
	/**
	 * Returns the formatted variable or <code>null</code> if
	 * none selected.
	 */
	public String getSelectedVariable() {
		if (activeComponentIndex != -1) {
			String varValue = null;
			IVariableComponent component = components[activeComponentIndex];
			if (component != null)
				varValue = component.getVariableValue();
			return ToolUtil.buildVariableTag(variables[activeComponentIndex].getTag(), varValue);
		}

		return null;
	}

	/**
	 * Returns whether the current variable selection is
	 * valid, including the selected variable value.
	 */
	public boolean isValid() {
		if (activeComponentIndex != -1) {
			IVariableComponent component = components[activeComponentIndex];
			if (component != null)
				return component.isValid();
		}
		
		return true;
	}

	private void populateVariableList() {
		String[] items = new String[variables.length];
		StringBuffer buffer = new StringBuffer(80);
		for (int i = 0; i < variables.length; i++) {
			ToolUtil.buildVariableTag(variables[i].getTag(), null, buffer);
			buffer.append(" - "); //$NON-NLS-1$
			buffer.append(variables[i].getDescription());
			items[i] = buffer.toString();
			buffer.setLength(0);
		}
		variableList.setItems(items);
	}

	public void selectVariable(String varName, String varValue) {	
		if (varName != null && varName.length() > 0) {
			for (int i = 0; i < variables.length; i++) {
				if (varName.equals(variables[i].getTag())) {
					variableList.select(i);
					updateVariableComposite(varValue);
					return;
				}
			}
		}
		
		variableList.deselectAll();
		updateVariableComposite(varValue);
	}
	
	private void setComponentVisible(int index, boolean visible) {
		if (index == -1)
			return;
		IVariableComponent component = components[index];
		if (component == null) {
			ExternalToolVariable var = variables[index];
			component = var.getComponent();
			component.createContents(variableComposite, var.getTag(), page);
			components[index] = component;
		}
		Control control = component.getControl();
		if (control != null) {
			control.setVisible(visible);
			if (visible) {
				Point newSize = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
				Point oldSize = variableComposite.getSize();
				GridData data = (GridData)variableComposite.getLayoutData();
				data.widthHint = Math.max(newSize.x, oldSize.x);
				data.heightHint = Math.max(newSize.y, oldSize.y);
				variableComposite.getParent().layout(true);
			}
		}
	}
	
	/**
	 * Enables or disables the variable form controls.
	 */
	public void setEnabled(boolean enabled) {
		variableListLabel.setEnabled(enabled);
		variableList.setEnabled(enabled);
		variableComposite.setVisible(enabled);
	}
	
	private void updateVariableComposite(String value) {
		setComponentVisible(activeComponentIndex, false);
		activeComponentIndex = variableList.getSelectionIndex();
		setComponentVisible(activeComponentIndex, true);
	}

	/**
	 * Validates the current variable selection is and
	 * its value are acceptable.
	 */
	public void validate() {
		if (activeComponentIndex != -1) {
			IVariableComponent component = components[activeComponentIndex];
			if (component != null)
				component.validate();
		}
	}
}
