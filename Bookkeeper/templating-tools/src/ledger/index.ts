import * as path from "node:path"
import * as fs from "node:fs"
import * as AdmZip from "adm-zip"
import {LinkedReference, ReferenceLinker} from "./linker"
import {TemplateRetriever} from "../template/retriever"
import * as Handlebars from "handlebars";
import * as handlebars from "handlebars";

interface LedgerObject {
    $reference: string
    id: string
}

interface Category extends LedgerObject {
    name: string
    description: string
    phase: string

    children: LedgerObject[]
}

type AdmonitionType = "note" | "tip" | "info" | "warning" | "danger"
type AdmonitionTarget = "execute" | "watch" | "require"

interface Admonition {
    type: AdmonitionType
    title?: string
    content: string
    on?: AdmonitionTarget[]
}

interface Contracts {
    executable?: string | false
    watchable?: string | false
    requireable?: string | false
}

interface InputOutputProperties {
    description?: string
    type?: string
    requiredOn?: AdmonitionTarget[]
    availableFor?: AdmonitionTarget[]
    supportsSince?: string
    supportsUntil?: string
    pattern?: string
    min?: number
    max?: number
    constValue?: any
    requiresActor?: boolean
    inheritedFrom?: string
    admonitions?: Admonition[]
}

interface InputOutput {
    [key: string]: InputOutputProperties
}

interface Property {
    name: string
    type: string
    description: string
    required?: boolean
    defaultValue?: string
    values?: string[]
    admonitions?: Admonition[]
}

interface Type extends LedgerObject {
    category: string
    name: string
    type: string
    class: string
    mapping_of?: string
    enums?: string[]
    properties?: { [key: string]: Property }
    admonitions?: Admonition[]
}

interface Action extends LedgerObject {
    super?: string
    name: string
    description: string
    category?: string
    events?: string[]
    contracts?: Contracts
    supportsSince?: string
    supportsUntil?: string
    inputs?: InputOutput
    outputs?: InputOutput
    admonitions?: Admonition[]
}

type EventSource = "spigot" | "paper" | "bukkit" | "waterfall" | "velocity" | "purpur"

interface Descriptions {
    ja: string
    en?: string

    [key: string]: string
}

interface Event extends LedgerObject {
    name: string
    javadoc?: string
    javadocLink?: string
    source: EventSource
    descriptions?: Descriptions
}

const createPrimitive = (reference: string, displayName: string, path?: string) => {
    return {
        name: displayName,
        id: displayName,
        $reference: reference,
        path: path
    }
}

class LedgerSession {
    private static readonly TYPES = [
        "categories",
        "types",
        "events",
        "actions",
    ]
    private
    private readonly ledgerFile: string
    private readonly tempDir: string
    private readonly outputExt: string
    private readonly outputDir: string
    private readonly basePath: string
    private readonly linker: ReferenceLinker
    private readonly templates: TemplateRetriever

    constructor(ledgerFile: string, outputDir: string, templates: TemplateRetriever, outputExt: string, basePath: string) {
        this.ledgerFile = ledgerFile
        this.outputDir = outputDir
        this.templates = templates
        this.outputExt = outputExt.startsWith(".") ? outputExt.slice(1) : outputExt
        this.basePath = basePath.endsWith("/") ? basePath.slice(0, -1) : basePath

        this.tempDir = path.join(outputDir, "temp")
        this.linker = new ReferenceLinker()
    }

    private static enumerateFilesRecursively(directory: string): string[] {
        const files = fs.readdirSync(directory)
        const result = []
        for (const file of files) {
            const fullPath = path.join(directory, file)
            if (fs.statSync(fullPath).isDirectory()) {
                result.push(...LedgerSession.enumerateFilesRecursively(fullPath))
            } else {
                result.push(fullPath)
            }
        }
        return result
    }

    private static getElementsByCategory(categoryRef: string, targets: ({
        type: string,
        data: LedgerObject
    })[]): ({ type: string, data: LedgerObject })[] {
        return targets.filter(target => {
            if (target.type === "categories") {
                return false
            }
            const data = target.data as Action | Type
            return data.category === categoryRef
        })
    }

    private static flushContent(content: string, outputFile: string) {
        const outputDir = path.dirname(outputFile)
        fs.mkdirSync(outputDir, {recursive: true})
        fs.writeFileSync(outputFile, content)
    }

    public init() {
        console.log("Initializing session...")
        try {
            fs.mkdirSync(this.tempDir, {recursive: true})
        } catch (error) {
            console.error("Error creating temp directory:", error)
        }

        this.initHandlebars()
    }

