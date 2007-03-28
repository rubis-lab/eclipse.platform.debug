/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.commands.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Terminate and remove action.
 * 
 * @since 3.3
 */
public class TerminateAndRemoveAction extends DebugCommandAction {

    
    class TerminateAndRemoveParticipant implements ICommandParticipant {
        private Object[] fElements;
        
        TerminateAndRemoveParticipant(Object[] elements) {
            fElements = elements;
        }
        
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.commands.actions.ICommandParticipant#requestDone(org.eclipse.debug.core.commands.IRequest)
		 */
		public void requestDone(IRequest request) {
			IStatus status = request.getStatus();
			if(status == null || status.isOK()) {
				for (int i = 0; i < fElements.length; i++) {
					Object element = fElements[i];
	                ILaunch launch= null;
	                if (element instanceof ILaunch) {
	                    launch= (ILaunch) element;
	                } else if (element instanceof IDebugElement) {
	                    launch= ((IDebugElement) element).getLaunch();
	                } else if (element instanceof IProcess) {
	                    launch= ((IProcess) element).getLaunch();
	                }   
	                if (launch != null)
	                    DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);					
				}
            }
		}
        
    }

    public String getText() {
        return ActionMessages.TerminateAndRemoveAction_0;
    }

    public String getHelpContextId() {
        return "terminate_and_remove_action_context"; //$NON-NLS-1$
    }

    public String getId() {
        return "org.eclipse.debug.ui.debugview.popupMenu.terminateAndRemove"; //$NON-NLS-1$
    }

    public String getToolTipText() {
        return ActionMessages.TerminateAndRemoveAction_3;
    }

    public ImageDescriptor getDisabledImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_TERMINATE_AND_REMOVE);
    }

    public ImageDescriptor getHoverImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_AND_REMOVE);
    }

    public ImageDescriptor getImageDescriptor() {
        return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_TERMINATE_AND_REMOVE);
    }

    protected Class getCommandType() {
		return ITerminateHandler.class;
	}

	protected ICommandParticipant getCommandParticipant(Object[] targets) {
		return new TerminateAndRemoveParticipant(targets);
	}

    
}