package uz.latin.keyboard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        int p = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(p, p * 2, p, p);

        TextView info = new TextView(this);
        info.setTextSize(16);
        info.setText("Öz klaviatura\n\n"
                + "1) Birinchi tugma bilan klaviaturani yoqing.\n"
                + "2) Ikkinchi tugma bilan uni tanlang.\n"
                + "3) Pastdagi maydonda sinab ko‘ring.\n\n"
                + "Klaviatura yozganingizni eslab qoladi: so‘zni 2 marta, jumlani 3 marta yozsangiz, "
                + "boshini yozganingizda to‘liq chiqadi. Hammasi faqat shu telefonda saqlanadi.");
        l.addView(info);

        Button enable = new Button(this);
        enable.setText("1. Klaviaturani yoqish");
        enable.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        l.addView(enable);

        Button pick = new Button(this);
        pick.setText("2. Klaviaturani tanlash");
        pick.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showInputMethodPicker();
        });
        l.addView(pick);

        Button clear = new Button(this);
        clear.setText("Yodlanganlarni tozalash");
        clear.setOnClickListener(v -> {
            SharedPreferences sp = getSharedPreferences("kb", MODE_PRIVATE);
            SharedPreferences.Editor ed = sp.edit();
            for (String k : new ArrayList<>(sp.getAll().keySet())) {
                if (!k.equals("lang")) ed.remove(k);
            }
            ed.apply();
            LatinIME.clearGen++;
            Toast.makeText(this, "Tozalandi", Toast.LENGTH_SHORT).show();
        });
        l.addView(clear);

        EditText test = new EditText(this);
        test.setHint("Shu yerda yozib ko‘ring");
        l.addView(test);

        setContentView(l);
    }
}
