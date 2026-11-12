#!/usr/bin/env sh
# Punk Disco! - baixa as dependências (Gradle + libGDX) e roda o jogo.
# Requisito único: JDK 17 ou mais novo. Uso: ./run.sh
set -e
cd "$(dirname "$0")"

if ! command -v java >/dev/null 2>&1; then
    echo "Java não encontrado. Instale o JDK 17 ou mais novo:"
    echo "  Ubuntu/Debian: sudo apt install openjdk-21-jdk"
    echo "  Fedora:        sudo dnf install java-21-openjdk-devel"
    echo "  macOS:         brew install openjdk@21"
    exit 1
fi

VERSAO=$(java -version 2>&1 | sed -n '1s/.*"\([0-9]*\)[."].*/\1/p')
if [ -n "$VERSAO" ] && [ "$VERSAO" -lt 17 ]; then
    echo "Java $VERSAO encontrado, mas o jogo precisa do JDK 17 ou mais novo."
    exit 1
fi

chmod +x gradlew 2>/dev/null || true
echo "Baixando dependências (só demora na primeira vez) e abrindo o jogo..."
exec ./gradlew desktop:run
