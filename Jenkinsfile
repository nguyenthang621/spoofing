pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                script {
                    echo 'Starting the build process...'
                    // Thực hiện các lệnh trong thư mục dự án
                    dir('/var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/') {
                        // Hiển thị danh sách tệp trong thư mục dự án
                        sh 'chmod 777 target'
                        sh 'ls'
                        // Thực hiện lệnh clean và install Maven
                        sh '/opt/apache-maven-3.9.6/bin/mvn clean install'
                    }
                }
            }
        }
        stage('Test') {
            steps {
                echo 'Running tests...'
                // Thực hiện các lệnh trong thư mục dự án
                dir('/var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/') {
                    // Thực hiện lệnh test Maven
                    sh '/opt/apache-maven-3.9.6/bin/mvn test'
                }
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying the application...'
                // Thực hiện các lệnh trong thư mục dự án
                dir('/var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/') {
                    // Chạy ứng dụng từ tệp JAR
                    sh 'java -jar target/inbound-traffic-0.0.1-SNAPSHOT.jar'
                }
            }
        }
    }
    post {
        always {
            echo 'Cleaning up...'
            // Thực hiện các lệnh trong thư mục dự án
            dir('/var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/') {
                // Thực hiện lệnh clean Maven
                sh '/opt/apache-maven-3.9.6/bin/mvn clean'
            }
        }
    }
}
