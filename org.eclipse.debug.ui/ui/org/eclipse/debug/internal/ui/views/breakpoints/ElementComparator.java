/*****************************************************************
 * Copyright (c) 2009 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Initial API and implementation (Bug 238956)
 *****************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import java.util.Comparator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.views.DebugModelPresentationContext;
import org.eclipse.debug.ui.breakpoints.IBreakpointContainer;

public class ElementComparator implements Comparator {
	final private static String SPACE = " "; //$NON-NLS-1$
	
	protected DebugModelPresentationContext fContext;
	
	public ElementComparator(IPresentationContext context) {
		if (context instanceof DebugModelPresentationContext)
			fContext = (DebugModelPresentationContext) context;
	}
	
	
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object arg0, Object arg1) {
		if (arg0 instanceof IBreakpoint & arg1 instanceof IBreakpoint) {
			return compare((IBreakpoint) arg0, (IBreakpoint) arg1);
		} if (arg0 instanceof IBreakpointContainer & arg1 instanceof IBreakpointContainer) {
			return compare((IBreakpointContainer) arg0, (IBreakpointContainer) arg1);
		} else {		
			return -1; // just return -1 if the two objects are not IBreakpoint type
		}
	}
    
	private int compare(IBreakpointContainer c1, IBreakpointContainer c2) {
		if (fContext != null) {
			String name1 = fContext.getModelPresentation().getText(c1);
			String name2 = fContext.getModelPresentation().getText(c2);
			
			return name1.compareTo(name2);
		}
		
		return -1;
	}	
	
	private int compare(IBreakpoint b1, IBreakpoint b2) {
		String text1 = IInternalDebugCoreConstants.EMPTY_STRING;
		String text2 = IInternalDebugCoreConstants.EMPTY_STRING;
		
		text1 += b1.getModelIdentifier();
		text2 += b2.getModelIdentifier();
		
		IMarker marker1 = b1.getMarker();
		IMarker marker2 = b2.getMarker();
		try {		
			if (marker1.exists() && marker2.exists()) {
				text1 += SPACE + marker1.getType();
				text2 += SPACE + marker2.getType();
			}
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}
	
		int result = text1.compareTo(text2);
		if (result != 0) {
			return result;
		}
	
		// model and type are the same	
		if (fContext != null) {
			String name1 = fContext.getModelPresentation().getText(b1);
			String name2 = fContext.getModelPresentation().getText(b2);
	
			boolean lineBreakpoint = false;
			try {
				lineBreakpoint = marker1.isSubtypeOf(IBreakpoint.LINE_BREAKPOINT_MARKER);
			} catch (CoreException ce) {
			}
			if (lineBreakpoint) {
				return compareLineBreakpoints(b1, b2, name1,name2);
			} 
			
			return name1.compareTo(name2);
		}
		
		return result;
	}
	
	private int compareLineBreakpoints(IBreakpoint b1, IBreakpoint b2, String name1, String name2) {
		int colon1 = name1.indexOf(':');
		if (colon1 != -1) {
			int colon2 = name2.indexOf(':');
			if (colon2 != -1) {
				String upToColon1 = name1.substring(0, colon1);
				if (name2.startsWith(upToColon1)) {
					int l1 = 0;
					int l2 = 0;
					try {
						l1 = ((ILineBreakpoint)b1).getLineNumber();	
					} catch (CoreException e) {
						DebugUIPlugin.log(e);
					}
					try {
						l2 = ((ILineBreakpoint)b2).getLineNumber();	
					} catch (CoreException e) {
						DebugUIPlugin.log(e);
					}
					return l1 - l2;
				}
			}
		}
		return name1.compareTo(name2);
	}     

}
