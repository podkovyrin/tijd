#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"

namespace spoken_time {
using namespace detail;
namespace {
constexpr std::array<std::string_view, 30> kurmanji = {
    "sifir",      "yek",         "du",         "sê",          "çar",         "pênc",
    "şeş",        "heft",        "heşt",       "neh",         "deh",         "yazdeh",
    "dwazdeh",    "sêzdeh",      "çardeh",     "panzdeh",     "şazdeh",      "hevdeh",
    "hejdeh",     "nozdeh",      "bîst",       "bîst û yek",  "bîst û du",   "bîst û sê",
    "bîst û çar", "bîst û pênc", "bîst û şeş", "bîst û heft", "bîst û heşt", "bîst û neh"};
constexpr std::array<std::string_view, 30> sorani = {
    "سفر",         "یەک",         "دوو",        "سێ",          "چوار",        "پێنج",
    "شەش",         "حەوت",        "هەشت",       "نۆ",          "دە",          "یازدە",
    "دوازدە",      "سێزدە",       "چواردە",     "پانزدە",      "شانزدە",      "حەڤدە",
    "هەژدە",       "نۆزدە",       "بیست",       "بیست و یەک",  "بیست و دوو",  "بیست و سێ",
    "بیست و چوار", "بیست و پێنج", "بیست و شەش", "بیست و حەوت", "بیست و هەشت", "بیست و نۆ"};

}

std::string format_kurmanji(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = "saet " + word(kurmanji, minute > 30 ? t.next : t.hour);
  if (minute == 0)
    return hour;
  if (minute == 15)
    return hour + " û çaryek";
  if (minute == 30)
    return hour + " û nîv";
  if (minute == 45)
    return hour + " kêm çaryek";
  return hour + (minute < 30 ? " û " : " kêm ") +
         word(kurmanji, minute < 30 ? minute : 60 - minute) + " deqe";
}

std::string format_sorani(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = "کاتژمێر " + word(sorani, minute > 30 ? t.next : t.hour);
  if (minute == 0)
    return hour;
  if (minute == 15)
    return hour + " و چارەک";
  if (minute == 30)
    return hour + " و نیو";
  if (minute == 45)
    return hour + " چارەکی دەوێ";
  if (minute < 30)
    return hour + " و " + word(sorani, minute) + " خولەک";
  return hour + " " + word(sorani, 60 - minute) + " خولەکی دەوێ";
}
}
