pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9.0'  // Configure in Jenkins Global Tools
        nodejs 'NodeJS-18'   // Configure in Jenkins Global Tools
        dockerTool 'Docker'  // Configure in Jenkins Global Tools
    }
    
    environment {
        // Docker Registry Configuration
        DOCKER_REGISTRY = credentials('docker-registry-url') // Configure in Jenkins credentials
        DOCKER_CREDENTIALS = credentials('docker-registry-credentials') // Docker Hub or private registry credentials
        
        // Database Configuration for Tests
        TEST_DB_URL = 'jdbc:h2:mem:testdb'
        
        // Application Versions
        APP_VERSION = "${env.BUILD_NUMBER}"
        
        // Service Names
        USER_SERVICE = 'user-service'
        TICKET_SERVICE = 'ticket-service'
        SUBSCRIPTION_SERVICE = 'subscription-service'
        API_GATEWAY = 'api-gateway'
        FRONTEND_APP = 'frontend-app'
        
        // Environment Variables
        MAVEN_OPTS = '-Dmaven.test.failure.ignore=false'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
                script {
                    // Set build name with version
                    currentBuild.displayName = "#${env.BUILD_NUMBER}-v${APP_VERSION}"
                }
            }
        }
        
        stage('Environment Setup') {
            steps {
                script {
                    echo "Setting up build environment..."
                    echo "Maven version: ${sh(script: 'mvn --version', returnStdout: true)}"
                    echo "Node.js version: ${sh(script: 'node --version', returnStdout: true)}"
                    echo "Docker version: ${sh(script: 'docker --version', returnStdout: true)}"
                }
            }
        }
        
        stage('Code Quality & Security') {
            parallel {
                stage('Backend Code Analysis') {
                    steps {
                        dir('soa/mymicroservices') {
                            echo 'Running static code analysis for backend...'
                            sh '''
                                # Run Maven checkstyle and spotbugs
                                mvn clean compile checkstyle:check spotbugs:check -DskipTests=true
                            '''
                        }
                    }
                }
                
                stage('Frontend Code Analysis') {
                    steps {
                        dir('soa/mymicroservices/Frontend') {
                            echo 'Installing frontend dependencies and running linting...'
                            sh '''
                                npm ci
                                npm run lint
                            '''
                        }
                    }
                }
                
                stage('Security Scan') {
                    steps {
                        dir('soa/mymicroservices') {
                            echo 'Running security vulnerability scan...'
                            sh '''
                                # Run OWASP dependency check
                                mvn org.owasp:dependency-check-maven:check
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Build & Test') {
            parallel {
                stage('Backend Services Build') {
                    steps {
                        dir('soa/mymicroservices') {
                            echo 'Building backend microservices...'
                            sh '''
                                # Clean and compile all services
                                mvn clean compile -DskipTests=true
                                
                                # Build each service individually to ensure isolation
                                echo "Building User Service..."
                                cd User && mvn package -DskipTests=true && cd ..
                                
                                echo "Building Ticket Service..."
                                cd Ticket && mvn package -DskipTests=true && cd ..
                                
                                echo "Building Subscription Service..."
                                cd Subscription && mvn package -DskipTests=true && cd ..
                                
                                echo "Building API Gateway..."
                                cd api-gateway && mvn package -DskipTests=true && cd ..
                            '''
                        }
                    }
                }
                
                stage('Frontend Build') {
                    steps {
                        dir('soa/mymicroservices/Frontend') {
                            echo 'Building frontend application...'
                            sh '''
                                npm ci
                                CI=false npm run build
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Unit Tests') {
            parallel {
                stage('Backend Tests') {
                    steps {
                        dir('soa/mymicroservices') {
                            echo 'Running backend unit tests...'
                            sh '''
                                # Run tests for all services
                                mvn test -Dspring.profiles.active=test
                            '''
                        }
                        
                        // Publish test results
                        publishTestResults testResultsPattern: '**/target/surefire-reports/*.xml'
                        
                        // Publish coverage reports
                        publishCoverage adapters: [jacocoAdapter('**/target/site/jacoco/jacoco.xml')],
                                        sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
                    }
                }
                
                stage('Frontend Tests') {
                    steps {
                        dir('soa/mymicroservices/Frontend') {
                            echo 'Running frontend unit tests...'
                            sh '''
                                CI=true npm test -- --coverage --watchAll=false
                            '''
                        }
                        
                        // Publish frontend test results
                        publishTestResults testResultsPattern: 'soa/mymicroservices/Frontend/coverage/lcov-report/*.html'
                    }
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                dir('soa/mymicroservices') {
                    echo 'Starting infrastructure for integration tests...'
                    sh '''
                        # Start test database and required services
                        docker-compose -f docker-compose.yml up -d postgres redis
                        
                        # Wait for services to be ready
                        sleep 30
                        
                        # Run integration tests
                        mvn verify -Dspring.profiles.active=integration-test
                    '''
                }
            }
            post {
                always {
                    dir('soa/mymicroservices') {
                        echo 'Cleaning up integration test environment...'
                        sh 'docker-compose down'
                    }
                }
            }
        }
        
        stage('Build Docker Images') {
            parallel {
                stage('Build User Service Image') {
                    steps {
                        dir('soa/mymicroservices/User') {
                            script {
                                def userImage = docker.build("${USER_SERVICE}:${APP_VERSION}")
                                userImage.tag("${USER_SERVICE}:latest")
                            }
                        }
                    }
                }
                
                stage('Build Ticket Service Image') {
                    steps {
                        dir('soa/mymicroservices/Ticket') {
                            script {
                                def ticketImage = docker.build("${TICKET_SERVICE}:${APP_VERSION}")
                                ticketImage.tag("${TICKET_SERVICE}:latest")
                            }
                        }
                    }
                }
                
                stage('Build Subscription Service Image') {
                    steps {
                        dir('soa/mymicroservices/Subscription') {
                            script {
                                def subscriptionImage = docker.build("${SUBSCRIPTION_SERVICE}:${APP_VERSION}")
                                subscriptionImage.tag("${SUBSCRIPTION_SERVICE}:latest")
                            }
                        }
                    }
                }
                
                stage('Build API Gateway Image') {
                    steps {
                        dir('soa/mymicroservices/api-gateway') {
                            script {
                                def gatewayImage = docker.build("${API_GATEWAY}:${APP_VERSION}")
                                gatewayImage.tag("${API_GATEWAY}:latest")
                            }
                        }
                    }
                }
                
                stage('Build Frontend Image') {
                    steps {
                        dir('soa/mymicroservices/Frontend') {
                            script {
                                def frontendImage = docker.build("${FRONTEND_APP}:${APP_VERSION}")
                                frontendImage.tag("${FRONTEND_APP}:latest")
                            }
                        }
                    }
                }
            }
        }
        
        stage('Container Security Scan') {
            parallel {
                stage('Scan User Service') {
                    steps {
                        echo 'Scanning User Service container for vulnerabilities...'
                        sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image ${USER_SERVICE}:${APP_VERSION}"
                    }
                }
                
                stage('Scan Ticket Service') {
                    steps {
                        echo 'Scanning Ticket Service container for vulnerabilities...'
                        sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image ${TICKET_SERVICE}:${APP_VERSION}"
                    }
                }
                
                stage('Scan Frontend App') {
                    steps {
                        echo 'Scanning Frontend container for vulnerabilities...'
                        sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image ${FRONTEND_APP}:${APP_VERSION}"
                    }
                }
            }
        }
        
        stage('End-to-End Tests') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                dir('soa/mymicroservices') {
                    echo 'Running end-to-end tests...'
                    sh '''
                        # Start full application stack
                        docker-compose up -d
                        
                        # Wait for all services to be ready
                        sleep 60
                        
                        # Run E2E tests (you can add Cypress, Selenium, etc.)
                        echo "E2E tests would run here..."
                        # npm run e2e:test
                    '''
                }
            }
            post {
                always {
                    dir('soa/mymicroservices') {
                        echo 'Cleaning up E2E test environment...'
                        sh 'docker-compose down'
                    }
                }
            }
        }
        
        stage('Push to Registry') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", "${DOCKER_CREDENTIALS}") {
                        // Push all service images
                        docker.image("${USER_SERVICE}:${APP_VERSION}").push()
                        docker.image("${USER_SERVICE}:latest").push()
                        
                        docker.image("${TICKET_SERVICE}:${APP_VERSION}").push()
                        docker.image("${TICKET_SERVICE}:latest").push()
                        
                        docker.image("${SUBSCRIPTION_SERVICE}:${APP_VERSION}").push()
                        docker.image("${SUBSCRIPTION_SERVICE}:latest").push()
                        
                        docker.image("${API_GATEWAY}:${APP_VERSION}").push()
                        docker.image("${API_GATEWAY}:latest").push()
                        
                        docker.image("${FRONTEND_APP}:${APP_VERSION}").push()
                        docker.image("${FRONTEND_APP}:latest").push()
                    }
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying to staging environment...'
                script {
                    // Update docker-compose with new image versions
                    sh '''
                        sed -i "s/:latest/:${APP_VERSION}/g" docker-compose.yml
                        
                        # Deploy to staging
                        docker-compose -f docker-compose.yml up -d
                        
                        # Health check
                        sleep 30
                        curl -f http://localhost:8082/actuator/health || exit 1
                    '''
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                script {
                    // Manual approval for production deployment
                    timeout(time: 10, unit: 'MINUTES') {
                        input message: 'Deploy to production?', ok: 'Deploy',
                              submitterParameter: 'DEPLOYER'
                    }
                    
                    echo "Deploying to production environment by ${DEPLOYER}..."
                    
                    // Production deployment steps
                    sh '''
                        echo "Production deployment would happen here..."
                        # kubectl apply -f k8s-manifests/
                        # or docker-compose deployment to production servers
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline completed. Cleaning up...'
            
            // Clean up Docker images
            sh '''
                docker image prune -f
                docker system prune -f --volumes
            '''
            
            // Archive build artifacts
            archiveArtifacts artifacts: '**/target/*.jar,**/build/**', fingerprint: true
            
            // Clean workspace
            cleanWs()
        }
        
        success {
            echo 'Pipeline succeeded! 🎉'
            
            // Send success notification
            emailext (
                subject: "✅ Build Success: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: """
                    <h2>Build Successful!</h2>
                    <p>Job: ${env.JOB_NAME}</p>
                    <p>Build Number: ${env.BUILD_NUMBER}</p>
                    <p>Version: ${APP_VERSION}</p>
                    <p>Branch: ${env.BRANCH_NAME}</p>
                    <p>Build URL: ${env.BUILD_URL}</p>
                """,
                to: "${env.CHANGE_AUTHOR_EMAIL},devops-team@company.com"
            )
        }
        
        failure {
            echo 'Pipeline failed! ❌'
            
            // Send failure notification
            emailext (
                subject: "❌ Build Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: """
                    <h2>Build Failed!</h2>
                    <p>Job: ${env.JOB_NAME}</p>
                    <p>Build Number: ${env.BUILD_NUMBER}</p>
                    <p>Branch: ${env.BRANCH_NAME}</p>
                    <p>Build URL: ${env.BUILD_URL}</p>
                    <p>Please check the build logs for details.</p>
                """,
                to: "${env.CHANGE_AUTHOR_EMAIL},devops-team@company.com"
            )
        }
        
        unstable {
            echo 'Pipeline unstable! ⚠️'
            
            // Send unstable notification
            emailext (
                subject: "⚠️ Build Unstable: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: """
                    <h2>Build Unstable!</h2>
                    <p>Job: ${env.JOB_NAME}</p>
                    <p>Build Number: ${env.BUILD_NUMBER}</p>
                    <p>Branch: ${env.BRANCH_NAME}</p>
                    <p>Build URL: ${env.BUILD_URL}</p>
                    <p>Some tests may have failed. Please review.</p>
                """,
                to: "${env.CHANGE_AUTHOR_EMAIL},devops-team@company.com"
            )
        }
    }
}