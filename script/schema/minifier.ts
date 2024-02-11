import * as fs from "fs"
import * as path from "path"

const EXT_FILE = ".json"

const parseArgs = () => {
    const args = process.argv.slice(2)
    if (args.length < 1) {
        console.error("Please specify a target directory")
        process.exit(1)
    }

    return args[0]
}

const enumerateFiles = (dir: string): string[] => {
    const files = fs.readdirSync(dir)
    const result: string[] = []
    for (const file of files) {
        const fullPath = path.join(dir, file)
        if (fs.statSync(fullPath).isDirectory()) {
            result.push(...enumerateFiles(fullPath))
        }
        else if (file.endsWith(EXT_FILE)) {
            result.push(fullPath)
        }
    }

    return result
}

const minifyOne = (file: string, output: string) => {
    const content = fs.readFileSync(file, "utf8")
    const data = JSON.parse(content)
    const minified = JSON.stringify(data)

    fs.writeFileSync(output, minified + "\n", "utf8")
}

const sizeOf = (file: string): number => {
    const stats = fs.statSync(file)
    return stats.size
}

const minifyFiles = (files: string[]) => {
    for (let i = 0; i < files.length; i++) {
        const file = files[i]
        const originalSize = sizeOf(file)
        minifyOne(file, file)
        const newSize = sizeOf(file)

        console.log(`[${i + 1}/${files.length}] ${file} ${originalSize} -> ${newSize}, saved ${originalSize - newSize} bytes(${((originalSize - newSize) / originalSize * 100).toFixed(2)}%)`)
    }
}

const main = () => {
    console.log("Scanning files to minify...")
    const targetDir = parseArgs()
    console.log(`Target directory: ${targetDir}`)

    const files = enumerateFiles(targetDir)
    console.log(`Found ${files.length} files.`)

    console.log("Minifying files...")
    minifyFiles(files)

    console.log("Done.")
}

main()
