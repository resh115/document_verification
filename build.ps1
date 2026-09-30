$cp = (Get-ChildItem -Path "src\main\webapp\WEB-INF\lib\*.jar" | ForEach-Object { $_.FullName }) -join ";"
$cp += ";C:\Tomcat9\apache-tomcat-9.0.120\lib\servlet-api.jar;C:\Tomcat9\apache-tomcat-9.0.120\lib\jsp-api.jar"
$files = (Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName })
if (!(Test-Path "build\classes")) { New-Item -ItemType Directory -Path "build\classes" -Force }
& "C:\Program Files\Java\jdk-24\bin\javac.exe" --release 11 -encoding UTF-8 -cp $cp -d "build\classes" $files
if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation SUCCESS!"
} else {
    Write-Host "Compilation FAILED!"
}
