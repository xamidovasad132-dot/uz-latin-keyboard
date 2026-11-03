package uz.zafron.app;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

/** Telegram, Instagram va pochta bildirishnomalarini o'qib, saqlaydi va (xohlasa) ovoz chiqarib aytadi. */
public class NotifService extends NotificationListenerService {

    private TextToSpeech tts;
    private boolean ttsReady = false;

    static String appName(String pkg) {
        if (pkg.startsWith("org.telegram") || pkg.equals("org.thunderdog.challegram")) return "Telegram";
        if (pkg.equals("com.instagram.android") || pkg.equals("com.instagram.barcelona")) return "Instagram";
        if (pkg.equals("com.google.android.gm") || pkg.contains("mail") || pkg.contains("outlook")) return "Pochta";
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.SUCCESS) return;
            String[] tags = {"uz", "ru", "en"};
            for (String t : tags) {
                int r = tts.setLanguage(Locale.forLanguageTag(t));
                if (r >= TextToSpeech.LANG_AVAILABLE) break;
            }
            ttsReady = true;
        });
    }

    @Override
    public void onDestroy() {
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            String app = appName(sbn.getPackageName());
            if (app == null) return;
            Notification n = sbn.getNotification();
            if ((n.flags & Notification.FLAG_GROUP_SUMMARY) != 0) return;
            Bundle e = n.extras;
            CharSequence title = e.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence text = e.getCharSequence(Notification.EXTRA_BIG_TEXT);
            if (text == null) text = e.getCharSequence(Notification.EXTRA_TEXT);
            if (text == null || text.length() == 0) return;
            String t = title == null ? "" : title.toString();
            String x = text.toString();

            SharedPreferences sp = getSharedPreferences("z", Context.MODE_PRIVATE);
            synchronized (NotifService.class) {
                JSONArray a = new JSONArray(sp.getString("inbox", "[]"));
                if (a.length() > 0) {
                    JSONObject last = a.getJSONObject(a.length() - 1);
                    if (last.optString("t").equals(t) && last.optString("x").equals(x)) return;
                }
                a.put(new JSONObject().put("a", app).put("t", t).put("x", x).put("ts", System.currentTimeMillis()));
                JSONArray b = new JSONArray();
                for (int i = Math.max(0, a.length() - 100); i < a.length(); i++) b.put(a.get(i));
                sp.edit().putString("inbox", b.toString()).apply();
            }
            if (sp.getBoolean("speak", false) && ttsReady) {
                tts.speak(app + ". " + t + ". " + x, TextToSpeech.QUEUE_ADD, null, "z" + System.nanoTime());
            }
        } catch (Exception ignored) {
        }
    }
}
