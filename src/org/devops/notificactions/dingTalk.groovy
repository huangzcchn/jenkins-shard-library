package org.devops.notificactions
import org.devops.kubernetes.Commit

def HttpReq(AppName,ImageTag,Status,CatchInfo=' ') {
    withCredentials(
            [string(credentialsId: "$DINGTALK_CREDENTIAL_ID", variable: 'DINGTALK_ACCESS_TOKEN')]
    ){
        def ChangeLog = new Commit().getChangeString()
        def DingTalkHook = "https://oapi.dingtalk.com/robot/send?access_token=" + DINGTALK_ACCESS_TOKEN
        def ReqBody = """{
            "msgtype": "markdown",
            "markdown": {
                "title": "项目构建信息",
                "text": "#### **项目构建信息**\n>- 应用名称: ${AppName}\n>- 构建时间: ${env.START_TIME}\n>- 构建结果: ${Status} ${CatchInfo}\n>- 当前版本: ${env.BRANCH_NAME}-${env.COMMIT_ID}-${ImageTag}\n>- 构建发起: ${env.COMMIT_USER}\n>- 持续时间: ${currentBuild.durationString}\n>- 构建日志: [点击查看详情](${env.BUILD_URL}console)\n##### **更新记录**: \n${ChangeLog}"
            },
            "at": {
                "atUserIds": [
                    "项目小助手"
                ],
                "isAtAll": false
            }
        }"""
        httpRequest acceptType: 'APPLICATION_JSON_UTF8', 
                consoleLogResponseBody: false, 
                contentType: 'APPLICATION_JSON_UTF8', 
                httpMode: 'POST', 
                ignoreSslErrors: true, 
                requestBody: ReqBody, 
                responseHandle: 'NONE', 
                url: "${DingTalkHook}",
                quiet: true
    }
}
