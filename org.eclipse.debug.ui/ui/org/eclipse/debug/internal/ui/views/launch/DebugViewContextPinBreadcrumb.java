/*******************************************************************************
 * Copyright (c) 2009, 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.viewers.breadcrumb.IBreadcrumbDropDownSite;
import org.eclipse.debug.internal.ui.viewers.model.IInternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDeltaVisitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IVirtualItemValidator;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualItem;
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualTree;
import org.eclipse.debug.internal.ui.viewers.model.provisional.VirtualTreeModelViewer;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.debug.ui.contexts.IPinnablePart;
import org.eclipse.debug.ui.contexts.IPinnedContextViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Breadcrumb for embedding in debug data views, which shows the active debug 
 * context based on the debug view content.
 * <p>
 * This breadcrumb uses a <code>VirtualTreeModelViewer</code> to track the 
 * debug model elements using the same presentation context as the Debug view.
 * While the Debug view shows only the elements that are visible on the screen, 
 * the virtual viewer needs to have a strategy for determining which elements are
 * "visible".  The the pin control, the viewer makes visible only the selected element 
 * and its children.
 * </p>
 * <p>
 * The pin context breadcrumb will attempt to remember the pinned context in case 
 * the selected element is removed from viewer (by using <code>IElementMementoProvider</code>.  
 * In this case it will keep attempting to re-select the element that was originally 
 * pinned.
 * 
 * @since 3.9
 */
public class DebugViewContextPinBreadcrumb extends AbstractLaunchViewBreadcrumb implements IPinnedContextViewer {

	private static class DebugViewItemValidator implements IVirtualItemValidator {

		public boolean isItemVisible(VirtualItem item) {
            // First level of items is always visible, since the input 
            // element cannot be part of selection
            if (item.getParent() instanceof VirtualTree) {
                return true;
            }
            // Selected item, its parents and children of selected item are visible.
			VirtualItem[] selection = getTree(item).getSelection();
			for (int i = 0; i < selection.length; i++) {
                VirtualItem selectionItem = selection[i]; 
                VirtualItem[] selectedItemChildren = selectionItem.getItems(); 
                for (int j = 0; j< selectedItemChildren.length; j++) {
                    if (item.equals(selectedItemChildren[j])) {
                    	return true;
                    }
                }
                while (selectionItem != null) {
                    if (item.equals(selectionItem)) {
                        return true;
                    }
                    selectionItem = selectionItem.getParent();
                }
            }
            return false;
		}

		private VirtualTree getTree(VirtualItem item) {
			while (!(item instanceof VirtualTree)) {
				item = item.getParent();
			}
			return (VirtualTree)item;
		}
		
		public void showItem(VirtualItem item) {
			// No op
		}
	}
	
	private IPartListener fPartListener = new IPartListener() {
		public void partOpened(IWorkbenchPart part) {
			if (part instanceof LaunchView) {
				IInternalTreeModelViewer debugViewViewer = (IInternalTreeModelViewer)((LaunchView)part).getViewer();
				debugViewViewer.addViewerUpdateListener(fUpdateListener);
			}
		}
		
		public void partClosed(IWorkbenchPart part) {
			if (part instanceof LaunchView) {
				IInternalTreeModelViewer debugViewViewer = (IInternalTreeModelViewer)((LaunchView)part).getViewer();
				if (!debugViewViewer.isDisposed()) {
					debugViewViewer.removeViewerUpdateListener(fUpdateListener);
				}
			}
		}
		
		public void partActivated(IWorkbenchPart part) {}
		public void partBroughtToTop(IWorkbenchPart part) {}
		public void partDeactivated(IWorkbenchPart part) {}
	};

	private IViewerUpdateListener fUpdateListener = new IViewerUpdateListener() {
		public void updateComplete(org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate update) {};
		public void updateStarted(org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate update) {};
		public void viewerUpdatesBegin() {};
		public void viewerUpdatesComplete() {
			((VirtualTreeModelViewer)getTreeModelViewer()).getTree().validate();
		}
	};
	
