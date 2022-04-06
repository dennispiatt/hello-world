package patches.vcsRoots

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.SvnVcsRoot

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the vcsRoot with id = 'SvnHttpEduSvn01educStateMnUsSvnAsdcsTrunkMainV52'
accordingly, and delete the patch script.
*/
changeVcsRoot(RelativeId("SvnHttpEduSvn01educStateMnUsSvnAsdcsTrunkMainV52")) {
    val expected = SvnVcsRoot({
        id("SvnHttpEduSvn01educStateMnUsSvnAsdcsTrunkMainV52")
        name = "svn: http://edu-svn01.educ.state.mn.us/svn/ASDCS/trunk/main-v52"
        url = "http://edu-svn01.educ.state.mn.us/svn/ASDCS/trunk/main-v52"
        userName = """educ\dluser01"""
        password = "zxx7183c3e9953db0c8bf358d36ac3fdd2c"
    })

    check(this == expected) {
        "Unexpected VCS root settings"
    }

    (this as SvnVcsRoot).apply {
        password = "credentialsJSON:193ac447-281f-419d-868f-39a052c659ce"
    }

}
