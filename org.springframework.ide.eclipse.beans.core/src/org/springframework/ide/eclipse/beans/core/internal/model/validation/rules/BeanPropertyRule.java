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
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansValidationContext;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.util.StringUtils;

/**
 * Validates a given {@link IBeanProperty}'s accessor methods in bean class.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class BeanPropertyRule implements
		IValidationRule<IBeanProperty, BeansValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return (element instanceof IBeanProperty
				// Skip properties with placeholders
				&& !ValidationRuleUtils.hasPlaceHolder(((IBeanProperty) element)
						.getElementName()));
	}

	public void validate(IBeanProperty property, BeansValidationContext context,
			IProgressMonitor monitor) {
		IBean bean = (IBean) property.getElementParent();
		BeanDefinition mergedBd = BeansModelUtils.getMergedBeanDefinition(bean,
				context.getContextElement());
		String mergedClassName = mergedBd.getBeanClassName();
		if (mergedClassName != null
				&& !ValidationRuleUtils.hasPlaceHolder(mergedClassName)) {
			IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(
					bean).getProject(), mergedClassName);
			if (type != null) {
				validateProperty(property, type, context);
			}
		}
	}

	private void validateProperty(IBeanProperty property, IType type,
			BeansValidationContext context) {
		String propertyName = property.getElementName();

		// Check for property accessor in given type
		try {

			// First check for nested property path
			int nestedIndex = getNestedPropertySeparatorIndex(propertyName,
					false);
			if (nestedIndex >= 0) {
				String nestedPropertyName = propertyName.substring(0,
						nestedIndex);
				PropertyTokenHolder tokens = getPropertyNameTokens(
						nestedPropertyName);
				String getterName = "get"
						+ StringUtils.capitalize(tokens.actualName);
				IMethod getter = Introspector.findMethod(type, getterName,
						0, Public.YES, Static.NO);
				if (getter == null) {
					context.error(property, "NO_GETTER",
							"No getter found for nested property '"
									+ nestedPropertyName + "' in class '"
									+ type.getFullyQualifiedName() + "'");
				} else {

					// Check getter's return type
					if (tokens.keys != null) {
						// TODO Check getter's return type for index or map
						// type
					}
				}
			} else {

				// Now check for mapped property
				int mappedIndex = propertyName
						.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR);
				if (mappedIndex != -1) {
					propertyName = propertyName.substring(0, mappedIndex);
				}

				// Finally check property
				if (!Introspector.isValidPropertyName(propertyName)) {
					context.error(property, "INVALID_PROPERTY_NAME",
							"Invalid property name '" + propertyName
									+ "' - not JavaBean compliant");
				} else if (!Introspector.hasWritableProperty(type,
						propertyName)) {
					context.error(property, "NO_SETTER",
							"No setter found for property '" + propertyName
									+ "' in class '"
									+ type.getFullyQualifiedName() + "'");
				}

				// TODO If mapped property then check type of setter's
				// argument
			}
		} catch (JavaModelException e) {
			BeansCorePlugin.log(e);
		}
	}

	/**
	 * Determine the first (or last) nested property separator in the given
	 * property path, ignoring dots in keys (like "map[my.key]").
	 * @param propertyPath the property path to check
	 * @param last whether to return the last separator rather than the first
	 * @return the index of the nested property separator, or -1 if none
	 */
	private int getNestedPropertySeparatorIndex(String propertyPath,
			boolean last) {
		boolean inKey = false;
		int i = (last ? propertyPath.length() - 1 : 0);
		while ((last && i >= 0) || i < propertyPath.length()) {
			switch (propertyPath.charAt(i)) {
			case PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR:
			case PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR:
				inKey = !inKey;
				break;
			case PropertyAccessor.NESTED_PROPERTY_SEPARATOR_CHAR:
				if (!inKey) {
					return i;
				}
			}
			if (last) {
				i--;
			} else {
				i++;
			}
		}
		return -1;
	}

	/**
	 * Parse the given property name into the corresponding property name
	 * tokens.
	 * 
	 * @param propertyName
	 *            the property name to parse
	 * @return representation of the parsed property tokens
	 */
	private PropertyTokenHolder getPropertyNameTokens(String propertyName) {
		PropertyTokenHolder tokens = new PropertyTokenHolder();
		String actualName = null;
		Set<String> keys = new LinkedHashSet<String>(2);
		int searchIndex = 0;
		while (searchIndex != -1) {
			int keyStart = propertyName.indexOf(
					PropertyAccessor.PROPERTY_KEY_PREFIX, searchIndex);
			searchIndex = -1;
			if (keyStart != -1) {
				int keyEnd = propertyName
						.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX, keyStart
								+ PropertyAccessor.PROPERTY_KEY_PREFIX.length());
				if (keyEnd != -1) {
					if (actualName == null) {
						actualName = propertyName.substring(0, keyStart);
					}
					String key = propertyName.substring(keyStart
							+ PropertyAccessor.PROPERTY_KEY_PREFIX.length(),
							keyEnd);
					if (key.startsWith("'") && key.endsWith("'")) {
						key = key.substring(1, key.length() - 1);
					} else if (key.startsWith("\"") && key.endsWith("\"")) {
						key = key.substring(1, key.length() - 1);
					}
					keys.add(key);
					searchIndex = keyEnd
							+ PropertyAccessor.PROPERTY_KEY_SUFFIX.length();
				}
			}
		}
		tokens.actualName = (actualName != null ? actualName : propertyName);
		tokens.canonicalName = tokens.actualName;
		if (!keys.isEmpty()) {
			tokens.canonicalName += PropertyAccessor.PROPERTY_KEY_PREFIX
					+ StringUtils.collectionToDelimitedString(keys,
							PropertyAccessor.PROPERTY_KEY_SUFFIX
									+ PropertyAccessor.PROPERTY_KEY_PREFIX)
					+ PropertyAccessor.PROPERTY_KEY_SUFFIX;
			tokens.keys = keys.toArray(new String[keys.size()]);
		}
		return tokens;
	}

	private static class PropertyTokenHolder {

		private String canonicalName;
		private String actualName;
		private String[] keys;
	}
}