pipeline {
    agent any 
    stages {
        stage('pull') {
            steps {
                script {
                    powershell 'git pull'
                }
            }
        }
    }
}