    protected boolean isBreadcrumbAutoexpand() {
    	return true;
    }
    
    protected boolean isWindowContextProvider() {
    	return false;
    }
    
    public DebugViewContextPinBreadcrumb(IPinnablePart part) {
    	this(part, new DebugViewItemValidator());
    }

    private DebugViewContextPinBreadcrumb(IPinnablePart part, DebugViewItemValidator validator) {
    	this(part, validator, createVirtualTreeViewer(validator, part));
    }
    private DebugViewContextPinBreadcrumb(IPinnablePart part, DebugViewItemValidator validator, VirtualTreeModelViewer viewer) {
    	this(part, validator, viewer, new TreeViewerContextProvider(part, viewer));
    }
    
    private DebugViewContextPinBreadcrumb(IPinnablePart part, DebugViewItemValidator validator, VirtualTreeModelViewer viewer, TreeViewerContextProvider contextProvider) {
    	super(part, viewer, contextProvider);
		IWorkbenchPage page = part.getSite().getPage();
		LaunchView debugView = (LaunchView)page.findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if (debugView != null) {
			IInternalTreeModelViewer debugViewViewer = (IInternalTreeModelViewer)debugView.getViewer();
			debugViewViewer.addViewerUpdateListener(fUpdateListener);
			//getTreeModelViewer().setFilters( debugViewViewer.getFilters() );
		}
		part.getSite().getPage().addPartListener(fPartListener);
    }
    
    private VirtualTreeModelViewer getVirtualTreeModelViewer() {
    	return (VirtualTreeModelViewer)getTreeModelViewer();
    }
    public String getFactoryId() {
    	return IDebugUIConstants.ID_DEBUG_VIEW;
    }
    
    public Control createControl(Composite parent) {
    	return createContent(parent);
    }
    
    public void savePinnedContext(IMemento memento) {
    	getVirtualTreeModelViewer().saveState(memento);
    	// TODO: save active selection in breadcrumb
    }
    
    public void restorePinnedContext(IMemento memento) {
    	getVirtualTreeModelViewer().initState(memento);
    	getVirtualTreeModelViewer().restoreViewerState();
    	getVirtualTreeModelViewer().refresh();
    }
    
    private static VirtualTreeModelViewer createVirtualTreeViewer(IVirtualItemValidator validator, IWorkbenchPart part) {
        ITreeModelViewer debugViewViewer = getDebugViewViewer(part);
        IPresentationContext presContext= null;
        if (debugViewViewer != null) {
        	presContext = debugViewViewer.getPresentationContext();
        } else {
        	presContext = new PresentationContext(IDebugUIConstants.ID_DEBUG_VIEW);
        }
        
        VirtualTreeModelViewer viewer  = new VirtualTreeModelViewer(part.getSite().getShell().getDisplay(), SWT.VIRTUAL,  
                presContext, validator, ~IModelDelta.SELECT);
        viewer.setInput(DebugPlugin.getDefault().getLaunchManager());
        return viewer;
    }
    
    public void debugContextChanged(DebugContextEvent event) {
    	super.debugContextChanged(event);
		((VirtualTreeModelViewer)getTreeModelViewer()).getTree().validate();
    }
    
    protected Input makeInputForTreePath(TreePath path) {
    	if (path == null) path = TreePath.EMPTY;
    	Input currentInput = (Input)getCurrentInput();
    	TreePath currentPath = currentInput.fPath;
    	if (currentPath != null && path != null && currentPath.startsWith(path, null) && !currentPath.equals(path)) {
    		int newTrailingElementsCount = currentPath.getSegmentCount() - path.getSegmentCount();
    		Placeholder[] placeholders = new Placeholder[newTrailingElementsCount + currentInput.fPlaceholders.length];
    		for (int i = 0; i < newTrailingElementsCount; i++ ) {
    			placeholders[i] = makePlaceholder( currentPath.getSegment(path.getSegmentCount() + i) );
    		}
    		
    		for (int i = 0; i < currentInput.fPlaceholders.length; i++) {
    			placeholders[newTrailingElementsCount + i] = currentInput.fPlaceholders[i];
    		}
    		return new Input(path, placeholders);
    	}
    	else {
    		return new Input(path);
    	}
    }
    
