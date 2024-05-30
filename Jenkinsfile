pipeline {
  agent {
    kubernetes {
      yamlFile 'jenkins/pod-templates/cdt-full-pod-plus-eclipse-install.yaml'
    }
  }
  options {
    timestamps()
    disableConcurrentBuilds()
  }
  stages {
    stage('initialize PGP') {
      steps {
        container('cdt') {
          withCredentials([file(credentialsId: 'secret-subkeys.asc', variable: 'KEYRING')]) {
            sh 'gpg --batch --import "${KEYRING}"'
            sh 'for fpr in $(gpg --list-keys --with-colons  | awk -F: \'/fpr:/ {print $10}\' | sort -u); do echo -e "5\ny\n" |  gpg --batch --command-fd 0 --expert --edit-key ${fpr} trust; done'
          }
        }
      }
    }
    stage('Install clangd') {
      steps {
        container('cdt') {
          timeout(activity: true, time: 30) {
            sh 'curl -L https://github.com/clangd/clangd/releases/download/15.0.6/clangd-linux-15.0.6.zip > clangd.zip'
            sh 'unzip clangd.zip'
          }
        }
      }
    }
    stage('Build and verify') {
      steps {
        container('cdt') {
          timeout(activity: true, time: 20) {
            withEnv(['MAVEN_OPTS=-XX:MaxRAMPercentage=60.0']) {
              withCredentials([string(credentialsId: 'gpg-passphrase', variable: 'KEYRING_PASSPHRASE')]) {
                sh '''
                  export PATH=$PWD/clangd_15.0.6/bin:$PATH
                  which clangd
                  clangd --version
                  /jipp/tools/apache-maven/latest/bin/mvn \
                      clean verify -B -V -e \
                      -Dmaven.test.failure.ignore=true \
                      -Dgpg.passphrase="${KEYRING_PASSPHRASE}"  \
                      -P production \
                      -Dmaven.repo.local=/home/jenkins/.m2/repository \
                      --settings /home/jenkins/.m2/settings.xml \
                '''
              }
            }
          }
        }
      }
    }
    stage('Deploy Snapshot') {
      steps {
        container('jnlp') {
          timeout(activity: true, time: 20) {
            sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
              sh '''
                  SSHUSER="genie.cdt@projects-storage.eclipse.org"
                  SSH="ssh ${SSHUSER}"
                  SCP="scp"


                  DOWNLOAD=download.eclipse.org/tools/cdt/builds/cdt-lsp/$BRANCH_NAME
                  DOWNLOAD_MOUNT=/home/data/httpd/$DOWNLOAD

                  # Deploying build to nightly location on download.eclipse.org
                  if $SSH test -e ${DOWNLOAD_MOUNT}-new; then
                      $SSH rm -r ${DOWNLOAD_MOUNT}-new
                  fi
                  if $SSH test -e ${DOWNLOAD_MOUNT}-last; then
                      $SSH rm -r ${DOWNLOAD_MOUNT}-last
                  fi
                  $SSH mkdir -p ${DOWNLOAD_MOUNT}-new
                  $SCP -rp releng/org.eclipse.cdt.lsp.repository/target/repository/* "${SSHUSER}:"${DOWNLOAD_MOUNT}-new
                  if $SSH test -e ${DOWNLOAD_MOUNT}; then
                      $SSH mv ${DOWNLOAD_MOUNT} ${DOWNLOAD_MOUNT}-last
                  fi
                  $SSH mv ${DOWNLOAD_MOUNT}-new ${DOWNLOAD_MOUNT}
              '''
            }
          }
        }
      }
    }

  }
  post {
    always {
      container('cdt') {
        archiveArtifacts '**/*.log,releng/org.eclipse.cdt.lsp.repository/target/**'
        junit '**/target/surefire-reports/*.xml'
      }
    }
  }
}
