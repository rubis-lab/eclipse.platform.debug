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
package org.eclipse.debug.internal.ui.model.viewers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.debug.internal.ui.model.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.model.IElementContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * @since 3.3
 */
class ChildrenCountUpdate extends ViewerUpdateMonitor implements IChildrenCountUpdate {

	private Map fCounts;
	private Set fParents = new HashSet();
	private boolean fStarted = false;
	private IElementContentProvider fPresentation;
	
	/**
	 * @param contentProvider
	 */
	public ChildrenCountUpdate(ModelContentProvider contentProvider, IElementContentProvider presentation) {
		super(contentProvider);
		fPresentation = presentation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.viewers.ViewerUpdateMonitor#performUpdate()
	 */
	protected void performUpdate() {
		Iterator iterator = fCounts.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry entry = (Entry) iterator.next();
			int count = ((Integer)(entry.getValue())).intValue();
			Object parent = entry.getKey();
			TreePath[] treePaths = getContentProvider().getTreePaths(parent);
			int viewCount = count;
			if (treePaths.length > 0) {
				// all children are filtered the same per parent occurrence
				if (count == 0) {
					getContentProvider().clearFilters(treePaths[0]);
				} else {
					viewCount = getContentProvider().modelToViewChildCount(treePaths[0], count);
				}
			}
			//System.out.println("setChildCount(" + parent + ", modelCount: " + count + " viewCount: " + viewCount + ")");
			((TreeViewer)(getContentProvider().getViewer())).setChildCount(parent, viewCount);
			if (treePaths.length > 0) {
				if (treePaths[0].getSegmentCount() > 0) {
					getContentProvider().doRestore(treePaths[0]);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.IChildCountRequestMonitor#setChildCount(int)
	 */
	public void setChildCount(Object parent, int numChildren) {
		if (fCounts == null) {
			fCounts = new HashMap();
		}
		fCounts.put(parent, new Integer(numChildren));
	}

	/**
	 * @param element
	 * @return
	 */
	protected boolean coalesce(Object element) {
		fParents.add(element);
		return true;
	}

	/**
	 * 
	 */
	protected void start() {
		synchronized (this) {
			if (fStarted) {
				return;
			}
			fStarted = true;
		}
		TreeModelContentProvider contentProvider = (TreeModelContentProvider)getContentProvider();
		contentProvider.countRequestStarted(fPresentation);
		fPresentation.update(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.IChildrenCountUpdate#getParents()
	 */
	public Object[] getParents() {
		return fParents.toArray();
	}

}
