# Air Alert Map

Android-приложение на Jetpack Compose для отображения официальных тревог по областям и районам.

## Что умеет

- Отображает официальные тревоги по областям и районам.
- Использует автоматическое обновление данных каждые 5 секунд.
- Сохраняет последний успешный экран при временной потере сети.
- Поддерживает масштабирование карты и цветовое выделение областей и районов с тревогой.
- Имеет отдельную статическую страницу проекта в `docs/` для GitHub Pages.

## Источники данных

- ДІМАП polling API: `https://api.dimap.live/api/polling` — используются только статусы тревог по территориям.
- NEPTUN GeoJSON: границы областей и районов для карты.

## Ссылки в настройках приложения

В разделе настроек сейчас есть такие ссылки:

- Сайт проекта
- GitHub проекта
- Скачать актуальную версию
- Поддержать через GitHub Sponsors

## Сборка

Проект рассчитан на Android Studio / Gradle на Windows.

1. Открой корень проекта в Android Studio.
2. Убедись, что `local.properties` указывает на установленный Android SDK.
3. Запусти `assembleDebug` или `assembleRelease`.

Если нужен ручной запуск из терминала:

```powershell
.\gradlew.bat assembleDebug
```

### Подпись и публикация release APK

Ключи не хранятся в проекте. Workflow `Release APK` собирает и публикует APK
при каждом push в `main`. Версия автоматически увеличивается в формате `1.0.N`;
APK добавляется в GitHub Release с тегом `v1.0.N`.

Чтобы включить публикацию, добавь в **Settings → Secrets and variables → Actions**
репозитория следующие Actions secrets:

- `KEYSTORE_BASE64` — keystore-файл в Base64.
- `KEYSTORE_PASSWORD` — пароль хранилища.
- `KEY_ALIAS` — alias ключа.
- `KEY_PASSWORD` — пароль ключа.

Для создания Base64-значения в PowerShell используй локальный файл keystore:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\secure\airalert-release.jks"))
```

Скопируй вывод непосредственно в secret `KEYSTORE_BASE64`; не добавляй
keystore или его содержимое в репозиторий. Пока secrets не настроены, workflow
остановится с сообщением об отсутствующей подписи, не создавая неподписанный релиз.

## Подпись и gitignore

Секреты подписи и локальные файлы сборки не должны попадать в репозиторий:

- `*.jks`
- `keystore.properties`
- `local.properties`
- `build/`
- `.gradle/`

## Публикация

Сайт проекта лежит в `docs/` и деплоится через GitHub Pages workflow:

- [Docs site](https://fan-world-me.github.io/airalertmap/)
- [GitHub repository](https://github.com/fan-world-me/airalertmap)
- [Latest release](https://github.com/fan-world-me/airalertmap/releases/latest)

## Примечание

Приложение показывает региональные статусы тревог, но не является единственным источником информации о безопасности. Во время тревог ориентируйся на официальные сообщения и указания местных властей.
