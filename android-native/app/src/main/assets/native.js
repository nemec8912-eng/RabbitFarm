if(typeof structuredClone!=='function')window.structuredClone=v=>JSON.parse(JSON.stringify(v));
if(typeof crypto.randomUUID!=='function')crypto.randomUUID=()=>{const b=crypto.getRandomValues(new Uint8Array(16));b[6]=(b[6]&15)|64;b[8]=(b[8]&63)|128;const h=[...b].map(n=>n.toString(16).padStart(2,'0')).join('');return h.slice(0,8)+'-'+h.slice(8,12)+'-'+h.slice(12,16)+'-'+h.slice(16,20)+'-'+h.slice(20)};
/* Storage and device services are available only in the bundled Android origin. */
(function(){if(!window.NativeFarm)return;const get=Storage.prototype.getItem,set=Storage.prototype.setItem;
const keys=new Set(['rabbitfarm-demo-v1','rabbitfarm-recovery-v1']);
Storage.prototype.getItem=function(key){if(!keys.has(key))return get.call(this,key);const value=NativeFarm.readStorage(key);return value||get.call(this,key)};
Storage.prototype.setItem=function(key,value){if(!keys.has(key))return set.call(this,key,value);if(!NativeFarm.writeStorage(key,String(value)))throw Error('Не удалось сохранить данные Android');};
})();
function syncNativeReminders(){if(!window.NativeFarm)return;try{const rows=db.litters.filter(l=>!l.weaned&&left('litter',l.id)>0).map(l=>({id:l.id,date:due(l),mother:l.female,count:left('litter',l.id)}));NativeFarm.syncReminders(JSON.stringify(rows));}catch(err){console.warn('Reminder sync failed',err)}}
function openNativeReminder(id){if(db.litters.some(l=>l.id===id))navigate('young',id)}
function nativeBack(){if($('#modal').open){$('#close').click();return}if(routeStack.length){const r=routeStack.pop();tab=r.tab;focusId=r.focusId;historyFilter=r.historyFilter;render();return}if(tab!=='overview'){navigate('overview');return}NativeFarm.closeApp()}
function initialiseNative(){if(!window.NativeFarm)return;try{
 if(NativeFarm.hasLegacy()&&!NativeFarm.migrationDone()&&!hasRecords(db)){
  const result=Backup.parse(JSON.parse(NativeFarm.legacyBackup()));replaceData(result.data,'Перенос старого Android');NativeFarm.markMigrationDone();
  mode='data';show('Данные перенесены',summaryData(db)+(result.warnings.length?'<div class="import-notes">'+result.warnings.map(w=>'<p>'+esc(w)+'</p>').join('')+'</div>':''),'Старая база сохранена без изменений. Сверьте показатели и сохраните резервную копию.');
 }
}catch(err){toast('Старая база сохранена, перенос не завершён: '+err.message)}syncNativeReminders()}
document.addEventListener('DOMContentLoaded',initialiseNative);
