import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.DotnetMsBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.dotnetMsBuild
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.nuGetInstaller
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.nuGetPack
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.nuGetPublish
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.powerShell
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.dockerRegistry
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.nuGetFeed
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.VcsTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.finishBuildTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot
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

    vcsRoot(EdFiOdsDocker)
    vcsRoot(EdFiOdsAdminApp)
    vcsRoot(MndoeTrunk)
    vcsRoot(EdFiOdsImplementation)
    vcsRoot(EdFiStandard)
    vcsRoot(EdFiOdsLegacy)
    vcsRoot(LegacyEdFiOdsImplementation)
    vcsRoot(HttpsGithubComMnMdeEdfiEdFiOdsRefsHeadsDevelopMnV31)
    vcsRoot(MainV52)
    vcsRoot(KeyretrievalV52)

    buildType(Test)
    buildType(BuildWebsites)
    buildType(BuildAndPushDockerContainers)
    buildType(id3BuildKeyRetrievalWebApp)
    buildType(GenerateTemplates)

    params {
        param("SemVerVersion", "1.0.0")
        param("MinorPackageVersion", "2")
        param("odsapi.build.runPester", "false")
        param("MajorPackageVersion", "5")
        param("school.year", "2023")
        param("DefaultTrunkName", "main-v52")
        param("odsapi.build.package.webApi.version", "%PackageVersion%")
        param("odsapi.build.package.sandboxAdmin.version", "%PackageVersion%")
        param("octopus.nuget.space", "Spaces-1")
        text("TrunkName", "%DefaultTrunkName%", allowEmpty = true)
        param("PatchPackageVersion", "%build.counter%")
        param("nexus.nuget.package.source", "%mn-mde-edfi.nexus.host%/repository/Ed-Fi/")
        param("odsapi.build.package.webApi.id", "MN.EdFi.Ods.WebApi")
        param("odsapi.build.runDotnetTest", "false")
        param("octopus.nuget.apikey", "%OctopusAPIKey%")
        param("nexus.docker.feed", "%mn-mde-edfi.nexus.host%/repository/edfi-docker/")
        param("octopus.nuget.package.source", "%OctopusServer%")
        param("odsapi.build.odsTokens", "2023")
        param("nexus.nuget.username", "EdFi-Admin")
        param("odsapi.build.package.sandboxAdmin.id", "MN.EdFi.Ods.SandboxAdmin")
        param("env.MNIT_TeamCity_Build", "true")
        param("env.msbuild_buildConfiguration", "Release")
        param("teamcity.build.branch", "%TrunkName%")
        param("odsapi.build.package.databases.version", "%PackageVersion%")
        param("odsapi.build.package.swaggerUI.version", "%PackageVersion%")
        param("odsapi.build.runPostman", "false")
        param("odsapi.build.package.databases.id", "MN.EdFi.RestApi.Databases")
        param("odsapi.build.runSmokeTest", "false")
        param("nexus.nuget.password", "Q!W@E#q1w2e3")
        param("odsapi.build.package.swaggerUI.id", "MN.EdFi.Ods.SwaggerUI")
        param("odsapi.build.noDeploy", "false")
        param("odsapi.build.installType", "yearspecific")
        param("nexus.nuget.api.key", "%mn-mde-edfi.nexus.nuget.apikey%")
    }

    features {
        dockerRegistry {
            id = "PROJECT_EXT_2"
            name = "Nexus"
            url = "http://edu-dockeru01.educ.state.mn.us:8081/repository/EdFi-Docker/"
            userName = "EdFi-Admin"
            password = "credentialsJSON:af64153b-959a-4cf6-ae87-8a04392392d9"
        }
        nuGetFeed {
            id = "repository-nuget-MDE_Nexus_EdFi_Repo"
            name = "MDE_Nexus_EdFi_Repo"
            description = "Internal Nexus Repository"
            indexPackages = true
        }
    }
}

