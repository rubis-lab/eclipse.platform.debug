package org.eclipse.ui.externaltools.group;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.externaltools.model.ExternalTool;

/**
 * The standard abstract implementation of an <code>IExternalToolGroup</code>.
 */
public abstract class ExternalToolGroup implements IExternalToolGroup {
	/**
	 * Recommended initial size for a text field within a group.
	 */
	public static final int SIZING_TEXT_FIELD_WIDTH = 250;

	/**
	 * Dialog page this group is part of.
	 */
	private DialogPage page;
	
	/**
	 * Whether the group is working with an existing external
	 * tool, or a, yet to be created, new external tool.
	 */
	private boolean isEditMode = true;

	/**
	 * Creates the group.
	 */
	public ExternalToolGroup() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public final Control createContents(Composite parent, ExternalTool tool, DialogPage page) {
		this.page = page;
		this.isEditMode = tool != null;
		return createGroupContents(parent, tool);
	}

	/**
	 * Creates this group's visual components.
	 *
	 * @param parent the composite to parent the group's control
	 * @param tool the external tool to be edited, or <code>null</code> for a new tool
	 * @return the control for the group
	 */
	protected abstract Control createGroupContents(Composite parent, ExternalTool tool);
	
	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void dispose() {
		page = null;
	}

	/**
	 * Returns the dialog page this group is part of.
	 * 
	 * @return the dialog page this group is part of
	 */
	public final DialogPage getPage() {
		return page;
	}

	/**
	 * Returns <code>true</code> if the group is editing an existing
	 * external tool, or <code>false</code> if the external tool is new
	 * and yet to be created.
	 * 
	 * @return <code>true</code> if the external tool exist already, or
	 * 		<code>false</code> if the external tool is yet to be created.
	 */
	public final boolean isEditMode() {
		return isEditMode;
	}

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public abstract boolean isValid();

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public abstract void restoreValues(ExternalTool tool);

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public abstract void updateTool(ExternalTool tool);
}
