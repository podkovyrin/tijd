#include <spoken_time/spoken_time.hpp>

#include "detail.hpp"

namespace spoken_time {
using namespace detail;
namespace {
constexpr Words hindi = {"शून्य",   "एक",    "दो",    "तीन",    "चार",    "पाँच",   "छह",   "सात",
                         "आठ",    "नौ",    "दस",    "ग्यारह",  "बारह",   "तेरह",   "चौदह", "पंद्रह",
                         "सोलह",  "सत्रह",  "अठारह", "उन्नीस",  "बीस",    "इक्कीस", "बाईस", "तेईस",
                         "चौबीस", "पच्चीस", "छब्बीस", "सत्ताईस", "अट्ठाईस", "उनतीस"};
constexpr Words urdu = {"صفر",   "ایک",  "دو",     "تین",    "چار",     "پانچ", "چھ",    "سات",
                        "آٹھ",   "نو",   "دس",     "گیارہ",  "بارہ",    "تیرہ", "چودہ",  "پندرہ",
                        "سولہ",  "سترہ", "اٹھارہ", "انیس",   "بیس",     "اکیس", "بائیس", "تئیس",
                        "چوبیس", "پچیس", "چھبیس",  "ستائیس", "اٹھائیس", "انتیس"};
constexpr Words punjabi = {"ਸਿਫ਼ਰ", "ਇੱਕ",    "ਦੋ",     "ਤਿੰਨ",   "ਚਾਰ",  "ਪੰਜ",  "ਛੇ",   "ਸੱਤ",
                           "ਅੱਠ",   "ਨੌਂ",     "ਦਸ",    "ਗਿਆਰਾਂ", "ਬਾਰਾਂ", "ਤੇਰਾਂ", "ਚੌਦਾਂ", "ਪੰਦਰਾਂ",
                           "ਸੋਲਾਂ",  "ਸਤਾਰਾਂ", "ਅਠਾਰਾਂ", "ਉੱਨੀ",   "ਵੀਹ",  "ਇੱਕੀ", "ਬਾਈ", "ਤੇਈ",
                           "ਚੌਵੀ",  "ਪੱਚੀ",   "ਛੱਬੀ",   "ਸਤਾਈ",  "ਅਠਾਈ", "ਉਨੱਤੀ"};
constexpr Words bengali = {"শূন্য",   "এক",    "দুই",     "তিন",   "চার",  "পাঁচ",   "ছয়",   "সাত",
                           "আট",    "নয়",    "দশ",     "এগারো", "বারো", "তেরো",  "চৌদ্দ", "পনেরো",
                           "ষোলো",  "সতেরো", "আঠারো",  "উনিশ",  "বিশ",  "একুশ",   "বাইশ", "তেইশ",
                           "চব্বিশ", "পঁচিশ",  "ছাব্বিশ", "সাতাশ", "আটাশ", "ঊনত্রিশ"};

std::string tamil_number(int n) {
  constexpr std::array<std::string_view, 20> small = {
      "பூஜ்ஜியம்",  "ஒன்று",     "இரண்டு",   "மூன்று",   "நான்கு",    "ஐந்து",     "ஆறு",
      "ஏழு",      "எட்டு",     "ஒன்பது",   "பத்து",    "பதினொன்று", "பன்னிரண்டு", "பதின்மூன்று",
      "பதினான்கு", "பதினைந்து", "பதினாறு", "பதினேழு", "பதினெட்டு", "பத்தொன்பது"};
  constexpr std::array<std::string_view, 6> tens = {"", "", "இருபது", "முப்பது", "நாற்பது", "ஐம்பது"};
  constexpr std::array<std::string_view, 6> stems = {"",        "",        "இருபத்து",
                                                     "முப்பத்து", "நாற்பத்து", "ஐம்பத்து"};
  if (n < 20)
    return word(small, n);
  if (n % 10 == 0)
    return word(tens, n / 10);
  return word(stems, n / 10) + " " + word(small, n % 10);
}

std::string telugu_number(int n) {
  constexpr std::array<std::string_view, 20> small = {
      "సున్నా",    "ఒకటి",   "రెండు",  "మూడు",  "నాలుగు",  "ఐదు",    "ఆరు",
      "ఏడు",     "ఎనిమిది",  "తొమ్మిది",  "పది",    "పదకొండు", "పన్నెండు", "పదమూడు",
      "పద్నాలుగు", "పదిహేను", "పదహారు", "పదిహేడు", "పద్దెనిమిది", "పంతొమ్మిది"};
  constexpr std::array<std::string_view, 6> tens = {"", "", "ఇరవై", "ముప్పై", "నలభై", "యాభై"};
  if (n < 20)
    return word(small, n);
  return word(tens, n / 10) + (n % 10 == 0 ? std::string() : " " + word(small, n % 10));
}
}

std::string format_bengali(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = [](int h) { return word(bengali, h) + "টা"; };
  if (minute == 0)
    return hour(t.hour);
  if (minute == 15)
    return "সোয়া " + hour(t.hour);
  if (minute == 30) {
    if (t.hour == 1)
      return "দেড়টা";
    if (t.hour == 2)
      return "আড়াইটা";
    return "সাড়ে " + hour(t.hour);
  }
  if (minute == 45)
    return "পৌনে " + hour(t.next);
  if (minute < 30)
    return hour(t.hour) + " " + word(bengali, minute) + " মিনিট";
  return hour(t.next) + " বাজতে " + word(bengali, 60 - minute) + " মিনিট";
}

std::string format_hindi(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return word(hindi, t.hour) + (t.hour == 1 ? " बजा" : " बजे");
  if (minute == 15)
    return "सवा " + word(hindi, t.hour);
  if (minute == 30) {
    if (t.hour == 1)
      return "डेढ़";
    if (t.hour == 2)
      return "ढाई";
    return "साढ़े " + word(hindi, t.hour);
  }
  if (minute == 45)
    return "पौने " + word(hindi, t.next);
  if (minute < 30)
    return word(hindi, t.hour) + " बजकर " + word(hindi, minute) + " मिनट";
  return word(hindi, t.next) + " बजने में " + word(hindi, 60 - minute) + " मिनट";
}

std::string format_punjabi(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return word(punjabi, t.hour) + (t.hour == 1 ? " ਵੱਜਾ" : " ਵੱਜੇ");
  if (minute == 15)
    return "ਸਵਾ " + word(punjabi, t.hour);
  if (minute == 30) {
    if (t.hour == 1)
      return "ਡੇਢ";
    if (t.hour == 2)
      return "ਢਾਈ";
    return "ਸਾਢੇ " + word(punjabi, t.hour);
  }
  if (minute == 45)
    return "ਪੌਣੇ " + word(punjabi, t.next);
  if (minute < 30)
    return word(punjabi, t.hour) + " ਵੱਜ ਕੇ " + word(punjabi, minute) + " ਮਿੰਟ";
  return word(punjabi, t.next) + " ਵੱਜਣ ਵਿੱਚ " + word(punjabi, 60 - minute) + " ਮਿੰਟ";
}

std::string format_tamil(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = (t.hour == 1 ? std::string("ஒரு") : tamil_number(t.hour)) + " மணி";
  if (minute == 0)
    return hour;
  const auto count = minute == 1 ? std::string("ஒரு") : tamil_number(minute);
  return hour + " " + count + " நிமிடம்";
}

std::string format_telugu(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  const auto hour = t.hour == 1 ? std::string("ఒంటి గంట")
                                : telugu_number(t.hour) + (minute == 0 ? " గంటలు" : " గంటల");
  if (minute == 0)
    return hour;
  if (minute == 1)
    return hour + " ఒక నిమిషం";
  return hour + " " + telugu_number(minute) + " నిమిషాలు";
}

std::string format_urdu(int hour_of_day, int minute) {
  const Time t(hour_of_day, minute);
  if (minute == 0)
    return word(urdu, t.hour) + (t.hour == 1 ? " بجا" : " بجے");
  if (minute == 15)
    return "سوا " + word(urdu, t.hour);
  if (minute == 30) {
    if (t.hour == 1)
      return "ڈیڑھ";
    if (t.hour == 2)
      return "ڈھائی";
    return "ساڑھے " + word(urdu, t.hour);
  }
  if (minute == 45)
    return "پونے " + word(urdu, t.next);
  if (minute < 30)
    return word(urdu, t.hour) + " بج کر " + word(urdu, minute) + " منٹ";
  return word(urdu, t.next) + " بجنے میں " + word(urdu, 60 - minute) + " منٹ";
}
}
