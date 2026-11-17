package com.rabbit.farm;
import android.content.*;
import android.net.Uri;
import android.graphics.*;
import android.util.Base64;
import org.json.*;
import java.util.*;
import java.io.*;
final class Legacy {
 static JSONObject export(Context c)throws Exception{
  SharedPreferences p=c.getSharedPreferences("rabbit_prefs",Context.MODE_PRIVATE);JSONObject data=new JSONObject(),photos=new JSONObject();JSONArray missing=new JSONArray();
  String[] keys={"rabbit_list","meat_list","adult_death_list","mating_list","young_list","young_death_history","adult_sales","young_sales"};
  for(String k:keys)data.put(k,new JSONArray(new ArrayList<String>(p.getStringSet(k,Collections.<String>emptySet()))));
  data.put("young_meat_total",p.getInt("young_meat_total",0));data.put("young_death_total",p.getInt("young_death_total",0));
  Set<String> uris=new HashSet<>();for(String k:new String[]{"rabbit_list","meat_list","adult_death_list"})for(String raw:p.getStringSet(k,Collections.<String>emptySet())){String[] parts=raw.split("\\|",-1);if(parts.length>4&&!parts[4].isEmpty())uris.add(parts[4]);}
  for(String value:uris)try{Uri uri=Uri.parse(value);BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;try(InputStream in=c.getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(in,null,bounds);}if(bounds.outWidth<=0)throw new IOException();int sample=1;while(Math.max(bounds.outWidth,bounds.outHeight)/sample>1280)sample*=2;BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=sample;Bitmap bitmap;try(InputStream in=c.getContentResolver().openInputStream(uri)){bitmap=BitmapFactory.decodeStream(in,null,o);}if(bitmap==null)throw new IOException();float ratio=Math.min(1f,640f/Math.max(bitmap.getWidth(),bitmap.getHeight()));Bitmap small=Bitmap.createScaledBitmap(bitmap,Math.max(1,(int)(bitmap.getWidth()*ratio)),Math.max(1,(int)(bitmap.getHeight()*ratio)),true);ByteArrayOutputStream out=new ByteArrayOutputStream();small.compress(Bitmap.CompressFormat.JPEG,78,out);photos.put(value,"data:image/jpeg;base64,"+Base64.encodeToString(out.toByteArray(),Base64.NO_WRAP));if(small!=bitmap)small.recycle();bitmap.recycle();}catch(Exception e){missing.put(value);}
  return new JSONObject().put("format","rabbitfarm-android-prefs").put("version",1).put("prefs",data).put("photos",photos).put("unavailablePhotos",missing);
 }
}
