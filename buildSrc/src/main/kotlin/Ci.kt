object Ci {
    const val org = "io.infinitic.rpc"

    private const val versionBase = "0.0.1"

    val isRelease = System.getenv("RELEASE") != null

    val version = if(isRelease) versionBase else "$versionBase-SNAPSHOT"
}
