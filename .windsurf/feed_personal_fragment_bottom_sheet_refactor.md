# Модификация FeedPersonalFragment: Диалог → Bottom Sheet

**Дата:** 12 мая 2026 г.  
**Статус:** В планировании  
**Приоритет:** Высокий

## 📋 **Задача**

Перенести логику фильтрации из `FeedFilterListDialogFragment` в Bottom Sheet с 3 состояниями для экрана `FeedPersonalFragment`.

## 🏗️ **Текущая архитектура**

### **FeedPersonalFragment.kt**
- **Вызов фильтра:** Строка 146-147 через `R.id.mabFeedPersFilter`
- **Текущий диалог:** `FeedFilterListDialogFragment()`
- **Меню:** `menu_top_feed_personal.xml` (строка 135)

### **FeedFilterListDialogFragment.kt**
- **Тип:** `DialogFragment`
- **Размер:** 80% ширины экрана (строка 139-149)
- **Функционал:** Фильтры через ChipGroup с редактированием

## 🔍 **Анализ текущей логики диалога**

### **Основные компоненты:**
1. **ChipGroup** - отображение текущих фильтров
2. **Режим редактирования** - включение/выключение удаления фильтров
3. **Добавление нового фильтра** - input поле с кнопками
4. **Сохранение в SharedPrefs** - при onPause/onDestroy

### **Состояния UI:**
```kotlin
// Состояние 1: Просмотр фильтров (по умолчанию)
isEnableToEdit = false
- ChipGroup с фильтрами
- Кнопка "Edit" 
- Кнопка "Add New Filter"

// Состояние 2: Редактирование фильтров
isEnableToEdit = true  
- ChipGroup с close иконками
- Кнопка "Done"
- Скрыта кнопка "Add New Filter"

// Состояние 3: Добавление нового фильтра
toggleInputVisibility(show = true)
- Input поле для нового фильтра
- Кнопки "Confirm" / "Cancel"
- Скрыты другие элементы
```

## 🎯 **Новая архитектура Bottom Sheet**

### **Предлагаемая структура:**
```kotlin
class FeedFilterBottomSheet : BottomSheetDialogFragment() {
    
    // 3 состояния:
    enum class FilterState {
        VIEW,      // Просмотр фильтров
        EDIT,      // Редактирование фильтров  
        ADD        // Добавление нового фильтра
    }
    
    private var currentState = FilterState.VIEW
}
```

### **Преимущества Bottom Sheet:**
1. **Современный UI:** Соответствует Material Design 3
2. **Больше пространства:** Для сложных фильтров
3. **Жесты:** Swipe to dismiss
4. **Анимации:** Плавные переходы между состояниями
5. **Расширяемость:** Легко добавлять новые фильтры

## 📊 **План миграции**

### **Этап 1: Подготовка**
- [ ] Создать `FeedFilterBottomSheet.kt`
- [ ] Создать XML layout для bottom sheet
- [ ] Определить 3 состояния UI

### **Этап 2: Перенос логики**
- [ ] Перенести ChipGroup логику
- [ ] Перенести режимы редактирования
- [ ] Перенести добавление фильтров
- [ ] Сохранить SharedPrefs логику

### **Этап 3: Интеграция**
- [ ] Обновить `FeedPersonalFragment.kt`
- [ ] Заменить вызов диалога на bottom sheet
- [ ] Тестирование функциональности

## 🎨 **Требования к дизайну**

### **Ожидаемые макеты (HTML → XML):**
1. **Состояние VIEW:** Список фильтров + кнопки Edit/Add
2. **Состояние EDIT:** Список с close иконками + кнопка Done  
3. **Состояние ADD:** Input поле + кнопки Confirm/Cancel

### **Компоненты для перевода:**
- **ChipGroup** - уже существует в диалоге
- **Input поля** - нужно адаптировать для bottom sheet
- **Кнопки** - Material 3 styling
- **Анимации** - переходы между состояниями

## 🔧 **Технические детали**

