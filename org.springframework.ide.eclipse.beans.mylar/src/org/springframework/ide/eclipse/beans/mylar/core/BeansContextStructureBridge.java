/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.mylar.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.mylar.context.core.AbstractContextStructureBridge;
import org.eclipse.mylar.context.core.ContextCorePlugin;
import org.eclipse.mylar.context.core.IMylarElement;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansContextStructureBridge extends AbstractContextStructureBridge {

	public static final String CONTENT_TYPE = "spring_beans";

	@Override
	public boolean acceptsObject(Object object) {
		return (object instanceof IModelElement || (object instanceof IResource && BeansCoreUtils
				.isBeansConfig((IResource) object)));
	}

	@Override
	public boolean canBeLandmark(String handle) {
		return true;
	}

	@Override
	public boolean canFilter(Object obj) {
		if (obj instanceof IModelElement) {
			IModelElement modelElement = (IModelElement) obj;

			IModelElement[] children = modelElement.getElementChildren();
			for (IModelElement child : children) {
				IMylarElement node = ContextCorePlugin.getContextManager()
						.getElement(getHandleIdentifier(child));
				if (node != null && node.getInterest().isInteresting()) {
					return false;
				}
			}

			if (modelElement instanceof ISpringProject) {
				IBeansProject beansProject = BeansModelUtils
						.getProject(modelElement);
				return canFilter(beansProject);
			}

			IMylarElement node = ContextCorePlugin.getContextManager()
					.getElement(getHandleIdentifier(obj));
			if (node != null && node.getInterest().isInteresting()) {
				return false;
			}
		}
		else if ((obj instanceof IResource && BeansCoreUtils
				.isBeansConfig((IResource) obj))) {
			return canFilter(BeansModelUtils.getResourceModelElement(obj));
		}
		return true;
	}

	@Override
	public List<String> getChildHandles(String handle) {
		Object obj = getObjectForHandle(handle);
		if (obj != null && obj instanceof IModelElement) {
			List<String> childHandles = new ArrayList<String>();
			IModelElement[] children = ((IModelElement) obj)
					.getElementChildren();
			for (IModelElement child : children) {
				childHandles.add(child.getElementID());
			}
			return childHandles;
		}
		return null;
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE;
	}

	@Override
	public String getContentType(String handle) {
		return CONTENT_TYPE;
	}

	@Override
	public String getHandleIdentifier(Object obj) {
		if (obj instanceof IModelElement) {
			return ((IModelElement) obj).getElementID();
		}
		else if ((obj instanceof IResource && BeansCoreUtils
				.isBeansConfig((IResource) obj))) {
			return BeansModelUtils.getResourceModelElement(obj).getElementID();
		}
		return null;
	}

	@Override
	public String getName(Object obj) {
		if (obj instanceof IModelElement) {
			return ((IModelElement) obj).getElementName();
		}
		else if ((obj instanceof IResource && BeansCoreUtils
				.isBeansConfig((IResource) obj))) {
			return BeansModelUtils.getResourceModelElement(obj)
					.getElementName();
		}
		return null;
	}

	@Override
	public Object getObjectForHandle(String handle) {
		return BeansCorePlugin.getModel().getElement(handle);
	}

	@Override
	public String getParentHandle(String handle) {
		Object obj = getObjectForHandle(handle);
		if (obj != null && obj instanceof IModelElement) {
			IModelElement parent = ((IModelElement) obj).getElementParent();
			if (parent != null) {
				return parent.getElementID();
			}
		}
		return null;
	}

	@Override
	public boolean isDocument(String handle) {
		Object obj = getObjectForHandle(handle);
		if (obj != null && obj instanceof IBeansConfig) {
			return true;
		}
		return false;
	}

	@Override
	public String getHandleForOffsetInObject(Object arg0, int arg1) {
		return null;
	}

}