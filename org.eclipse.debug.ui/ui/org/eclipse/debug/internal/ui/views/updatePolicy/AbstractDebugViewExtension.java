/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.updatePolicy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

public abstract class AbstractDebugViewExtension extends AbstractDebugView implements IDebugViewExtension {

	private static final DebugModel[] EMPTY = new DebugModel[0];
	private static final IUpdatePolicy[] EMPTY_POLICY = new IUpdatePolicy[0];
	
	private static final String DEBUG_MODEL_MEMENTO_TYPE = "debugModel"; //$NON-NLS-1$
	private static final String DEBUG_MODEL_ROOT = "debugModels"; //$NON-NLS-1$
	private static final String MODEL_DATA_FILENAME = "debugModel.xml"; //$NON-NLS-1$
	
	protected Set fModels = new HashSet();
	protected Set fActiveModels = new HashSet();
	protected Set fUpdateListeners = new HashSet();
	
	protected ListenerList fListeners = new ListenerList();
	protected String fActivePolicySetId;

	class NotifyListenersRunnable implements ISafeRunnable
	{
		private IPropertyChangeListener fListener;
		private PropertyChangeEvent fEvent;
		
		NotifyListenersRunnable(IPropertyChangeListener listener, PropertyChangeEvent event)
		{
			fEvent = event;
			fListener = listener;
		}

		public void handleException(Throwable exception) {
			DebugUIPlugin.log(exception);
		}

		public void run() throws Exception {
			fListener.propertyChange(fEvent);
		}
	}
	
	
	// This model is always active and stores a set of mandatory update
	// policies that should be active all the time.
	protected DebugModel fMandatoryModel = new DebugModel("", this); //$NON-NLS-1$
	
