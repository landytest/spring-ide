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
package org.springframework.ide.eclipse.aop.core.internal.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeanAspectDefinitionElementFactory implements IElementFactory {

	protected static final String ADVICE_TYPE_ATTRIBUTE = "advice-type";

	protected static final String PROXY_TARGET_CLASS_ATTRIBUTE = "proxy-target-class";

	protected static final String FILE_ATTRIBUTE = "file";

	protected static final String ASPECT_LINE_NUMBER_ATTRIBUTE = "aspect-line-number";

	protected static final String ARG_NAMES_ATTRIBUTE = "arg-names";

	protected static final String THROWING_ATTRIBUTE = "throwing";

	protected static final String RETURNING_ATTRIBUTE = "returning";

	protected static final String POINTCUT_EXPRESSION_ATTRIBUTE = "pointcut-expression";

	protected static final String ASPECT_NAME_ATTRIBUTE = "aspect-name";

	protected static final String ADIVCE_METHOD_PARAMETER_TYPES_ATTRIBUTE = "adivce-method-parameter-types";

	protected static final String ADVICE_CLASS_NAME_ATTRIBUTE = "advice-class-name";

	protected static final String ADVICE_METHOD_NAME_ATTRIBUTE = "advice-method-name";

	public static String FACTORY_ID = Activator.PLUGIN_ID
			+ ".beanAspectDefinitionElementFactory";

	protected BeanAspectDefinition createAspectDefinition() {
		return new BeanAspectDefinition();
	}

	public final IAdaptable createElement(IMemento memento) {
		BeanAspectDefinition definition = createAspectDefinition();
		populateAspectDefinition(definition, memento);
		return definition;
	}

	protected final void populateAspectDefinition(
			BeanAspectDefinition definition, IMemento memento) {
		String adivceMethodName = memento
				.getString(ADVICE_METHOD_NAME_ATTRIBUTE);
		String aspectClassName = memento.getString(ADVICE_CLASS_NAME_ATTRIBUTE);
		String adivceMethodParameterTypesString = memento
				.getString(ADIVCE_METHOD_PARAMETER_TYPES_ATTRIBUTE);
		String[] adivceMethodParameterTypes = null;
		if (adivceMethodParameterTypesString != null) {
			adivceMethodParameterTypes = StringUtils
					.delimitedListToStringArray(
							adivceMethodParameterTypesString, ",");
		}
		String aspectName = memento.getString(ASPECT_NAME_ATTRIBUTE);
		String pointcutExpressionString = memento
				.getString(POINTCUT_EXPRESSION_ATTRIBUTE);
		String returning = memento.getString(RETURNING_ATTRIBUTE);
		String throwing = memento.getString(THROWING_ATTRIBUTE);
		String argNamesString = memento.getString(ARG_NAMES_ATTRIBUTE);
		String[] argNames = null;
		if (argNamesString != null) {
			argNames = StringUtils.delimitedListToStringArray(argNamesString,
					",");
		}
		int aspectLineNumber = memento.getInteger(ASPECT_LINE_NUMBER_ATTRIBUTE);
		String fileName = memento.getString(FILE_ATTRIBUTE);
		boolean proxyTargetClass = Boolean.valueOf(memento
				.getString(PROXY_TARGET_CLASS_ATTRIBUTE));
		IAopReference.ADVICE_TYPES type = ADVICE_TYPES.valueOf(memento
				.getString(ADVICE_TYPE_ATTRIBUTE));

		definition.setAdviceMethodName(adivceMethodName);
		definition.setAspectClassName(aspectClassName);
		definition.setAdviceMethodParameterTypes(adivceMethodParameterTypes);
		definition.setAspectName(aspectName);
		definition.setPointcutExpression(pointcutExpressionString);
		definition.setReturning(returning);
		definition.setThrowing(throwing);
		definition.setArgNames(argNames);
		definition.setAspectLineNumber(aspectLineNumber);
		definition.setProxyTargetClass(proxyTargetClass);
		definition.setType(type);

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource member = root.findMember(fileName);
		if (member != null) {
			definition.setResource(member);
		}

		postPopulateAspectDefinition(definition, memento);
	}

	protected void postPopulateAspectDefinition(
			BeanAspectDefinition definition, IMemento memento) {

	}
}