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
package org.eclipse.debug.ui.viewers;


public interface IModelDelta {
	
	//change type
	public static int NOCHANGE = 0;
	public static int ADDED = 1;
	public static int REMOVED	 = 1 << 1;
	public static int CHANGED = 1 << 2;
	
	//how it changed.
	public static int CONTENT = 1 << 10;
	public static int ACTIVATED = 1 << 11;
	public static int EXPAND = 1 << 12;
	public static int SELECT = 1 << 13;

	public IModelDeltaNode addNode(Object element, int flags);
	public IModelDeltaNode[] getNodes();
}
