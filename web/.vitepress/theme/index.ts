import DefaultTheme from 'vitepress/theme'
import './minecraft-hover.css'
import type { Theme } from 'vitepress/client';
import tippy from 'tippy.js';
import { onMounted } from 'vue'
const theme: Theme = {
	extends: DefaultTheme,
	setup() {
		onMounted(() => {
			document.querySelectorAll("code[fqfi]").forEach(entry => {
				const [ns, path] = parseIdent(entry.textContent)
				tippy(entry, {
					content: `This is a fully-qualified file identifier. You would edit the file <code>assets/${ns}/${path}</code>.`,
					allowHTML: true,
					theme: 'translucent'
				})
			})
		})
	},
}
function parseIdent(str: string): [string, string] {
	const parts = str.split(":")
	if (parts.length == 1)
		return ["minecraft", parts[0]]
	if (parts.length == 2)
		return [parts[0], parts[1]]
	throw `${str} is not a valid identifier`
}
export default theme;