#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"

namespace spoken_time {
using namespace detail;
namespace {
constexpr std::array<std::string_view, 30> egyptian = {
    "صفر",          "واحد",        "اتنين",      "تلاتة",       "أربعة",         "خمسة",
    "ستة",          "سبعة",        "تمانية",     "تسعة",        "عشرة",          "حداشر",
    "اتناشر",       "تلتاشر",      "أربعتاشر",   "خمستاشر",     "ستاشر",         "سبعتاشر",
    "تمنتاشر",      "تسعتاشر",     "عشرين",      "واحد وعشرين", "اتنين وعشرين",  "تلاتة وعشرين",
    "أربعة وعشرين", "خمسة وعشرين", "ستة وعشرين", "سبعة وعشرين", "تمانية وعشرين", "تسعة وعشرين"};
constexpr std::array<std::string_view, 30> levantine = {
    "صفر",          "واحد",        "تنين",       "تلاتة",       "أربعة",         "خمسة",
    "ستة",          "سبعة",        "تمانية",     "تسعة",        "عشرة",          "حدعش",
    "تنعش",         "تلتعش",       "أربعتعش",    "خمستعش",      "ستعش",          "سبعتعش",
    "تمنتعش",       "تسعتعش",      "عشرين",      "واحد وعشرين", "تنين وعشرين",   "تلاتة وعشرين",
    "أربعة وعشرين", "خمسة وعشرين", "ستة وعشرين", "سبعة وعشرين", "تمانية وعشرين", "تسعة وعشرين"};

std::string arabic_dialect_time(const Time& t, const Words& numbers, std::string_view one,
                                std::string_view two) {
  const int h = t.minute > 30 ? t.next : t.hour;
  const auto hour = "الساعة " + (h == 1   ? std::string(one)
                                 : h == 2 ? std::string(two)
                                          : word(numbers, h));
  if (t.minute == 0)
    return hour;
  if (t.minute == 30)
    return hour + " ونص";
  const int n = t.minute < 30 ? t.minute : 60 - t.minute;
  std::string part;
  if (n == 1)
    part = "دقيقة";
  else if (n == 2)
    part = "دقيقتين";
  else if (n == 15)
    part = "ربع";
  else if (n == 20)
    part = "تلت";
  else
    part = word(numbers, n);
  return hour + (t.minute < 30 ? " و" : " إلا ") + part;
}
}

std::string format_egyptian_arabic(int hour_of_day, int minute) {
  return arabic_dialect_time(Time(hour_of_day, minute), egyptian, "واحدة", "اتنين");
}

std::string format_levantine_arabic(int hour_of_day, int minute) {
  return arabic_dialect_time(Time(hour_of_day, minute), levantine, "وحدة", "تنتين");
}
}
