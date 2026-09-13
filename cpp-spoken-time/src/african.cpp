#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"

namespace spoken_time {
using namespace detail;
namespace {
constexpr std::array<std::string_view, 30> afrikaans = {"nul",
                                                        "een",
                                                        "twee",
                                                        "drie",
                                                        "vier",
                                                        "vyf",
                                                        "ses",
                                                        "sewe",
                                                        "agt",
                                                        "nege",
                                                        "tien",
                                                        "elf",
                                                        "twaalf",
                                                        "dertien",
                                                        "veertien",
                                                        "vyftien",
                                                        "sestien",
                                                        "sewentien",
                                                        "agtien",
                                                        "negentien",
                                                        "twintig",
                                                        "een-en-twintig",
                                                        "twee-en-twintig",
                                                        "drie-en-twintig",
                                                        "vier-en-twintig",
                                                        "vyf-en-twintig",
                                                        "ses-en-twintig",
                                                        "sewe-en-twintig",
                                                        "agt-en-twintig",
                                                        "nege-en-twintig"};
constexpr std::array<std::string_view, 10> amharic_units = {"ባዶ",   "አንድ",  "ሁለት", "ሦስት",  "አራት",
                                                            "አምስት", "ስድስት", "ሰባት", "ስምንት", "ዘጠኝ"};
constexpr std::array<std::string_view, 6> amharic_tens = {"", "አስር", "ሃያ", "ሰላሳ", "አርባ", "ሃምሳ"};
constexpr std::array<std::string_view, 10> hausa_units = {
    "sifili", "ɗaya", "biyu", "uku", "huɗu", "biyar", "shida", "bakwai", "takwas", "tara"};
constexpr std::array<std::string_view, 6> hausa_tens = {"",        "goma",    "ashirin",
                                                        "talatin", "arba'in", "hamsin"};
constexpr std::array<std::string_view, 10> igbo_units = {"efu", "otu",  "abụọ", "atọ",   "anọ",
                                                         "ise", "isii", "asaa", "asatọ", "itoolu"};
constexpr std::array<std::string_view, 10> oromo_units = {
    "zeeroo", "tokko", "lama", "sadii", "afur", "shan", "jaha", "torba", "saddeet", "sagal"};
constexpr std::array<std::string_view, 6> oromo_tens = {"",        "kudhan",   "digdama",
                                                        "soddoma", "afurtama", "shantama"};
constexpr std::array<std::string_view, 6> oromo_stems = {"",         "kudha",     "digdamii",
                                                         "soddomii", "afurtamii", "shantamii"};
constexpr std::array<std::string_view, 10> somali_units = {
    "eber", "kow", "laba", "saddex", "afar", "shan", "lix", "toddoba", "siddeed", "sagaal"};
constexpr std::array<std::string_view, 6> somali_tens = {"",       "toban",   "labaatan",
                                                         "soddon", "afartan", "konton"};
constexpr std::array<std::string_view, 13> somali_hours = {
    "",         "kowdii",           "labadii",          "saddexdii",  "afartii",
    "shantii",  "lixdii",           "toddobadii",       "siddeeddii", "sagaalkii",
    "tobankii", "kow iyo tobankii", "laba iyo tobankii"};
constexpr std::array<std::string_view, 30> yoruba = {"òdo",
                                                     "kan",
                                                     "méjì",
                                                     "mẹ́ta",
                                                     "mẹ́rin",
                                                     "márùn-ún",
                                                     "mẹ́fà",
                                                     "méje",
                                                     "mẹ́jọ",
                                                     "mẹ́sàn-án",
                                                     "mẹ́wàá",
                                                     "mọ́kànlá",
                                                     "méjìlá",
                                                     "mẹ́tàlá",
                                                     "mẹ́rìnlá",
                                                     "mẹ́ẹ̀dógún",
                                                     "mẹ́rìndínlógún",
                                                     "mẹ́tàdínlógún",
                                                     "méjìdínlógún",
                                                     "mọ́kàndínlógún",
                                                     "ogún",
                                                     "mọ́kànlélógún",
                                                     "méjìlélógún",
                                                     "mẹ́tàlélógún",
                                                     "mẹ́rìnlélógún",
                                                     "márùndínlọ́gbọ̀n",
                                                     "mẹ́rìndínlọ́gbọ̀n",
                                                     "mẹ́tàdínlọ́gbọ̀n",
                                                     "méjìdínlọ́gbọ̀n",
                                                     "mọ́kàndínlọ́gbọ̀n"};
constexpr std::array<std::string_view, 13> zulu_hours = {
    "",          "lokuqala",      "lesibili",       "lesithathu",         "lesine",
    "lesihlanu", "lesithupha",    "lesikhombisa",   "lesishiyagalombili", "lesishiyagalolunye",
    "leshumi",   "leshumi nanye", "leshumi nambili"};
constexpr std::array<std::string_view, 30> zulu_minutes = {
    "",
    "umzuzu owodwa",
    "imizuzu emibili",
    "imizuzu emithathu",
    "imizuzu emine",
    "imizuzu emihlanu",
    "imizuzu eyisithupha",
    "imizuzu eyisikhombisa",
    "imizuzu eyisishiyagalombili",
    "imizuzu eyisishiyagalolunye",
    "imizuzu eyishumi",
    "imizuzu eyishumi nanye",
    "imizuzu eyishumi nambili",
    "imizuzu eyishumi nantathu",
    "imizuzu eyishumi nane",
    "imizuzu eyishumi nanhlanu",
    "imizuzu eyishumi nesithupha",
    "imizuzu eyishumi nesikhombisa",
    "imizuzu eyishumi nesishiyagalombili",
    "imizuzu eyishumi nesishiyagalolunye",
    "imizuzu engamashumi amabili",
    "imizuzu engamashumi amabili nanye",
    "imizuzu engamashumi amabili nambili",
    "imizuzu engamashumi amabili nantathu",
    "imizuzu engamashumi amabili nane",
    "imizuzu engamashumi amabili nanhlanu",
    "imizuzu engamashumi amabili nesithupha",
    "imizuzu engamashumi amabili nesikhombisa",
    "imizuzu engamashumi amabili nesishiyagalombili",
    "imizuzu engamashumi amabili nesishiyagalolunye"};

std::string amharic_number(int n) {
  if (n < 10)
    return word(amharic_units, n);
  if (n % 10 == 0)
    return word(amharic_tens, n / 10);
  return (n < 20 ? std::string("አስራ") : word(amharic_tens, n / 10)) + " " +
         word(amharic_units, n % 10);
}

std::string hausa_number(int n) {
  if (n < 10)
    return word(hausa_units, n);
  return word(hausa_tens, n / 10) +
         (n % 10 == 0 ? std::string() : (n < 20 ? " sha " : " da ") + word(hausa_units, n % 10));
}

std::string igbo_number(int n) {
  if (n < 10)
    return word(igbo_units, n);
  return std::string("iri") + (n < 20 ? std::string() : " " + word(igbo_units, n / 10)) +
         (n % 10 == 0 ? std::string() : " na " + word(igbo_units, n % 10));
}

std::string oromo_number(int n) {
  if (n < 10)
    return word(oromo_units, n);
  if (n % 10 == 0)
    return word(oromo_tens, n / 10);
  return word(oromo_stems, n / 10) + " " + word(oromo_units, n % 10);
}

std::string somali_number(int n) {
  if (n < 10)
    return word(somali_units, n);
  return (n % 10 == 0 ? std::string() : word(somali_units, n % 10) + " iyo ") +
         word(somali_tens, n / 10);
}
}

std::string format_afrikaans(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return word(afrikaans, t.hour) +
           (t.hour == 2 || t.hour == 3 || t.hour == 7 || t.hour == 9 ? "-uur" : "uur");
  if (minute == 30)
    return "half " + word(afrikaans, t.next);
  const int n = minute < 30 ? minute : 60 - minute;
  return (n == 15 ? std::string("kwart") : word(afrikaans, n)) +
         (minute < 30 ? " oor " : " voor ") + word(afrikaans, minute < 30 ? t.hour : t.next);
}

std::string format_amharic(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const int h = (t.hour + 5) % 12 + 1;
  const auto hour = amharic_number(h) + " ሰዓት";
  if (minute == 0)
    return hour;
  if (minute == 15)
    return hour + " ከሩብ";
  if (minute == 30)
    return hour + " ተኩል";
  if (minute > 30)
    return "ለ" + amharic_number(h % 12 + 1) + " ሰዓት " +
           (minute == 45 ? std::string("ሩብ") : amharic_number(60 - minute) + " ደቂቃ") + " ጉዳይ";
  auto count = amharic_number(minute);
  count = count.starts_with("አ") ? "ካ" + count.substr(std::string_view("አ").size()) : "ከ" + count;
  return hour + " " + count + " ደቂቃ";
}

std::string format_hausa(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = "ƙarfe " + hausa_number(t.hour);
  if (minute == 0)
    return hour;
  if (minute == 15)
    return hour + " da kwata";
  if (minute == 30)
    return hour + " da rabi";
  return hour + " da minti " + hausa_number(minute);
}

std::string format_igbo(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = [](int h) {
    return h == 1 ? std::string("otu elekere") : "elekere " + igbo_number(h);
  };
  if (minute == 0)
    return hour(t.hour);
  if (minute == 30)
    return hour(t.hour) + " na ọkara";
  if (minute < 30)
    return "nkeji " + igbo_number(minute) + " gafee " + hour(t.hour);
  return "nkeji " + igbo_number(60 - minute) + " na-aga " + hour(t.next);
}

std::string format_oromo(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = "sa'aatii " + oromo_number((t.hour + 5) % 12 + 1);
  if (minute == 0)
    return hour;
  if (minute == 15)
    return hour + " fi ruubi";
  if (minute == 30)
    return hour + " fi walakkaa";
  return hour + " fi daqiiqaa " + oromo_number(minute);
}

std::string format_somali(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = word(somali_hours, t.hour);
  if (minute == 0)
    return hour;
  if (minute == 30)
    return hour + " iyo badhkii";
  return hour + " iyo " + (minute == 1 ? std::string("hal") : somali_number(minute)) + " daqiiqo";
}

std::string format_yoruba(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = "aago " + word(yoruba, minute > 30 ? t.next : t.hour);
  if (minute == 0)
    return hour;
  if (minute == 30)
    return hour + " àbọ̀";
  return hour + (minute < 30 ? " kọjá ìṣẹ́jú " : " kù ìṣẹ́jú ") +
         word(yoruba, minute < 30 ? minute : 60 - minute);
}

std::string format_zulu(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return "ihora " + word(zulu_hours, t.hour);
  if (minute == 30)
    return "ukugamanxa kwehora " + word(zulu_hours, t.hour);
  const int n = minute < 30 ? minute : 60 - minute;
  return (n == 15 ? std::string("ikota") : word(zulu_minutes, n)) +
         (minute < 30 ? " emva kwehora " : " ngaphambi kwehora ") +
         word(zulu_hours, minute < 30 ? t.hour : t.next);
}
}
