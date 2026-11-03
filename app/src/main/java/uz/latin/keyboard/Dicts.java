package uz.latin.keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Tayyor so'z ro'yxatlari. Qolgan tillar uchun klaviatura siz yozgan so'zlarni o'zi o'rganadi. */
final class Dicts {
    private Dicts() {}

    // O'zbek (xom yozuv: o' g' sh ch; tutuq belgisi = ’). Lotin va kirillga o'zgartiriladi.
    static final String UZ =
            "assalomu alaykum|va alaykum assalom|xayrli tong|xayrli kun|xayrli kech|xayrli tun|rahmat katta|katta rahmat|"
            + "yaxshimisiz|qalaysiz|nima gap|nima qilyapsiz|hammasi yaxshi|xush kelibsiz|ko'rishguncha|omad tilayman|"
            + "tug'ilgan kun muborak|bayram muborak|iltimos|kechirasiz|marhamat|salom|assalomu|assalom|alaykum|"
            + "va|bu|men|sen|siz|biz|ular|u|shu|o'sha|nima|kim|qayerda|qachon|nega|qanday|qancha|yoki|lekin|ammo|chunki|"
            + "agar|ham|hali|endi|juda|katta|kichik|yaxshi|yomon|ha|yo'q|bor|emas|kerak|mumkin|bilan|uchun|haqida|kabi|"
            + "bo'ldi|bo'lsin|bo'lmaydi|qildi|qilish|qilaman|ketdim|keldim|boraman|kelaman|aytdi|ayting|bering|oling|"
            + "ko'ring|qarang|turing|yozing|o'qing|ishlayman|o'ylayman|bilaman|bilmayman|tushundim|xohlayman|istayman|"
            + "kerakmi|mumkinmi|bormi|yo'qmi|qayerga|qayerdan|qachongacha|nechta|necha|nima uchun|shuning uchun|hozir|"
            + "bugun|ertaga|kecha|ertalab|kechqurun|kechasi|tushlik|ovqat|choy|non|suv|uy|ko'cha|shahar|qishloq|mamlakat|"
            + "o'zbekiston|toshkent|samarqand|buxoro|xiva|andijon|farg'ona|namangan|qarshi|nukus|dunyo|odam|inson|bola|"
            + "ota|ona|aka|uka|opa|singil|do'st|o'g'il|qiz|oila|ish|maktab|universitet|talaba|o'qituvchi|kitob|daftar|"
            + "qalam|telefon|kompyuter|internet|dastur|klaviatura|rasm|video|musiqa|qo'shiq|kino|o'yin|vaqt|kun|hafta|"
            + "oy|yil|soat|daqiqa|pul|narx|so'm|dollar|bozor|do'kon|mashina|yo'l|poyezd|samolyot|havo|yomg'ir|qor|issiq|"
            + "sovuq|rahmat|tashakkur|salomat bo'ling|xayr|ko'rishamiz|omad|baxt|sevgi|mehr|hayot|orzu|maqsad|"
            + "muvaffaqiyat|sog'lik|salomatlik|bayram|tabrik|tabriklayman|muborak|xursand|xafa|charchadim|ochman|"
            + "to'ydim|uxlayman|uyg'ondim|boraylik|keling|ketdik|qani|mana|mana shu|ana|shunday|shunaqa|xuddi|ayniqsa|"
            + "albatta|balki|ehtimol|doim|ba’zan|hech qachon|har doim|har kuni|birinchi|ikkinchi|uchinchi|bir|ikki|uch|"
            + "to'rt|besh|olti|yetti|sakkiz|to'qqiz|o'n|yuz|ming|million";

    static final String EN =
            "thank you|good morning|good night|how are you|see you|i love you|hello|hi|thanks|please|sorry|the|be|to|of|"
            + "and|a|in|that|have|i|it|for|not|on|with|he|as|you|do|at|this|but|his|by|from|they|we|say|her|she|or|an|"
            + "will|my|one|all|would|there|their|what|so|up|out|if|about|who|get|which|go|me|when|make|can|like|time|no|"
            + "just|him|know|take|people|into|year|your|good|some|could|them|see|other|than|then|now|look|only|come|its|"
            + "over|think|also|back|after|use|two|how|our|work|first|well|way|even|new|want|because|any|these|give|day|"
            + "most|us|today|tomorrow|yes|okay|where|why|home|great|really|very|here|always|never|again|much|many|little|"
            + "still|already|before|between|another|different|important|every|together|understand|something|everything|"
            + "nothing|someone|everyone|should|maybe|friend|love";

    static final String RU =
            "привет|здравствуйте|спасибо|пожалуйста|хорошо|как дела|доброе утро|добрый день|добрый вечер|спокойной ночи|"
            + "до свидания|извините|и|в|не|на|я|быть|он|с|что|а|по|это|она|этот|к|но|они|мы|как|из|у|который|то|за|свой|"
            + "от|же|ты|весь|год|мочь|для|вы|плохо|да|нет|сегодня|завтра|вчера|сейчас|потом|всегда|никогда|очень|можно|"
            + "нужно|хочу|буду|было|есть|где|когда|почему|работа|дом|друг|человек|время|жизнь|день|рука|слово|место|"
            + "город|страна|если|или|тоже|ещё|уже|только|даже|надо|давай|конечно|наверное|может|потому что";

