import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.nuGetPublish
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.powerShell
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.dockerRegistry
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.nuGetFeed
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.VcsTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.finishBuildTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.SvnVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2021.2"

project {

    vcsRoot(SvnHttpEduSvn01educStateMnUsSvnAsdcsTrunkMainV52)

    buildType(BuildAndPushDockerContainers)
    buildType(BuildWebsites)
    buildType(GenerateTemplates)

    params {
        param("odsapi.build.runPester", "false")
        param("school.year", "2023")
        param("odsapi.build.package.webApi.version", "5.2.%build.counter%")
        param("odsapi.build.package.sandboxAdmin.version", "5.2.%build.counter%")
        param("octopus.nuget.space", "Spaces-1")
        param("nexus.nuget.package.source", "http://edu-dockeru01.educ.state.mn.us:8081/repository/Ed-Fi/")
        param("odsapi.build.package.webApi.id", "MN.EdFi.Ods.WebApi")
        param("odsapi.build.runDotnetTest", "false")
        param("octopus.nuget.apikey", "API-VOYBXBWTTZSUKVD6QPEAMLCF2VGLRXTF")
        param("nexus.docker.feed", "http://edu-dockeru01.educ.state.mn.us:8081/repository/edfi-docker/")
        param("octopus.nuget.package.source", "http://edu-edfidployv5/")
        param("odsapi.build.odsTokens", "2023")
        param("nexus.nuget.username", "EdFi-Admin")
        param("odsapi.build.package.sandboxAdmin.id", "MN.EdFi.Ods.SandboxAdmin")
        param("env.MNIT_TeamCity_Build", "true")
        param("env.msbuild_buildConfiguration", "Release")
        param("odsapi.build.package.databases.version", "5.2.%build.counter%")
        param("odsapi.build.package.swaggerUI.version", "5.2.%build.counter%")
        param("odsapi.build.runPostman", "false")
        param("odsapi.build.package.databases.id", "MN.EdFi.RestApi.Databases")
        param("odsapi.build.runSmokeTest", "false")
        param("nexus.nuget.password", "Q!W@E#q1w2e3")
        param("odsapi.build.package.swaggerUI.id", "MN.EdFi.Ods.SwaggerUI")
        param("odsapi.build.noDeploy", "false")
        param("odsapi.build.installType", "yearspecific")
        param("nexus.nuget.api.key", "7a7a0676-447f-39a2-87a9-6fbb251794de ")
    }

    features {
        dockerRegistry {
            id = "PROJECT_EXT_2"
            name = "Nexus"
            url = "http://edu-dockeru01.educ.state.mn.us:8081/repository/EdFi-Docker/"
            userName = "EdFi-Admin"
            password = "zxxeb6303fcd39ad33fdf804aa6e65315f5"
        }
        nuGetFeed {
            id = "repository-nuget-MDE_Nexus_EdFi_Repo"
            name = "MDE_Nexus_EdFi_Repo"
            description = "Internal Nexus Repository"
            indexPackages = true
        }
    }
}

