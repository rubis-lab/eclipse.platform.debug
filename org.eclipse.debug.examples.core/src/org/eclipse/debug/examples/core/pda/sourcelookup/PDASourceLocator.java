/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.core.pda.sourcelookup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
//#ifdef ex_ec2009        
//#else 
import java.net.URI;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.examples.core.pda.model.PDAStackFrame;
//#endif

/**
 * Source locator examines the stack frame and returns the corresponding
 * source file object.
 */
public class PDASourceLocator implements IPersistableSourceLocator {
    
    public Object getSourceElement(IStackFrame stackFrame) {
        //#ifdef ex_ec2009
        // TODO Exercise 3
        //
        // The source locator must take the stack frame and based on its
        // information determine the source file.
        //
        // Use the PDAStackFrame.getSourcePath() to obtain the source file 
        // information.  Then use the ResourcesPlugin and WorkspaceRoot 
        // container to find the resource corresponding to the source path.
//#        return null;
        //#else 
        if (!(stackFrame instanceof PDAStackFrame)) {
            return null;
        }
        IPath sourcePath = ((PDAStackFrame)stackFrame).getSourcePath();
        URI sourceUri = URIUtil.toURI(sourcePath.makeAbsolute()); 
        IFile[] sourceFiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(sourceUri);
        if (sourceFiles == null || sourceFiles.length == 0) {
            return null;
        }
        return sourceFiles[0];
        //#endif
    }
    
    public String getMemento() throws CoreException {
        return "";
    }
    
    public void initializeFromMemento(String memento) throws CoreException {
    }
    
    public void initializeDefaults(ILaunchConfiguration configuration) throws CoreException {
    }
}
