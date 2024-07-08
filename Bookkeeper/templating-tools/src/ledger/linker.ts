import {LedgerObject} from "./index"

const singular2plural = {
    "action": "actions",
    "event": "events",
    "type": "types",
    "category": "categories",
}

class ReferenceLinker {
    private readonly references: LinkedReference[] = []


    public addReference(reference: string, displayName: string, to: string, obj: LedgerObject) {
        this.references.push({reference, displayName, referenceType: to, obj})
    }

    public getReferences(): LinkedReference[] {
        return this.references
    }

    public resolveReference(reference: string): LinkedReference {
        return this.references.find((r) => r.reference === reference)
    }

    public reorderReferences() {
        this.references.sort((a, b) => a.reference.localeCompare(b.reference))
    }

    public getOrderOfReference(reference: string): number {
        if (!reference.startsWith("$ref:")) {
            throw new Error("Invalid reference format")
        }

        const referenceType = reference.split(":")[1]
        if (!singular2plural[referenceType]) {
            throw new Error("Invalid reference type")
        }
        const referenceTypePlural = singular2plural[referenceType]

        let idx = 0
        for (let i = 0; i < this.references.length; i++) {
            const ref = this.references[i]
            if (ref.referenceType === referenceTypePlural) {
                if (ref.reference === reference) {
                    return idx
                }
                idx++
            }
        }

        return -1
    }
}

interface LinkedReference {
    reference: string

    displayName: string
    referenceType: string
    path?: string
    obj: LedgerObject
}

export {
    ReferenceLinker,
    LinkedReference
}
