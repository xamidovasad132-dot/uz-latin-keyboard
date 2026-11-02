package uz.latin.keyboard;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

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

    // Lotin: ş = ş, ç = ç, ö = ö, ğ = ğ, ’ = ’
    private static final String[][] LETTERS = {
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"},
            {"q", "w", "e", "r", "t", "y", "u", "i", "o", "p"},
            {"a", "s", "d", "f", "g", "h", "j", "k", "l"},
            {"’", "z", "x", "c", "v", "b", "n", "m", "-"},
            {SHIFT, "!", "ş", "ç", "ö", "ğ", "?", DEL},
            {P_A, NUM, ",", SPACE, ".", ENTER}
    };

    // Kirill
    private static final String[][] CYRILLIC = {
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"},
            {"й", "ц", "у", "к", "е", "н", "г", "ш", "ў", "з"},
            {"ф", "қ", "в", "а", "п", "р", "о", "л", "д", "ж"},
            {"я", "ч", "с", "м", "и", "т", "ь", "б", "ю", "х"},
            {SHIFT, "ё", "ъ", "э", "ғ", "ҳ", "ы", "щ", DEL},
            {P_A, NUM, ",", SPACE, ".", ENTER}
    };

    private static final String[][] PAGE_A = {
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"},
            {"%", "=", "×", "+", "•", "~", "✓", "«", "»", "♡"},
            {"@", "#", "$", "&", "_", "-", "—", "(", ")", "/"},
            {P_B, "*", "\"", "'", ":", ";", "!", "?", DEL},
            {ABC, NUM, ",", SPACE, ".", ENTER}
    };

    private static final String[][] PAGE_B = {
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"},
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
    private boolean caps = false;
    private boolean cyr = false;
    private int page = LETTERS_PAGE;

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
        page = LETTERS_PAGE;
        caps = false;
        if (root != null) build();
    }

    private void build() {
        root.removeAllViews();
        root.addView(toolbar());
        if (page == NUMPAD) {
            buildNumpad();
            return;
        }
        String[][] rows;
        if (page == SYM_A) rows = PAGE_A;
        else if (page == SYM_B) rows = PAGE_B;
        else if (page == EMOJI_PAGE) rows = emojiRows();
        else rows = cyr ? CYRILLIC : LETTERS;
        for (String[] r : rows) root.addView(row(r));
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

    // ---- tepadagi qator: O'z | Ўз | globus ....... emoji | sozlama | yopish ----

    private View toolbar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(42)));

        bar.addView(tab("O‘z", !cyr, v -> {
            cyr = false;
            page = LETTERS_PAGE;
            caps = false;
            build();
        }));
        bar.addView(tab("Ўз", cyr, v -> {
            cyr = true;
            page = LETTERS_PAGE;
            caps = false;
            build();
        }));
        bar.addView(tool("🌐", v -> showPicker()));

        View sp = new View(this);
        sp.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        bar.addView(sp);

        bar.addView(tool("😊", v -> goTo(EMOJI_PAGE)));
        bar.addView(tool("⚙", v -> openSettings()));
        bar.addView(tool("▽", v -> requestHideSelf(0)));
        return bar;
    }

    private TextView tab(String text, boolean selected, View.OnClickListener l) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setGravity(Gravity.CENTER);
        t.setTextSize(19);
        t.setTextColor(selected ? 0xFFFFFFFF : 0xFF7C7C80);
        t.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        t.setPadding(dp(14), 0, dp(14), 0);
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
        t.setPadding(dp(14), 0, dp(14), 0);
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

    // ---- raqamlar paneli ----

    private void buildNumpad() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.HORIZONTAL);
        wrap.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.7f));
        for (String k : NUM_LEFT) {
            View v = key(k);
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
        boolean pad = keys.length == 9 && !hasSpecial(keys);
        if (pad) row.addView(spacer());
        for (String k : keys) {
            View v = key(k);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(48), weight(k));
            lp.setMargins(dp(2), dp(3), dp(2), dp(3));
            v.setLayoutParams(lp);
            row.addView(v);
        }
        if (pad) row.addView(spacer());
        return row;
    }

    private View spacer() {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 0.5f));
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
        return k.equals("ş") || k.equals("ç") || k.equals("ö") || k.equals("ğ");
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
        return k.toUpperCase(Locale.ENGLISH);
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
                return cyr ? "АБВ" : "ABC";
            case SPACE:
                return cyr ? "Ўз" : "O‘z";
            case ENTER:
                return "↵";
            case TAB:
                return "⇥";
            default:
                return (caps && page == LETTERS_PAGE) ? upper(k) : k;
        }
    }

    private View key(final String k) {
        TextView t = new TextView(this);
        t.setGravity(Gravity.CENTER);
        t.setTextColor(0xFFFFFFFF);
        boolean special = isSpecial(k);
        t.setTextSize(special ? 16 : (page == EMOJI_PAGE ? 24 : 20));
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
                commit(" ");
                return;
            case ENTER:
                enter();
                return;
            case TAB:
                commit("\t");
                return;
            default:
                boolean wasCaps = caps && page == LETTERS_PAGE;
                commit(wasCaps ? upper(k) : k);
                if (wasCaps) {
                    caps = false;
                    build();
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

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