object id3BuildKeyRetrievalWebApp : BuildType({
    id("3BuildKeyRetrievalWebApp")
    name = "3. Build Key Retrieval Web App"

    params {
        param("keyRetrieval.version", "5.2.%build.counter%")
    }

    vcs {
        root(KeyretrievalV52)
    }

    steps {
        nuGetInstaller {
            toolPath = "%teamcity.tool.NuGet.CommandLine.DEFAULT%"
            projects = "Ed-Fi-ODS-Implementation/Application/EdFi.Ods.SecurityConfiguration.KeyRetrieval.sln"
        }
        dotnetMsBuild {
            name = "Build Key Retrieval Web Site"
            projects = """Ed-Fi-ODS-Implementation\Application\EdFi.Ods.SecurityConfiguration.KeyRetrieval.sln"""
            version = DotnetMsBuildStep.MSBuildVersion.V16
            configuration = "%buildConfiguration%"
            runtime = "win"
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
        nuGetPack {
            name = "Package Key Retrieval Site"
            toolPath = "%teamcity.tool.NuGet.CommandLine.DEFAULT%"
            paths = """
                Ed-Fi-ODS-Implementation/Application/SecurityConfigurationTool
                Ed-Fi-ODS-Implementation/Application/SecurityConfigurationTool/EdFi.Ods.SecurityConfiguration.KeyRetrieval.Web/EdFi.Ods.SecurityConfiguration.KeyRetrieval.Web.nuspec
            """.trimIndent()
            version = "%keyRetrieval.version%"
            outputDir = "packages"
            cleanOutputDir = true
            publishPackages = true
            properties = "Configuration=%buildConfiguration%"
        }
        nuGetPublish {
            name = "Publish NuGet packages to Nexus"
            toolPath = "%teamcity.tool.NuGet.CommandLine.DEFAULT%"
            packages = """packages\*.nupkg"""
            serverUrl = "%nexus.nuget.package.source%"
            apiKey = "credentialsJSON:b5e78adb-405c-481e-ab62-4af7b6635952"
        }
    }
})

object BuildAndPushDockerContainers : BuildType({
    name = "2. Build and push Docker images"
    description = "Builds docker containers for Ed-Fi websites"

    buildNumberPattern = "${BuildWebsites.depParamRefs["build.counter"]}"

    params {
        param("PackageVersion", "${BuildWebsites.depParamRefs["PackageVersion"]}")
        param("TrunkName", "${BuildWebsites.depParamRefs["TrunkName"]}")
        param("teamcity.build.branch", "${BuildWebsites.depParamRefs["teamcity.build.branch"]}")
    }

    vcs {
        root(MndoeTrunk, "+:%TrunkName%=>.")

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
                    
                    ${'$'}tagLatest = "latest"
                    ${'$'}tag = "%build.counter%"
                    if ("%TrunkName%" -ne "%DefaultTrunkName%")
                    {
                    	${'$'}tag = "%PackageVersion%"
                        ${'$'}tagLatest = ${'$'}tag.split("-",2)[-1]
                    }
                    
                    Write-Host "retrieve ssl zip"
                    
                    ${'$'}User = "%nexus.nuget.username%"
                    ${'$'}PWord = ConvertTo-SecureString -String "%nexus.nuget.password%" -AsPlainText -Force
                    ${'$'}Credential = New-Object -TypeName System.Management.Automation.PSCredential -ArgumentList ${'$'}User, ${'$'}PWord
                    ${'$'}url = "%mn-mde-edfi.nexus.host%/repository/edfi-raw/ssl.zip"
                    
                    Invoke-WebRequest -Uri ${'$'}url -Credential ${'$'}Credential -AllowUnencryptedAuthentication -OutFile ./Web-Ods-AdminApp/Alpine/mssql/ssl.zip  
                    
                    ${'$'}imagesToBuild = @{
                        "ods-api-web-admin-app" = "./Web-Ods-AdminApp/Alpine/mssql"
                        "ods-api-web-api"="./Web-Ods-Api/Alpine/mssql"
                        "ods-api-web-sandbox-admin"="./Web-Sandbox-Admin/Alpine/mssql"
                        "ods-api-web-swagger-ui"="./Web-SwaggerUI/Alpine"
                        }
                    
                    foreach(${'$'}imageName in ${'$'}imagesToBuild.Keys) {
                        Write-Host "docker ${'$'}imageName"
                        docker build -t ${'$'}imageName ${'$'}imagesToBuild[${'$'}imageName]
                        if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                        docker tag ${'$'}imageName ${'$'}repo/${'$'}(${'$'}imageName).%school.year%:${'$'}tag
                        if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                        docker tag ${'$'}imageName ${'$'}repo/${'$'}(${'$'}imageName):${'$'}tagLatest
                        if (${'$'}error.count -gt 0 -or ${'$'}LASTEXITCODE -gt 0) { exit 1; }
                        docker save ${'$'}repo/${'$'}(${'$'}imageName):${'$'}tagLatest -o "${'$'}(${'$'}imageName).tar"
                    }
                    
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
                    ${'$'}destPath = "%mn-mde-edfi.nexus.host%/repository/edfi-raw/"
                    if ("%DefaultTrunkName%" -ne "%TrunkName%") {
                    	${'$'}packageVersion = "%PackageVersion%"
                    	${'$'}channel = ${'$'}packageVersion.split("-",2)[-1]
                        ${'$'}destPath = ${'$'}destPath + "${'$'}channel/" 
                    }
                    Get-ChildItem *.tar | ForEach-Object { 
                        write-host "uploading ${'$'}_"
                        curl --user "${'$'}("%nexus.nuget.username%"):${'$'}("%nexus.nuget.password%")" --upload-file "${'$'}_" ${'$'}destPath 2>&1
                    }
                """.trimIndent()
            }
        }
        step {
            name = "Octopack Docker IaC"
            type = "octopus.pack.package"
            param("octopus_packageoutputpath", "Octopack")
            param("octopus_packageid", "MN.EdFi.Octopus.Deploy")
            param("octopus_packageversion", "%PackageVersion%")
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
            param("octopus_forcepush", "true")
            param("secure:octopus_apikey", "credentialsJSON:e6aacf31-740c-42fb-831f-2257142b455a")
        }
    }

    triggers {
        finishBuildTrigger {
            buildType = "${BuildWebsites.id}"
            successfulOnly = true
            branchFilter = "+:*"
        }
    }

    features {
        dockerSupport {
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_5"
            }
        }
    }

    dependencies {
        dependency(BuildWebsites) {
            snapshot {
            }

            artifacts {
                cleanDestination = true
                artifactRules = """+:* => Ed-Fi-ODS-Implementation\packages"""
            }
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
        param("adminApp.version", "%adminApp.MajorVersion%.%adminApp.MinorVersion%.%build.counter%")
        param("env.Nuget_Source", "%nexus.nuget.package.source%")
        param("odsapi.build.runPostman", "False")
        param("env.Nuget_ApiKey", "%nexus.nuget.api.key%")
        param("adminApp.MajorVersion", "2")
        param("adminApp.MinorVersion", "2")
    }

    vcs {
        root(MndoeTrunk, "+:%TrunkName%/Ed-Fi-ODS => Ed-Fi-ODS", "+:%TrunkName%/Ed-Fi-ODS-AdminApp => Ed-Fi-ODS-AdminApp", "+:%TrunkName%/Ed-Fi-Ods-Docker => Ed-Fi-Ods-Docker", "+:%TrunkName%/Ed-Fi-ODS-Implementation => Ed-Fi-ODS-Implementation", "+:%TrunkName%/Ed-Fi-Standard => Ed-Fi-Standard")

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
        step {
            name = "Calculate admin app version"
            type = "CalculatePackageVersion32_svn"
            param("cpv.PackageVersion", "%adminApp.version%")
            param("cpv.MajorVersion", "%adminApp.MajorVersion%")
            param("cpv.MinorVersion", "%adminApp.MinorVersion%")
        }
        powerShell {
            name = "Build Ed-Fi ODS Admin App"
            formatStderrAsError = true
            workingDir = "Ed-Fi-ODS-AdminApp"
            scriptMode = script {
                content = """
                    .\build.ps1 -Version "%PackageVersion%" -BuildCounter %build.counter% -Command Build -Configuration Release
                    .\build.ps1 -Command UnitTest -Configuration Release
                    .\build.ps1 -Version "%PackageVersion%" -BuildCounter %build.counter% -Command Package -Configuration Release
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
            name = "Add Nuget Source for Codegen Package"
            formatStderrAsError = true
            workingDir = "Ed-Fi-ODS-Implementation"
            scriptMode = script {
                content = "& dotnet nuget update source github -u %teamcity.github.user% -p %teamcity.github.personalAccessToken% --store-password-in-clear-text --configfile ./NuGet.Config"
            }
            param("jetbrains_powershell_script_file", "")
        }
        step {
            name = "Calculate api version"
            type = "CalculatePackageVersion32_svn"
        }
        powerShell {
            name = "Build Ed-Fi ODS API websites and databases"
            formatStderrAsError = true
            workingDir = "Ed-Fi-ODS-Implementation"
            scriptMode = file {
                path = "build.teamcity.ps1"
            }
            param("jetbrains_powershell_script_code", "")
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
            apiKey = "credentialsJSON:b5e78adb-405c-481e-ab62-4af7b6635952"
        }
    }

    triggers {
        vcs {
            quietPeriodMode = VcsTrigger.QuietPeriodMode.USE_DEFAULT
            branchFilter = ""
        }
    }

    requirements {
        equals("env.OS", "Windows_NT")
    }
})

