#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"

namespace spoken_time {
using namespace detail;
namespace {
std::string malay_number(int n, bool malaysian) {
  constexpr std::array<std::string_view, 12> small = {"kosong",  "satu",     "dua",     "tiga",
                                                      "empat",   "lima",     "enam",    "tujuh",
                                                      "delapan", "sembilan", "sepuluh", "sebelas"};
  const auto unit = [&](int u) {
    return u == 8 && malaysian ? std::string("lapan") : word(small, u);
  };
  if (n < 12)
    return unit(n);
  if (n < 20)
    return unit(n - 10) + " belas";
  return unit(n / 10) + " puluh" + (n % 10 == 0 ? std::string() : " " + unit(n % 10));
}

std::string vietnamese_number(int n) {
  constexpr std::array<std::string_view, 11> small = {"không", "một", "hai", "ba",   "bốn", "năm",
                                                      "sáu",   "bảy", "tám", "chín", "mười"};
  if (n <= 10)
    return word(small, n);
  const auto tens = n < 20 ? std::string("mười") : word(small, n / 10) + " mươi";
  if (n % 10 == 0)
    return tens;
  const auto unit = n % 10 == 5             ? std::string("lăm")
                    : n % 10 == 1 && n > 20 ? std::string("mốt")
                                            : word(small, n % 10);
  return tens + " " + unit;
}

std::string thai_number(int n) {
  constexpr std::array<std::string_view, 10> digits = {"ศูนย์", "หนึ่ง", "สอง", "สาม", "สี่",
                                                       "ห้า",  "หก",  "เจ็ด", "แปด", "เก้า"};
  if (n < 10)
    return word(digits, n);
  const auto tens = n < 20   ? std::string("สิบ")
                    : n < 30 ? std::string("ยี่สิบ")
                             : word(digits, n / 10) + "สิบ";
  if (n % 10 == 0)
    return tens;
  return tens + (n % 10 == 1 ? std::string("เอ็ด") : word(digits, n % 10));
}

std::string filipino_number(int n) {
  constexpr std::array<std::string_view, 20> small = {
      "sero",    "uno",   "dos",      "tres",       "kuwatro",  "singko",    "sais",
      "siyete",  "otso",  "nuwebe",   "diyes",      "onse",     "dose",      "trese",
      "katorse", "kinse", "disisais", "disisiyete", "disiotso", "disinuwebe"};
  constexpr std::array<std::string_view, 6> tens = {"",       "",          "bente",
                                                    "trenta", "kuwarenta", "singkuwenta"};
  if (n < 20)
    return word(small, n);
  if (n % 10 == 0)
    return word(tens, n / 10);
  return word(tens, n / 10) + (n < 30 ? " " : " y ") + word(small, n % 10);
}
}

std::string format_filipino(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = t.hour == 1 ? std::string("ala una") : "alas " + filipino_number(t.hour);
  if (minute == 0)
    return hour;
  if (minute == 30)
    return hour + " y medya";
  return hour + " " + filipino_number(minute);
}

std::string format_indonesian(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return "jam " + malay_number(t.hour, false);
  if (minute == 30)
    return "jam setengah " + malay_number(t.next, false);
  const int n = minute < 30 ? minute : 60 - minute;
  const auto part = n == 15 ? std::string("seperempat") : malay_number(n, false) + " menit";
  return "jam " + malay_number(minute < 30 ? t.hour : t.next, false) +
         (minute < 30 ? " lewat " : " kurang ") + part;
}

std::string format_malay(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = "pukul " + malay_number(t.hour, true);
  if (minute == 0)
    return hour;
  if (minute == 30)
    return hour + " setengah";
  if (minute == 15)
    return hour + " suku";
  if (minute == 45)
    return "pukul " + malay_number(t.next, true) + " kurang suku";
  if (minute < 30)
    return hour + " " + malay_number(minute, true) + " minit";
  return "pukul " + malay_number(t.next, true) + " kurang " + malay_number(60 - minute, true) +
         " minit";
}

std::string format_thai(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  constexpr std::array<std::string_view, 24> hours = {
      "เที่ยงคืน", "ตีหนึ่ง",   "ตีสอง",  "ตีสาม",     "ตีสี่",    "ตีห้า",    "หกโมง",     "เจ็ดโมง",
      "แปดโมง", "เก้าโมง", "สิบโมง", "สิบเอ็ดโมง", "เที่ยง",  "บ่ายโมง", "บ่ายสองโมง", "บ่ายสามโมง",
      "สี่โมง",   "ห้าโมง",  "หกโมง", "หนึ่งทุ่ม",    "สองทุ่ม", "สามทุ่ม",  "สี่ทุ่ม",       "ห้าทุ่ม"};
  const auto hour = word(hours, hour_of_day);
  if (t.minute == 0)
    return hour;
  if (t.minute == 30)
    return hour + "ครึ่ง";
  return hour + thai_number(t.minute) + "นาที";
}

std::string format_vietnamese(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = vietnamese_number(t.hour) + " giờ";
  if (minute == 0)
    return hour;
  if (minute == 30)
    return hour + " rưỡi";
  if (minute < 30)
    return hour + " " + vietnamese_number(minute) + " phút";
  return vietnamese_number(t.next) + " giờ kém " + vietnamese_number(60 - minute) + " phút";
}
}
