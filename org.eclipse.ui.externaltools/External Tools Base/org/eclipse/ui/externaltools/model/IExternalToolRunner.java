package org.eclipse.ui.externaltools.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The implementation of this interface is responsible for running
 * an external tool within a context.
 * <p>
 * Clients using the extension point to define a new external
 * tool type must provide an implementation of this interface.
 * </p><p>
 * This interface is not intended to be extended by clients.
 * Clients must only implement this interface.
 * </p>
 */
public interface IExternalToolRunner {
	/**
	 * Runs an external tool using the specified context.
	 * 
	 * @param monitor the monitor to show progress of running tool
	 * @param runnerContext the context representing the tool to run
	 */
	public void run(IProgressMonitor monitor, IRunnerContext runnerContext) throws CoreException, InterruptedException;
}
