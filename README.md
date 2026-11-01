[![Android](https://img.shields.io/badge/Android-26%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)   [![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)   [![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)   [![License](https://img.shields.io/badge/License-GPL--3.0-blue?style=flat-square)](LICENSE)

```text
    _    _      ____            _          _
   / \  (_)_ __|  _ \ ___  ___ | |__   ___| |_
  / _ \ | | '__| |_) / _ \/ _ \| '_ \ / _ \ __|
 / ___ \| | |  |  _ <  __/ (_) | |_) |  __/ |_
/_/   \_\_|_|  |_| \_\___|\___/|_.__/ \___|\__|
             Air Alert Map · Android
```

Android-приложение с интерактивной картой официальных повітряних тривог по областям и районам Украины. Интерфейс построен на Jetpack Compose и Material 3.

### 💭 Возможности

- 🗺️ **Интерактивная карта** — контуры областей и районов, панорамирование, масштабирование и быстрый сброс масштаба
- 🚨 **Статусы по территориям** — области и районы с активными тревогами подсвечиваются на карте
- 🎨 **Material 3 Expressive** — тёмная палитра, выразительные карточки и скруглённые формы; доступна светлая тема карты
- 🔄 **Автообновление** — статусы запрашиваются каждые 5 секунд
- 📶 **Состояние соединения** — приложение показывает, когда источник данных недоступен
- 🌐 **Украинские названия** — подписи областей и основных городов отображаются на карте

> Приложение показывает только статусы тревог по регионам. Оно не отображает координаты, маршруты или перемещения целей.

---

### 🧰 Технологии

**Приложение**

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-UI-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material_3-Expressive-6750A4?style=flat-square)
![Android SDK](https://img.shields.io/badge/min_SDK-26-3DDC84?style=flat-square&logo=android&logoColor=white)

**Данные и сеть**

![Retrofit](https://img.shields.io/badge/Retrofit-2.11-48B983?style=flat-square)
![OkHttp](https://img.shields.io/badge/OkHttp-4.12-3E4348?style=flat-square)
![Moshi](https://img.shields.io/badge/Moshi-JSON-59666C?style=flat-square)
![Coroutines](https://img.shields.io/badge/Kotlin_Coroutines-1.8-7F52FF?style=flat-square)

---

### 🗂️ Структура проекта

```text
app/src/main/
├── java/com/yaroslav/airalertmap/
│   ├── MainActivity.kt             — Compose UI и экран приложения
│   ├── data/
│   │   ├── AlertsRepository.kt     — периодический опрос статусов тревог
│   │   ├── DimapApiService.kt      — endpoint провайдера ДІМАП
│   │   ├── GeoJsonLoader.kt        — загрузка границ областей и районов
│   │   ├── NetworkModule.kt        — HTTP-клиент, Retrofit и Moshi
│   │   └── model/                  — модели тревог и полигонов карты
│   └── ui/
│       ├── AlertMap.kt             — отрисовка карты и управление масштабом
│       ├── MapViewModel.kt         — состояние карты и обновление данных
│       └── theme/Theme.kt          — Material 3 Expressive тема
├── res/                            — строки, тема Android и иконки
└── AndroidManifest.xml

docs/                               — сайт проекта для GitHub Pages
.github/workflows/build-apk.yml     — сборка подписанного APK в Actions
.github/workflows/pages.yml         — публикация сайта
```

---

### 🚀 Источники данных

- **ДІМАП polling API** — `https://api.dimap.live/api/polling`; используются статусы тревог по областям и районам.
- **NEPTUN GeoJSON** — геометрия границ регионов для отображения на карте.

Данные регулярно обновляются. При отсутствии сети приложение оставляет на экране последнее полученное состояние и показывает статус соединения.

---

### 📦 Локальная сборка

Открой проект в Android Studio с установленным Android SDK или используй Gradle Wrapper.

**Windows**

```powershell
.\gradlew.bat assembleDebug
```

**Linux / macOS**

```bash
bash ./gradlew assembleDebug
```

Debug APK появится в `app/build/outputs/apk/debug/`.

---

### 🔐 Подписанный APK из GitHub Actions

Workflow `Build signed APK` запускается при push в `main`, если изменились Android-приложение или файлы сборки. Изменения только в README и `docs/` не запускают APK-сборку.

Подпись использует GitHub Actions secrets:

| Secret | Назначение |
|---|---|
| `KEYSTORE_BASE64` | Keystore-файл в Base64 |
| `KEYSTORE_PASSWORD` | Пароль keystore |
| `KEY_ALIAS` | Alias ключа подписи |
| `KEY_PASSWORD` | Пароль ключа |

Чтобы получить Base64 keystore в PowerShell:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\path\to\release.jks"))
```

Добавь значения в **Repository Settings → Secrets and variables → Actions**. Сам файл keystore и секреты нельзя добавлять в репозиторий.

После успешной сборки скачай APK из **Actions → Build signed APK → последний успешный запуск → Artifacts**. Артефакт доступен 30 дней. Workflow проверяет подпись APK до загрузки и не создаёт GitHub Release.

Для локальной release-сборки задайте переменные окружения `AIRALERT_KEYSTORE_PATH`, `AIRALERT_KEYSTORE_PASSWORD`, `AIRALERT_KEY_ALIAS` и `AIRALERT_KEY_PASSWORD`, затем запустите:

```powershell
.\gradlew.bat assembleRelease
```

---

### 🌐 Ссылки

- [Сайт проекта](https://fan-world-me.github.io/airalertmap/)
- [GitHub](https://github.com/fan-world-me/airalertmap)
- [Releases](https://github.com/fan-world-me/airalertmap/releases)
- [Сборки APK и artifacts](https://github.com/fan-world-me/airalertmap/actions/workflows/build-apk.yml)
- [Сообщить о проблеме](https://github.com/fan-world-me/airalertmap/issues)

---

### ⚠️ Важно

Приложение не является единственным источником информации о безопасности. Во время тревоги ориентируйтесь на официальные оповещения и указания местных властей.

---

### 📄 Лицензия

[GNU GPL v3.0](LICENSE)

---

Made with 🩵 in Ukraine 🇺🇦

[![Telegram](https://img.shields.io/badge/Telegram-@fan__world__me-2CA5E0?style=flat-square&logo=telegram)](https://t.me/fan_world_me)   [![GitHub](https://img.shields.io/badge/GitHub-fan--world--me-181717?style=flat-square&logo=github)](https://github.com/fan-world-me)   [![Portfolio](https://img.shields.io/badge/Portfolio-fan--world--me.github.io-00e5ff?style=flat-square&logo=githubpages&logoColor=white)](https://fan-world-me.github.io/)
