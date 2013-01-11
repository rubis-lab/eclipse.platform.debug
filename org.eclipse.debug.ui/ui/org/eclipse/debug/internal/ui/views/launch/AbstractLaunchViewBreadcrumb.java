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
package org.eclipse.debug.internal.ui.views.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.viewers.breadcrumb.AbstractBreadcrumb;
import org.eclipse.debug.internal.ui.viewers.breadcrumb.BreadcrumbViewer;
import org.eclipse.debug.internal.ui.viewers.breadcrumb.IBreadcrumbDropDownSite;
import org.eclipse.debug.internal.ui.viewers.breadcrumb.TreeViewerDropDown;
import org.eclipse.debug.internal.ui.viewers.model.IInternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.ILabelUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.ITreeModelContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.SubTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDeltaVisitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.ui.contexts.AbstractDebugContextProvider;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextProvider;
import org.eclipse.debug.ui.contexts.IDebugContextProvider2;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreePathContentProvider;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.UIJob;

/**
 * Base class shared by the breadcrumb in debug view compact mode and 
 * breadcrumb embedded into pinned data views.
 * <p>
 * The breadcrumb control only shows a single element at a time and it
 * needs a tree model viewer to track all the elements that are relevant in the
 * debug view.  The underlying tree viewer can be a jface viewer or a virtual
 * viewer.  The breadcrumb also has a debug context provider which uses the 
 * element shown in breadcrumb combined with the user selected cell of the 
 * breadcrumb to indicate the active debug context.
 * 
 * @since 3.9
 */
abstract public class AbstractLaunchViewBreadcrumb extends AbstractBreadcrumb implements IDebugContextListener, ILabelUpdateListener  {

	/**
	 * This class is used by the pin to context breadcrumb to represent 
	 * an element that is to be automatically re-selected when it becomes 
	 * available.  A placeholder is shown with a grayed out text and disabled icon.
	 * 
	 */
	protected static class Placeholder {
		public Placeholder(Object element, String label, Image image) {
			fElement = element;
			fLabel = label;
			fImage = image;
		}
		final Object fElement;
		final String fLabel;
		final Image fImage;
		
		public boolean equals(Object obj) {
			return obj instanceof Placeholder && fElement.equals(((Placeholder)obj).fElement);
		}
		
		public int hashCode() {
			return fElement.hashCode();
		}
	}
	
	/**
	 * Abstraction for the debug context path displayed in the breadcrumb.
	 */
    protected static class Input {
    	private static final Placeholder[] EMPTY_PLACEHOLDERS = new Placeholder[0];
    	
        final protected TreePath fPath;
        final protected Placeholder[] fPlaceholders;

        Input(TreePath path) {
            this(path, EMPTY_PLACEHOLDERS);
        }

        Input(TreePath path, Placeholder[] placeholders) {
            fPath = path;
            fPlaceholders = placeholders;
        }

        public boolean equals(Object obj) {
            return obj instanceof Input && 
                ((fPath == null && ((Input)obj).fPath == null) ||
                 (fPath != null && fPath.equals( ((Input)obj).fPath )));
        }
        
        public int hashCode() {
            return fPath == null ? 0 : fPath.hashCode();
        }
    }
    
    /**
     * Content provider for the breadcrumb.
     */
    private static class ContentProvider implements ITreePathContentProvider {

        private static final Object[] EMPTY_ELEMENTS_ARRAY = new Object[0];
        
        public Input fInput;  
        
        public Object[] getChildren(TreePath parentPath) {
            if (hasChildren(parentPath)) {
            	if (fInput.fPath.getSegmentCount() > parentPath.getSegmentCount()) {
            		return new Object[] { fInput.fPath.getSegment(parentPath.getSegmentCount()) };
            	} else {
            		int trailingIndex = parentPath.getSegmentCount() - fInput.fPath.getSegmentCount();
            		return new Placeholder[] { fInput.fPlaceholders[trailingIndex] };
            	}
            }
            return EMPTY_ELEMENTS_ARRAY;
        }

        public TreePath[] getParents(Object element) {
            // Not supported
            return new TreePath[] { TreePath.EMPTY };
        }

