package org.eclipse.debug.ui.viewers;

import org.eclipse.debug.internal.ui.viewers.update.ModelDeltaNode;

public interface IModelDeltaNode {
	public IModelDeltaNode getParent();
	public Object getElement();
	public int getFlags();
	public ModelDeltaNode[] getNodes();
	public IModelDeltaNode addNode(Object object, int flags);
	
}
