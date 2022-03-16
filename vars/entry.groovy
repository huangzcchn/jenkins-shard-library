def call(Map config=[:]) {    

    init.Config(config)
    def yaml = new org.devops.kubernetes.Yaml()
    def dingTalk = new org.devops.notificactions.dingTalk()
    def docker = new org.devops.kubernetes.Docker()

    pipeline {
    agent {
        node {
        label Config.data.serviceType
        }
    }

        environment {
            DOCKER_CREDENTIAL_ID = 'harbor-id'
            GITHUB_CREDENTIAL_ID = 'gitlab-id'
            KUBECONFIG_CREDENTIAL_ID = 'kubeconfig'
            DINGTALK_CREDENTIAL_ID = 'dingtalk-access-token'
            REGISTRY = '10.0.5.200:30003'
            IMAGEPULLSECRETS = 'image-harbor-id'
            DOCKERHUB_NAMESPACE = 'kubernetes'
            GITHUB_ACCOUNT = 'kubesphere'
            NAMESPACE = 'dev'
        }

        stages {
            stage ('checkout scm') {
                steps {
                    checkout(scm)
                }
            }

            stage ('init') {
                steps {
                    script {
                        getEnv.env()
                    }
                }
            }

            stage ('build & push') {
                steps {
                    container (Config.data.serviceType) {
                        script {
                            docker.dockerBuild(Config.data.serviceType)
                            docker.dockerPush()
                        }
                    }
                }
            }

            stage('push latest'){
            when{
                branch 'master'
            }
            steps{
                    container (Config.data.serviceType) {
                        script {
                            docker.dockerTag()
                        }    
                    }
            }
            }

            stage('deploy to dev') {
            when{
                branch env.BRANCH_NAME
            }
            steps {
                container (Config.data.serviceType) {
                    withCredentials([
                        kubeconfigFile(
                        credentialsId: env.KUBECONFIG_CREDENTIAL_ID,
                        variable: 'KUBECONFIG')
                        ]) {
                        script {
                            yaml.deploymentYamlBuilder()
                            yaml.serviceYamlBuilder('service.yaml', Config.data.serviceNodePort)
                            yaml.pvcYamlBuilder()
                            sh 'cat deployment.yaml'
                            sh 'envsubst < pvc.yaml | kubectl apply -f -'
                            sh 'envsubst < deployment.yaml | kubectl apply -f -'
                            sh 'envsubst < service.yaml | kubectl apply -f -'
                        }
                    }
                }
            }
            }
        }
        
        post{
            success{
                script{
                    dingTalk.HttpReq(APP_NAME,BUILD_NUMBER,"构建成功 ✅")
                }
            }
            failure{
                script{
                    dingTalk.HttpReq(APP_NAME,BUILD_NUMBER,"构建失败 ❌")
                }
            }
            unstable{
                script{
                    dingTalk.HttpReq(APP_NAME,BUILD_NUMBER,"构建失败 ❌","不稳定异常")
                }
            }
            aborted{
                script{
                    dingTalk.HttpReq(APP_NAME,BUILD_NUMBER,"构建失败 ❌","暂停或中断")
                }
            }
        }
    }
}