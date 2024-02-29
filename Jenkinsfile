// pipeline {
//     agent any
//     stages {
//         stage('Build') {
//             steps {
//                 script {
//                     echo 'Starting the build process...'
//                     sh 'cd /var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/ && /opt/apache-maven-3.9.6/bin/mvn clean install'  // for 10.0.10.153

//                 }
//             }
//         }
//         stage('Test') {
//             steps {
//                 echo 'Running tests...'
//                 sh 'cd /var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/ && /opt/apache-maven-3.9.6/bin/mvn test'
//             }
//         }
//         stage('Deploy') {
//             steps {
//                 echo 'Deploying the application...'
//                 sh 'java -jar /var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/target/inbound-traffic-0.0.1-SNAPSHOT.jar &'
//             }
//         }
        
//     }
//     // post {
//     //     always {
//     //         echo 'Cleaning up...'
//     //         sh 'cd /var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/ && /opt/apache-maven-3.9.6/bin/mvn clean'
//     //     }
//     // }
// }


pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                script {
                    echo 'Starting the build process...'
                    sh 'cd /var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/ && /opt/apache-maven-3.9.6/bin/mvn clean install'
                }
            }
        }
        stage('Test') {
            steps {
                echo 'Running tests...'
                sh 'cd /var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/ && /opt/apache-maven-3.9.6/bin/mvn test'
            }
        }
        stage('Check service inbound is running') {
            steps {
                echo 'Checking service inbound...'
                script {
                    def serviceStatus = sh(script: 'service inbound status', returnStatus: true)
                    if (serviceStatus == 0) {
                        echo 'Service inbound is running.'
                    } else {
                        echo 'Service inbound is not running.'
                    }
                }
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying the application...'
                script {
                    def remote = [:]
                    remote.name = 'spoofing'
                    remote.host = '192.168.10.44'
                    remote.user = 'rnd'
                    remote.password = 'abcd456789'
                    remote.allowAnyHosts = true
                    sshCommand remote: remote, command: "systemctl restart inbound"
                    // check status service
                    def serviceStatus = sh(script: 'service inbound status', returnStatus: true)
                    if (serviceStatus == 0) {
                        echo 'Service inbound start success.'
                    } else {
                        error 'Service inbound start fail.'
                    }
                }
            }
        }
    }
    post {
        always {
            echo 'Checking service inbound...'
            script {
                def serviceStatus = sh(script: 'service inbound status', returnStatus: true)
                if (serviceStatus == 0) {
                    echo 'Service inbound is running.'
                } else {
                    echo 'Service inbound is not running.'
                }
            }
        }
    }
}

