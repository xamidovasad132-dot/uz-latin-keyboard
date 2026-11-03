package uz.latin.keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Bitta til: nomi, klaviatura harflari va qo'shimcha belgilari. */
final class Lang {
    static final int UZ_LATIN = 0, UZ_CYR = 1, LATIN = 2, CYR = 3, OTHER = 4;
    static final List<Lang> ALL = new ArrayList<>();

    final String id, name, code, dict, r1, r2, r3, extras;
    final int kind;
    final Locale loc;

    private Lang(String id, String name, String code, int kind, String dict, String tag,
                 String r1, String r2, String r3, String extras) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.kind = kind;
        this.dict = dict;
        this.loc = Locale.forLanguageTag(tag);
        this.r1 = r1;
        this.r2 = r2;
        this.r3 = r3;
        this.extras = extras == null ? "" : extras;
    }

    private static final String Q1 = "qwertyuiop", Q2 = "asdfghjkl", Q3 = "zxcvbnm";
    private static final String Z1 = "qwertzuiop", Z3 = "yxcvbnm";
    private static final String A1 = "azertyuiop", A2 = "qsdfghjklm", A3 = "wxcvbn";
    private static final String RU1 = "йцукенгшщзхъ";
    private static final String RU2 = "фывапролджэ";
    private static final String RU3 = "ячсмитьбюё";

    private static void lat(String id, String name, String code, String dict, String tag, String extras) {
        ALL.add(new Lang(id, name, code, LATIN, dict, tag, Q1, Q2, Q3, extras));
    }

    private static void latz(String id, String name, String code, String dict, String tag, String extras) {
        ALL.add(new Lang(id, name, code, LATIN, dict, tag, Z1, Q2, Z3, extras));
    }

    private static void cyr(String id, String name, String code, String dict, String tag,
                            String r1, String r2, String r3, String extras) {
        ALL.add(new Lang(id, name, code, CYR, dict, tag, r1, r2, r3, extras));
    }

    private static void oth(String id, String name, String code, String tag,
                            String r1, String r2, String r3, String extras) {
        ALL.add(new Lang(id, name, code, OTHER, null, tag, r1, r2, r3, extras));
    }

    static {
        ALL.add(new Lang("uz", "Öz", "ÖZ", UZ_LATIN, "uz", "uz", null, null, null, null));
        ALL.add(new Lang("uzc", "Ўз", "ЎЗ", UZ_CYR, "uzc", "uz", null, null, null, null));
        cyr("ru", "Русский", "RU", "ru", "ru", RU1, RU2, RU3, "");
        lat("en", "English", "EN", "en", "en", "");
        lat("tr", "Türkçe", "TR", "tr", "tr", "çğıöşü");
        latz("de", "Deutsch", "DE", "de", "de", "äöüß");
        ALL.add(new Lang("fr", "Français", "FR", LATIN, "fr", "fr", A1, A2, A3,
                "éèêàùçâîôûëïœ"));
        lat("es", "Español", "ES", "es", "es", "áéíóúñü¿¡");
        lat("it", "Italiano", "IT", null, "it", "àèéìòù");
        lat("pt", "Português", "PT", null, "pt", "áâãàçéêíóôõú");
        lat("nl", "Nederlands", "NL", null, "nl", "éëïóöü");
        lat("pl", "Polski", "PL", null, "pl", "ąćęłńóśźż");
        latz("cs", "Čeština", "CS", null, "cs", "áčďéěíňóřšťúůýž");
        latz("sk", "Slovenčina", "SK", null, "sk", "áäčďéíľĺňóôŕšťúýž");
        latz("hu", "Magyar", "HU", null, "hu", "áéíóöőúüű");
        lat("ro", "Română", "RO", null, "ro", "ăâîșț");
        lat("sv", "Svenska", "SV", null, "sv", "åäö");
        lat("no", "Norsk", "NO", null, "nb", "åæø");
        lat("da", "Dansk", "DA", null, "da", "åæø");
        lat("fi", "Suomi", "FI", null, "fi", "äöå");
        lat("is", "Íslenska", "IS", null, "is", "áðéíóúýþæö");
        latz("hr", "Hrvatski", "HR", null, "hr", "čćđšž");
        latz("sl", "Slovenščina", "SL", null, "sl", "čšž");
        lat("lv", "Latviešu", "LV", null, "lv", "āčēģīķļņšūž");
        lat("lt", "Lietuvių", "LT", null, "lt", "ąčęėįšųūž");
        lat("et", "Eesti", "ET", null, "et", "äöõüšž");
        lat("sq", "Shqip", "SQ", null, "sq", "çë");
        lat("az", "Azərbaycan", "AZ", null, "az", "əçğıöşü");
        lat("tk", "Türkmen", "TK", null, "tk", "äçžňöşüý");
        lat("vi", "Tiếng Việt", "VI", null, "vi",
                "ăâđêôơưáàảãạéèẻẽẹíìỉĩị"
                        + "óòỏõọúùủũụýỳỷỹỵ");
        lat("id", "Indonesia", "ID", null, "id", "");
        lat("ms", "Melayu", "MS", null, "ms", "");
        lat("fil", "Filipino", "FIL", null, "fil", "ñ");
        lat("sw", "Kiswahili", "SW", null, "sw", "");
        lat("ca", "Català", "CA", null, "ca", "àçéèíïòóúü·");
        lat("eu", "Euskara", "EU", null, "eu", "ñç");
        lat("af", "Afrikaans", "AF", null, "af", "éèêëïôû");

        // Kirill yozuvidagi tillar
        cyr("uk", "Українська", "UK", null, "uk",
                "йцукенгшщзхї",
                "фівапролджє",
                "ячсмитьбюґ", "");
        cyr("be", "Беларуская", "BE", null, "be",
                "йцукенгшўзх",
                RU2, "ячсмітьбюё", "'");
        cyr("bg", "Български", "BG", null, "bg",
                "явертъуиопшщ",
                "асдфгхйклч",
                "зьцжбнмю", "");
        cyr("sr", "Српски", "SR", null, "sr",
                "љњертзуиопшђ",
                "асдфгхјклчћж",
                "џцвбнм", "");
        cyr("kk", "Қазақша", "KK", null, "kk", RU1, RU2, RU3,
                "әіңғүұқөһ");
        cyr("ky", "Кыргызча", "KY", null, "ky", RU1, RU2, RU3,
                "ңөү");
        cyr("tg", "Тоҷикӣ", "TG", null, "tg", RU1, RU2, RU3,
                "ғӣқӯҳҷ");
        cyr("mn", "Монгол", "MN", null, "mn", RU1, RU2, RU3,
                "өү");

        // Boshqa yozuvlar
        oth("ar", "العربية", "AR", "ar",
                "ضصثقفغعهخحجد",
                "شسيبلاتنمكط",
                "ئءؤرىةوزظذ", "");
        oth("fa", "فارسی", "FA", "fa",
                "ضصثقفغعهخحجچ",
                "شسیبلاتنمکگ",
                "ظطزرذدپو", "ژآئء");
        oth("he", "עברית", "HE", "he",
                "קראטוןםפ",
                "שדגכעיחלךף",
                "זסבהנמצתץ", "");
        oth("el", "Ελληνικά", "EL", "el",
                "ςερτυθιοπ",
                "ασδφγηξκλ",
                "ζχψωβνμ", "");
        oth("ka", "ქართული", "KA", "ka",
                "ქწერტყუიოპ",
                "ასდფგჰჯკლ",
                "ზხცვბნმ", "ჭღთშჟძჩ");
        oth("hy", "Հայերեն", "HY", "hy",
                "քոեռտըւիօպ",
                "ասդֆգհջկլ",
                "զղցվբնմ",
                "էթժխծձճյշչրփ");
    }

    static Lang byId(String id) {
        for (Lang l : ALL) if (l.id.equals(id)) return l;
        return ALL.get(0);
    }
}
