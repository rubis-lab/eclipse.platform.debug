package org.eclipse.ui.externaltools.variable;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.externaltools.internal.core.ToolMessages;

/**
 * Visual component to edit the <code>resource_loc</code> variable
 * value for the file location. Variable is limited to a specific
 * <code>IFile</code> resource.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 */
public class SpecificFileLocVariableComponent extends ResourceLocVariableComponent {

	/**
	 * Creates an instance
	 */
	public SpecificFileLocVariableComponent() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on ResourceLocVariableComponent.
	 */
	protected void createSelectedResourceOption() {
		// Do not present this option...
	}
	
	/* (non-Javadoc)
	 * Method declared on ResourceLocVariableComponent.
	 */
	protected void createSpecificResourceOption() {
		Label label = new Label(mainGroup, SWT.NONE);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		label.setLayoutData(data);
		label.setText(ToolMessages.getString("ResourceLocVariableComponent.specificResLabel")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * Method declared on ResourceLocVariableComponent.
	 */
	protected boolean validateResourceListSelection() {
		if (resourceList == null)
			return true;
		IStructuredSelection sel = (IStructuredSelection) resourceList.getSelection();
		IResource resource = (IResource) sel.getFirstElement();
		return resource != null && resource.getType() == resource.FILE;
	}
}
