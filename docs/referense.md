# iOS UI-тесты: референс `lmru--mobile-customer--app` (команда LMWork)

Документ описывает, как в customer-app организованы UI-автотесты, с акцентом на работу команды **LMWork**.  
Цель — понять, что можно переиспользовать при запуске iOS-тестов в `lmru--services-platform--mobile-backoffice`.

---

## 1. Ключевой вывод для backoffice

| Аспект | Customer-app (LMWork) | Backoffice |
|--------|----------------------|------------|
| UI на iOS | **SwiftUI** (нативный) | **Compose Multiplatform** (`MainViewController`) |
| UI-тесты iOS | **Swift XCTest + XCUIElement** | **Открытый вопрос** (Compose UI Test KMP или XCTest) |
| UI-тесты Android | Kotlin + Kaspresso + Compose Test | Kotlin + Kaspresso + Compose Test (уже есть) |
| Общий код тестов UI | **Нет** — параллельные suite на Swift и Kotlin | Потенциально `commonTest` (Сценарий 1Б) |
| KMM `NetworkMockApi` | Есть в `common/core` | Есть в `androidTest` (аналог) |
| `iosTest` KMP module | **Отсутствует** | **Отсутствует** |

**LMWork не тестирует Compose на iOS** — у них iOS UI нативный. Переиспользовать напрямую можно **паттерны** (моки, launch args, CI, Page Object, scenarios, shared JSON), но не стек драйвера UI (XCUI ≠ Compose UI Test).

---

## 2. Общая архитектура customer-app

```text
┌─────────────────────────────────────────────────────────────────┐
│                         KMM (common/)                            │
│  Business logic, ViewModels, NetworkMockApi, Ktor MockEngine     │
└──────────────┬──────────────────────────────┬───────────────────┘
               │                              │
     ┌─────────▼─────────┐          ┌─────────▼─────────┐
     │  Android UI        │          │  iOS UI            │
     │  Jetpack Compose   │          │  SwiftUI/UIKit     │
     │  testTag           │          │  accessibilityId   │
     └─────────┬─────────┘          └─────────┬─────────┘
               │                              │
     ┌─────────▼─────────┐          ┌─────────▼─────────┐
     │  androidTest       │          │  LeroyMerlinUITests│
     │  Kaspresso+Kakao   │          │  XCTest+XCUI       │
     │  JUnit4            │          │  Swift             │
     └────────────────────┘          └────────────────────┘
```

- **Gradle** собирает KMM framework → CocoaPods → Xcode.
- **Gradle не запускает** iOS UI-тесты — только `xcodebuild test`.
- **Нет единого cross-platform UI test module** — Android и iOS пишутся отдельно, но с **одинаковыми Allure ID** и **синхронизированными строками локаторов**.

---

## 3. Команда LMWork — структура

### 3.1. iOS (Swift)

```text
ios/LeroyMerlinUITests/
├── Tests/LMWork/                          # 9 Swift-файлов, команда LMWork
│   ├── DivisionNavigation/
│   │   ├── DivisionNavigationBottomSheet/ # 6 test-классов
│   │   └── DivisionNavigationBlockOnPdp/  # 2 test-класса
│   └── Scenario/
│       └── SelectSpecificStoreScenario.swift
├── Common/                                # BaseTestCase, mocks, helpers
├── Screens/                               # ~63 Page Object
├── Data/Mocks/                            # iOS-only JSON
├── Helpers/AppLaunchArguments.swift
├── Utils/Resources.swift
└── Testplans/LMWork.xctestplan            # 23 теста для CI
```

### 3.2. Android (Kotlin) — зеркало

```text
android/app/src/androidTest/.../product_choice/divisionNavigation/
├── divisionNavigationBottomSheet/         # те же сценарии
├── helpers/LMWorkMockHelper.kt
└── scenario/SelectSpecificStoreScenario.kt
```

### 3.3. Соответствие тестов

| iOS (Swift) | Android (Kotlin) | AllureId (пример) |
|-------------|------------------|-------------------|
| `BottomSheetElementsTests.testElementsDisplaying` | `BottomSheetElementsTests` | `270936` |
| `testElementsDisplayingWhenResponseError` | аналог | `270850` |
| `testAddressStorageHelpButtonTap` | аналог | `270944` |

