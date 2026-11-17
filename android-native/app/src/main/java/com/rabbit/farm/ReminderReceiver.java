package com.rabbit.farm;
import android.content.*;
public class ReminderReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context,Intent intent){final PendingResult result=goAsync();new Thread(()->{try{Reminders.deliver(context,intent.getStringExtra("id"));}catch(Exception ignored){}finally{result.finish();}}).start();}
}