### **Вызов из FeedPersonalFragment:**
```kotlin
// Текущий код (строка 146-147):
val dialog = FeedFilterListDialogFragment()
dialog.show(childFragmentManager, Constants.FEED_FILTER_DIALOG_TAG)

// Новый код:
val bottomSheet = FeedFilterBottomSheet()
bottomSheet.show(parentFragmentManager, FeedFilterBottomSheet.TAG)
```

### **ViewModel инъекция:**
```kotlin
private val viewModel by viewModels<FeedViewModel>(
    ownerProducer = { requireParentFragment() }
)
```

### **Сохранение состояния:**
- `onPause()` → `viewModel.saveRssFeedPersonalFiltersIntoSharedPref()`
- `onDestroy()` → `viewModel.refreshRssFeedPersonal()`

## ⚠️ **Риски и митигации**

### **Риск 1: Сохранение состояния**
- **Проблема:** Bottom sheet может уничтожаться при повороте
- **Митигация:** Использовать `setRetainInstance(true)` или сохранять состояние во ViewModel

### **Риск 2: Анимации переходов**
- **Проблема:** Плавные переходы между 3 состояниями
- **Митигация:** Использовать Material Transitions API

### **Риск 3: Обработка жестов**
- **Проблема:** Конфликт swipe-to-dismiss с внутренними жестами
- **Митигация:** Правильная настройка `BottomSheetBehavior`

## 📝 **Следующие шаги**

1. **Получить макеты дизайнера** (HTML файлы)
2. **Перевести макеты в XML** 
3. **Создать 3 состояния UI**
4. **Реализовать логику переходов**
5. **Протестировать функциональность**

## 🎨 **Анализ HTML макетов и логические связки**

### **State 1: VIEW - Просмотр фильтров**
```html
<!-- Основные элементы: -->
- Drag handle (w-12 h-1.5)
- Header: "Feed Filters" (font-headline font-bold text-2xl)
- Input field: "Add new keyword..." (disabled)
- Active chips: "Sound Design", "Acoustics", "Analogue", "Studio"
- Glassmorphism hint card с info иконкой
- Action buttons: EDIT / SAVE CHANGES / DISCARD
```

### **State 2: EDIT - Редактирование фильтров**
```html
<!-- Основные элементы: -->
- Drag handle (w-12 h-1.5)
- Header: "Manage Filters" + "EDITING MODE" бейдж
- Secondary search input: "Add custom filter..."
- Delete chips: красные с close иконками
- Action buttons: SAVE CHANGES / DISCARD (2 колонки)
```

### **State 3: ADD - Добавление нового фильтра**
```html
<!-- Основные элементы: -->
- Drag handle (w-12 h-1.5)
- Header: "Manage Keywords" + close кнопка
- Primary input: "Binaural" (с активным состоянием)
- ADD кнопка внутри input
- Active filters chips с close иконками
- Action buttons: Clear All / Save Changes (2 колонки)
```

## 📋 **План создания файлов**

### **Этап 1: XML Layouts (3 файла)**
```
res/layout/
├── bottom_sheet_feed_filter_view.xml      # State 1: VIEW
├── bottom_sheet_feed_filter_edit.xml     # State 2: EDIT  
└── bottom_sheet_feed_filter_add.xml      # State 3: ADD
```

### **Этап 2: Kotlin классы (2 файла)**
```
ui/dialogs/
├── FeedFilterBottomSheet.kt             # Основной класс с 3 состояниями
└── FilterState.kt                      # Enum для состояний
```

### **Этап 3: Интеграция (1 файл)**
```
ui/fragments/
└── FeedPersonalFragment.kt              # Обновить вызов
```

**Всего к созданию: 6 файлов**

## 🔄 **Логические связки и переходы**

### **VIEW → EDIT:**
- Нажатие кнопки "EDIT"
- Скрыть input field (disabled)
- Показать secondary search input
- Преобразовать chips в delete mode (красные)
- Изменить header и кнопки

### **VIEW → ADD:**
- Фокус на input field
- Активировать ADD кнопку
- Сохранить текущие chips