Команда на iOS: `team("LMWork")`. На Android: `@ProductChoice` (аннотация `@LMWork` есть, но на division nav не используется).

---

## 4. Запуск автотестов

### 4.1. iOS — только через Xcode

**Локально:**
- Scheme: `LeroyMerlinUITests`
- Test Plan: `LMWork.xctestplan` (или другой team plan)
- Команда: Product → Test (⌘U) с выбранным test plan

**CI** (`.ci/Jenkinsfile.ios_ui_tests.groovy`):
1. `bundle exec fastlane ios install_pods`  
   → внутри: `./gradlew :common:umbrella:generateDummyFramework`
2. `xcodebuild test -workspace ios/LeroyMerlin.xcworkspace -scheme LeroyMerlinUITests -testPlan LMWork ...`
3. `TestResults.xcresult` → Allure → Allure TestOps (project **274**)

**Gradle для iOS UI-тестов не используется** — только сборка KMM framework.

### 4.2. Android — Gradle

```bash
./gradlew :android:app:connectedDebugAndroidTest
```

- Orchestrator, Kaspresso, Allure TestOps (project **85**)
- Отдельный Jenkins pipeline от iOS

### 4.3. Test Plans (iOS)

Каждая команда имеет свой `.xctestplan`:
- `LMWork.xctestplan` — 23 selected tests
- Скрипты обновления: `tools/aqa/scripts/ios/update_test_plan.py`

---

## 5. Базовый каркас iOS-тестов

### 5.1. `BaseTestCase` (`Common/BaseTestCase.swift`)

- Наследник `XCTestCase`
- **Все Page Objects** — `lazy var` на уровне базового класса (~60+ экранов)
- `XCUIApplication` — singleton на класс теста
- `launchApp(withArgs:)` — launch arguments + SBT tunnel
- `setUp` / `tearDown` — Allure labels, `unsetMocks()`, сброс состояния
- Allure: `team()`, `epic()`, `feature()`, `displayName()`, `allureId()`, `step {}`

### 5.2. Типичный LMWork-тест

```swift
class BottomSheetElementsTests: BaseTestCase {
    private lazy var selectStoreScenario = SelectSpecificStoreScenario(self)

    override func setUp() {
        super.setUp()
        team("LMWork")
        epic("Адресное хранение товара")
        launchApp(withArgs: [
            .cleanAuth,
            .skipPurchaseModeSelection,
            .featureFlag(name: "feature_flag_pdp_division_navigation_stores", value: "[117]"),
        ])
    }

    override func setMocks() {
        super.setMocks()
        mockEvery(url: pdpMain, response: assetResponse("pdp/main/default"))
        // ...
    }

    func testElementsDisplaying() {
        allureId("270936")
        selectStoreScenario.selectStoreAndOpenPdpNavigationBottomSheet()
        step("Проверить отображение элементов") {
            pdpNavigationPageScreen.closeButton.assertExists()
        }
    }
}
```

### 5.3. Android-аналог (тот же сценарий)

```kotlin
class BottomSheetElementsTests : BaseTest() {
    override fun setFeatureFlags() {
        FeatureFlag.pdpDivisionNavigationStores = "[${store.id}]"
    }
    override fun setMocks() {
        mockCommonResponsesOnPdp()
        api.mockNext("/pdp/main", readAssetResponse("pdp/main/default"))
    }
    @Test
    @AllureId("270936")
    fun testElementsDisplaying() = run { /* Kaspresso steps */ }
}
```

---

## 6. Переиспользование Kotlin-кода

### 6.1. Что shared в KMM

| Компонент | Путь | Использование в тестах |
|-----------|------|------------------------|
| `NetworkMockApi` | `common/core/.../ktor/mock/NetworkMockApi.kt` | Android in-process; iOS через IPC; JVM unit tests |
| `MockEngine` | `common/core/.../ktor/mock/MockEngine.kt` | Ktor `MockEngine` |
| `HttpEngineFactory` | `common/core/.../ktor/engine/HttpEngineFactory.kt` | `isNetworkMocked` → mock или real |
| `Configuration.isNetworkMocked` | `common/core/.../configuration/` | Флаг переключения движка |
| Unit tests | `commonTest`, `jvmTest` | `NetworkMockApiTest` и др. |

### 6.2. Что НЕ shared для UI-тестов

