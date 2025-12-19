pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9.0'  // Configure in Jenkins Global Tools
        nodejs 'NodeJS-18'   // Configure in Jenkins Global Tools
        dockerTool 'Docker'  // Configure in Jenkins Global Tools
    }
    
    environment {
        // Optional: Slack webhook URL for notifications (set in Jenkins job/env)
        SLACK_WEBHOOK_URL = ""
        
        // Docker Registry Configuration
        DOCKER_REGISTRY = credentials('docker-registry-url') // Configure in Jenkins credentials
        DOCKER_CREDENTIALS = credentials('docker-registry-credentials') // Docker Hub or private registry credentials
        DOCKER_NAMESPACE = 'your-organization' // Docker Hub namespace or org (override with env if needed)
        
        // Database Configuration for Tests
        TEST_DB_URL = 'jdbc:h2:mem:testdb'
        
        // Application Versions
        APP_VERSION = "${env.BUILD_NUMBER}"
        
        // Service Names
        USER_SERVICE = 'user-service'
        TICKET_SERVICE = 'ticket-service'
        SUBSCRIPTION_SERVICE = 'subscription-service'
        ROUTE_SERVICE = 'route-service'
        BUS_GEOLOCATION_SERVICE = 'bus-geolocation-service'
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
                        echo 'Running static code analysis for backend...'
                        sh '''
                            # Run Maven checkstyle and spotbugs
                            mvn clean compile checkstyle:check spotbugs:check -DskipTests=true
                        '''
                    }
                }
                
                stage('Frontend Code Analysis') {
                    steps {
                        dir('Frontend') {
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
                        echo 'Running security vulnerability scan...'
                        sh '''
                            # Run OWASP dependency check
                            mvn org.owasp:dependency-check-maven:check
                        '''
                    }
                }
            }
        }
        
        stage('Build & Test') {
            parallel {
                stage('Backend Services Build') {
                    steps {
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

                            echo "Building Route Service..."
                            cd Route && mvn package -DskipTests=true && cd ..

                            echo "Building Bus Geolocation Service..."
                            cd BusGeolocation && mvn package -DskipTests=true && cd ..

                            echo "Building API Gateway..."
                            cd api-gateway && mvn package -DskipTests=true && cd ..
                        '''
                    }
                }
                
                stage('Frontend Build') {
                    steps {
                        dir('Frontend') {
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
                        echo 'Running backend unit tests...'
                        sh '''
                            # Run tests for all services
                            mvn test -Dspring.profiles.active=test
                        '''

                        // Publish test results
                        publishTestResults testResultsPattern: '**/target/surefire-reports/*.xml'

                        // Publish coverage reports
                        publishCoverage adapters: [jacocoAdapter('**/target/site/jacoco/jacoco.xml')],
                                        sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
                    }
                }
                
                stage('Frontend Tests') {
                    steps {
                        dir('Frontend') {
                            echo 'Running frontend unit tests...'
                            sh '''
                                CI=true npm test -- --coverage --watchAll=false
                            '''
                        }

                        // Publish frontend test results
                        publishTestResults testResultsPattern: 'Frontend/coverage/lcov-report/*.html'
                    }
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
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
            post {
                always {
                    echo 'Cleaning up integration test environment...'
                    sh 'docker-compose down'
                }
            }
        }
        
        stage('Build Docker Images') {
            parallel {
                stage('Build User Service Image') {
                    steps {
                        dir('User') {
                            script {
                                def userImage = docker.build("${DOCKER_NAMESPACE}/${USER_SERVICE}:${APP_VERSION}")
                                userImage.tag("${USER_SERVICE}:latest")
                            }
                        }
                    }
                }
                
                stage('Build Ticket Service Image') {
                    steps {
                        dir('Ticket') {
                            script {
                                def ticketImage = docker.build("${DOCKER_NAMESPACE}/${TICKET_SERVICE}:${APP_VERSION}")
                                ticketImage.tag("${TICKET_SERVICE}:latest")
                            }
                        }
                    }
                }

                stage('Build Subscription Service Image') {
                    steps {
                        dir('Subscription') {
                            script {
                                def subscriptionImage = docker.build("${DOCKER_NAMESPACE}/${SUBSCRIPTION_SERVICE}:${APP_VERSION}")
                                subscriptionImage.tag("${SUBSCRIPTION_SERVICE}:latest")
                            }
                        }
                    }
                }

                stage('Build Route Service Image') {
                    steps {
                        dir('Route') {
                            script {
                                def routeImage = docker.build("${DOCKER_NAMESPACE}/${ROUTE_SERVICE}:${APP_VERSION}")
                                routeImage.tag("${ROUTE_SERVICE}:latest")
                            }
                        }
                    }
                }

                stage('Build Bus Geolocation Service Image') {
                    steps {
                        dir('BusGeolocation') {
                            script {
                                def geolocationImage = docker.build("${DOCKER_NAMESPACE}/${BUS_GEOLOCATION_SERVICE}:${APP_VERSION}")
                                geolocationImage.tag("${BUS_GEOLOCATION_SERVICE}:latest")
                            }
                        }
                    }
                }

                stage('Build API Gateway Image') {
                    steps {
                        dir('api-gateway') {
                            script {
                                def gatewayImage = docker.build("${DOCKER_NAMESPACE}/${API_GATEWAY}:${APP_VERSION}")
                                gatewayImage.tag("${API_GATEWAY}:latest")
                            }
                        }
                    }
                }

                stage('Build Frontend Image') {
                    steps {
                        dir('Frontend') {
                            script {
                                def frontendImage = docker.build("${DOCKER_NAMESPACE}/${FRONTEND_APP}:${APP_VERSION}")
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
                        sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image ${DOCKER_NAMESPACE}/${USER_SERVICE}:${APP_VERSION}"
                    }
                }

                stage('Scan Ticket Service') {
                    steps {
                        echo 'Scanning Ticket Service container for vulnerabilities...'
                        sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image ${DOCKER_NAMESPACE}/${TICKET_SERVICE}:${APP_VERSION}"
                    }
                }

                stage('Scan Subscription Service') {
                    steps {
                        echo 'Scanning Subscription Service container for vulnerabilities...'
                        sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image ${DOCKER_NAMESPACE}/${SUBSCRIPTION_SERVICE}:${APP_VERSION}"
                    }
                }

                stage('Scan Route Service') {
                    steps {
                        echo 'Scanning Route Service container for vulnerabilities...'
                        sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image ${DOCKER_NAMESPACE}/${ROUTE_SERVICE}:${APP_VERSION}"
                    }
                }

                stage('Scan Bus Geolocation Service') {
                    steps {
                        echo 'Scanning Bus Geolocation Service container for vulnerabilities...'
                        sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image ${DOCKER_NAMESPACE}/${BUS_GEOLOCATION_SERVICE}:${APP_VERSION}"
                    }
                }

                stage('Scan API Gateway') {
                    steps {
                        echo 'Scanning API Gateway container for vulnerabilities...'
                        sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image ${DOCKER_NAMESPACE}/${API_GATEWAY}:${APP_VERSION}"
                    }
                }

                stage('Scan Frontend App') {
                    steps {
                        echo 'Scanning Frontend container for vulnerabilities...'
                        sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image ${DOCKER_NAMESPACE}/${FRONTEND_APP}:${APP_VERSION}"
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
            post {
                always {
                    echo 'Cleaning up E2E test environment...'
                    sh 'docker-compose down'
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
                        docker.image("${DOCKER_NAMESPACE}/${USER_SERVICE}:${APP_VERSION}").push()
                        docker.image("${DOCKER_NAMESPACE}/${USER_SERVICE}:latest").push()

                        docker.image("${DOCKER_NAMESPACE}/${TICKET_SERVICE}:${APP_VERSION}").push()
                        docker.image("${DOCKER_NAMESPACE}/${TICKET_SERVICE}:latest").push()

                        docker.image("${DOCKER_NAMESPACE}/${SUBSCRIPTION_SERVICE}:${APP_VERSION}").push()
                        docker.image("${DOCKER_NAMESPACE}/${SUBSCRIPTION_SERVICE}:latest").push()

                        docker.image("${DOCKER_NAMESPACE}/${ROUTE_SERVICE}:${APP_VERSION}").push()
                        docker.image("${DOCKER_NAMESPACE}/${ROUTE_SERVICE}:latest").push()

                        docker.image("${DOCKER_NAMESPACE}/${BUS_GEOLOCATION_SERVICE}:${APP_VERSION}").push()
                        docker.image("${DOCKER_NAMESPACE}/${BUS_GEOLOCATION_SERVICE}:latest").push()

                        docker.image("${DOCKER_NAMESPACE}/${API_GATEWAY}:${APP_VERSION}").push()
                        docker.image("${DOCKER_NAMESPACE}/${API_GATEWAY}:latest").push()

                        docker.image("${DOCKER_NAMESPACE}/${FRONTEND_APP}:${APP_VERSION}").push()
                        docker.image("${DOCKER_NAMESPACE}/${FRONTEND_APP}:latest").push()
                    }
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying to Kubernetes staging environment...'
                script {
                    sh '''
                        set -e
                        K8S_NS=transport-staging
                        kubectl get ns ${K8S_NS} || kubectl create ns ${K8S_NS}
                        
                        # Apply base manifests
                        kubectl apply -n ${K8S_NS} -f k8s/manifests/
                        
                        # Update images to the version built in this pipeline
                        kubectl -n ${K8S_NS} set image deployment/user-service user-service=${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${USER_SERVICE}:${APP_VERSION}
                        kubectl -n ${K8S_NS} set image deployment/ticket-service ticket-service=${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${TICKET_SERVICE}:${APP_VERSION}
                        kubectl -n ${K8S_NS} set image deployment/subscription-service subscription-service=${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${SUBSCRIPTION_SERVICE}:${APP_VERSION}
                        kubectl -n ${K8S_NS} set image deployment/route-service route-service=${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${ROUTE_SERVICE}:${APP_VERSION}
                        kubectl -n ${K8S_NS} set image deployment/bus-geolocation-service bus-geolocation-service=${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${BUS_GEOLOCATION_SERVICE}:${APP_VERSION}
                        kubectl -n ${K8S_NS} set image deployment/api-gateway api-gateway=${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${API_GATEWAY}:${APP_VERSION}
                        kubectl -n ${K8S_NS} set image deployment/frontend-app frontend-app=${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${FRONTEND_APP}:${APP_VERSION}

                        # Wait for rollouts
                        kubectl -n ${K8S_NS} rollout status deployment/user-service --timeout=120s
                        kubectl -n ${K8S_NS} rollout status deployment/ticket-service --timeout=120s
                        kubectl -n ${K8S_NS} rollout status deployment/subscription-service --timeout=120s
                        kubectl -n ${K8S_NS} rollout status deployment/route-service --timeout=120s
                        kubectl -n ${K8S_NS} rollout status deployment/bus-geolocation-service --timeout=120s
                        kubectl -n ${K8S_NS} rollout status deployment/api-gateway --timeout=180s
                        kubectl -n ${K8S_NS} rollout status deployment/frontend-app --timeout=180s
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
                    echo "Deploying to Kubernetes production environment by ${DEPLOYER}..."
                    sh '''
                        set -e
                        K8S_NS=transport-prod
                        kubectl get ns ${K8S_NS} || kubectl create ns ${K8S_NS}
                        kubectl apply -n ${K8S_NS} -f k8s/manifests/
                        kubectl -n ${K8S_NS} set image deployment/user-service user-service=${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${USER_SERVICE}:${APP_VERSION}
                        kubectl -n ${K8S_NS} set image deployment/ticket-service ticket-service=${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${TICKET_SERVICE}:${APP_VERSION}
                        kubectl -n ${K8S_NS} set image deployment/subscription-service subscription-service=${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${SUBSCRIPTION_SERVICE}:${APP_VERSION}
                        kubectl -n ${K8S_NS} set image deployment/route-service route-service=${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${ROUTE_SERVICE}:${APP_VERSION}
                        kubectl -n ${K8S_NS} set image deployment/bus-geolocation-service bus-geolocation-service=${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${BUS_GEOLOCATION_SERVICE}:${APP_VERSION}
                        kubectl -n ${K8S_NS} set image deployment/api-gateway api-gateway=${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${API_GATEWAY}:${APP_VERSION}
                        kubectl -n ${K8S_NS} set image deployment/frontend-app frontend-app=${DOCKER_REGISTRY}/${DOCKER_NAMESPACE}/${FRONTEND_APP}:${APP_VERSION}
                        kubectl -n ${K8S_NS} rollout status deployment/user-service --timeout=180s
                        kubectl -n ${K8S_NS} rollout status deployment/ticket-service --timeout=180s
                        kubectl -n ${K8S_NS} rollout status deployment/subscription-service --timeout=180s
                        kubectl -n ${K8S_NS} rollout status deployment/route-service --timeout=180s
                        kubectl -n ${K8S_NS} rollout status deployment/bus-geolocation-service --timeout=180s
                        kubectl -n ${K8S_NS} rollout status deployment/api-gateway --timeout=240s
                        kubectl -n ${K8S_NS} rollout status deployment/frontend-app --timeout=240s
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
            
            // Slack notification (optional if SLACK_WEBHOOK_URL is set)
            sh '''
                if [ -n "$SLACK_WEBHOOK_URL" ]; then
                  curl -s -X POST -H 'Content-type: application/json' \
                    --data '{"text":"✅ Build Success: '"${JOB_NAME}"' #'"${BUILD_NUMBER}"' (v'"${APP_VERSION}"') - '"${BUILD_URL}"'"}' "$SLACK_WEBHOOK_URL" >/dev/null || true
                fi
            '''
            
            // Email notification
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
            
            // Slack notification (optional if SLACK_WEBHOOK_URL is set)
            sh '''
                if [ -n "$SLACK_WEBHOOK_URL" ]; then
                  curl -s -X POST -H 'Content-type: application/json' \
                    --data '{"text":"❌ Build Failed: '"${JOB_NAME}"' #'"${BUILD_NUMBER}"' - '"${BUILD_URL}"'"}' "$SLACK_WEBHOOK_URL" >/dev/null || true
                fi
            '''
            
            // Email notification
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
            
            // Slack notification (optional if SLACK_WEBHOOK_URL is set)
            sh '''
                if [ -n "$SLACK_WEBHOOK_URL" ]; then
                  curl -s -X POST -H 'Content-type: application/json' \
                    --data '{"text":"⚠️ Build Unstable: '"${JOB_NAME}"' #'"${BUILD_NUMBER}"' - '"${BUILD_URL}"'"}' "$SLACK_WEBHOOK_URL" >/dev/null || true
                fi
            '''
            
            // Email notification
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