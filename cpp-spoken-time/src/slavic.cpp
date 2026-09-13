// SPDX-FileCopyrightText: 2026 Andrei Podkovyrin
// SPDX-License-Identifier: MIT

#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"
#include "numbers.hpp"

namespace spoken_time {
using namespace detail;

namespace {
constexpr Hours russian_next = {
    "",         "первого",  "второго",  "третьего", "четвёртого",    "пятого",      "шестого",
    "седьмого", "восьмого", "девятого", "десятого", "одиннадцатого", "двенадцатого"};
constexpr Words russian_genitive = {"нуля",
                                    "одной",
                                    "двух",
                                    "трёх",
                                    "четырёх",
                                    "пяти",
                                    "шести",
                                    "семи",
                                    "восьми",
                                    "девяти",
                                    "десяти",
                                    "одиннадцати",
                                    "двенадцати",
                                    "тринадцати",
                                    "четырнадцати",
                                    "пятнадцати",
                                    "шестнадцати",
                                    "семнадцати",
                                    "восемнадцати",
                                    "девятнадцати",
                                    "двадцати",
                                    "двадцати одной",
                                    "двадцати двух",
                                    "двадцати трёх",
                                    "двадцати четырёх",
                                    "двадцати пяти",
                                    "двадцати шести",
                                    "двадцати семи",
                                    "двадцати восьми",
                                    "двадцати девяти"};
std::string russian_minutes(int n) {
  auto number = n % 10 == 1 && n != 11
                    ? (n == 1 ? std::string("одна") : std::string("двадцать одна"))
                : n == 2  ? std::string("две")
                : n == 22 ? std::string("двадцать две")
                          : word(ru, n);
  const int last = n % 10;
  return number + (n >= 11 && n <= 14       ? " минут"
                   : last == 1              ? " минута"
                   : last >= 2 && last <= 4 ? " минуты"
                                            : " минут");
}
std::string czech_minutes(int n, bool accusative) {
  if (n == 1)
    return accusative ? "jednu minutu" : "jedna minuta";
  return word(cs, n) + (n >= 2 && n <= 4 ? " minuty" : " minut");
}
} // namespace

std::string format_russian(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0) {
    if (t.hour == 1)
      return "час";
    return word(ru, t.hour) + (t.hour <= 4 ? " часа" : " часов");
  }
  if (minute == 30) {
    return std::string(t.next == 11 ? "пол-" : "пол") + word(russian_next, t.next);
  }
  if (minute < 30)
    return russian_minutes(minute) + " " + word(russian_next, t.next);
  const int remaining = 60 - minute;
  auto part = minute == 45 ? std::string("четверти") : word(russian_genitive, remaining);
  if (remaining == 1 || remaining == 21)
    part += " минуты";
  return "без " + part + " " + (t.next == 1 ? "час" : word(ru, t.next));
}

std::string format_czech(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours genitive = {"",      "jedné", "druhé",  "třetí",  "čtvrté",    "páté",    "šesté",
                              "sedmé", "osmé",  "deváté", "desáté", "jedenácté", "dvanácté"};
  const auto next_acc = t.next == 1 ? std::string("jednu") : word(cs, t.next);
  const auto quarter = "čtvrt na " + next_acc;
  const auto half = "půl " + word(genitive, t.next);
  const auto three_quarters = "tři čtvrtě na " + next_acc;
  if (minute == 0)
    return word(cs, t.hour) + (t.hour == 1 ? " hodina" : t.hour <= 4 ? " hodiny" : " hodin");
  if (minute == 15)
    return quarter;
  if (minute == 30)
    return half;
  if (minute == 45)
    return three_quarters;
  if (minute < 15)
    return "za " + czech_minutes(15 - minute, true) + " " + quarter;
  if (minute < 30)
    return "za " + czech_minutes(30 - minute, true) + " " + half;
  if (minute < 40)
    return czech_minutes(minute - 30, false) + " po " + half;
  if (minute < 45)
    return "za " + czech_minutes(45 - minute, true) + " " + three_quarters;
  return "za " + czech_minutes(60 - minute, true) + " " + word(cs, t.next);
}

std::string format_polish(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours nominative = {"",          "pierwsza",  "druga",   "trzecia", "czwarta",
                                "piąta",     "szósta",    "siódma",  "ósma",    "dziewiąta",
                                "dziesiąta", "jedenasta", "dwunasta"};
  constexpr Hours oblique = {"",           "pierwszej",  "drugiej",  "trzeciej", "czwartej",
                             "piątej",     "szóstej",    "siódmej",  "ósmej",    "dziewiątej",
                             "dziesiątej", "jedenastej", "dwunastej"};
  if (minute == 0)
    return word(nominative, t.hour);
  if (minute == 15)
    return "kwadrans po " + word(oblique, t.hour);
  if (minute == 30)
    return "wpół do " + word(oblique, t.next);
  if (minute == 45)
    return "za kwadrans " + word(nominative, t.next);
  const int n = minute < 30 ? minute : 60 - minute;
  std::string part;
  if (n == 1)
    part = minute < 30 ? "minuta" : "minutę";
  else if (n == 21)
    part = "dwadzieścia jeden minut";
  else
    part = word(pl, n);
  return minute < 30 ? part + " po " + word(oblique, t.hour)
                     : "za " + part + " " + word(nominative, t.next);
}

