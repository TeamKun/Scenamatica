// Note: type annotations allow type checking and IDEs autocompletion

const {themes} = require("prism-react-renderer");

/** @type {import("@docusaurus/types").Config} */
const config = {
  title: "Scenamatica",
  tagline: "Let's enhance your PaperMC plugin quality with Scenamatica: YAML-defined scenarios, CI/CD integration.",
  favicon: "img/favicon.ico",
  url: "https://scenamatica.kunlab.org/",
  baseUrl: "/",

  clientModules: [
    require.resolve("@fontsource/jetbrains-mono/index.css")
  ],

  // GitHub pages deployment config.
  // If you aren"t using GitHub pages, you don"t need these.
  organizationName: "TeamKUN", // Usually your GitHub org/user name.
  projectName: "ScenamaticaDocs", // Usually your repo name.

  onBrokenLinks: "throw",
  onBrokenMarkdownLinks: "warn",

  // Even if you don"t use internalization, you can use this field to set useful
  // metadata like html lang. For example, if your site is Chinese, you may want
  // to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: "ja",
    locales: ["ja"],
  },

  plugins: [
    require.resolve("docusaurus-plugin-image-zoom"),
    [
      "@docusaurus/plugin-client-redirects",
      {
        redirects: [
          {
            to: "/docs/home",
            from: ["/docs/"],
          },
        ],
      },
    ],
    [
      "@docusaurus/plugin-pwa",
      {
        debug: false,
        pwaHead: [
          {
            tagName: "link",
            rel: "icon",
            href: "/img/favicon.ico",
          },
          {
            tagName: "link",
            rel: "manifest",
            href: "/manifest.json",
          },
          {
            tagName: "meta",
            name: "theme-color",
            content: "#53BBFFFF",
          },
          {
            tagName: "meta",
            name: "apple-mobile-web-app-capable",
            content: "yes",
          },
          {
            tagName: "meta",
            name: "apple-mobile-web-app-status-bar-style",
            content: "black",
          },
          {
            tagName: "link",
            rel: "apple-touch-icon",
            href: "/img/logo2.png",
          },
          {
            tagName: "link",
            rel: "mask-icon",
            href: "/img/logo2.png",
            color: "#53BBFFFF",
          },
          {
            tagName: "meta",
            name: "msapplication-TileImage",
            content: "/img/logo2.png",
          },
          {
            tagName: "meta",
            name: "msapplication-TileColor",
            content: "#53BBFFFF",
          },
        ],
      }
    ]
  ],

  presets: [
    [
      "classic",
      /** @type {import("@docusaurus/preset-classic").Options} */
      ({
        docs: {
          sidebarPath: require.resolve("./sidebars.js"),
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl: "https://github.com/TeamKUN/ScenamaticaDocs/edit/develop/",
          showLastUpdateAuthor: true,
          showLastUpdateTime: true,
          lastVersion: "current",
          versions: {
            current: {
              label: "Scenamatica Beta",
            },
          },
        },
        theme: {
          customCss: require.resolve("./src/css/custom.css"),
        },
      }),
    ],
  ],

  themeConfig:
  /** @type {import("@docusaurus/preset-classic").ThemeConfig} */
      ({
        announcementBar: {
          id: "announcementBar-1",
          content: `✨ もし Scenamatica が気に入ったなら、 GitHub で <a href="https://github.com/TeamKUN/Scenamatica">Star</a> をしてください！`,
        },
        navbar: {
          title: "Scenamatica",
          logo: {
            alt: "Scenamatica Logo",
            src: "img/logo2.png",
          },
          items: [
            {
              to: "/docs/home",
              position: "left",
              label: "ホーム",
            },
            {
              to: "/docs/getting-started",
              position: "left",
              label: "使い始める",
            },
            {
              to: "/docs/use/scenario",
              position: "left",
              label: "シナリオ",
            },
            {
              label: "コマンド",
              position: "left",
              to: "/docs/use/commandS",
            },
            {
              type: "docsVersionDropdown",
              position: "right",
              dropdownActiveClassDisabled: true,
            },
            {
              href: "https://github.com/TeamKUN/Scenamatica/releases",
              position: "right",
              label: "ダウンロード",
            },
            {
              href: "https://github.com/TeamKUN/Scenamatica",
              position: "right",
              className: "icon-link i-github",
            },
          ],
        },
        footer: {
          style: "dark",
          links: [
            {
              title: "ドキュメント",
              items: [
                {
                  label: "ホーム",
                  to: "/docs/home",
                },
                {
                  label: "使い始める",
                  to: "/docs/getting-started",
                },
                {
                  label: "チュートリアル",
                  to: "/docs/getting-started/tutorials",
                },
                {
                  label: "コマンド",
                  to: "/docs/use/commandS",
                },
                {
                  label: "シナリオ",
                  to: "/docs/use/scenario",
                }
              ],
            },
            {
              title: "開発者 - Peyang",
              items: [
                {
                  label: "GitHub",
                  href: "https://github.com/PeyaPeyaPeyang",
                },
                {
                  label: "Twitter",
                  href: "https://twitter.com/peyang9799",
                },
                {
                  label: "Email",
                  href: "mailto:peyang@peya.tokyo",
                },
              ],
            },
            {
              title: "Scenamatica",
              items: [
                {
                  label: "GitHub",
                  href: "https://github.com/TeamKUN/Scenamatica",
                },
                {
                  label: "バグを報告",
                  href: `https://github.com/TeamKUN/Scenamatica/issues/new`,
                },
                {
                  label: "機能をリクエスト",
                  href: `https://github.com/TeamKUN/Scenamatica/issues/new`,
                },
              ],
            },
          ],
          copyright: `Copyright &copy; ${new Date().getFullYear()} <a href="https://github.com/TeamKUN">KUN Development Team</a>., <a href="https://peya.tokyo/">Peyang</a>`,
        },
        prism: {
          theme: themes.github,
          darkTheme: themes.vsDark,
          additionalLanguages: ["java", "yaml", "ebnf"],
        },
        zoom: {
          background: {
            light: "rgba(0, 0, 0, 0.6)",
            dark: "rgba(0, 0, 0, 0.6)",
          },
        },
        colorMode: {
          defaultMode: "dark",
          respectPrefersColorScheme: false,
        },
        docs: {
          sidebar: {
            autoCollapseCategories: true,
            hideable: true,
          },
        },
        algolia: {
          appId: "QWWI0CDV6V",
          apiKey: "cc1542c44da9a2e9d96d7d681ea7ee04",
          indexName: "scenamatica-kunlab",
          contextualSearch: true,
          searchPagePath: "search",
        }
      }),
};

module.exports = config;
