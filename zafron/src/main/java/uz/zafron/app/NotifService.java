package uz.zafron.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
        if (pkg.contains("messaging") || pkg.contains(".mms") || pkg.contains("sms")) return "SMS";
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

    private void notifyUser(String title, String text) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        Notification.Builder b;
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(new NotificationChannel("zafaron", "Zafaron xabarlari", NotificationManager.IMPORTANCE_HIGH));
            b = new Notification.Builder(this, "zafaron");
        } else {
            b = new Notification.Builder(this);
        }
        b.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setAutoCancel(true);
        nm.notify((int) (System.nanoTime() & 0x7fffffff), b.build());
    }

    static String ask(String key, String prompt) throws Exception {
        JSONObject body = new JSONObject().put("contents", new JSONArray().put(new JSONObject().put("role", "user")
                .put("parts", new JSONArray().put(new JSONObject().put("text", prompt)))));
        URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent");
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(15000);
        c.setReadTimeout(40000);
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json");
        c.setRequestProperty("x-goog-api-key", key);
        OutputStream os = c.getOutputStream();
        os.write(body.toString().getBytes("UTF-8"));
        os.close();
        if (c.getResponseCode() != 200) return null;
        InputStream is = c.getInputStream();
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int r;
        while ((r = is.read(buf)) > 0) bo.write(buf, 0, r);
        JSONArray ps = new JSONObject(bo.toString("UTF-8")).getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ps.length(); i++) sb.append(ps.getJSONObject(i).optString("text", ""));
        return sb.toString().trim();
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
            final String fa = app, ft = t, fx = x;
            final boolean speak = sp.getBoolean("speak", false);
            final String key = sp.getString("key", "");
            new Thread(() -> {
                String uz = null;
                if (!key.isEmpty()) {
                    try {
                        uz = ask(key, "Quyidagi kelgan xabarni o\u2018zbek tilida (lotin yozuvi, \u015f \u00e7 \u00f6 \u011f harflari bilan) 1-2 qisqa gapda ayt: kim yozdi va nima demoqchi. "
                                + "Shoshilinch yoki muhim bo\u2018lsa, boshiga \u00abMuhim:\u00bb qo\u2018sh. Reklama bo\u2018lsa, \u00abReklama\u00bb deb yoz. Faqat natijani yoz.\n\n"
                                + "Ilova: " + fa + "\nYuboruvchi: " + ft + "\nMatn: " + fx);
                    } catch (Exception ignored) {
                    }
                }
                String shown = uz == null || uz.isEmpty() ? fx : uz;
                notifyUser(fa + ": " + ft, shown);
                if (speak && ttsReady) {
                    tts.speak(shown, TextToSpeech.QUEUE_ADD, null, "z" + System.nanoTime());
                }
            }).start();
        } catch (Exception ignored) {
        }
    }
}
