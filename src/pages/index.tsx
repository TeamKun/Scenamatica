import React from "react"
import clsx from "clsx"
import Link from "@docusaurus/Link"
import useDocusaurusContext from "@docusaurus/useDocusaurusContext"
import Layout from "@theme/Layout"

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
                <div className={styles.container}>
                    <h2>概要</h2>
                    <p className={styles.description}>
                        <span>Scenamatica は, PaperMC プラグイン開発者向けのシナリオテスト自動化フレームワークです。</span>
                    </p>
                    <p className={styles.description}>
                        <span>あなたの PaperMC プラグインが正常に動作しているか,</span>
                        <span>あるいはデグレードしていないかを自動でチェックできます。</span>
                    </p>
                    <p className={styles.description}>
                        <span>シナリオを YAML で事前に定義しておき任意のタイミングで実行します。</span>
                    </p>
                </div>
            </main>
        </Layout>
    )
}
