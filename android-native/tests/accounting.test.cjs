const assert=require('node:assert/strict');const Farm=require('../app/src/main/assets/accounting.js');
const db={adults:[{id:'a',sex:'male'},{id:'b',sex:'female'}],litters:[{id:'l',count:8,male:'M',female:'F',weaned:false}],matings:[{}],movements:[]};
const balance=()=>{const t=Farm.totals(db);assert.equal(t.lifetime,t.current+t.sold+t.meat+t.death);return t};
assert.equal(balance().current,10);db.litters[0].weaned=true;assert.equal(balance().young,8);
Farm.promote(db,{sourceId:'l',count:3,sex:'female',name:'New',id:'p',date:'2026-11-14'});assert.equal(balance().young,5);assert.equal(balance().females,4);assert.equal(balance().lifetime,10);
Farm.move(db,{kind:'adult',sourceId:'p-0',reason:'sold',count:1,id:'s'});Farm.move(db,{kind:'litter',sourceId:'l',reason:'meat',count:2,id:'m'});Farm.move(db,{kind:'litter',sourceId:'l',reason:'death',count:1,id:'d'});assert.equal(balance().current,6);assert.equal(balance().sold,1);assert.equal(balance().meat,2);assert.equal(balance().death,1);
const before=JSON.stringify(db);for(const count of [3,-1,0,1.5])assert.throws(()=>Farm.move(db,{kind:'litter',sourceId:'l',reason:'sold',count}));assert.throws(()=>Farm.move(db,{kind:'adult',sourceId:'p-0',reason:'death',count:1}));assert.throws(()=>Farm.promote(db,{sourceId:'l',count:3,sex:'male'}));assert.throws(()=>Farm.promote(db,{sourceId:'l',count:1,sex:'unknown'}));assert.equal(JSON.stringify(db),before);
console.log('Passed: balance, weaning, promotion, sale, meat, death, overdraw and invalid counts.');
