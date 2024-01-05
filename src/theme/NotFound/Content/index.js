import React from 'react';
import clsx from 'clsx';
import Translate from '@docusaurus/Translate';
import Heading from '@theme/Heading';
import Link from "@docusaurus/Link";
export default function NotFoundContent({className}) {
  return (
    <main className={clsx('container margin-vert--xl', className)}>
      <div className="row">
        <div className="col col--6 col--offset-3">
          <Heading as="h1" className="hero__title">ページが見つかりません</Heading>
          <p>
            お探しのページが見つかりませんでした。URLが正しいかご確認ください。
          </p>
        </div>
        <div className="col col--9 col--offset-3">
          <Heading as="h2" className="hero__subtitle">便利リンク集</Heading>
          <ul>
            <li><Link to="/docs">ドキュメントホーム</Link></li>
            <li><Link to="/docs/getting-started/installation">Scenamatica インストールガイド</Link></li>
            <li><Link to="/docs/use/scenario/actions">アクション一覧</Link></li>
            <li><Link to="/docs/getting-started/tutorials">各種チュートリアル</Link></li>
            <li><Link to="/docs/use/scenario/types#scenario-file-structure">シナリオファイルの構造</Link></li>
          </ul>
        </div>
      </div>
    </main>
  );
}
