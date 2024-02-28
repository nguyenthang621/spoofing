pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                script {
                    echo 'Starting the build process...'
                    sh 'cd /var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/ && sudo -S chmod 777 target'
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
        stage('Deploy') {
            steps {
                echo 'Deploying the application...'
                // Assuming your application needs to be run as a background process
                sh 'nohup java -jar /var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/target/inbound-traffic-0.0.1-SNAPSHOT.jar &'
            }
        }
    }
    post {
        always {
            echo 'Cleaning up...'
            sh 'cd /var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/ && /opt/apache-maven-3.9.6/bin/mvn clean'
        }
    }
}
