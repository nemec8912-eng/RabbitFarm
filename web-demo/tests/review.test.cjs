const assert=require('node:assert/strict'),vm=require('node:vm'),fs=require('node:fs'),path=require('node:path');
const flags={fail:false},elements=new Map(),listeners={},el=s=>{if(!elements.has(s))elements.set(s,{innerHTML:'',style:{},setAttribute(){},addEventListener(){},close(){},showModal(){}});return elements.get(s)};
const ctx=vm.createContext({Farm:require('../accounting.js'),Backup:require('../backup.js'),document:{querySelector:el,querySelectorAll:()=>[],addEventListener:(name,fn)=>listeners[name]=fn},localStorage:{getItem:()=>null,setItem(){if(flags.fail)throw Error('QuotaExceeded')}},window:{scrollTo(){}},crypto:{randomUUID:()=>Math.random().toString(36).slice(2)},structuredClone,Date,console,setTimeout:()=>{}});
vm.runInContext(fs.readFileSync(path.join(__dirname,'../app.js'),'utf8'),ctx);
vm.runInContext(`
if(!act('Action','data-new="adult"').includes('type="button"'))throw Error('Action accidentally submits form');
profileOriginal={ageMonths:18,ageDate:'2025-01-01'};photoDraft='';let current=ageMonths(profileOriginal);let result=readProfile({get:()=>String(current),has:()=>true});if(result.ageMonths!==18||result.ageDate!=='2025-01-01')throw Error('Photo edit changed age reference');
profileOriginal={birthDate:'2024-01-20'};result=readProfile({get:()=>String(ageMonths(profileOriginal)),has:()=>false});if(result.ageMonths!==null)throw Error('Known birth date lost precision');
db.adults=[{id:'one',sex:'male',name:'Same'},{id:'two',sex:'male',name:'Same'}];if(namedParent({male:'Same'},'male')!==null)throw Error('Ambiguous parent silently selected');if(namedParent({maleId:'two',male:'Same'},'male').id!=='two')throw Error('Explicit parent ID lost');
db.litters=[{id:'l',count:2,born:'2026-01-01',weaned:false,maleId:'two'}];db.matings=[{id:'m',archived:true,done:false}];const oldMode=mode;openLitter('m');if(mode!==oldMode)throw Error('Archived mating accepts new birth');
`,ctx);
flags.fail=true;listeners.click({target:{closest:()=>({dataset:{wean:'l'}})}});assert.equal(vm.runInContext("db.litters[0].weaned",ctx),false);
vm.runInContext("Farm.promote(db,{sourceId:'l',count:1,sex:'male',name:'New',date:today(),id:'p'});if(db.adults.find(a=>a.id==='p-0').maleId!=='two')throw Error('Promotion lost parent ID')",ctx);
console.log('Passed: safe action buttons, stable age on photo edit, exact birth age, ambiguous parents, archived matings, failed weaning save rollback, inherited parent IDs.');
