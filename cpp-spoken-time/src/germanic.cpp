// SPDX-FileCopyrightText: 2026 Andrei Podkovyrin
// SPDX-License-Identifier: MIT

#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"
#include "numbers.hpp"

namespace spoken_time {
using namespace detail;

std::string format_dutch(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto current = word(nl, t.hour);
  const auto next = word(nl, t.next);
  if (minute == 0)
    return current + " uur";
  if (minute == 15)
    return "kwart over " + current;
  if (minute == 30)
    return "half " + next;
  if (minute == 45)
    return "kwart voor " + next;
  if (minute >= 20 && minute <= 29)
    return word(nl, 30 - minute) + " voor half " + next;
  if (minute >= 31 && minute <= 40)
    return word(nl, minute - 30) + " over half " + next;
  if (minute <= 19)
    return word(nl, minute) + " over " + current;
  return word(nl, 60 - minute) + " voor " + next;
}

std::string format_german(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto current = word(de, t.hour);
  const auto next = word(de, t.next);
  const auto amount = [](int n) { return n == 1 ? std::string("eine minute") : word(de, n); };
  if (minute == 0)
    return (t.hour == 1 ? "ein" : current) + std::string(" uhr");
  if (minute == 15)
    return "viertel nach " + current;
  if (minute == 30)
    return "halb " + next;
  if (minute == 45)
    return "viertel vor " + next;
  if (minute > 20 && minute < 30)
    return amount(30 - minute) + " vor halb " + next;
  if (minute > 30 && minute < 40)
    return amount(minute - 30) + " nach halb " + next;
  if (minute <= 20)
    return amount(minute) + " nach " + current;
  return amount(60 - minute) + " vor " + next;
}

std::string format_english(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 1)
    return "one minute past " + word(en, t.hour);
  if (minute == 59)
    return "one minute to " + word(en, t.next);
  if (minute % 5 != 0) {
    const bool after = minute < 30;
    return word(en, after ? minute : 60 - minute) + (after ? " minutes past " : " minutes to ") +
           word(en, after ? t.hour : t.next);
  }
  return past_to(t, en, " past ", " to ", "quarter", "half", " o'clock");
}

namespace {
std::string nordic(const Time& t, const Words& words, std::string_view clock, std::string_view past,
                   std::string_view to, std::string_view half, std::string_view quarter,
                   std::string_view one_past, std::string_view one_to) {
  const auto current = word(words, t.hour);
  const auto next = word(words, t.next);
  const auto part = [&](int n, bool after) {
    return n == 1 ? std::string(after ? one_past : one_to) : word(words, n);
  };
  if (t.minute == 0)
    return std::string(clock) + current;
  if (t.minute == 15)
    return std::string(quarter) + std::string(past) + current;
  if (t.minute == 30)
    return std::string(half) + next;
  if (t.minute == 45)
    return std::string(quarter) + std::string(to) + next;
  if (t.minute > 20 && t.minute < 30)
    return part(30 - t.minute, false) + std::string(to) + std::string(half) + next;
  if (t.minute > 30 && t.minute < 40)
    return part(t.minute - 30, true) + std::string(past) + std::string(half) + next;
  const bool after = t.minute < 30;
  return part(after ? t.minute : 60 - t.minute, after) + std::string(after ? past : to) +
         (after ? current : next);
}
} // namespace

std::string format_danish(int hour_of_day, int minute) {
  return nordic(Time(hour_of_day, minute), da, "klokken ", " over ", " i ", "halv ", "kvart",
                "et minut", "et minut");
}
std::string format_norwegian(int hour_of_day, int minute) {
  return nordic(Time(hour_of_day, minute), nb, "klokka ", " over ", " på ", "halv ", "kvart",
                "ett minutt", "ett minutt");
}
std::string format_swedish(int hour_of_day, int minute) {
  return nordic(Time(hour_of_day, minute), sv, "klockan ", " över ", " i ", "halv ", "kvart",
                "en minut", "en minut");
}
std::string format_icelandic(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return "klukkan " + word(is, t.hour);
  if (minute == 30)
    return "hálf " + word(is, t.next);
  if (minute == 1)
    return "ein mínúta yfir " + word(is, t.hour);
  if (minute == 59)
    return "eina mínútu í " + word(is, t.next);
  return past_to(t, is, " yfir ", " í ", "korter", "");
}
std::string format_yiddish(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 30)
    return "האַלב " + word(yi, t.next);
  if (minute == 1)
    return "איין מינוט נאָך " + word(yi, t.hour);
  if (minute == 59)
    return "איין מינוט פֿאַר " + word(yi, t.next);
  return past_to(t, yi, " נאָך ", " פֿאַר ", "אַ פֿערטל", "");
}

std::string format_finnish(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return "kello " + word(fi, t.hour);
  if (minute == 30)
    return "puoli " + word(fi, t.next);
  if (minute > 20 && minute < 30)
    return word(fi, 30 - minute) + " vaille puoli " + word(fi, t.next);
  if (minute > 30 && minute < 40)
    return word(fi, minute - 30) + " yli puoli " + word(fi, t.next);
  return past_to(t, fi, " yli ", " vaille ", "varttia", "");
}
} // namespace spoken_time