### **EDIT → VIEW:**
- Нажатие "FINISH EDITING" или "SAVE CHANGES"
- Скрыть secondary input
- Восстановить primary input (disabled)
- Преобразовать chips в normal mode

### **ADD → VIEW:**
- Нажатие ADD или закрытие
- Добавить новый chip
- Очистить input
- Вернуться в VIEW режим

## 🔍 **Анализ текущего кода для удаления/изменений**

### **Файлы к удалению:**

#### **1. FeedFilterListDialogFragment.kt** 
```kotlin
// ВЕСЬ файл (202 строки) - будет полностью удален
class FeedFilterListDialogFragment : DialogFragment() {
    // Вся логика переедет в FeedFilterBottomSheet
}
```

#### **2. dialog_chip_filter_list.xml**
```xml
<!-- ВЕСЬ файл (108 строк) - будет полностью удален -->
<androidx.constraintlayout.widget.ConstraintLayout>
    <!-- Вся верстка переедет в 3 отдельных XML -->
</androidx.constraintlayout.widget.ConstraintLayout>
```

### **Файлы к изменению:**

#### **1. FeedPersonalFragment.kt**
```kotlin
// Строка 146-147 - ИЗМЕНИТЬ:
val dialog = FeedFilterListDialogFragment()
dialog.show(childFragmentManager, Constants.FEED_FILTER_DIALOG_TAG)

// НА:
val bottomSheet = FeedFilterBottomSheet()
bottomSheet.show(parentFragmentManager, FeedFilterBottomSheet.TAG)
```

#### **2. FeedViewModel.kt**
```kotlin
// Строки 140-156 - методы фильтров ОСТАЮТСЯ:
fun addNewFilterToRssFeedPersonalFilters(newFilter: String) { ... }
fun removeFilterFromRssFeedPersonal(filter: String) { ... }
fun saveRssFeedPersonalFiltersIntoSharedPref(filters: Set<String>?) { ... }

// Строки 37-38 - LiveData фильтров ОСТАЮТСЯ:
private val _filterStringsSet = MutableLiveData(emptySet<String>())
val filterStringsSet: LiveData<Set<String>> get() = _filterStringsSet

// Строки 118-132 - логика фильтрации ОСТАЕТСЯ:
fun refreshRssFeedPersonal(sourceList: List<Episode>? = _rssFeed.value) { ... }
```

### **Вызовы для перепривязки:**

#### **1. FeedPersonalFragment.kt - Menu Provider**
```kotlin
// Строка 145-148 - ИЗМЕНИТЬ вызов:
R.id.mabFeedPersFilter -> {
    val dialog = FeedFilterListDialogFragment()
    dialog.show(childFragmentManager, Constants.FEED_FILTER_DIALOG_TAG)
    true
}

// НА:
R.id.mabFeedPersFilter -> {
    val bottomSheet = FeedFilterBottomSheet()
    bottomSheet.show(parentFragmentManager, FeedFilterBottomSheet.TAG)
    true
}
```

### **Constants.kt - проверка**
```kotlin
// Constants.FEED_FILTER_DIALOG_TAG - можно переиспользовать
// или создать новую константу для bottom sheet
```

### **Styles и Themes - проверка**
```xml
<!-- Стили из dialog_chip_filter_list.xml нужно перенести в новые layouts -->
@style/FeedFilterDialogTitle
@style/FeedFilterDialogButtonText  
@style/FeedFilterDialogButtonFilled
```

## 🗑️ **Итог по удалению:**

### **Полностью удаляются (2 файла):**
1. ✅ `FeedFilterListDialogFragment.kt` (202 строки)
2. ✅ `dialog_chip_filter_list.xml` (108 строк)

### **Сохраняются и переиспользуются:**
1. ✅ `FeedViewModel.kt` - все методы фильтрации остаются
2. ✅ `FeedPersonalFragment.kt` - только вызов меняется  
3. ✅ `Constants.FEED_FILTER_DIALOG_TAG` - можно переиспользовать
4. ✅ Стили и темы - переносятся в новые layouts

---

**Статус:** Анализ завершен, план готов к реализации.
