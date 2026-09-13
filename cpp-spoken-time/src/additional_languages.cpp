#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"

namespace spoken_time {
using namespace detail;
namespace {
std::string arabic_minutes(int n, bool before) {
  constexpr std::array<std::string_view, 20> small = {
      "",          "إحدى",     "اثنتان",  "ثلاث",     "أربع",       "خمس",        "ست",
      "سبع",       "ثماني",    "تسع",     "عشر",      "إحدى عشرة",  "اثنتا عشرة", "ثلاث عشرة",
      "أربع عشرة", "خمس عشرة", "ست عشرة", "سبع عشرة", "ثماني عشرة", "تسع عشرة"};
  if (n == 1)
    return "دقيقة";
  if (n == 2)
    return before ? "دقيقتين" : "دقيقتان";
  if (n <= 10)
    return word(small, n) + " دقائق";
  if (n < 20) {
    const auto count = n == 12 && before ? std::string("اثنتي عشرة") : word(small, n);
    return count + " دقيقة";
  }
  const auto tens = before ? std::string("عشرين") : std::string("عشرون");
  if (n == 20)
    return tens + " دقيقة";
  const auto unit = n == 22 && before    ? std::string("اثنتين")
                    : n == 28 && !before ? std::string("ثمان")
                                         : word(small, n % 10);
  return unit + " و" + tens + " دقيقة";
}

std::string persian_number(int n) {
  constexpr std::array<std::string_view, 21> small = {
      "صفر",    "یک",     "دو",     "سه",   "چهار",  "پنج",    "شش",
      "هفت",    "هشت",    "نه",     "ده",   "یازده", "دوازده", "سیزده",
      "چهارده", "پانزده", "شانزده", "هفده", "هجده",  "نوزده",  "بیست"};
  if (n <= 20)
    return word(small, n);
  return "بیست و " + word(small, n - 20);
}

std::string swahili_number(int n) {
  constexpr std::array<std::string_view, 11> small = {
      "sifuri", "moja", "mbili", "tatu", "nne", "tano", "sita", "saba", "nane", "tisa", "kumi"};
  if (n <= 10)
    return word(small, n);
  const auto tens = n < 20 ? std::string("kumi") : std::string("ishirini");
  return tens + (n % 10 == 0 ? std::string() : " na " + word(small, n % 10));
}
}

std::string format_arabic(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours hours = {"",        "الواحدة",      "الثانية",     "الثالثة", "الرابعة",
                           "الخامسة", "السادسة",      "السابعة",     "الثامنة", "التاسعة",
                           "العاشرة", "الحادية عشرة", "الثانية عشرة"};
  const auto hour = "الساعة " + word(hours, minute <= 30 ? t.hour : t.next);
  if (minute == 0)
    return hour;
  if (minute == 30)
    return hour + " والنصف";
  if (minute == 15)
    return hour + " والربع";
  if (minute == 20)
    return hour + " والثلث";
  if (minute == 40)
    return hour + " إلا الثلث";
  if (minute == 45)
    return hour + " إلا الربع";
  if (minute < 30)
    return hour + " و" + arabic_minutes(minute, false);
  return hour + " إلا " + arabic_minutes(60 - minute, true);
}

std::string format_brazilian_portuguese(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Words numbers = {
      "zero",          "um",           "dois",         "três",         "quatro",
      "cinco",         "seis",         "sete",         "oito",         "nove",
      "dez",           "onze",         "doze",         "treze",        "catorze",
      "quinze",        "dezesseis",    "dezessete",    "dezoito",      "dezenove",
      "vinte",         "vinte e um",   "vinte e dois", "vinte e três", "vinte e quatro",
      "vinte e cinco", "vinte e seis", "vinte e sete", "vinte e oito", "vinte e nove"};
  const auto hour = [&](int h) {
    return h == 1 ? std::string("uma") : h == 2 ? std::string("duas") : word(numbers, h);
  };
  if (minute == 0)
    return hour(t.hour) + (t.hour == 1 ? " hora" : " horas");
  if (minute == 30)
    return hour(t.hour) + " e meia";
  if (minute < 30)
    return hour(t.hour) + " e " + word(numbers, minute);
  return word(numbers, 60 - minute) + (t.next == 1 ? " para a " : " para as ") + hour(t.next);
}

std::string format_european_portuguese(int hour_of_day, int minute) {
  return format_portuguese(hour_of_day, minute);
}

std::string format_persian(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = "ساعت " + persian_number(minute <= 30 ? t.hour : t.next);
  if (minute == 0)
    return hour;
  if (minute == 15)
    return hour + " و ربع";
  if (minute == 30)
    return hour + " و نیم";
  if (minute == 45)
    return hour + " ربع کم";
  if (minute < 30)
    return hour + " و " + persian_number(minute) + " دقیقه";
  return hour + " " + persian_number(60 - minute) + " دقیقه کم";
}

std::string format_swahili(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const int current = (t.hour + 5) % 12 + 1;
  const int next = current % 12 + 1;
  const auto hour = "saa " + swahili_number(minute <= 30 ? current : next);
  if (minute == 0)
    return hour;
  if (minute == 30)
    return hour + " na nusu";
  const int n = minute < 30 ? minute : 60 - minute;
  const auto part = n == 15 ? std::string("robo") : "dakika " + swahili_number(n);
  return hour + (minute < 30 ? " na " : " kasoro ") + part;
}
}
