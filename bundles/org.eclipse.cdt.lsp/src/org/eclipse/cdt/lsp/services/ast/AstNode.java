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
package org.eclipse.cdt.lsp.services.ast;

import java.util.Arrays;

import org.eclipse.cdt.lsp.services.ClangdLanguageServer;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.util.Preconditions;
import org.eclipse.lsp4j.jsonrpc.util.ToStringBuilder;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

/**
 * Return type for the <em>textDocument/ast</em> request.
 * This class was generated by the <em>org.eclipse.lsp4j.generator</em> bundle
 * using xtend (see {@link org.eclipse.lsp4j.generator.JsonRpcData JsonRpcData} and
 * the <a href="https://github.com/eclipse-lsp4j/lsp4j/blob/main/documentation/jsonrpc.md">documentation</a>).
 *
 * @see {@link ClangdLanguageServer#getAst(AstParams)}
 */
public class AstNode {

	@NonNull
	private String role;

	@NonNull
	private String kind;

	private String detail;

	private String arcana;

	@NonNull
	private Range range;

	private AstNode[] children;

	public AstNode() {

	}

	@NonNull
	public String getRole() {
		return role;
	}

	public void setRole(@NonNull final String role) {
		this.role = Preconditions.<String>checkNotNull(role, "role"); //$NON-NLS-1$
	}

	@NonNull
	public String getKind() {
		return this.kind;
	}

	public void setKind(@NonNull final String kind) {
		this.kind = Preconditions.<String>checkNotNull(kind, "kind"); //$NON-NLS-1$
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(final String detail) {
		this.detail = detail;
	}

	public String getArcana() {
		return arcana;
	}

	public void setArcana(final String arcana) {
		this.arcana = arcana;
	}

	@NonNull
	public Range getRange() {
		return range;
	}

	public void setRange(@NonNull final Range range) {
		this.range = Preconditions.<Range>checkNotNull(range, "range"); //$NON-NLS-1$
	}

	public AstNode[] getChildren() {
		return children;
	}

	public void setChildren(final AstNode[] children) {
		this.children = children;
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("role", this.role); //$NON-NLS-1$
		b.add("kind", this.kind); //$NON-NLS-1$
		b.add("detail", this.detail); //$NON-NLS-1$
		b.add("arcana", this.arcana); //$NON-NLS-1$
		b.add("range", this.range); //$NON-NLS-1$
		b.add("children", this.children); //$NON-NLS-1$
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
		AstNode other = (AstNode) obj;
		if (this.role == null) {
			if (other.role != null)
				return false;
		} else if (!this.role.equals(other.role))
			return false;
		if (this.kind == null) {
			if (other.kind != null)
				return false;
		} else if (!this.kind.equals(other.kind))
			return false;
		if (this.detail == null) {
			if (other.detail != null)
				return false;
		} else if (!this.detail.equals(other.detail))
			return false;
		if (this.arcana == null) {
			if (other.arcana != null)
				return false;
		} else if (!this.arcana.equals(other.arcana))
			return false;
		if (this.range == null) {
			if (other.range != null)
				return false;
		} else if (!this.range.equals(other.range))
			return false;
		if (this.children == null) {
			if (other.children != null)
				return false;
		} else if (!Arrays.deepEquals(this.children, other.children))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.role == null) ? 0 : this.role.hashCode());
		result = prime * result + ((this.kind == null) ? 0 : this.kind.hashCode());
		result = prime * result + ((this.detail == null) ? 0 : this.detail.hashCode());
		result = prime * result + ((this.arcana == null) ? 0 : this.arcana.hashCode());
		result = prime * result + ((this.range == null) ? 0 : this.range.hashCode());
		return prime * result + ((this.children == null) ? 0 : Arrays.deepHashCode(this.children));
	}
}
