package org.eclipse.ui.externaltools.internal.ant.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.ant.core.TargetInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * General utility class dealing with Ant build files
 */
public final class AntUtil {
	public static final String RUN_TARGETS_ATTRIBUTE = IExternalToolConstants.TOOL_TYPE_ANT_BUILD + ".runTargets"; //$NON-NLS-1$;
	private static final String TARGET_SEPARATOR = ","; //$NON-NLS-1$;
	
	/**
	 * No instances allowed
	 */
	private AntUtil() {
		super();
	}
	
	/**
	 * Returns the list of all targets for the Ant build file specified by
	 * the provided IPath, or <code>null</code> if no targets found.
	 * 
	 * @param path the location of the ant build file to get the targets from
	 * @return a list of <code>TargetInfo</code>
	 * 
	 * @throws CoreException if file does not exist, IO problems, or invalid format.
	 */
	public static TargetInfo[] getTargetList(String path) throws CoreException {
		AntRunner runner = new AntRunner();
		runner.setBuildFileLocation(path);
	 	return runner.getAvailableTargets();
	}

	/**
	 * Returns the list of target names to run
	 * 
	 * @param extraAttibuteValue the external tool's extra attribute value
	 * 		for the run targets key.
	 * @return a list of target names
	 */
	public static String[] parseRunTargets(String extraAttibuteValue) {
		if (extraAttibuteValue == null)
			return new String[0];
		
		// Need to handle case where separator character is
		// actually part of the target name!
		StringTokenizer tokenizer = new StringTokenizer(extraAttibuteValue, TARGET_SEPARATOR);
		String[] results = new String[tokenizer.countTokens()];
		for (int i = 0; i < results.length; i++) {
			results[i] = tokenizer.nextToken();
		}
		
		return results;
	}
}
