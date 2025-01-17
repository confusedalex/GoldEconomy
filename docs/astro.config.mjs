// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
// https://astro.build/config
export default defineConfig({
	site: "https://thegoldeconomy.confusedalex.dev",
	integrations: [
		starlight({
			title: 'TheGoldEconomy',
			description: 'TheGoldEconomy is a simple, easy to use, and powerful economy plugin for Minecraft servers.',
			editLink: {
				baseUrl: 'https://github.com/confusedalex/goldeconomy-docs/edit/main/docs',
			},
			logo: {
				src: './src/assets/logo.svg',
				replacesTitle: true,
			},
			social: {
				github: 'https://github.com/confusedalex/goldeconomy',
			},
			sidebar: [
				'installation', 'configuration', 'commands', 'permissions',
			],
		}),
	],

});
