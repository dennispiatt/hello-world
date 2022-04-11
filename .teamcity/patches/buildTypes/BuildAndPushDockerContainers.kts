package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.DockerSupportFeature
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.PowerShellStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.powerShell
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'BuildAndPushDockerContainers'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("BuildAndPushDockerContainers")) {
    vcs {
        remove(RelativeId("SvnHttpEduSvn01educStateMnUsSvnAsdcsTrunkMainV52"))
        add(RelativeId("EdFiOdsImplementation"), "+:. => Ed-Fi-ODS-Implementation")
        add(RelativeId("EdFiOdsDocker"), "+:. => Ed-Fi-ODS-Docker")
    }

    expectSteps {
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
            param("octopus_packageformat", "NuPkg")
            param("octopus_packageid", "MN.EdFi.Octopus.Deploy")
            param("octopus_packageoutputpath", "Octopack")
            param("octopus_packagesourcepath", "Ed-Fi-Ods-Docker/Octopus")
            param("octopus_packageversion", "%odsapi.build.package.webApi.version%")
            param("octopus_publishartifacts", "true")
        }
        step {
            name = "Octopush IaC to Octopus package repo"
            type = "octopus.push.package"
            param("octopus_forcepush", "false")
            param("octopus_host", "%octopus.nuget.package.source%")
            param("octopus_packagepaths", "Octopack/*.nupkg")
            param("octopus_publishartifacts", "true")
            param("octopus_space_name", "%octopus.nuget.space%")
            param("secure:octopus_apikey", "zxxbcda8bfdd7ad142f33d136bf3b8bc257638544d9ad4efa1f")
        }
    }
    steps {
        update<PowerShellStep>(1) {
            clearConditions()
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
                    ${'$'}url = "%mn-mde-edfi.nexus.host%/repository/edfi-raw/ssl.zip"
                    
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
        update<BuildStep>(5) {
            clearConditions()
            param("secure:octopus_apikey", "credentialsJSON:e6aacf31-740c-42fb-831f-2257142b455a")
        }
    }

    features {
        val feature1 = find<DockerSupportFeature> {
            dockerSupport {
                loginToRegistry = on {
                    dockerRegistryId = "PROJECT_EXT_2"
                }
            }
        }
        feature1.apply {
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_5"
            }
        }
    }

    dependencies {
        expect(RelativeId("BuildWebsites")) {
            artifacts {
                buildRule = lastSuccessful()
                cleanDestination = true
                artifactRules = """+:* => Ed-Fi-ODS-Implementation\packages"""
            }
        }
        update(RelativeId("BuildWebsites")) {
            snapshot {
            }

            artifacts {
                buildRule = lastSuccessful()
                cleanDestination = true
                artifactRules = """+:* => Ed-Fi-ODS-Implementation\packages"""
            }
        }

    }
}
