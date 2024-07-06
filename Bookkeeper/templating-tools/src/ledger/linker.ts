import {LedgerObject} from "./index";

class ReferenceLinker {
    private readonly references: LinkedReference[] = [];


    public addReference(reference: string, displayName: string, to: string, obj: LedgerObject) {
        this.references.push({reference, displayName, to, obj});
    }

    public getReferences(): LinkedReference[] {
        return this.references;
    }

    public resolveReference(reference: string): LinkedReference {
        return this.references.find((r) => r.reference === reference);
    }
}

interface LinkedReference {
    reference: string

    displayName: string;
    to: string;
    obj: LedgerObject
}

export {
    ReferenceLinker,
    LinkedReference
}
