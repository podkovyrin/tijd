#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"

namespace spoken_time {
using namespace detail;
namespace {
std::string han_number(int n) {
  constexpr std::array<std::string_view, 10> digits = {"零", "一", "二", "三", "四",
                                                       "五", "六", "七", "八", "九"};
  if (n < 10)
    return word(digits, n);
  const auto tens = (n < 20 ? std::string() : word(digits, n / 10)) + "十";
  return tens + (n % 10 == 0 ? std::string() : word(digits, n % 10));
}

std::string korean_number(int n) {
  constexpr std::array<std::string_view, 10> digits = {"영", "일", "이", "삼", "사",
                                                       "오", "육", "칠", "팔", "구"};
  if (n < 10)
    return word(digits, n);
  const auto tens = (n < 20 ? std::string() : word(digits, n / 10)) + "십";
  return tens + (n % 10 == 0 ? std::string() : word(digits, n % 10));
}
}

std::string format_japanese(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = han_number(t.hour) + "時";
  if (minute == 0)
    return hour;
  if (minute == 30)
    return hour + "半";
  return hour + han_number(minute) + "分";
}

std::string format_korean(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr Hours hours = {"",     "한",   "두",   "세", "네",   "다섯", "여섯",
                           "일곱", "여덟", "아홉", "열", "열한", "열두"};
  const auto hour = word(hours, t.hour) + " 시";
  if (minute == 0)
    return hour;
  if (minute == 30)
    return hour + " 반";
  return hour + " " + korean_number(minute) + " 분";
}

std::string format_mandarin(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = (t.hour == 2 ? std::string("两") : han_number(t.hour)) + "点";
  if (minute == 0)
    return hour;
  if (minute == 15)
    return hour + "一刻";
  if (minute == 30)
    return hour + "半";
  return hour + (minute < 10 ? "零" : "") + han_number(minute) + "分";
}
}
