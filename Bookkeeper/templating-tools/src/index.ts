import {Command} from "commander"
import {LedgerSession} from "./ledger";
import {TemplateRetriever} from "./template/retriever";

const ARGS_PARSER = new Command()
    .requiredOption("-t, --template <template>", "The directory of the template")
    .requiredOption("-o, --output <output>", "The directory to output the templated files to")
    .requiredOption("-e, --output-ext <outputExt>", "The extension of the output files")
    .argument("<ledger>", "The file of the ledger")
    .addHelpText("after", `
Examples:
  $ templating-tools -t templates dist
`).parse()

interface Arguments {
    template: string
    ledger: string
    output: string
    outputExt: string
}

const main = () => {
    const opts = ARGS_PARSER.opts()
    const args = ARGS_PARSER.args
    const parsedArgs: Arguments = {
        template: opts.template,
        ledger: args[0],
        output: opts.output,
        outputExt: opts.outputExt
    }

    const templateSession = new TemplateRetriever(parsedArgs.template)
    const ledgerSession = new LedgerSession(parsedArgs.ledger, parsedArgs.output,
        templateSession, parsedArgs.outputExt
    )
    ledgerSession.init()
    templateSession.compileAll()

    console.log("Validating template files...")
    try {
        templateSession.checkFilesExist()
        templateSession.loadAllTemplates()
    } catch (e) {
        console.log("Failed to validate template files:", e)
        return
    }

    console.log("Reading ledger file...")
    ledgerSession.extract()
    ledgerSession.processLedger()

    console.log("Finishing...")
    // ledgerSession.dispose()
}

main()