        public boolean hasChildren(TreePath parentPath) {
            if ( parentPath.getSegmentCount() == 0) {
                return fInput != null;
            } else if (fInput != null && 
                       fInput.fPath != null) 
            {
            	if (fInput.fPath.getSegmentCount() > parentPath.getSegmentCount()) 
            	{
	                for (int i = 0; i < parentPath.getSegmentCount(); i++) {
	                    if (i >= fInput.fPath.getSegmentCount()) {
	                        return false;
	                    } else {
	                        Object parentElement = parentPath.getSegment(i);
	                        Object contextElement = fInput.fPath.getSegment(i);
	                        if (!parentElement.equals(contextElement)) {
	                            return false;
	                        }
	                    }
	                }
	                return true;
            	} else if (fInput.fPlaceholders.length > 
            			   (parentPath.getSegmentCount() - fInput.fPath.getSegmentCount())) 
            	{ 
            		return true;
            	}
            }
            return false;
        }

        public Object[] getElements(Object inputElement) {
            if (fInput != null && 
                fInput.fPath != null) 
            {
                return getChildren(TreePath.EMPTY);
            } else {
                return new Object[] { fgEmptyDebugContextElement };
            }
        }

        public void dispose() {
            fInput = null;
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (newInput instanceof Input) {
                fInput = ((Input)newInput);
            } else {
                fInput = null;
            }
        }
    }
        
    /**
     * Label provider for the breadcrumb.  It retrieves the label for the 
     * corresponding path from the breadcrumb's underlying tree viewer.
     */
    private class LabelProvider extends BaseLabelProvider implements ITreePathLabelProvider {
    	
    	Display fDisplay;
    	
    	/**
    	 * Disabled font is standard font italicized.
    	 */
    	Font fDisabledFont;
    	
    	/**
    	 * Disabled color is background and foreground colors blended 50/50.
    	 */
    	Color fDisabledColor;
    	
    	/**
    	 * Cached of disabled icons.  Keys are the original icons, values are 
    	 * disabled icons.
    	 */
    	private Map fDisabledImageCache = new HashMap();
    	
    	LabelProvider(Composite parent) {
    		fDisplay = parent.getDisplay();
            FontData fontData = JFaceResources.getFontDescriptor(JFaceResources.DEFAULT_FONT).getFontData()[0];
            fontData.setStyle(SWT.ITALIC);
    		fDisabledFont = new Font(parent.getDisplay(), fontData);
    		RGB f = parent.getForeground().getRGB();
    		RGB b = parent.getBackground().getRGB();
    		fDisabledColor = new Color(parent.getDisplay(), new RGB((f.red + b.red)/2, (f.green + b.green)/2, (f.blue + b.blue)/2));
    	}
        
        public void updateLabel(ViewerLabel label, TreePath elementPath) {
            if (fgEmptyDebugContextElement.equals(elementPath.getLastSegment())) {
                label.setText(LaunchViewMessages.Breadcrumb_NoActiveContext);
                label.setImage(null);
                label.setFont(fDisabledFont);
                label.setForeground(fDisabledColor);
                label.setBackground(null);
            } else {
                ViewerLabel treeViewerLabel = fTreeViewer.getElementLabel(elementPath, null);
                if (treeViewerLabel != null) {
                	String text = treeViewerLabel.getText();
                	if (text == null) {
                		text = IInternalDebugCoreConstants.EMPTY_STRING;
                	}
                	label.setText(text);
                    label.setTooltipText(text);
                    label.setImage(treeViewerLabel.getImage());
                    label.setFont(treeViewerLabel.getFont());
                    label.setForeground(treeViewerLabel.getForeground());
                    label.setBackground(treeViewerLabel.getBackground());
                } else {
                	Placeholder placeholder = getPlaceholder(elementPath.getLastSegment());
                	if (placeholder != null) {
                		label.setText(placeholder.fLabel);
                		label.setImage(getDisabledImage(placeholder.fImage));
                	} else {
	                    label.setText(LaunchViewMessages.Breadcrumb_NoActiveContext);
	                    label.setImage(null);                	
                	}
                    label.setFont(fDisabledFont);
                    label.setForeground(fDisabledColor);
                }
            }            
        }
        
