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
import org.eclipse.debug.ui.AbstractDebugView;
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

/**
 * Common debug view extension to support pinning to debug context. It 
 * implements creating and clearing the pin control.  Also, adds the pin and 
 * clone actions to the view's tool-bar. 
 * 
 * @since 3.9
 */
abstract public class AbstractPinnableDebugView extends AbstractDebugView implements IPinnablePart {

	private static final String PINNED_CONTEXT_VIEWER_ID = DebugUIPlugin.getUniqueIdentifier() + ".PINNED_CONTEXT_VIEWER_ID"; //$NON-NLS-1$
	
    private Composite fComposite;
    private IPinnedContextViewer fPinnedContextViewer;

    public void pinToProvider(IPinnableDebugContextProvider provider) {
    	pinToFactory(provider.getFactoryId());
    }

    private void pinToFactory(String factoryId) {
        if (fPinnedContextViewer != null) {
            fPinnedContextViewer.dispose();
        }
        IPinnedContextViewerFactory factory = DebugUITools.getDebugContextManager().getPinnedContextViewerFactory( factoryId );
        if (factory != null) {
	        fPinnedContextViewer = factory.createPinnedContextViewer(this);
	        Control control = fPinnedContextViewer.createControl(fComposite);
	        control.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        control.moveAbove(null);
	        fComposite.layout(true,  true);
        }    	
    }
    
    public void clearPinnedProvider() {
        if (fPinnedContextViewer != null) {
            fPinnedContextViewer.dispose();
            fPinnedContextViewer = null;
        }
        if (fComposite != null && !fComposite.isDisposed()) {
        	fComposite.layout(true,  true);
        }
    }

    public boolean isPinned() {
        return fPinnedContextViewer != null;
    }

    public final void createPartControl(Composite parent) {
        setPartTitle();
        fComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginTop = layout.marginHeight = layout.marginLeft= layout.marginRight = layout.marginBottom = layout.marginWidth = layout.verticalSpacing = 0;   
        fComposite.setLayout(layout);
        
        Composite viewerComposite = SWTFactory.createComposite(fComposite, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
        viewerComposite.setLayout(new FillLayout());
        super.createPartControl(viewerComposite);
    }
	
    public void dispose() {
        NewViewInstanceAction.recycleCounterId(this);
        clearPinnedProvider();
        super.dispose();
    }

    /**
     * Initialize the pinned context viewer state from the given memento. 
     * @param memento
     */
    public void initPinnedContextViewerState(IMemento memento) {
    	if (memento != null) {
	    	String factoryId = memento.getString(PINNED_CONTEXT_VIEWER_ID); 
	    	if (factoryId != null) {
	    		pinToFactory(factoryId);
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
	
    protected void createActions() {
        setAction("PinViewToContext", new PinViewToContextAction(this)); //$NON-NLS-1$
        setAction("NewViewInstance", new NewViewInstanceAction(this)); //$NON-NLS-1$
    }
    
    protected void configureToolBar(IToolBarManager tbm) {
        tbm.add(new Separator(this.getClass().getName()));
        tbm.add(new Separator(IDebugUIConstants.NAVIGATION_GROUP));
        tbm.add(getAction("PinViewToContext")); //$NON-NLS-1$
        tbm.add(getAction("NewViewInstance")); //$NON-NLS-1$
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
