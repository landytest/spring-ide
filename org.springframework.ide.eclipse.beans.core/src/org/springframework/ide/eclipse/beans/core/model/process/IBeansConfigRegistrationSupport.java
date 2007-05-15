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
package org.springframework.ide.eclipse.beans.core.model.process;

import org.springframework.beans.factory.parsing.AliasDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;

/**
 * Utitity that provides methods to register elements with the
 * {@link BeansConfig}.
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IBeansConfigRegistrationSupport {

	/**
	 * Retuns the {@link IBeansConfig} that should be post processed.
	 * @return
	 */
	IBeansConfig getBeansConfig();

	/**
	 * Register a new {@link ComponentDefinition} with the {@link IBeansConfig}.
	 * @param componentDefinition the {@link ComponentDefinition} to register.
	 */
	void registerComponent(ComponentDefinition componentDefinition);

	/**
	 * Register a new {@link AliasDefinition} with the {@link IBeansConfig}.
	 * @param aliasDefinition the {@link AliasDefinition} to register.
	 */
	void registerAlias(AliasDefinition aliasDefinition);

}