        Image getDisabledImage(Image image) {
        	if (image == null) return null;
        	Image disabledImage = (Image)fDisabledImageCache.get(image);
        	if (disabledImage == null) {
        		disabledImage = new Image(fDisplay, image, SWT.IMAGE_DISABLE);
        		fDisabledImageCache.put(image, disabledImage);
        	}
        	return disabledImage;
        }

        public void dispose() {
        	super.dispose();
        	fDisabledColor.dispose();
        	fDisabledFont.dispose();
        	for (Iterator itr = fDisabledImageCache.values().iterator(); itr.hasNext();) {
        		Image next = (Image)itr.next();
        		next.dispose();
        	}
        	fDisabledImageCache.clear();
        }
    }

    /**
     * Finds a placeholder for the given element object.  
     * @param element  Element to search for.
     * @return Placeholder object or <code>null</code> if not found.
     */
    protected Placeholder getPlaceholder(Object element) {
    	if (element == null) return null;
    	Object inputObject = getCurrentInput();
    	if (inputObject instanceof Input) {
    		Input input = (Input)inputObject;
	    	for (int i = 0; i < input.fPlaceholders.length; i++) {
	    		if (input.fPlaceholders[i].equals(element)) {
	    			return input.fPlaceholders[i];
	    		}
	    	}    	
    	}
    	return null;
    }
    
    /**
     * The workbench part that the breadcrumb is embedded in.
     */
    private final IWorkbenchPart fPart;
    
    /**
     * Underlying tree viewer that underlies the breadcrumb's element.  
     */
    private final IInternalTreeModelViewer fTreeViewer;
    
    /**
     * Debug context provider used to calculate the input into the breadcrumb.
     */
    private final IDebugContextProvider fTreeViewerContextProvider;
    
    /**
     * Element to show in breadrumb.
     */
    private Input fBreadcrumbInput;
    
    /**
     * Dummy element to show in breadcrumb when no debug context is set.
     */
    static final private Object fgEmptyDebugContextElement = new Object();
    
    private BreadcrumbViewer fViewer;
    
    /**
     * Internal flag indicating to refresh breadcrumb after changes in the 
     * tree viewer.
     */
    private boolean fRefreshBreadcrumb = false;
    
    /**
     * Breadcrumb's debug context provider.  This is the context provider that
     * other views listen to to access active selection in breadcrumb.
     */
    private class BreadcrumbContextProvider extends AbstractDebugContextProvider 
    	implements IDebugContextProvider2, IDebugContextListener, ISelectionChangedListener 
    {
        
        private ISelection fBreadcrumbSelection = null;
        
        BreadcrumbContextProvider() {
            super(fPart);
            fViewer.addSelectionChangedListener(this);
            fBreadcrumbSelection = fViewer.getSelection();
            fTreeViewerContextProvider.addDebugContextListener(this);
        }
        
        public boolean isWindowContextProvider() {
        	return AbstractLaunchViewBreadcrumb.this.isWindowContextProvider();
        }
        
        public ISelection getActiveContext() {
            if (fBreadcrumbSelection != null && !fBreadcrumbSelection.isEmpty()) {
                return fBreadcrumbSelection;
            } else {
                ISelection treeViewerSelection = fTreeViewerContextProvider.getActiveContext();
                return treeViewerSelection != null ? treeViewerSelection : StructuredSelection.EMPTY;
            }
        }
        
        void dispose() {
            fViewer.removeSelectionChangedListener(this);
            fTreeViewerContextProvider.removeDebugContextListener(this);
        }
        
        public void debugContextChanged(DebugContextEvent event) {
            fire(new DebugContextEvent(this, getActiveContext(), event.getFlags()));
        }
        
        public void selectionChanged(SelectionChangedEvent event) {
            ISelection oldContext = getActiveContext();
            fBreadcrumbSelection = event.getSelection();
            if (!getActiveContext().equals(oldContext)) {
                fire(new DebugContextEvent(this, getActiveContext(), DebugContextEvent.ACTIVATED));
            }
        }
    }

    private BreadcrumbContextProvider fBreadcrumbContextProvider;
    
