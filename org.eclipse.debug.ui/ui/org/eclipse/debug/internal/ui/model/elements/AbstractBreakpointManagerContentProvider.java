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
package org.eclipse.debug.internal.ui.model.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.elements.adapters.AbstractBreakpointManagerInput;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointContainer;
import org.eclipse.debug.internal.ui.views.breakpoints.ElementComparator;
import org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointFilterListener;
import org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointOrganizerListener;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.breakpoints.IBreakpointOrganizer;
import org.eclipse.jface.viewers.IStructuredSelection;

public abstract class AbstractBreakpointManagerContentProvider extends ElementContentProvider 
		implements IBreakpointFilterListener, IBreakpointOrganizerListener, IBreakpointsListener {
	
	/**
	 * Contains all input specific data.
	 */
	protected class InputData {
		AbstractBreakpointManagerInput fInput;
		AbstractModelProxy fProxy;				
		IStructuredSelection fSelectionFilter;
		ElementComparator fComparator;
		
		IBreakpointOrganizer[] fOrganizers;
		BreakpointContainer fContainer;
		
		InputData(AbstractBreakpointManagerInput input, AbstractModelProxy proxy, IStructuredSelection filter, ElementComparator comparator) {
			fInput = input;
			fProxy = proxy;
			fSelectionFilter = filter;
			fComparator = comparator;
		}
		
		/**
		 * Change the breakpoint organizers for the root container.
		 * 
		 * @param organizers the new organizers.
		 */
		synchronized void setOrganizers(IBreakpointOrganizer[] organizers) {
			fOrganizers = organizers;
			
			if (fContainer == null) {
				ModelDelta initialDelta = new ModelDelta(fInput, 0, IModelDelta.NO_CHANGE, -1);
				fContainer = createRootContainer(initialDelta, fInput, fOrganizers, fBpManager);
				initialDelta.setChildCount(fContainer.getChildren().length);
				
				// select the first breakpoint
				IBreakpoint[] breakpoints = fContainer.getBreakpoints();
				if (breakpoints.length > 0)
					appendModelDeltaToElement(initialDelta, breakpoints[0], IModelDelta.SELECT);
				
				fireModelChanged(fInput, initialDelta, "Organizer Changed - Initial Container."); //$NON-NLS-1$
			} else {
				// create a reference container, use for deleting elements and adding elements
				ModelDelta dummyDelta = new ModelDelta(null, IModelDelta.NO_CHANGE);				
				BreakpointContainer refContainer = createRootContainer(dummyDelta, fInput, organizers, fBpManager);

				// delete the removed elements
				ModelDelta deletedDelta = new ModelDelta(fInput, IModelDelta.NO_CHANGE);
				deleteRemovedElements(fContainer, refContainer, deletedDelta);
				fireModelChanged(fInput, deletedDelta, "setOrganizers - Delete removed elements"); //$NON-NLS-1$
				
				// adjust the old organizer with the reference organizer
				BreakpointContainer.copyOrganizers(fContainer, refContainer);
				
				// insert the added elements
				ModelDelta addedDelta = new ModelDelta(fInput, 0, IModelDelta.NO_CHANGE, -1);
				IBreakpoint newBreakpoint = insertAddedElements(fContainer, refContainer, addedDelta);
				addedDelta.setChildCount(fContainer.getChildren().length);
				
				// select the new breakpoint
				if (newBreakpoint != null)
					appendModelDeltaToElement(addedDelta, newBreakpoint, IModelDelta.SELECT);
				
				fireModelChanged(fInput, addedDelta, "setOrganizers - Insert added elements"); //$NON-NLS-1$
			}
		}

		/**
		 * Insert elements from the reference container to an existing container.
		 * 
		 * @param container the existing  container to insert the new elements.
		 * @param refContainer the reference container to compare elements that are added.
		 * @param containerDelta the delta of the existing container.
		 */
		private IBreakpoint insertAddedElements(BreakpointContainer container, BreakpointContainer refContainer, ModelDelta containerDelta) {
			IBreakpoint newBreakpoint = null;
			
			Object[] children = container.getChildren();
			Object[] refChildren = refContainer.getChildren();
			

			for (int i = 0; i < refChildren.length; ++i) {
				Object element = getElement(children, refChildren[i]);

				// if a child of refContainer doesn't exist in container, than insert it to container
				//		- if the reference child is a container, than copy the reference child container to container
				//		- otherwise (Breakpoint), add the breakpoint to container
				if (element == null) {
					if (refChildren[i] instanceof BreakpointContainer) {
						BreakpointContainer.addChildContainer(container, (BreakpointContainer) refChildren[i], containerDelta);
					} else {
						BreakpointContainer.addBreakpoint(container, (IBreakpoint) refChildren[i], containerDelta);
						if (newBreakpoint == null)
							newBreakpoint = (IBreakpoint) refChildren[i];
					}
					
				// if a child exist in container, than recursively search into container. And also update the organizer of
				// of container to the one in the refContainer's child.
				} else if (element instanceof BreakpointContainer) {
					int index = Arrays.asList(children).indexOf(element);  
					ModelDelta childDelta = containerDelta.addNode(element, index, IModelDelta.STATE, -1);
					BreakpointContainer.copyOrganizers((BreakpointContainer) element, (BreakpointContainer) refChildren[i]);
					newBreakpoint = insertAddedElements((BreakpointContainer) element, (BreakpointContainer) refChildren[i], childDelta);
					childDelta.setChildCount(((BreakpointContainer) element).getChildren().length);
				}
			}
			
			return newBreakpoint;
		}
		
		/**
		 * Delete elements from existing container that doesn't exist in the reference container.
		 * 
		 * @param container the existing container to delete the removed elements.
		 * @param refContainer the reference container to compare elements that are removed.
		 * @param containerDelta the delta of the existing container.
		 */
		private void deleteRemovedElements(BreakpointContainer container, BreakpointContainer refContainer, ModelDelta containerDelta) {
			Object[] children = container.getChildren();
			Object[] refChildren = refContainer.getChildren();
			
			// if a child of container doesn't exist in refContainer, than remove it from container
			for (int i = 0; i < children.length; ++i) {
				Object element = getElement(refChildren, children[i]);
				
				if (element == null) {
					if (children[i] instanceof BreakpointContainer) {
						BreakpointContainer.removeAll((BreakpointContainer) children[i], containerDelta);
					} else {
						BreakpointContainer.removeBreakpoint(container, (IBreakpoint) children[i], containerDelta);
					}
				} else if (element instanceof BreakpointContainer){

					ModelDelta childDelta = containerDelta.addNode(children[i], IModelDelta.STATE);						
					deleteRemovedElements((BreakpointContainer) children[i], (BreakpointContainer) element, childDelta);
				}
			}
		}
		
		/**
		 * Get the element that is in the collection.
		 * 
		 * @param collection the collection of elements.
		 * @param element the element to search.
		 * @return if element exist in collection, than it is returned, otherwise <code>null</code> is returned.
		 * @see insertAddedElements
		 * @see deleteRemovedElements
		 */
		private Object getElement(Object[] collection, Object element) {
			for (int i = 0; i < collection.length; ++i)
				if (collection[i] instanceof BreakpointContainer && element instanceof BreakpointContainer) {				
					if (collection[i].equals(element))
						return collection[i];
				} else {
					if (collection[i].equals(element))
						return collection[i];
				}
			return null;
		}
		
		/**
		 * Create a root container.
		 * 
		 * @param rootDelta the root delta.
		 * @param input the view input.
		 * @param organizers the breakpoint organizers.
		 * @param oldContainer the old container, use to determine whether a new breakpoint should be expanded.
		 * @param the breakpoint manager.
		 */
		private BreakpointContainer createRootContainer(ModelDelta rootDelta, AbstractBreakpointManagerInput input, 
				IBreakpointOrganizer[] organizers, IBreakpointManager bpManager) {
			
			IBreakpoint[] breakpoints = filterBreakpoints(input, bpManager.getBreakpoints());			
			BreakpointContainer container = new BreakpointContainer(organizers, fComparator);
			container.initDefaultContainers(rootDelta);
			
			for (int i = 0; i < breakpoints.length; ++i) {
				container.addBreakpoint(breakpoints[i], rootDelta);				
			}
			
			return container;
		}		
	}
	
	// debug flags
	public static boolean DEBUG_BREAKPOINT_DELTAS = false;
	
	static {
		DEBUG_BREAKPOINT_DELTAS = DebugUIPlugin.DEBUG && "true".equals( 					//$NON-NLS-1$
		 Platform.getDebugOption("org.eclipse.debug.ui/debug/viewers/breakpointDeltas")); 	//$NON-NLS-1$
	} 
		
	// a map of input to info data cache
	protected Map fInputToData;
	
	/**
	 * The breakpoint manager.
	 */
	protected IBreakpointManager fBpManager = null;
	
	/**
	 * Constructor.
	 */
	protected AbstractBreakpointManagerContentProvider() {
		fInputToData = new HashMap();		
		fBpManager = DebugPlugin.getDefault().getBreakpointManager();	
	}
	
	/**
	 * Sub-classes is required to implements this method to filter the breakpoints.
	 * 
	 * @param input the breakpoint manager input.
	 * @param breakpoints the list of breakpoint to filter.
	 * @return the filtered list of breakpoint based on the input.
	 */
	protected abstract IBreakpoint[] filterBreakpoints(AbstractBreakpointManagerInput input, IBreakpoint[] breakpoints);
	
	/**
	 * Determine whether the breakpoint is supported by the selection.
	 * 
	 * @param ss the selection of the debug elements.
	 * @param breakpoint the breakpoint.
	 * @return true if supported.
	 */
	protected abstract boolean supportsBreakpoint(IStructuredSelection ss, IBreakpoint breakpoint);
	
	/**
	 * Register the breakpoint manager input with this content provider.
	 * 
	 * @param input the breakpoint manager input to register.
	 * @param proxy the model proxy of the input.
	 * @param organizers the breakpoint organizer, can be <code>null</code>.
	 * @param selectionFilter the selection filter, can be <code>null</code>.
	 * @param comparator the element comparator.
	 */
	public void registerModelProxy(AbstractBreakpointManagerInput input, AbstractModelProxy proxy, 
			IBreakpointOrganizer[] organizers, IStructuredSelection selectionFilter, ElementComparator comparator) {
		
		if (fInputToData.size() == 0)
			fBpManager.addBreakpointListener(this);
		
		InputData data = new InputData(input, proxy, selectionFilter, comparator);
				
		// cache the input and it's data
		fInputToData.put(input, data);	
		
		data.setOrganizers(organizers);
	}
	
	/**
	 * Unregister the breakpoint manager input with this content provider.
	 * 
	 * @param input the breakpoint manager input to unregister.
	 */
	public void unregisterModelProxy(AbstractBreakpointManagerInput input) {
		InputData data = (InputData) fInputToData.remove(input);
		Assert.isNotNull(data);
		
		if (fInputToData.size() == 0)
			fBpManager.removeBreakpointListener(this);
	}	
	
	
	/**
	 * Returns the input data from the cache.
	 * 
	 * @param input the input.
	 * @return the input data cache, can be <code>null</code>.
	 */
	protected InputData getInputData(Object input) {
		return (InputData) fInputToData.get(input);
	}
	
	/**
	 * Returns the model proxy for the input.
	 * 
	 * @param input the input.
	 * @return the model proxy.
	 */
	public AbstractModelProxy getModelProxy(Object input) {
		InputData data = getInputData(input);
		return data != null ? data.fProxy : null;
	}	
	
	/**
	 * Returns the breakpoint container for the input.
	 * 
	 * @param input the input.
	 * @return the breakpoint container.
	 */
	public BreakpointContainer getBreakpointContainer(Object input) {
		InputData data = getInputData(input);
		return data != null ? data.fContainer : null;
	}
	
	/**
	 * Sets the input container.
	 */
	public void setBreakpointContainer(Object input, BreakpointContainer container) {
		InputData data = getInputData(input);
		if (data != null) {
			data.fContainer = container;
		}
	}
	
	/**
	 * Returns the selection filter for the input.
	 * 
	 * @param input the selection.
	 */
	public IStructuredSelection getSelectionFilter(Object input) {
		InputData data = getInputData(input);
		return data != null ? data.fSelectionFilter : null;
	}
	
	/**
	 * Fire model change event for the input.
	 * 
	 * @param input the input.
	 * @param delta the model delta.
	 * @param debugReason the debug string.
	 */
	public void fireModelChanged(Object input, IModelDelta delta, String debugReason) {
		AbstractModelProxy proxy = getModelProxy(input);
		if (proxy != null) {
			if (DEBUG_BREAKPOINT_DELTAS)
				System.out.println("FIRE BREAKPOINT DELTA (" + debugReason + ")\n" + delta.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				
			proxy.fireModelChanged(delta);				
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#supportsContextId(java.lang.String)
	 */
	protected boolean supportsContextId(String id) {
		return id.equals(IDebugUIConstants.ID_BREAKPOINT_VIEW);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getChildCount(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	protected int getChildCount(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		InputData data = getInputData(element);
		if (data != null && data.fContainer != null)
			return data.fContainer.getChildren().length;
		
		return 0;		
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementContentProvider#getChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate)
	 */
	protected Object[] getChildren(Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		InputData data = getInputData(parent);
		if (data != null && data.fContainer != null)
			return getElements(data.fContainer.getChildren(), index, length);
				
		return EMPTY;
	}	

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointOrganizerListener#setOrganizers(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.debug.internal.ui.views.breakpoints.IBreakpointOrganizer[])
	 */
	synchronized public void setOrganizers(Object input, IPresentationContext context, IBreakpointOrganizer[] organizers) {
		InputData data = getInputData(input);
		if (data != null)
			data.setOrganizers(organizers);		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.breakpoints.IBreakpointFilterContentProvider#setFilterSelection(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	synchronized public void setFilterSelection(Object input, IPresentationContext context, IStructuredSelection ss) {
		InputData data = getInputData(input);
		if (data == null) return;
		
		data.fSelectionFilter = ss;
		
		BreakpointContainer container = data.fContainer;
		if (container != null) {
			ModelDelta delta = new ModelDelta(input, IModelDelta.NO_CHANGE);
			
			Set existingBreakpoints = new HashSet(Arrays.asList(container.getBreakpoints()));
			IBreakpoint[] allBreakpoints = fBpManager.getBreakpoints();
			
			for (int i = 0; i < allBreakpoints.length; ++i) {
				boolean supported = supportsBreakpoint(ss, allBreakpoints[i]);
				boolean contain = existingBreakpoints.contains(allBreakpoints[i]);									
					
				if (supported) {
					if (!contain)
						container.addBreakpoint(allBreakpoints[i], delta);
				} else {
					if (contain)
						container.removeBreakpoint(allBreakpoints[i], delta);
				}
				
			}

			fireModelChanged(input, delta, "setFilterSelection"); //$NON-NLS-1$
		}
	}
	
	private void breakpointsAddedInput(InputData data, IBreakpoint[] breakpoints) {
		if (data == null || data.fContainer == null) {
			return;
		}
		IBreakpoint[] filteredBreakpoints = filterBreakpoints(data.fInput, breakpoints);
		ModelDelta delta = new ModelDelta(data.fInput, 0, IModelDelta.NO_CHANGE, -1);
		for (int i = 0; i < filteredBreakpoints.length; ++i) {
			data.fContainer.addBreakpoint(filteredBreakpoints[i], delta);
		}
		delta.setChildCount(data.fContainer.getChildren().length);
		
		// select the breakpoint
		if (filteredBreakpoints.length > 0) {
			appendModelDeltaToElement(delta, filteredBreakpoints[0], IModelDelta.SELECT);
		}
		
		fireModelChanged(data.fInput, delta, "breakpointsAddedInput"); //$NON-NLS-1$
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse.debug.core.model.IBreakpoint[])
	 */
	synchronized public void breakpointsAdded(IBreakpoint[] breakpoints) {		
		Iterator it = fInputToData.keySet().iterator();
		while (it.hasNext()) {
			AbstractBreakpointManagerInput input = (AbstractBreakpointManagerInput) it.next();
			InputData data = getInputData(input);
			breakpointsAddedInput(data, breakpoints);
		}				
	}
	
	private void breakpointsRemovedInput(InputData data, IBreakpoint[] breakpoints) {
		if (data == null || data.fContainer == null) {
			return;	
		}
		
		IBreakpoint[] filteredBreakpoints = filterBreakpoints(data.fInput, breakpoints);
		ModelDelta delta = new ModelDelta(data.fInput, IModelDelta.NO_CHANGE);
		for (int i = 0; i < filteredBreakpoints.length; ++i) {
			data.fContainer.removeBreakpoint(filteredBreakpoints[i], delta);
		}
		fireModelChanged(data.fInput, delta, "breakpointsRemovedInput"); //$NON-NLS-1$
		
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsRemoved(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	synchronized public void breakpointsRemoved(final IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		Iterator it = fInputToData.keySet().iterator();
		while (it.hasNext()) {
			AbstractBreakpointManagerInput input = (AbstractBreakpointManagerInput) it.next();
			InputData data = getInputData(input);
			breakpointsRemovedInput(data, breakpoints);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsChanged(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	synchronized public void breakpointsChanged(final IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		Iterator it = fInputToData.keySet().iterator();
		while (it.hasNext()) {
			AbstractBreakpointManagerInput input = (AbstractBreakpointManagerInput) it.next();
			InputData data = getInputData(input);
			if (data == null || data.fContainer == null) continue;			
				
			IBreakpoint[] filteredBreakpoints = filterBreakpoints(input, breakpoints);
			
			// If the change caused a breakpoint to be added (installed) or remove (un-installed) update accordingly.
			List removed = new ArrayList();
			List added = new ArrayList();
			List filteredAsList = Arrays.asList(filteredBreakpoints);
			for (int i = 0; i < breakpoints.length; i++) {
				IBreakpoint bp = breakpoints[i];
				boolean oldContainedBp = data.fContainer.contains(bp);
				boolean newContained = filteredAsList.contains(bp);
				if (oldContainedBp && !newContained) {
					removed.add(bp);
				} else if (!oldContainedBp && newContained) {
					added.add(bp);
				}					
			}
			if (!added.isEmpty()) {
				breakpointsAddedInput(data, (IBreakpoint[]) added.toArray(new IBreakpoint[added.size()]));
			}
			if (!removed.isEmpty()) {
				breakpointsRemovedInput(data, (IBreakpoint[]) removed.toArray(new IBreakpoint[removed.size()]));
			}						
			
			ModelDelta delta = new ModelDelta(input, IModelDelta.NO_CHANGE);
			for (int i = 0; i < filteredBreakpoints.length; ++i)
				appendModelDelta(data.fContainer, delta, IModelDelta.STATE, filteredBreakpoints[i]);
			fireModelChanged(input, delta, "breakpointsChanged");		 //$NON-NLS-1$
		}
	}	
	/**
	 * Appends the model delta flags to child containers that contains the breakpoint.
	 * 
	 * @param parent the parent container.
	 * @param parentDelta the parent model delta.
	 * @param flags the model delta flags.
	 * @param breakpoint the breakpoint to search in the children containers.
	 */
	private void appendModelDelta(BreakpointContainer parent, ModelDelta parentDelta, int flags, IBreakpoint breakpoint) {
		BreakpointContainer[] containers = parent.getContainers();
		
		if (parent.contains(breakpoint)) {
			if ((containers.length != 0)) {
				for (int i = 0; i < containers.length; ++i) {
					ModelDelta nodeDelta = parentDelta.addNode(containers[i], flags);
					appendModelDelta(containers[i], nodeDelta, flags, breakpoint);
				}			
			} else {
				parentDelta.addNode(breakpoint, flags);
			}
		}			
	}
	
	/**
	 * Appends the model delta to the first found element in the model delta tree.
	 * 
	 * @param parentDelta the parent delta
	 * @param element the element to search
	 * @param flags the delta flags
	 */
	private void appendModelDeltaToElement(IModelDelta parentDelta, Object element, int flags) {
		if (element.equals(parentDelta.getElement())) {
			((ModelDelta) parentDelta).setFlags(parentDelta.getFlags() | flags);
			return;
		}
		
		IModelDelta[] childDeltas = parentDelta.getChildDeltas();
		for (int i = 0; i < childDeltas.length; ++i) {
			if (element.equals(childDeltas[i].getElement())) {
				((ModelDelta) childDeltas[i]).setFlags(childDeltas[i].getFlags() | flags);
				return;
			}
			
			appendModelDeltaToElement(childDeltas[i], element, flags);
		}
	}
}
