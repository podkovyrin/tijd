// SPDX-FileCopyrightText: 2026 Andrei Podkovyrin
// SPDX-License-Identifier: MIT

#include <jni.h>
#include <spoken_time/spoken_time.hpp>

#include <exception>
#include <string>

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_podkovyrin_tijd_SpokenTime_nativeLanguages(JNIEnv* env, jobject) {
  const auto languages = spoken_time::supported_languages();
  const auto string_class = env->FindClass("java/lang/String");
  if (string_class == nullptr)
    return nullptr;
  auto result =
      env->NewObjectArray(static_cast<jsize>(languages.size() * 2), string_class, nullptr);
  env->DeleteLocalRef(string_class);
  if (result == nullptr)
    return nullptr;
  jsize index = 0;
  for (const auto& language : languages) {
    for (auto value : {language.code, language.name}) {
      auto entry = env->NewStringUTF(std::string(value).c_str());
      if (entry == nullptr)
        return nullptr;
      env->SetObjectArrayElement(result, index++, entry);
      env->DeleteLocalRef(entry);
      if (env->ExceptionCheck())
        return nullptr;
    }
  }
  return result;
}

extern "C" JNIEXPORT jbyteArray JNICALL Java_com_podkovyrin_tijd_SpokenTime_nativeFormat(
    JNIEnv* env, jobject, jint index, jint hour, jint minute) {
  const auto languages = spoken_time::supported_languages();
  if (index < 0 || static_cast<std::size_t>(index) >= languages.size() || hour < 0 || hour > 23 ||
      minute < 0 || minute > 59) {
    env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), "Invalid language or time");
    return nullptr;
  }
  try {
    const auto text =
        spoken_time::format(languages[static_cast<std::size_t>(index)].language, hour, minute);
    // Return standard UTF-8 bytes, not JNI's modified UTF-8 encoding.
    auto result = env->NewByteArray(static_cast<jsize>(text.size()));
    if (result != nullptr) {
      env->SetByteArrayRegion(result, 0, static_cast<jsize>(text.size()),
                              reinterpret_cast<const jbyte*>(text.data()));
    }
    return result;
  } catch (const std::exception&) {
    env->ThrowNew(env->FindClass("java/lang/IllegalStateException"),
                  "Unable to format spoken time");
    return nullptr;
  }
}
