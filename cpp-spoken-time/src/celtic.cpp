// SPDX-FileCopyrightText: 2026 Andrei Podkovyrin
// SPDX-License-Identifier: MIT

#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"
#include "numbers.hpp"

namespace spoken_time {
using namespace detail;

std::string format_breton(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours hours = {"",         "un eur",       "div eur",    "teir eur", "peder eur",
                           "pemp eur", "c'hwec'h eur", "seizh eur",  "eizh eur", "nav eur",
                           "dek eur",  "unnek eur",    "daouzek eur"};
  if (minute == 0)
    return word(hours, t.hour);
  if (minute == 15)
    return word(hours, t.hour) + " ha kard";
  if (minute == 30)
    return word(hours, t.hour) + " hanter";
  if (minute == 45)
    return word(hours, t.next) + " nemet kard";
  if (minute < 30)
    return word(hours, t.hour) + " " + word(br, minute);
  return word(hours, t.next) + " nemet " + word(br, 60 - minute);
}

namespace {
std::string irish_minutes(int n) {
  constexpr std::array<std::string_view, 11> units = {"",
                                                      "aon nóiméad",
                                                      "dhá nóiméad",
                                                      "trí nóiméad",
                                                      "ceithre nóiméad",
                                                      "cúig nóiméad",
                                                      "sé nóiméad",
                                                      "seacht nóiméad",
                                                      "ocht nóiméad",
                                                      "naoi nóiméad",
                                                      "deich nóiméad"};
  if (n == 1)
    return "nóiméad";
  if (n <= 10)
    return word(units, n);
  if (n < 20)
    return word(units, n - 10) + " déag";
  if (n == 20)
    return "fiche nóiméad";
  return word(units, n - 20) + " is fiche";
}
std::string gaelic_minutes(int n) {
  constexpr std::array<std::string_view, 11> units = {"",
                                                      "aon mhionaid",
                                                      "dà mhionaid",
                                                      "trì mionaidean",
                                                      "ceithir mionaidean",
                                                      "còig mionaidean",
                                                      "sia mionaidean",
                                                      "seachd mionaidean",
                                                      "ochd mionaidean",
                                                      "naoi mionaidean",
                                                      "deich mionaidean"};
  if (n <= 10)
    return word(units, n);
  if (n < 20)
    return word(units, n - 10) + (n == 12 ? " dheug" : " deug");
  if (n == 20)
    return "fichead mionaid";
  return word(units, n - 20) + " fichead";
}
std::string welsh_minutes(int n) {
  constexpr std::array<std::string_view, 11> units = {
      "",           "un munud",    "dau funud",  "tri munud", "pedwar munud", "pum munud",
      "chwe munud", "saith munud", "wyth munud", "naw munud", "deng munud"};
  if (n <= 10)
    return word(units, n);
  if (n == 11)
    return "un munud ar ddeg";
  if (n == 12)
    return "deuddeg munud";
  if (n <= 14)
    return word(units, n - 10) + " ar ddeg";
  if (n == 15)
    return "pymtheg munud";
  if (n <= 17)
    return word(units, n - 15) + " ar bymtheg";
  if (n == 18)
    return "deunaw munud";
  if (n == 19)
    return "pedwar munud ar bymtheg";
  if (n == 20)
    return "ugain munud";
  return word(units, n - 20) + " ar hugain";
}
} // namespace

std::string format_irish(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours hours = {"",        "a haon",      "a dó",      "a trí",   "a ceathair",
                           "a cúig",  "a sé",        "a seacht",  "a hocht", "a naoi",
                           "a deich", "a haon déag", "a dó dhéag"};
  if (minute == 0)
    return word(hours, t.hour) + " a chlog";
  const auto part = minute == 30 ? std::string("leathuair")
                    : minute == 15 || minute == 45
                        ? std::string("ceathrú")
                        : irish_minutes(minute < 30 ? minute : 60 - minute);
  return part + (minute <= 30 ? " tar éis " : " chun ") +
         word(hours, minute <= 30 ? t.hour : t.next);
}

std::string format_scottish_gaelic(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours hours = {"",
                           "uair",
                           "dà uair",
                           "trì uairean",
                           "ceithir uairean",
                           "còig uairean",
                           "sia uairean",
                           "seachd uairean",
                           "ochd uairean",
                           "naoi uairean",
                           "deich uairean",
                           "aon uair deug",
                           "dà uair dheug"};
  const auto reference = [&](int h) {
    return h == 1 || h >= 11 ? word(hours, h) : h == 2 ? std::string("dhà") : word(gd, h);
  };
  if (minute == 0)
    return word(hours, t.hour);
  const auto part = minute == 30 ? std::string("leth-uair")
                    : minute == 15 || minute == 45
                        ? std::string("cairteal")
                        : gaelic_minutes(minute < 30 ? minute : 60 - minute);
  return part + (minute <= 30 ? " an dèidh " : " gu ") + reference(minute <= 30 ? t.hour : t.next);
}

std::string format_welsh(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours mutated = {"",      "un",   "ddau", "dri",  "bedwar",     "bump",    "chwech",
                             "saith", "wyth", "naw",  "ddeg", "un ar ddeg", "ddeuddeg"};
  if (minute == 0)
    return word(cy, t.hour) + " o'r gloch";
  const auto part = minute == 30 ? std::string("hanner")
                    : minute == 15 || minute == 45
                        ? std::string("chwarter")
                        : welsh_minutes(minute < 30 ? minute : 60 - minute);
  return part + (minute <= 30 ? " wedi " : " i ") +
         (minute <= 30 ? word(cy, t.hour) : word(mutated, t.next));
}
} // namespace spoken_time
