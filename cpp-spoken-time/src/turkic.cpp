#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"

namespace spoken_time {
using namespace detail;
namespace {
constexpr std::array<std::string_view, 30> azerbaijani = {
    "sıfır",         "bir",          "iki",         "üç",
    "dörd",          "beş",          "altı",        "yeddi",
    "səkkiz",        "doqquz",       "on",          "on bir",
    "on iki",        "on üç",        "on dörd",     "on beş",
    "on altı",       "on yeddi",     "on səkkiz",   "on doqquz",
    "iyirmi",        "iyirmi bir",   "iyirmi iki",  "iyirmi üç",
    "iyirmi dörd",   "iyirmi beş",   "iyirmi altı", "iyirmi yeddi",
    "iyirmi səkkiz", "iyirmi doqquz"};
constexpr std::array<std::string_view, 30> kazakh = {
    "нөл",         "бір",        "екі",         "үш",          "төрт",         "бес",
    "алты",        "жеті",       "сегіз",       "тоғыз",       "он",           "он бір",
    "он екі",      "он үш",      "он төрт",     "он бес",      "он алты",      "он жеті",
    "он сегіз",    "он тоғыз",   "жиырма",      "жиырма бір",  "жиырма екі",   "жиырма үш",
    "жиырма төрт", "жиырма бес", "жиырма алты", "жиырма жеті", "жиырма сегіз", "жиырма тоғыз"};
constexpr std::array<std::string_view, 13> azerbaijani_dative = {
    "",        "birə",    "ikiyə",   "üçə", "dördə",   "beşə",    "altıya",
    "yeddiyə", "səkkizə", "doqquza", "ona", "on birə", "on ikiyə"};
constexpr std::array<std::string_view, 13> azerbaijani_genitive = {
    "",         "birin",    "ikinin",   "üçün", "dördün",   "beşin",    "altının",
    "yeddinin", "səkkizin", "doqquzun", "onun", "on birin", "on ikinin"};
constexpr std::array<std::string_view, 13> kazakh_ablative = {
    "",        "бірден",   "екіден",   "үштен", "төрттен",   "бестен",   "алтыдан",
    "жетіден", "сегізден", "тоғыздан", "оннан", "он бірден", "он екіден"};
constexpr std::array<std::string_view, 13> kazakh_dative = {
    "",       "бірге",   "екіге",   "үшке", "төртке",   "беске",   "алтыға",
    "жетіге", "сегізге", "тоғызға", "онға", "он бірге", "он екіге"};
constexpr std::array<std::string_view, 10> uzbek_units = {
    "nol", "bir", "ikki", "uch", "to‘rt", "besh", "olti", "yetti", "sakkiz", "to‘qqiz"};

std::string uzbek_number(int n) {
  if (n < 10)
    return word(uzbek_units, n);
  return std::string(n < 20 ? "o‘n" : "yigirma") +
         (n % 10 == 0 ? std::string() : " " + word(uzbek_units, n % 10));
}
}

std::string format_azerbaijani(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return "saat " + word(azerbaijani, t.hour);
  if (minute == 30)
    return "saat " + word(azerbaijani_genitive, t.next) + " yarısı";
  return "saat " + word(azerbaijani_dative, t.next) + " " +
         word(azerbaijani, minute < 30 ? minute : 60 - minute) +
         (minute < 30 ? " dəqiqə işləyib" : " dəqiqə qalıb");
}

std::string format_kazakh(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return "сағат " + word(kazakh, t.hour);
  if (minute == 30)
    return "сағат " + word(kazakh, t.hour) + " жарым";
  return "сағат " +
         word(minute < 30 ? kazakh_ablative : kazakh_dative, minute < 30 ? t.hour : t.next) + " " +
         word(kazakh, minute < 30 ? minute : 60 - minute) +
         (minute < 30 ? " минут өтті" : " минут қалды");
}

std::string format_uzbek(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return "soat " + uzbek_number(t.hour);
  if (minute == 30)
    return "soat " + uzbek_number(t.hour) + " yarim";
  if (minute < 30)
    return "soat " + uzbek_number(t.hour) + "dan " + uzbek_number(minute) + " daqiqa o‘tdi";
  return "soat " + uzbek_number(t.next) + "ga " + uzbek_number(60 - minute) + " daqiqa kam";
}
}
