# Цветовая палитра Nocturne Gold (Android XML Guide)

Эта палитра разработана для премиального темного интерфейса с золотыми акцентами. Ниже приведены конкретные значения для вашего файла `res/values/colors.xml`.

## 1. Основные цвета (Core Palette)

| Назначение | HEX-код | Имя в проекте | Описание |
| :--- | :--- | :--- | :--- |
| **Background (Deep)** | `#131313` | `colorBackground` | Основной фон приложения. |
| **Surface (Elevated)** | `#1C1C1C` | `colorSurface` | Фон карточек, диалогов и нижних шторок. |
| **Primary (Gold)** | `#E6AF2E` | `colorPrimary` | Основной акцент: кнопки, активные иконки, слайдеры. |
| **Primary Variant** | `#FFCC61` | `colorPrimaryVariant` | Светло-золотой для эффектов наведения или градиентов. |
| **On Surface (High)** | `#FFFFFF` | `colorTextPrimary` | Основной текст (заголовки). |
| **On Surface (Med)** | `#D3C5AE` | `colorTextSecondary` | Второстепенный текст, подписи, неактивные иконки. |
| **Disabled/Dim** | `#353534` | `colorDisabled` | Фон для неактивных кнопок или разделителей. |

## 2. Акцентный голубой (Accent Blue)

Этот цвет мы берем из вашего оригинального дизайна, но адаптируем его под темную тему.

| Назначение | HEX-код | Имя в проекте | Использование |
| :--- | :--- | :--- | :--- |
| **Action Blue** | `#4DB6AC` | `colorAccentBlue` | Ссылки, кнопка "YouTube", индикаторы загрузки (Cloud/Download). |

**Совет по использованию:** 
Используйте голубой точечно. Например, иконка облака (готовность к скачиванию) может быть серой, а процесс активной загрузки или кнопка перехода на внешний ресурс — голубой. Это разделит внутренние действия плеера (золото) и внешние/статусные действия (голубой).

## 3. Пример реализации в XML

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Backgrounds -->
    <color name="vault_bg_dark">#131313</color>
    <color name="vault_surface_dark">#1C1C1C</color>
    
    <!-- Gold Accents -->
    <color name="vault_gold_primary">#E6AF2E</color>
    <color name="vault_gold_light">#FFCC61</color>
    <color name="vault_gold_dim">#353534</color>
    
    <!-- Text -->
    <color name="vault_text_high">#FFFFFF</color>
    <color name="vault_text_med">#D3C5AE</color>
    
    <!-- Blue Accent -->
    <color name="vault_blue_accent">#4DB6AC</color>
</resources>
```

## 4. Как применять в стилях Material 3
Для прослушанных эпизодов используйте `android:alpha="0.5"` для всей карточки или `vault_text_med` с прозрачностью 30-50% в коде.
