/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Content provider for a virtual tree.
 * 
 * @since 3.3
 */
class TreeModelContentProvider extends ModelContentProvider implements ILazyTreeContentProvider {
	
	protected static final String[] STATE_PROPERTIES = new String[]{IBasicPropertyConstants.P_TEXT, IBasicPropertyConstants.P_IMAGE};
	
	private Map fPendingChildRequests = new HashMap();
	private Map fPendingCountRequests = new HashMap();
	
	private Timer fTimer = new Timer();
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		return null;
	}
	
	/**
	 * Re-filters any filtered children of the given parent element.
	 * 
	 * @param path parent element
	 */
	protected void refilterChildren(TreePath path) {
		if (getViewer() != null) {
			int[] filteredChildren = getFilteredChildren(path);
			if (filteredChildren != null) {
				Object parent = getViewer().getInput();
				if (path.getSegmentCount() > 0) {
					parent = path.getLastSegment();
				}
				for (int i = 0; i < filteredChildren.length; i++) {
					doUpdateElement(parent, path, filteredChildren[i]);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#updateChildCount(java.lang.Object, int)
	 */
	public synchronized void updateChildCount(Object element, int currentChildCount) {
		//System.out.println("updateChildCount(" + element + ", " + currentChildCount + ")");
		TreePath[] treePaths = getTreePaths(element);
		for (int i = 0; i < treePaths.length; i++) {
			// re-filter children when asked to update the child count for an element (i.e.
			// when refreshing, see if filtered children are still filtered)
			refilterChildren(treePaths[i]);
		}
		doUpdateChildCount(element, currentChildCount);
	}
	
	protected synchronized void doUpdateChildCount(Object element, int currentChildCount) {
		IElementContentProvider contentAdapter = getContentAdapter(element);
		if (contentAdapter != null) {
			ChildrenCountUpdate request = (ChildrenCountUpdate) fPendingCountRequests.get(contentAdapter);
			if (request != null) {
				if (request.coalesce(element)) {
					return;
				} else {
					request.start();
				}
			}
			final ChildrenCountUpdate newRequest = new ChildrenCountUpdate(this, contentAdapter);
			newRequest.coalesce(element);
			fPendingCountRequests.put(contentAdapter, newRequest);
			fTimer.schedule(new TimerTask() {
				public void run() {
					newRequest.start();
				}
			}, 10L);
		}
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#updateElement(java.lang.Object, int)
	 */
	public synchronized void updateElement(Object parent, int viewIndex) {
		//System.out.println("updateElement(" + parent + ", " + viewIndex + ")");
		TreePath[] paths = getTreePaths(parent);
		if (paths.length > 0) {
			TreePath path = paths[0]; // all children filter the same, per parent occurrence
			int modelIndex = viewToModelIndex(path, viewIndex);
			//System.out.println("updateElement("+ parent + ", " + viewIndex + ") > modelIndex = " + modelIndex);
			doUpdateElement(parent, path, modelIndex);
		}
	}
	
	protected synchronized void doUpdateElement(Object parent, TreePath parentPath, int modelIndex) {
		ChildrenUpdate request = (ChildrenUpdate) fPendingChildRequests.get(parent);
		if (request != null) {
			if (request.coalesce(modelIndex)) {
				return;
			} else {
				request.start();
			}
		} 
		IElementContentProvider contentAdapter = getContentAdapter(parent);
		if (contentAdapter != null) {
			final ChildrenUpdate newRequest = new ChildrenUpdate(this, parent, parentPath, modelIndex, contentAdapter);
			fPendingChildRequests.put(parent, newRequest);
			fTimer.schedule(new TimerTask() {
				public void run() {
					newRequest.start();
				}
			}, 10L);
		}			
	}	
	
	protected synchronized void childRequestStarted(IChildrenUpdate update) {
		fPendingChildRequests.remove(update.getParent());
	}
	
	protected synchronized void countRequestStarted(Object key) {
		fPendingCountRequests.remove(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#getPresentationContext()
	 */
	protected IPresentationContext getPresentationContext() {
		return ((TreeModelViewer)getViewer()).getPresentationContext();
	}
	
	/**
	 * Returns the tree viewer this content provider is working for
	 * 
	 * @return tree viewer
	 */
	protected TreeViewer getTreeViewer() {
		return (TreeViewer)getViewer();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleAdd(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleAdd(IModelDelta delta) {
		doUpdateChildCount(delta.getParentDelta().getElement(), 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleContent(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleContent(IModelDelta delta) {
		getTreeViewer().refresh(delta.getElement());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleExpand(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleExpand(IModelDelta delta) {
		// expand each parent, then this node
		IModelDelta parentDelta = delta.getParentDelta();
		if (parentDelta != null) {
			handleExpand(parentDelta);
			expand(delta);
		}
	}
	
	protected void expand(IModelDelta delta) {
		int childCount = delta.getChildCount();
		int index = delta.getIndex();
		TreeViewer treeViewer = getTreeViewer();
		if (index >= 0) {
			treeViewer.replace(delta.getParentDelta().getElement(), index, delta.getElement());
		}
		if (childCount > 0) {
			treeViewer.setChildCount(delta.getElement(), childCount);
			treeViewer.expandToLevel(getTreePath(delta), 1);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleInsert(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleInsert(IModelDelta delta) {
		// TODO: filters
		getTreeViewer().insert(getTreePath(delta.getParentDelta()), delta.getElement(), delta.getIndex());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleRemove(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleRemove(IModelDelta delta) {
		getTreeViewer().remove(getTreePath(delta));
		// refresh the parent to properly update for non-visible/unmapped children
		getTreeViewer().refresh(delta.getParentDelta().getElement());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleReplace(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleReplace(IModelDelta delta) {
		getTreeViewer().replace(delta.getParentDelta().getElement(), delta.getIndex(), delta.getElement());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleSelect(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleSelect(IModelDelta delta) {
		int index = delta.getIndex();
		TreeViewer treeViewer = getTreeViewer();
		if (index >= 0) {
			treeViewer.replace(delta.getParentDelta().getElement(), index, delta.getElement());
		}
		treeViewer.setSelection(new TreeSelection(getTreePath(delta)));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#handleState(org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta)
	 */
	protected void handleState(IModelDelta delta) {
		getTreeViewer().update(delta.getElement(), STATE_PROPERTIES);
	}

	public synchronized void dispose() {
		fTimer.cancel();
		fPendingChildRequests.clear();
		fPendingCountRequests.clear();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#buildViewerState(org.eclipse.debug.internal.ui.viewers.provisional.ModelDelta)
	 */
	protected void buildViewerState(ModelDelta delta) {
		Tree tree = (Tree) getViewer().getControl();
		TreeItem[] selection = tree.getSelection();
		Set set = new HashSet();
		for (int i = 0; i < selection.length; i++) {
			set.add(selection[i]);
		}
		TreeItem[] items = tree.getItems();
		for (int i = 0; i < items.length; i++) {
			buildViewerState(delta, items[i], set);
		}
	}

	/**
	 * @param delta parent delta to build on
	 * @param item item
	 * @param set set of selected tree items
	 */
	private void buildViewerState(ModelDelta delta, TreeItem item, Set set) {
		Object element = item.getData();
		if (element != null) {
			boolean expanded = item.getExpanded();
			boolean selected = set.contains(item);
			if (expanded || selected) {
				int flags = IModelDelta.NO_CHANGE;
				if (expanded) {
					flags = flags | IModelDelta.EXPAND;
				}
				if (selected) {
					flags = flags | IModelDelta.SELECT;
				}
				ModelDelta childDelta = delta.addNode(element, flags);
				if (expanded) {
					TreeItem[] items = item.getItems();
					for (int i = 0; i < items.length; i++) {
						buildViewerState(childDelta, items[i], set);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.viewers.ModelContentProvider#doInitialRestore()
	 */
	protected void doInitialRestore() {
		Tree tree = (Tree) getViewer().getControl();
		TreeItem[] items = tree.getItems();
		for (int i = 0; i < items.length; i++) {
			TreeItem item = items[i];
			Object data = item.getData();
			if (data != null) {
				doRestore(new TreePath(new Object[]{data}));
			}
		}
	}

}