    /**
     * Breadcrumb constructor.
     * 
     * @param view View that breadcrumb is contained in.
     * @param treeViewer Tree Model Viewer that provides the breadcrumb content.
     * @param contextProvider Debug context provider that drives the active 
     * breadcrumb selection.
     */
    public AbstractLaunchViewBreadcrumb(IWorkbenchPart view, IInternalTreeModelViewer treeViewer, IDebugContextProvider contextProvider) {
        fPart = view;
        fTreeViewer = treeViewer;
        fTreeViewer.addLabelUpdateListener(this);
        fTreeViewerContextProvider = contextProvider;
        fBreadcrumbInput = new Input( getPathForSelection(fTreeViewerContextProvider.getActiveContext()) );
        fTreeViewerContextProvider.addDebugContextListener(this);
    }

    protected IWorkbenchPart getPart() {
    	return fPart;
    }
    
    protected void setCurrentInput(Input input) {
    	fBreadcrumbInput = input;
    }
    
    protected BreadcrumbViewer getBreadcrumbViewer() {
    	return fViewer;
    }
    
    protected ITreeModelViewer getTreeModelViewer() {
    	return fTreeViewer;
    }
    
    protected IDebugContextProvider getTreeViewerContextProvider() {
    	return fTreeViewerContextProvider;
    }
    
    public IDebugContextProvider getBreadcrumbContextProvider() {
    	return fBreadcrumbContextProvider;
    }

    /**
     * Creates a placeholder object for the given element.  It saves the current
     * label information about the element into the placehodler, if available. 
     * @param element
     * @return
     */
    protected Placeholder makePlaceholder(Object element) {
    	ViewerLabel label = getBreadcrumbViewer().getElementLabel(element);
    	if (label != null) {
    		return new Placeholder(element, label.getText(), label.getImage());
    	} else {
        	return new Placeholder(element, LaunchViewMessages.AbstractLaunchViewBreadcrumb_unknownPlaceholder_label, null);
    	}
    }
    
    /**
     * Flag indicating whether the breadcrumb's debug context provider should
     * provide context for the whole window or just the containing part.
     * @return
     */
    abstract protected boolean isWindowContextProvider();

    /** 
     * @return Returns a flag indicating whether the breadcrumb should 
     * auto-expand the drop-down tree, or just show first level.
     */
    abstract protected boolean isBreadcrumbDropDownAutoexpand();

    /** 
     * Allows the breadcrumb to register its popup menu manager.
     */
    abstract protected void createMenuManager();
    
    /**
     * @return Indicates whether the breadcrumb is currently visible and should 
     * update itself.  In debug view the breadcrumb is only visible when in 
     * compact mode.
     */
    abstract protected boolean isBreadcrumbVisible();
    
    /**
     * 
     */
    protected void activateBreadcrumb() {
    }

    protected void deactivateBreadcrumb() {
        if (fViewer.isDropDownOpen()) {
            Shell shell = fViewer.getDropDownShell();
            if (shell != null && !shell.isDisposed()) {
                shell.close();
            }
        }
    }

    protected BreadcrumbViewer createViewer(Composite parent) {
        fViewer = new BreadcrumbViewer(parent, SWT.NONE) {
            protected Control createDropDown(Composite dropDownParent, IBreadcrumbDropDownSite site, TreePath path) {
                return createDropDownControl(dropDownParent, site, path);
            }
        };

        // Force the layout of the breadcrumb viewer so that we may calcualte 
        // its proper size.
        parent.pack(true);

        fViewer.setContentProvider(new ContentProvider());
        fViewer.setLabelProvider(new LabelProvider(parent));

        createMenuManager();
        
        fViewer.setInput(getCurrentInput());
        
        fBreadcrumbContextProvider = new BreadcrumbContextProvider();
        
        return fViewer;
    }

    protected Object getCurrentInput() {
        return fBreadcrumbInput;
    }

    protected boolean open(ISelection selection) {
        // Let the drop-down control implementation itself handle activating a new context.
        return false;
    }

    public void dispose() {
    	if (fBreadcrumbContextProvider != null) {
    		fBreadcrumbContextProvider.dispose();
    		fBreadcrumbContextProvider = null;
	        fTreeViewerContextProvider.removeDebugContextListener(this);
	        fTreeViewer.removeLabelUpdateListener(this);
	        fViewer = null;
    	}
        super.dispose();
    }

