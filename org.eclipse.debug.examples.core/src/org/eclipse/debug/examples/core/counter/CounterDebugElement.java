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
package org.eclipse.debug.examples.core.counter;

import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * Common function for elements from the example counter debug model.
 */
public abstract class CounterDebugElement extends DebugElement {
	
	/**
	 * Constructs a debug element for the counter debug model.
	 * 
	 * @param target debug target
	 */
	public CounterDebugElement(IDebugTarget target) {
		super(target);
	}

	/**
	 * Debug model identifier for the counter debug model example
	 */
	public static final String COUNTER_MODEL_ID = "counter.debugModel";

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return COUNTER_MODEL_ID;
	}

}
