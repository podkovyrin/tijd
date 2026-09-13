// SPDX-FileCopyrightText: 2026 Andrei Podkovyrin
// SPDX-License-Identifier: MIT

#pragma once

#include <array>
#include <cstddef>
#include <stdexcept>
#include <string>
#include <string_view>

namespace spoken_time::detail {
using Words = std::array<std::string_view, 30>;
using Hours = std::array<std::string_view, 13>;

struct Time {
  int hour;
  int next;
  int minute;
  Time(int hour_of_day, int m) {
    if (hour_of_day < 0 || hour_of_day > 23)
      throw std::invalid_argument("hour_of_day must be between 0 and 23");
    if (m < 0 || m > 59)
      throw std::invalid_argument("minute must be between 0 and 59");
    hour = (hour_of_day + 11) % 12 + 1;
    next = hour % 12 + 1;
    minute = m;
  }
};

template <std::size_t N> std::string word(const std::array<std::string_view, N>& words, int n) {
  return std::string(words.at(static_cast<std::size_t>(n)));
}

inline std::string past_to(const Time& t, const Words& words, std::string_view past,
                           std::string_view to, std::string_view quarter, std::string_view half,
                           std::string_view exact_suffix = "") {
  if (t.minute == 0)
    return word(words, t.hour) + std::string(exact_suffix);
  const auto part = t.minute == 30 ? std::string(half)
                    : (t.minute == 15 || t.minute == 45)
                        ? std::string(quarter)
                        : word(words, t.minute < 30 ? t.minute : 60 - t.minute);
  return part + std::string(t.minute <= 30 ? past : to) +
         word(words, t.minute <= 30 ? t.hour : t.next);
}
} // namespace spoken_time::detail
