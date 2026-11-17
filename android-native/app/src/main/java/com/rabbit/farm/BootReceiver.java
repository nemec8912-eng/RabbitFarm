package com.rabbit.farm;
import android.content.*;
public class BootReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context,Intent intent){final PendingResult result=goAsync();new Thread(()->{try{Reminders.reschedule(context);}catch(Exception ignored){}finally{result.finish();}}).start();}
}
