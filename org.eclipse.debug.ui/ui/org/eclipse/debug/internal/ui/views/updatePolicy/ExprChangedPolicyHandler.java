/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.updatePolicy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.ui.progress.UIJob;

public class ExprChangedPolicyHandler extends ExprEvtPolicyHandler {

	public void expressionsChanged(final IExpression[] expressions) {
		UIJob job = new UIJob("Expression Changed Job") { //$NON-NLS-1$

			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (isDisposed())
					return Status.OK_STATUS;
				
				getView().getViewer().getControl().setRedraw(false);
				for (int i = 0; i < expressions.length; i++) {
					IExpression expression = expressions[i];
					getView().refresh(expression, true);
				}
				getView().getViewer().getControl().setRedraw(true);
				return Status.OK_STATUS;
			}};
		job.setSystem(true);
		job.schedule();
	}
}
