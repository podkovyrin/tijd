#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"

namespace spoken_time {
using namespace detail;
namespace {
constexpr std::array<std::string_view, 30> hebrew = {
    "אפס",         "אחת",        "שתיים",     "שלוש",       "ארבע",         "חמש",
    "שש",          "שבע",        "שמונה",     "תשע",        "עשר",          "אחת עשרה",
    "שתים עשרה",   "שלוש עשרה",  "ארבע עשרה", "חמש עשרה",   "שש עשרה",      "שבע עשרה",
    "שמונה עשרה",  "תשע עשרה",   "עשרים",     "עשרים ואחת", "עשרים ושתיים", "עשרים ושלוש",
    "עשרים וארבע", "עשרים וחמש", "עשרים ושש", "עשרים ושבע", "עשרים ושמונה", "עשרים ותשע"};

std::string hebrew_minutes(int n) {
  if (n == 1)
    return "דקה";
  if (n == 2)
    return "שתי דקות";
  return word(hebrew, n) + " דקות";
}
}

std::string format_hebrew(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return word(hebrew, t.hour);
  if (minute == 15)
    return word(hebrew, t.hour) + " ורבע";
  if (minute == 30)
    return word(hebrew, t.hour) + " וחצי";
  if (minute == 45)
    return "רבע ל" + word(hebrew, t.next);
  if (minute < 30)
    return word(hebrew, t.hour) + " ו" + hebrew_minutes(minute);
  return hebrew_minutes(60 - minute) + " ל" + word(hebrew, t.next);
}
}