- Page Objects — **отдельно**: Swift `Screens/` vs Kotlin `presentation/main/screens/`
- Scenarios — **отдельно**: `.swift` vs `.kt`, но **одинаковые шаги**
- Test classes — **отдельно**, связаны через **AllureId**
- Test runner — JUnit4 (Android) vs XCTest (iOS)

### 6.3. `commonTest` / `iosTest`

- `commonTest` — только **unit**-тесты (23 файла)
- **`iosTest` source set отсутствует** в проекте
- Compose UI Test на iOS **не используется**

---

## 7. Моки и движки

### 7.1. Два пути моков на iOS

```text
                    iOS UI Test Process                App Process
                    ─────────────────                  ───────────
Path A (SBT):       SBTUITestTunnelClient  ──HTTP──►  URL interception
                    assetResponse("pdp/...")           (не KMM MockEngine)

Path B (KMM):       AppTestingIpc (files)  ──IPC───►  NetworkMockApi.mockEvery()
                    + launch arg -isKmmMocking         Ktor MockEngine in KMM
                    resourceString("responses/...")
```

| Путь | Когда | LMWork |
|------|-------|--------|
| **SBT** (`assetResponse`) | Legacy, HTTP stub на уровне tunnel | **Используют** все LMWork-тесты |
| **KMM IPC** (`-isKmmMocking`) | Запись команд в файлы → app выполняет `NetworkMockApi` | Новые тесты (Services и др.), LMWork — **нет** |

`tools/quality/ui_tests_checks.py` помечает SBT-моки как `"Non-KMM mocks found"` — LMWork в этой категории.

### 7.2. Android — in-process

```kotlin
// BaseTest.kt
BuildConfig.IS_NETWORK_MOCKED.set(mocksEnabled)  // до launch app
api.mockEvery(url, readAssetResponse("pdp/main/default"))  // тот же процесс
```

Тест и приложение в **одном процессе** — моки вызываются напрямую.

### 7.3. iOS KMM IPC (рекомендуемый путь для backoffice)

Документ: `lmru--mobile-customer--app/docs/MOCK_ENGINE_AND_REAL_ENGINE.md`

1. Тест пишет команды (`mockEvery`, `mockNext`) через `AppTestingIpc` в файлы
2. В `launchEnvironment` передаётся `APP_TESTING_IPC_STORAGE_DIRECTORY`
3. App при старте (DEBUG) вызывает `AppTestingIpc.executeCommands()`
4. Команды применяются к `NetworkMockApi` в KMM → `MockEngine`

Файлы:
- `ios/LeroyMerlin/Classes/Utils/UITests/AppTestingIpc.swift`
- `ios/LeroyMerlinUITests/Common/BaseTestCase+Mock.swift`
- `AppLaunchArgument.isKmmMocking` (`-isKmmMocking`)

### 7.4. Shared mock JSON

Каноническое хранилище:
```text
android/app/src/androidTest/assets/responses/
  pdp/main/default.json
  stores/stores_117_only.json
  pdp/cells/default.json
  ...
```

- Android: `readAssetResponse("pdp/main/default")`
- iOS SBT: `assetResponse("pdp/main/default")`
- iOS KMM: `resourceString(name: "pdp/main/default")` — тот же JSON в test bundle

**WireMock не используется** — plain JSON + `mockEvery(url, response)`.

### 7.5. Сравнение с backoffice

| | Customer-app | Backoffice |
|---|-------------|------------|
| Формат fixtures | `responses/*.json` | WireMock JSON (`mappings/cmb/`) |
| Mock API | `NetworkMockApi` (KMM) | `NetworkMockApi` (androidTest) |
| Android bootstrap | `BuildConfig.IS_NETWORK_MOCKED` | `SboAllureAndroidJUnitRunner` + `TestKoinOverrideRegistry` |
| iOS bootstrap | `AppTestingIpc` + `-isKmmMocking` | **Не реализовано** |

Для backoffice POC логичнее **KMM IPC-путь** (как Path B), а не SBT — тот же Ktor `MockEngine`, те же WireMock JSON (конвертировать или адаптировать loader).

---

## 8. Локаторы: testTag vs accessibilityIdentifier

### 8.1. Android (Compose)

Production:
```kotlin
// PdpNavigationTestTags.kt (androidMain)
object PdpNavigationTestTags {
    const val CLOSE_BUTTON = "pdp_navigation_close"
}
// Modifier.testTag(PdpNavigationTestTags.CLOSE_BUTTON)
```

