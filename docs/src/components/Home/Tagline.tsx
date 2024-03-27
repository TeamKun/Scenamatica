import taglineStyles from "./Tagline.module.css";

const Tagline = (): JSX.Element => {
    return (
        <pre>
            <p className={taglineStyles.tagline}>
                <span className={taglineStyles.keepSpace}><span className={taglineStyles.verb}>With </span>
                <span className={taglineStyles.noun}>your </span>
                </span>
                <span className={taglineStyles.etymo}>Scena</span>
                <span className={taglineStyles.nonEtymo}>
                    <span className={taglineStyles.keepSpace}>rio, </span>
                    <span className={taglineStyles.verb}>Build </span>
                    <span className={taglineStyles.keepSpace}>
                    <span className={taglineStyles.noun}>your </span>
                        Plugin,
                    </span>
                    <span className={taglineStyles.verb}> Run </span>
                    <span className={taglineStyles.keepSpace}>
                        <span className={taglineStyles.noun}>your </span>
                        Tests auto
                    </span>
                </span>
                <span className={taglineStyles.etymo}>matica</span>
                <span className={taglineStyles.afterEtymo}>lly.</span>
            </p>
        </pre>
    );
};

export default Tagline;
