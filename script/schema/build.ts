// noinspection TypeScriptValidateJSTypes

import * as fs from "fs"
import * as path from "path"

const DEF_SCHEMA= "http://json-schema.org/draft-07/schema"

const PATH_IN_PRIME = "prime.json"
const PATH_OUT_SCHEMA = "schema"
const PATH_OUT_META = "meta.json"
const PATH_OUT_SCENAMATICA_FILE = "scenamatica-file.json"

const PATH_DIR_DEFINITIONS = "definitions"
const PATH_DIR_ACTIONS = "actions"


type Action = {
    name: string
    file: string
    description: string
    namespace: string
    base?: string
    arguments?: {
        [key: string]: unknown | {
            enum?: string[]
        }
    }
    required?: string[]
    definitions?: {
        [key: string]: unknown
    }
}

type Definable = {
    definitions?: {
        [key: string]: unknown
    }
}

type ActionDiversion = {
    if: {
        properties: {
            [key: string]: {
                const: string
                description: string
            }
        }
    },
    then: {
        properties: {
            with?: {
                $ref: string
            }
        }
        required?: string[]
    }
}

type Prime = Definable & {
    $schema: string
    definitions: {
        action: {
            allOf: ActionDiversion[]
            definitions: {
                [key: string]: {
                    [key: string]: unknown
                }
            }
        }
        actionKinds: {
            enum: string[]
        }
    }
}

type Meta = {
    definitionsDir: string
    actionsDir: string
    prime: string
    definitions: {
        [key: string]: string[]
    }
    actions: {
        [key: string]: {
            file: string
            description: string
        }
    }
}


const parseArgs = () => {
    // noinspection JSUnresolvedReference
    const args = process.argv.slice(2)
    if (args.length < 2) {
        console.log("Error: No arguments provided")
        console.log("Usage: build.ts base-dir output-dir")
        process.exit(1)
    }
    return {
        baseDir: args[0],
        outputDir: args[1]
    }
}

const loadJson = (baseDir: string, pathLike: string) : unknown => {
    // noinspection JSUnresolvedReference
    try
    {
        return JSON.parse(fs.readFileSync(path.join(baseDir, pathLike), "utf8"))
    }
    catch (e)
    {
        console.log(`Error: Failed to load ${pathLike}`)
        console.log(e)
        process.exit(1)
    }
}

const loadPrime = (baseDir: string) : Prime => {
    return loadJson(baseDir, PATH_IN_PRIME) as Prime
}


const setDefinitions = (definable: Definable, ...definitions: {[key: string]: unknown}[]) : Definable => {
    if (!definable.definitions) {
        definable.definitions = {}
    }

    for (const definition of definitions) {
        for (const key in definition) {
            definable.definitions[key] = definition[key]
        }
    }

    return definable
}

const retrieveExtendedDefinitions = (baseDir: string): {
    [key: string]: {
        [key: string]: unknown
    }
} => {
    const definitionsPath = path.join(baseDir, PATH_DIR_DEFINITIONS)

    const definitions: { [key: string]: {[key: string]: unknown} } = {}
    walkDir(definitionsPath, (file) => {
        const fileNameWithDir = path.relative(definitionsPath, file)
            .replace(/\\/g, "/")  // For Windows
            .replace(/\.json$/, "")
        const definition = loadJson("", file)
        definitions[fileNameWithDir] = {
            ...(definition as {[key: string]: unknown}),
        }
    })

    return definitions
}



const retrieveCompletePrimeFile = (baseDir: string) : Prime => {
    const prime = loadPrime(baseDir)

    prime["$schema"] = DEF_SCHEMA

    return prime as Prime
}

const loadActions = (baseDir: string) : Action[] => {
    const actionsPath = path.join(baseDir, PATH_DIR_ACTIONS)
    const actions: Action[] = []

    walkDir(actionsPath, (file) => {
        if (file.endsWith(".json")) {
            const namespace = path.relative(actionsPath, path.dirname(file))
                .replace(/\\/g, "/")  // For Windows
            const action = loadJson("", file) as Action
            action.namespace = namespace
            action.file = file.split(/[/\\]/).pop()
            actions.push(action)
        }
    })

    return actions
}

const walkDir = (dir: string, callback: (file: string) => void) => {
    const files = fs.readdirSync(dir)
    for (const file of files) {
        const filePath = path.join(dir, file)
        const stat = fs.statSync(filePath)
        if (stat.isDirectory()) {
            walkDir(filePath, callback)
        } else {
            callback(filePath)
        }
    }
}

const saveOutputSchema = (schema: Prime, outputFile: string) => {
    // create dirs
    const dir = path.dirname(outputFile)
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, {recursive: true})
    }

    // noinspection JSUnresolvedReference
    fs.writeFileSync(outputFile, JSON.stringify(schema, null))
}

const buildDefinitionIndex = (definitions: {[key: string]: {[key: string]: unknown}}) => {
    const meta = {}
    for (const file in definitions) {
        for (const key in definitions[file]) {
            if (!meta[file]) {
                meta[file] = []
            }
            meta[file].push(key)
        }
    }

    return meta
}

const buildActionsIndex = (actions: Action[]) => {
    const meta = {}
    for (const action of actions) {
        if (!meta[action.namespace]) {
            meta[action.namespace] = {}
        }
        meta[action.namespace][action.name] = {
            file: action.file,
            description: action.description
        }
    }

    return meta
}

