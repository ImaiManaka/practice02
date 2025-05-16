pipeline {
    agent any 

    stages {

        
        stage('init') {
            steps {
                script {
                    prj_name = "CBOX"
                }
            }
        }

        stage('Checkout') {
            steps {
                script {
                    //プロジェクトごとに変更する箇所
                    svn_url = 'http://pct-dev.net/svn/B1RDDEV-0022/trunk/07_ソフトウェア構築(SWC)/eMobi_ControlBox'
                    
                    // エラーフラグの初期化
                    env.ERROR_OCCURRED = 'false' // 環境変数を使用してエラーフラグを初期化
                    
                    try {
                        // SVNからソースコードをチェックアウト
                        checkout([
                            $class: 'SubversionSCM',
                            additionalCredentials: [],
                            excludedCommitMessages: '',
                            excludedRegions: '',
                            excludedRevprop: '',
                            excludedUsers: '',
                            filterChangelog: false,
                            ignoreDirPropChanges: false,
                            includedRegions: '',
                            locations: [[
                                cancelProcessOnExternalsFail: true,
                                credentialsId: '4d937dc3-5126-4367-83b0-2d06cba7fe28',
                                depthOption: 'infinity',
                                ignoreExternalsOption: true,
                                local: '.',
                                remote: svn_url
                            ]],
                            quietOperation: true,
                            workspaceUpdater: [$class: 'UpdateWithRevertUpdater']
                        ])
                    } catch (Exception e) {
                        // エラーが発生した場合、フラグを設定
                        env.ERROR_OCCURRED = 'true'
                        echo "Checkout failed: ${e.message}"
                    }
                }
            }
        }

        stage('Build-IAR') {
            steps {
                script {
                    //プロジェクトごとに変更する箇所
                    IAR_bin_pass = '"Embedded Workbench 9.1\\common\\bin"'//IAR
                    EWARM_pass = env.WORKSPACE+"\\Main\\Target\\ControlBox\\EWARM\\ControlBox.ewp"
                    build_setting = 'ControlBox'
                    catchError(stageResult:'FAILURE') {
                        // エラーフラグを確認
                        if (env.ERROR_OCCURRED == 'true') {
                            echo 'Skipping Build stage due to previous errors.'
                        } else {
                            try {
                                // ビルドステップを追加
                                bat "C:\\ProgramData\\Jenkins\\script\\BuildIAR.bat ${IAR_bin_pass} ${EWARM_pass} ${build_setting}"
                            } catch (Exception e) {
                                // エラーが発生した場合、フラグを設定
                                env.ERROR_OCCURRED = 'true'
                                error  "Build failed: ${e.message}"
                            }
                        }
                    }
                }
            }
        }


        stage('Build-Sim') {
            steps {
                script {
                    //プロジェクトごとに変更する箇所
                    build_sim_pass = "${env.WORKSPACE}\\Main\\sim\\DUsim.vcxproj"
                    
                    // エラーフラグを確認
                    if (env.ERROR_OCCURRED == 'true') {
                        echo 'Skipping Build stage due to previous errors.'
                    } else {
                        try {
                            // ビルドステップを追加
                            bat "C:\\ProgramData\\Jenkins\\script\\BuildSim.bat ${build_sim_pass}"
                        } catch (Exception e) {
                            // エラーが発生した場合、フラグを設定
                            env.ERROR_OCCURRED = 'true'
                            echo "Build failed: ${e.message}"
                        }
                    }
                }
            }
        }



        stage('CheckRomRamSize') {
            steps {
                // PythonでROM/RAMサイズを抽出
                script {
                    //プロジェクトごとに変更する箇所
                    romram_map_pass = "${env.WORKSPACE}\\Main\\Target\\ControlBox\\EWARM\\ControlBox\\List\\ControlBox.map"
                    // エラーフラグを確認
                    if (env.ERROR_OCCURRED == 'true') {
                        echo 'Skipping Build stage due to previous errors.'
                    } else {
                        try {
                            bat """C:\\Users\\JP0800Z_soft01\\AppData\\Local\\Programs\\Python\\Python37\\python C:\\ProgramData\\Jenkins\\script\\extract_bytes.py ${romram_map_pass} ${env.WORKSPACE}\\romram.csv"""
                            
                            plot csvFileName: 'plot-bae285a8-454b-4d01-b0ec-ed48f79cbdba.csv', 
                            csvSeries: [[
                                displayTableFlag: false, 
                                exclusionValues: '', 
                                file: 'romram.csv', 
                                inclusionFlag: 'OFF', 
                                url: ''
                            ]], 
                            group: 'ROMRAMサイズ', 
                            style: 'line'
                        } catch (Exception e) {
                            // エラーが発生した場合の処理
                            env.ERROR_OCCURRED = 'true'
                            echo "エラーが発生しました: ${e.message}"
                        }
                    }
                }
            }
        }

        stage('C++Test@Linux') {
            steps {
                script {
                    if (env.ERROR_OCCURRED == 'true') {
                        echo 'Skipping C++Test@Linux stage due to previous errors.'
                    } else {
                        try {
                            bat 'chcp 65001'
                            bat 'plink -batch -ssh pct_soft@10.75.234.50 -pw Soft_comm -hostkey "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAILEagbN587df7FZUwFCIXJBKdytD2G8jIS7bfMMhmNIj" "$HOME/CI/scripts/CI_all_copy.sh ControlBox ControlBox /home/pct_soft/ControlBox/Main/Target"'
                        } catch (Exception e) {
                            env.ERROR_OCCURRED = 'true'
                            echo "C++Test@Linux failed: ${e.message}"
                        }
                    }
                }
            }
        }

        // report.xmlからグラフを生成する！
        stage('StaticAnaReport') {
            steps {
                script {
                    // プロジェクトごとに変更する箇所
                    // C:\ProgramData\Jenkins\script\replace_stringsTabl.pyも！
                    linux_report_pass = "ControlBox\\cpptest_report\\StaticAnalysis\\report.xml"
                    jenkins__report_pass = "PipelineTestman_CBOX\\staticAna\\report.xml"
                    cpptest_Settings_pass = "${env.WORKSPACE}\\Main\\Target\\ControlBox_cpptest\\ControlBox"
                    
                    if (env.ERROR_OCCURRED == 'true') {
                        echo 'Skipping StaticAnaReport stage due to previous errors.'
                    } else {
                        try {

                            bat "C:\\ProgramData\\Jenkins\\script\\pickup_report.bat ${linux_report_pass} ${jenkins__report_pass} "
                            bat "C:\\Users\\JP0800Z_soft01\\AppData\\Local\\Programs\\Python\\Python37\\python C:\\ProgramData\\Jenkins\\script\\replace_stringsTabl.py ${env.WORKSPACE}\\staticAna\\report.xml ${prj_name}"

                            recordIssues sourceCodeRetention: 'LAST_BUILD', tools: [
                                parasoftFindings(
                                    localSettingsPath: """${cpptest_Settings_pass}\\SettingFiles\\TestConfig\\01.Run Static Analysis.properties""",
                                    pattern: """staticAna\\report.xml""",
                                    reportEncoding: 'UTF-8'
                                )
                            ]
                        } catch (Exception e) {
                            env.ERROR_OCCURRED = 'true'
                            echo "StaticAnaReport failed: ${e.message}"
                        }
                    }
                }
            }
        }

        stage('UnitTestReport') {
            steps {
                script {
                    //プロジェクトごとに変更する箇所
                    linux_unitTest_pass = "\\\\10.75.234.50\\pct_soft\\CI\\ControlBox\\cpptest_report\\UnitTest"
                    
                    if (env.ERROR_OCCURRED == 'true') {
                        echo 'Skipping UnitTestReport stage due to previous errors.'
                    } else {
                        try {
                            bat """C:\\Users\\JP0800Z_soft01\\AppData\\Local\\Programs\\Python\\Python37\\python C:\\ProgramData\\Jenkins\\script\\pickup_report_unittest5.py ${linux_unitTest_pass}"""
                            bat """C:\\Users\\JP0800Z_soft01\\AppData\\Local\\Programs\\Python\\Python37\\python C:\\ProgramData\\Jenkins\\script\\replace_stringsTabl.py ${env.WORKSPACE}\\unitTest\\report.xml ${prj_name}"""
                            xunit (
                                tools: [[$class: 'ParasoftType', pattern: 'unitTest/report.xml']]   
                            )
                        } catch (Exception e) {
                            env.ERROR_OCCURRED = 'true'
                            echo "UnitTestReport failed: ${e.message}"
                        }
                    }
                }
            }
        }

        stage('SendToTeams') {
            steps {
                script {
                    // エラーフラグに基づいてメッセージを送信
                    if (env.ERROR_OCCURRED == 'true') {
                        office365ConnectorSend(
                            webhookUrl: 'https://prod-17.japaneast.logic.azure.com:443/workflows/53f706dd943c420b8317b2b7b908f12b/triggers/manual/paths/invoke?api-version=2016-06-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=RIW9W587HmiHVXU6ksJhg7Qx1RDyIGK_I4H9lodY4D8',
                            message: 'エラーが発生しました。'
                        )
                        // エラーが発生した場合、パイプラインを失敗させる
                        error('ビルド中にエラーが発生しました。')
                    } else {
                        office365ConnectorSend(
                            webhookUrl: 'https://prod-03.japaneast.logic.azure.com:443/workflows/afcdf64c15b24ba29052b1986cb29f56/triggers/manual/paths/invoke?api-version=2016-06-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=9oGz3T_EPcqqvXRSE_eN6mqBaj1hr--Km50gdkklqus',
                            message: 'ビルドが成功しました。'
                        )
                    }
                }
            }
        }

    }
}