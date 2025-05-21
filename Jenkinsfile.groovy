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


        stage('Build-Sim') {
            steps {
                script {
                }
            }
        }



        stage('CheckRomRamSize') {
            steps {
                // PythonでROM/RAMサイズを抽出
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

        // report.xmlからグラフを生成する！
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