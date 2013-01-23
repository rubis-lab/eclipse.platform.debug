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

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.part.ViewPart;

/**
 * Base class for views that allow pinning to a debug context.  It implements
 * creating and clearing the pin control.  Also adds the pin and clone actions 
 * to the view's tool-bar. 
 * 
 * @since 3.9
 */
abstract public class AbstractPinnableView extends ViewPart implements IPinnablePart {

	private static final String PINNED_CONTEXT_VIEWER_ID = DebugUIPlugin.getUniqueIdentifier() + ".PINNED_CONTEXT_VIEWER_ID"; //$NON-NLS-1$

	/**
	 * Composite that holds the pin control and part's contents.
	 */
    private Composite fComposite;
    
    /**
     * Pin control created by the pinnable debug context provider.
     */
    private IPinnedContextViewer fPinnedContextViewer;

    public void pin(IPinnedContextFactory factory) {
        fPinnedContextViewer = factory.createPinnedContextViewer(this);
        Control control = fPinnedContextViewer.createControl(fComposite);
        control.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        control.moveAbove(null);
        fComposite.layout(true,  true);
    }

    public void clearPin() {
        if (fPinnedContextViewer != null) {
            fPinnedContextViewer.dispose();
            fPinnedContextViewer = null;
        }
        fComposite.layout(true,  true);
    }

	public boolean isPinned() {
	    return fPinnedContextViewer != null;
	}
	
    public String getPinnedFactoryId() {
    	if (fPinnedContextViewer != null) {
    		return fPinnedContextViewer.getFactoryId();
    	}
    	return null;
    }
	
	public final void createPartControl(Composite parent) {
	    setPartTitle();
	    final IToolBarManager tbm= getViewSite().getActionBars().getToolBarManager();
	    configureToolBar(tbm);

        fComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginTop = layout.marginHeight = layout.marginLeft= layout.marginRight = layout.marginBottom = layout.marginWidth = layout.verticalSpacing = 0;   
        fComposite.setLayout(layout);
        
        Composite viewerComposite = SWTFactory.createComposite(fComposite, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
        viewerComposite.setLayout(new FillLayout());
        doCreatePartControl(viewerComposite);
	}
	
	/**
	 * Creates the SWT control for the pinnable view.
	 * @param parent Parent for the view control.
	 */
	abstract protected void doCreatePartControl(Composite parent);
	
	public void dispose() {
	    clearPin();
		super.dispose();
	}
	
    /**
     * Initialize the pinned context viewer state from the given memento. 
     * @param memento
     */
    public void initPinnedContextViewerState(IMemento memento) {
    	String secondaryId = NewViewInstanceAction.decodeClonedPartSecondaryId(((IViewSite)getSite()).getSecondaryId());
    	String factoryId = memento.getString(secondaryId + "." + PINNED_CONTEXT_VIEWER_ID); //$NON-NLS-1$
    	if (factoryId != null) {
    		IPinnedContextFactory factory = 
    				DebugUITools.getDebugContextManager().getPinnedContextViewerFactory(factoryId);
    		if (factory != null ) {
	    		pin(factory);
	        	if (fPinnedContextViewer != null) {
	        		fPinnedContextViewer.restorePinnedContext(memento);
	        	}
    		}
    	}
    }
    
	/**
	 * Saves the current state of the viewer
	 * @param memento the memento to write the viewer state into
	 */
	public void savePinnedContextViewerState(IMemento memento) {
		if (fPinnedContextViewer != null) {
			memento.putString(PINNED_CONTEXT_VIEWER_ID, fPinnedContextViewer.getFactoryId());
			fPinnedContextViewer.savePinnedContext(memento);
		}
	}

	
	/**
	 * Configures the view toolbar actions.
	 * @param tbm
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(new Separator(IDebugUIConstants.NAVIGATION_GROUP));
		tbm.add(new PinViewToContextDropDownAction(this));
		tbm.add(new NewViewInstanceAction(this)); 
	}
	    
	/**
	 * Set the part title to include the secondary id as part of the title.
	 * 
	 * @param part the view part
	 */
	private void setPartTitle() {
		if (!NewViewInstanceAction.isClonedPart(this))
			return;
		
		String secondaryId = getViewSite().getSecondaryId();
		secondaryId = NewViewInstanceAction.decodeClonedPartSecondaryId(secondaryId);			
		
		// use reflection to set the part name of the new view
		String name = getPartName();
	
		String tag = " <" + secondaryId + ">";   //$NON-NLS-1$//$NON-NLS-2$
		if (name.indexOf(tag) < 0) {
			name = name + tag;
			setPartName(name);
		}
	}
}