std::string format_slovak(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours genitive = {"",         "jednej",     "druhej",   "tretej", "štvrtej",
                              "piatej",   "šiestej",    "siedmej",  "ôsmej",  "deviatej",
                              "desiatej", "jedenástej", "dvanástej"};
  const auto next_acc = t.next == 1 ? std::string("jednu") : word(sk, t.next);
  const auto quarter = "štvrť na " + next_acc;
  const auto half = "pol " + word(genitive, t.next);
  const auto three_quarters = "trištvrte na " + next_acc;
  const auto minutes = [](int n) {
    return n == 1 ? std::string("jednu minútu") : word(sk, n) + (n <= 4 ? " minúty" : " minút");
  };
  if (minute == 0)
    return word(sk, t.hour) + (t.hour == 1 ? " hodina" : t.hour <= 4 ? " hodiny" : " hodín");
  if (minute == 15)
    return quarter;
  if (minute == 30)
    return half;
  if (minute == 45)
    return three_quarters;
  if (minute < 15)
    return "o " + minutes(15 - minute) + " " + quarter;
  if (minute < 30)
    return "o " + minutes(30 - minute) + " " + half;
  if (minute < 45)
    return "o " + minutes(45 - minute) + " " + three_quarters;
  return "o " + minutes(60 - minute) + " " + word(sk, t.next);
}

std::string format_slovenian(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours genitive = {"",        "enih",     "dveh",      "treh",  "štirih",
                              "petih",   "šestih",   "sedmih",    "osmih", "devetih",
                              "desetih", "enajstih", "dvanajstih"};
  constexpr Hours accusative = {"",      "eno",  "dve",   "tri",   "štiri",  "pet",     "šest",
                                "sedem", "osem", "devet", "deset", "enajst", "dvanajst"};
  if (minute == 0)
    return word(sl, t.hour);
  if (minute == 15)
    return "četrt čez " + word(accusative, t.hour);
  if (minute == 30)
    return "pol " + word(genitive, t.next);
  if (minute == 45)
    return "tri četrt na " + word(accusative, t.next);
  if (minute < 30)
    return word(sl, t.hour) + " in " + word(sl, minute);
  const int n = 60 - minute;
  return "za " + (n == 1 ? std::string("eno minuto") : word(sl, n)) + " " + word(sl, t.next);
}

std::string format_serbo_croatian(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return word(sh, t.hour);
  if (minute == 30)
    return word(sh, t.hour) + " i po";
  if (minute < 30)
    return word(sh, t.hour) + " i " + word(sh, minute);
  return word(sh, 60 - minute) + " do " + word(sh, t.next);
}

std::string format_ukrainian(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours nominative = {"",       "перша",      "друга",     "третя",  "четверта",
                                "п’ята",  "шоста",      "сьома",     "восьма", "дев’ята",
                                "десята", "одинадцята", "дванадцята"};
  constexpr Hours accusative = {"",       "першу",      "другу",     "третю",  "четверту",
                                "п’яту",  "шосту",      "сьому",     "восьму", "дев’яту",
                                "десяту", "одинадцяту", "дванадцяту"};
  if (minute == 0)
    return word(nominative, t.hour) + " година";
  if (minute == 30)
    return "пів на " + word(accusative, t.next);
  if (minute == 15)
    return "чверть на " + word(accusative, t.next);
  if (minute == 45)
    return "за чверть " + word(nominative, t.next);
  const int n = minute < 30 ? minute : 60 - minute;
  const bool one = n % 10 == 1 && n != 11;
  std::string number = word(uk, n);
  if (one)
    number = n == 1 ? "одна" : "двадцять одна";
  if (n == 2 || n == 22)
    number = n == 2 ? "дві" : "двадцять дві";
  if (one && minute > 30)
    number = n == 1 ? "одну" : "двадцять одну";
  const int last = n % 10;
  const auto noun = n >= 11 && n <= 14       ? " хвилин"
                    : one                    ? (minute < 30 ? " хвилина" : " хвилину")
                    : last >= 2 && last <= 4 ? " хвилини"
                                             : " хвилин";
  if (minute < 30)
    return number + noun + " на " + word(accusative, t.next);
  return "за " + number + noun + " " + word(nominative, t.next);
}
} // namespace spoken_time
