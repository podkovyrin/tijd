// SPDX-FileCopyrightText: 2026 Andrei Podkovyrin
// SPDX-License-Identifier: MIT

#include <spoken_time/spoken_time.hpp>

#include <array>
#include <stdexcept>

namespace spoken_time {
namespace {
constexpr auto languages = std::to_array<LanguageInfo>({
    {Language::Afrikaans, "af", "Afrikaans"},
    {Language::Albanian, "sq", "Albanian"},
    {Language::Amharic, "am", "Amharic"},
    {Language::Arabic, "ar", "Arabic"},
    {Language::Armenian, "hy", "Armenian"},
    {Language::Azerbaijani, "az", "Azerbaijani"},
    {Language::Basque, "eu", "Basque"},
    {Language::Bengali, "bn", "Bengali"},
    {Language::BrazilianPortuguese, "pt-BR", "Brazilian Portuguese"},
    {Language::Breton, "br", "Breton"},
    {Language::Bulgarian, "bg", "Bulgarian"},
    {Language::Burmese, "my", "Burmese"},
    {Language::Cantonese, "yue", "Cantonese"},
    {Language::Catalan, "ca", "Catalan"},
    {Language::Cebuano, "ceb", "Cebuano"},
    {Language::Czech, "cs", "Czech"},
    {Language::Danish, "da", "Danish"},
    {Language::Dutch, "nl", "Dutch"},
    {Language::EgyptianArabic, "arz", "Egyptian Arabic"},
    {Language::English, "en", "English"},
    {Language::Estonian, "et", "Estonian"},
    {Language::EuropeanPortuguese, "pt-PT", "European Portuguese"},
    {Language::Filipino, "fil", "Filipino"},
    {Language::Finnish, "fi", "Finnish"},
    {Language::French, "fr", "French"},
    {Language::Georgian, "ka", "Georgian"},
    {Language::German, "de", "German"},
    {Language::Greek, "el", "Greek"},
    {Language::Gujarati, "gu", "Gujarati"},
    {Language::Hausa, "ha", "Hausa"},
    {Language::Hebrew, "he", "Hebrew"},
    {Language::Hindi, "hi", "Hindi"},
    {Language::Hungarian, "hu", "Hungarian"},
    {Language::Icelandic, "is", "Icelandic"},
    {Language::Igbo, "ig", "Igbo"},
    {Language::Indonesian, "id", "Indonesian"},
    {Language::Irish, "ga", "Irish"},
    {Language::Italian, "it", "Italian"},
    {Language::Japanese, "ja", "Japanese"},
    {Language::Javanese, "jv", "Javanese"},
    {Language::Kannada, "kn", "Kannada"},
    {Language::Kazakh, "kk", "Kazakh"},
    {Language::Khmer, "km", "Khmer"},
    {Language::Korean, "ko", "Korean"},
    {Language::Kurmanji, "ku", "Kurmanji Kurdish"},
    {Language::Lao, "lo", "Lao"},
    {Language::Latvian, "lv", "Latvian"},
    {Language::LevantineArabic, "apc", "Levantine Arabic"},
    {Language::Lithuanian, "lt", "Lithuanian"},
    {Language::Malay, "ms", "Malay"},
    {Language::Malayalam, "ml", "Malayalam"},
    {Language::Maltese, "mt", "Maltese"},
    {Language::Mandarin, "zh-Hans", "Mandarin Chinese (Simplified)"},
    {Language::MandarinTraditional, "zh-Hant", "Mandarin Chinese (Traditional)"},
    {Language::Marathi, "mr", "Marathi"},
    {Language::Nepali, "ne", "Nepali"},
    {Language::Norwegian, "nb", "Norwegian"},
    {Language::Odia, "or", "Odia"},
    {Language::Oromo, "om", "Oromo"},
    {Language::Pashto, "ps", "Pashto"},
    {Language::Persian, "fa", "Persian"},
    {Language::Polish, "pl", "Polish"},
    {Language::Portuguese, "pt", "Portuguese"},
    {Language::Punjabi, "pa-Guru", "Punjabi (Gurmukhi)"},
    {Language::PunjabiShahmukhi, "pa-Arab", "Punjabi (Shahmukhi)"},
    {Language::Romanian, "ro", "Romanian"},
    {Language::Russian, "ru", "Russian"},
    {Language::ScottishGaelic, "gd", "Scottish Gaelic"},
    {Language::SerboCroatian, "sh", "Serbo Croatian"},
    {Language::Sindhi, "sd", "Sindhi"},
    {Language::Sinhala, "si", "Sinhala"},
    {Language::Slovak, "sk", "Slovak"},
    {Language::Slovenian, "sl", "Slovenian"},
    {Language::Somali, "so", "Somali"},
    {Language::Sorani, "ckb", "Sorani Kurdish"},
    {Language::Spanish, "es", "Spanish"},
    {Language::Sundanese, "su", "Sundanese"},
    {Language::Swahili, "sw", "Swahili"},
    {Language::Swedish, "sv", "Swedish"},
    {Language::Tamil, "ta", "Tamil"},
    {Language::Telugu, "te", "Telugu"},
    {Language::Thai, "th", "Thai"},
    {Language::Turkish, "tr", "Turkish"},
    {Language::Ukrainian, "uk", "Ukrainian"},
    {Language::Urdu, "ur", "Urdu"},
    {Language::Uzbek, "uz", "Uzbek"},
    {Language::Vietnamese, "vi", "Vietnamese"},
    {Language::Welsh, "cy", "Welsh"},
    {Language::Yiddish, "yi", "Yiddish"},
    {Language::Yoruba, "yo", "Yoruba"},
    {Language::Zulu, "zu", "Zulu"},
});
} // namespace

std::span<const LanguageInfo> supported_languages() noexcept { return languages; }

std::string format(Language language, int hour_of_day, int minute) {
  switch (language) {
  case Language::Afrikaans:
    return format_afrikaans(hour_of_day, minute);
  case Language::Albanian:
    return format_albanian(hour_of_day, minute);
  case Language::Amharic:
    return format_amharic(hour_of_day, minute);
  case Language::Arabic:
    return format_arabic(hour_of_day, minute);
  case Language::Armenian:
    return format_armenian(hour_of_day, minute);
  case Language::Azerbaijani:
    return format_azerbaijani(hour_of_day, minute);
  case Language::Basque:
    return format_basque(hour_of_day, minute);
  case Language::Bengali:
    return format_bengali(hour_of_day, minute);
  case Language::BrazilianPortuguese:
    return format_brazilian_portuguese(hour_of_day, minute);
  case Language::Breton:
    return format_breton(hour_of_day, minute);
  case Language::Bulgarian:
    return format_bulgarian(hour_of_day, minute);
  case Language::Burmese:
    return format_burmese(hour_of_day, minute);
  case Language::Cantonese:
    return format_cantonese(hour_of_day, minute);
  case Language::Catalan:
    return format_catalan(hour_of_day, minute);
  case Language::Cebuano:
    return format_cebuano(hour_of_day, minute);
  case Language::Czech:
    return format_czech(hour_of_day, minute);
  case Language::Danish:
    return format_danish(hour_of_day, minute);
  case Language::Dutch:
    return format_dutch(hour_of_day, minute);
  case Language::EgyptianArabic:
    return format_egyptian_arabic(hour_of_day, minute);
  case Language::English:
    return format_english(hour_of_day, minute);
  case Language::Estonian:
    return format_estonian(hour_of_day, minute);
  case Language::EuropeanPortuguese:
    return format_european_portuguese(hour_of_day, minute);
  case Language::Filipino:
    return format_filipino(hour_of_day, minute);
  case Language::Finnish:
    return format_finnish(hour_of_day, minute);
  case Language::French:
    return format_french(hour_of_day, minute);
  case Language::Georgian:
    return format_georgian(hour_of_day, minute);
  case Language::German:
    return format_german(hour_of_day, minute);
  case Language::Greek:
    return format_greek(hour_of_day, minute);
  case Language::Gujarati:
    return format_gujarati(hour_of_day, minute);
  case Language::Hausa:
    return format_hausa(hour_of_day, minute);
  case Language::Hebrew:
    return format_hebrew(hour_of_day, minute);
  case Language::Hindi:
    return format_hindi(hour_of_day, minute);
  case Language::Hungarian:
    return format_hungarian(hour_of_day, minute);
  case Language::Icelandic:
    return format_icelandic(hour_of_day, minute);
  case Language::Igbo:
    return format_igbo(hour_of_day, minute);
  case Language::Indonesian:
    return format_indonesian(hour_of_day, minute);
  case Language::Irish:
    return format_irish(hour_of_day, minute);
  case Language::Italian:
    return format_italian(hour_of_day, minute);
  case Language::Japanese:
    return format_japanese(hour_of_day, minute);
  case Language::Javanese:
    return format_javanese(hour_of_day, minute);
  case Language::Kannada:
    return format_kannada(hour_of_day, minute);
  case Language::Kazakh:
    return format_kazakh(hour_of_day, minute);
  case Language::Khmer:
    return format_khmer(hour_of_day, minute);
  case Language::Korean:
    return format_korean(hour_of_day, minute);
  case Language::Kurmanji:
    return format_kurmanji(hour_of_day, minute);
  case Language::Lao:
    return format_lao(hour_of_day, minute);
  case Language::Latvian:
    return format_latvian(hour_of_day, minute);
  case Language::LevantineArabic:
    return format_levantine_arabic(hour_of_day, minute);
  case Language::Lithuanian:
    return format_lithuanian(hour_of_day, minute);
  case Language::Malay:
    return format_malay(hour_of_day, minute);
  case Language::Malayalam:
    return format_malayalam(hour_of_day, minute);
  case Language::Maltese:
    return format_maltese(hour_of_day, minute);
  case Language::Mandarin:
    return format_mandarin(hour_of_day, minute);
  case Language::MandarinTraditional:
    return format_mandarin_traditional(hour_of_day, minute);
  case Language::Marathi:
    return format_marathi(hour_of_day, minute);
  case Language::Nepali:
    return format_nepali(hour_of_day, minute);
  case Language::Norwegian:
    return format_norwegian(hour_of_day, minute);
  case Language::Odia:
    return format_odia(hour_of_day, minute);
  case Language::Oromo:
    return format_oromo(hour_of_day, minute);
  case Language::Pashto:
    return format_pashto(hour_of_day, minute);
  case Language::Persian:
    return format_persian(hour_of_day, minute);
  case Language::Polish:
    return format_polish(hour_of_day, minute);
  case Language::Portuguese:
    return format_portuguese(hour_of_day, minute);
  case Language::Punjabi:
    return format_punjabi(hour_of_day, minute);
  case Language::PunjabiShahmukhi:
    return format_punjabi_shahmukhi(hour_of_day, minute);
  case Language::Romanian:
    return format_romanian(hour_of_day, minute);
  case Language::Russian:
    return format_russian(hour_of_day, minute);
  case Language::ScottishGaelic:
    return format_scottish_gaelic(hour_of_day, minute);
  case Language::SerboCroatian:
    return format_serbo_croatian(hour_of_day, minute);
  case Language::Sindhi:
    return format_sindhi(hour_of_day, minute);
  case Language::Sinhala:
    return format_sinhala(hour_of_day, minute);
  case Language::Slovak:
    return format_slovak(hour_of_day, minute);
  case Language::Slovenian:
    return format_slovenian(hour_of_day, minute);
  case Language::Somali:
    return format_somali(hour_of_day, minute);
  case Language::Sorani:
    return format_sorani(hour_of_day, minute);
  case Language::Spanish:
    return format_spanish(hour_of_day, minute);
  case Language::Sundanese:
    return format_sundanese(hour_of_day, minute);
  case Language::Swahili:
    return format_swahili(hour_of_day, minute);
  case Language::Swedish:
    return format_swedish(hour_of_day, minute);
  case Language::Tamil:
    return format_tamil(hour_of_day, minute);
  case Language::Telugu:
    return format_telugu(hour_of_day, minute);
  case Language::Thai:
    return format_thai(hour_of_day, minute);
  case Language::Turkish:
    return format_turkish(hour_of_day, minute);
  case Language::Ukrainian:
    return format_ukrainian(hour_of_day, minute);
  case Language::Urdu:
    return format_urdu(hour_of_day, minute);
  case Language::Uzbek:
    return format_uzbek(hour_of_day, minute);
  case Language::Vietnamese:
    return format_vietnamese(hour_of_day, minute);
  case Language::Welsh:
    return format_welsh(hour_of_day, minute);
  case Language::Yiddish:
    return format_yiddish(hour_of_day, minute);
  case Language::Yoruba:
    return format_yoruba(hour_of_day, minute);
  case Language::Zulu:
    return format_zulu(hour_of_day, minute);
  }
  throw std::invalid_argument("unknown spoken-time language");
}
} // namespace spoken_time
