const { chromium } = require('C:/Users/lucia/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const path = require('node:path');
const { pathToFileURL } = require('node:url');
(async () => {
  const root = path.resolve(__dirname, '../..');
  const browser = await chromium.launch({executablePath:'C:/Program Files/Google/Chrome/Application/chrome.exe',headless:true,args:['--disable-gpu']});
  try {
    const page = await browser.newPage({viewport:{width:1100,height:900},deviceScaleFactor:1});
    for (const [source, name] of [['pruebas/index.html','E13_pruebas_20261104.png'],['correcciones.html','E14_correcciones_20261104.png']]) {
      await page.goto(pathToFileURL(path.join(root,'tmp/s13_cierre',source)).href,{waitUntil:'load'});
      await page.screenshot({path:path.join(__dirname,'capturas',name),fullPage:false});
      console.log(name);
    }
  } finally { await browser.close(); }
})().catch(error => { console.error(error.message); process.exitCode=1; });
