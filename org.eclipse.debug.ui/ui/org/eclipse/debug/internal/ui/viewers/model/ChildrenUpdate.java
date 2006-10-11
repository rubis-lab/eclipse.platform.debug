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

import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.jface.viewers.TreePath;

/**
 * @since 3.3 
 */
class ChildrenUpdate extends ViewerUpdateMonitor implements IChildrenUpdate {
	
	private Object fParent;
	private TreePath fParentPath;
	private Object[] fElements;
	private int fIndex;
	private int fLength;
	private IElementContentProvider fContentProvider;
	private boolean fStarted = false;

	/**
	 * Constructs a request to update an element
	 * 
	 * @param node node to update
	 * @param model model containing the node
	 */
	public ChildrenUpdate(ModelContentProvider provider, Object parent, TreePath parentPath, int index, IElementContentProvider presentation) {
		super(provider);
		fParentPath = parentPath;
		fIndex = index;
		fLength = 1;
		fContentProvider = presentation;
		fParent = parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.AsynchronousRequestMonitor#performUpdate()
	 */
	protected void performUpdate() {
		TreeModelContentProvider provider = (TreeModelContentProvider) getContentProvider();
		if (fElements != null) {
			InternalTreeModelViewer viewer = (InternalTreeModelViewer) provider.getViewer();
			for (int i = 0; i < fElements.length; i++) {
				int modelIndex = fIndex + i;
				Object element = fElements[i];
				if (element != null) {
					int viewIndex = provider.modelToViewIndex(fParentPath, modelIndex);
					if (provider.shouldFilter(fParentPath, element)) {
						if (provider.addFilteredIndex(fParentPath, modelIndex)) {
							//System.out.println("REMOVE(" + fParent + ", modelIndex: " + modelIndex + " viewIndex: " + viewIndex + ", " + element + ")");
							viewer.remove(fParent, viewIndex);
						}
					} else {
						if (provider.isFiltered(fParentPath, modelIndex)) {
							provider.clearFilteredChild(fParentPath, modelIndex);
							int insertIndex = provider.modelToViewIndex(fParentPath, modelIndex);
							//System.out.println("insert(" + fParentPath.getLastSegment() + ", modelIndex: " + modelIndex + " insertIndex: " + insertIndex + ", " + element + ")");
							if (fParentPath.getSegmentCount() == 0) {
								viewer.insert(fParent, element, insertIndex);
							} else {
								viewer.insert(fParentPath, element, insertIndex);
							}
						} else {
							//System.out.println("replace(" + fParent + ", modelIndex: " + modelIndex + " viewIndex: " + viewIndex + ", " + element + ")");
							viewer.replace(fParent, viewIndex, element);
						}
						provider.updateChildCount(element, 0);
					}
				}
			}
		} else {
			provider.updateChildCount(fParent, 0);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate#setChild(java.lang.Object, int)
	 */
	public void setChild(Object child, int index) {
		if (fElements == null) {
			fElements = new Object[fLength];
		}
		fElements[index - fIndex] = child;
	}
	
	/**
	 * Coalesce the request with the given index. Return whether the requests can be 
	 * coalesced.
	 * 
	 * @param index
	 * @return whether it worked
	 */
	public boolean coalesce(int index) {
		if (index == fIndex + fLength) {
			fLength++;
			return true;
		}
		return false;
	}
	
	public void start() {
		synchronized (this) {
			if (fStarted) {
				return;
			}
			fStarted = true;
		}
		//System.out.println("\tRequest (" + fParent + "): " + fIndex + " length: " + fLength);
		TreeModelContentProvider contentProvider = (TreeModelContentProvider)getContentProvider();
		contentProvider.childRequestStarted(this);
		fContentProvider.update(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate#getLength()
	 */
	public int getLength() {
		return fLength;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate#getOffset()
	 */
	public int getOffset() {
		return fIndex;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate#getParent()
	 */
	public Object getParent() {
		return fParent;
	}
	
}