object BuildAndPushDockerContainers : BuildType({
    name = "2. Build and push Docker images"
    description = "Builds docker containers for Ed-Fi websites"

    buildNumberPattern = "${BuildWebsites.depParamRefs["build.counter"]}"

    vcs {
        root(SvnHttpEduSvn01educStateMnUsSvnAsdcsTrunkMainV52)

        cleanCheckout = true
    }

    steps {
        powerShell {
            name = "Copy artifact binaries for docker build"
            scriptMode = script {
                content = """
                    ls Ed-Fi-ODS-Implementation/packages/*.nupkg
                    
                    Write-Host "Copying WebApi"
                    copy Ed-Fi-ODS-Implementation\packages\MN.EdFi.Ods.WebApi.*.nupkg Ed-Fi-Ods-Docker\Web-Ods-Api\Alpine\mssql\app.zip
                    
                    Write-Host "Copying SwaggerUI"
                    copy Ed-Fi-ODS-Implementation\packages\MN.EdFi.Ods.SwaggerUI.*.nupkg  Ed-Fi-Ods-Docker\Web-SwaggerUI\Alpine\app.zip
                    
                    Write-Host "Copying SandboxAdmin"
                    copy Ed-Fi-ODS-Implementation\packages\MN.EdFi.Ods.SandboxAdmin.*.nupkg Ed-Fi-Ods-Docker\Web-Sandbox-Admin\Alpine\mssql\app.zip
                    
                    Write-Host "Copying AdminApp"
                    copy Ed-Fi-ODS-Implementation\packages\EdFi.Suite3.ODS.AdminApp.Web.*.nupkg Ed-Fi-Ods-Docker\Web-Ods-AdminApp\Alpine\mssql\app.zip
                    
                    # ls -r Ed-Fi-Ods-Docker\app.zip
                """.trimIndent()
            }
        }
        powerShell {
            name = "Build Docker Images"
            formatStderrAsError = true
            workingDir = "Ed-Fi-Ods-Docker"
            scriptMode = script {
                content = """
                    ${'$'}ErrorActionPreference = "Stop"
                    ${'$'}repo = "nexus"
                    ${'$'}imageName = "edfi-docker"
                    ${'$'}schoolYear = "%school.year%"
                    ${'$'}buildCounter = "%build.counter%"
                    
                    Write-Host "docker build admin app"
                    ${'$'}imageName = "ods-api-web-admin-app"
                    
                    ${'$'}User = "%nexus.nuget.username%"
                    ${'$'}PWord = ConvertTo-SecureString -String "%nexus.nuget.password%" -AsPlainText -Force
                    ${'$'}Credential = New-Object -TypeName System.Management.Automation.PSCredential -ArgumentList ${'$'}User, ${'$'}PWord
                    ${'$'}url = "http://edu-dockeru01.educ.state.mn.us:8081/repository/edfi-raw/ssl.zip"
                    
                    Invoke-WebRequest -Uri ${'$'}url -Credential ${'$'}Credential -AllowUnencryptedAuthentication -OutFile ./Web-Ods-AdminApp/Alpine/mssql/ssl.zip  
                    
                    docker build -t ${'$'}imageName ./Web-Ods-AdminApp/Alpine/mssql
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker tag ${'$'}imageName ${'$'}repo/${'$'}(${'$'}imageName).%school.year%:%build.counter%
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker tag ${'$'}imageName ${'$'}repo/${'$'}(${'$'}imageName):latest
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker save ${'$'}repo/${'$'}(${'$'}imageName):latest -o "${'$'}(${'$'}imageName).tar"
                    
                    Write-Host "docker build ods web api"
                    ${'$'}imageName = "ods-api-web-api"
                    docker build -t ${'$'}imageName ./Web-Ods-Api/Alpine/mssql
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker tag ${'$'}imageName ${'$'}repo/${'$'}(${'$'}imageName).%school.year%:%build.counter%
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker tag ${'$'}imageName ${'$'}repo/${'$'}(${'$'}imageName):latest
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker save ${'$'}repo/${'$'}(${'$'}imageName):latest -o "${'$'}(${'$'}imageName).tar"
                    
                    Write-Host "docker build sandbox admin"
                    ${'$'}imageName = "ods-api-web-sandbox-admin"
                    docker build -t ${'$'}imageName ./Web-Sandbox-Admin/Alpine/mssql
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker tag ${'$'}imageName ${'$'}repo/${'$'}(${'$'}imageName).%school.year%:%build.counter%
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker tag ${'$'}imageName ${'$'}repo/${'$'}(${'$'}imageName):latest
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker save ${'$'}repo/${'$'}(${'$'}imageName):latest -o "${'$'}(${'$'}imageName).tar"
                    
                    Write-Host "docker build swagger"
                    ${'$'}imageName = "ods-api-web-swagger-ui"
                    docker build -t ${'$'}imageName ./Web-SwaggerUI/Alpine
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker tag ${'$'}imageName ${'$'}repo/${'$'}(${'$'}imageName).%school.year%:%build.counter%
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker tag ${'$'}imageName ${'$'}repo/${'$'}(${'$'}imageName):latest
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker save ${'$'}repo/${'$'}(${'$'}imageName):latest -o "${'$'}(${'$'}imageName).tar"
                    
                    ls *.tar | Out-Host
                """.trimIndent()
            }
        }
        powerShell {
            name = "Build Docker Images WIP"
            enabled = false
            formatStderrAsError = true
            workingDir = "Ed-Fi-Ods-Docker"
            scriptMode = script {
                content = """
                    ${'$'}ErrorActionPreference = "Stop"
                    ${'$'}repo = "nexus"
                    ${'$'}imageName = "edfi-docker"
                    ${'$'}schoolYear = "%school.year%"
                    ${'$'}buildCounter = "%build.counter%"
                    
                    Write-Host "docker build admin app"
                    ${'$'}imageName = "ods-api-web-admin-app"
                    
                    ${'$'}User = "%nexus.nuget.username%"
                    ${'$'}PWord = ConvertTo-SecureString -String "%nexus.nuget.password%" -AsPlainText -Force
                    ${'$'}Credential = New-Object -TypeName System.Management.Automation.PSCredential -ArgumentList ${'$'}User, ${'$'}PWord
                    ${'$'}url = "http://edu-dockeru01.educ.state.mn.us:8081/repository/edfi-raw/ssl.zip"
                    
                    Test-Connection edu-dockeru01
                    "%nexus.nuget.password%" | docker login "%nexus.docker.feed%" --username "%nexus.nuget.username%" --password-stdin
                    
                    Invoke-WebRequest -Uri ${'$'}url -Credential ${'$'}Credential -AllowUnencryptedAuthentication -OutFile ./Web-Ods-AdminApp/Alpine/mssql/ssl.zip  
                    
                    docker build -t ${'$'}imageName ./Web-Ods-AdminApp/Alpine/mssql
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    
                    # docker tag ${'$'}imageName "http://%nexus.docker.feed%/${'$'}(${'$'}imageName):%school.year%.%build.counter%"
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    
                    docker tag ${'$'}imageName "%nexus.docker.feed%/${'$'}(${'$'}imageName):latest"
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    
                    Write-Host "docker image push --all-tags %nexus.docker.feed%/${'$'}(${'$'}imageName)"
                    docker push --all-tags "%nexus.docker.feed%/${'$'}(${'$'}imageName)"
                    # docker save ${'$'}repo/${'$'}(${'$'}imageName):latest -o "${'$'}(${'$'}imageName).tar"
                    
                    Write-Host "docker build ods web api"
                    ${'$'}imageName = "ods-api-web-api"
                    docker build -t ${'$'}imageName ./Web-Ods-Api/Alpine/mssql
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker tag ${'$'}imageName ${'$'}repo/${'$'}(${'$'}imageName).%school.year%:%build.counter%
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker tag ${'$'}imageName ${'$'}repo/${'$'}(${'$'}imageName):latest
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    # docker save ${'$'}repo/${'$'}(${'$'}imageName):latest -o "${'$'}(${'$'}imageName).tar"
                    
                    Write-Host "docker build sandbox admin"
                    ${'$'}imageName = "ods-api-web-sandbox-admin"
                    docker build -t ${'$'}imageName ./Web-Sandbox-Admin/Alpine/mssql
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker tag ${'$'}imageName ${'$'}repo/${'$'}(${'$'}imageName).%school.year%:%build.counter%
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker tag ${'$'}imageName ${'$'}repo/${'$'}(${'$'}imageName):latest
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    # docker save ${'$'}repo/${'$'}(${'$'}imageName):latest -o "${'$'}(${'$'}imageName).tar"
                    
                    Write-Host "docker build swagger"
                    ${'$'}imageName = "ods-api-web-swagger-ui"
                    docker build -t ${'$'}imageName ./Web-SwaggerUI/Alpine
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker tag ${'$'}imageName ${'$'}repo/${'$'}(${'$'}imageName).%school.year%:%build.counter%
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    docker tag ${'$'}imageName ${'$'}repo/${'$'}(${'$'}imageName):latest
                    if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                    # docker save ${'$'}repo/${'$'}(${'$'}imageName):latest -o "${'$'}(${'$'}imageName).tar"
                    
                    ls *.tar | Out-Host
                """.trimIndent()
            }
        }
        powerShell {
            name = "Upload saved Docker images to Nexus"
            formatStderrAsError = true
            workingDir = "Ed-Fi-Ods-Docker"
            scriptMode = script {
                content = """
                    Get-ChildItem *.tar | ForEach-Object { 
                        write-host "uploading ${'$'}_"
                        curl --user "${'$'}("%nexus.nuget.username%"):${'$'}("%nexus.nuget.password%")" --upload-file "${'$'}_" http://edu-dockeru01.educ.state.mn.us:8081/repository/edfi-raw/ 2>&1
                    }
                """.trimIndent()
            }
        }
        step {
            name = "Octopack Docker IaC"
            type = "octopus.pack.package"
            param("octopus_packageoutputpath", "Octopack")
            param("octopus_packageid", "MN.EdFi.Octopus.Deploy")
            param("octopus_packageversion", "%odsapi.build.package.webApi.version%")
            param("octopus_packageformat", "NuPkg")
            param("octopus_publishartifacts", "true")
            param("octopus_packagesourcepath", "Ed-Fi-Ods-Docker/Octopus")
        }
        step {
            name = "Octopush IaC to Octopus package repo"
            type = "octopus.push.package"
            param("octopus_space_name", "%octopus.nuget.space%")
            param("octopus_host", "%octopus.nuget.package.source%")
            param("octopus_packagepaths", "Octopack/*.nupkg")
            param("octopus_publishartifacts", "true")
            param("octopus_forcepush", "false")
            param("secure:octopus_apikey", "zxxbcda8bfdd7ad142f33d136bf3b8bc257638544d9ad4efa1f")
        }
    }

    triggers {
        finishBuildTrigger {
            buildType = "${BuildWebsites.id}"
            successfulOnly = true
        }
    }

    features {
        dockerSupport {
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_2"
            }
        }
    }

    dependencies {
        artifacts(BuildWebsites) {
            buildRule = lastSuccessful()
            cleanDestination = true
            artifactRules = """+:* => Ed-Fi-ODS-Implementation\packages"""
        }
    }
})

