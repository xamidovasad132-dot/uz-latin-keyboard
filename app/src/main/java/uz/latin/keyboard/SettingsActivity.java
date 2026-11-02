package uz.latin.keyboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        info.setText("O‘zbek lotin klaviaturasi (ş ç ö ğ)\n\n"
                + "1) Pastdagi birinchi tugmani bosib, klaviaturani yoqing.\n"
                + "2) Ikkinchi tugma orqali uni tanlang.\n"
                + "3) Quyidagi maydonda sinab ko‘ring.");
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

        EditText test = new EditText(this);
        test.setHint("Shu yerda yozib ko‘ring");
        l.addView(test);

        setContentView(l);
    }
}
