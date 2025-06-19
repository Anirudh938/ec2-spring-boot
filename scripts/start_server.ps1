# PowerShell script to start Spring Boot server
Write-Host "Starting Spring Boot server..."

# Log the start
$logFile = "C:\codedeploy-debug.log"
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
Add-Content -Path $logFile -Value "$timestamp - ApplicationStart server script started"

# Set Java and Maven paths
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.14"
$env:MAVEN_HOME = "C:\Program Files\Apache\maven\apache-maven-3.9.10"
$env:PATH = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"

# Navigate to application directory
Set-Location "C:\app"

# Start Spring Boot application
Write-Host "Starting Spring Boot application on port 8080..."
Start-Process -FilePath "java" -ArgumentList "-jar", "target\*.jar", "--server.port=8080" -WindowStyle Hidden

# Wait a moment for startup
Start-Sleep -Seconds 10

# Verify application is running
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080" -TimeoutSec 30 -ErrorAction Stop
    Write-Host "Spring Boot application started successfully"
    Add-Content -Path $logFile -Value "$timestamp - Spring Boot application started successfully"
}
catch {
    Write-Host "Warning: Could not verify application startup"
    Add-Content -Path $logFile -Value "$timestamp - Could not verify application startup: $($_.Exception.Message)"
}

$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
Add-Content -Path $logFile -Value "$timestamp - ApplicationStart server script completed"
Write-Host "Server startup completed"

exit 0