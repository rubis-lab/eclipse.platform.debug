/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.core.ping;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

/**
 * Example launch delegate for a ping command.
 */
public class PingLaunchDelegate extends LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	/**
	 * Launch configuration attribute key. The address to ping - can be an IP address or domain name.
	 */
	public static final String PING_ADDRESS = "PING_ADDRESS";
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		String address = configuration.getAttribute(PING_ADDRESS, "localhost");
		String[] cmdLine = new String[]{"ping", address};
		Process systemProcess = DebugPlugin.exec(cmdLine, null);
		IProcess process = DebugPlugin.newProcess(launch, systemProcess, "ping " + address);
	}

}
