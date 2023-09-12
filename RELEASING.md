This is the Release plan and TODO list for CDT LSP.

## Steps for Release

Items at the beginning of development

- [ ] Create an Endgame Issue to track the release. As a starting point use [RELEASING.md](https://github.com/eclipse-cdt/cdt-lsp/blob/master/RELEASING.md).
    - [ ] Add the [Endgame](https://github.com/eclipse-cdt/cdt-lsp/labels/endgame) label
- [ ] Create a [New milestone](https://github.com/eclipse-cdt/cdt-lsp/milestones/new) for the release, and if available add a due date 
    - [ ] Apply the milestone to the endgame issue
- [ ] Ensure all previous [Endgame issues](https://github.com/eclipse-cdt/cdt-lsp/labels/endgame) are done.
    - [ ] Make sure any previous edits made to [Endgame issues](https://github.com/eclipse-cdt/cdt-lsp/labels/endgame) of previous releases are updated in [RELEASING.md](https://github.com/eclipse-cdt/cdt-lsp/blob/master/RELEASING.md)
- [ ] Update version numbers on master branch (after the release branch was created), this is generally the next minor version (or major version if that is what the committers on the project agree) and applies to the following types of files:
    - [ ] `feature.xml` version
    - [ ] `pom.xml` version
    - It does not apply to versions in MANIFEST.MF which follow [API rules](https://github.com/eclipse-cdt/cdt/blob/main/POLICY.md#api).
- [ ] Ensure [the CI build](https://ci.eclipse.org/cdt/job/cdt-lsp/job/master/) is stable - it is always better to release a "Green Dot" build

Items in the days ahead of Release day:

- [ ] Create release on [PMI](https://projects.eclipse.org/projects/tools.cdt) (e.g. `1.0.0 (CDT LSP)`)
    - [ ] Fill in the *Review Documentation* -> *New & Noteworthy URL* with the [CHANGELOG.md](https://github.com/eclipse-cdt/cdt-lsp/blob/master/CHANGELOG.md)
- [ ] Check [CHANGELOG.md](https://github.com/eclipse-cdt/cdt-lsp/blob/master/CHANGELOG.md) is up to date. The changelog should have a version entry, release date, API Breakages and other information consistent with current entries in the changelog.
- [ ] Check [README.md](https://github.com/eclipse-cdt/cdt-lsp/blob/master/README.md) is up to date, in particular:
    - [ ] the planned release and which versions of main dependencies are supported in the version support table
    - [ ] screenshots are up to date and consistent
    - [ ] try it out steps are correct and where suitable versions are up to date
- [ ] Check all closed PRs and Issues to make sure their milestone is set. [This search may be useful to identify such closed issues](https://github.com/eclipse-cdt/cdt-lsp/issues?q=is%3Aclosed)
- [ ] Create a branch for the release
- [ ] Create the endgame for the next scheduled release right away and update the versions on the master branch

Items on Release day:

- [ ] Run [the CI build](https://ci.eclipse.org/cdt/job/cdt-lsp/) for the branch
- [ ] Mark the build as Keep Forever and add the version to the description
- [ ] Create a GitHub releases page (like https://github.com/eclipse-cdt/cdt-lsp/releases/tag/CDT_LSP_1_0_0)
- [ ] [Promote a cdt build from jenkins](https://ci.eclipse.org/cdt/job/promote-a-build/) to releases
- [ ] Add description to the promote-a-build job and the job it promoted.
- [ ] Unmark as keep all old Milestone and RC jobs
- [ ] Update or create [composites](https://github.com/eclipse-cdt/cdt/tree/main/releng/download/releases) in preparation for going public on release day
  - [ ] Include the update to latest URL https://download.eclipse.org/tools/cdt/releases/cdt-lsp-latest to point to latest release
- [ ] Tag the release. Example: `git tag -a CDT_LSP_1_0_0 HEAD -m"CDT LSP 1.0.0" && git push origin CDT_LSP_1_0_0`
- [ ] Create a [release page on github](https://github.com/eclipse-cdt/cdt-lsp/releases/new)
- [ ] Publish the GitHub release page
- [ ] Forward the GitHub release page email to cdt-dev
