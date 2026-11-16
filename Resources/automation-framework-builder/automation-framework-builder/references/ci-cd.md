# CI/CD

Generate only what the user chose (default both). Place in the project root. Secrets (passwords, tokens) go in Jenkins credentials / GitHub secrets and are read from environment variables, never committed.

## Jenkinsfile (Java)
```groovy
pipeline {
  agent any
  tools { maven 'Maven3'; jdk 'JDK17' }
  parameters {
    choice(name: 'BROWSER', choices: ['chrome','firefox'], description: 'Browser')
    string(name: 'TEST_GROUPS', defaultValue: 'smoke', description: 'TestNG groups')
    string(name: 'ENV', defaultValue: 'qa', description: 'Environment')
  }
  stages {
    stage('Test') {
      steps { sh "mvn -B clean test -Denv=${params.ENV} -Dbrowser=${params.BROWSER} -Dheadless=true -Dgroups=${params.TEST_GROUPS}" }
    }
  }
  post {
    always {
      archiveArtifacts artifacts: 'output/**', allowEmptyArchive: true
      publishHTML(target: [reportDir: 'output/reports', reportFiles: 'ExtentReport.html', reportName: 'Extent Report', keepAll: true, allowMissing: true, alwaysLinkToLastBuild: true])
    }
  }
}
```
Playwright variant: tools block uses `nodejs 'Node20'`; the Test stage runs `sh 'npm ci && npx playwright install --with-deps && npx playwright test --grep "${TEST_GROUPS}"'`; publish `playwright-report/` and archive `output/**`.

## GitHub Actions (Java) `.github/workflows/tests.yml`
```yaml
name: tests
on: { push: { branches: [main] }, pull_request: {}, workflow_dispatch: {} }
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17, cache: maven }
      - run: mvn -B clean test -Denv=qa -Dbrowser=chrome -Dheadless=true -Dgroups=smoke
      - uses: actions/upload-artifact@v4
        if: always()
        with: { name: test-output, path: output/ }
```

## GitHub Actions (Playwright)
```yaml
name: tests
on: { push: { branches: [main] }, pull_request: {}, workflow_dispatch: {} }
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: 20, cache: npm }
      - run: npm ci
      - run: npx playwright install --with-deps
      - run: npx playwright test --grep "@smoke|@api"
        env: { CI: 'true', ENV: qa }
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-output
          path: |
            output/
            playwright-report/
```
