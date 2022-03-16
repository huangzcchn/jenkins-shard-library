package org.devops

// 构建函数

def build(buildType) {
    def buildTools = [ "maven":"/usr/local/maven", "golang":"/usr/local/golang" ]
    switch(buildType) {
        case 'maven':
            sh '${buildTools["maven"]}/bin/mvn clean package';
            break;
        case 'golang':
            sh '${buildTools["golang"]/bin/go build -o demo.go}';
            break;
        default:
            println('buildType ==> [maven|golang]');
            break;
    }
}