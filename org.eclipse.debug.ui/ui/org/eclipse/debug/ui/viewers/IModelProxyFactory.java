package org.eclipse.debug.ui.viewers;

public interface IModelProxyFactory {
	public IModelProxy createModelProxy(Object element, IPresentationContext context);
}
