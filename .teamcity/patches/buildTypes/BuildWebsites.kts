package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.NuGetPublishStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.PowerShellStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.nuGetPublish
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.powerShell
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'BuildWebsites'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("BuildWebsites")) {
    vcs {
        remove(RelativeId("SvnHttpEduSvn01educStateMnUsSvnAsdcsTrunkMainV52"))
        add(RelativeId("HttpsGithubComMnMdeEdfiEdFiOdsRefsHeadsDevelopMnV31"), "+:. => Ed-Fi-ODS")
        add(RelativeId("EdFiOdsAdminApp"), "+:. => Ed-Fi-ODS-AdminApp")
        add(RelativeId("EdFiOdsImplementation"), "+:. => Ed-Fi-ODS-Implementation")
        add(RelativeId("EdFiStandard"), "+:. => Ed-Fi-Standard")
    }

    expectSteps {
        powerShell {
            name = "Clean package directories"
            scriptMode = script {
                content = """
                    if(Test-Path "Ed-Fi-ODS-Implementation\packages") {
                    	Remove-Item "Ed-Fi-ODS-Implementation\packages\*" -Recurse -Force | out-null
                    } else { 
                    	md packages | out-null
                    }
                    
                    Remove-Item Ed-Fi-ODS-AdminApp\*.nupkg | out-null
                """.trimIndent()
            }
        }
        powerShell {
            name = "Build Ed-Fi ODS Admin App"
            formatStderrAsError = true
            workingDir = "Ed-Fi-ODS-AdminApp"
            scriptMode = script {
                content = """
                    .\build.ps1 -Version "%adminApp.version%" -BuildCounter %build.counter% -Command Build -Configuration Release
                    .\build.ps1 -Command UnitTest -Configuration Release
                    .\build.ps1 -Version "%adminApp.version%" -BuildCounter %build.counter% -Command Package -Configuration Release
                    ${'$'}packageDir = "..\Ed-Fi-Ods-Implementation\packages"
                    if(-not (Test-Path ${'$'}packageDir)) { 
                    	md ${'$'}packageDir | out-null
                    }
                    copy *.nupkg ${'$'}packageDir
                    ls ${'$'}packageDir
                """.trimIndent()
            }
        }
        powerShell {
            name = "Build Ed-Fi ODS API websites and databases"
            formatStderrAsError = true
            workingDir = "Ed-Fi-ODS-Implementation"
            scriptMode = file {
                path = "build.teamcity.ps1"
            }
        }
        powerShell {
            name = "Copy Built Binaries for Docker"
            formatStderrAsError = true
            scriptMode = script {
                content = """
                    Write-Host "Copying WebApi"
                    copy Ed-Fi-ODS-Implementation\packages\MN.EdFi.Ods.WebApi.*.nupkg Ed-Fi-ODS-Docker\Web-Ods-Api\Alpine\mssql\app.zip
                    
                    Write-Host "Copying SwaggerUI"
                    copy Ed-Fi-ODS-Implementation\packages\MN.EdFi.Ods.SwaggerUI.*.nupkg  Ed-Fi-ODS-Docker\Web-SwaggerUI\Alpine\app.zip
                    
                    Write-Host "Copying SandboxAdmin"
                    copy Ed-Fi-ODS-Implementation\packages\MN.EdFi.Ods.SandboxAdmin.*.nupkg Ed-Fi-ODS-Docker\Web-Sandbox-Admin\Alpine\mssql\app.zip
                    
                    Write-Host "Copying AdminApp"
                    copy Ed-Fi-ODS-AdminApp\EdFi.Suite3.ODS.AdminApp.Web.*.nupkg Ed-Fi-ODS-Docker\Web-Ods-AdminApp\Alpine\mssql\app.zip
                    
                    ls -r Ed-Fi-ODS-Docker\app.zip
                """.trimIndent()
            }
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
        update<PowerShellStep>(1) {
            clearConditions()
            param("jetbrains_powershell_script_file", "")
        }
        update<PowerShellStep>(2) {
            clearConditions()
            param("jetbrains_powershell_script_code", "")
        }
        update<PowerShellStep>(3) {
            clearConditions()
        }
        update<NuGetPublishStep>(4) {
            clearConditions()
            apiKey = "credentialsJSON:b5e78adb-405c-481e-ab62-4af7b6635952"
        }
        insert(5) {
            powerShell {
                name = "Add Nuget Source for Codegen Package"
                formatStderrAsError = true
                workingDir = "Ed-Fi-ODS-Implementation"
                scriptMode = script {
                    content = "& dotnet nuget update source github -u %teamcity.github.user% -p %teamcity.github.personalAccessToken% --store-password-in-clear-text --configfile ./NuGet.Config"
                }
                param("jetbrains_powershell_script_file", "")
            }
        }
    }
}
