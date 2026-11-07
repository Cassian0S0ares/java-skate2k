# Auditoria de versões · Aula 08 (PowerShell)
# Rode na pasta do projeto:  powershell -ExecutionPolicy Bypass -File .\auditoria_aula08.ps1

function Tem($cmd) { [bool](Get-Command $cmd -ErrorAction SilentlyContinue) }

Write-Host "=== 01 · Runtimes ===" -ForegroundColor Cyan
if (Tem php)  { php -v | Select-Object -First 1 } else { "PHP: não instalado" }
if (Tem node) { "Node.js " + (node -v) } else { "Node.js: não instalado" }
if (Tem java) { (cmd /c "java -version 2>&1") | Select-Object -First 1 } else { "Java: não instalado" }

$temPkg = Test-Path package.json

Write-Host "`n=== 03 · Express / body-parser ===" -ForegroundColor Cyan
if ($temPkg -and (Tem npm)) { npm ls express body-parser } else { "sem package.json ou npm" }

Write-Host "`n=== 04 · React Server Components / Next ===" -ForegroundColor Cyan
if ($temPkg -and (Tem npm)) { npm ls react react-server-dom-webpack next } else { "sem package.json ou npm" }

Write-Host "`n=== 05 · Auditoria de dependências ===" -ForegroundColor Cyan
if ($temPkg -and (Tem npm)) { npm audit --audit-level=high | Select-Object -Last 20 }
if ((Test-Path composer.json) -and (Tem composer)) { composer audit }

Write-Host "`n=== 06 · Containers ===" -ForegroundColor Cyan
if (Tem docker) { docker images --format "{{.Repository}}:{{.Tag}}" | Select-Object -First 10 } else { "Docker: não instalado" }
