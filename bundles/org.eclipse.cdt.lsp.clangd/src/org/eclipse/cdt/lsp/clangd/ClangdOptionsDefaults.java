package org.eclipse.cdt.lsp.clangd;

import java.util.List;

public interface ClangdOptionsDefaults extends ClangdOptions {

	@Override
	public default List<String> toList() {
		throw new RuntimeException("This method is not intended to be called by clients"); //$NON-NLS-1$
	}

}
