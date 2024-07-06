import * as path from "node:path"
import * as fs from "node:fs"
import * as AdmZip from "adm-zip"
import {LinkedReference, ReferenceLinker} from "./linker"
import {TemplateRetriever} from "../template/retriever"
import * as Handlebars from "handlebars";

interface LedgerObject {

}

interface Category extends LedgerObject {
    $reference: string
    id: string
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
    $reference: string
    id: string
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
    $reference: string
    super?: string
    id: string
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
    id: string
    name: string
    javadoc?: string
    javadocLink?: string
    source: EventSource
    descriptions?: Descriptions
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
    private readonly linker: ReferenceLinker
    private readonly templates: TemplateRetriever

    constructor(ledgerFile: string, outputDir: string, templates: TemplateRetriever, outputExt: string) {
        this.ledgerFile = ledgerFile
        this.outputDir = outputDir
        this.templates = templates
        this.outputExt = outputExt.startsWith(".") ? outputExt.slice(1) : outputExt

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
        data: Category | Action | Type
    })[]) {
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

        Handlebars.registerHelper("resolve", (reference: string, property: string) => {
            const linkedReference = this.resolveReference(reference)
            if (linkedReference) {
                return new Handlebars.SafeString(linkedReference.obj[property])
            } else {
                throw new Error(`Reference not found: ${reference}`)
            }
        })
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

        const targets = []
        LedgerSession.TYPES.forEach(type => {
            if (type === "events") {
                // イベントは処理しない
                return
            }

            const directory = path.join(this.tempDir, type)
            if (!fs.existsSync(directory)) {
                console.error(`Unable to find ${type} directory.`)
                return
            }

            const files = LedgerSession.enumerateFilesRecursively(directory)

            for (const file of files) {
                if (!file.endsWith(".json")) {
                    console.debug("Skipping non-JSON file:", file)
                    return
                }

                try {
                    const content = fs.readFileSync(file).toString()
                    const data: Category | Action | Type = JSON.parse(content)
                    targets.push({type, data})
                } catch (error) {
                    console.error(`Error reading file ${file}:`, error)
                }
            }
        })

        for (const target of targets) {
            const {type, data} = target
            // カテゴリには自身の子を追加しておく
            if (type === "categories") {
                const category = data as Category
                const categoryReference = category.$reference
                category.children = LedgerSession.getElementsByCategory(categoryReference, targets)
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

    private getCategory(categoryReference: string): Category {
        const reference = this.resolveReference(categoryReference)
        if (reference) {
            return reference.obj as Category
        } else {
            throw new Error(`Category not found: ${categoryReference}`)
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
        const reference = data.$reference
        const displayName = data.name

        const template = this.templates.getCompiledTemplate(type)
        // noinspection TypeScriptValidateTypes
        const output = template(data)

        // Write the output to the output directory
        this.linker.addReference(reference, displayName, type, data)

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
