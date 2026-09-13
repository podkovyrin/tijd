#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"

namespace spoken_time {
using namespace detail;
namespace {
constexpr std::array<std::string_view, 60> gujarati = {
    "શૂન્ય",     "એક",      "બે",      "ત્રણ",     "ચાર",     "પાંચ",      "છ",      "સાત",
    "આઠ",      "નવ",      "દસ",     "અગિયાર",  "બાર",     "તેર",       "ચૌદ",    "પંદર",
    "સોળ",     "સત્તર",    "અઢાર",   "ઓગણીસ",   "વીસ",     "એકવીસ",    "બાવીસ",  "તેવીસ",
    "ચોવીસ",   "પચ્ચીસ",   "છવીસ",   "સત્તાવીસ", "અઠ્ઠાવીસ", "ઓગણત્રીસ",  "ત્રીસ",   "એકત્રીસ",
    "બત્રીસ",   "તેત્રીસ",   "ચોત્રીસ", "પાંત્રીસ",  "છત્રીસ",   "સડત્રીસ",   "અડત્રીસ", "ઓગણચાલીસ",
    "ચાલીસ",   "એકતાલીસ", "બેતાલીસ", "ત્રેતાલીસ", "ચુંમાલીસ",  "પિસ્તાલીસ", "છેતાલીસ", "સુડતાલીસ",
    "અડતાલીસ", "ઓગણપચાસ", "પચાસ",   "એકાવન",   "બાવન",    "ત્રેપન",     "ચોપન",   "પંચાવન",
    "છપ્પન",    "સત્તાવન",  "અઠ્ઠાવન", "ઓગણસાઠ"};
constexpr std::array<std::string_view, 20> kannada_small = {
    "ಸೊನ್ನೆ",    "ಒಂದು",   "ಎರಡು",   "ಮೂರು",   "ನಾಲ್ಕು",   "ಐದು",      "ಆರು",
    "ಏಳು",     "ಎಂಟು",   "ಒಂಬತ್ತು", "ಹತ್ತು",   "ಹನ್ನೊಂದು", "ಹನ್ನೆರಡು",   "ಹದಿಮೂರು",
    "ಹದಿನಾಲ್ಕು", "ಹದಿನೈದು", "ಹದಿನಾರು", "ಹದಿನೇಳು", "ಹದಿನೆಂಟು",  "ಹತ್ತೊಂಬತ್ತು"};
constexpr std::array<std::string_view, 6> kannada_tens = {"",       "",       "ಇಪ್ಪತ್ತು",
                                                          "ಮೂವತ್ತು", "ನಲವತ್ತು", "ಐವತ್ತು"};
constexpr std::array<std::string_view, 6> kannada_stems = {"",      "",      "ಇಪ್ಪತ್ತ",
                                                           "ಮೂವತ್ತ", "ನಲವತ್ತ", "ಐವತ್ತ"};
constexpr std::array<std::string_view, 10> kannada_endings = {
    "", "ೊಂದು", "ೆರಡು", "ಮೂರು", "ನಾಲ್ಕು", "ೈದು", "ಾರು", "ೇಳು", "ೆಂಟು", "ೊಂಬತ್ತು"};
constexpr std::array<std::string_view, 20> malayalam_small = {
    "പൂജ്യം",   "ഒന്ന്",    "രണ്ട്",    "മൂന്ന്",    "നാല്",     "അഞ്ച്",    "ആറ്",
    "ഏഴ്",     "എട്ട്",    "ഒമ്പത്",   "പത്ത്",    "പതിനൊന്ന്", "പന്ത്രണ്ട്", "പതിമൂന്ന്",
    "പതിനാല്", "പതിനഞ്ച്", "പതിനാറ്", "പതിനേഴ്", "പതിനെട്ട്", "പത്തൊമ്പത്"};
constexpr std::array<std::string_view, 6> malayalam_tens = {"",     "",      "ഇരുപത്",
                                                            "മുപ്പത്", "നാല്പത്", "അമ്പത്"};
constexpr std::array<std::string_view, 6> malayalam_stems = {"",       "",        "ഇരുപത്തി",
                                                             "മുപ്പത്തി", "നാല്പത്തി", "അമ്പത്തി"};
constexpr std::array<std::string_view, 10> malayalam_endings = {
    "", "യൊന്ന്", "രണ്ട്", "മൂന്ന്", "നാല്", "യഞ്ച്", "യാറ്", "യേഴ്", "യെട്ട്", "യൊമ്പത്"};
constexpr std::array<std::string_view, 30> marathi = {
    "शून्य", "एक",    "दोन",   "तीन",  "चार",   "पाच",   "सहा",   "सात",     "आठ",      "नऊ",
    "दहा", "अकरा",  "बारा",  "तेरा",  "चौदा",  "पंधरा",  "सोळा",  "सतरा",    "अठरा",    "एकोणीस",
    "वीस", "एकवीस", "बावीस", "तेवीस", "चोवीस", "पंचवीस", "सव्वीस", "सत्तावीस", "अठ्ठावीस", "एकोणतीस"};
constexpr std::array<std::string_view, 30> nepali = {
    "शून्य", "एक",     "दुई",   "तीन", "चार",   "पाँच",   "छ",     "सात",    "आठ",     "नौ",
    "दस",  "एघार",   "बाह्र", "तेह्र", "चौध",   "पन्ध्र",  "सोह्र",  "सत्र",    "अठार",   "उन्नाइस",
    "बीस", "एक्काइस", "बाइस", "तेइस", "चौबीस", "पच्चीस", "छब्बीस", "सत्ताइस", "अट्ठाइस", "उनन्तीस"};
constexpr std::array<std::string_view, 30> odia = {
    "ଶୂନ୍ୟ",  "ଏକ",    "ଦୁଇ",   "ତିନି",   "ଚାରି", "ପାଞ୍ଚ", "ଛଅ",   "ସାତ",   "ଆଠ",    "ନଅ",
    "ଦଶ",   "ଏଗାର",  "ବାର",  "ତେର",  "ଚଉଦ", "ପନ୍ଦର", "ଷୋହଳ", "ସତର",   "ଅଠର",   "ଉଣେଇଶ",
    "କୋଡ଼ିଏ", "ଏକୋଇଶ", "ବାଇଶି", "ତେଇଶି", "ଚବିଶି", "ପଚିଶି",  "ଛବିଶି",  "ସତେଇଶି", "ଅଠାଇଶି", "ଅଣତିରିଶି"};
constexpr std::array<std::string_view, 30> pashto = {
    "صفر",      "یو",        "دوه",     "درې",       "څلور",     "پنځه",    "شپږ",      "اووه",
    "اته",      "نهه",       "لس",      "یوولس",     "دولس",     "دیارلس",  "څوارلس",   "پنځلس",
    "شپاړس",    "اوولس",     "اتلس",    "نولس",      "شل",       "یوویشت",  "دوه ویشت", "درویشت",
    "څلورویشت", "پنځه ویشت", "شپږویشت", "اووه ویشت", "اته ویشت", "نهه ویشت"};
constexpr std::array<std::string_view, 30> shahmukhi = {
    "صفر",  "اک",     "دو",    "تِن",    "چار",   "پنج",    "چھے",   "ست",     "اٹھ",     "نَو",
    "دس",   "گیاراں", "باراں", "تیراں", "چوداں", "پندراں", "سولاں", "ستاراں", "اٹھاراں", "اُنی",
    "ویہہ", "اکی",    "بائی",  "تئی",   "چووی",  "پچی",    "چھبی",  "ستائی",  "اٹھائی",  "اُنتی"};
constexpr std::array<std::string_view, 30> sindhi = {
    "ٻڙي",    "هڪ",      "ٻه",    "ٽي",      "چار",     "پنج",    "ڇهه",    "ست",
    "اٺ",     "نو",      "ڏهه",   "يارهن",   "ٻارهن",   "تيرهن",  "چوڏهن",  "پندرهن",
    "سورهن",  "سترهن",   "ارڙهن", "اوڻيهه",  "ويهه",    "ايڪيهه", "ٻاويهه", "ٽيويهه",
    "چوويهه", "پنجويهه", "ڇويهه", "ستاويهه", "اٺاويهه", "اڻٽيهه"};
constexpr std::array<std::string_view, 20> sinhala_small = {
    "බිංදුවයි", "එකයි",    "දෙකයි",  "තුනයි",   "හතරයි",   "පහයි",    "හයයි",   "හතයි",   "අටයි",   "නවයයි",
    "දහයයි",  "එකොළහයි", "දොළහයි", "දහතුනයි", "දහහතරයි", "පහළොවයි", "දහසයයි", "දහහතයි", "දහඅටයි", "දහනවයයි"};
constexpr std::array<std::string_view, 6> sinhala_tens = {"", "", "විස්සයි", "තිහයි", "හතළිහයි", "පනහයි"};
constexpr std::array<std::string_view, 6> sinhala_stems = {"", "", "විසි", "තිස්", "හතළිස්", "පනස්"};

std::string kannada_number(int n) {
  if (n < 20)
    return word(kannada_small, n);
  if (n % 10 == 0)
    return word(kannada_tens, n / 10);
  return word(kannada_stems, n / 10) + word(kannada_endings, n % 10);
}

std::string malayalam_number(int n) {
  if (n < 20)
    return word(malayalam_small, n);
  if (n % 10 == 0)
    return word(malayalam_tens, n / 10);
  return word(malayalam_stems, n / 10) + word(malayalam_endings, n % 10);
}

std::string sinhala_number(int n) {
  if (n < 20)
    return word(sinhala_small, n);
  if (n % 10 == 0)
    return word(sinhala_tens, n / 10);
  return word(sinhala_stems, n / 10) + " " + word(sinhala_small, n % 10);
}
}

std::string format_gujarati(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return word(gujarati, t.hour) + (t.hour == 1 ? " વાગ્યો" : " વાગ્યા");
  if (minute == 15)
    return "સવા " + word(gujarati, t.hour);
  if (minute == 30) {
    if (t.hour == 1)
      return "દોઢ";
    if (t.hour == 2)
      return "અઢી";
    return "સાડા " + word(gujarati, t.hour);
  }
  if (minute == 45)
    return "પોણા " + word(gujarati, t.next);
  return word(gujarati, t.hour) + " વાગીને " + word(gujarati, minute) + " મિનિટ";
}

std::string format_kannada(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = kannada_number(t.hour) + " ಗಂಟೆ";
  if (minute == 0)
    return hour;
  return hour + " " + kannada_number(minute) + " ನಿಮಿಷ";
}

std::string format_malayalam(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = malayalam_number(t.hour);
  if (minute == 0)
    return t.hour == 1 ? std::string("ഒരു മണി") : hour + " മണി";
  return hour + " " + malayalam_number(minute);
}

std::string format_marathi(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return word(marathi, t.hour) + (t.hour == 1 ? " वाजला" : " वाजले");
  if (minute == 15)
    return "सव्वा " + word(marathi, t.hour);
  if (minute == 30) {
    if (t.hour == 1)
      return "दीड";
    if (t.hour == 2)
      return "अडीच";
    return "साडे " + word(marathi, t.hour);
  }
  if (minute == 45)
    return t.next == 1 ? std::string("पाऊण") : "पावणे " + word(marathi, t.next);
  const int n = minute < 30 ? minute : 60 - minute;
  const auto part = word(marathi, n) + (n == 1 ? " मिनिट" : " मिनिटे");
  if (minute < 30)
    return word(marathi, t.hour) + " वाजून " + part;
  return word(marathi, t.next) + " वाजायला " + part;
}

std::string format_nepali(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return word(nepali, t.hour) + " बज्यो";
  if (minute == 15)
    return "सवा " + word(nepali, t.hour);
  if (minute == 30)
    return "साढे " + word(nepali, t.hour);
  if (minute == 45)
    return "पौने " + word(nepali, t.next);
  if (minute < 30)
    return word(nepali, t.hour) + " बजेर " + word(nepali, minute) + " मिनेट";
  return word(nepali, t.next) + " बज्न " + word(nepali, 60 - minute) + " मिनेट";
}

std::string format_odia(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return word(odia, t.hour) + "ଟା";
  if (minute == 30)
    return word(odia, t.hour) + "ଟା ତିରିଶି ମିନିଟ୍";
  if (minute < 30)
    return word(odia, t.hour) + "ଟା " + word(odia, minute) + " ମିନିଟ୍";
  return word(odia, t.next) + "ଟା ବାଜିବାକୁ " + word(odia, 60 - minute) + " ମିନିଟ୍ ବାକି";
}

std::string format_pashto(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = [](int h) { return h == 1 ? std::string("یوه") : word(pashto, h); };
  if (minute == 0)
    return hour(t.hour) + (t.hour == 1 ? " بجه" : " بجې");
  if (minute == 30)
    return hour(t.hour) + " او دېرش";
  if (minute < 30)
    return hour(t.hour) + " او " + (minute == 1 ? std::string("یوه دقیقه") : word(pashto, minute));
  if (minute == 59)
    return "یوه دقیقه کمه " + hour(t.next);
  return word(pashto, 60 - minute) + " کمې " + hour(t.next);
}

std::string format_punjabi_shahmukhi(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return word(shahmukhi, t.hour) + (t.hour == 1 ? " وجّا" : " وجّے");
  if (minute == 15)
    return "سوا " + word(shahmukhi, t.hour);
  if (minute == 30) {
    if (t.hour == 1)
      return "ڈیڈھ";
    if (t.hour == 2)
      return "ڈھائی";
    return "ساڈھے " + word(shahmukhi, t.hour);
  }
  if (minute == 45)
    return "پونے " + word(shahmukhi, t.next);
  if (minute < 30)
    return word(shahmukhi, t.hour) + " وجّ کے " + word(shahmukhi, minute) + " منٹ";
  return word(shahmukhi, t.next) + " وجّن وچ " + word(shahmukhi, 60 - minute) + " منٹ";
}

std::string format_sindhi(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return word(sindhi, t.hour) + (t.hour == 1 ? " وڳو" : " وڳا");
  if (minute == 15)
    return "سوا " + word(sindhi, t.hour);
  if (minute == 30) {
    if (t.hour == 1)
      return "ڏيڍ";
    if (t.hour == 2)
      return "اڍائي";
    return "ساڍا " + word(sindhi, t.hour);
  }
  if (minute == 45)
    return "پوڻا " + word(sindhi, t.next);
  if (minute < 30)
    return word(sindhi, t.hour) + " وڄي " + word(sindhi, minute) + " منٽ";
  return word(sindhi, t.next) + " وڄڻ ۾ " + word(sindhi, 60 - minute) + " منٽ";
}

std::string format_sinhala(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return sinhala_number(t.hour);
  return sinhala_number(t.hour) + " " + sinhala_number(minute);
}
}
