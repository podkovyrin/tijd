// SPDX-FileCopyrightText: 2026 Andrei Podkovyrin
// SPDX-License-Identifier: MIT

#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"
#include "numbers.hpp"

namespace spoken_time {
using namespace detail;

std::string format_french(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto minutes = [](int n) {
    if (n == 1)
      return std::string("une minute");
    if (n == 21)
      return std::string("vingt et une minutes");
    return word(fr, n);
  };
  const auto hour = [](int h) {
    return h == 1 ? std::string("une heure") : word(fr, h) + " heures";
  };
  if (minute == 0)
    return hour(t.hour);
  if (minute == 15)
    return hour(t.hour) + " et quart";
  if (minute == 30)
    return hour(t.hour) + " et demie";
  if (minute == 45)
    return hour(t.next) + " moins le quart";
  if (minute < 30)
    return hour(t.hour) + " " + minutes(minute);
  return hour(t.next) + " moins " + minutes(60 - minute);
}

std::string format_italian(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = [](int h) { return h == 1 ? std::string("l'una") : "le " + word(it, h); };
  if (minute == 0)
    return hour(t.hour);
  if (minute == 30)
    return hour(t.hour) + " e mezza";
  const auto part = minute == 15 || minute == 45 ? std::string("un quarto")
                                                 : word(it, minute < 30 ? minute : 60 - minute);
  return hour(minute < 30 ? t.hour : t.next) + (minute < 30 ? " e " : " meno ") + part;
}

std::string format_spanish(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = [](int h) { return h == 1 ? std::string("la una") : "las " + word(es, h); };
  if (minute == 0)
    return hour(t.hour);
  if (minute == 30)
    return hour(t.hour) + " y media";
  const auto part = minute == 15 || minute == 45 ? std::string("cuarto")
                                                 : word(es, minute < 30 ? minute : 60 - minute);
  return hour(minute < 30 ? t.hour : t.next) + (minute < 30 ? " y " : " menos ") + part;
}

std::string format_portuguese(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = [](int h) {
    if (h == 1)
      return std::string("uma");
    if (h == 2)
      return std::string("duas");
    return word(pt, h);
  };
  if (minute == 0)
    return hour(t.hour) + (t.hour == 1 ? " hora" : " horas");
  if (minute == 30)
    return hour(t.hour) + " e meia";
  const auto part = minute == 15 || minute == 45 ? std::string("um quarto")
                                                 : word(pt, minute < 30 ? minute : 60 - minute);
  return hour(minute < 30 ? t.hour : t.next) + (minute < 30 ? " e " : " menos ") + part;
}

std::string format_romanian(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return word(ro, t.hour);
  if (minute == 30)
    return word(ro, t.hour) + " și jumătate";
  if (minute == 1)
    return word(ro, t.hour) + " și un minut";
  if (minute == 59)
    return word(ro, t.next) + " fără un minut";
  const auto part = minute == 15 || minute == 45 ? std::string("un sfert")
                                                 : word(ro, minute < 30 ? minute : 60 - minute);
  return word(ro, minute < 30 ? t.hour : t.next) + (minute < 30 ? " și " : " fără ") + part;
}

std::string format_catalan(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = [](int h) {
    return h == 1 ? std::string("una") : h == 2 ? std::string("dues") : word(ca, h);
  };
  const auto current = (t.hour == 1 ? "la " : "les ") + hour(t.hour);
  if (minute == 0)
    return current;
  if (minute < 15)
    return current + " i " + word(ca, minute);
  const int quarters = minute / 15;
  std::string result = quarters == 1 ? "un quart" : quarters == 2 ? "dos quarts" : "tres quarts";
  if (minute % 15 != 0)
    result += " i " + word(ca, minute % 15);
  // Elision belongs to the following hour, including after an inserted minute.
  return result + (t.next == 1 || t.next == 11 ? " d'" : " de ") + hour(t.next);
}

std::string format_basque(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours hours = {"",         "ordu bata", "ordu biak", "hirurak",  "laurak",
                           "bostak",   "seiak",     "zazpiak",   "zortziak", "bederatziak",
                           "hamarrak", "hamaikak",  "hamabiak"};
  if (minute == 0)
    return word(hours, t.hour);
  if (minute == 30) {
    const auto base = t.hour == 1   ? std::string("ordu bat")
                      : t.hour == 2 ? std::string("ordu bi")
                                    : word(eu, t.hour);
    return base + " eta erdiak";
  }
  const auto part = minute == 15 || minute == 45 ? std::string("laurden")
                                                 : word(eu, minute < 30 ? minute : 60 - minute);
  if (minute < 30)
    return word(hours, t.hour) + " eta " + part;
  return word(hours, t.next) + " " + part + " gutxi";
}

std::string format_albanian(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return word(sq, t.hour);
  if (minute == 30)
    return word(sq, t.hour) + " e gjysmë";
  const auto part = minute == 15 || minute == 45 ? std::string("një çerek")
                                                 : word(sq, minute < 30 ? minute : 60 - minute);
  return word(sq, minute < 30 ? t.hour : t.next) + (minute < 30 ? " e " : " pa ") + part;
}

std::string format_bulgarian(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return word(bg, t.hour) + (t.hour == 1 ? " час" : " часа");
  if (minute == 30)
    return word(bg, t.hour) + " и половина";
  if (minute == 1)
    return word(bg, t.hour) + " и една минута";
  if (minute == 59)
    return word(bg, t.next) + " без една минута";
  const int n = minute < 30 ? minute : 60 - minute;
  const auto part = minute == 15 || minute == 45 ? std::string("четвърт")
                    : n == 2                     ? std::string("две")
                    : n == 21                    ? std::string("двадесет и една")
                    : n == 22                    ? std::string("двадесет и две")
                                                 : word(bg, n);
  return word(bg, minute < 30 ? t.hour : t.next) + (minute < 30 ? " и " : " без ") + part;
}

std::string format_greek(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours hours = {"",     "μία",  "δύο",   "τρεις", "τέσσερις", "πέντε", "έξι",
                           "επτά", "οκτώ", "εννέα", "δέκα",  "έντεκα",   "δώδεκα"};
  if (minute == 0)
    return word(hours, t.hour);
  if (minute == 30)
    return word(hours, t.hour) + " και μισή";
  const auto part = minute == 15 || minute == 45 ? std::string("τέταρτο")
                                                 : word(el, minute < 30 ? minute : 60 - minute);
  return word(hours, minute < 30 ? t.hour : t.next) + (minute < 30 ? " και " : " παρά ") + part;
}

std::string format_maltese(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours hours = {"",         "is-siegħa", "is-sagħtejn", "it-tlieta",  "l-erbgħa",
                           "il-ħamsa", "is-sitta",  "is-sebgħa",   "it-tmienja", "id-disgħa",
                           "l-għaxra", "il-ħdax",   "it-tnax"};
  if (minute == 0)
    return word(hours, t.hour);
  if (minute == 30)
    return word(hours, t.hour) + " u nofs";
  if (minute == 1)
    return word(hours, t.hour) + " u minuta";
  if (minute == 59)
    return word(hours, t.next) + (t.next == 1 ? " nieqsa minuta" : " neqsin minuta");
  const auto part = minute == 15 || minute == 45 ? std::string("kwart")
                                                 : word(mt, minute < 30 ? minute : 60 - minute);
  if (minute < 30)
    return word(hours, t.hour) + " u " + part;
  return word(hours, t.next) + (t.next == 1 ? " nieqsa " : " neqsin ") + part;
}
} // namespace spoken_time
