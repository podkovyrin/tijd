#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"

namespace spoken_time {
using namespace detail;
namespace {

std::string traditional_han_number(int n) {
  constexpr std::array<std::string_view, 10> digits = {"零", "一", "二", "三", "四",
                                                       "五", "六", "七", "八", "九"};
  if (n < 10)
    return word(digits, n);
  return (n < 20 ? std::string() : word(digits, n / 10)) + "十" +
         (n % 10 == 0 ? std::string() : word(digits, n % 10));
}
}

std::string format_cantonese(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = (t.hour == 2 ? std::string("兩") : traditional_han_number(t.hour)) + "點";
  if (minute == 0)
    return hour;
  if (minute == 30)
    return hour + "半";
  if (minute % 5 == 0)
    return hour + (minute == 10 ? std::string("兩") : traditional_han_number(minute / 5)) + "個字";
  return hour + (minute < 10 ? "零" : "") + traditional_han_number(minute) + "分";
}

std::string format_mandarin_traditional(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = (t.hour == 2 ? std::string("兩") : traditional_han_number(t.hour)) + "點";
  if (minute == 0)
    return hour;
  if (minute == 15)
    return hour + "一刻";
  if (minute == 30)
    return hour + "半";
  return hour + (minute < 10 ? "零" : "") + traditional_han_number(minute) + "分";
}
}