    public extract() {
        console.log("Extracting ledger file...")
        try {
            const zip = new AdmZip(this.ledgerFile)
            zip.extractAllTo(this.tempDir, true)
        } catch (error) {
            console.error("Error extracting ledger file:", error)
        }
    }

    public processLedger() {
        console.log("Processing ledger file...")

        const targets: { type: string, data: Category | Action | Type }[] = []
        LedgerSession.TYPES.forEach(type => {
            const directory = path.join(this.tempDir, type)
            if (!fs.existsSync(directory)) {
                console.error(`Unable to find ${type} directory.`)
                return
            }

            const files = LedgerSession.enumerateFilesRecursively(directory)

            for (const file of files) {
                if (!file.endsWith(".json")) {
                    console.debug("Skipping non-JSON file:", file)
                    continue
                }

                console.log("Processing file:", file)
                try {
                    const content = fs.readFileSync(file).toString()
                    const data: Category | Action | Type = JSON.parse(content)

                    const reference = data.$reference
                    const displayName = data.name
                    // Write the output to the output directory
                    this.linker.addReference(reference, displayName, type, data)

                    if (type !== "events") {
                        // イベントは処理しない
                        targets.push({type, data})
                    }
                } catch (error) {
                    console.error("Error processing file:", file, error)
                }
            }
        })

        this.linker.reorderReferences()
        console.log("Processing targets...")
        for (const target of targets) {
            const {type, data} = target
            // カテゴリには自身の子を追加しておく
            if (type === "categories") {
                const category = data as Category
                const categoryReference = category.$reference
                category.children = LedgerSession.getElementsByCategory(categoryReference, targets)
                    .map(target => target.data)
            }

            this.processOneFile(type, data)
        }
    }

    public resolveReference(reference: string): LinkedReference {
        return this.linker.resolveReference(reference)
    }

    public dispose() {
        console.log("Disposing session...")
        this.deleteTempFiles()
    }


