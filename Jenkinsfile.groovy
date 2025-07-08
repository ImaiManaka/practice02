pipeline {
    agent any 
    stages {
        stage('pull') {
            steps {
                script {
                    powershell '''
                        echo "ここでpull"
                        git pull origin main
                        echo "ここでpull完了"
                    '''
                }
            }
        }
        
        stage('Build') {
            steps {
                echo 'ビルドステージ（ここでは何もしない）'
            }
        }

        stage('C++ Test') {
            steps {
                echo 'C++テストを実行中...'
                // 意図的に失敗するコマンド
                sh 'exit 1'
            }
            post {
                failure {
                    echo 'C++テストステージが失敗しました'
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'デプロイステージ（テスト失敗時は実行されない）'
            }
        }
    }
}
