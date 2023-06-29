package org.eclipse.cdt.lsp.examples.preferences;

import java.util.List;

import org.eclipse.cdt.lsp.clangd.ClangdOptionsDefaults;

public class MyClangdOptionsDefaults extends ClangdOptionsDefaults {
	
	@Override
	public boolean preferClangd() {
		return true;
	}
	
	@Override
	public List<String> additionalOptions() {
		return List.of("--header-insertion=never", "--default-config");
	}

}
