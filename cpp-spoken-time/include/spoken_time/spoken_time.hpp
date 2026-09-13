// SPDX-FileCopyrightText: 2026 Andrei Podkovyrin
// SPDX-License-Identifier: MIT

#pragma once

#include <spoken_time/export.hpp>

#include <span>
#include <string>
#include <string_view>

namespace spoken_time {

enum class Language {
  Afrikaans,
  Albanian,
  Amharic,
  Arabic,
  Armenian,
  Azerbaijani,
  Basque,
  Bengali,
  BrazilianPortuguese,
  Breton,
  Bulgarian,
  Burmese,
  Cantonese,
  Catalan,
  Cebuano,
  Czech,
  Danish,
  Dutch,
  EgyptianArabic,
  English,
  Estonian,
  EuropeanPortuguese,
  Filipino,
  Finnish,
  French,
  Georgian,
  German,
  Greek,
  Gujarati,
  Hausa,
  Hebrew,
  Hindi,
  Hungarian,
  Icelandic,
  Igbo,
  Indonesian,
  Irish,
  Italian,
  Japanese,
  Javanese,
  Kannada,
  Kazakh,
  Khmer,
  Korean,
  Kurmanji,
  Lao,
  Latvian,
  LevantineArabic,
  Lithuanian,
  Malay,
  Malayalam,
  Maltese,
  Mandarin,
  MandarinTraditional,
  Marathi,
  Nepali,
  Norwegian,
  Odia,
  Oromo,
  Pashto,
  Persian,
  Polish,
  Portuguese,
  Punjabi,
  PunjabiShahmukhi,
  Romanian,
  Russian,
  ScottishGaelic,
  SerboCroatian,
  Sindhi,
  Sinhala,
  Slovak,
  Slovenian,
  Somali,
  Sorani,
  Spanish,
  Sundanese,
  Swahili,
  Swedish,
  Tamil,
  Telugu,
  Thai,
  Turkish,
  Ukrainian,
  Urdu,
  Uzbek,
  Vietnamese,
  Welsh,
  Yiddish,
  Yoruba,
  Zulu,
};

struct LanguageInfo {
  Language language;
  std::string_view code;
  std::string_view name;
};

SPOKEN_TIME_EXPORT std::span<const LanguageInfo> supported_languages() noexcept;

SPOKEN_TIME_EXPORT std::string format(Language language, int hour_of_day, int minute);

SPOKEN_TIME_EXPORT std::string format_afrikaans(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_albanian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_amharic(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_arabic(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_armenian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_azerbaijani(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_basque(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_bengali(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_brazilian_portuguese(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_breton(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_bulgarian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_burmese(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_cantonese(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_catalan(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_cebuano(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_czech(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_danish(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_dutch(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_egyptian_arabic(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_english(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_estonian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_european_portuguese(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_filipino(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_finnish(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_french(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_georgian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_german(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_greek(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_gujarati(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_hausa(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_hebrew(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_hindi(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_hungarian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_icelandic(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_igbo(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_indonesian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_irish(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_italian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_japanese(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_javanese(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_kannada(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_kazakh(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_khmer(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_korean(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_kurmanji(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_lao(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_latvian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_levantine_arabic(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_lithuanian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_malay(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_malayalam(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_maltese(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_mandarin(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_mandarin_traditional(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_marathi(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_nepali(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_norwegian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_odia(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_oromo(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_pashto(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_persian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_polish(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_portuguese(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_punjabi(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_punjabi_shahmukhi(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_romanian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_russian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_scottish_gaelic(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_serbo_croatian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_sindhi(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_sinhala(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_slovak(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_slovenian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_somali(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_sorani(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_spanish(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_sundanese(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_swahili(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_swedish(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_tamil(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_telugu(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_thai(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_turkish(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_ukrainian(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_urdu(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_uzbek(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_vietnamese(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_welsh(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_yiddish(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_yoruba(int hour_of_day, int minute);
SPOKEN_TIME_EXPORT std::string format_zulu(int hour_of_day, int minute);

} // namespace spoken_time
