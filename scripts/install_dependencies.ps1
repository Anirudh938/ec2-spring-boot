# PowerShell script to install Spring Boot dependencies
Write-Host "Starting Spring Boot dependencies installation..."

# Log the start
$logFile = "C:\codedeploy-debug.log"
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
Add-Content -Path $logFile -Value "$timestamp - ApplicationStart script started"

# Set Java and Maven paths
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.14.7-hotspot"
$env:MAVEN_HOME = "C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.10"
$env:PATH = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"

# Navigate to application directory
Set-Location "C:\app"

# Install dependencies
Write-Host "Installing Maven dependencies..."
mvn clean install -DskipTests *>> $logFile

$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
Add-Content -Path $logFile -Value "$timestamp - ApplicationStart script completed"
Write-Host "Dependencies installation completed"

exit 0