const saveMetaData = (actions: Action[], definitions: {[key: string]: {[key: string]: unknown}}, outputFile) => {
    const definitionIndex = buildDefinitionIndex(definitions)
    const actionIndex = buildActionsIndex(actions)
    const meta: Meta = {
        definitionsDir: PATH_DIR_DEFINITIONS,
        actionsDir: PATH_DIR_ACTIONS,
        prime: PATH_IN_PRIME,
        definitions: definitionIndex,
        actions: actionIndex
    }


    // noinspection JSUnresolvedReference
    fs.writeFileSync(outputFile, JSON.stringify(meta, null, 2))
}

const buildActions = (prime: Prime, actions: Action[]) => {
    prime.definitions.actionKinds.enum = actions
        .filter((action) => !action.name.includes("$base"))
        .map((action) => action.name)

    const argumentDivergences = []
    const argumentDefinitions = {}
    // noinspection TypeScriptValidateJSTypes
    for (const action of actions) {
        if (!action.arguments) {
            continue
        }

        const divergence: ActionDiversion = {
            if: {
                properties: {
                    action: {
                        const: action.name,
                        description: action.description
                    }
                }
            },
            then: {
                properties: {
                    with: {
                        $ref: `#/definitions/action/definitions/${action.name}`
                    }
                }
            }
        }

        // action.arguments のどれかに required: true があるかどうか
        // noinspection JSUnresolvedReference
        if (action.arguments && Object.values(action.arguments).some((argument) => {
            return (argument as {[key: string]: unknown}).required === true
        })) {
            divergence.then.required = ["with"]
        }

        const definition = {
            type: "object",
            description: action.description,
            properties: action.arguments
        }

        if (action.base) {
            const reference = action.base
            if (reference.startsWith("#/")) {
                definition["$ref"] = reference
            } else {
                definition["$ref"] = `#/definitions/action/definitions/${action.base}`
            }
        }

        if (action.required) {
            definition["required"] = action.required
        }

        if (action.definitions) {
            definition["definitions"] = action.definitions
        }

        if (!action.name.includes("$base"))
            argumentDivergences.push(divergence)

        appendOtherCaseOfEnum(action, definition)
        argumentDefinitions[action.name] = definition
    }

    prime.definitions.action.allOf = argumentDivergences
    prime.definitions.action.definitions = argumentDefinitions
}

const normalizeEnumRecursively = (obj: {[key: string]: any}): void => {
    const enumKey = "enum"
    const enumValue = obj[enumKey]
    if (enumValue) {
        if (Array.isArray(enumValue)) {
            const arrayEnumValue = enumValue as unknown[]
            for (let i = 0; i < enumValue.length; i++) {
                const value = enumValue[i]
                if (typeof value === "string") {
                    const isOnlyUpperCase = value.match(/^[A-Z_]+$/)
                    if (isOnlyUpperCase) {
                        arrayEnumValue.push(value.toLowerCase())
                    }
                }
            }
        }
    }

    for (const key in obj) {
        const value = obj[key]
        if (typeof value === "object") {
            normalizeEnumRecursively(value as {[key: string]: unknown})
        }
    }
}

const appendOtherCaseOfEnum = (action: Action, definitions: {[key: string]: unknown}) => {
    if (!action.arguments) {
        return
    }

    for (const key in action.arguments) {
        const value = action.arguments[key]
        if (typeof value !== "object" || !(value as {[key: string]: unknown}).enum) {
            continue
        }

        normalizeEnumRecursively(value as {[key: string]: unknown})
    }
}

const convertTypeToDefinitions = (obj: {[key: string]: any}): void => {
    const knownTypes = [
        "string",
        "number",
        "integer",
        "boolean",
        "array",
        "object",
        "null"
    ]

    // すべてのプロパティを深さ優先探索, キーtype のみを処理。 type が knownTypes 以外であれば、その値を $ref に変換する。
    for (const key in obj) {
        const value = obj[key]
        if (key === "type" && typeof value === "string" && !knownTypes.includes(value)) {
            obj["$ref"] = `#/definitions/${value}`
            delete obj["type"]
        } else if (typeof value === "object") {
            convertTypeToDefinitions(value as {[key: string]: unknown})
        }
    }
}

const main = () => {
    const args = parseArgs()
    const baseDir = args.baseDir

    console.log("Loading prime file...")
    const prime = retrieveCompletePrimeFile(baseDir)

    console.log("Loading extended definitions...")
    const extendedDefinitions = retrieveExtendedDefinitions(baseDir)
    for (const key in extendedDefinitions) {
        setDefinitions(prime, extendedDefinitions[key])
        normalizeEnumRecursively(extendedDefinitions[key])
    }

    console.log("Loading actions...")
    const actions = loadActions(baseDir)
    buildActions(prime, actions)

    console.log("Converting type to definitions...")
    convertTypeToDefinitions(prime)

    console.log("Saving output schema...")
    saveOutputSchema(prime, path.join(args.outputDir, PATH_OUT_SCHEMA, PATH_OUT_SCENAMATICA_FILE))
    saveMetaData(actions, extendedDefinitions, path.join(args.outputDir, PATH_OUT_SCHEMA, PATH_OUT_META))
    // Legacy support.
    saveOutputSchema(prime, path.join(args.outputDir, PATH_OUT_SCENAMATICA_FILE))

    console.log("Done!")
}

main()