    private initHandlebars() {
        const coPrimitives = {
            "$ref:type:playerSpecifier": createPrimitive("$ref:type:playerSpecifier", "プレイヤ指定子", "/"),
            "$ref:type:entitySpecifier": createPrimitive("$ref:type:entitySpecifier", "エンティティ指定子", "/"),
            "$ref:type:namespacedKey": createPrimitive("$ref:type:namespacedKey", "名前空間付きキー", "/"),
            "$ref:type:namespaced": createPrimitive("$ref:type:namespaced", "名前空間付きキー", "/"),
            "$ref:type:uuid": createPrimitive("$ref:type:uuid", "UUID", "/"),
            "string": createPrimitive("string", "文字列"),
            "integer": createPrimitive("integer", "整数値"),
            "boolean": createPrimitive("boolean", "真偽値"),
            "float": createPrimitive("float", "浮動小数点数"),
            "double": createPrimitive("double", "倍精度浮動小数点数"),
            "long": createPrimitive("long", "長整数値"),
            "short": createPrimitive("short", "短整数値"),
            "byte": createPrimitive("byte", "バイト値"),
            "char": createPrimitive("char", "文字"),
            "object": createPrimitive("object", "オブジェクト"),
            "map": createPrimitive("map", "マップ")
        }

        Handlebars.registerPartial("admonitions", `
{{#if admonitions}}
{{#each admonitions}}
<Admonition type="{{type}}" {{#if title}}title="{{title}}"{{/if}}>
  {{{content}}}
</Admonition>
{{/each}}
{{/if}}
`);

        Handlebars.registerHelper("safe", (content: string) => {
            return new Handlebars.SafeString(content)
        })

        const resolve = (reference: string) => {
            if (!reference.startsWith("$")) {
                throw new Error(`Invalid reference: ${reference}`)
            } else if (reference.match(/^\$ref:event:\?/)) {  // イベントは参照切れが想定される
                return {
                    name: reference.substring("$ref:event:?".length)
                }
            }

            const linkedReference = this.resolveReference(reference)
            if (linkedReference) {
                return linkedReference.obj
            } else {
                throw new Error(`Reference not found: ${reference}`)
            }
        }

        Handlebars.registerHelper("$", (obj: any, key: string) => {
            return obj[key]
        })

        Handlebars.registerHelper("resolve", resolve)
        Handlebars.registerHelper("resolveType", (type: string) => {
            if (coPrimitives[type]) {
                return coPrimitives[type]
            } else {
                return resolve(type)
            }
        })

        Handlebars.registerHelper("path", (reference: string) => {
            if (coPrimitives[reference]) {
                return coPrimitives[reference].path
            }

            const linkedReference = this.resolveReference(reference)
            if (!linkedReference) {
                return undefined
            }
            const type = linkedReference.referenceType
            const obj = linkedReference.obj
            if (type === "types" || type === "actions") {
                const data = obj as Action | Type
                const category = this.getCategory(data.category)

                if (category) {
                    return `${this.basePath}/${type}/${category.id}/${obj.id}`
                }
            }
            return `${this.basePath}/${type}/${obj.id}`
        })
        Handlebars.registerHelper("markdown", (content: string | Handlebars.SafeString, indent: number = 0) => {
            if (content instanceof Handlebars.SafeString)
                content = content.toString()
            return new Handlebars.SafeString(content.replace(/\n/g, "<br />\n" + " ".repeat(indent)))
        })
        Handlebars.registerHelper("lineOf", (content: string, sliceQuery: string) => {
            if (!sliceQuery.includes(":")) {
                const line = parseInt(sliceQuery)
                const lines = content.split("\n")
                if (line < 0 || line >= lines.length) {
                    throw new Error(`Invalid line number: ${line}`)
                }

                return new Handlebars.SafeString(lines[line])
            }

            const range = sliceQuery.split(":")
            if (range.length !== 2) {
                throw new Error(`Invalid slice query: ${sliceQuery}`)
            }

            const start = range[0] === "" ? 0 : parseInt(range[0])
            const end = range[1] === "" ? content.length : parseInt(range[1])

            return new Handlebars.SafeString(content.slice(start, end))
        })

        Handlebars.registerHelper("expr", (a, operator, b) => {
            const boolA = !!a
            const boolB = !!b

            let result = false
            switch (operator) {
                case "==":
                    result = boolA === boolB
                    break
                case "!=":
                    result = boolA !== boolB
                    break
                case "&&":
                    result = boolA && boolB
                    break
                case "||":
                    result = boolA || boolB
                    break
                default:
                    throw new Error(`Unknown operator: ${operator}`)
            }

            return result
        })

        Handlebars.registerHelper("contains", (array: string[], value: string) => {
            return array && array.map(v => v.toLowerCase()).includes(value.toLowerCase())
        })

        Handlebars.registerHelper("not", (value: boolean) => {
            return !value
        })

        Handlebars.registerHelper("join", (array: string[], separator: string) => {
            return array.join(separator)
        })


        Handlebars.registerHelper("orderOf", (reference: string) => {
            return this.linker.getOrderOfReference(reference)
        })

        Handlebars.registerHelper("isMultiLine", (content: string) => {
            return content.includes("\n")
        })


        Handlebars.registerHelper("isEmpty", (contents: any[]) => {
            return contents == undefined || contents.length === 0
        })

        Handlebars.registerHelper("sort", (array: any[]) => {
            return array.sort()
        })

        handlebars.registerHelper("groupingWith", (array: any[], size: number) => {
            const result = []
            while (true) {
                const group = array.splice(0, size)
                if (group.length === 0) {
                    break
                }
                result.push(group)
            }

            return result
        })
    }

    private getCategory(categoryReference: string): Category {
        const reference = this.resolveReference(categoryReference)
        if (reference) {
            return reference.obj as Category
        } else {
            return undefined
        }
    }

    private getCategorisedOutputPath(type: string, id: string, categoryRef?: string) {
        if (categoryRef) {
            const category = this.getCategory(categoryRef)
            return path.join(this.outputDir, type, category.id, `${id}.${this.outputExt}`)
        } else {
            return path.join(this.outputDir, type, `${id}.${this.outputExt}`)
        }
    }

    private processOneFile(type: string, data: Action | Type | Category) {

        const template = this.templates.getCompiledTemplate(type)
        // noinspection TypeScriptValidateTypes
        const output = template(data)

        let outputFile
        if (type === "types" || type === "actions") {
            const dataWithCategory = data as Action | Type
            outputFile = this.getCategorisedOutputPath(type, dataWithCategory.id, dataWithCategory.category)
        } else /* type === "categories" */ {
            const category = data as Category
            outputFile = path.join(this.outputDir, category.phase, category.id, `index.${this.outputExt}`)
        }

        console.log("Writing to:", outputFile)
        LedgerSession.flushContent(output, outputFile)
    }

    private deleteTempFiles() {
        console.log("Deleting temp files...")
        try {
            fs.rmSync(this.tempDir, {recursive: true})
        } catch (error) {
            console.error("Error deleting temp files:", error)
        }
    }
}


export {
    LedgerSession,
    LedgerObject
}
