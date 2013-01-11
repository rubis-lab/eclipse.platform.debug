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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDeltaVisitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ITreeModelViewer;
import org.eclipse.debug.ui.contexts.AbstractDebugContextProvider;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.UIJob;

/**
 * Debug context provider based on the tree model viewer.
 * <p>
 * This debug context provider monitors the selection and event in a tree model 
 * viewer and updates the active debug context accordingly.
 * 
 * @since 3.9
 */
public class TreeViewerContextProvider extends AbstractDebugContextProvider implements IModelChangedListener, ISelectionChangedListener {
	
	private ISelection fContext = null;
	private ITreeModelViewer fViewer = null;
	private Visitor fVisitor = new Visitor();
	
	class Visitor implements IModelDeltaVisitor {
		public boolean visit(IModelDelta delta, int depth) {
			if ((delta.getFlags() & (IModelDelta.STATE | IModelDelta.CONTENT)) > 0) {
				// state and/or content change
				if ((delta.getFlags() & IModelDelta.SELECT) == 0) {
					// no select flag
					if ((delta.getFlags() & IModelDelta.CONTENT) > 0) {
						// content has changed without select >> possible re-activation
						possibleChange(getViewerTreePath(delta), DebugContextEvent.ACTIVATED);
					} else if ((delta.getFlags() & IModelDelta.STATE) > 0) {
						// state has changed without select >> possible state change of active context
						possibleChange(getViewerTreePath(delta), DebugContextEvent.STATE);
					}
				}
			}
			return true;
		}	
	}
	
	/**
	 * Returns a tree path for the node, *not* including the root element.
	 * 
	 * @param node
	 *            model delta
	 * @return corresponding tree path
	 */
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
	
	public TreeViewerContextProvider(IWorkbenchPart part, ITreeModelViewer viewer) {
		super(part);
		fViewer = viewer;
		fViewer.addModelChangedListener(this);
	}
	
	public void dispose() { 
		fContext = null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextProvider#getActiveContext()
	 */
	public synchronized ISelection getActiveContext() {
		return fContext;
	}	
	
	public void activate(ISelection selection) {
		synchronized (this) {
			fContext = selection;
		}
		fire(new DebugContextEvent(this, selection, DebugContextEvent.ACTIVATED));
	}
	
    protected void possibleChange(TreePath element, int type) {
        DebugContextEvent event = null;
        synchronized (this) {
            if (fContext instanceof ITreeSelection) {
                ITreeSelection ss = (ITreeSelection) fContext;
                TreePath[] ssPaths = ss.getPaths(); 
                for (int i = 0; i < ssPaths.length; i++) {
                    if (ssPaths[i].startsWith(element, null)) {
                        if (ssPaths[i].getSegmentCount() == element.getSegmentCount()) {
                            event = new DebugContextEvent(this, fContext, type);
                        } else {
                            // if parent of the currently selected element 
                            // changes, issue event to update STATE only
                            event = new DebugContextEvent(this, fContext, DebugContextEvent.STATE);
						}
					}
				}
			} 
		}
		if (event == null) {
			return;
		}
		
		if (getPart().getSite().getShell().getDisplay().getThread() == Thread.currentThread()) {
			fire(event);
		} else {
			final DebugContextEvent finalEvent = event;
			Job job = new UIJob("context change") { //$NON-NLS-1$
				public IStatus runInUIThread(IProgressMonitor monitor) {
					// verify selection is still the same context since job was scheduled
					synchronized (TreeViewerContextProvider.this) {
						if (fContext instanceof IStructuredSelection) {
							IStructuredSelection ss = (IStructuredSelection) fContext;
							Object changed = ((IStructuredSelection)finalEvent.getContext()).getFirstElement();
							if (!(ss.size() == 1 && ss.getFirstElement().equals(changed))) {
								return Status.OK_STATUS;
							}
						}
					}
					fire(finalEvent);
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.interTreeViewerContextProvidernal.ui.viewers.model.provisional.IModelChangedListener#modelChanged(org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta)
	 */
	public void modelChanged(IModelDelta delta, IModelProxy proxy) {
		delta.accept(fVisitor);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		if (event.getSelection() instanceof ITreeSelection) {
			TreePath[] paths  = ((ITreeSelection)event.getSelection()).getPaths();
			if (paths.length == 0) {
				activate(event.getSelection());
			} else {
				activate(new TreeSelection(paths[0]));
			}
		}
	}
}