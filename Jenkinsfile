pipeline {
  agent {
    kubernetes {
      yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: maven:3.9-eclipse-temurin-21
    command: ["sleep"]
    args: ["3600"]
    resources:
      requests: { cpu: "500m", memory: "1Gi" }
      limits: { cpu: "2", memory: "4Gi" }
'''
      defaultContainer 'maven'
    }
  }
  options { timeout(time: 45, unit: 'MINUTES') }
  stages {
    stage('Test & package') {
      steps {
        sh '''
          set -eu
          mvn -B test
          mvn -B -DskipTests package
          java -jar target/langstitch-spring-ai-*-SNAPSHOT-all.jar version
        '''
      }
    }
  }
}
