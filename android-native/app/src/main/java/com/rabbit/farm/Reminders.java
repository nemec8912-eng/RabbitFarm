package com.rabbit.farm;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Build;
import org.json.*;
import java.time.*;
import java.util.*;
final class Reminders {
 static final String CHANNEL="rabbitfarm_weaning";
 static void ensureChannel(Context c){if(Build.VERSION.SDK_INT>=26){NotificationChannel channel=new NotificationChannel(CHANNEL,"Отсадка молодняка",NotificationManager.IMPORTANCE_DEFAULT);channel.setDescription("Напоминания на 45-й день после рождения");c.getSystemService(NotificationManager.class).createNotificationChannel(channel);}}
 static PendingIntent pending(Context c,String id){Intent i=new Intent(c,ReminderReceiver.class).setAction("com.rabbit.farm.WEAN").setData(Uri.parse("rabbitfarm://weaning/"+Uri.encode(id))).putExtra("id",id);return PendingIntent.getBroadcast(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 static synchronized void sync(Context c,JSONArray rows)throws Exception{
  SharedPreferences p=c.getSharedPreferences("native_reminders",Context.MODE_PRIVATE);JSONArray old=new JSONArray(p.getString("rows","[]"));AlarmManager alarms=c.getSystemService(AlarmManager.class);Set<String> current=new HashSet<>();for(int n=0;n<rows.length();n++)current.add(rows.getJSONObject(n).getString("id"));
  for(int n=0;n<old.length();n++){String id=old.getJSONObject(n).getString("id");alarms.cancel(pending(c,id));if(!current.contains(id)){c.getSystemService(NotificationManager.class).cancel(id.hashCode());p.edit().remove("sent_"+id+"_"+old.getJSONObject(n).getString("date")).commit();}}
  p.edit().putString("rows",rows.toString()).commit();schedule(c,rows);
 }
 static void schedule(Context c,JSONArray rows)throws Exception{AlarmManager alarms=c.getSystemService(AlarmManager.class);SharedPreferences p=c.getSharedPreferences("native_reminders",Context.MODE_PRIVATE);
  for(int n=0;n<rows.length();n++){JSONObject r=rows.getJSONObject(n);String id=r.getString("id"),date=r.getString("date");if(p.getBoolean("sent_"+id+"_"+date,false))continue;long target=LocalDate.parse(date).atTime(9,0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,Math.max(System.currentTimeMillis()+3000,target),pending(c,id));}}
 static void reschedule(Context c)throws Exception{schedule(c,new JSONArray(c.getSharedPreferences("native_reminders",Context.MODE_PRIVATE).getString("rows","[]")));}
 static void deliver(Context c,String id)throws Exception{
  JSONArray rows=new JSONArray(c.getSharedPreferences("native_reminders",Context.MODE_PRIVATE).getString("rows","[]"));JSONObject r=null;for(int n=0;n<rows.length();n++)if(id.equals(rows.getJSONObject(n).getString("id")))r=rows.getJSONObject(n);if(r==null)return;
  NotificationManager manager=c.getSystemService(NotificationManager.class);ensureChannel(c);if(!manager.areNotificationsEnabled())return;
  Intent open=new Intent(c,MainActivity.class).putExtra("litterId",id).setData(Uri.parse("rabbitfarm://open/"+Uri.encode(id))).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
  PendingIntent click=PendingIntent.getActivity(c,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  String text="От "+r.optString("mother","матери")+" — "+r.getInt("count")+" крольчат";
  Notification.Builder b=new Notification.Builder(c,CHANNEL).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Пора отсадить молодняк").setContentText(text).setStyle(new Notification.BigTextStyle().bigText(text)).setContentIntent(click).setOnlyAlertOnce(true).setAutoCancel(true);
  manager.notify(id.hashCode(),b.build());c.getSharedPreferences("native_reminders",Context.MODE_PRIVATE).edit().putBoolean("sent_"+id+"_"+r.getString("date"),true).commit();
 }
}
