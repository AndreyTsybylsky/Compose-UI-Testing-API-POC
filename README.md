# Compose Multiplatform UI Testing Experiment

Этот проект демонстрирует, как организовать кроссплатформенное UI-тестирование в **Kotlin Multiplatform (KMP)** и **Compose Multiplatform (CMP)** с использованием официального пакета **Compose UI Testing API** (`runComposeUiTest`).

Все UI-тесты написаны в директории `commonTest` (`composeApp/src/commonTest`), что позволяет запускать их на любой поддерживаемой платформе (Desktop, Android, iOS) без дублирования кода.

---

## Архитектура и Решения

### 1. Отказ от Kaspresso в пользу Compose UI Testing API
Kaspresso сильно завязан на Android (Espresso/Kakao). Здесь используется кроссплатформенный Compose Testing API.
* Вместо `KButton { click() }` используется стандартный:
  ```kotlin
  onNodeWithTag("login_button").performClick()
  ```

### 2. Запуск тестов без Xcode и живого iOS-таргета
Тесты находятся в `commonTest` и могут запускаться локально на JVM (Desktop) или на Android-эмуляторе без необходимости собирать Xcode-проект. 
Когда iOS-окружение будет готово, те же самые тесты запустятся на iOS-симуляторе.

### 3. Тестирование expect/actual и нативных компонентов
Если у вас есть платформозависимые Composable (карты, WebView и т.д.), в тестах их можно изолировать. В проекте реализован паттерн **PlatformProvider override**:
* В `commonMain` объявлен `expect fun NativeBanner()`
* В `PlatformProvider` добавлено поле `nativeBannerOverride`, позволяющее подменить реальный нативный компонент на простой `Text` заглушку во время выполнения тестов в `commonTest`.

---

## Структура проекта

* **`composeApp/src/commonMain`**: Общий UI (`App.kt`), интерфейсы платформ (`Platform.kt`) и провайдер заглушек (`PlatformProvider.kt`).
* **`composeApp/src/commonTest`**: Общие автотесты (`AppTest.kt`).
* **`composeApp/src/desktopMain`**: Реализация для Desktop JVM.
* **`composeApp/src/androidMain`**: Реализация для Android.
* **`composeApp/src/iosMain`**: Заглушка реализации для iOS.

---

## Как запускать тесты

Перед запуском убедитесь, что у вас установлен JDK 17+ и Gradle. Если у вас нет сгенерированного wrapper, выполните `gradle wrapper` в корневой папке.

### 1. На Desktop JVM (локально, быстро, без эмуляторов)
Запускает тесты на JVM в headless-режиме:
```bash
./gradlew :composeApp:desktopTest
```

### 2. На Android (Instrumented Tests на эмуляторе)
Запускает тесты на подключенном Android-эмуляторе или устройстве:
```bash
./gradlew :composeApp:connectedAndroidTest
```

### 3. На iOS Simulator (требуется macOS и Xcode)
Запускает тесты в симуляторе iOS:
```bash
./gradlew :composeApp:iosSimulatorArm64Test
```
