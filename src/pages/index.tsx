import React from "react"
import clsx from "clsx"
import Link from "@docusaurus/Link"
import useDocusaurusContext from "@docusaurus/useDocusaurusContext"
import Layout from "@theme/Layout"
import Features from "@site/src/components/Features"

import styles from "./index.module.css"

function Header() {
    const { siteConfig } = useDocusaurusContext()
    return (
        <header className={clsx("hero hero--primary", styles.heroBanner)}>
            <div className="container">
                <h1 className="hero__title">{siteConfig.title}</h1>
                <p className="hero__subtitle">{siteConfig.tagline}</p>
                <div className={styles.buttons}>
                    <Link className="button button--secondary button--lg" to="/docs/home">
                        Scenamatica を使ってみる &gt;
                    </Link>
                </div>
            </div>
        </header>
    )
}

export default function Home(): JSX.Element {
    const { siteConfig } = useDocusaurusContext()
    return (
        <Layout title={siteConfig.title} description="Scenamatica - 世界初の PaperMC プラグインのシナリオテスト自動化ツール">
            <Header />
            <main>
            </main>
        </Layout>
    )
}
