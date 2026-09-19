import { defineConfig } from 'vitepress'

// GitHub Pages serves a project site from /<repository>/. For a custom domain or a user site, build with DOCS_BASE=/.
const base = process.env.DOCS_BASE ?? '/SkippingStone/'
const STONE_ICON = 'https://storage.googleapis.com/coolerpromc/textures/skippingstone/skipping_stone_perfect.png'

export default defineConfig({
  title: 'Skipping Stone',
  description: 'Skip stones across water in Minecraft 26.1+: shoreline stones, a timing-based power meter, real skipping physics and personal records.',
  base,
  cleanUrls: true,
  srcExclude: ['README.md', 'scripts/**'],
  head: [['link', { rel: 'icon', type: 'image/png', href: STONE_ICON }]],
  themeConfig: {
    logo: { src: STONE_ICON, alt: '' },
    nav: [
      { text: 'Guide', link: '/guide/getting-started' },
      { text: 'The math', link: '/guide/the-math' },
      { text: 'Configuration', link: '/configuration' },
    ],
    sidebar: [
      {
        text: 'Guide',
        items: [
          { text: 'Getting started', link: '/guide/getting-started' },
          { text: 'Finding stones', link: '/guide/finding-stones' },
          { text: 'Stone quality', link: '/guide/stone-quality' },
          { text: 'Throwing', link: '/guide/throwing' },
          { text: 'The math', link: '/guide/the-math' },
          { text: 'Records', link: '/guide/records' },
        ],
      },
      {
        text: 'Reference',
        items: [
          { text: 'Configuration', link: '/configuration' },
          { text: 'Commands', link: '/commands' },
        ],
      },
      { text: 'FAQ', link: '/faq' },
    ],
    search: { provider: 'local' },
    outline: { level: [2, 3] },
    footer: { message: 'Released under the MIT License. Stone skipping behaviour adapted from Hezaerd/Skipping-Stones (MIT).' },
  },
})
