import * as path from "node:path";
import * as fs from "node:fs";
import * as Handlebars from "handlebars";

const TEMPLATE_FILE_NAMES = [
    "types",
    "actions",
    "categories"
]

class TemplateRetriever {
    private readonly directoryPath: string;
    private readonly templateFiles: { [key: string]: string } = {};
    private readonly compiledTemplates: { [key: string]: Handlebars.TemplateDelegate } = {};

    public constructor(directoryPath: string) {
        this.directoryPath = directoryPath;
        // ディレクトリの存在チェック
        if (!fs.existsSync(this.directoryPath)) {
            throw new Error("Directory does not exist: " + this.directoryPath);
        }
    }

    public checkFilesExist(): string[] {
        // ファイルの存在チェック
        try {
            const files = fs.readdirSync(this.directoryPath).map(file => path.parse(file).name);
            return TEMPLATE_FILE_NAMES.filter(templateFile => files.some(file => file === templateFile));
        } catch (e) {
            console.error("Error reading directory:", e);
            throw e;
        }
    }

    public loadAllTemplates(): { [key: string]: string } {
        const templates: { [key: string]: string } = {};
        TEMPLATE_FILE_NAMES.forEach(templateName => {
            templates[templateName] = this.getTemplateFile(templateName);
        });
        return templates;
    }

    public getTemplateFile(templateName: string): string {
        if (this.templateFiles[templateName]) {
            return this.templateFiles[templateName];
        }

        // 拡張子が不明なので、ファイル名から推測する
        const files = fs.readdirSync(this.directoryPath);
        const file = files.find(file => file.startsWith(templateName + "."));
        if (!file) {
            throw new Error("Template file not found: " + templateName);
        }
        const finalTemplatePath = path.join(this.directoryPath, file);

        const template = fs.readFileSync(finalTemplatePath).toString();
        return this.templateFiles[templateName] = template;
    }

    public getCompiledTemplate(templateName: string): Handlebars.TemplateDelegate {
        if (this.compiledTemplates[templateName]) {
            return this.compiledTemplates[templateName];
        }

        const template = this.getTemplateFile(templateName);
        return this.compiledTemplates[templateName] = Handlebars.compile(template);
    }

    public compileAll(): { [key: string]: Handlebars.TemplateDelegate } {
        const templates: { [key: string]: Handlebars.TemplateDelegate } = {};
        // templateFiles には、ファイル名とテンプレートの内容が格納されている
        for (const templateName in this.templateFiles) {
            const template = this.templateFiles[templateName];
            templates[templateName] = Handlebars.compile(template);
        }

        return templates;
    }
}

export {
    TemplateRetriever
}
