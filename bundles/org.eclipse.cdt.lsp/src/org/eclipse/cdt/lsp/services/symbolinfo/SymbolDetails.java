/*******************************************************************************
 * Copyright (c) 2024 Advantest Europe GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Dietrich Travkin (Solunar GmbH) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.services.symbolinfo;

import org.eclipse.cdt.lsp.services.ClangdLanguageServer;
import org.eclipse.lsp4j.jsonrpc.util.Preconditions;
import org.eclipse.lsp4j.jsonrpc.util.ToStringBuilder;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

/**
 * Return type for the <em>textDocument/symbolInfo</em> request.
 * This class was generated by the <em>org.eclipse.lsp4j.generator</em> bundle
 * using xtend (see {@link org.eclipse.lsp4j.generator.JsonRpcData JsonRpcData} and
 * the <a href="https://github.com/eclipse-lsp4j/lsp4j/blob/main/documentation/jsonrpc.md">documentation</a>).
 *
 * @see {@link ClangdLanguageServer#getSymbolInfo(org.eclipse.lsp4j.TextDocumentPositionParams)}
 */
public class SymbolDetails {

	@NonNull
	private String name;

	@NonNull
	private String containerName;

	@NonNull
	private String usr;

	private String id;

	private RangeAndUri declarationRange;

	private RangeAndUri definitionRange;

	@NonNull
	public String getName() {
		return this.name;
	}

	public void setName(@NonNull final String name) {
		this.name = Preconditions.<String>checkNotNull(name, "name"); //$NON-NLS-1$
	}

	@NonNull
	public String getContainerName() {
		return this.containerName;
	}

	public void setContainerName(@NonNull final String containerName) {
		this.containerName = Preconditions.<String>checkNotNull(containerName, "containerName"); //$NON-NLS-1$
	}

	@NonNull
	public String getUsr() {
		return this.usr;
	}

	public void setUsr(@NonNull final String usr) {
		this.usr = Preconditions.<String>checkNotNull(usr, "usr"); //$NON-NLS-1$
	}

	public String getId() {
		return this.id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public RangeAndUri getDeclarationRange() {
		return this.declarationRange;
	}

	public void setDeclarationRange(final RangeAndUri declarationRange) {
		this.declarationRange = declarationRange;
	}

	public RangeAndUri getDefinitionRange() {
		return this.definitionRange;
	}

	public void setDefinitionRange(final RangeAndUri definitionRange) {
		this.definitionRange = definitionRange;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("name", this.name); //$NON-NLS-1$
		b.add("containerName", this.containerName); //$NON-NLS-1$
		b.add("usr", this.usr); //$NON-NLS-1$
		b.add("id", this.id); //$NON-NLS-1$
		b.add("declarationRange", this.declarationRange); //$NON-NLS-1$
		b.add("definitionRange", this.definitionRange); //$NON-NLS-1$
		return b.toString();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SymbolDetails other = (SymbolDetails) obj;
		if (this.name == null) {
			if (other.name != null)
				return false;
		} else if (!this.name.equals(other.name))
			return false;
		if (this.containerName == null) {
			if (other.containerName != null)
				return false;
		} else if (!this.containerName.equals(other.containerName))
			return false;
		if (this.usr == null) {
			if (other.usr != null)
				return false;
		} else if (!this.usr.equals(other.usr))
			return false;
		if (this.id == null) {
			if (other.id != null)
				return false;
		} else if (!this.id.equals(other.id))
			return false;
		if (this.declarationRange == null) {
			if (other.declarationRange != null)
				return false;
		} else if (!this.declarationRange.equals(other.declarationRange))
			return false;
		if (this.definitionRange == null) {
			if (other.definitionRange != null)
				return false;
		} else if (!this.definitionRange.equals(other.definitionRange))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + ((this.containerName == null) ? 0 : this.containerName.hashCode());
		result = prime * result + ((this.usr == null) ? 0 : this.usr.hashCode());
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
		result = prime * result + ((this.declarationRange == null) ? 0 : this.declarationRange.hashCode());
		return prime * result + ((this.definitionRange == null) ? 0 : this.definitionRange.hashCode());
	}
}
