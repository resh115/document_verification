$targets = @(
    "C:\Tomcat9\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\web_1",
    "C:\Tomcat9\apache-tomcat-9.0.120\webapps\web_1"
)

foreach ($target in $targets) {
    if (Test-Path $target) {
        Write-Host "Syncing to $target ..."
        # Copy webapp static files and JSPs
        Copy-Item -Path "src\main\webapp\*" -Destination $target -Recurse -Force
        
        # Ensure WEB-INF/classes exists
        $targetClasses = Join-Path $target "WEB-INF\classes"
        if (!(Test-Path $targetClasses)) {
            New-Item -ItemType Directory -Path $targetClasses -Force | Out-Null
        }
        
        # Copy compiled classes
        Copy-Item -Path "build\classes\*" -Destination $targetClasses -Recurse -Force
        Write-Host "Sync complete for $target"
    } else {
        Write-Host "Target not found: $target"
    }
}
