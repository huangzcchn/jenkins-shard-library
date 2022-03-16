package org.devops.kubernetes

def dockerBuild(String buildType) {
    switch(buildType) {
        case "maven":
            sh 'mvn clean package -DskipTests';
            break;
        case "nodejs":
            sh 'npm config set registry=http://registry.npm.taobao.org && npm install && npm run build:dev'
        default: 
            println("The value is unknown"); 
            break;
    }
    sh 'docker build -f Dockerfile -t $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:$BRANCH_NAME-$BUILD_NUMBER .'
}

def dockerPush() {
    withCredentials([usernamePassword(passwordVariable : 'DOCKER_PASSWORD' ,usernameVariable : 'DOCKER_USERNAME' ,credentialsId : "$DOCKER_CREDENTIAL_ID" ,)]) {
                        sh '''
                        echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin
                        docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:$BRANCH_NAME-$BUILD_NUMBER
                        '''
                    }
}

def dockerTag() {
    sh '''
    docker tag $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest
    docker push $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest
    '''
}