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

    private static final int[] EMOJI = {
            0x1F600, 0x1F602, 0x1F60D, 0x1F60A, 0x1F618, 0x1F609, 0x1F604, 0x1F606,
            0x1F60E, 0x1F622, 0x1F62D, 0x1F621, 0x1F914, 0x1F62E, 0x1F634, 0x1F60B,
            0x1F44D, 0x1F44F, 0x1F64F, 0x1F4AA, 0x1F44B, 0x1F44C, 0x1F64C, 0x1F91D,
            0x1F525, 0x1F389, 0x1F381, 0x1F339, 0x1F31F, 0x1F495, 0x1F4AF, 0x1F680
    };

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
    private int learnedSinceSave = 0;
    private final Map<String, List<String>> dicts = new HashMap<>();
    private final Map<String, Map<String, Integer>> learned = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences("kb", MODE_PRIVATE);
        lang = Lang.byId(prefs.getString("lang", "uz"));
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
        saveLearned();
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
        if (page == EMOJI_PAGE) return emojiRows();
        if (lang.kind == Lang.UZ_LATIN) return LETTERS;
        if (lang.kind == Lang.UZ_CYR) return CYRILLIC;
        String[] c3 = chars(lang.r3);
        String[] row3 = new String[c3.length + 2];
        row3[0] = SHIFT;
        System.arraycopy(c3, 0, row3, 1, c3.length);
        row3[row3.length - 1] = DEL;
        return new String[][]{NUMBERS, chars(lang.r1), chars(lang.r2), row3, BOTTOM};
    }

    private String[][] emojiRows() {
        String[][] r = new String[5][];
        for (int i = 0; i < 4; i++) {
            r[i] = new String[8];
            for (int j = 0; j < 8; j++) {
                r[i][j] = new String(Character.toChars(EMOJI[i * 8 + j]));
            }
        }
        r[4] = new String[]{ABC, SPACE, DEL};
        return r;
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
    // Tepadagi qator: til tugmalari yoki so'z takliflari
    // ------------------------------------------------------------------

    private void refreshTop() {
        if (topHolder == null) return;
        topHolder.removeAllViews();
        List<String> sg = computeSuggestions();
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

    private View suggestionBar(List<String> sg) {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(42)));
        for (int i = 0; i < 3; i++) {
            final String s = i < sg.size() ? sg.get(i) : "";
            TextView t = new TextView(this);
            t.setGravity(Gravity.CENTER);
            t.setSingleLine(true);
            t.setEllipsize(TextUtils.TruncateAt.END);
            t.setTextSize(17);
            t.setTextColor(0xFFFFFFFF);
            t.setTypeface(null, i == 0 ? Typeface.BOLD : Typeface.NORMAL);
            t.setPadding(dp(6), 0, dp(6), 0);
            t.setText(s);
            t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            if (!s.isEmpty()) {
                t.setClickable(true);
                t.setOnClickListener(v -> applySuggestion(s));
            }
            bar.addView(t);
            if (i < 2) {
                View d = new View(this);
                d.setBackgroundColor(0xFF3A3A3C);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(1), dp(24));
                bar.addView(d, lp);
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
                enter();
                refreshTop();
                return;
            case TAB:
                commit("\t");
                return;
            default:
                boolean wasCaps = caps && page == LETTERS_PAGE;
                if (isSeparator(k)) learnCurrent();
                commit(wasCaps ? upper(k) : k);
                if (wasCaps) {
                    caps = false;
                    build();
                } else {
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
        refreshTop();
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

    private String beforeText() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return "";
        CharSequence cs = ic.getTextBeforeCursor(40, 0);
        return cs == null ? "" : cs.toString();
    }

    private String curWord() {
        String b = beforeText();
        int i = b.length();
        while (i > 0 && isWordChar(b.charAt(i - 1))) i--;
        return b.substring(i);
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

    private List<String> computeSuggestions() {
        List<String> out = new ArrayList<>();
        if (!suggestOn || page != LETTERS_PAGE) return out;
        String b = beforeText();
        int i = b.length();
        while (i > 0 && isWordChar(b.charAt(i - 1))) i--;
        String cur = b.substring(i);
        List<String> dict = dict(lang);

        if (cur.isEmpty()) {
            if (!b.endsWith(" ")) return out;
            int j = b.length();
            while (j > 0 && b.charAt(j - 1) == ' ') j--;
            int k = j;
            while (k > 0 && isWordChar(b.charAt(k - 1))) k--;
            if (k >= j) return out;
            String key = b.substring(k, j).toLowerCase(lang.loc) + " ";
            for (String p : dict) {
                if (p.startsWith(key) && p.length() > key.length()) {
                    String rest = p.substring(key.length());
                    if (!out.contains(rest)) out.add(rest);
                    if (out.size() >= 3) break;
                }
            }
            return out;
        }

        String low = cur.toLowerCase(lang.loc);
        boolean cap = Character.isUpperCase(cur.codePointAt(0));
        List<Map.Entry<String, Integer>> es = new ArrayList<>();
        for (Map.Entry<String, Integer> e : learnedMap(lang).entrySet()) {
            if (e.getKey().startsWith(low) && !e.getKey().equals(low)) es.add(e);
        }
        Collections.sort(es, (x, y) -> y.getValue() - x.getValue());

        List<String> res = new ArrayList<>();
        for (Map.Entry<String, Integer> e : es) {
            if (e.getValue() >= 3 && res.size() < 3) res.add(e.getKey());
        }
        for (String p : dict) {
            if (res.size() >= 3) break;
            if (p.startsWith(low) && !p.equals(low) && !res.contains(p)) res.add(p);
        }
        for (Map.Entry<String, Integer> e : es) {
            if (res.size() >= 3) break;
            if (!res.contains(e.getKey())) res.add(e.getKey());
        }
        for (String r : res) out.add(cap ? capFirst(r) : r);
        return out;
    }

    private void applySuggestion(String s) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        int n = curWord().length();
        ic.beginBatchEdit();
        if (n > 0) ic.deleteSurroundingText(n, 0);
        ic.commitText(s + " ", 1);
        ic.endBatchEdit();
        for (String w : s.split(" ")) learn(w);
        refreshTop();
    }

    private Map<String, Integer> learnedMap(Lang l) {
        Map<String, Integer> m = learned.get(l.id);
        if (m == null) {
            m = new HashMap<>();
            String s = prefs.getString("w_" + l.id, "");
            for (String line : s.split("\n")) {
                int t = line.indexOf('\t');
                if (t > 0) {
                    try {
                        m.put(line.substring(0, t), Integer.parseInt(line.substring(t + 1).trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            learned.put(l.id, m);
        }
        return m;
    }

    private void learnCurrent() {
        if (!suggestOn) return;
        String w = curWord();
        if (!w.isEmpty()) learn(w);
    }

    private void learn(String w) {
        if (!suggestOn) return;
        w = w.toLowerCase(lang.loc);
        while (w.length() > 0 && (w.charAt(0) == '\'' || w.charAt(0) == '’')) w = w.substring(1);
        while (w.length() > 0 && (w.charAt(w.length() - 1) == '\'' || w.charAt(w.length() - 1) == '’')) {
            w = w.substring(0, w.length() - 1);
        }
        if (w.length() < 2 || w.length() > 24) return;
        Map<String, Integer> m = learnedMap(lang);
        Integer c = m.get(w);
        m.put(w, c == null ? 1 : c + 1);
        if (m.size() > 3000) {
            List<String> drop = new ArrayList<>();
            for (Map.Entry<String, Integer> e : m.entrySet()) if (e.getValue() <= 1) drop.add(e.getKey());
            for (String d : drop) m.remove(d);
        }
        learnedSinceSave++;
        if (learnedSinceSave >= 8) saveLearned();
    }

    private void saveLearned() {
        if (prefs == null || learned.isEmpty()) return;
        SharedPreferences.Editor ed = prefs.edit();
        for (Map.Entry<String, Map<String, Integer>> e : learned.entrySet()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> w : e.getValue().entrySet()) {
                sb.append(w.getKey()).append('\t').append(w.getValue()).append('\n');
            }
            ed.putString("w_" + e.getKey(), sb.toString());
        }
        ed.apply();
        learnedSinceSave = 0;
    }

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
