package org.eclipse.cdt.lsp.examples.preferences;

import java.util.List;

import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.cdt.lsp.clangd.ClangdOptionsDefaults;
import org.osgi.service.component.annotations.Component;

@Component(service = ClangdOptions.class, property = { "service.ranking:Integer=100", "isDefaultOptions:Boolean=true" })
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
