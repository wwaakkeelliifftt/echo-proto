# Дизайн-система Echo Proto

> **📋 Project Index**: See [.windsurf/workflows/PROJECT_WORK_LOG.md](../.windsurf/workflows/PROJECT_WORK_LOG.md) for complete overview of all project work.

## Цветовая схема Nocturne Gold

### Основные цвета (Core Palette)
| Назначение | HEX-код | Имя в проекте | Описание |
| :--- | :--- | :--- | :--- |
| **Background (Deep)** | `#131313` | `colorBackground` | Основной фон приложения |
| **Surface (Elevated)** | `#1C1C1C` | `colorSurface` | Фон карточек, диалогов и нижних шторок |
| **Primary (Gold)** | `#E6AF2E` | `colorPrimary` | Основной акцент: кнопки, активные иконки, слайдеры |
| **Primary Variant** | `#FFCC61` | `colorPrimaryVariant` | Светло-золотой для эффектов наведения или градиентов |
| **On Surface (High)** | `#FFFFFF` | `colorTextPrimary` | Основной текст (заголовки) |
| **On Surface (Med)** | `#D3C5AE` | `colorTextSecondary` | Второстепенный текст, подписи, неактивные иконки |
| **Disabled/Dim** | `#353534` | `colorDisabled` | Фон для неактивных кнопок или разделителей |

### Акцентный голубой (Accent Blue)
| Назначение | HEX-код | Имя в проекте | Использование |
| :--- | :--- | :--- | :--- |
| **Action Blue** | `#4DB6AC` | `colorAccentBlue` | Ссылки, кнопка "YouTube", индикаторы загрузки (Cloud/Download) |

**Совет по использованию:** Используйте голубой точечно. Например, иконка облака (готовность к скачиванию) может быть серой, а процесс активной загрузки или кнопка перехода на внешний ресурс — голубой. Это разделит внутренние действия плеера (золото) и внешние/статусные действия (голубой).

### Дополнительные цвета
- **Error:** [будет определено]
- **Success:** [будет определено]
- **Warning:** [будет определено]

## Экраны

### DownloadsFragment
- **Комментарии дизайнера:** [здесь будут заметки от дизайнера]
- **Цветовая схема:** [ссылки на цвета выше]
- **Компоненты:**
  - RecyclerView: [описание стиля]
  - Empty state: [описание пустого состояния]
  - Toolbar: [описание тулбара]

### FeedFragment
- **Комментарии дизайнера:** [здесь будут заметки от дизайнера]
- **Цветовая схема:** [ссылки на цвета выше]
- **Компоненты:**
  - SwipeRefreshLayout: [описание стиля]
  - RecyclerView: [описание стиля]
  - FAB: [описание плавающей кнопки]

### SpeedControlBottomSheet
- **Комментарии дизайнера:** Premium дизайн с большой типографикой скорости, кастомным слайдером и кнопками +/-
- **Цветовая схема:** [ссылки на цвета выше]
- **Компоненты v2:**
  - Bottom sheet: фон `colorSurface`, rounded corners 2.5rem
  - Speed display: увеличенная цифра (56sp) `colorPrimary` + "x" (24sp) `colorPrimaryVariant`
  - Custom slider: кнопки +/- (48dp), кастомный track с прогрессом
  - Preset chips: `colorDisabled` фон, `colorTextSecondary` текст, активный `colorPrimary` с иконкой check
  - Add New button: dashed border, иконка bookmark_add
  - Bottom actions: только Apply Speed (справа), убран Dismiss
- **Типографика:** Заголовок "Tempo Control", letterSpacing 0.2, font weights medium/black
- **Особенности:** 
  - Кастомный слайдер с визуальным прогрессом
  - Активный чип с иконкой check_circle
  - Кнопка Add New с dashed border
  - Текст подсказки "Hold to clear"
  - Только Material Components (без Material3)

## Примеры кода

### Применение цветов в XML
```xml
<!-- Для фонов -->
android:background="@color/colorBackground"
android:background="@color/colorSurface"

<!-- Для текста -->
android:textColor="@color/colorTextPrimary"
android:textColor="@color/colorTextSecondary"

<!-- Для акцентов -->
android:tint="@color/colorPrimary"
android:backgroundTint="@color/colorAccentBlue"
```

### Применение в коде (Kotlin)
```kotlin
// Установка фона
binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBackground))

// Установка цвета текста
binding.textView.setTextColor(ContextCompat.getColor(context, R.color.colorTextPrimary))

// Для прослушанных эпизодов
binding.episodeCard.alpha = 0.5f
// или
binding.episodeTitle.setTextColor(ContextCompat.getColor(context, R.color.colorTextSecondary))
```

### Применение в Material 3 темах
```xml
<style name="Theme.EchoProto" parent="Theme.Material3.DayNight">
    <item name="colorPrimary">@color/colorPrimary</item>
    <item name="colorOnPrimary">@color/colorTextPrimary</item>
    <item name="colorSurface">@color/colorSurface</item>
    <item name="colorOnSurface">@color/colorTextSecondary</item>
    <item name="android:colorBackground">@color/colorBackground</item>
    <item name="colorOnBackground">@color/colorTextPrimary</item>
</style>
```

### Советы по применению Material 3
- Для прослушанных эпизодов используйте `android:alpha="0.5"` для всей карточки
- Или используйте `colorTextSecondary` с прозрачностью 30-50% в коде
- Голубой акцент (`colorAccentBlue`) используйте точечно для внешних действий
- Золотой акцент (`colorPrimary`) для основных элементов управления плеером

## Скриншоты/Мокапы

<!-- Здесь можно добавлять изображения или ссылки на мокапы -->

## Заметки по реализации

- [Здесь можно добавлять технические заметки]
- [Особенности реализации]
- [Известные проблемы или ограничения]

---

**Инструкция для дизайнера:**
1. Добавляйте комментарии в соответствующие секции
2. Используйте markdown для форматирования
3. Можно добавлять кодовые примеры
4. Для изображений используйте относительные пути или ссылки
