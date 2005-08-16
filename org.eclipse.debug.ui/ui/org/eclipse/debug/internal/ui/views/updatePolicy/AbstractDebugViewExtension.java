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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.AbstractDebugView;

public abstract class AbstractDebugViewExtension extends AbstractDebugView implements IDebugViewExtension {

	private static final DebugModel[] EMPTY = new DebugModel[0];
	private static final IUpdatePolicy[] EMPTY_POLICY = new IUpdatePolicy[0];
	
	protected Set fModels = new HashSet();
	protected Set fActiveModels = new HashSet();
	protected Set fUpdateListeners = new HashSet();
	
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
}
