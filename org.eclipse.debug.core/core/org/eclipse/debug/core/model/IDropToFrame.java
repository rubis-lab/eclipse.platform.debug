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
package org.eclipse.debug.core.model;

import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to drop to frame. Drop to frame
 * generally means popping a selected stack frame (and all frames above it) from
 * the execution stack and then stepping back into the frame.
 * 
 * @since 3.1
 */
public interface IDropToFrame {

    /**
     * Returns whether this element can currently perform a drop to frame.
     * @return whether this element can currently perform a drop to frame
     */
    public boolean canDropToFrame();
    
    /**
     * Drops to frame. Can only be called
     * when the associated thread is suspended.
     * @throws DebugException on failure. Reasons include:<ul>
     * <li>TARGET_REQUEST_FAILED - The request failed in the target</li>
     * <li>NOT_SUPPORTED - The capability is not supported by the target</li>
     * </ul>
     */
    public void dropToFrame() throws DebugException;
}
