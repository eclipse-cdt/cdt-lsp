# Contributing to Eclipse C/C++ Development Tools

Thanks for your interest in this project.

The CDT LSP project is a part of the Eclipse CDT project.
Please refer to CDT's [main contributing](https://github.com/eclipse-cdt/cdt/blob/main/CONTRIBUTING.md) file for details on CDT contribution processes and below for some specifics on CDT LSP.

## Setup CDT LSP for development

To try out and develop the plug-ins in you can edit and run them from source.

1. Setup a CDT development environment following the instructions in [CDT's contributing instructions](https://github.com/eclipse-cdt/cdt/blob/main/CONTRIBUTING.md#contributing-to-cdt).
2. Clone this repo
3. Import the plug-ins in this repo into Eclipse development environment from Step 1
4. Consider closing the `org.eclipse.cdt.lsp.examples.*` projects as they demonstrate how to extend CDT LSP and may affect the overall behaviour, for example by removing the "Prefer C/C++ Editor (LSP)" checkbox.
5. Launch the Eclipse IDE with this Pug-ins tab settings from the launch config: *All workspace and enabled target Pug-ins* from your development IDE

## CI Builds

All PRs are built using GitHub Actions using the workflows in the [.github/workflows](.github/workflows) directory.

All branches are built using the [Jenkinsfile](Jenkinsfile) on the [Eclipse Foundations Jenkins infrastructure](https://wiki.eclipse.org/Jenkins) in the [cdt-lsp](https://ci.eclipse.org/cdt/job/cdt-lsp) multi-branch pipeline.
The pipeline publishes continuously to download.eclipse.org, for example the `master` branch publishes to https://download.eclipse.org/tools/cdt/builds/cdt-lsp/master/

## CI Milestone and Release Builds

The [cdt-lsp](https://ci.eclipse.org/cdt/job/cdt-lsp) multi-branch pipeline's build results can be published as milestones or release builds by using the [promote-a-build](https://ci.eclipse.org/cdt/job/promote-a-build/) building with parameters and choosing the CDT repo and branch to publish.
