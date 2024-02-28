pipeline {
    agent any
    
    stages {
        stage('Build') {
            steps {
                echo 'Starting the build process...'
                sh 'mvn clean package'
            }
        }
        stage('Test') {
            steps {
                echo 'Running tests...'
                sh 'mvn test'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying the application...'
                sh 'java -jar inbound-traffic/target/inbound-traffic-0.0.1-SNAPSHOT.jar'
            }
        }
    }
    post {
        always {
            echo 'Cleaning up...'
            sh 'mvn clean'
        }
    }
}