object GenerateTemplates : BuildType({
    name = "0. Generate Minimal and Populated Templates"

    buildNumberPattern = "%PackageVersion%"

    params {
        param("odsapi.build.runSmokeTest", "False")
        param("odsapi.build.runPester", "False")
        param("odsapi.build.runPostman", "False")
        param("odsapi.build.runDotnetTest", "False")
        param("env.msbuild_buildConfiguration", "Release")
    }

    vcs {
        root(MndoeTrunk, "+:%TrunkName%=>.")
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
            name = "Add Nuget Source for Codegen Package"
            formatStderrAsError = true
            workingDir = "Ed-Fi-ODS-Implementation"
            scriptMode = script {
                content = "& dotnet nuget update source github -u %teamcity.github.user% -p %teamcity.github.personalAccessToken% --store-password-in-clear-text --configfile ./NuGet.Config"
            }
            param("jetbrains_powershell_script_file", "")
        }
        step {
            type = "CalculatePackageVersion32_svn"
            param("cpv.MajorVersion", "%build.counter%")
            param("cpv.PatchVersion", "0")
            param("cpv.MinorVersion", "0")
        }
        powerShell {
            name = "Build Database Templates"
            formatStderrAsError = true
            workingDir = "Ed-Fi-ODS-Implementation"
            scriptMode = file {
                path = "buildDatabaseTemplates.ps1"
            }
            noProfile = false
            param("jetbrains_powershell_scriptArguments", "-version %PackageVersion%")
        }
        nuGetPublish {
            name = "Publish NuGet packages to Nexus"
            toolPath = "%teamcity.tool.NuGet.CommandLine.DEFAULT%"
            packages = """Ed-Fi-ODS-Implementation\packages\*.nupkg"""
            serverUrl = "%nexus.nuget.package.source%"
            apiKey = "credentialsJSON:b5e78adb-405c-481e-ab62-4af7b6635952"
            args = "-Timeout 6000"
        }
    }

    triggers {
        vcs {
            enabled = false
        }
    }
})

