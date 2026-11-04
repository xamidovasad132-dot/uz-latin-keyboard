package uz.zafron.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.widget.ImageView;
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
    private static final int INK = 0xFFEDEDED;
    private static final int BG = 0xFF161616;

    private static final String SYSTEM =
            "Sen Zafaron — sun’iy intellektsan, har qanday mavzuda yordam beradigan aqlli yordamchisan (ta’lim, fan, tarix, til, "
            + "matematika, dasturlash, sog‘liq haqida umumiy ma’lumot, biznes, hayotiy maslahatlar, tarjima, yozish) "
            + "Asosiy ixtisosing — telemasterlik (telefon va televizor ta’miri): bu mavzuda eng chuqur va aniq javob ber. "
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

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    private ImageView roundLogo(int sizeDp) {
        int px = dp(sizeDp);
        Bitmap src = BitmapFactory.decodeResource(getResources(), R.drawable.zlogo);
        Bitmap out = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(out);
        Paint pt = new Paint(Paint.ANTI_ALIAS_FLAG);
        pt.setShader(new BitmapShader(Bitmap.createScaledBitmap(src, px, px, true), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        cv.drawCircle(px / 2f, px / 2f, px / 2f, pt);
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(out);
        return iv;
    }

    private TextView iconBtn(String s, View.OnClickListener l) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(26);
        t.setTextColor(INK);
        t.setGravity(Gravity.CENTER);
        t.setClickable(true);
        t.setOnClickListener(l);
        return t;
    }

    private void showChat() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(8), dp(34), dp(16), dp(6));
        head.addView(iconBtn("≡", v -> showDrawer()), new LinearLayout.LayoutParams(dp(52), dp(48)));
        head.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
        TextView ulash = new TextView(this);
        ulash.setText("Ulash");
        ulash.setTextColor(INK);
        ulash.setTextSize(16);
        ulash.setGravity(Gravity.CENTER);
        ulash.setPadding(dp(18), 0, dp(18), 0);
        ulash.setBackground(rounded(0xFF2B2B2B, 20));
        ulash.setClickable(true);
        ulash.setOnClickListener(v -> showConnect2());
        head.addView(ulash, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(40)));
        root.addView(head);

        scroll = new ScrollView(this);
        chat = new LinearLayout(this);
        chat.setOrientation(LinearLayout.VERTICAL);
        chat.setPadding(dp(14), dp(8), dp(14), dp(8));
        scroll.addView(chat);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        attLabel = new TextView(this);
        attLabel.setTextColor(0xFF9A9A9A);
        attLabel.setPadding(dp(20), dp(4), dp(20), 0);
        attLabel.setVisibility(View.GONE);
        attLabel.setOnClickListener(v -> {
            pend.clear();
            drawAtt();
        });
        root.addView(attLabel);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackground(rounded(0xFF222222, 30));
        bar.setPadding(dp(8), dp(6), dp(8), dp(6));
        TextView plus = iconBtn("+", v -> showAddSheet());
        plus.setBackground(rounded(0xFF333333, 22));
        bar.addView(plus, new LinearLayout.LayoutParams(dp(44), dp(44)));
        inp = new EditText(this);
        inp.setHint("Savol yozing");
        inp.setHintTextColor(0xFF8A8A8A);
        inp.setTextColor(INK);
        inp.setBackground(null);
        inp.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        inp.setMaxLines(5);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ilp.setMargins(dp(10), 0, dp(6), 0);
        bar.addView(inp, ilp);
        sendBtn = new Button(this);
        sendBtn.setText("↑");
        sendBtn.setTextSize(20);
        sendBtn.setTextColor(0xFF111111);
        sendBtn.setMinWidth(0);
        sendBtn.setMinimumWidth(0);
        sendBtn.setPadding(0, 0, 0, 0);
        sendBtn.setBackground(rounded(0xFFF2F2F2, 22));
        sendBtn.setOnClickListener(v -> send());
        bar.addView(sendBtn, new LinearLayout.LayoutParams(dp(44), dp(44)));
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        blp.setMargins(dp(12), dp(4), dp(12), dp(12));
        root.addView(bar, blp);

        setContentView(root);
        drawAll();
    }

    // ---- "+" oynasi, yon menyu, sozlamalar, ulash ----

    private void showAddSheet() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setBackground(rounded(0xFF1E1E1E, 24));
        v.setPadding(dp(16), dp(16), dp(16), dp(20));
        TextView t = new TextView(this);
        t.setText("Qo‘shish");
        t.setTextSize(18);
        t.setTypeface(null, Typeface.BOLD);
        t.setTextColor(INK);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, 0, 0, dp(14));
        v.addView(t);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        final android.app.AlertDialog[] dlg = new android.app.AlertDialog[1];
        String[] names = {"Kamera", "Foto", "Fayllar"};
        String[] icons = {"📷", "🖼", "📄"};
        for (int i = 0; i < 3; i++) {
            final int k = i;
            TextView c = new TextView(this);
            c.setText(icons[i] + "\n" + names[i]);
            c.setTextColor(INK);
            c.setTextSize(15);
            c.setGravity(Gravity.CENTER);
            c.setBackground(rounded(0xFF2A2A2A, 18));
            c.setClickable(true);
            c.setOnClickListener(x -> {
                dlg[0].dismiss();
                if (k == 0) camera();
                else if (k == 1) pick("image/*");
                else pick("*/*");
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(96), 1f);
            lp.setMargins(dp(4), 0, dp(4), 0);
            row.addView(c, lp);
        }
        v.addView(row);
        dlg[0] = new android.app.AlertDialog.Builder(this).setView(v).create();
        dlg[0].show();
        if (dlg[0].getWindow() != null) {
            dlg[0].getWindow().setGravity(Gravity.BOTTOM);
            dlg[0].getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }
    }

    private Uri camUri;
    private static final int CAM = 12;

    private void camera() {
        if (android.os.Build.VERSION.SDK_INT < 29) {
            Toast.makeText(this, "Kamera uchun Android 10 yoki yangisi kerak. Foto tugmasidan foydalaning.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.Images.Media.DISPLAY_NAME, "zafaron_cam_" + System.currentTimeMillis() + ".jpg");
            cv.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            cv.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Zafaron");
            camUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            i.putExtra(MediaStore.EXTRA_OUTPUT, camUri);
            startActivityForResult(i, CAM);
        } catch (Exception e) {
            Toast.makeText(this, "Kamerani ochib bo‘lmadi", Toast.LENGTH_SHORT).show();
        }
    }

    private TextView menuRow(String text, final Runnable r) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(19);
        t.setTextColor(INK);
        t.setPadding(dp(8), dp(16), dp(8), dp(16));
        t.setClickable(true);
        t.setOnClickListener(v -> r.run());
        return t;
    }

    private void newChat() {
        if (busy) return;
        hist.clear();
        saveHist();
        showChat();
    }

    private void showDrawer() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(20), dp(40), dp(20), dp(16));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView name = new TextView(this);
        name.setText("Zafaron");
        name.setTextSize(30);
        name.setTypeface(null, Typeface.BOLD);
        name.setTextColor(INK);
        top.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(iconBtn("✕", v -> showChat()), new LinearLayout.LayoutParams(dp(48), dp(48)));
        root.addView(top);

        root.addView(menuRow("Video yasash", this::showVideo));
        root.addView(menuRow("Xabarlar", this::showInbox));
        root.addView(menuRow("Ulash", this::showConnect2));

        TextView rec = new TextView(this);
        rec.setText("So‘nggi savollar");
        rec.setTextColor(0xFF9A9A9A);
        rec.setPadding(dp(8), dp(18), 0, dp(6));
        root.addView(rec);
        ScrollView sv = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        int shown = 0;
        for (int i = hist.size() - 1; i >= 0 && shown < 10; i--) {
            if (!hist.get(i)[0].equals("user")) continue;
            String q = hist.get(i)[1].replace("\n", " ");
            if (q.length() > 42) q = q.substring(0, 42) + "…";
            list.addView(menuRow(q, this::showChat));
            shown++;
        }
        sv.addView(list);
        root.addView(sv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        ImageView av = roundLogo(52);
        av.setClickable(true);
        av.setOnClickListener(v -> showSettings());
        bottom.addView(av, new LinearLayout.LayoutParams(dp(52), dp(52)));
        bottom.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
        Button nw = new Button(this);
        nw.setText("+  Yangi suhbat");
        nw.setAllCaps(false);
        nw.setTextColor(0xFF111111);
        nw.setBackground(rounded(0xFFF2F2F2, 28));
        nw.setOnClickListener(v -> newChat());
        bottom.addView(nw, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(52)));
        root.addView(bottom);
        setContentView(root);
    }

    private TextView card(String text, final Runnable r) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(17);
        t.setTextColor(INK);
        t.setPadding(dp(18), dp(18), dp(18), dp(18));
        t.setBackground(rounded(0xFF222222, 16));
        t.setClickable(true);
        t.setOnClickListener(v -> r.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(5), 0, dp(5));
        t.setLayoutParams(lp);
        return t;
    }

    private void showSettings() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(BG);
        l.setPadding(dp(16), dp(36), dp(16), dp(16));
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(iconBtn("≡", v -> showDrawer()), new LinearLayout.LayoutParams(dp(52), dp(48)));
        TextView t = new TextView(this);
        t.setText("Sozlamalar");
        t.setTextSize(22);
        t.setTypeface(null, Typeface.BOLD);
        t.setTextColor(INK);
        t.setPadding(dp(8), 0, 0, 0);
        top.addView(t);
        l.addView(top);
        l.addView(card("Google kaliti", this::showSetup));
        l.addView(card("Ulash (email, Telegram, telefon raqam)", this::showConnect2));
        l.addView(card("Xabarlar", this::showInbox));
        final TextView pro = card(sp.getBoolean("pro2", false) ? "Kuchli rejim: yoqilgan" : "Kuchli rejim: o‘chiq", () -> { });
        pro.setOnClickListener(v -> {
            boolean on = !sp.getBoolean("pro2", false);
            sp.edit().putBoolean("pro2", on).apply();
            pro.setText(on ? "Kuchli rejim: yoqilgan" : "Kuchli rejim: o‘chiq");
        });
        l.addView(pro);
        l.addView(card("Suhbatni tozalash", this::newChat));
        l.addView(card("Orqaga", this::showChat));
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(BG);
        sv.addView(l);
        setContentView(sv);
    }

    private void addSource(LinearLayout l, String label, final String pref, final String src,
                           String hint, int type, final boolean required) {
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(17);
        t.setTypeface(null, Typeface.BOLD);
        t.setTextColor(INK);
        t.setPadding(0, dp(16), 0, dp(2));
        l.addView(t);
        final EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(0xFF8A8A8A);
        e.setTextColor(INK);
        e.setInputType(type);
        e.setSingleLine(true);
        e.setText(sp.getString(pref, ""));
        l.addView(e);
        final boolean on = sp.getBoolean("src_" + src, false);
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(on ? "Ulangan ✓  (uzish uchun bosing)" : "Ulash");
        b.setOnClickListener(v -> {
            if (on) {
                sp.edit().putBoolean("src_" + src, false).apply();
            } else {
                String val = e.getText().toString().trim();
                if (required && val.length() < 5) {
                    Toast.makeText(this, "Avval maydonni to‘ldiring", Toast.LENGTH_SHORT).show();
                    return;
                }
                sp.edit().putString(pref, val).putBoolean("src_" + src, true).apply();
            }
            showConnect2();
        });
        l.addView(b);
    }

    private void showConnect2() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(BG);
        l.setPadding(dp(16), dp(36), dp(16), dp(16));
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(iconBtn("≡", v -> showDrawer()), new LinearLayout.LayoutParams(dp(52), dp(48)));
        TextView t = new TextView(this);
        t.setText("Ulash");
        t.setTextSize(22);
        t.setTypeface(null, Typeface.BOLD);
        t.setTextColor(INK);
        top.addView(t);
        l.addView(top);
        TextView info = new TextView(this);
        info.setTextColor(0xFFB5B5B5);
        info.setPadding(0, dp(8), 0, dp(4));
        info.setText("Qaysi hisobdan kelgan xabarlar o‘zbekcha aytilishini o‘zingiz tanlaysiz. Parol so‘ralmaydi. "
                + "Avval pastdagi ruxsatni bering, keyin har birini kiritib «Ulash» ni bosing.");
        l.addView(info);
        Button perm = new Button(this);
        perm.setAllCaps(false);
        perm.setText("Ruxsat berish (bildirishnomalarni o‘qish)");
        perm.setOnClickListener(v -> {
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 6);
            }
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        });
        l.addView(perm);
        addSource(l, "Email (Gmail)", "email_addr", "Pochta", "Gmail manzilingiz: ism@gmail.com",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, true);
        addSource(l, "Telegram", "tg_name", "Telegram", "Telegram nomingiz: @nom (ixtiyoriy)",
                InputType.TYPE_CLASS_TEXT, false);
        addSource(l, "Telefon raqam (SMS)", "phone", "SMS", "+998 90 123 45 67",
                InputType.TYPE_CLASS_PHONE, true);
        Button bat = new Button(this);
        bat.setAllCaps(false);
        bat.setText("Orqa fonda ishlashga ruxsat (batareya)");
        bat.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName())));
            } catch (Exception e) {
                startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
            }
        });
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        blp.setMargins(0, dp(20), 0, 0);
        l.addView(bat, blp);
        Button back = new Button(this);
        back.setText("Orqaga");
        back.setOnClickListener(v -> showChat());
        l.addView(back);
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(BG);
        sv.addView(l);
        setContentView(sv);
    }

    private void drawImage(final String prompt, final TextView out, final String key) {
        out.setText("Rasm chizilmoqda...");
        busy = true;
        sendBtn.setEnabled(false);
        new Thread(() -> {
            Bitmap bm = null;
            String err = "Rasm chizib bo\u2018lmadi. Boshqacha tasvirlab yozing yoki keyinroq urinib ko\u2018ring.";
            try {
                JSONObject body = new JSONObject()
                        .put("contents", new JSONArray().put(new JSONObject().put("role", "user")
                                .put("parts", new JSONArray().put(new JSONObject().put("text",
                                        "Rasm chiz (draw an image): " + prompt)))))
                        .put("generationConfig", new JSONObject()
                                .put("responseModalities", new JSONArray().put("TEXT").put("IMAGE")));
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent");
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setConnectTimeout(20000);
                c.setReadTimeout(120000);
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
                if (code == 429) {
                    err = "Rasm chizish hozir band. Keyinroq urinib ko\u2018ring.";
                } else if (code >= 200 && code < 300) {
                    JSONArray ps = new JSONObject(resp).getJSONArray("candidates").getJSONObject(0)
                            .getJSONObject("content").getJSONArray("parts");
                    for (int i = 0; i < ps.length(); i++) {
                        JSONObject pt = ps.getJSONObject(i);
                        JSONObject d = pt.optJSONObject("inlineData");
                        if (d == null) d = pt.optJSONObject("inline_data");
                        if (d != null) {
                            byte[] raw = Base64.decode(d.getString("data"), Base64.DEFAULT);
                            bm = BitmapFactory.decodeByteArray(raw, 0, raw.length);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            final Bitmap fb = bm;
            final String ferr = err;
            runOnUiThread(() -> {
                busy = false;
                sendBtn.setEnabled(true);
                if (fb == null) {
                    out.setText(ferr);
                    hist.remove(hist.size() - 1);
                    saveHist();
                    return;
                }
                chat.removeView(out);
                ImageView iv = new ImageView(this);
                iv.setImageBitmap(fb);
                iv.setAdjustViewBounds(true);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(300), ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, dp(5), 0, dp(5));
                chat.addView(iv, lp);
                Button sv = new Button(this);
                sv.setText("Galereyaga saqlash");
                sv.setOnClickListener(v -> saveImage(fb));
                chat.addView(sv);
                hist.add(new String[]{"model", "[Rasm chizildi: " + prompt + "]"});
                saveHist();
                scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
            });
        }).start();
    }

    private void saveImage(Bitmap bm) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Images.Media.DISPLAY_NAME, "zafaron_" + System.currentTimeMillis() + ".png");
                cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                cv.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Zafaron");
                Uri u = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
                OutputStream o = getContentResolver().openOutputStream(u);
                bm.compress(Bitmap.CompressFormat.PNG, 100, o);
                o.close();
                Toast.makeText(this, "Galereyaga saqlandi", Toast.LENGTH_SHORT).show();
            } else {
                String r = MediaStore.Images.Media.insertImage(getContentResolver(), bm, "zafaron", "Zafaron");
                Toast.makeText(this, r != null ? "Galereyaga saqlandi" : "Saqlab bo\u2018lmadi", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Saqlab bo\u2018lmadi", Toast.LENGTH_SHORT).show();
        }
    }

    private void showConnect() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Ulash")
                .setMessage("Telegram, Instagram, pochta (Gmail) va telefon raqamingizga kelgan SMS xabarlarini o\u2018zbekcha aytib turishim uchun uchta ruxsat kerak:\n\n"
                        + "1) Bildirishnoma yuborish\n2) Bildirishnomalarni o\u2018qish (Zafaron ni yoqing)\n3) Orqa fonda ishlash (batareya)\n\n"
                        + "Parolingiz so\u2018ralmaydi. Telegram, Instagram va Gmail ilovalarida hisobingiz ochiq bo\u2018lsa kifoya.")
                .setPositiveButton("Ruxsat berish", (d, w) -> {
                    if (android.os.Build.VERSION.SDK_INT >= 33) {
                        requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 6);
                    }
                    startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    Toast.makeText(this, "Ro\u2018yxatdan Zafaron ni toping va yoqing", Toast.LENGTH_LONG).show();
                })
                .setNeutralButton("Batareya", (d, w) -> {
                    try {
                        startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                Uri.parse("package:" + getPackageName())));
                    } catch (Exception e) {
                        startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                    }
                })
                .setNegativeButton("Bekor", null)
                .show();
    }

    private void showVideo() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(BG);
        l.setPadding(dp(16), dp(40), dp(16), dp(12));
        TextView t = new TextView(this);
        t.setText("Video yasash");
        t.setTextSize(26);
        t.setTypeface(null, Typeface.BOLD);
        t.setTextColor(SAFFRON);
        l.addView(t);
        TextView info = new TextView(this);
        info.setTextColor(INK);
        info.setPadding(0, dp(6), 0, dp(6));
        info.setText("Tez kesiladigan, pastda qalin yozuvli qisqa video. Har bir qator \u2014 bitta kadr. Mavzuni yozing va Zafaron matnni yozsin, yoki o\u2018zingiz yozing. Video ovozsiz chiqadi.");
        l.addView(info);
        final EditText topic = new EditText(this);
        topic.setHint("Mavzu, masalan: qorong\u2018i uyda bir tun");
        l.addView(topic);
        final EditText lines = new EditText(this);
        lines.setHint("Har qatorga bitta qisqa yozuv");
        lines.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        lines.setMinLines(6);
        lines.setGravity(Gravity.TOP);
        final Button gen = new Button(this);
        gen.setText("Matnni Zafaron yozsin");
        final TextView status = new TextView(this);
        status.setTextColor(INK);
        gen.setOnClickListener(v -> {
            String tp = topic.getText().toString().trim();
            if (tp.isEmpty()) {
                Toast.makeText(this, "Avval mavzuni yozing", Toast.LENGTH_SHORT).show();
                return;
            }
            status.setText("Yozilmoqda...");
            final String key = sp.getString("key", "");
            new Thread(() -> {
                String r = null;
                try {
                    r = NotifService.ask(key, "\"" + tp + "\" mavzusida qisqa tez kesiladigan video uchun 8 ta juda qisqa yozuv yoz (har biri 1-4 so\u2018z), "
                            + "o\u2018zbek tilida (lotin, \u015f \u00e7 \u00f6 \u011f harflari bilan). Qiziqarli boshla, oxirida kuchli xulosa. Faqat yozuvlar, har qatorga bittadan, raqamsiz.");
                } catch (Exception ignored) {
                }
                final String fr = r;
                runOnUiThread(() -> {
                    if (fr == null || fr.isEmpty()) {
                        status.setText("Matn olinmadi. Internetni va kalitni tekshiring.");
                    } else {
                        lines.setText(fr.replace("*", "").trim());
                        status.setText("Tayyor. Xohlasangiz tahrir qiling.");
                    }
                });
            }).start();
        });
        l.addView(gen);
        l.addView(lines);
        final Button make = new Button(this);
        make.setText("Videoni yasash");
        make.setOnClickListener(v -> {
            final List<String> caps = new ArrayList<>();
            for (String ln : lines.getText().toString().split("\n")) {
                if (!ln.trim().isEmpty()) caps.add(ln.trim());
            }
            if (caps.isEmpty()) {
                Toast.makeText(this, "Matn yozing", Toast.LENGTH_SHORT).show();
                return;
            }
            make.setEnabled(false);
            status.setText("Video yasalmoqda: 0%");
            new Thread(() -> {
                Uri res = null;
                String err = null;
                try {
                    java.io.File f = VideoMaker.make(this, caps, p -> runOnUiThread(() -> status.setText("Video yasalmoqda: " + p + "%")));
                    res = saveVideo(f);
                } catch (Throwable e) {
                    err = "Video yasab bo\u2018lmadi (" + e.getClass().getSimpleName() + ")";
                }
                final Uri fu = res;
                final String fe = err;
                runOnUiThread(() -> {
                    make.setEnabled(true);
                    if (fu != null) {
                        status.setText("Tayyor! Galereyada: Movies/Zafaron");
                        Intent it = new Intent(Intent.ACTION_VIEW);
                        it.setDataAndType(fu, "video/mp4");
                        it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        try {
                            startActivity(it);
                        } catch (Exception ignored) {
                        }
                    } else {
                        status.setText(fe != null ? fe : "Saqlab bo\u2018lmadi");
                    }
                });
            }).start();
        });
        l.addView(make);
        l.addView(status);
        Button back = new Button(this);
        back.setText("Orqaga");
        back.setOnClickListener(v -> showChat());
        l.addView(back);
        ScrollView sv = new ScrollView(this);
        sv.addView(l);
        setContentView(sv);
    }

    private Uri saveVideo(java.io.File f) throws Exception {
        if (android.os.Build.VERSION.SDK_INT < 29) return null;
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Video.Media.DISPLAY_NAME, "zafaron_" + System.currentTimeMillis() + ".mp4");
        cv.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        cv.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Zafaron");
        Uri u = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, cv);
        OutputStream o = getContentResolver().openOutputStream(u);
        InputStream in = new java.io.FileInputStream(f);
        byte[] buf = new byte[65536];
        int n;
        while ((n = in.read(buf)) > 0) o.write(buf, 0, n);
        in.close();
        o.close();
        return u;
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
        info.setText("Hech narsa o\u2018zi ulanmaydi. Avval \u00abRuxsat berish\u00bb ni bosib Zafaron ni yoqing, keyin qaysi hisobni o\u2018zingiz xohlasangiz, shuni pastda \u00abulash\u00bb bilan yoqasiz.");
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

        TextView st = new TextView(this);
        st.setTextColor(INK);
        st.setPadding(0, dp(6), 0, dp(6));
        String en = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        boolean on = en != null && en.contains(getPackageName());
        StringBuilder ss = new StringBuilder("Ruxsat: " + (on ? "yoqilgan \u2713" : "yoqilmagan \u2717") + "\n");
        try {
            JSONArray ia = new JSONArray(sp.getString("inbox", "[]"));
            String[] srcs = {"Telegram", "Instagram", "SMS", "Pochta"};
            for (String sname : srcs) {
                boolean seen = false;
                for (int k = 0; k < ia.length(); k++) if (ia.getJSONObject(k).getString("a").equals(sname)) seen = true;
                
            }
        } catch (Exception ignored) {
        }
        st.setText(ss.toString().trim());
        l.addView(st);
        for (final String sname : new String[]{"Telegram", "Instagram", "SMS", "Pochta"}) {
            final Button tb = new Button(this);
            tb.setText(sname + (sp.getBoolean("src_" + sname, false) ? ": ulangan \u2713 (o\u2018chirish uchun bosing)" : ": ulash"));
            tb.setOnClickListener(v -> {
                sp.edit().putBoolean("src_" + sname, !sp.getBoolean("src_" + sname, false)).apply();
                showInbox();
            });
            l.addView(tb);
        }
        Button bat = new Button(this);
        bat.setText("Orqa fonda ishlashga ruxsat (batareya)");
        bat.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName())));
            } catch (Exception e) {
                startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
            }
        });
        l.addView(bat);

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
        Button keyB = new Button(this);
        keyB.setText("Kalit");
        keyB.setOnClickListener(v -> showSetup());
        row.addView(keyB);
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
        if (hist.isEmpty()) {
            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setGravity(Gravity.CENTER);
            box.addView(roundLogo(96), new LinearLayout.LayoutParams(dp(96), dp(96)));
            TextView hi = new TextView(this);
            hi.setText("Salom!");
            hi.setTextSize(32);
            hi.setTypeface(Typeface.SERIF);
            hi.setTextColor(INK);
            hi.setGravity(Gravity.CENTER);
            hi.setPadding(0, dp(18), 0, dp(14));
            box.addView(hi);

            chat.addView(box, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(480)));
        }
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
        g.setColor(me ? 0xFF2B2B2B : Color.TRANSPARENT);
        t.setTextColor(INK);
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

    private void pick(String type) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType(type);
        if (type.equals("*/*")) i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*", "text/*", "application/json"});
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, PICK);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == CAM && res == RESULT_OK && camUri != null) {
            data = new Intent().setData(camUri);
        } else if (req != PICK || res != RESULT_OK || data == null) {
            return;
        }
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
        if (hist.size() == 1) chat.removeAllViews();
        pend.clear();
        inp.setText("");
        drawAtt();
        bubble(label, true);
        final TextView out = bubble("…", false);
        busy = true;
        sendBtn.setEnabled(false);
        final int n = hist.size();
        String lowq = shown.toLowerCase();
        if (lowq.startsWith("rasm chiz") || lowq.startsWith("rasm yarat") || lowq.startsWith("/rasm")) {
            drawImage(shown, out, key);
            return;
        }

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

                String[] models = sp.getBoolean("pro2", false)
                        ? new String[]{"gemini-2.5-pro", "gemini-2.5-flash", "gemini-2.5-flash-lite", MODEL}
                        : new String[]{"gemini-2.5-flash", "gemini-2.5-flash-lite", MODEL};
                int code = 0;
                String resp = "";
                for (int round = 0; round < 3; round++) {
                for (String mdl : models) {
                for (int tool = 1; tool >= 0; tool--) {
                    if (tool == 1) body.put("tools", new JSONArray().put(new JSONObject().put("google_search", new JSONObject())));
                    else body.remove("tools");
                    URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/" + mdl + ":generateContent");
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
                    code = c.getResponseCode();
                    InputStream is = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    int r;
                    while (is != null && (r = is.read(buf)) > 0) bo.write(buf, 0, r);
                    resp = bo.toString("UTF-8");
                    if (code != 429) break;
                }
                    if (code != 429 && code != 503 && code != 404) break;
                }
                if (code != 429 && code != 503 && code != 404) break;
                try {
                    Thread.sleep(4000);
                } catch (Exception ignored) {
                }
                }
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
                        ans = "Kalit noto‘g‘ri. Yon menyudagi logoni bosing → Sozlamalar → Google kaliti.";
                    } else if (code == 429) {
                        ans = "Zafaron hozir band. Bir ozdan keyin qayta yuboring." + (msg.isEmpty() ? "" : "\n(" + msg.substring(0, Math.min(140, msg.length())) + ")");
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