Tests:
```kotlin
// PdpNavigationScreen.kt (androidTest)
val closeButton = KNode { hasTestTag(PdpNavigationTestTags.CLOSE_BUTTON) }
```

### 8.2. iOS (SwiftUI)

Production — **дублирование строк** в Swift:
```swift
// PdpNavigationUiConstants.swift
static let closeButton = "pdp_navigation_close"
// .accessibilityIdentifier(PdpNavigationUiConstants.closeButton)
```

Tests:
```swift
// PdpNavigationPageScreen.swift
var closeButton: XCUIElement {
    application.buttons[SheetHeaderTopBarTestTags.closeButton]
}
```

### 8.3. Синхронизация

- Строки **вручную синхронизируются** между Kotlin `*TestTags.kt` и Swift `*UiConstants.swift`
- QA quality check: `tools/quality/ui_tests_checks.py`
- **Нет автоматической генерации** из commonMain

### 8.4. Backoffice

- UI в **`commonMain`** — `testTag` уже в production (`*TestTags.kt`, 21 файл)
- На iOS Compose MP `testTag` → accessibility tree автоматически
- При **Compose UI Test iOS** — те же теги, что на Android
- При **XCUITest** — `XCUIElement` по accessibility identifier (= testTag value)

---

## 9. Page Object и Scenario (iOS)

### 9.1. Иерархия

```text
XCTestCase
  └── BaseTestCase          # все PO как lazy var
        └── BottomSheetElementsTests

ElementsContainer           # base PO (XCUIApplication)
  └── PdpNavigationPageScreen

BaseScenario                # reusable flows
  └── SelectSpecificStoreScenario(testCase: BaseTestCase)
```

### 9.2. Assertions

Extensions на `XCUIElement`:
- `assertExists()`, `assertLabelEquals()`, `assertWaitForElement(timeout:)`
- `tap()`, `wait()`

Файлы: `Common/XCUIElement+Assert.swift`, `XCUIElement+Action.swift`

### 9.3. Launch Arguments

`Helpers/AppLaunchArguments.swift` — enum `AppLaunchArgument`:
- `.cleanAuth` — обход авторизации
- `.skipPurchaseModeSelection`
- `.featureFlag(name:value:)` — feature flags в тестах
- `.isKmmMocking` — KMM mock engine

Аналог backoffice: `use.mocks=true`, stub token в `AuthModeRule`.

---

## 10. CI и отчётность

| | Android | iOS |
|---|---------|-----|
| Jenkins | `Jenkinsfile_android_ui_tests` | `Jenkinsfile.ios_ui_tests.groovy` |
| Runner | Gradle + emulators | `xcodebuild` + simulators |
| Sharding | Gradle Managed Devices | Parallel simulators / test plans |
| Allure TestOps | Project 85 | Project 274 |
| Team filter | `@Team`, annotations | `team("LMWork")` + test plan |

Документация: `docs/QA/Jenkins UI Tests.md`, `docs/QA/UI Testing iOS.md`

---

## 11. Что применимо для backoffice

### 11.1. Можно взять как референс

| Паттерн | Как применить в backoffice |
|---------|---------------------------|
| **Параллельные suite** (Android Kotlin + iOS) с одними AllureId | Чеклисты/manual → потом autotest с теми же ID |
| **`NetworkMockApi` + Ktor MockEngine** в KMM | Уже есть в androidTest — вынести в shared + iOS IPC |
| **`AppTestingIpc` для iOS** | Реализовать аналог для передачи моков в app process |
| **Launch arguments** | `use.mocks`, stub auth token, feature flags |
| **Shared JSON fixtures** | 58 WireMock JSON → iOS test bundle |
| **Page Object + Scenario** | Структура папок, даже если драйвер другой |
| **Team test plan** (`LMWork.xctestplan`) | `Backoffice.xctestplan` или Gradle test filter |
| **CI: Gradle build + xcodebuild test** | Аналог `Jenkinsfile.ios_ui_tests` |
| **Документация моков** | `MOCK_ENGINE_AND_REAL_ENGINE.md` как шаблон |

### 11.2. Нельзя скопировать напрямую