    public Control createContent(Composite parent) {
    	Control control = super.createContent(parent);
    	initializeSelection();
    	return control;
    }

    public Control createDropDownControl(Composite parent, final IBreadcrumbDropDownSite site, TreePath paramPath) {
    	if (getPlaceholder(paramPath.getLastSegment()) != null) {
    		// Do not open a drop down for placeholder elements
    		return null;
    	}
    	
    	return super.createDropDownControl(parent, site, paramPath);
    }
    
    private static ITreeModelViewer getDebugViewViewer(IWorkbenchPart part) {
		LaunchView launchView = (LaunchView)part.getSite().getPage().findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if (launchView != null) {
			return (ITreeModelViewer)launchView.getViewer();
		}
		return null;
    }
    
    protected boolean isBreadcrumbVisible() {
    	return true;
    }
    
    protected void createMenuManager() {
    }
    
    protected boolean isBreadcrumbDropDownAutoexpand() {
    	return true;
    }
    
    private void initializeSelection() {
		ITreeModelViewer debugViewViwer = getDebugViewViewer(getPart());
		if (debugViewViwer != null) {
			ModelDelta delta = new ModelDelta(debugViewViwer.getInput(), IModelDelta.NO_CHANGE);
			debugViewViwer.saveElementState(TreePath.EMPTY, delta, IModelDelta.SELECT);
			delta.accept(new IModelDeltaVisitor() {
				public boolean visit(IModelDelta visistDelta, int depth) {
					if ((visistDelta.getFlags() & IModelDelta.SELECT) == 0) {
						((ModelDelta)visistDelta).setFlags(visistDelta.getFlags() | IModelDelta.EXPAND);
					}
					return true;
				}
			});
	    	getTreeModelViewer().updateViewer(delta);
	    	ISelection selection = getTreeModelViewer().getSelection();
	        setCurrentInput( new Input(getPathForSelection(selection)) );
	        setInput(getCurrentInput());
	        ((TreeViewerContextProvider)getTreeViewerContextProvider()).activate(selection);
	        getVirtualTreeModelViewer().preserveViewerState( IModelDelta.SELECT | IModelDelta.REVEAL | IModelDelta.FORCE, false);
	        getVirtualTreeModelViewer().restoreViewerState();
		}
        getTreeModelViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				((TreeViewerContextProvider)getTreeViewerContextProvider()).activate(event.getSelection());
			}
		});
		IDebugContextService service = DebugUITools.getDebugContextManager().getContextService( 
				getPart().getSite().getWorkbenchWindow() );
		service.addDebugContextProvider(getBreadcrumbContextProvider());
		
    }
    
    public void dispose() {
    	if (isDisposed()) return;
		IDebugContextService service = DebugUITools.getDebugContextManager().getContextService( 
				getPart().getSite().getWorkbenchWindow() );
		service.removeDebugContextProvider(getBreadcrumbContextProvider());
    	super.dispose();
    	((TreeViewerContextProvider)getTreeViewerContextProvider()).dispose();
    	getVirtualTreeModelViewer().dispose();
    }

    protected boolean open(ISelection selection) {
        // User selected new input into the view.  The drop down already set the new 
        // selection to the virtual viewer, which triggered the breadcrumb input to be
        // recreated.  Now remove any placeholder elements in the input.
        Input currentInput = (Input)getCurrentInput();
        if (currentInput.fPlaceholders.length != 0) {
            Input newInput = new Input(currentInput.fPath);
            setInput(newInput);
        }
        // Save the state of the viewer and make the viewer restore it.  Thereafter, 
        // if the viewer selection is lost the viewer will attempt to re-select it. 
    	getVirtualTreeModelViewer().preserveViewerState( IModelDelta.SELECT | IModelDelta.REVEAL | IModelDelta.FORCE, false);
    	getVirtualTreeModelViewer().restoreViewerState();
    	return super.open(selection);
    }    
}

