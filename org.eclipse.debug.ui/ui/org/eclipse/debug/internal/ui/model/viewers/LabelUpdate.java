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

import org.eclipse.debug.internal.ui.actions.context.AbstractRequestMonitor;
import org.eclipse.debug.internal.ui.model.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * @since 3.3
 */
class LabelUpdate extends AbstractRequestMonitor implements ILabelUpdate {
	
	private Object fElement;
	private String fColumnId;
	private RGB fBackground;
	private RGB fForeground;
	private ImageDescriptor fImageDescriptor;
	private String fLabel;
	private FontData fFontData;
	private TreeModelLabelProvider fProvider;
	private int fColumnIndex;
	
	/**
	 * @param element element the label is for
	 * @param provider label provider to callback to 
	 * @param columnId column identifier or <code>null</code>
	 */
	public LabelUpdate(Object element, TreeModelLabelProvider provider, String columnId, int columnIndex) {
		fElement = element;
		fProvider = provider;
		fColumnId = columnId;
		fColumnIndex = columnIndex;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.ILabelUpdate#getColumnId()
	 */
	public String getColumnId() {
		return fColumnId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.ILabelUpdate#getElement()
	 */
	public Object getElement() {
		return fElement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.ILabelUpdate#setBackground(org.eclipse.swt.graphics.RGB)
	 */
	public void setBackground(RGB background) {
		fBackground = background;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.ILabelUpdate#setFontData(org.eclipse.swt.graphics.FontData)
	 */
	public void setFontData(FontData fontData) {
		fFontData = fontData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.ILabelUpdate#setForeground(org.eclipse.swt.graphics.RGB)
	 */
	public void setForeground(RGB foreground) {
		fForeground = foreground;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.ILabelUpdate#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
	 */
	public void setImageDescriptor(ImageDescriptor image) {
		fImageDescriptor = image;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.ILabelUpdate#setLabel(java.lang.String)
	 */
	public void setLabel(String text) {
		fLabel = text;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.IPresentationUpdate#getPresentationContext()
	 */
	public IPresentationContext getPresentationContext() {
		return fProvider.getPresentationContext();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public void done() {
		fProvider.complete(this);
	}

	/**
	 * Applies settings to viewer cell
	 */
	public void update(ViewerRow row) {
		row.setText(fColumnIndex, fLabel);
		row.setImage(fColumnIndex, fProvider.getImage(fImageDescriptor));
		row.setForeground(fColumnIndex, fProvider.getColor(fForeground));
		row.setBackground(fColumnIndex, fProvider.getColor(fBackground));
		row.setFont(fColumnIndex, fProvider.getFont(fFontData));
	}
	
}
