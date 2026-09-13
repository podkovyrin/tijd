#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"

namespace spoken_time {
using namespace detail;
namespace {
constexpr std::array<std::string_view, 20> burmese_small = {
    "သုည", "တစ်",   "နှစ်",   "သုံး",   "လေး",   "ငါး",   "ခြောက်",   "ခုနှစ်",   "ရှစ်",   "ကိုး",
    "ဆယ်", "ဆယ့်တစ်", "ဆယ့်နှစ်", "ဆယ့်သုံး", "ဆယ့်လေး", "ဆယ့်ငါး", "ဆယ့်ခြောက်", "ဆယ့်ခုနှစ်", "ဆယ့်ရှစ်", "ဆယ့်ကိုး"};
constexpr std::array<std::string_view, 60> khmer = {
    "សូន្យ",       "មួយ",         "ពីរ",         "បី",         "បួន",         "ប្រាំ",       "ប្រាំមួយ",
    "ប្រាំពីរ",     "ប្រាំបី",       "ប្រាំបួន",      "ដប់",        "ដប់មួយ",       "ដប់ពីរ",      "ដប់បី",
    "ដប់បួន",      "ដប់ប្រាំ",      "ដប់ប្រាំមួយ",    "ដប់ប្រាំពីរ",   "ដប់ប្រាំបី",     "ដប់ប្រាំបួន",   "ម្ភៃ",
    "ម្ភៃមួយ",     "ម្ភៃពីរ",      "ម្ភៃបី",       "ម្ភៃបួន",     "ម្ភៃប្រាំ",     "ម្ភៃប្រាំមួយ",  "ម្ភៃប្រាំពីរ",
    "ម្ភៃប្រាំបី",   "ម្ភៃប្រាំបួន",   "សាមសិប",      "សាមសិបមួយ",   "សាមសិបពីរ",    "សាមសិបបី",    "សាមសិបបួន",
    "សាមសិបប្រាំ",  "សាមសិបប្រាំមួយ", "សាមសិបប្រាំពីរ", "សាមសិបប្រាំបី", "សាមសិបប្រាំបួន", "សែសិប",      "សែសិបមួយ",
    "សែសិបពីរ",    "សែសិបបី",      "សែសិបបួន",     "សែសិបប្រាំ",   "សែសិបប្រាំមួយ",  "សែសិបប្រាំពីរ", "សែសិបប្រាំបី",
    "សែសិបប្រាំបួន", "ហាសិប",       "ហាសិបមួយ",     "ហាសិបពីរ",    "ហាសិបបី",      "ហាសិបបួន",    "ហាសិបប្រាំ",
    "ហាសិបប្រាំមួយ", "ហាសិបប្រាំពីរ",  "ហាសិបប្រាំបី",   "ហាសិបប្រាំបួន"};
constexpr std::array<std::string_view, 60> lao = {
    "ສູນ",       "ໜຶ່ງ",       "ສອງ",     "ສາມ",     "ສີ່",       "ຫ້າ",       "ຫົກ",       "ເຈັດ",
    "ແປດ",      "ເກົ້າ",      "ສິບ",      "ສິບເອັດ",   "ສິບສອງ",   "ສິບສາມ",    "ສິບສີ່",      "ສິບຫ້າ",
    "ສິບຫົກ",     "ສິບເຈັດ",    "ສິບແປດ",   "ສິບເກົ້າ",   "ຊາວ",     "ຊາວເອັດ",   "ຊາວສອງ",   "ຊາວສາມ",
    "ຊາວສີ່",     "ຊາວຫ້າ",    "ຊາວຫົກ",   "ຊາວເຈັດ",  "ຊາວແປດ",  "ຊາວເກົ້າ",   "ສາມສິບ",    "ສາມສິບເອັດ",
    "ສາມສິບສອງ", "ສາມສິບສາມ", "ສາມສິບສີ່",  "ສາມສິບຫ້າ", "ສາມສິບຫົກ", "ສາມສິບເຈັດ", "ສາມສິບແປດ", "ສາມສິບເກົ້າ",
    "ສີ່ສິບ",      "ສີ່ສິບເອັດ",   "ສີ່ສິບສອງ",  "ສີ່ສິບສາມ",  "ສີ່ສິບສີ່",    "ສີ່ສິບຫ້າ",    "ສີ່ສິບຫົກ",    "ສີ່ສິບເຈັດ",
    "ສີ່ສິບແປດ",   "ສີ່ສິບເກົ້າ",   "ຫ້າສິບ",    "ຫ້າສິບເອັດ", "ຫ້າສິບສອງ", "ຫ້າສິບສາມ",  "ຫ້າສິບສີ່",    "ຫ້າສິບຫ້າ",
    "ຫ້າສິບຫົກ",   "ຫ້າສິບເຈັດ",  "ຫ້າສິບແປດ", "ຫ້າສິບເກົ້າ"};
constexpr std::array<std::string_view, 30> sundanese = {"nol",
                                                        "hiji",
                                                        "dua",
                                                        "tilu",
                                                        "opat",
                                                        "lima",
                                                        "genep",
                                                        "tujuh",
                                                        "dalapan",
                                                        "salapan",
                                                        "sapuluh",
                                                        "sabelas",
                                                        "dua belas",
                                                        "tilu belas",
                                                        "opat belas",
                                                        "lima belas",
                                                        "genep belas",
                                                        "tujuh belas",
                                                        "dalapan belas",
                                                        "salapan belas",
                                                        "dua puluh",
                                                        "dua puluh hiji",
                                                        "dua puluh dua",
                                                        "dua puluh tilu",
                                                        "dua puluh opat",
                                                        "dua puluh lima",
                                                        "dua puluh genep",
                                                        "dua puluh tujuh",
                                                        "dua puluh dalapan",
                                                        "dua puluh salapan"};
constexpr std::array<std::string_view, 20> cebuano_small = {
    "sero",    "uno",   "dos",       "tres",       "kwatro",    "singko",    "sais",
    "syete",   "otso",  "noybe",     "diyes",      "onse",      "dose",      "trese",
    "katorse", "kinse", "dise sais", "dise syete", "dise otso", "dise noybe"};
constexpr std::array<std::string_view, 6> cebuano_tens = {"",        "",         "baynte",
                                                          "traynta", "kwarenta", "singkwenta"};
constexpr std::array<std::string_view, 30> javanese = {
    "nol",      "siji",     "loro",       "telu",      "papat",     "lima",
    "enem",     "pitu",     "wolu",       "sanga",     "sepuluh",   "sewelas",
    "rolas",    "telulas",  "patbelas",   "limalas",   "nembelas",  "pitulas",
    "wolulas",  "sangalas", "rong puluh", "selikur",   "rolikur",   "telulikur",
    "patlikur", "selawe",   "nemlikur",   "pitulikur", "wolulikur", "sangalikur"};

std::string burmese_number(int n) {
  if (n < 20)
    return word(burmese_small, n);
  return word(burmese_small, n / 10) + (n % 10 == 0 ? "ဆယ်" : "ဆယ့်") +
         (n % 10 == 0 ? std::string() : word(burmese_small, n % 10));
}

std::string cebuano_number(int n) {
  if (n < 20)
    return word(cebuano_small, n);
  return word(cebuano_tens, n / 10) +
         (n % 10 == 0 ? std::string() : (n < 30 ? " " : " y ") + word(cebuano_small, n % 10));
}
}

std::string format_burmese(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = burmese_number(t.hour) + "နာရီ";
  if (minute == 0)
    return hour;
  if (minute == 30)
    return hour + "ခွဲ";
  if (minute >= 20 && minute % 10 == 0)
    return hour + "မိနစ်" + burmese_number(minute);
  return hour + burmese_number(minute) + "မိနစ်";
}

std::string format_cebuano(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = t.hour == 1 ? std::string("ala una") : "alas " + cebuano_number(t.hour);
  if (minute == 0)
    return hour;
  if (minute == 30)
    return hour + " y medya";
  return hour + " y " + cebuano_number(minute);
}

std::string format_javanese(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return "jam " + word(javanese, t.hour);
  if (minute == 30)
    return "jam setengah " + word(javanese, t.next);
  const int n = minute < 30 ? minute : 60 - minute;
  const auto part = n == 15 ? std::string("seprapat") : word(javanese, n) + " menit";
  if (minute < 30)
    return "jam " + word(javanese, t.hour) + " luwih " + part;
  return "jam kurang " + part + " " + word(javanese, t.next);
}

std::string format_khmer(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = "ម៉ោង" + word(khmer, t.hour);
  if (minute == 0)
    return hour;
  if (minute == 30)
    return hour + "កន្លះ";
  return hour + " " + word(khmer, minute) + "នាទី";
}

std::string format_lao(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = word(lao, t.hour) + "ໂມງ";
  if (minute == 0)
    return hour;
  if (minute == 30)
    return hour + "ເຄິ່ງ";
  return hour + word(lao, minute);
}

std::string format_sundanese(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return "tabuh " + word(sundanese, t.hour);
  if (minute == 30)
    return "tabuh satengah " + word(sundanese, t.next);
  if (minute < 30)
    return "tabuh " + word(sundanese, t.hour) + " langkung " + word(sundanese, minute) + " menit";
  return "tabuh " + word(sundanese, t.next) + " kirang " + word(sundanese, 60 - minute) + " menit";
}
}
