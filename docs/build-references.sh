#!/bin/sh

# 1. Build Scenamatica.jar
cd ../Scenamatica
chmod +x build.sh

./build.sh -D"jar.finalName=Scenamatica" -P release

if [ $? -ne 0 ]; then
    echo "Build failed."
    exit 1
fi

# 2. Build ledger
cd ../Bookkeeper
chmod +x run.sh
./run.sh -o dist -cp "\$paper:1.16.5" -i ../Scenamatica/ScenamaticaPlugin/target/Scenamatica.jar

if [ $? -ne 0 ]; then
    echo "Build failed."
    exit 1
fi

cd ../docs

# 3. Build templating-tools
CWD=$(pwd)
cd ../Bookkeeper/templating-tools
pnpm install
pnpm start -t $CWD/bookkeeper-templates -e .mdx  $CWD/references $CWD/dist/ledger.zip

if [ $? -ne 0 ]; then
    echo "Build failed."
    cd $CWD
    exit 1
fi

cd $CWD

# 4. Copy schemas to static directory

mkdir -p static/schemas

## 4.1 Copy schemas

cp -r dist/* static/schemas

## 4.2 Remove LICENSE and README.md files

rm -f static/schemas/LICENSE
rm -f static/schemas/README.md
