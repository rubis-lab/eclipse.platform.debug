package org.eclipse.ui.externaltools.internal.ant.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ui.externaltools.model.IExternalToolRunner;
import org.eclipse.ui.externaltools.model.IRunnerContext;

/**
 * Responsible for running ant build files.
 */
public class AntFileRunner implements IExternalToolRunner {
	private static final String ANT_LOGGER_CLASS = "org.eclipse.ui.externaltools.internal.ant.logger.AntBuildLogger"; //$NON-NLS-1$
	private static final String BASE_DIR_PREFIX = "-Dbasedir="; //$NON-NLS-1$

	/**
	 * Creates an ant build file runner
	 */
	public AntFileRunner() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared in IExternalToolsRunner.
	 */
	public void run(IProgressMonitor monitor, IRunnerContext runnerContext) throws CoreException, InterruptedException {
		try {
			// Determine the targets to run.
			String value = runnerContext.getExtraAttribute(AntUtil.RUN_TARGETS_ATTRIBUTE);
			String[] targets = AntUtil.parseRunTargets(value);
			if (targets.length == 0)
				return;

			AntRunner runner = new AntRunner();
			
			// Setup the arguments
			String[] args = runnerContext.getExpandedArguments();
			String baseDir = runnerContext.getExpandedWorkingDirectory();
			if (baseDir.length() > 0) {
				// Ant requires the working directory to be specified
				// as one of the arguments, so it needs to be appended.
				String[] newArgs = new String[args.length + 1];
				System.arraycopy(args, 0, newArgs, 0, args.length);
				newArgs[args.length] = BASE_DIR_PREFIX + baseDir;
				runner.setArguments(newArgs);
			} else {
				runner.setArguments(args);	
			}
			
			runner.setBuildFileLocation(runnerContext.getExpandedLocation());
			if (targets.length > 0)
				runner.setExecutionTargets(targets);
			runner.addBuildLogger(ANT_LOGGER_CLASS);
			runner.run(monitor);
		} catch (CoreException e) {
			Throwable carriedException = e.getStatus().getException();
			if (carriedException instanceof OperationCanceledException) {
				throw new InterruptedException(carriedException.getMessage());
			} else {
				throw e;
			}
		} finally {
			monitor.done();
		}
	}
}
