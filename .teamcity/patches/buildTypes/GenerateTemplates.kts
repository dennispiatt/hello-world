package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.NuGetPublishStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.nuGetPublish
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.powerShell
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'GenerateTemplates'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("GenerateTemplates")) {
    vcs {
        remove(RelativeId("SvnHttpEduSvn01educStateMnUsSvnAsdcsTrunkMainV52"))
        add(RelativeId("EdFiOdsImplementation"), "+:. => Ed-Fi-ODS-Implementation")
        add(RelativeId("HttpsGithubComMnMdeEdfiEdFiOdsRefsHeadsDevelopMnV31"), "+:. => Ed-Fi-ODS")
        add(RelativeId("EdFiStandard"), "+:. => Ed-Fi-Standard")
    }

    expectSteps {
        powerShell {
            name = "Clean database templates directory"
            formatStderrAsError = true
            scriptMode = script {
                content = """
                    write-host "*** determining sql service account"
                    ${'$'}sqlQuery = @"
                    	SELECT servicename, service_account
                    	FROM sys.dm_server_services
                    	GO
                    "@
                    
                    Invoke-SqlCmd -ServerInstance "(local)" -Query ${'$'}sqlQuery
                    
                    ${'$'}backupPath = "Ed-Fi-ODS-Implementation\DatabaseTemplate\Database"
                    
                    if(Test-Path ${'$'}backupPath) { 
                        Write-Host "Removing contents of ${'$'}backupPath"
                    	Remove-Item "${'$'}backupPath\*" -Recurse -Force
                    } else { 
                    	Write-Host "Creating Directory ${'$'}backupPath"
                    	md ${'$'}backupPath
                    }
                    
                    ${'$'}user = "NT Service\MSSQLSERVER"
                    ${'$'}ACL = Get-ACL -Path ${'$'}backupPath
                    
                    (Get-ACL -Path ${'$'}backupPath).Access | `
                    	Format-Table IdentityReference,FileSystemRights,AccessControlType,IsInherited,InheritanceFlags -AutoSize
                    
                    ${'$'}AccessRule = New-Object System.Security.AccessControl.FileSystemAccessRule(${'$'}user,"FullControl","Allow")
                    ${'$'}ACL.SetAccessRule(${'$'}AccessRule)
                    ${'$'}ACL | Set-Acl -Path ${'$'}backupPath
                    
                    (Get-ACL -Path ${'$'}backupPath).Access | `
                        Format-Table IdentityReference,FileSystemRights,AccessControlType,IsInherited,InheritanceFlags -AutoSize
                """.trimIndent()
            }
        }
        powerShell {
            name = "Build Database Templates"
            formatStderrAsError = true
            workingDir = "Ed-Fi-ODS-Implementation"
            scriptMode = file {
                path = "buildDatabaseTemplates.ps1"
            }
            noProfile = false
        }
        nuGetPublish {
            name = "Publish NuGet packages to Nexus"
            toolPath = "%teamcity.tool.NuGet.CommandLine.DEFAULT%"
            packages = """Ed-Fi-ODS-Implementation\packages\*.nupkg"""
            serverUrl = "%nexus.nuget.package.source%"
            apiKey = "zxxa0be481900a4b3ec255e6980f95e81b4d3bf3725bfb0a1b0"
        }
    }
    steps {
        update<NuGetPublishStep>(2) {
            clearConditions()
            apiKey = "credentialsJSON:b5e78adb-405c-481e-ab62-4af7b6635952"
        }
    }
}
