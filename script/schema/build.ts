// noinspection TypeScriptValidateJSTypes

import * as fs from "fs"
import * as path from "path"

const DEF_SCHEMA= "http://json-schema.org/draft-07/schema"

const PATH_PRIME = "prime.json"
const PATH_DEFINITIONS = "definitions.json"
const PATH_DIR_DEFINITIONS = "definitions/"
const PATH_DIR_ACTIONS = "actions/"


type Action = {
    name: string
    description: string
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
            properties: {
                action: {
                    enum: string[]
                }
            },
            allOf: ActionDiversion[]
            definitions: {
                [key: string]: {
                    [key: string]: unknown
                }
            }
        }
    }
}


const parseArgs = () => {
    // noinspection JSUnresolvedReference
    const args = process.argv.slice(2)
    if (args.length < 2) {
        console.log("Error: No arguments provided")
        console.log("Usage: build.ts base-dir output-file")
        process.exit(1)
    }
    return {
        baseDir: args[0],
        outputFile: args[1]
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
    return loadJson(baseDir, PATH_PRIME) as Prime
}

const loadPrimeDefinition = (baseDir: string) : {[key: string]: unknown} => {
    return loadJson(baseDir, PATH_DEFINITIONS) as {[key: string]: unknown}
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
        const name = file.split(".")[0]
        const definition = loadJson("", file)
        definitions[name] = definition as {[key: string]: unknown}
    })

    return definitions
}



const retrieveCompletePrimeFile = (baseDir: string) : Prime => {
    const prime = loadPrime(baseDir)
    const definitions = loadPrimeDefinition(baseDir)
    const completedPrime = setDefinitions(prime, definitions)

    completedPrime["$schema"] = DEF_SCHEMA

    return completedPrime as Prime
}

const loadActions = (baseDir: string) : Action[] => {
    const actionsPath = path.join(baseDir, PATH_DIR_ACTIONS)
    const actions: Action[] = []

    walkDir(actionsPath, (file) => {
        if (file.endsWith(".json")) {
            const action = loadJson("", file) as Action
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
    // noinspection JSUnresolvedReference
    fs.writeFileSync(outputFile, JSON.stringify(schema, null, 2))
}


const buildActions = (prime: Prime, actions: Action[]) => {
    prime.definitions.action.properties.action.enum = actions
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

const appendOtherCaseOfEnum = (action: Action, definitions: {[key: string]: unknown}) => {
    if (!action.arguments) {
        return
    }

    for (const key in action.arguments) {
        const value = action.arguments[key]
        if (typeof value !== "object" || !(value as {[key: string]: unknown}).enum) {
            continue
        }

        const enumValues = (value as {[key: string]: unknown}).enum as string[]
        const aliases = []
        let applyAlias = true
        for (const enumValue of enumValues) {
            if (enumValue.match(/^[A-Z_]+$/)) {
                aliases.push(enumValue.toLowerCase())
            } else {
                applyAlias = false
                break
            }
        }

        if (applyAlias) {
            definitions.properties[key].enum = enumValues.concat(aliases)
        }
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
    }

    console.log("Loading actions...")
    const actions = loadActions(baseDir)
    buildActions(prime, actions)

    console.log("Converting type to definitions...")
    convertTypeToDefinitions(prime)

    console.log("Saving output schema...")
    saveOutputSchema(prime, args.outputFile)
}

main()
