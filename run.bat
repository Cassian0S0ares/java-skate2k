@echo off
rem Punk Disco! - baixa as dependencias (Gradle + libGDX) e roda o jogo.
rem Requisito unico: JDK 17 ou mais novo. Uso: run.bat (duplo clique funciona).
cd /d "%~dp0"

where java >nul 2>nul
if errorlevel 1 (
    echo Java nao encontrado. Instale o JDK 17 ou mais novo:
    echo   winget install EclipseAdoptium.Temurin.21.JDK
    echo   ou baixe em https://adoptium.net
    pause
    exit /b 1
)

echo Baixando dependencias (so demora na primeira vez) e abrindo o jogo...
call gradlew.bat desktop:run
if errorlevel 1 (
    echo.
    echo O jogo terminou com erro.
    pause
    exit /b 1
)
