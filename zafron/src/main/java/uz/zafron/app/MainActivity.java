package uz.zafron.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    private static final String MODEL = "gemini-flash-latest";
    private static final String PRO = "gemini-pro-latest";
    private static final int PICK = 11;
    private static final int SAFFRON = 0xFFD9780F;
    private static final int INK = 0xFF1D1A16;
    private static final int BG = 0xFFF6F3EE;

    private static final String SYSTEM =
            "Sen Zafaron — sun’iy intellektsan, har qanday mavzuda yordam beradigan aqlli yordamchisan (ta’lim, fan, tarix, til, "
            + "matematika, dasturlash, sog‘liq haqida umumiy ma’lumot, biznes, hayotiy maslahatlar, tarjima, yozish) "
            + "va ayniqsa telemasterlik (telefon va televizor ta’miri) bo‘yicha tajribali ustozsan. "
            + "Har qanday savolga to‘liq, aniq javob ber; bilmasang, bilmasligingni ayt. Boshqa tilda yozishsa, shu tilda javob ber. "
            + "O‘zbek tilida (lotin yozuvi, ş ç ö ğ harflari bilan: o‘→ö, g‘→ğ, sh→ş, ch→ç) "
            + "qisqa va tushunarli javob ber. Foydalanuvchi texnik odam emas: qadam-baqadam tushuntir, murakkab so‘zni oddiy so‘z bilan izohla. "
            + "Telemasterlik bilimlaring: telefon (ekran, batareya, zaryad porti, dinamik, mikrofon, kamera, suv tekkan telefon, "
            + "o‘chib qolish, qizish, tarmoq, IMEI, dasturiy ta’minot va flesh, backup), televizor (LED backlight, quvvat bloki, "
            + "mainboard, T-con, matritsa, HDMI, ovoz, Smart TV, pult), asboblar (multimetr, lehim cho‘pi, fen stansiya, mikroskop, ESD), "
            + "sxema o‘qish, nosozlikni topish ketma-ketligi, xavfsizlik (yuqori kuchlanish, litiy batareya), ehtiyot qismlar, narx belgilash, "
            + "mijoz bilan ishlash, ustaxona ochish va o‘rganish rejasi. "
            + "TELEFON DIAGNOSTIKASI (ketma-ketlik): 1) Tashqi ko‘rik: yorilish, suv izi, kuygan hid, shishgan batareya. 2) Zaryad: boshqa kabel va adapter, port ichini tozalash. 3) Majburiy qayta yoqish va recovery/DFU rejimi. 4) Laboratoriya blokida (DC power supply) tok o‘lchash: batareya ulanganda 0 mA — quvvat zanjiri (batareya, ulagich, PMIC) nosoz; tok bor-u yuklanmaydi — dasturiy ta’minot yoki xotira; tok sakrab yuqori ko‘tarilsa — qisqa tutashuv, tez qiziydigan element (termokamera yoki spirt/kanifol bilan toping). 5) Multimetr: batareya 3.0–4.45 V, USB 5 V, zaryad IC/PMIC atrofidagi kondensatorlar qisqa tutashuvi (diod rejimi). Odatiy nosozliklar: zaryad olmaydi (port, zaryad IC, kabel), tez zaryadsizlanadi (batareya yeyilishi), ekranda yo‘llar/dog‘ (displey), tuş ishlamaydi (displey yoki tach shleyfi), tovush yo‘q (dinamik, audio IC, suv), tarmoq yo‘q (antenna, modem, IMEI), qizish (dasturiy jarayon, PMIC, batareya), boot-loop (dastur, xotira). Ekran almashtirish: telefonni o‘chiring, batareyani ajrating, fen bilan 70–80 °C isitib yelim eritiladi, plektr bilan sekin ajrating, shleyflarni ehtiyot bilan ajrating, yangi ekranni sinab ko‘rib keyin yelimlang. "
            + "TELEVIZOR DIAGNOSTIKASI: 1) Standby chirog‘i yonadimi? Yonmasa — quvvat bloki (saqlagich, shishgan kondensator, standby kuchlanish ~5 V). 2) Chiroq yonib, ekran qora, ovoz bor — orqa yoritish (backlight): fonarik bilan ekranga qarab ko‘ring, xira tasvir ko‘rinsa LED lenta yoki LED driver nosoz. 3) Yoqilib o‘chib qolsa (qayta-qayta) — LED lenta qisqa tutashuvi yoki quvvat bloki himoyasi. 4) Tasvir bor, ovoz yo‘q — ovoz sozlamasi, ovoz kuchaytirgichi, dinamik. 5) Vertikal/gorizontal yo‘llar — T-con, shleyf yoki matritsa (panel almashtirish ko‘pincha arzon emas). 6) HDMI signal yo‘q — port, kabel, mainboard. 7) Smart TV qotib qolishi — zavod sozlamasi, dastur yangilash. Eng ko‘p uchraydigan: shishgan elektrolit kondensatorlar (6.3–25 V, 105 °C), LED lenta, saqlagich, standby bo‘limi. Televizor kondensatorlari uzoq zaryadini saqlaydi: ochishdan oldin o‘chirib, 10 daqiqa kuting va razryad qiling. "
            + "ASBOBLAR VA USUL: lehim cho‘pi 320–350 °C (qo‘rg‘oshinsiz 350–380 °C), fen 300–380 °C, flus/kanifol, lehim simi 0.3–0.5 mm, lehim o‘rami (opletka), isopropil spirti, mikroskop, ESD gilam va bilak tasmasi, multimetr, DC power supply, plektr, vakuum so‘rg‘ich, ekran ajratgich. BGA va mikrosxema ishi faqat tajriba bilan. Sxemalarni olish: ehtiyot qismlar bozori, model nomi bo‘yicha qidirish. Narx va biznes: avval diagnostika narxi, ish narxi va ehtiyot qism narxi alohida, kafolat muddati, mijozga nosozlikni oddiy tilda tushuntirish, ish tartibi (qabul varaqasi, ma’lumot zaxirasi, tekshiruv). "
            + "O‘RGANISH REJASI: 1-hafta elektronika asoslari (kuchlanish, tok, qarshilik), multimetr; 2-hafta lehimlash mashqi; 3–4-hafta eski telefon va televizorni ochish, nosozlik izlash; 2-oy ekran, batareya, port almashtirish; 3-oy zaryad va quvvat zanjiri, sxema o‘qish; keyin mikrolehimlash va mijoz bilan ishlash. "
            + "Sen ko‘p mavzuda har qanday vazifani bajarasan: bosqichma-bosqich mantiqiy fikrlash, hisob-kitob, dasturlash, yozish va tahrirlash, tarjima, tahlil, reja tuzish, o‘rgatish. Murakkab masalada avval o‘ylab, so‘ng aniq javob ber; faktga ishonching komil bo‘lmasa, buni ayt va internetdan qidir. "
            + "Rasm, video kadrlari yoki fayl kelsa: avval nimani ko‘rganingni ayt, so‘ng ehtimoliy sabablarni tartibla va tekshirish qadamlarini ber. "
            + "Xavfli ish (220V blok, shishgan batareya) haqida ogohlantir. O‘g‘irlangan yoki birovning qulflangan qurilmasini ochishga yordam berma. "
            + "Javobni ortiqcha uzaytirma.";

    private static class Pending {
        String name;
        String text;
        List<String> images = new ArrayList<>();
    }

    private SharedPreferences sp;
    private LinearLayout chat;
    private ScrollView scroll;
    private EditText inp;
    private TextView attLabel;
    private Button sendBtn;
    private final List<String[]> hist = new ArrayList<>();
    private final List<Pending> pend = new ArrayList<>();
    private boolean busy = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        sp = getSharedPreferences("z", MODE_PRIVATE);
        loadHist();
        if (android.os.Build.VERSION.SDK_INT >= 33) requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 5);
        if (sp.getString("key", "").isEmpty()) showSetup();
        else showChat();
    }

    // ------------------------------------------------------------ setup

    private void showSetup() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(BG);
        l.setPadding(dp(20), dp(48), dp(20), dp(20));

        TextView t = new TextView(this);
        t.setText("Zafaron");
        t.setTextSize(32);
        t.setTypeface(null, Typeface.BOLD);
        t.setTextColor(SAFFRON);
        l.addView(t);

        TextView info = new TextView(this);
        info.setTextSize(16);
        info.setTextColor(INK);
        info.setPadding(0, dp(12), 0, dp(12));
        info.setText("Zafaron ishlashi uchun bepul Google kaliti kerak. Bir marta qilasiz:\n\n"
                + "1) «Kalit olish» tugmasini bosing va Google hisobingiz bilan kiring.\n"
                + "2) «Create API key» tugmasini bosing.\n"
                + "3) Chiqqan kalitni nusxalab (copy), pastdagi maydonga qo‘ying.");
        l.addView(info);

        Button get = new Button(this);
        get.setText("Kalit olish");
        get.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://aistudio.google.com/apikey"))));
        l.addView(get);

        final EditText key = new EditText(this);
        key.setHint("Kalitni shu yerga qo‘ying");
        key.setSingleLine(true);
        l.addView(key);

        Button save = new Button(this);
        save.setText("Saqlash va boshlash");
        save.setOnClickListener(v -> {
            String k = key.getText().toString().trim();
            if (k.length() < 20) {
                Toast.makeText(this, "Kalit to‘liq emas", Toast.LENGTH_SHORT).show();
                return;
            }
            sp.edit().putString("key", k).apply();
            showChat();
        });
        l.addView(save);
        setContentView(l);
    }

    // ------------------------------------------------------------ chat UI

    private void showChat() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(16), dp(36), dp(8), dp(8));
        TextView title = new TextView(this);
        title.setText("Zafaron");
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(SAFFRON);
        head.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button neu = small("Yangi");
        neu.setOnClickListener(v -> {
            if (busy) return;
            hist.clear();
            saveHist();
            drawAll();
        });
        head.addView(neu);
        Button keyBtn = small("Kalit");
        keyBtn.setOnClickListener(v -> showSetup());
        head.addView(keyBtn);
        Button inbox = small("Xabarlar");
        inbox.setOnClickListener(v -> showInbox());
        head.addView(inbox);
        final Button pro = small(sp.getBoolean("pro", false) ? "Kuchli: ha" : "Kuchli: yo\u2018q");
        pro.setOnClickListener(v -> {
            boolean on = !sp.getBoolean("pro", false);
            sp.edit().putBoolean("pro", on).apply();
            pro.setText(on ? "Kuchli: ha" : "Kuchli: yo\u2018q");
            Toast.makeText(this, on ? "Eng kuchli model yoqildi (sekinroq, limiti kamroq)" : "Tez model yoqildi", Toast.LENGTH_SHORT).show();
        });
        head.addView(pro);
        root.addView(head);

        scroll = new ScrollView(this);
        chat = new LinearLayout(this);
        chat.setOrientation(LinearLayout.VERTICAL);
        chat.setPadding(dp(12), dp(8), dp(12), dp(8));
        scroll.addView(chat);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        attLabel = new TextView(this);
        attLabel.setTextColor(0xFF6F685E);
        attLabel.setPadding(dp(16), dp(4), dp(16), 0);
        attLabel.setVisibility(View.GONE);
        attLabel.setOnClickListener(v -> {
            pend.clear();
            drawAtt();
        });
        root.addView(attLabel);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.BOTTOM);
        row.setPadding(dp(8), dp(4), dp(8), dp(8));
        Button plus = new Button(this);
        plus.setText("+");
        plus.setTextSize(20);
        plus.setOnClickListener(v -> pick());
        row.addView(plus, new LinearLayout.LayoutParams(dp(52), dp(48)));
        inp = new EditText(this);
        inp.setHint("Savol yozing yoki rasm yuboring");
        inp.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        inp.setMaxLines(5);
        row.addView(inp, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        sendBtn = new Button(this);
        sendBtn.setText("Yuborish");
        sendBtn.setTextColor(Color.WHITE);
        sendBtn.setBackgroundColor(SAFFRON);
        sendBtn.setOnClickListener(v -> send());
        row.addView(sendBtn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48)));
        root.addView(row);

        setContentView(root);
        drawAll();
    }

    private void showInbox() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(BG);
        l.setPadding(dp(16), dp(40), dp(16), dp(12));

        TextView t = new TextView(this);
        t.setText("Xabarlar");
        t.setTextSize(26);
        t.setTypeface(null, Typeface.BOLD);
        t.setTextColor(SAFFRON);
        l.addView(t);

        TextView info = new TextView(this);
        info.setTextColor(INK);
        info.setPadding(0, dp(6), 0, dp(6));
        info.setText("Telegram, Instagram, SMS va pochtaga kelgan har bir xabarni o\u2018zbekcha qilib o\u2018zim aytib turaman: telefon bildirishnomasida chiqadi. Buning uchun \u00abRuxsat berish\u00bb ni bosib, Zafaron ni yoqing.");
        l.addView(info);

        Button perm = new Button(this);
        perm.setText("Ruxsat berish");
        perm.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        l.addView(perm);

        final Button speak = new Button(this);
        speak.setText(sp.getBoolean("speak", false) ? "Ovozda aytish: ha" : "Ovozda aytish: yo\u2018q");
        speak.setOnClickListener(v -> {
            boolean on = !sp.getBoolean("speak", false);
            sp.edit().putBoolean("speak", on).apply();
            speak.setText(on ? "Ovozda aytish: ha" : "Ovozda aytish: yo\u2018q");
        });
        l.addView(speak);

        final StringBuilder all = new StringBuilder();
        StringBuilder shown = new StringBuilder();
        try {
            JSONArray a = new JSONArray(sp.getString("inbox", "[]"));
            SimpleDateFormat f = new SimpleDateFormat("dd.MM HH:mm", Locale.US);
            for (int i = a.length() - 1; i >= 0 && i >= a.length() - 40; i--) {
                JSONObject o = a.getJSONObject(i);
                String line = o.getString("a") + " \u2022 " + o.getString("t") + ": " + o.getString("x");
                shown.append(f.format(new Date(o.getLong("ts")))).append("  ").append(line).append("\n\n");
                all.insert(0, line + "\n");
            }
        } catch (Exception ignored) {
        }

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button ask = new Button(this);
        ask.setText("Zafaronga ko\u2018rsatish");
        ask.setOnClickListener(v -> {
            if (all.length() == 0) {
                Toast.makeText(this, "Hozircha xabar yo\u2018q", Toast.LENGTH_SHORT).show();
                return;
            }
            showChat();
            inp.setText("Menga kelgan xabarlar. Muhimlarini ayt, kim nima so\u2018raganini qisqa yoz va javob taklif qil:\n\n" + all);
            send();
        });
        row.addView(ask, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button clr = new Button(this);
        clr.setText("Tozalash");
        clr.setOnClickListener(v -> {
            sp.edit().remove("inbox").apply();
            showInbox();
        });
        row.addView(clr);
        Button back = new Button(this);
        back.setText("Orqaga");
        back.setOnClickListener(v -> showChat());
        row.addView(back);
        l.addView(row);

        ScrollView sv = new ScrollView(this);
        TextView list = new TextView(this);
        list.setTextColor(INK);
        list.setTextIsSelectable(true);
        list.setText(shown.length() == 0 ? "Hozircha xabar yo\u2018q." : shown.toString());
        sv.addView(list);
        l.addView(sv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(l);
    }

    private Button small(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        return b;
    }

    private void drawAll() {
        chat.removeAllViews();
        bubble("Salom! Men Zafaronman, sun\u2019iy intellektman. Hamma narsadan so‘rang, telefon va televizor ta’miri bo‘yicha o‘rgataman. "
                + "Rasm, video yoki matnli fayl yuborish uchun + tugmasini bosing.", false);
        for (String[] m : hist) bubble(m[1], m[0].equals("user"));
        drawAtt();
    }

    private TextView bubble(String text, boolean me) {
        TextView t = new TextView(this);
        t.setTextSize(16);
        t.setTextIsSelectable(true);
        t.setText(fmt(text));
        t.setPadding(dp(14), dp(10), dp(14), dp(10));
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(14));
        if (me) {
            g.setColor(INK);
            t.setTextColor(BG);
        } else {
            g.setColor(Color.WHITE);
            g.setStroke(dp(1), 0xFFE2DCD2);
            t.setTextColor(INK);
        }
        t.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = me ? Gravity.END : Gravity.START;
        lp.setMargins(me ? dp(48) : 0, dp(5), me ? 0 : dp(48), dp(5));
        chat.addView(t, lp);
        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
        return t;
    }

    private CharSequence fmt(String s) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        Matcher m = Pattern.compile("\\*\\*(.+?)\\*\\*", Pattern.DOTALL).matcher(s);
        int last = 0;
        while (m.find()) {
            sb.append(s, last, m.start());
            int st = sb.length();
            sb.append(m.group(1));
            sb.setSpan(new StyleSpan(Typeface.BOLD), st, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            last = m.end();
        }
        sb.append(s, last, s.length());
        return sb;
    }

    private void drawAtt() {
        if (attLabel == null) return;
        if (pend.isEmpty()) {
            attLabel.setVisibility(View.GONE);
            return;
        }
        StringBuilder sb = new StringBuilder("Qo‘shildi: ");
        for (int i = 0; i < pend.size(); i++) sb.append(i > 0 ? ", " : "").append(pend.get(i).name);
        sb.append("  (olib tashlash uchun bosing)");
        attLabel.setText(sb);
        attLabel.setVisibility(View.VISIBLE);
    }

    // ------------------------------------------------------------ attachments

    private void pick() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*", "text/*", "application/json"});
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, PICK);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != PICK || res != RESULT_OK || data == null) return;
        final List<Uri> uris = new ArrayList<>();
        ClipData cd = data.getClipData();
        if (cd != null) {
            for (int i = 0; i < cd.getItemCount(); i++) uris.add(cd.getItemAt(i).getUri());
        } else if (data.getData() != null) {
            uris.add(data.getData());
        }
        Toast.makeText(this, "Fayl tayyorlanmoqda...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            for (Uri u : uris) {
                Pending p = new Pending();
                p.name = u.getLastPathSegment() == null ? "fayl" : u.getLastPathSegment();
                try {
                    String type = getContentResolver().getType(u);
                    if (type == null) type = "";
                    if (type.startsWith("image/")) {
                        String b = imageB64(u);
                        if (b != null) p.images.add(b);
                    } else if (type.startsWith("video/")) {
                        p.text = "[Video: " + p.name + " — undan olingan kadrlar rasm sifatida ilova qilindi]";
                        p.images.addAll(videoFrames(u));
                    } else {
                        p.text = "[Fayl: " + p.name + "]\n" + readText(u);
                    }
                } catch (Exception e) {
                    p.name = p.name + " (o‘qib bo‘lmadi)";
                }
                final Pending fp = p;
                runOnUiThread(() -> {
                    pend.add(fp);
                    drawAtt();
                });
            }
        }).start();
    }

    private String readText(Uri u) throws Exception {
        InputStream in = getContentResolver().openInputStream(u);
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0 && o.size() < 40000) o.write(buf, 0, n);
        in.close();
        String s = o.toString("UTF-8");
        return s.length() > 20000 ? s.substring(0, 20000) : s;
    }

    private String imageB64(Uri u) throws Exception {
        BitmapFactory.Options bo = new BitmapFactory.Options();
        bo.inJustDecodeBounds = true;
        InputStream in = getContentResolver().openInputStream(u);
        BitmapFactory.decodeStream(in, null, bo);
        in.close();
        int s = 1;
        while (Math.max(bo.outWidth, bo.outHeight) / (s * 2) >= 1024) s *= 2;
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inSampleSize = s;
        in = getContentResolver().openInputStream(u);
        Bitmap bm = BitmapFactory.decodeStream(in, null, o);
        in.close();
        return bmB64(bm);
    }

    private String bmB64(Bitmap bm) {
        if (bm == null) return null;
        int m = Math.max(bm.getWidth(), bm.getHeight());
        if (m > 1024) {
            float k = 1024f / m;
            bm = Bitmap.createScaledBitmap(bm, Math.round(bm.getWidth() * k), Math.round(bm.getHeight() * k), true);
        }
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 80, o);
        return Base64.encodeToString(o.toByteArray(), Base64.NO_WRAP);
    }

    private List<String> videoFrames(Uri u) throws Exception {
        List<String> out = new ArrayList<>();
        MediaMetadataRetriever r = new MediaMetadataRetriever();
        r.setDataSource(this, u);
        long ms = 0;
        try {
            ms = Long.parseLong(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception ignored) {
        }
        for (int i = 0; i < 4; i++) {
            Bitmap f = r.getFrameAtTime(ms * 1000L * (2 * i + 1) / 8, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            String b = bmB64(f);
            if (b != null) out.add(b);
        }
        r.release();
        return out;
    }

    // ------------------------------------------------------------ sending

    private void send() {
        if (busy) return;
        String text = inp.getText().toString().trim();
        if (text.isEmpty() && pend.isEmpty()) return;
        final String key = sp.getString("key", "");
        final List<Pending> files = new ArrayList<>(pend);
        final String shown = text.isEmpty() ? "Shuni ko‘rib bering." : text;
        StringBuilder full = new StringBuilder(shown);
        int imgs = 0;
        for (Pending p : files) {
            if (p.text != null) full.append("\n\n").append(p.text);
            imgs += p.images.size();
        }
        final String userText = full.toString();
        String label = shown;
        for (Pending p : files) label += "\n📎 " + p.name;

        hist.add(new String[]{"user", label});
        pend.clear();
        inp.setText("");
        drawAtt();
        bubble(label, true);
        final TextView out = bubble("…", false);
        busy = true;
        sendBtn.setEnabled(false);
        final int n = hist.size();

        new Thread(() -> {
            String ans;
            boolean ok = true;
            try {
                JSONArray contents = new JSONArray();
                int from = Math.max(0, n - 1 - 30);
                for (int i = from; i < n - 1; i++) {
                    String[] m = hist.get(i);
                    contents.put(new JSONObject().put("role", m[0].equals("user") ? "user" : "model")
                            .put("parts", new JSONArray().put(new JSONObject().put("text", m[1]))));
                }
                JSONArray parts = new JSONArray();
                parts.put(new JSONObject().put("text", userText));
                int count = 0;
                for (Pending p : files) {
                    for (String b : p.images) {
                        if (count++ >= 8) break;
                        parts.put(new JSONObject().put("inline_data",
                                new JSONObject().put("mime_type", "image/jpeg").put("data", b)));
                    }
                }
                contents.put(new JSONObject().put("role", "user").put("parts", parts));
                JSONObject body = new JSONObject()
                        .put("system_instruction", new JSONObject()
                                .put("parts", new JSONArray().put(new JSONObject().put("text", SYSTEM))))
                        .put("contents", contents);
                body.put("tools", new JSONArray().put(new JSONObject().put("google_search", new JSONObject())));

                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/" + (sp.getBoolean("pro", false) ? PRO : MODEL) + ":generateContent");
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setConnectTimeout(20000);
                c.setReadTimeout(90000);
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type", "application/json");
                c.setRequestProperty("x-goog-api-key", key);
                OutputStream os = c.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();
                int code = c.getResponseCode();
                InputStream is = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int r;
                while (is != null && (r = is.read(buf)) > 0) bo.write(buf, 0, r);
                String resp = bo.toString("UTF-8");
                if (code >= 200 && code < 300) {
                    JSONArray ps = new JSONObject(resp).getJSONArray("candidates").getJSONObject(0)
                            .getJSONObject("content").getJSONArray("parts");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < ps.length(); i++) sb.append(ps.getJSONObject(i).optString("text", ""));
                    ans = sb.toString().trim();
                    if (ans.isEmpty()) {
                        ok = false;
                        ans = "Javob bo‘sh keldi. Savolni boshqacha yozib ko‘ring.";
                    }
                } else {
                    ok = false;
                    String msg = "";
                    try {
                        msg = new JSONObject(resp).getJSONObject("error").optString("message", "");
                    } catch (Exception ignored) {
                    }
                    if (code == 400 && msg.toLowerCase().contains("key")) {
                        ans = "Kalit noto‘g‘ri. Tepadagi «Kalit» tugmasi bilan yangisini kiriting.";
                    } else if (code == 429) {
                        ans = "Bepul limit tugadi. Biroz kutib, qayta yuboring.";
                    } else {
                        ans = "Xato (" + code + "). " + msg;
                    }
                }
            } catch (Exception e) {
                ok = false;
                ans = "Internetga ulanib bo‘lmadi. Internetni tekshiring.";
            }
            final String fa = ans;
            final boolean fok = ok;
            runOnUiThread(() -> {
                out.setText(fmt(fa));
                if (fok) hist.add(new String[]{"model", fa});
                else hist.remove(hist.size() - 1);
                saveHist();
                busy = false;
                sendBtn.setEnabled(true);
                scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
            });
        }).start();
    }

    // ------------------------------------------------------------ storage

    private void loadHist() {
        try {
            JSONArray a = new JSONArray(sp.getString("hist", "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                hist.add(new String[]{o.getString("r"), o.getString("t")});
            }
        } catch (Exception ignored) {
        }
    }

    private void saveHist() {
        try {
            JSONArray a = new JSONArray();
            int from = Math.max(0, hist.size() - 40);
            for (int i = from; i < hist.size(); i++) {
                a.put(new JSONObject().put("r", hist.get(i)[0]).put("t", hist.get(i)[1]));
            }
            sp.edit().putString("hist", a.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
