// SPDX-FileCopyrightText: 2026 Andrei Podkovyrin
// SPDX-License-Identifier: MIT

#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"
#include "numbers.hpp"

namespace spoken_time {
using namespace detail;

std::string format_estonian(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr std::array<std::string_view, 21> genitive = {"nulli",
                                                         "ühe",
                                                         "kahe",
                                                         "kolme",
                                                         "nelja",
                                                         "viie",
                                                         "kuue",
                                                         "seitsme",
                                                         "kaheksa",
                                                         "üheksa",
                                                         "kümne",
                                                         "üheteistkümne",
                                                         "kaheteistkümne",
                                                         "kolmeteistkümne",
                                                         "neljateistkümne",
                                                         "viieteistkümne",
                                                         "kuueteistkümne",
                                                         "seitsmeteistkümne",
                                                         "kaheksateistkümne",
                                                         "üheksateistkümne",
                                                         "kahekümne"};
  if (minute == 0)
    return "kell " + word(et, t.hour);
  if (minute == 15)
    return "veerand " + word(et, t.next);
  if (minute == 30)
    return "pool " + word(et, t.next);
  if (minute == 45)
    return "kolmveerand " + word(et, t.next);
  if (minute < 30)
    return word(et, minute) + (minute == 1 ? " minut üle " : " minutit üle ") +
           word(genitive, t.hour);
  const int n = 60 - minute;
  const auto count = n <= 20 ? word(genitive, n) : "kahekümne " + word(genitive, n - 20);
  return count + " minuti pärast " + word(et, t.next);
}

std::string format_latvian(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours dative = {"",          "vieniem",        "diviem",       "trijiem",  "četriem",
                            "pieciem",   "sešiem",         "septiņiem",    "astoņiem", "deviņiem",
                            "desmitiem", "vienpadsmitiem", "divpadsmitiem"};
  constexpr std::array<std::string_view, 10> feminine = {
      "", "viena", "divas", "trīs", "četras", "piecas", "sešas", "septiņas", "astoņas", "deviņas"};
  constexpr std::array<std::string_view, 10> feminine_dative = {
      "", "vienas", "divām", "trim", "četrām", "piecām", "sešām", "septiņām", "astoņām", "deviņām"};
  if (minute == 0)
    return "pulksten " + word(lv, t.hour);
  if (minute == 30)
    return "pus" + word(lv, t.next);
  const int n = minute < 30 ? minute : 60 - minute;
  const bool singular = n == 1 || n == 21;
  const auto count = [&](bool before) {
    const auto& units = before ? feminine_dative : feminine;
    if (n < 10)
      return word(units, n);
    if (n <= 20)
      return word(lv, n);
    return std::string("divdesmit ") + word(units, n - 20);
  };
  if (minute < 30)
    return count(false) + (singular ? " minūte pāri " : " minūtes pāri ") + word(dative, t.hour);
  return "bez " + count(true) + (singular ? " minūtes " : " minūtēm ") + word(lv, t.next);
}

std::string format_lithuanian(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours ordinal = {"",        "pirma",       "antra",    "trečia",  "ketvirta",
                             "penkta",  "šešta",       "septinta", "aštunta", "devinta",
                             "dešimta", "vienuolikta", "dvylikta"};
  constexpr Hours hour_genitive = {"",         "pirmos",      "dviejų",   "trijų",    "keturių",
                                   "penkių",   "šešių",       "septynių", "aštuonių", "devynių",
                                   "dešimtos", "vienuolikos", "dvylikos"};
  constexpr std::array<std::string_view, 21> minute_genitive = {"",
                                                                "vienos",
                                                                "dviejų",
                                                                "trijų",
                                                                "keturių",
                                                                "penkių",
                                                                "šešių",
                                                                "septynių",
                                                                "aštuonių",
                                                                "devynių",
                                                                "dešimties",
                                                                "vienuolikos",
                                                                "dvylikos",
                                                                "trylikos",
                                                                "keturiolikos",
                                                                "penkiolikos",
                                                                "šešiolikos",
                                                                "septyniolikos",
                                                                "aštuoniolikos",
                                                                "devyniolikos",
                                                                "dvidešimties"};
  if (minute == 0)
    return word(ordinal, t.hour) + " valanda";
  if (minute == 15)
    return "ketvirtis po " + word(hour_genitive, t.hour);
  if (minute == 30)
    return "pusė " + word(hour_genitive, t.next);
  const auto next = t.next == 1 ? std::string("pirma") : word(lt, t.next);
  if (minute == 45)
    return "be ketvirčio " + next;
  if (minute < 30)
    return word(lt, minute) + " po " + word(hour_genitive, t.hour);
  const int n = 60 - minute;
  const auto count =
      n <= 20 ? word(minute_genitive, n) : "dvidešimt " + word(minute_genitive, n - 20);
  return "be " + count + " " + next;
}

std::string format_hungarian(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = [](int h) { return h == 2 ? std::string("két") : word(hu, h); };
  if (minute == 0)
    return hour(t.hour) + " óra";
  const int q = minute / 15;
  const int remainder = minute % 15;
  const auto anchor = q == 0 ? hour(t.hour) + " óra"
                             : std::string(q == 1   ? "negyed "
                                           : q == 2 ? "fél "
                                                    : "háromnegyed ") +
                                   word(hu, t.next);
  if (remainder == 0)
    return anchor;
  if (remainder <= 7) {
    const auto count = remainder == 2 ? std::string("két") : word(hu, remainder);
    return anchor + " múlt " + count + " perccel";
  }
  const int remaining = 15 - remainder;
  const auto count = remaining == 2 ? std::string("két") : word(hu, remaining);
  const auto target = q == 3 ? hour(t.next) + " óra"
                             : std::string(q == 0   ? "negyed "
                                           : q == 1 ? "fél "
                                                    : "háromnegyed ") +
                                   word(hu, t.next);
  return count + " perc múlva " + target;
}

std::string format_turkish(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours accusative = {"",       "biri",   "ikiyi",  "üçü", "dördü",   "beşi",    "altıyı",
                                "yediyi", "sekizi", "dokuzu", "onu", "on biri", "on ikiyi"};
  constexpr Hours dative = {"",       "bire",   "ikiye",  "üçe", "dörde",   "beşe",    "altıya",
                            "yediye", "sekize", "dokuza", "ona", "on bire", "on ikiye"};
  if (minute == 0)
    return word(tr, t.hour);
  if (minute == 30)
    return word(tr, t.hour) + " buçuk";
  const auto part = minute == 15 || minute == 45 ? std::string("çeyrek")
                                                 : word(tr, minute < 30 ? minute : 60 - minute);
  if (minute < 30)
    return word(accusative, t.hour) + " " + part + " geçiyor";
  return word(dative, t.next) + " " + part + " var";
}

std::string format_armenian(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours definite = {"",     "մեկը", "երկուսը", "երեքը", "չորսը",    "հինգը",      "վեցը",
                              "յոթը", "ութը", "ինը",     "տասը",  "տասնմեկը", "տասներկուսը"};
  constexpr Hours before_vowel = {"",      "մեկն",     "երկուսն",    "երեքն", "չորսն",
                                  "հինգն", "վեցն",     "յոթն",       "ութն",  "ինն",
                                  "տասն",  "տասնմեկն", "տասներկուսն"};
  constexpr Hours dative = {"",       "մեկին",     "երկուսին",    "երեքին", "չորսին",
                            "հինգին", "վեցին",     "յոթին",       "ութին",  "իննին",
                            "տասին",  "տասնմեկին", "տասներկուսին"};
  if (minute == 0)
    return word(definite, t.hour);
  if (minute == 30)
    return word(before_vowel, t.hour) + " անց կես";
  const auto part = minute == 15 || minute == 45 ? std::string("քառորդ")
                                                 : word(hy, minute < 30 ? minute : 60 - minute);
  if (minute < 30)
    return word(before_vowel, t.hour) + " անց " + part;
  return word(dative, t.next) + " " + part + " պակաս";
}

std::string format_georgian(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours genitive = {"",      "პირველის",  "ორის",    "სამის", "ოთხის",
                              "ხუთის", "ექვსის",    "შვიდის",  "რვის",  "ცხრის",
                              "ათის",  "თერთმეტის", "თორმეტის"};
  constexpr Hours dative = {"",      "პირველს", "ორს",   "სამს", "ოთხს",     "ხუთს",   "ექვსს",
                            "შვიდს", "რვას",    "ცხრას", "ათს",  "თერთმეტს", "თორმეტს"};
  if (minute == 0)
    return word(ka, t.hour) + " საათი";
  if (minute == 30)
    return word(genitive, t.next) + " ნახევარი";
  if (minute < 30)
    return word(genitive, t.next) + " " + word(ka, minute) + " წუთი";
  return word(dative, t.next) + " აკლია " + word(ka, 60 - minute) + " წუთი";
}
} // namespace spoken_time