| LMWork | Почему |
|--------|--------|
| Swift XCTest + XCUI | Backoffice UI — Compose MP, не SwiftUI |
| SBTUITestTunnel | HTTP-level stub; backoffice уже на Ktor MockEngine |
| Kakao Page Objects (Android customer) | Backoffice уже на Kakao — на iOS не работает |
| Дублирование TestTags Swift/Kotlin | Backoffice: теги уже в `commonMain` — **преимущество** |

### 11.3. Два возможных пути для backoffice POC

**Путь A — по аналогии с LMWork (XCTest на iOS):**
- Compose `testTag` → accessibility identifier
- Swift PO + XCTest
- Моки через `AppTestingIpc` + `NetworkMockApi`
- Тесты пишутся **дважды** (Kotlin Android + Swift iOS)
- Не требует Compose UI Test на iOS

**Путь B — Сценарий 1Б (Compose UI Test KMP):**
- Один тест на Kotlin в shared module
- Тот же `onNodeWithTag` на обеих платформах
- Требует POC: работает ли `org.jetbrains.compose.ui:ui-test` на iOS simulator
- LMWork **не использует** этот путь — референса в customer-app нет

---

## 12. Рекомендации для POC `NAV-002` (backoffice)

С учётом LMWork-референса и ответа dev («Kaspresso не прикладывается», «iOS не собирается»):

### Фаза 0 — Dev (блокер)
- [ ] iOS KMP сборка
- [ ] `iosMain` actuals
- [ ] `TestKoinOverrideRegistry` / `AppTestingIpc` в iOS app startup

### Фаза 1 — AQA (после сборки)
- [ ] Изучить `AppTestingIpc` + `BaseTestCase+Mock.swift` в customer-app
- [ ] Адаптировать IPC-моки под WireMock JSON backoffice
- [ ] Launch args: `use.mocks`, stub auth (аналог `AuthModeRule`)
- [ ] Spike: **Путь A** (XCTest) vs **Путь B** (Compose UI Test iOS)

### Фаза 2 — POC тест
- [ ] Один тест: NAV-002 (переключение на «Закрытые»)
- [ ] Моки: `tasks-search-actual-p0.json`, `tasks-search-closed-p0.json`, config, auth
- [ ] PO: MyOrdersScreen (tabs, list item 10024)
- [ ] Android `androidTest` **не трогаем**

---

## 13. Полезные файлы customer-app

| Назначение | Путь |
|------------|------|
| LMWork iOS tests | `ios/LeroyMerlinUITests/Tests/LMWork/` |
| LMWork test plan | `ios/LeroyMerlinUITests/Testplans/LMWork.xctestplan` |
| Base test case | `ios/LeroyMerlinUITests/Common/BaseTestCase.swift` |
| Mocks (SBT + KMM) | `ios/LeroyMerlinUITests/Common/BaseTestCase+Mock.swift` |
| KMM IPC | `ios/LeroyMerlin/Classes/Utils/UITests/AppTestingIpc.swift` |
| Mock engine docs | `docs/MOCK_ENGINE_AND_REAL_ENGINE.md` |
| iOS UI testing guide | `docs/QA/UI Testing iOS.md` |
| Jenkins iOS | `.ci/Jenkinsfile.ios_ui_tests.groovy` |
| NetworkMockApi | `common/core/src/commonMain/.../ktor/mock/NetworkMockApi.kt` |
| Android LMWork tests | `android/.../product_choice/divisionNavigation/` |
| Shared mock JSON | `android/app/src/androidTest/assets/responses/` |
| UI quality checks | `tools/quality/ui_tests_checks.py` |

---

## 14. Схема моков для backoffice (целевая, по аналогии с customer-app Path B)

```text
┌──────────────────┐     IPC files      ┌─────────────────────────┐
│  iOS Test         │ ────────────────► │  iOS App (Compose MP)    │
│  (Kotlin/Swift)   │  mockEvery cmds   │  TestKoinOverrideRegistry│
│                   │                   │  NetworkMockApi (KMM)    │
│  WireMock JSON    │                   │  Ktor MockEngine         │
└──────────────────┘                    └─────────────────────────┘

┌──────────────────┐   same process     ┌─────────────────────────┐
│  Android Test     │ ────────────────► │  Android App             │
│  (androidTest)    │  TestKoinRegistry │  NetworkMockApi          │
└──────────────────┘                    └─────────────────────────┘
```

---

*Документ подготовлен на основе анализа `lmru--mobile-customer--app` (июнь 2026).*