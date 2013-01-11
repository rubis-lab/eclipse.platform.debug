/*******************************************************************************
 * Copyright (c) 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.ui.contexts;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Creates a new instances of views.  It manages the view's secondary IDs and
 * re-uses them as needed.
 * 
 * @since 3.9
 */
public class NewViewInstanceAction extends Action {

    public static String PIN_CLONE_VIEW_TAG = "PLATFORM_PIN_CLONE_VIEW_"; //$NON-NLS-1$
    private static final Map viewIdToNextCounterMap = Collections.synchronizedMap(new HashMap());

	private IViewPart fView;

	/**
	 * Creates the new instance action for the given view.
	 */
	public NewViewInstanceAction(IViewPart view) {
		super(ActionMessages.NewViewAction_label);
		fView = view;
		setToolTipText(ActionMessages.NewViewAction_label);   
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_NEW_CONFIG));
		setId(DebugUIPlugin.getUniqueIdentifier() + ".NewViewAction"); //$NON-NLS-1$
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugHelpContextIds.NEW_VIEW_ACTION);
	}
	
	public void run() {
		try {
			String viewId = fView.getSite().getId();
			String secondaryId = encodeClonedPartSecondaryId(getNextCounter(viewId).toString());
			if (fView instanceof IPinnablePart) {
				((IPinnablePart)fView).copyViewSettings(secondaryId);
			}
			fView.getSite().getPage().showView(viewId, secondaryId, IWorkbenchPage.VIEW_CREATE);
		} catch (PartInitException e) {
			DebugUIPlugin.log(e);
		}
	}

	/**
	 * Returns the given part's secondary ID to the pool of available IDs.  
	 * This method should be called when a cloneable view instance is 
	 * being disposed.  
	 * 
	 * @param part Part to recycle.
	 */
	public static void recycleCounterId(IViewPart part) {
	    if ( !isClonedPart(part))
	        return;
	    
	    String viewId = part.getSite().getId();
	    String secondaryId = ((IViewSite)part.getSite()).getSecondaryId();
	        
	    if (secondaryId != null) {
	        Set secondaryIdSet = (Set)viewIdToNextCounterMap.get(viewId);
	        if (secondaryIdSet != null) {   
	            secondaryIdSet.remove(new Integer(decodeClonedPartSecondaryId(secondaryId)));
	        }
	    }
	}
	    
	
	/**
	 * Returns the next available secondary ID for the given primary view ID.
	 * 
	 * @param viewId Primary view ID to look up.
	 * @return Next available secondary ID.
	 */
    protected static Integer getNextCounter(String viewId) {
        Set secondaryIdSet = (Set)viewIdToNextCounterMap.get(viewId);
        if (secondaryIdSet == null) {
            secondaryIdSet = new HashSet();
            viewIdToNextCounterMap.put(viewId, secondaryIdSet);
        }
        
        for (int i = 1; i < Integer.MAX_VALUE; ++i) {
            Integer next = new Integer(i);
            if (!secondaryIdSet.contains(next)) {
                secondaryIdSet.add(next);
                return next;
            }
        }
        
        return new Integer(0);
    }

    
    /**
     * Encodes cloned part secondary id.
     * 
     * @param secondaryId the part's secondary id.
     * @return an encoded part secondary id, can be <code>null</code>.
     */
    public static String encodeClonedPartSecondaryId(String secondaryId) {
        return PIN_CLONE_VIEW_TAG + secondaryId;
    }
    
    /**
     * Decodes cloned part secondary id.
     * 
     * @param secondaryId the part's secondary id
     * @return a decoded part secondary id
     */
    public static String decodeClonedPartSecondaryId(String secondaryId) {
        if (secondaryId == null)
            return ""; //$NON-NLS-1$
        return secondaryId.replaceFirst(PIN_CLONE_VIEW_TAG, ""); //$NON-NLS-1$
    }
    
    /**
     * Determine whether the view part is a cloned part.
     * 
     * @param part the view part
     * @return true if it is a cloned part
     */
    public static boolean isClonedPart(IViewPart part) {
        String secondaryId = part.getViewSite().getSecondaryId();
        return hasCloneTag(secondaryId);        
    }
    
    /**
     * Determine whether the view reference is a cloned part.
     *  
     * @param ref the view reference
     * @return true if it is a cloned part
     */
    public static boolean isClonedPart(IViewReference ref) {
        String secondaryId = ref.getSecondaryId();
        return hasCloneTag(secondaryId);
    }
    
    /**
     * Returns whether the id has the <code>PIN_CLONE_VIEW_TAG</code>.
     * 
     * @param id view id
     * @return true if it has the tag, otherwise false
     */
    private static boolean hasCloneTag(String id) {
        return id != null && id.startsWith(PIN_CLONE_VIEW_TAG);
    }       
}