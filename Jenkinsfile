pipeline {
    agent any
    stages {
        // stage('Check Maven') {
        //     steps {
        //         script {
        //             // def mvnHome = tool 'Maven'
        //             // if (mvnHome == null) {
        //                 echo 'Maven is not installed. Downloading...'
        //                 def mvnUrl = 'https://apache.org/dyn/closer.cgi?action=download&filename=maven/maven-3/3.8.4/binaries/apache-maven-3.8.4-bin.tar.gz'
        //                 sh "curl -O $mvnUrl"
        //                 sh 'tar -xvzf apache-maven-3.8.4-bin.tar.gz'
        //                 sh 'export PATH=$PATH:apache-maven-3.8.4/bin'
        //             // }
        //         }
        //     }
        // }
        stage('Build') {
            steps {
                echo 'Starting the build process...'
                sh 'ls'
                sh 'cd /var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/'
                sh '/opt/apache-maven-3.9.6/bin/mvn clean package'
            }
        }
        stage('Test') {
            steps {
                echo 'Running tests...'
                sh 'cd /var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/'
                sh '/opt/apache-maven-3.9.6/bin/mvn test'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying the application...'
                sh 'cd /var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/'
                sh 'java -jar inbound-traffic/target/inbound-traffic-0.0.1-SNAPSHOT.jar'
            }
        }
    }
    post {
        always {
            echo 'Cleaning up...'
            sh 'cd /var/lib/jenkins/workspace/spoofing_sip_main/inbound-traffic/'
            sh '/opt/apache-maven-3.9.6/bin/mvn clean'
        }
    }
}
