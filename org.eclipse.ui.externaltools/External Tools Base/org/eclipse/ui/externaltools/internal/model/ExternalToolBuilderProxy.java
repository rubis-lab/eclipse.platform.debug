package org.eclipse.ui.externaltools.internal.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * This project builder implementation will run an external tool during the
 * build process. 
 * <p>
 * Note that there is only ever one instance of ExternalToolBuilderProxy per project,
 * and the external tool to run is specified in the builder's arguments.
 * </p>
 */
public class ExternalToolBuilderProxy extends IncrementalProjectBuilder {
	public static final String ID = "org.eclipse.ui.externaltools.ExternalToolBuilderProxy";

	/**
	 * Creates an uninitialized external tool builder.
	 */
	public ExternalToolBuilderProxy() {
		super();
	}
	
	/* (non-Javadoc)
	 * Method declared on IncrementalProjectBuilder.
	 */
	protected final IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		ExternalTool tool = getToolFromMap(args);
		if (tool == null)
			return null;
					
		boolean runTool = false;
		int[] buildKinds = tool.getRunForBuildKinds();
		for (int i = 0; i < buildKinds.length; i++) {
			if (kind == buildKinds[i]) {
				runTool = true;
				break;
			}
		}
		if (!runTool)
			return null;
			
		DefaultRunnerContext context = new DefaultRunnerContext(tool, getProject(), kind);
		try {
			MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
			context.run(monitor, status);
			if (!monitor.isCanceled() && !status.isOK())
				throw new CoreException(status);
		} finally {
			forgetLastBuiltState();
		}
		
		return null;
	}
	
	/**
	 * Returns the external tool based on the arguments
	 * or <code>null</code> if not possible;
	 */
	/* package */ ExternalTool getToolFromMap(Map args) {
		return null;
	}
}