	public void addDebugModels(DebugModel[] models) {
		for (int i=0; i<models.length; i++)
			fModels.add(models[i]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.updatePolicy.IDebugViewExtension#getUpdaters(org.eclipse.debug.core.model.IDebugElement)
	 */
	public DebugModel[] getDebugModels(String  modelId) {
		if (fModels.isEmpty())
			return EMPTY;
		
		if (modelId == null)
			return (DebugModel[])fModels.toArray(new DebugModel[fModels.size()]);
		
		ArrayList returnList = null;
		DebugModel[] models = (DebugModel[])fModels.toArray(new DebugModel[fModels.size()]);
		for (int i =0; i<models.length; i++)
		{
			DebugModel model = models[i];
			if (model.getModelIdentifier().equals(modelId))
			{
				if (returnList == null)
					returnList = new ArrayList();
				returnList.add(model);
			}
		}
		
		if (returnList == null)
			return EMPTY;
		
		return (DebugModel[])returnList.toArray(new DebugModel[returnList.size()]);
	}

	public boolean isVisible(Object obj) {
		
		if (obj == null || getViewer().getInput() == null)
			return true;
		
		if (obj.equals(getViewer().getInput()))
			return true;
		
		return false;
	}

	public void removeDebugModels(DebugModel[] models) {
		for (int i=0; i<models.length; i++)
			fModels.remove(models[i]);
	}
	
	
	public void activateModel(IDebugElement elm)
	{
		fMandatoryModel.setModelIdentifier(elm.getModelIdentifier());
		fMandatoryModel.activateModel(elm);
		
		// activate models with the same model id
		DebugModel[] models = getDebugModels(elm.getModelIdentifier());
		for (int i=0; i<models.length; i++){
			models[i].activateModel(elm);
			fActiveModels.add(models[i]);
		}
	}
	
	/**
	 * Deactivate a model, if model Id is null, all
	 * models will be deactivated
	 * @param modelId
	 */
	public void deactivateModel(String modelId)
	{
		if (modelId == null)
		{
			DebugModel[] models = getActiveModels();
			for (int i=0; i<models.length; i++)
			{
				models[i].deactivateModel();
				fActiveModels.remove(models[i]);
			}
			return;
		}
		
		DebugModel[] models = getDebugModels(modelId);
		for (int i=0; i<models.length; i++){
			models[i].deactivateModel();
			fActiveModels.remove(models[i]);
		}
	}
	
	public DebugModel[] getActiveModels()
	{
		return (DebugModel[])fActiveModels.toArray(new DebugModel[fActiveModels.size()]);
	}

	protected void becomesHidden() {
		
		IUpdatePolicyHandler[] handlers = fMandatoryModel.getPolicyHandlers();
		for (int j=0; j<handlers.length; j++)
		{
			handlers[j].becomesHidden(this);
		}
		
		DebugModel[] models = getActiveModels();
		for (int i=0; i<models.length; i++)
		{
			handlers = models[i].getPolicyHandlers();
			for (int j=0; j<handlers.length; j++)
			{
				handlers[j].becomesHidden(this);
			}
		}
		super.becomesHidden();
	}

	protected void becomesVisible() {
		
		IUpdatePolicyHandler[] handlers = fMandatoryModel.getPolicyHandlers();
		for (int j=0; j<handlers.length; j++)
		{
			handlers[j].becomesVisible(this);
		}
		
		DebugModel[] models = getActiveModels();
		for (int i=0; i<models.length; i++)
		{
			handlers = models[i].getPolicyHandlers();
			for (int j=0; j<handlers.length; j++)
			{
				handlers[j].becomesVisible(this);
			}
		}
		super.becomesVisible();
	}
	
	/**
	 * @return a list of default update policies to use if a model
	 * has not define any update policy
	 */
	public IUpdatePolicy[] getDefaultUpdatePolicies()
	{
		return EMPTY_POLICY;
	}
	
	public IUpdatePolicy[] getMandatoryUpdatePolicies()
	{
		return EMPTY_POLICY;
	}

	/**
	 * Helper Method to set up mandatory model
	 */
	protected void setupMandatoryModel() {
		ArrayList handlers = new ArrayList();
		
		IUpdatePolicy[] updatePolicies = getMandatoryUpdatePolicies();
		for (int i=0; i<updatePolicies.length; i++)
		{
			IUpdatePolicyHandler handler;
			try {
				handler = updatePolicies[i].createHandler();
				handler.init(updatePolicies[i], this);
				handlers.add(handler);
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		
		fMandatoryModel.addPolicyHandlers((IUpdatePolicyHandler[])handlers.toArray(new IUpdatePolicyHandler[handlers.size()]));
	}

	public void dispose() {
		
		saveModelsToFile();
		
        fMandatoryModel.dispose();
        DebugModel[] models = getDebugModels(null);
        for (int i=0; i<models.length; i++)
        {
        	models[i].dispose();
        }
		super.dispose();
	}

	public void addUpdateListener(IDebugViewUpdateListener listener) {
		fUpdateListeners.add(listener);
	}

	public void removeUpdateListener(IDebugViewUpdateListener listener) {
		fUpdateListeners.remove(listener);
	}
	
	public IDebugViewUpdateListener[] getUpdateListeners()
	{
		return (IDebugViewUpdateListener[])fUpdateListeners.toArray(new IDebugViewUpdateListener[fUpdateListeners.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager menu) {
		
		DebugModel[] models = getActiveModels();
		
		// TODO:  move actions to plugin.xml... these strings move to plugin.properties
		menu.add(new Separator("UpdatePolicy")); //$NON-NLS-1$
		IMenuManager updatePolicyMenu = new MenuManager("Update Policy",  "updatePolicy"); //$NON-NLS-2$
		for (int i=0; i<models.length; i++)
		{
			IUpdatePolicySet activePolicySet = models[i].getActivePolicySet();
			IUpdatePolicySet[] policySets = models[i].getPolicySets();
			if (policySets.length > 0)
			{
				for (int j=0; j<policySets.length; j++)
				{
					SetUpdatePolicyAction action = new SetUpdatePolicyAction(this, policySets[j]);
					
					if (policySets[j] == activePolicySet)
						action.setChecked(true);
					
					updatePolicyMenu.add(action);
				}
			}
		}
		menu.add(updatePolicyMenu);
		menu.add(new Action("Manage Policy Set..."){

			public void run() {
			
				PolicySetDialog dialog = new PolicySetDialog(DebugUIPlugin.getActiveWorkbenchWindow().getShell());
				dialog.open();
			}});
	}

	public void saveModels(IMemento memento) {	
		// save debug models
		Iterator iter = fModels.iterator();
		while (iter.hasNext())
		{
			DebugModel model = (DebugModel)iter.next();
			IMemento node= memento.createChild(DEBUG_MODEL_MEMENTO_TYPE);
			model.saveState(node);
		}
	}
	
	protected void restoreDebugModels(IMemento memento)
	{
		if (memento == null)
			return;
		
		IMemento[] children = memento.getChildren(DEBUG_MODEL_MEMENTO_TYPE);
		for (int i=0; i<children.length; i++){
			DebugModel model = new DebugModel(children[i], this);
			fModels.add(model);
		}
	}

	public void saveModelsToFile()
	{
		XMLMemento memento = XMLMemento.createWriteRoot(DEBUG_MODEL_ROOT);
		saveModels(memento);

		IPath file = getDataFile();
		File fileHandle = new File(file.toOSString());
		if (!fileHandle.exists())
		{
			try {
				fileHandle.createNewFile();
			} catch (IOException e) {
				DebugUIPlugin.log(e);
				return;
			}
		}
		String fileName = file.toOSString();
		try {
			FileOutputStream stream = new FileOutputStream(fileName);
			OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8"); //$NON-NLS-1$
			memento.save(writer);
			writer.close();
			stream.close();
		} catch (FileNotFoundException e) {
			DebugUIPlugin.log(e);
		} catch (UnsupportedEncodingException e) {
			DebugUIPlugin.log(e);
		} catch (IOException e) {
			DebugUIPlugin.log(e);
		}
	}
	
	public void readModelsFromFile()
	{
		IPath file = getDataFile();
		File fileHandle = new File(file.toOSString());
		if (!fileHandle.exists())
		{
			return;
		}
		
		try {
			FileInputStream stream = new FileInputStream(fileHandle);
			InputStreamReader reader = new InputStreamReader(stream, "utf-8"); //$NON-NLS-1$
			XMLMemento memento = XMLMemento.createReadRoot(reader);
			restoreDebugModels(memento);
			
		} catch (FileNotFoundException e) {
			DebugUIPlugin.log(e);
		} catch (UnsupportedEncodingException e) {
			DebugUIPlugin.log(e);
		} catch (WorkbenchException e) {
			DebugUIPlugin.log(e);
		}
	}
	
	protected IPath getDataFile()
	{
		String dir = getUniqueId();
		
		String filename = MODEL_DATA_FILENAME;
		IPath path = DebugUIPlugin.getDefault().getStateLocation().append(dir);
		path = path.addTrailingSeparator();
		
		File dirPath = new File(path.toOSString());
		if (!dirPath.exists())
			dirPath.mkdir();
		
		path = path.append(filename);
		
		return path;
	}

	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		readModelsFromFile();
	}
	
	protected String getUniqueId()
	{
		StringBuffer id = new StringBuffer(getSite().getId());
		
		if (getSite() instanceof IViewSite)
		{
			IViewSite vs = (IViewSite)getSite();
			if (vs.getSecondaryId() != null)
			{
				// use secondary id if it's not null
				// for handling multiple views in the future
				id.append("_"); //$NON-NLS-1$
				id.append(vs.getSecondaryId());
			}
		}
		return id.toString();
	}
	
	public void setActivePolicySet(String policySetId)
	{
		String oldId = fActivePolicySetId;
		fActivePolicySetId = policySetId;
		Object[] listeners = fListeners.getListeners();
		PropertyChangeEvent evt = new PropertyChangeEvent(this, IDebugViewExtension.PROPERTY_UPDATE_POLICY, oldId, fActivePolicySetId);
		for (int i=0; i<listeners.length; i++)
		{
			if (listeners[i] instanceof IPropertyChangeListener)
			{
				NotifyListenersRunnable runnable = new NotifyListenersRunnable((IPropertyChangeListener)listeners[i], evt);
				Platform.run(runnable);
			}
		}
	}
	
	public String getActivePolicySet()
	{
		return fActivePolicySetId;
	}
	
	public void addListener(IPropertyChangeListener listener)
	{
		fListeners.add(listener);
	}
	
	public void removeListener(IPropertyChangeListener listener)
	{
		fListeners.remove(listener);
	}

}
