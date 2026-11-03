package uz.latin.keyboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LatinIME extends InputMethodService {

    /** Sozlamalar ekrani "tozalash" bosilganda shuni oshiradi. */
    static volatile int clearGen = 0;

    private static final String SHIFT = "SHIFT";
    private static final String DEL = "DEL";
    private static final String P_A = "P_A";
    private static final String P_B = "P_B";
    private static final String NUM = "NUM";
    private static final String ABC = "ABC";
    private static final String SPACE = "SPACE";
    private static final String ENTER = "ENTER";
    private static final String TAB = "TAB";

    private static final int LETTERS_PAGE = 0;
    private static final int SYM_A = 1;
    private static final int SYM_B = 2;
    private static final int NUMPAD = 3;
    private static final int EMOJI_PAGE = 4;
    private static final int LANG_PAGE = 5;

    // Yodlash chegaralari
    private static final int WORD_TOP = 2;      // so'z 2 marta yozilsa, birinchi bo'lib chiqadi
    private static final int PAIR_MIN = 2;      // juftlik 2 marta yozilsa, keyingi so'z chiqadi
    private static final int PHRASE_MIN = 3;    // jumla 3 marta yozilsa, to'liq chiqadi

    private static final String[] NUMBERS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
    private static final String[] BOTTOM = {P_A, NUM, ",", SPACE, ".", ENTER};

    // O'zbek lotin: ş = ş, ç = ç, ö = ö, ğ = ğ, ’ = ’
    private static final String[][] LETTERS = {
            NUMBERS,
            {"q", "w", "e", "r", "t", "y", "u", "i", "o", "p"},
            {"a", "s", "d", "f", "g", "h", "j", "k", "l"},
            {"’", "z", "x", "c", "v", "b", "n", "m", "-"},
            {SHIFT, "!", "ş", "ç", "ö", "ğ", "?", DEL},
            BOTTOM
    };

    // O'zbek kirill
    private static final String[][] CYRILLIC = {
            NUMBERS,
            {"й", "ц", "у", "к", "е", "н", "г", "ш", "ў", "з"},
            {"ф", "қ", "в", "а", "п", "р", "о", "л", "д", "ж"},
            {"я", "ч", "с", "м", "и", "т", "ь", "б", "ю", "х"},
            {SHIFT, "ё", "ъ", "э", "ғ", "ҳ", "ы", "щ", DEL},
            BOTTOM
    };

    private static final String[][] PAGE_A = {
            NUMBERS,
            {"%", "=", "×", "+", "•", "~", "✓", "«", "»", "♡"},
            {"@", "#", "$", "&", "_", "-", "—", "(", ")", "/"},
            {P_B, "*", "\"", "'", ":", ";", "!", "?", DEL},
            {ABC, NUM, ",", SPACE, ".", ENTER}
    };

    private static final String[][] PAGE_B = {
            NUMBERS,
            {"₽", "€", "£", "¥", "₩", "`", "|", "÷", "√", "π"},
            {TAB, "^", "°", "{", "}", "[", "]", "<", ">", "\\"},
            {P_A, "№", "Δ", "®", "™", ":-)", "§", "©", DEL},
            {ABC, NUM, ",", SPACE, ".", ENTER}
    };

    private static final String[] NUM_LEFT = {"’", "+", "×", "/", "-", ":"};
    private static final String[][] NUM_GRID = {
            {"1", "2", "3", DEL},
            {"4", "5", "6", P_A},
            {"7", "8", "9", SPACE},
            {ABC, "0", ".", ENTER}
    };

    private static final String[] EMOJI_ALL;
    private static final String[] EMOJI_DEFAULT;

    static {
        int[] cps = {
                0x1F600, 0x1F603, 0x1F604, 0x1F601, 0x1F606, 0x1F605, 0x1F602, 0x1F923,
                0x1F60A, 0x1F607, 0x1F642, 0x1F609, 0x1F60D, 0x1F618, 0x1F617, 0x1F60B,
                0x1F61B, 0x1F61C, 0x1F914, 0x1F610, 0x1F611, 0x1F644, 0x1F60F, 0x1F623,
                0x1F625, 0x1F62E, 0x1F62A, 0x1F634, 0x1F60C, 0x1F61F, 0x1F641, 0x1F622,
                0x1F62D, 0x1F631, 0x1F616, 0x1F629, 0x1F624, 0x1F621, 0x1F620, 0x1F608,
                0x1F480, 0x1F4A9, 0x1F921, 0x1F47B, 0x1F47D, 0x1F916, 0x1F648, 0x1F649,
                0x1F44D, 0x1F44E, 0x1F44C, 0x1F91E, 0x1F91D, 0x1F44F, 0x1F64C, 0x1F64F,
                0x1F4AA, 0x1F44B, 0x1F448, 0x1F449, 0x1F446, 0x1F447, 0x1F440, 0x1F9E1,
                0x1F49B, 0x1F49A, 0x1F499, 0x1F49C, 0x1F5A4, 0x1F494, 0x1F495, 0x1F496,
                0x1F4AF, 0x1F525, 0x1F389, 0x1F381, 0x1F382, 0x1F31F, 0x1F308, 0x1F319,
                0x1F680, 0x1F697, 0x1F3E0, 0x1F4F1, 0x1F4BB, 0x1F4DA, 0x1F3B5, 0x1F355,
                0x1F354, 0x1F37A, 0x1F375, 0x1F3C6, 0x1F3AF, 0x1F4A1, 0x1F4B0, 0x1F339
        };
        List<String> l = new ArrayList<>();
        for (int cp : cps) l.add(new String(Character.toChars(cp)));
        Collections.addAll(l, "❤️", "✅", "❌", "⭐", "✨",
                "✌️", "☀️", "⚡");
        EMOJI_ALL = l.toArray(new String[0]);
        EMOJI_DEFAULT = new String[]{
                new String(Character.toChars(0x1F602)), "❤️",
                new String(Character.toChars(0x1F44D)), new String(Character.toChars(0x1F60D)),
                new String(Character.toChars(0x1F64F)), new String(Character.toChars(0x1F525)),
                new String(Character.toChars(0x1F389)), new String(Character.toChars(0x1F60A))
        };
    }

    /** Taklif: ko'rinadigan matn va uni qo'yishdan oldin o'chiriladigan belgilar soni. */
    private static final class Sug {
        final String text;
        final int del;

        Sug(String text, int del) {
            this.text = text;
            this.del = del;
        }
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable repeat;
    private LinearLayout root;
    private LinearLayout topHolder;
    private SharedPreferences prefs;
    private Lang lang;
    private boolean caps = false;
    private boolean suggestOn = true;
    private int page = LETTERS_PAGE;
    private int keyH;
    private int seenGen = 0;
    private int unsaved = 0;
    private final Map<String, List<String>> dicts = new HashMap<>();
    private final Map<String, Map<String, Integer>> stores = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences("kb", MODE_PRIVATE);
        lang = Lang.byId(prefs.getString("lang", "uz"));
        seenGen = clearGen;
    }

    @Override
    public View onCreateInputView() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF1C1C1E);
        int p = dp(4);
        root.setPadding(p, 0, p, p + dp(2));
        build();
        return root;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        int it = info == null ? 0 : info.inputType;
        int cls = it & InputType.TYPE_MASK_CLASS;
        int var = it & InputType.TYPE_MASK_VARIATION;
        boolean secret = cls == InputType.TYPE_CLASS_TEXT
                && (var == InputType.TYPE_TEXT_VARIATION_PASSWORD
                || var == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                || var == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
                || var == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                || var == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
                || var == InputType.TYPE_TEXT_VARIATION_URI);
        boolean numeric = cls == InputType.TYPE_CLASS_NUMBER || cls == InputType.TYPE_CLASS_PHONE;
        suggestOn = !(secret || numeric || (it & InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) != 0);
        page = numeric ? NUMPAD : LETTERS_PAGE;
        caps = false;
        if (root != null) build();
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        saveAll();
    }

    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd,
                                  int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        if (root != null && page == LETTERS_PAGE) refreshTop();
    }

    // ------------------------------------------------------------------
    // Qurilish
    // ------------------------------------------------------------------

    private void build() {
        root.removeAllViews();
        topHolder = new LinearLayout(this);
        topHolder.setOrientation(LinearLayout.VERTICAL);
        topHolder.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(topHolder);

        boolean extra = page == LETTERS_PAGE && lang.kind >= Lang.LATIN && lang.extras.length() > 0;
        keyH = extra ? dp(42) : dp(48);

        if (page == NUMPAD) {
            buildNumpad();
        } else if (page == LANG_PAGE) {
            buildLangPage();
        } else if (page == EMOJI_PAGE) {
            buildEmojiPage();
        } else {
            String[][] rows = rowsForPage();
            for (int i = 0; i < rows.length; i++) {
                root.addView(row(rows[i]));
                if (i == 0 && extra) root.addView(extrasRow(chars(lang.extras)));
            }
        }
        refreshTop();
    }

    private String[][] rowsForPage() {
        if (page == SYM_A) return PAGE_A;
        if (page == SYM_B) return PAGE_B;
        if (lang.kind == Lang.UZ_LATIN) return LETTERS;
        if (lang.kind == Lang.UZ_CYR) return CYRILLIC;
        String[] c3 = chars(lang.r3);
        String[] row3 = new String[c3.length + 2];
        row3[0] = SHIFT;
        System.arraycopy(c3, 0, row3, 1, c3.length);
        row3[row3.length - 1] = DEL;
        return new String[][]{NUMBERS, chars(lang.r1), chars(lang.r2), row3, BOTTOM};
    }

    private static String[] chars(String s) {
        List<String> l = new ArrayList<>();
        int i = 0;
        while (i < s.length()) {
            int cp = s.codePointAt(i);
            l.add(new String(Character.toChars(cp)));
            i += Character.charCount(cp);
        }
        return l.toArray(new String[0]);
    }

    private View extrasRow(String[] ks) {
        if (ks.length <= 10) return row(ks);
        HorizontalScrollView sv = new HorizontalScrollView(this);
        sv.setHorizontalScrollBarEnabled(false);
        sv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout in = new LinearLayout(this);
        in.setOrientation(LinearLayout.HORIZONTAL);
        for (String k : ks) {
            View v = key(k, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(46), keyH);
            lp.setMargins(dp(2), dp(3), dp(2), dp(3));
            v.setLayoutParams(lp);
            in.addView(v);
        }
        sv.addView(in);
        return sv;
    }

    // ------------------------------------------------------------------
    // Smaylik sahifasi: eng ko'p ishlatilganlar + hammasi
    // ------------------------------------------------------------------

    private String[] recentEmoji(int n) {
        List<Map.Entry<String, Integer>> es = new ArrayList<>(store("emoji").entrySet());
        Collections.sort(es, (x, y) -> y.getValue() - x.getValue());
        List<String> r = new ArrayList<>();
        for (Map.Entry<String, Integer> e : es) {
            if (r.size() >= n) break;
            r.add(e.getKey());
        }
        for (String d : EMOJI_DEFAULT) {
            if (r.size() >= n) break;
            if (!r.contains(d)) r.add(d);
        }
        return r.toArray(new String[0]);
    }

    private void buildEmojiPage() {
        root.addView(row(recentEmoji(8)));
        ScrollView sv = new ScrollView(this);
        sv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(204)));
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < EMOJI_ALL.length; i += 8) {
            int n = Math.min(8, EMOJI_ALL.length - i);
            String[] ks = new String[n];
            System.arraycopy(EMOJI_ALL, i, ks, 0, n);
            LinearLayout r = (LinearLayout) row(ks);
            if (n < 8) r.addView(spacer(8 - n));
            col.addView(r);
        }
        sv.addView(col);
        root.addView(sv);
        root.addView(row(new String[]{ABC, SPACE, DEL}));
    }

    // ------------------------------------------------------------------
    // Tepadagi qator: til tugmalari yoki so'z takliflari
    // ------------------------------------------------------------------

    private void refreshTop() {
        if (topHolder == null) return;
        topHolder.removeAllViews();
        List<Sug> sg = computeSuggestions();
        topHolder.addView(sg.isEmpty() ? toolbar() : suggestionBar(sg));
    }

    private View toolbar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(42)));

        bar.addView(tab("Öz", lang.kind == Lang.UZ_LATIN, v -> setLang(Lang.byId("uz"))));
        bar.addView(tab("Ўз", lang.kind == Lang.UZ_CYR, v -> setLang(Lang.byId("uzc"))));
        if (lang.kind != Lang.UZ_LATIN && lang.kind != Lang.UZ_CYR) {
            bar.addView(tab(lang.code, true, v -> goTo(LANG_PAGE)));
        }
        TextView globe = tool("🌐", v -> goTo(LANG_PAGE));
        globe.setOnLongClickListener(v -> {
            showPicker();
            return true;
        });
        bar.addView(globe);

        View sp = new View(this);
        sp.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        bar.addView(sp);

        bar.addView(tool("😊", v -> goTo(EMOJI_PAGE)));
        bar.addView(tool("⚙", v -> openSettings()));
        bar.addView(tool("▽", v -> requestHideSelf(0)));
        return bar;
    }

    private View suggestionBar(List<Sug> sg) {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(42)));
        for (int i = 0; i < 3; i++) {
            final Sug s = i < sg.size() ? sg.get(i) : null;
            TextView t = new TextView(this);
            t.setGravity(Gravity.CENTER);
            t.setSingleLine(true);
            t.setEllipsize(TextUtils.TruncateAt.END);
            t.setTextSize(17);
            t.setTextColor(0xFFFFFFFF);
            t.setTypeface(null, i == 0 ? Typeface.BOLD : Typeface.NORMAL);
            t.setPadding(dp(6), 0, dp(6), 0);
            t.setText(s == null ? "" : s.text);
            t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            if (s != null) {
                t.setClickable(true);
                t.setOnClickListener(v -> applySuggestion(s));
            }
            bar.addView(t);
            if (i < 2) {
                View d = new View(this);
                d.setBackgroundColor(0xFF3A3A3C);
                bar.addView(d, new LinearLayout.LayoutParams(dp(1), dp(24)));
            }
        }
        return bar;
    }

    private TextView tab(String text, boolean selected, View.OnClickListener l) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setGravity(Gravity.CENTER);
        t.setTextSize(19);
        t.setTextColor(selected ? 0xFFFFFFFF : 0xFF7C7C80);
        t.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        t.setPadding(dp(10), 0, dp(10), 0);
        t.setClickable(true);
        t.setOnClickListener(l);
        t.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return t;
    }

    private TextView tool(String text, View.OnClickListener l) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setGravity(Gravity.CENTER);
        t.setTextSize(20);
        t.setTextColor(0xFFC7C7CC);
        t.setPadding(dp(12), 0, dp(12), 0);
        t.setClickable(true);
        t.setOnClickListener(l);
        t.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return t;
    }

    private void showPicker() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showInputMethodPicker();
    }

    private void openSettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    // ------------------------------------------------------------------
    // Til tanlash sahifasi
    // ------------------------------------------------------------------

    private void setLang(Lang l) {
        lang = l;
        page = LETTERS_PAGE;
        caps = false;
        prefs.edit().putString("lang", l.id).apply();
        build();
    }

    private void buildLangPage() {
        final ScrollView sv = new ScrollView(this);
        sv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(264)));
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        List<Lang> all = Lang.ALL;
        LinearLayout row = null;
        int selRow = 0;
        for (int i = 0; i < all.size(); i++) {
            if (i % 3 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                col.addView(row, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(46)));
            }
            final Lang l = all.get(i);
            boolean sel = l == lang;
            if (sel) selRow = i / 3;
            TextView t = new TextView(this);
            t.setGravity(Gravity.CENTER);
            t.setSingleLine(true);
            t.setEllipsize(TextUtils.TruncateAt.END);
            t.setTextSize(15);
            t.setText(l.name);
            t.setTextColor(sel ? 0xFF1C1C1E : 0xFFFFFFFF);
            t.setBackground(shape(sel ? 0xFFFFE250 : 0xFF48484A));
            t.setClickable(true);
            t.setOnClickListener(v -> setLang(l));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            lp.setMargins(dp(2), dp(3), dp(2), dp(3));
            row.addView(t, lp);
        }
        int rem = (3 - all.size() % 3) % 3;
        for (int j = 0; j < rem; j++) {
            row.addView(new View(this), new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        }
        sv.addView(col);
        root.addView(sv);
        final int y = Math.max(0, selRow - 1) * dp(46);
        sv.post(() -> sv.scrollTo(0, y));
        root.addView(row(new String[]{ABC}));
    }

    // ------------------------------------------------------------------
    // Raqamlar paneli va qatorlar
    // ------------------------------------------------------------------

    private void buildNumpad() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.HORIZONTAL);
        wrap.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.7f));
        for (String k : NUM_LEFT) {
            View v = key(k, false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(30));
            lp.setMargins(dp(2), dp(3), dp(2), dp(3));
            v.setLayoutParams(lp);
            left.addView(v);
        }

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 3f));
        for (String[] r : NUM_GRID) grid.addView(row(r));

        wrap.addView(left);
        wrap.addView(grid);
        root.addView(wrap);
    }

    private View row(String[] keys) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        float total = 0;
        for (String k : keys) total += weight(k);
        boolean dense = total > 11.5f;
        boolean center = page == LETTERS_PAGE && !hasSpecial(keys) && keys.length < 10;
        float side = (10 - keys.length) / 2f;
        if (center) row.addView(spacer(side));
        for (String k : keys) {
            View v = key(k, dense);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, keyH, weight(k));
            lp.setMargins(dp(2), dp(3), dp(2), dp(3));
            v.setLayoutParams(lp);
            row.addView(v);
        }
        if (center) row.addView(spacer(side));
        return row;
    }

    private View spacer(float w) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, 1, w));
        return v;
    }

    private boolean hasSpecial(String[] keys) {
        for (String k : keys) if (isSpecial(k)) return true;
        return false;
    }

    private boolean isSpecial(String k) {
        return k.equals(SHIFT) || k.equals(DEL) || k.equals(P_A) || k.equals(P_B)
                || k.equals(NUM) || k.equals(ABC) || k.equals(SPACE) || k.equals(ENTER)
                || k.equals(TAB);
    }

    private boolean isHighlight(String k) {
        if (lang.kind == Lang.UZ_LATIN) {
            return k.equals("ş") || k.equals("ç") || k.equals("ö") || k.equals("ğ");
        }
        return lang.kind >= Lang.LATIN && page == LETTERS_PAGE
                && lang.extras.length() > 0 && lang.extras.contains(k);
    }

    private float weight(String k) {
        if (page == NUMPAD) return 1f;
        switch (k) {
            case SHIFT:
            case DEL:
            case P_A:
            case P_B:
            case NUM:
            case ABC:
            case ENTER:
                return 1.5f;
            case SPACE:
                return 4f;
            default:
                return 1f;
        }
    }

    private String upper(String k) {
        String u = k.toUpperCase(lang.loc);
        return u.length() == k.length() ? u : k;
    }

    private String label(String k) {
        switch (k) {
            case SHIFT:
                return caps ? "⇪" : "⇧";
            case DEL:
                return "⌫";
            case P_A:
                return page == SYM_B ? "#!?" : "@?!";
            case P_B:
                return "÷\\<";
            case NUM:
                return "123";
            case ABC:
                if (page == NUMPAD) return "↩";
                if (lang.kind == Lang.UZ_CYR || lang.kind == Lang.CYR) return "АБВ";
                if (lang.kind == Lang.OTHER) return lang.code;
                return "ABC";
            case SPACE:
                return lang.name;
            case ENTER:
                return "↵";
            case TAB:
                return "⇥";
            default:
                return (caps && page == LETTERS_PAGE) ? upper(k) : k;
        }
    }

    private View key(final String k, boolean dense) {
        TextView t = new TextView(this);
        t.setGravity(Gravity.CENTER);
        t.setTextColor(0xFFFFFFFF);
        boolean special = isSpecial(k);
        t.setTextSize(special ? 16 : (page == EMOJI_PAGE ? 24 : (dense ? 17 : 20)));
        t.setText(label(k));
        int color = special ? 0xFF2C2C2E : (isHighlight(k) ? 0xFF3D4A66 : 0xFF48484A);
        t.setBackground(bg(color));
        t.setClickable(true);

        if (k.equals(DEL)) {
            attachDelete(t);
        } else {
            t.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                onKey(k);
            });
            if (k.equals(SPACE)) {
                t.setOnLongClickListener(v -> {
                    showPicker();
                    return true;
                });
            }
        }
        return t;
    }

    private Drawable bg(int color) {
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_pressed}, shape(0xFF7A7A80));
        s.addState(new int[]{}, shape(color));
        return s;
    }

    private GradientDrawable shape(int color) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(6));
        return g;
    }

    private void attachDelete(View v) {
        v.setOnTouchListener((view, e) -> {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    view.setPressed(true);
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    deleteOne();
                    repeat = new Runnable() {
                        @Override
                        public void run() {
                            deleteOne();
                            handler.postDelayed(this, 50);
                        }
                    };
                    handler.postDelayed(repeat, 400);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    view.setPressed(false);
                    if (repeat != null) {
                        handler.removeCallbacks(repeat);
                        repeat = null;
                    }
                    return true;
                default:
                    return true;
            }
        });
    }

    private void goTo(int p) {
        page = p;
        build();
    }

    // ------------------------------------------------------------------
    // Tugmalar
    // ------------------------------------------------------------------

    private void onKey(String k) {
        switch (k) {
            case SHIFT:
                caps = !caps;
                build();
                return;
            case P_A:
                goTo(SYM_A);
                return;
            case P_B:
                goTo(SYM_B);
                return;
            case NUM:
                goTo(NUMPAD);
                return;
            case ABC:
                goTo(LETTERS_PAGE);
                return;
            case SPACE:
                learnCurrent();
                commit(" ");
                refreshTop();
                return;
            case ENTER:
                learnCurrent();
                learnSentence();
                enter();
                refreshTop();
                return;
            case TAB:
                commit("\t");
                return;
            default:
                boolean wasCaps = caps && page == LETTERS_PAGE;
                if (page == EMOJI_PAGE) {
                    countEmoji(k);
                } else if (isSeparator(k)) {
                    learnCurrent();
                    if (k.equals(".") || k.equals("!") || k.equals("?")) learnSentence();
                }
                commit(wasCaps ? upper(k) : k);
                if (wasCaps) {
                    caps = false;
                    build();
                } else if (page == LETTERS_PAGE) {
                    refreshTop();
                }
        }
    }

    private void commit(String s) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.commitText(s, 1);
    }

    private void deleteOne() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        CharSequence sel = ic.getSelectedText(0);
        if (sel != null && sel.length() > 0) {
            ic.commitText("", 1);
        } else {
            ic.deleteSurroundingText(1, 0);
        }
        if (page == LETTERS_PAGE) refreshTop();
    }

    private void enter() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        EditorInfo ei = getCurrentInputEditorInfo();
        int action = ei == null ? 0 : (ei.imeOptions & EditorInfo.IME_MASK_ACTION);
        boolean noEnterAction = ei != null && (ei.imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0;
        if (!noEnterAction && action != EditorInfo.IME_ACTION_NONE
                && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(action);
        } else {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
        }
    }

    // ------------------------------------------------------------------
    // Xotira: so'zlar, juftliklar, jumlalar, smaylikalar
    // ------------------------------------------------------------------

    private void checkClear() {
        if (seenGen != clearGen) {
            stores.clear();
            seenGen = clearGen;
            unsaved = 0;
        }
    }

    private Map<String, Integer> store(String key) {
        checkClear();
        Map<String, Integer> m = stores.get(key);
        if (m == null) {
            m = new HashMap<>();
            String s = prefs.getString(key, "");
            for (String line : s.split("\n")) {
                int t = line.lastIndexOf('\t');
                if (t > 0) {
                    try {
                        m.put(line.substring(0, t), Integer.parseInt(line.substring(t + 1).trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            stores.put(key, m);
        }
        return m;
    }

    private void inc(Map<String, Integer> m, String k, int max) {
        Integer c = m.get(k);
        m.put(k, c == null ? 1 : c + 1);
        if (m.size() > max) {
            List<String> drop = new ArrayList<>();
            for (Map.Entry<String, Integer> e : m.entrySet()) if (e.getValue() <= 1) drop.add(e.getKey());
            for (String d : drop) m.remove(d);
        }
        unsaved++;
        if (unsaved >= 6) saveAll();
    }

    private void saveAll() {
        checkClear();
        if (prefs == null || stores.isEmpty()) return;
        SharedPreferences.Editor ed = prefs.edit();
        for (Map.Entry<String, Map<String, Integer>> e : stores.entrySet()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> w : e.getValue().entrySet()) {
                sb.append(w.getKey()).append('\t').append(w.getValue()).append('\n');
            }
            ed.putString(e.getKey(), sb.toString());
        }
        ed.apply();
        unsaved = 0;
    }

    private String clean(String s) {
        return s.replace('\t', ' ').replace('\n', ' ');
    }

    private String norm(String w) {
        w = w.toLowerCase(lang.loc);
        while (w.length() > 0 && (w.charAt(0) == '\'' || w.charAt(0) == '’')) w = w.substring(1);
        while (w.length() > 0 && (w.charAt(w.length() - 1) == '\'' || w.charAt(w.length() - 1) == '’')) {
            w = w.substring(0, w.length() - 1);
        }
        return w;
    }

    private void countEmoji(String e) {
        inc(store("emoji"), e, 300);
    }

    private void learn(String w) {
        if (!suggestOn) return;
        w = norm(w);
        if (w.length() < 2 || w.length() > 24) return;
        inc(store("w_" + lang.id), w, 3000);
    }

    private void learnPair(String prev, String next) {
        prev = norm(prev);
        next = norm(next);
        if (prev.isEmpty() || next.isEmpty() || prev.length() > 24 || next.length() > 24) return;
        inc(store("b_" + lang.id), prev + "\t" + next, 4000);
    }

    /** Kursor oldidagi so'zni (va oldingi so'z bilan juftligini) yodlaydi. */
    private void learnCurrent() {
        if (!suggestOn) return;
        String b = beforeText(120);
        int i = b.length();
        while (i > 0 && isWordChar(b.charAt(i - 1))) i--;
        String cur = b.substring(i);
        if (cur.isEmpty()) return;
        learn(cur);
        int j = i;
        while (j > 0 && b.charAt(j - 1) == ' ') j--;
        if (j < i && j > 0) {
            int k = j;
            while (k > 0 && isWordChar(b.charAt(k - 1))) k--;
            if (k < j) learnPair(b.substring(k, j), cur);
        }
    }

    /** Gap tugagach (nuqta, ?, !, Enter) butun jumlani yodlaydi. */
    private void learnSentence() {
        if (!suggestOn) return;
        String b = beforeText(120);
        int bd = lastBoundary(b);
        if (bd < 0 && b.length() >= 120) return;
        String seg = clean(b.substring(bd + 1).trim()).toLowerCase(lang.loc);
        if (seg.length() < 5 || seg.length() > 70) return;
        int spaces = 0;
        for (int i = 0; i < seg.length(); i++) if (seg.charAt(i) == ' ') spaces++;
        if (spaces < 1 || spaces > 8) return;
        inc(store("p_" + lang.id), seg, 1500);
    }

    // ------------------------------------------------------------------
    // So'z takliflari
    // ------------------------------------------------------------------

    private boolean isWordChar(char c) {
        return Character.isLetter(c) || c == '’' || c == '\'';
    }

    private boolean isSeparator(String k) {
        if (k.length() != 1) return false;
        char c = k.charAt(0);
        return !Character.isLetterOrDigit(c) && c != '’' && c != '\'';
    }

    private int lastBoundary(String b) {
        for (int i = b.length() - 1; i >= 0; i--) {
            char c = b.charAt(i);
            if (c == '.' || c == '!' || c == '?' || c == '\n') return i;
        }
        return -1;
    }

    private String ltrim(String s) {
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        return s.substring(i);
    }

    private String beforeText(int n) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return "";
        CharSequence cs = ic.getTextBeforeCursor(n, 0);
        return cs == null ? "" : cs.toString();
    }

    private List<String> dict(Lang l) {
        if (l.dict == null) return Collections.emptyList();
        List<String> d = dicts.get(l.dict);
        if (d == null) {
            d = Dicts.load(l.dict);
            dicts.put(l.dict, d);
        }
        return d;
    }

    private String capFirst(String s) {
        if (s.isEmpty()) return s;
        int n = Character.charCount(s.codePointAt(0));
        return s.substring(0, n).toUpperCase(lang.loc) + s.substring(n);
    }

    private void addSug(List<Sug> out, String t, int del) {
        if (out.size() >= 3 || t.isEmpty()) return;
        for (Sug s : out) if (s.text.equalsIgnoreCase(t)) return;
        out.add(new Sug(t, del));
    }

    private List<Sug> computeSuggestions() {
        List<Sug> out = new ArrayList<>();
        if (!suggestOn || page != LETTERS_PAGE) return out;
        String b = beforeText(120);
        int bd = lastBoundary(b);
        boolean truncated = bd < 0 && b.length() >= 120;
        int i = b.length();
        while (i > 0 && isWordChar(b.charAt(i - 1))) i--;
        String cur = b.substring(i);
        String segT = ltrim(b.substring(bd + 1));
        String lowSeg = segT.toLowerCase(lang.loc);

        String prev = "";
        int j = i;
        while (j > 0 && b.charAt(j - 1) == ' ') j--;
        if (j < i && j > 0) {
            int k = j;
            while (k > 0 && isWordChar(b.charAt(k - 1))) k--;
            if (k < j) prev = norm(b.substring(k, j));
        }

        // 1. Ko'p yozilgan jumlalar: boshini yozsangiz, to'liq chiqadi
        if (!truncated && lowSeg.length() >= 1 && lowSeg.length() <= 60) {
            List<Map.Entry<String, Integer>> ps = new ArrayList<>();
            for (Map.Entry<String, Integer> e : store("p_" + lang.id).entrySet()) {
                String k = e.getKey();
                if (e.getValue() >= PHRASE_MIN && k.length() > lowSeg.length() && k.startsWith(lowSeg)) {
                    ps.add(e);
                }
            }
            Collections.sort(ps, (x, y) -> y.getValue() - x.getValue());
            boolean cap = !segT.isEmpty() && Character.isUpperCase(segT.codePointAt(0));
            for (Map.Entry<String, Integer> e : ps) {
                if (out.size() >= 2) break;
                addSug(out, cap ? capFirst(e.getKey()) : e.getKey(), segT.length());
            }
        }

        Map<String, Integer> pairs = store("b_" + lang.id);
        List<String> dict = dict(lang);

        // 2. Bo'sh joydan keyin: keyingi so'z
        if (cur.isEmpty()) {
            if (prev.isEmpty()) return out;
            String key = prev + "\t";
            List<Map.Entry<String, Integer>> bs = new ArrayList<>();
            for (Map.Entry<String, Integer> e : pairs.entrySet()) {
                if (e.getValue() >= PAIR_MIN && e.getKey().startsWith(key)) bs.add(e);
            }
            Collections.sort(bs, (x, y) -> y.getValue() - x.getValue());
            for (Map.Entry<String, Integer> e : bs) addSug(out, e.getKey().substring(key.length()), 0);
            String pk = prev + " ";
            for (String p : dict) {
                if (p.startsWith(pk) && p.length() > pk.length()) addSug(out, p.substring(pk.length()), 0);
            }
            return out;
        }

        // 3. So'z boshlangan: harflarga mos so'zlar
        String low = cur.toLowerCase(lang.loc);
        boolean cap = Character.isUpperCase(cur.codePointAt(0));
        List<String> res = new ArrayList<>();

        if (!prev.isEmpty()) {
            String pk = prev + "\t" + low;
            List<Map.Entry<String, Integer>> bs = new ArrayList<>();
            for (Map.Entry<String, Integer> e : pairs.entrySet()) {
                if (e.getValue() >= PAIR_MIN && e.getKey().startsWith(pk)) bs.add(e);
            }
            Collections.sort(bs, (x, y) -> y.getValue() - x.getValue());
            for (Map.Entry<String, Integer> e : bs) {
                String w = e.getKey().substring(prev.length() + 1);
                if (!w.equals(low) && !res.contains(w)) res.add(w);
            }
        }

        List<Map.Entry<String, Integer>> es = new ArrayList<>();
        for (Map.Entry<String, Integer> e : store("w_" + lang.id).entrySet()) {
            if (e.getKey().startsWith(low) && !e.getKey().equals(low)) es.add(e);
        }
        Collections.sort(es, (x, y) -> y.getValue() - x.getValue());
        for (Map.Entry<String, Integer> e : es) {
            if (e.getValue() >= WORD_TOP && !res.contains(e.getKey())) res.add(e.getKey());
        }
        for (String p : dict) {
            if (p.startsWith(low) && !p.equals(low) && !res.contains(p)) res.add(p);
        }
        for (Map.Entry<String, Integer> e : es) {
            if (!res.contains(e.getKey())) res.add(e.getKey());
        }
        for (String r : res) addSug(out, cap ? capFirst(r) : r, cur.length());
        return out;
    }

    private void applySuggestion(Sug s) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        if (s.del > 0) ic.deleteSurroundingText(s.del, 0);
        ic.commitText(s.text + " ", 1);
        ic.endBatchEdit();
        for (String w : s.text.split(" ")) learn(w);
        refreshTop();
    }

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
