package patches.vcsRoots

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, create a vcsRoot with id = 'EdFiStandard'
in the root project, and delete the patch script.
*/
create(DslContext.projectId, GitVcsRoot({
    id("EdFiStandard")
    name = "Ed-Fi-Standard"
    url = "https://github.com/mn-mde-edfi/Ed-Fi-Standard"
    branch = "refs/heads/UpdatetesToAllowSuccessfulBuild"
    branchSpec = "+:refs/heads(*)"
    authMethod = password {
        userName = "dennispiatt"
        password = "credentialsJSON:0f12473a-0eb1-4d19-b397-866c46491400"
    }
}))