object Test : BuildType({
    name = "test"
    description = "Build Web API, Sandbox Admin, Swagger UI, ODS Databases"

    params {
        param("odsapi.build.runSmokeTest", "False")
        param("odsapi.build.runPester", "False")
        param("odsapi.build.runDotnetTest", "False")
        param("school.year", "2022")
        param("adminApp.version", "%adminApp.MajorVersion%.%adminApp.MinorVersion%.%build.counter%")
        param("env.Nuget_Source", "%nexus.nuget.package.source%")
        param("odsapi.build.runPostman", "False")
        param("env.Nuget_ApiKey", "%nexus.nuget.api.key%")
        param("adminApp.MajorVersion", "2")
        param("adminApp.MinorVersion", "2")
    }

    vcs {
        root(MndoeTrunk, "+:%TrunkName%/Ed-Fi-ODS => Ed-Fi-ODS", "+:%TrunkName%/Ed-Fi-ODS-AdminApp => Ed-Fi-ODS-AdminApp", "+:%TrunkName%/Ed-Fi-Ods-Docker => Ed-Fi-Ods-Docker", "+:%TrunkName%/Ed-Fi-ODS-Implementation => Ed-Fi-ODS-Implementation", "+:%TrunkName%/Ed-Fi-Standard => Ed-Fi-Standard")
    }

    steps {
        step {
            name = "Calculate admin app version"
            type = "CalculatePackageVersion32_svn"
            param("cpv.PackageVersion", "%adminApp.version%")
            param("cpv.MajorVersion", "%adminApp.MajorVersion%")
            param("cpv.Channel", "%Channel%")
            param("cpv.MinorVersion", "%adminApp.MinorVersion%")
        }
        step {
            name = "Calculate api version"
            type = "CalculatePackageVersion32_svn"
            param("cpv.Channel", "%Channel%")
        }
        powerShell {
            scriptMode = script {
                content = """write-host "##teamcity[setParameter name='ddd' value='fff']""""
            }
        }
    }

    requirements {
        equals("env.OS", "Windows_NT")
    }
})

