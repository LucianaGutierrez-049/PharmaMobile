const { chromium } = require('C:/Users/lucia/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const path = require('node:path');
(async () => {
 const browser=await chromium.launch({executablePath:'C:/Program Files/Google/Chrome/Application/chrome.exe',headless:true,args:['--disable-gpu']});
 const page=await browser.newPage({viewport:{width:1100,height:850},deviceScaleFactor:1});
 const items=[['migracion.html','E02_migracion'],['base_local.html','E02_base_local'],['sincronizacion.html','E03_sincronizacion'],['permisos.html','E09_manifest'],['firebase.html','E10_firebase'],['pruebas/index.html','E11_pruebas'],['git.html','E12_historial']];
 for(const [route,name] of items){
  await page.goto('http://127.0.0.1:8877/'+route,{waitUntil:'load',timeout:30000});
  await page.screenshot({path:path.join(__dirname,'capturas',name+'.png'),fullPage:false});
  console.log(name,await page.title());
 }
 await browser.close();
})().catch(e=>{console.error(e.message);process.exitCode=1;});