object BuildWebsites : BuildType({
    name = "1. Build Ed-Fi ODS Web Apps and Databases"
    description = "Build Web API, Sandbox Admin, Swagger UI, ODS Databases"

    params {
        param("odsapi.build.runSmokeTest", "False")
        param("odsapi.build.runPester", "False")
        param("odsapi.build.runDotnetTest", "False")
        param("env.msbuild_buildConfiguration", "Release")
        param("school.year", "2022")
        param("adminApp.version", "2.2.%build.counter%")
        param("env.Nuget_Source", "%nexus.nuget.package.source%")
        param("odsapi.build.runPostman", "False")
        param("env.Nuget_ApiKey", "%nexus.nuget.api.key%")
    }

    vcs {
        root(SvnHttpEduSvn01educStateMnUsSvnAsdcsTrunkMainV52)

        cleanCheckout = true
    }

    steps {
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

    triggers {
        vcs {
            quietPeriodMode = VcsTrigger.QuietPeriodMode.USE_DEFAULT
            branchFilter = ""
            perCheckinTriggering = true
            groupCheckinsByCommitter = true
            enableQueueOptimization = false
        }
    }
})

object GenerateTemplates : BuildType({
    name = "0. Generate Minimal and Populated Templates"

    params {
        param("odsapi.build.runSmokeTest", "False")
        param("odsapi.build.runPester", "False")
        param("odsapi.build.runPostman", "False")
        param("odsapi.build.runDotnetTest", "False")
        param("env.msbuild_buildConfiguration", "Release")
    }

    vcs {
        root(SvnHttpEduSvn01educStateMnUsSvnAsdcsTrunkMainV52)
    }

    steps {
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

    triggers {
        vcs {
            enabled = false
        }
    }
})

object SvnHttpEduSvn01educStateMnUsSvnAsdcsTrunkMainV52 : SvnVcsRoot({
    name = "svn: http://edu-svn01.educ.state.mn.us/svn/ASDCS/trunk/main-v52"
    url = "http://edu-svn01.educ.state.mn.us/svn/ASDCS/trunk/main-v52"
    userName = """educ\dluser01"""
    password = "zxx7183c3e9953db0c8bf358d36ac3fdd2c"
})
