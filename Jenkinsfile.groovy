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
        stage('Build-IAR') {
            steps {
                script {
                }
            }
        }

        stage('Build-Sim') {
            steps {
                script {
                }
            }
        }

        stage('CheckRomRamSize') {
            steps {
                script {
                }
            }
        }

        stage('C++Test@Linux') {
            steps {
                script {
                    }
                }
            }
        }

        stage('StaticAnaReport') {
            steps {
                script {
                }
            }
        }

        stage('UnitTestReport') {
            steps {
                script {
                }
            }
        }

        stage('SendToTeams') {
            steps {
                script {
                }
            }
        }
    }
}