    static final String TR =
            "merhaba|selam|teşekkürler|teşekkür ederim|lütfen|günaydın|iyi akşamlar|iyi geceler|görüşürüz|nasılsın|ve|"
            + "bir|bu|da|için|ben|sen|o|biz|siz|onlar|ne|nasıl|neden|nerede|evet|hayır|tamam|çok|güzel|iyi|kötü|bugün|"
            + "yarın|şimdi|sonra|her zaman|asla|belki|olur|gerek|istiyorum|geliyorum|gidiyorum|biliyorum|anlıyorum|"
            + "çünkü|ama|veya|eğer|ile|gibi|kadar|daha|en|bile|hâlâ|artık|zaten|hiç|değil|var|yok|arkadaş|aile|ev|iş|"
            + "okul|zaman|gün|hafta|yıl";

    static final String DE =
            "hallo|danke|bitte|guten morgen|guten tag|guten abend|gute nacht|auf wiedersehen|wie geht's|ja|nein|ich|du|"
            + "er|sie|es|wir|ihr|und|oder|aber|nicht|der|die|das|ein|eine|ist|sind|haben|sein|werden|heute|morgen|"
            + "gestern|jetzt|immer|nie|sehr|gut|schlecht|danke schön|entschuldigung";

    static final String FR =
            "bonjour|salut|merci|s'il vous plaît|bonsoir|bonne nuit|au revoir|comment ça va|oui|non|je|tu|il|elle|nous|"
            + "vous|ils|et|ou|mais|pas|le|la|les|un|une|est|sont|avoir|être|aujourd'hui|demain|hier|maintenant|toujours|"
            + "jamais|très|bien|mal|désolé|d'accord";

    static final String ES =
            "hola|gracias|por favor|buenos días|buenas tardes|buenas noches|adiós|hasta luego|cómo estás|sí|no|yo|tú|él|"
            + "ella|nosotros|ustedes|y|o|pero|el|la|los|las|un|una|es|son|tener|ser|hoy|mañana|ayer|ahora|siempre|nunca|"
            + "muy|bien|mal|perdón|de acuerdo";

    static String raw(String id) {
        switch (id) {
            case "uz":
            case "uzc":
                return UZ;
            case "en":
                return EN;
            case "ru":
                return RU;
            case "tr":
                return TR;
            case "de":
                return DE;
            case "fr":
                return FR;
            case "es":
                return ES;
            default:
                return "";
        }
    }

    /** o' -> ö, g' -> ğ, sh -> ş, ch -> ç (sizning yozuvingiz). */
    static String latinize(String s) {
        return s.replace("o'", "ö").replace("g'", "ğ").replace("sh", "ş").replace("ch", "ç");
    }

    /** Lotindan kirillga (taxminiy). */
    static String cyr(String s) {
        StringBuilder b = new StringBuilder();
        int i = 0;
        boolean start = true;
        while (i < s.length()) {
            char c = s.charAt(i);
            String two = i + 1 < s.length() ? s.substring(i, i + 2) : "";
            if (c == ' ') {
                b.append(' ');
                i++;
                start = true;
                continue;
            }
            if (s.startsWith("yo'", i)) { b.append("й"); i += 1; }
            else if (two.equals("sh")) { b.append("ш"); i += 2; }
            else if (two.equals("ch")) { b.append("ч"); i += 2; }
            else if (two.equals("o'")) { b.append("ў"); i += 2; }
            else if (two.equals("g'")) { b.append("ғ"); i += 2; }
            else if (two.equals("yo")) { b.append("ё"); i += 2; }
            else if (two.equals("yu")) { b.append("ю"); i += 2; }
            else if (two.equals("ya")) { b.append("я"); i += 2; }
            else if (two.equals("ye")) { b.append("е"); i += 2; }
            else {
                switch (c) {
                    case 'a': b.append('а'); break;
                    case 'b': b.append('б'); break;
                    case 'd': b.append('д'); break;
                    case 'e': b.append(start ? 'э' : 'е'); break;
                    case 'f': b.append('ф'); break;
                    case 'g': b.append('г'); break;
                    case 'h': b.append('ҳ'); break;
                    case 'i': b.append('и'); break;
                    case 'j': b.append('ж'); break;
                    case 'k': b.append('к'); break;
                    case 'l': b.append('л'); break;
                    case 'm': b.append('м'); break;
                    case 'n': b.append('н'); break;
                    case 'o': b.append('о'); break;
                    case 'p': b.append('п'); break;
                    case 'q': b.append('қ'); break;
                    case 'r': b.append('р'); break;
                    case 's': b.append('с'); break;
                    case 't': b.append('т'); break;
                    case 'u': b.append('у'); break;
                    case 'v': b.append('в'); break;
                    case 'x': b.append('х'); break;
                    case 'y': b.append('й'); break;
                    case 'z': b.append('з'); break;
                    case '’':
                    case '\'': b.append('ъ'); break;
                    default: b.append(c);
                }
                i++;
            }
            start = false;
        }
        return b.toString();
    }

    static List<String> load(String id) {
        List<String> out = new ArrayList<>();
        String r = raw(id);
        if (r.isEmpty()) return out;
        for (String e : r.split("\\|")) {
            e = e.trim().toLowerCase(Locale.ROOT);
            if (e.isEmpty()) continue;
            if (id.equals("uz")) e = latinize(e);
            else if (id.equals("uzc")) e = cyr(e);
            out.add(e);
        }
        return out;
    }
}
