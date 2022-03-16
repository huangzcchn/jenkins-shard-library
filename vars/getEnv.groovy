import org.devops.kubernetes.Commit

def env() {
    def amap = [:]
    def commit = new Commit().getCommit()
    def datetime = new Commit().getTime()
    amap = [COMMIT_BRANCH: commit[0], COMMIT_ID: commit[1], COMMIT_USER: commit[2], APP_NAME: commit[-1], START_TIME: datetime]
    amap.each { k, v ->
        env."${k}" = "${v}"
    }
}