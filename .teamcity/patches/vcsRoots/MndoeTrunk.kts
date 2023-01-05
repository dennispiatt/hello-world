package patches.vcsRoots

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.SvnVcsRoot

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the vcsRoot with id = 'MndoeTrunk'
accordingly, and delete the patch script.
*/
changeVcsRoot(RelativeId("MndoeTrunk")) {
    val expected = SvnVcsRoot({
        id("MndoeTrunk")
        name = "mndoe_trunk"
        url = "http://192.168.0.63:8280/svn/mndoe/trunk/"
        userName = "d.piatt"
        password = "credentialsJSON:dba06d21-730a-471d-a498-ed3d067c0db1"
    })

    check(this == expected) {
        "Unexpected VCS root settings"
    }

    (this as SvnVcsRoot).apply {
        url = "http://teamcity-svn-1/svn/mndoe/trunk/"
    }

}
