# PowerShell deployment preparation script for Spring Boot
Write-Host "Starting Spring Boot deployment preparation..."

# Log the start
$logFile = "C:\codedeploy-debug.log"
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
Add-Content -Path $logFile -Value "$timestamp - BeforeInstall script started"

# Stop Spring Boot processes gracefully
Write-Host "Stopping Spring Boot application..."

$javaProcesses = Get-Process -Name "java", "javaw" -ErrorAction SilentlyContinue
foreach ($process in $javaProcesses) {
    Write-Host "Stopping Java process: $($process.Id)"
    $process.CloseMainWindow()
}

# Wait for graceful shutdown
Start-Sleep -Seconds 15

# Force kill any remaining Java processes
Get-Process -Name "java", "javaw" -ErrorAction SilentlyContinue | Stop-Process -Force

# Stop Spring Boot service if it exists (adjust service name as needed)
$service = Get-Service -Name "MySpringBootApp" -ErrorAction SilentlyContinue
if ($service -and $service.Status -eq "Running") {
    Write-Host "Stopping Spring Boot service..."
    Stop-Service -Name "MySpringBootApp" -Force
}

# Verify CodeDeploy agent is still running
Get-Service -Name "codedeployagent" *>> $logFile

$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
Add-Content -Path $logFile -Value "$timestamp - BeforeInstall script completed"
Write-Host "Deployment preparation completed successfully"

exit 0