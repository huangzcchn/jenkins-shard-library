package org.devops.kubernetes
import java.text.SimpleDateFormat

// 获取时间
def getTime() {
    try {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"))
        return dateFormat.format(new Date())
    } catch (Exception e) {
        System.out.println("e.getMessage()=" +e.getMessage())
    }
}

// 获取提交信息
def getCommit() {
    BRANCH = env.GIT_BRANCH
    COMMIT_ID = env.GIT_COMMIT[0..7]
    COMMIT_USER = sh(returnStdout: true, script: "git log --pretty=format:%an ${env.GIT_COMMIT} -1").trim()
    appname_matcher = "${env.GIT_URL}" =~ "^(.*)/(.*)\\..*"
    appname_matcher.matches()
    APPNAME = appname_matcher.group(2)
    return [BRANCH, COMMIT_ID, COMMIT_USER, APPNAME]
}

// 获取更新记录
def getChangeString() {
    def changeString = ""
    def MAX_MSG_LEN = 20
    def changeLogSets = currentBuild.changeSets
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            truncatedMsg = entry.msg.take(MAX_MSG_LEN)
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"))
            commitTime = dateFormat.format(new Date(Long.parseLong(String.valueOf(entry.timestamp))))
            changeString += " - ${truncatedMsg} [${entry.author} ${commitTime}]\n"
        }
    }
    if (!changeString) {
        changeString = " - No new changes"
    }
    return (changeString)
}