    public void debugContextChanged(DebugContextEvent event) {
        if (isBreadcrumbVisible()) {
            fBreadcrumbInput = makeInputForTreePath(getPathForSelection(event.getContext())); 
            if ((event.getFlags() & DebugContextEvent.ACTIVATED) != 0) {
                setInput(getCurrentInput());
                
                // If the context was activated, then clear the selection in breadcrumb
                // so that the activated context will become the active context for the 
                // window.
                fViewer.setSelection(StructuredSelection.EMPTY);
            } else {
                refresh();
            }
        }
    }
    
    protected Input makeInputForTreePath(TreePath path) {
    	return new Input(path);
    }
    
    public void labelUpdateStarted(ILabelUpdate update) {
    }
    
    public void labelUpdateComplete(ILabelUpdate update) {
        if (fBreadcrumbInput != null && fBreadcrumbInput.fPath != null) {
            if (fBreadcrumbInput.fPath.startsWith(update.getElementPath(), null)) {
                synchronized (this) {
                    fRefreshBreadcrumb = true;
                }
            }
        }
    }
    
    public void labelUpdatesBegin() {
    }
    
    public void labelUpdatesComplete() {
        boolean refresh = false;
        synchronized(this) {
            refresh = fRefreshBreadcrumb;
            fRefreshBreadcrumb = false;
        }
        if (isBreadcrumbVisible() && refresh) {
            new UIJob(fViewer.getControl().getDisplay(), "refresh breadcrumb") { //$NON-NLS-1$
                { setSystem(true); }
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    refresh();
                    return Status.OK_STATUS;
                }
            }.schedule();
        }
    }
    
    int getHeight() {
        return fViewer.getControl().getSize().y;
    }
    
    void clearSelection() {
        fViewer.setSelection(StructuredSelection.EMPTY);
    }
    
    protected TreePath getPathForSelection(ISelection selection) {
        if (selection instanceof ITreeSelection && !selection.isEmpty()) {
            return ((ITreeSelection)selection).getPaths()[0];
        }
        return null;
    }
    
    public Control createDropDownControl(Composite parent, final IBreadcrumbDropDownSite site, TreePath paramPath) {
        
        TreeViewerDropDown dropDownTreeViewer = new TreeViewerDropDown() {
            
            SubTreeModelViewer fDropDownViewer;
            
            protected TreeViewer createTreeViewer(Composite composite, int style, final TreePath path) {
                fDropDownViewer = new SubTreeModelViewer(
                    composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL | SWT.POP_UP, 
                    fTreeViewer.getPresentationContext());

                Object launchViewInput = fTreeViewer.getInput();
                fDropDownViewer.setInput(launchViewInput, path.getParentPath());

                ViewerFilter[] filters = fTreeViewer.getFilters();
                fDropDownViewer.setFilters(filters);
                // TODO: create filters based on LaunchView preferences
                
                ModelDelta stateDelta = new ModelDelta(launchViewInput, IModelDelta.NO_CHANGE);
                fTreeViewer.saveElementState(TreePath.EMPTY, stateDelta, IModelDelta.EXPAND | IModelDelta.SELECT);
                
                // If we do not want to expand the elements in the drop-down.
                // Prune the delta to only select the element in the 
                // top-most list.
                if (!isBreadcrumbDropDownAutoexpand()) {
                    final ModelDelta prunedDelta = new ModelDelta(launchViewInput, IModelDelta.NO_CHANGE);
                    stateDelta.accept(new IModelDeltaVisitor() {
                        ModelDelta copy = prunedDelta;
                        public boolean visit(IModelDelta delta, int depth) {
                            TreePath deltaPath = getViewerTreePath(delta);
                            if (deltaPath.getSegmentCount() == 0) {
                                // skip copying the root element, only copy it's child count
                                copy.setChildCount(delta.getChildCount());
                            } else if (deltaPath.getSegmentCount() != 0 && path.startsWith(deltaPath, null) ) {
                                // Build up the delta copy along the path of the drop-down element.
                                copy = copy.addNode(
                                    delta.getElement(), delta.getIndex(), delta.getFlags(), delta.getChildCount());
                            } 
                            
                            // If the delta is for the drop-down element, set its select flag and stop traversing 
                            // the delta..
                            if (deltaPath.equals(path)) {
                                copy.setFlags(IModelDelta.SELECT | IModelDelta.REVEAL);
                                return false;
                            }
                            
                            // Continue traversing the delta.
                            return true;
                        }
                        
                        private TreePath getViewerTreePath(IModelDelta node) {
                            ArrayList list = new ArrayList();
                            IModelDelta parentDelta = node.getParentDelta();
                            while (parentDelta != null) {
                                list.add(0, node.getElement());
                                node = parentDelta;
                                parentDelta = node.getParentDelta();
                            }
                            return new TreePath(list.toArray());
                        }
                    });
                    stateDelta = prunedDelta;
                }
                
                fDropDownViewer.updateViewer(stateDelta);
                
                fDropDownViewer.addLabelUpdateListener(new ILabelUpdateListener() {
                    public void labelUpdateComplete(ILabelUpdate update) {}
                    public void labelUpdatesBegin() {}
                    public void labelUpdateStarted(ILabelUpdate update) {}
                    public void labelUpdatesComplete() {
                        new UIJob(fViewer.getControl().getDisplay(), "resize breadcrub dropdown") { //$NON-NLS-1$
                            { setSystem(true); }
                            public IStatus runInUIThread(IProgressMonitor monitor) {
                                site.updateSize();
                                return Status.OK_STATUS;
                            }
                        }.schedule();
                    }
                });

                return fDropDownViewer;
            }

            protected void openElement(ISelection selection) {
                if (fTreeViewer.isDisposed()) {
                    return;
                }
                
                if (selection != null && (selection instanceof ITreeSelection) && !selection.isEmpty()) {
                    // Create the path to the root element of the drop-down viewer.  Need to calcualte
                    // indexes and counts for the delta in order for the selection from the drop-down 
                    // viewer to work properly.
                    ITreeModelContentProvider contentProvider = fTreeViewer.getTreeModelContentProvider();
                    TreePath path = TreePath.EMPTY;
                    int count = fTreeViewer.getChildCount(path);
                    count = contentProvider.viewToModelCount(path, count);
                    ModelDelta rootDelta = 
                        new ModelDelta(fTreeViewer.getInput(), -1, IModelDelta.NO_CHANGE, count);
                    TreePath rootPath = fDropDownViewer.getRootPath();
                    ModelDelta delta = rootDelta;
                    for (int i = 0; i < rootPath.getSegmentCount(); i++) {
                        Object element = rootPath.getSegment(i);
                        int index = fTreeViewer.findElementIndex(path, element);
                        index = contentProvider.viewToModelIndex(path, index);
                        path = path.createChildPath(element);
                        count = fTreeViewer.getChildCount(path);
                        count = contentProvider.viewToModelCount(path, count);
                        delta = delta.addNode(rootPath.getSegment(i), index, IModelDelta.NO_CHANGE, count);
                    }
                    
                    // Create the delta and save the drop-down viewer's state to it.
                    fDropDownViewer.saveElementState(TreePath.EMPTY, delta, IModelDelta.EXPAND | IModelDelta.SELECT);
                    
                    // Add the IModelDelta.FORCE flag to override the current selection in view.
                    rootDelta.accept(new IModelDeltaVisitor(){
                        public boolean visit(IModelDelta paramDelta, int depth) {
                            if ((paramDelta.getFlags() & IModelDelta.SELECT) != 0) {
                                ((ModelDelta)paramDelta).setFlags(paramDelta.getFlags() | IModelDelta.FORCE);
                            }
                            return true;
                        }
                    });

                    // If elements in the drop-down were auto-expanded, then collapse the drop-down's sub tree in the 
                    // full viewer.  After the drop-down's full expansion state is saved out to the tree viewer, the
                    // tree viewer will accurately reflect the state changes made by the user. 
                    if (isBreadcrumbDropDownAutoexpand()) {
                        fTreeViewer.collapseToLevel(rootPath, AbstractTreeViewer.ALL_LEVELS);
                    }                    
                    
                    // Save the state of the drop-down out into the tree viewer.
                    fTreeViewer.updateViewer(rootDelta);
                    fViewer.setSelection(StructuredSelection.EMPTY);
                    site.close();
                }
                    
                super.openElement(selection);
            }
        };
        

        return dropDownTreeViewer.createDropDown(parent, site, paramPath);
    }
}
