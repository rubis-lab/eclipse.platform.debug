package org.eclipse.ui.externaltools.internal.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.util.Map;

import org.eclipse.ui.externaltools.internal.registry.ExternalToolMigration;
import org.eclipse.ui.externaltools.model.ExternalTool;

/**
 * This project builder implementation will run an external tool during the
 * build process. This builder handles external tools saved in the old
 * format (release 2.0).
 * <p>
 * Note that there is only ever one instance of ExternalToolOldBuilder per project,
 * and the external tool to run is specified in the builder's arguments.
 * </p>
 */
public final class ExternalToolOldBuilder extends ExternalToolBuilderProxy {
	public static final String ID = "org.eclipse.ui.externaltools.ExternalToolBuilder"; //$NON-NLS-1$

	/**
	 * Creates an uninitialized external tool builder.
	 */
	public ExternalToolOldBuilder() {
		super();
	}
	
	/* (non-Javadoc)
	 * Method declared on ExternalToolBuilderProxy.
	 */
	/* package */ ExternalTool getToolFromMap(Map args) {
		return ExternalToolMigration.toolFromArgumentMap(args, null, "ExternalToolOldBuilderName"); //$NON-NLS-1$;
	}
}