object EdFiOdsAdminApp : GitVcsRoot({
    name = "Ed-Fi-ODS-AdminApp"
    url = "https://github.com/mn-mde-edfi/Ed-Fi-ODS-AdminApp"
    branch = "refs/heads/main-v221"
    authMethod = password {
        userName = "dennispiatt"
        password = "credentialsJSON:0f12473a-0eb1-4d19-b397-866c46491400"
    }
})

object EdFiOdsDocker : GitVcsRoot({
    name = "Ed-Fi-ODS-Docker"
    url = "https://github.com/mn-mde-edfi/Ed-Fi-ODS-Docker"
    branch = "refs/heads/MOAI-912-ChannelDeploy"
    branchSpec = "+:refs/heads/(MOAI-784-2)"
    authMethod = password {
        userName = "dennispiatt"
        password = "credentialsJSON:0f12473a-0eb1-4d19-b397-866c46491400"
    }
})

object EdFiOdsImplementation : GitVcsRoot({
    name = "Ed-Fi-ODS-Implementation"
    url = "https://github.com/mn-mde-edfi/Ed-Fi-ODS-Implementation"
    branch = "refs/heads/UpdateFromSvn"
    authMethod = password {
        userName = "dennispiatt"
        password = "credentialsJSON:0f12473a-0eb1-4d19-b397-866c46491400"
    }
})

object EdFiOdsLegacy : GitVcsRoot({
    name = "Legacy-Ed-Fi-ODS"
    url = "https://github.com/mn-mde-edfi/Ed-Fi-ODS"
    branch = "refs/heads/keyretrieval-v52"
    useTagsAsBranches = true
    serverSideAutoCRLF = true
    authMethod = password {
        userName = "dennispiatt"
        password = "credentialsJSON:0f12473a-0eb1-4d19-b397-866c46491400"
    }
})

object EdFiStandard : GitVcsRoot({
    name = "Ed-Fi-Standard"
    url = "https://github.com/mn-mde-edfi/Ed-Fi-Standard"
    branch = "refs/heads/main-v33a"
    branchSpec = "+:refs/heads/(updatesFromSvn)"
    authMethod = password {
        userName = "dennispiatt"
        password = "credentialsJSON:0f12473a-0eb1-4d19-b397-866c46491400"
    }
})

object HttpsGithubComMnMdeEdfiEdFiOdsRefsHeadsDevelopMnV31 : GitVcsRoot({
    name = "Ed-Fi-ODS"
    url = "https://github.com/mn-mde-edfi/Ed-Fi-ODS"
    branch = "refs/heads/UpdateFromSvn"
    authMethod = password {
        userName = "dennispiatt"
        password = "credentialsJSON:0f12473a-0eb1-4d19-b397-866c46491400"
    }
})

object KeyretrievalV52 : SvnVcsRoot({
    name = "keyretrieval-v52"
    url = "http://192.168.0.62:8280/svn/mndoe/trunk/keyretrival-v52/"
})

object LegacyEdFiOdsImplementation : GitVcsRoot({
    name = "Legacy-Ed-Fi-ODS-Implementation"
    url = "https://github.com/mn-mde-edfi/Ed-Fi-ODS-Implementation"
    branch = "refs/heads/keyretrieval-v52"
    useTagsAsBranches = true
    serverSideAutoCRLF = true
    authMethod = password {
        userName = "dennispiatt"
        password = "credentialsJSON:0f12473a-0eb1-4d19-b397-866c46491400"
    }
})

object MainV52 : SvnVcsRoot({
    name = "main-v52"
    url = "http://192.168.0.62:8280/svn/mndoe/trunk/main-v52"
    userName = "d.piatt"
    password = "credentialsJSON:dba06d21-730a-471d-a498-ed3d067c0db1"
})

object MndoeTrunk : SvnVcsRoot({
    name = "mndoe_trunk"
    url = "http://192.168.0.63:8280/svn/mndoe/trunk/"
    userName = "d.piatt"
    password = "credentialsJSON:dba06d21-730a-471d-a498-ed3d067c0db1"
})
