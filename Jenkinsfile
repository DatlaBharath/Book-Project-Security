pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        PAT = credentials('pat-key')
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/DatlaBharath/Book-Project-Security'
            }
        }
        stage('Curl Request') {
            steps {
                script {
                    def response = sh(script: """
                        curl --location "http://microservice-genai.uksouth.cloudapp.azure.com/api/vmsb/pipelines/initscan" \
                        --header "Content-Type: application/json" \
                        --data '{
                            "encrypted_user_id": "gAAAAABn2mbbXK_j224yxwaL7uqlw2tpbxELipeQD2iZCMl7lX0OQbcZAgPP4jIDedBTp81VwlkiCjijrfZDW3QN8MVuc8x92A==",
                            "scanner_id": 1,
                            "target_branch": "main", 
                            "repo_url": "https://github.com/DatlaBharath/Book-Project-Security",
                            "pat": "${PAT}"
                        }'
                    """, returnStdout: true).trim()
                    echo "Curl response: ${response}"
                    
                    def escapedResponse = sh(script: "echo '${response}' | sed 's/\"/\\\\\"/g'", returnStdout: true).trim()
                    def jsonData = "{\"response\": \"${escapedResponse}\"}"
                    def contentLength = jsonData.length()
                    
                    sh """
                    curl -X POST http://ec2-13-201-18-57.ap-south-1.compute.amazonaws.com/app/save-curl-response-jenkins \
                    -H "Content-Type: application/json" \
                    -H "Content-Length: ${contentLength}" \
                    -d '${jsonData}'
                    """
                    
                    def total_vulnerabilities = sh(script: "echo '${response}' | jq -r '.total_vulnerabilites'", returnStdout: true).trim()
                    try {
                        total_vulnerabilities = total_vulnerabilities.toInteger()
                    } catch (Exception e) {
                        echo "Warning: Could not parse total_vulnerabilities as integer: ${total_vulnerabilities}"
                        total_vulnerabilities = -1
                    }

                    if (total_vulnerabilities <= 0) {
                        echo "Success: No vulnerabilities found."
                        env.CURL_STATUS = 'true'
                    } else {
                        echo "Failure: Found ${total_vulnerabilities} vulnerabilities."
                        env.CURL_STATUS = 'false'
                        error("Vulnerabilities found, terminating pipeline.")
                    }
                }
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    def imageName = "ratneshpuskar/book-project-security:${env.BUILD_NUMBER}"
                    sh "docker build -t ${imageName} ."
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub_credentials', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                        sh 'echo ${DOCKER_PASSWORD} | docker login -u ${DOCKER_USERNAME} --password-stdin'
                        def imageName = "ratneshpuskar/book-project-security:${env.BUILD_NUMBER}"
                        sh "docker push ${imageName}"
                    }
                }
            }
        }
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    def deploymentYaml = """
                    apiVersion: apps/v1
                    kind: Deployment
                    metadata:
                      name: book-project-security-deployment
                      labels:
                        app: book-project-security
                    spec:
                      replicas: 1
                      selector:
                        matchLabels:
                          app: book-project-security
                      template:
                        metadata:
                          labels:
                            app: book-project-security
                        spec:
                          containers:
                          - name: book-project-security
                            image: ratneshpuskar/book-project-security:${env.BUILD_NUMBER}
                            ports:
                            - containerPort: 7073
                    """
                    def serviceYaml = """
                    apiVersion: v1
                    kind: Service
                    metadata:
                      name: book-project-security-service
                    spec:
                      selector:
                        app: book-project-security
                      ports:
                      - protocol: TCP
                        port: 7073
                        targetPort: 7073
                        nodePort: 30007
                      type: NodePort
                    """
                    sh """echo "${deploymentYaml}" > deployment.yaml"""
                    sh """echo "${serviceYaml}" > service.yaml"""
                    sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@3.6.238.137 "kubectl apply -f -" < deployment.yaml'
                    sh 'ssh -i /var/test.pem -o StrictHostKeyChecking=no ubuntu@3.6.238.137 "kubectl apply -f -" < service.yaml'
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment was successful'
        }
        failure {
            echo 'Deployment failed'
        }
    }
}