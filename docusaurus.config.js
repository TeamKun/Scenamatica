// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require("prism-react-renderer/themes/github");
const darkCodeTheme = require("prism-react-renderer/themes/dracula");

/** @type {import("@docusaurus/types").Config} */
const config = {
  title: "Scenamatica",
  tagline: "世界初のプラグインのシナリオテスト自動化ツール",
  favicon: "img/favicon.ico",
  url: "https://scenamatica.kunlab.org/",
  baseUrl: "/",

  clientModules: [
    require.resolve("@fontsource/jetbrains-mono/index.css"),
    require.resolve("@fortawesome/fontawesome-free/css/all.min.css"),
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
            src: "img/logo.png",
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
              to: "/docs/expressions",
              position: "left",
              label: "用語集",
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
                  label: "用語集",
                  to: "/docs/expressions",
                },
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
          theme: lightCodeTheme,
          darkTheme: darkCodeTheme,
          additionalLanguages: ["java", "yaml"],
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
      }),
};

module.exports = config;
