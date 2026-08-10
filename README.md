# Air Alert Map

Android-приложение на Jetpack Compose для отображения повітряних загроз и официальных тревог NEPTUN на карте.

## Что умеет

- Показує активні загрози на мапі.
- Отображает официальные тревоги по областям и районам.
- Поддерживает фильтры по типам целей.
- Показывает детали цели по нажатию на маркер.
- Использует автоматическое обновление данных каждые 5 секунд.
- Сохраняет последний успешный экран при временной потере сети.
- Имеет отдельную статическую страницу проекта в `docs/` для GitHub Pages.

## Источники данных

- NEPTUN API: `https://neptun.in.ua/`
- OpenStreetMap: используется как картографическая основа в UI

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

Проект использует открытый API NEPTUN и не является единственным источником информации о безопасности. Во время тревог ориентируйся на официальные сообщения и указания местных властей.
