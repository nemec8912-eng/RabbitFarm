package com.rabbit.farm;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.util.AtomicFile;
import android.webkit.*;
import android.widget.Toast;
import android.content.pm.PackageManager;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

/** Offline-only app: privileged bridge is exposed only to bundled, CSP-restricted assets. */
public class MainActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> chooser;
    private String pendingExport;
    private static final String ORIGIN="https://rabbitfarm.local/";
    private final java.util.concurrent.ExecutorService io=Executors.newSingleThreadExecutor();
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(22,77,54));
        getWindow().setNavigationBarColor(Color.rgb(243,247,238));
        web=new WebView(this); android.widget.FrameLayout root=new android.widget.FrameLayout(this); root.setBackgroundColor(Color.rgb(243,247,238)); root.addView(web,new android.widget.FrameLayout.LayoutParams(-1,-1)); root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets.consumeSystemWindowInsets();}); setContentView(root);
        WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);s.setAllowContentAccess(true);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.addJavascriptInterface(new Bridge(),"NativeFarm");
        web.setWebViewClient(new WebViewClient(){
            @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest r){
                Uri u=r.getUrl();String p=u.getPath();
                if(!"https".equals(u.getScheme())||!"rabbitfarm.local".equals(u.getHost()))return blocked();
                if(p==null||p.equals("/"))p="/index.html";
                if(!p.matches("/[A-Za-z0-9_.-]+"))return blocked();
                try {String file=p.substring(1),mime=file.endsWith(".js")?"application/javascript":file.endsWith(".css")?"text/css":"text/html";
                    Map<String,String> h=new HashMap<>();h.put("Content-Security-Policy","default-src 'none'; script-src 'self'; style-src 'self'; img-src 'self' data: blob:; connect-src 'none'; frame-src 'none'; object-src 'none'; base-uri 'none'; form-action 'none'");
                    return new WebResourceResponse(mime,"UTF-8",200,"OK",h,getAssets().open(file));
                }catch(Exception e){return blocked();}
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return !r.getUrl().toString().startsWith(ORIGIN);}
            @Override public void onPageFinished(WebView v,String url){String id=getIntent().getStringExtra("litterId");if(id!=null)openReminder(id);}
        });
        web.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onShowFileChooser(WebView w,ValueCallback<Uri[]> callback,FileChooserParams params){
                if(chooser!=null)chooser.onReceiveValue(null);chooser=callback;
                Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);
                String[] types=params.getAcceptTypes();String type="*/*";
                if(types.length>0&&types[0]!=null&&!types[0].isEmpty())type=types[0].contains("image")?"image/*":"application/json";
                intent.setType(type);intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                try{startActivityForResult(intent,501);}catch(Exception e){chooser.onReceiveValue(null);chooser=null;}
                return true;
            }
        });
        web.loadUrl(ORIGIN+"index.html");
        Reminders.ensureChannel(this);
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED
            &&!getSharedPreferences("native_state",MODE_PRIVATE).getBoolean("asked_notifications",false)){
            new AlertDialog.Builder(this).setTitle("Напоминания об отсадке")
                .setMessage("Разрешите уведомления, чтобы получать напоминания на 45-й день, даже когда приложение закрыто. Внутри приложения напоминания останутся в любом случае.")
                .setPositiveButton("Разрешить",(d,w)->{getSharedPreferences("native_state",MODE_PRIVATE).edit().putBoolean("asked_notifications",true).apply();requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},502);})
                .setNegativeButton("Позже",(d,w)->{}).show();
        }
    }
    private WebResourceResponse blocked(){return new WebResourceResponse("text/plain","UTF-8",403,"Forbidden",Collections.emptyMap(),new ByteArrayInputStream(new byte[0]));}
    private void openReminder(String id){web.evaluateJavascript("if(typeof openNativeReminder==='function')openNativeReminder("+JSONObject.quote(id)+")",null);}
    @Override protected void onResume(){super.onResume();io.execute(()->{try{Reminders.reschedule(this);}catch(Exception ignored){}});}
    @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);if(i.hasExtra("litterId"))openReminder(i.getStringExtra("litterId"));}
    @Override public void onBackPressed(){web.evaluateJavascript("nativeBack()",null);}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);
        if(req==501){if(chooser!=null){Uri[] selected=result==RESULT_OK&&data!=null&&data.getData()!=null?new Uri[]{data.getData()}:null;
            if(selected!=null)try{getContentResolver().takePersistableUriPermission(selected[0],Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}
            chooser.onReceiveValue(selected);chooser=null;}}
        if(req==503&&result==RESULT_OK&&data!=null&&pendingExport!=null){final String text=pendingExport;final Uri uri=data.getData();pendingExport=null;
            io.execute(()->{try(OutputStream out=getContentResolver().openOutputStream(uri)){if(out==null)throw new IOException();out.write(text.getBytes(StandardCharsets.UTF_8));runOnUiThread(()->message("Резервная копия сохранена"));}catch(Exception e){runOnUiThread(()->message("Не удалось сохранить файл копии"));}});}
    }
    private void message(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override public void onRequestPermissionsResult(int r,String[] permissions,int[] grants){super.onRequestPermissionsResult(r,permissions,grants);if(r==502){try{Reminders.reschedule(this);}catch(Exception ignored){}}}
    public class Bridge {
        @JavascriptInterface public synchronized String readStorage(String key){File f=storageFile(key);if(f==null)return "";try(InputStream in=new AtomicFile(f).openRead()){return read(in);}catch(Exception e){return "";}}
        @JavascriptInterface public synchronized boolean writeStorage(String key,String value){File f=storageFile(key);if(f==null||value==null||value.length()>64*1024*1024)return false;AtomicFile a=new AtomicFile(f);FileOutputStream out=null;
            try{new JSONObject(value);out=a.startWrite();out.write(value.getBytes(StandardCharsets.UTF_8));a.finishWrite(out);return true;}catch(Exception e){if(out!=null)a.failWrite(out);return false;}}
        @JavascriptInterface public boolean hasLegacy(){SharedPreferences p=getSharedPreferences("rabbit_prefs",MODE_PRIVATE);return p.contains("rabbit_list")||p.contains("young_list")||p.contains("mating_list");}
        @JavascriptInterface public boolean migrationDone(){return getSharedPreferences("native_state",MODE_PRIVATE).getBoolean("migrated",false);}
        @JavascriptInterface public boolean markMigrationDone(){return getSharedPreferences("native_state",MODE_PRIVATE).edit().putBoolean("migrated",true).commit();}
        @JavascriptInterface public String legacyBackup(){try{return Legacy.export(MainActivity.this).toString();}catch(Exception e){return new JSONObject().toString();}}
        @JavascriptInterface public void exportJson(String json){if(json==null||json.length()>64*1024*1024)return;runOnUiThread(()->{pendingExport=json;Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/json");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"RabbitFarm-"+java.time.LocalDate.now()+".json");try{startActivityForResult(i,503);}catch(Exception e){message("Не удалось открыть сохранение файла");}});}
        @JavascriptInterface public void syncReminders(String json){if(json==null||json.length()>1000000)return;io.execute(()->{try{Reminders.sync(MainActivity.this,new JSONArray(json));}catch(Exception ignored){}});}
        @JavascriptInterface public void closeApp(){runOnUiThread(()->finish());}
    }
    private File storageFile(String key){if("rabbitfarm-demo-v1".equals(key))return new File(getFilesDir(),"rabbitfarm-current.json");if("rabbitfarm-recovery-v1".equals(key))return new File(getFilesDir(),"rabbitfarm-recovery.json");return null;}
    static String read(InputStream in)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){out.write(b,0,n);if(out.size()>64*1024*1024)throw new IOException("File too large");}return out.toString("UTF-8");}
}
