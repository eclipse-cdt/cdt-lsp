/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 * Alexander Fedorov (ArSysOp) - use Platform for logging
 *******************************************************************************/

package org.eclipse.cdt.lsp.server;

import java.util.function.Supplier;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;

/**
 * Checks whether the LSP based C/C++ Editor and language server shall be enabled
 * 
 */
public final class EnableExpression {
	private final Expression cExpression;
	private final Supplier<IEvaluationContext> cParent;

	public EnableExpression(Supplier<IEvaluationContext> parent, Expression expression) {
		this.cExpression = expression;
		this.cParent = parent;
	}

	/**
	 * Evaluates enable expression from enabledWhen element in extension point with the given default variable.
	 *
	 * @return true if expression evaluates to true, false otherwise
	 */
	public boolean evaluate(Object defaultVariable) {
		try {
			if (defaultVariable == null) {
				defaultVariable = new Object();
			}
			final var context = new EvaluationContext(cParent.get(), defaultVariable);
			context.setAllowPluginActivation(true);
			return cExpression.evaluate(context).equals(EvaluationResult.TRUE);
		} catch (CoreException e) {
			Platform.getLog(getClass()).log(e.getStatus());
		}
		return false;
	}

}
