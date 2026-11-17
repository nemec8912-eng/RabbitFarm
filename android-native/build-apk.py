#!/usr/bin/env python3
"""Builds with official SDK build-tools + Eclipse ECJ (Java runtime only required)."""
import argparse,pathlib,subprocess,zipfile,xml.etree.ElementTree as ET,os,shutil
p=argparse.ArgumentParser();p.add_argument('--tools',required=True);p.add_argument('--android-jar',required=True);p.add_argument('--ecj',required=True);p.add_argument('--out',required=True);p.add_argument('--preview',action='store_true');p.add_argument('--keystore');p.add_argument('--alias',default='androiddebugkey');p.add_argument('--storepass-env',default='ANDROID_STORE_PASSWORD');a=p.parse_args()
root=pathlib.Path(__file__).resolve().parent;out=pathlib.Path(a.out).resolve();out.mkdir(parents=True,exist_ok=True);tools=pathlib.Path(a.tools).resolve();android=pathlib.Path(a.android_jar).resolve();ecj=pathlib.Path(a.ecj).resolve();classes=out/'classes';classes.mkdir(exist_ok=True)
def run(args):subprocess.run([str(s) for s in args],check=True)
run(['java','-jar',ecj,'-1.8','-proc:none','-nowarn','-bootclasspath',str(android)+os.pathsep+str(tools/'core-lambda-stubs.jar'),'-d',classes,*sorted((root/'app/src/main/java').rglob('*.java'))])
with zipfile.ZipFile(out/'classes.jar','w') as z:
 for f in classes.rglob('*.class'):z.write(f,f.relative_to(classes))
run(['java','-cp',tools/'lib/d8.jar','com.android.tools.r8.D8','--lib',android,'--min-api','28','--output',out,out/'classes.jar'])
ET.register_namespace('android','http://schemas.android.com/apk/res/android');ns='{http://schemas.android.com/apk/res/android}'
manifest=ET.parse(root/'app/src/main/AndroidManifest.xml');e=manifest.getroot();e.set('package','com.rabbit.farm.preview' if a.preview else 'com.rabbit.farm');e.set(ns+'versionCode','3');e.set(ns+'versionName','2.0-preview' if a.preview else '2.0');
if a.preview:e.find('application').set(ns+'label','Ушастый учёт · тест')
manifest.write(out/'AndroidManifest.xml',encoding='utf-8',xml_declaration=True)
run([tools/'aapt2','compile','--dir',root/'app/src/main/res','-o',out/'resources.zip'])
run([tools/'aapt2','link','-I',android,'--manifest',out/'AndroidManifest.xml','-A',root/'app/src/main/assets','-o',out/'unsigned.apk',out/'resources.zip'])
with zipfile.ZipFile(out/'unsigned.apk','a') as z:z.write(out/'classes.dex','classes.dex',compress_type=zipfile.ZIP_DEFLATED)
run([tools/'zipalign','-f','4',out/'unsigned.apk',out/'aligned.apk'])
if a.keystore:
 result=out/('RabbitFarm-preview.apk' if a.preview else 'RabbitFarm-update.apk')
 run(['java','-jar',tools/'lib/apksigner.jar','sign','--ks',pathlib.Path(a.keystore).resolve(),'--ks-key-alias',a.alias,'--ks-pass','env:'+a.storepass_env,'--key-pass','env:'+a.storepass_env,'--out',result,out/'aligned.apk'])
 run(['java','-jar',tools/'lib/apksigner.jar','verify','--verbose',result]);print('APK:',result)
else:print('UNSIGNED APK (not installable until signed):',out/'aligned.apk')
