package org.eclipse.cdt.lsp.examples.preferences;

import java.util.List;

import org.eclipse.cdt.lsp.clangd.ClangdOptionsDefaultsImpl;

public class MyClangdOptionsDefaultsImpl extends ClangdOptionsDefaultsImpl {
	
	@Override
	public boolean preferClangd() {
		return true;
	}
	
	@Override
	public List<String> customOptions() {
		return List.of("--header-insertion=never", "--default-config");
	}

}
