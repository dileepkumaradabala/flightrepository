// Jenkins Declarative Pipeline for the flightreservation Spring Boot app.
// Configure a Maven tool named 'Maven3' in Jenkins:
//   Manage Jenkins -> Tools -> Maven installations -> Add Maven
//   -> Name: Maven3, tick "Install automatically".
pipeline {
    agent any

    tools {
        maven 'Maven3'   // must match the name configured in Manage Jenkins -> Tools
    }

    options {
        timestamps()                       // add timestamps to console log
        buildDiscarder(logRotator(numToKeepStr: '10'))  // keep only the last 10 builds
    }

    stages {
        stage('Checkout') {
            steps {
                // 'checkout scm' uses the repo/branch configured in the Jenkins job.
                checkout scm
            }
        }

        stage('Build & Unit Test') {
            steps {
                // 'mvn test' compiles the code and runs all 32 unit tests.
                // If any test fails, mvn returns a non-zero exit code and the build fails here.
                sh 'mvn clean test'
            }
        }

        stage('Package') {
            steps {
                // Build the runnable JAR. '-DskipTests' because tests already ran above.
                sh 'mvn package -DskipTests'
            }
        }
    }

    post {
        always {
            // Publish the JUnit test report so results show up in the Jenkins UI.
            junit 'target/surefire-reports/*.xml'
            // Save the built JAR as a downloadable build artifact.
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true, fingerprint: true
        }
        success {
            echo 'Build succeeded — all unit tests passed.'
        }
        failure {
            echo 'Build failed — check the console log and test report above.'
        }
    }
}
