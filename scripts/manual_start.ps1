# Manual Spring Boot deployment script

New-NetFirewallRule -DisplayName "Allow Port 8080 Inbound" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow

Write-Host "Starting manual Spring Boot deployment..."

# Set Java and Maven paths
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.14.7-hotspot"
$env:MAVEN_HOME = "C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.10"
$env:PATH = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"

# Navigate to application directory
Set-Location "C:\app"

# Step 1: Stop existing application
Write-Host "Step 1: Stopping existing Spring Boot application..."
$javaProcesses = Get-Process -Name "java", "javaw" -ErrorAction SilentlyContinue
foreach ($process in $javaProcesses) {
    Write-Host "Stopping Java process: $($process.Id)"
    $process.CloseMainWindow()
}
Start-Sleep -Seconds 15
Get-Process -Name "java", "javaw" -ErrorAction SilentlyContinue | Stop-Process -Force

# Step 2: Build application
Write-Host "Step 2: Building application with Maven..."
& "$env:MAVEN_HOME\bin\mvn.cmd" clean install -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build failed!"
    exit 1
}

Write-Host "Maven build completed successfully"

# Step 3: Start application in background
Write-Host "Step 3: Starting Spring Boot application in background..."

# Option 1: Using Start-Process (Recommended)
Start-Process -FilePath "$env:MAVEN_HOME\bin\mvn.cmd" -ArgumentList "spring-boot:run" -WindowStyle Hidden -WorkingDirectory "C:\app"

# Option 2: Alternative - run as background job
# Start-Job -ScriptBlock {
#     Set-Location "C:\app"
#     & "C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.10\bin\mvn.cmd" spring-boot:run
# }

# Wait a bit for the application to start
Write-Host "Waiting for application to start..."
Start-Sleep -Seconds 30

# Verify the application is running
$javaProcess = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcess) {
    Write-Host "Spring Boot application started successfully (PID: $($javaProcess.Id))"
} else {
    Write-Host "Warning: No Java process found. Application may not have started properly."
}

Write-Host "Manual deployment completed!"
exit 0