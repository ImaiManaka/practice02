pipeline {
    agent any 
    stages {
        stage('pull') {
            steps {
                script {
                    powershell '''
                        @echo off
                        echo "ここでpull"
                        git pull origin main
                        echo "ここでpull完了"
                    '''
                }
            }
        }
    }
}
