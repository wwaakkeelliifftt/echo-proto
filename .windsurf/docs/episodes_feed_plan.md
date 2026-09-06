# Episodes Feed Plan & Implementation

> **📋 Project Index**: See [.windsurf/workflows/PROJECT_WORK_LOG.md](../.windsurf/workflows/PROJECT_WORK_LOG.md) for complete overview of all project work.

## 📋 **Overview**Анализ HTML дизайна**

### **🎯 Ключевые элементы из design_layouts.html:**

#### **1. Структура ленты:**
- **Секции по датам**: Today, Yesterday, Earlier This Week
- **Количество эпизодов**: 3 episodes, 2 episodes, 1 episode
- **Группировка**: Эпизоды сгруппированы по времени

#### **2. Карточка эпизода:**
```html
<div class="flex items-center bg-surface-container-low hover:bg-surface-container transition-colors p-2 rounded-lg group">
  <!-- Drag handle dots -->
  <div class="drag-handle-dots p-1 cursor-grab opacity-40 group-hover:opacity-100">
    <div class="dot"></div><div class="dot"></div>
    <div class="dot"></div><div class="dot"></div>
    <div class="dot"></div>
  </div>
  
  <!-- Episode info -->
  <div class="flex-1 min-w-0 pr-4">
    <h3 class="font-headline font-bold text-sm leading-tight text-on-surface line-clamp-2">
      The Architecture of Silence: Designing Acoustic Sanctuaries...
    </h3>
    <p class="font-label text-[11px] text-on-surface-variant mt-0.5 opacity-80">
      Sonic Landscapes • 42:15
    </p>
  </div>
  
  <!-- Action buttons -->
  <div class="flex items-center gap-3 shrink-0">
    <!-- Queue/Play/Download buttons with different states -->
  </div>
</div>
```

#### **3. Состояния кнопок (NEW из второго дизайна):**
- **Favorite**: `favorite` (пустой) / `favorite` (заполненный) - ЗОЛОТОЙ когда в избранном
- **Queue**: `queue_music` (пустой) / `playlist_add_check` (заполненный) - ЗОЛОТОЙ когда в очереди
- **Playback**: `pause_circle` (заполненный) / `play_circle` (заполненный) - РАЗНЫЕ ЦВЕТА
- **Download**: `download_for_offline` / `downloading` (с прогрессом) / `download_done`

#### **4. Цветовая схема (NEW):**
- **Playing**: `accent-blue` (#4DB6AC) - СИНИЙ для play/pause
- **Favorite/Queue**: `nocturne-gold` (#E6AF2E) - ЗОЛОТОЙ при активном состоянии
- **New Episode**: `nocturne-sand` (#D3C5AE) - ПЕСОЧНЫЙ для новых эпизодов
- **Inactive**: `on-surface-variant` - СЕРЫЙ для неактивных состояний

## 🎯 **План реализации**

### **Phase 1: Создание layout для item_episode.xml**
```xml
<!-- Основная карточка эпизода -->
<com.google.android.material.card.MaterialCardView
    android:id="@+id/cardEpisode"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="8dp"
    android:layout_marginVertical="4dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="0dp"
    app:cardBackgroundColor="@color/colorSurfaceContainerLow">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp">

        <!-- Drag handle dots -->
        <LinearLayout
            android:id="@+id/dragHandle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintBottom_toBottomOf="parent">

            <View style="@style/DragDot" />
            <View style="@style/DragDot" />
            <View style="@style/DragDot" />
            <View style="@style/DragDot" />

        </LinearLayout>

        <!-- Episode info -->
        <LinearLayout
            android:id="@+id/episodeInfo"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="12dp"
            android:orientation="vertical"
            app:layout_constraintStart_toEndOf="@id/dragHandle"
            app:layout_constraintEnd_toStartOf="@id/actionButtons"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintBottom_toBottomOf="parent">

            <TextView
                android:id="@+id/tvEpisodeTitle"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:textAppearance="@style/TextAppearance.EpisodeFeed.Title"
                android:maxLines="2"
                android:ellipsize="end"
                tools:text="The Architecture of Silence: Designing Acoustic Sanctuaries..." />

            <TextView
                android:id="@+id/tvEpisodeMetadata"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="4dp"
                android:textAppearance="@style/TextAppearance.EpisodeFeed.Metadata"
                tools:text="Sonic Landscapes • 42:15" />

        </LinearLayout>

        <!-- Action buttons -->
        <LinearLayout
            android:id="@+id/actionButtons"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintBottom_toBottomOf="parent">

            <!-- Favorite button -->
            <ImageButton
                android:id="@+id/btnFavorite"
                android:layout_width="32dp"
                android:layout_height="32dp"
                android:layout_marginEnd="8dp"
                android:background="?attr/selectableItemBackground"
                android:src="@drawable/ic_favorite"
                app:tint="@color/colorOnSurfaceVariant" />

            <!-- Queue button -->
            <ImageButton
                android:id="@+id/btnQueue"
                android:layout_width="32dp"
                android:layout_height="32dp"
                android:layout_marginEnd="8dp"
                android:background="?attr/selectableItemBackground"
                android:src="@drawable/button_ic_queue_add"
                app:tint="@color/colorOnSurfaceVariant" />

            <!-- Play/Pause button -->
            <ImageButton
                android:id="@+id/btnPlayback"
                android:layout_width="40dp"
                android:layout_height="40dp"
                android:background="?attr/selectableItemBackground"
                android:src="@drawable/ic_play_circle"
                app:tint="@color/colorAccentBlue" />

        </LinearLayout>

    </androidx.constraintlayout.widget.ConstraintLayout>

</com.google.android.material.card.MaterialCardView>
```

### **Phase 2: Создание стилей и drawable ресурсов**
```xml
<!-- Drag dots -->
<style name="DragDot">
    <item name="android:layout_width">3dp</item>
    <item name="android:layout_height">3dp</item>
    <item name="android:layout_marginStart">2dp</item>
    <item name="android:background">@drawable/drag_dot</item>
</style>

<!-- Text appearances -->
<style name="TextAppearance.EpisodeFeed.Title" parent="TextAppearance.MaterialComponents.Body1">
    <item name="android:textColor">@color/colorOnSurface</item>
    <item name="android:textSize">16sp</item>
    <item name="android:fontFamily">@font/manrope</item>
    <item name="android:textStyle">bold</item>
</style>

<style name="TextAppearance.EpisodeFeed.Metadata" parent="TextAppearance.MaterialComponents.Caption">
    <item name="android:textColor">@color/colorOnSurfaceVariant</item>
    <item name="android:textSize">10sp</item>
    <item name="android:fontFamily">@font/inter</item>
    <item name="android:textAllCaps">true</item>
    <item name="android:letterSpacing">0.1</item>
</style>

<style name="TextAppearance.EpisodeFeed.DateHeader" parent="TextAppearance.MaterialComponents.Body1">
    <item name="android:textColor">@color/colorPrimary</item>
    <item name="android:textSize">18sp</item>
    <item name="android:fontFamily">@font/manrope</item>
    <item name="android:textStyle">bold</item>
</style>
```

### **Phase 2.1: Новые drawable ресурсы**
```xml
<!-- ic_favorite.xml -->
<vector android:height="24dp" android:width="24dp"
    android:viewportHeight="24" android:viewportWidth="24"
    xmlns:android="http://schemas.android.com/apk/res/android">
    <path android:fillColor="@android:color/white"
        android:pathData="M12,21.35l-1.45,-1.32C5.4,15.36 2,12.28 2,8.5 2,5.42 4.42,3 7.5,3c1.74,0 3.41,0.81 4.5,2.09C13.09,3.81 14.76,3 16.5,3 19.58,3 22,5.42 22,8.5c0,3.78 -3.4,6.86 -8.55,11.54L12,21.35z"/>
</vector>

<!-- ic_play_circle.xml -->
<vector android:height="24dp" android:width="24dp"
    android:viewportHeight="24" android:viewportWidth="24"
    xmlns:android="http://schemas.android.com/apk/res/android">
    <path android:fillColor="@android:color/white"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM10,16.5v-9l6,4.5 -6,4.5z"/>
</vector>

<!-- ic_pause_circle.xml -->
<vector android:height="24dp" android:width="24dp"
    android:viewportHeight="24" android:viewportWidth="24"
    xmlns:android="http://schemas.android.com/apk/res/android">
    <path android:fillColor="@android:color/white"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM11,16L9,16L9,8h2v8zM15,16h-2L13,8h2v8z"/>
</vector>

<!-- drag_dot.xml -->
<shape android:shape="oval">
    <solid android:color="@color/colorAccentBlue"/>
    <size android:width="3dp" android:height="3dp"/>
</shape>
```
```

### **Phase 3: Дата заголовок (sticky элемент)**
```xml
<!-- Дата заголовок для группировки -->
<LinearLayout
    android:id="@+id/dateHeaderLayout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:paddingHorizontal="16dp"
    android:paddingVertical="8dp">

    <TextView
        android:id="@+id/tvDateHeader"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textAppearance="@style/TextAppearance.EpisodeFeed.DateHeader"
        android:textAllCaps="true"
        tools:text="TODAY" />

    <View
        android:layout_width="0dp"
        android:layout_height="1dp"
        android:layout_weight="1"
        android:layout_marginStart="16dp"
        android:background="@color/colorOutlineVariant" />

    <TextView
        android:id="@+id/tvEpisodeCount"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="16dp"
        android:textAppearance="@style/TextAppearance.EpisodeFeed.Metadata"
        tools:text="24 EPISODES" />

</LinearLayout>
```

### **Phase 4: RecyclerView с группировкой**
```kotlin
// Adapter с группировкой по датам
class EpisodeFeedAdapter : ListAdapter<FeedItem, RecyclerView.ViewHolder>() {
    
    companion object {
        const val TYPE_DATE_HEADER = 0
        const val TYPE_EPISODE = 1
    }
    
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is FeedItem.DateHeader -> TYPE_DATE_HEADER
            is FeedItem.Episode -> TYPE_EPISODE
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE_HEADER -> DateHeaderViewHolder.create(parent)
            TYPE_EPISODE -> EpisodeViewHolder.create(parent)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }
}
```

## 🔧 **Технические требования (UPDATED)**

### **1. Drag & Drop:**
- **Drag handle**: 4 точки (как в первом дизайне), возможно уменьшить размер
- **Visual feedback**: opacity 60% → 100% на hover
- **Reordering**: Позволить менять порядок эпизодов
- **Color**: Синий акцентный цвет для точек

### **2. Состояния кнопок (NEW):**
- **Favorite**: В избранном/не в избранном (золотой при активном)
- **Queue**: В очереди/не в очереди (золотой при активном)
- **Playback**: Играет (синий) / пауза (синий) / воспроизвести (песочный)
- **Download**: Скачан/скачивается/не скачан (с прогрессом)

### **3. Визуальная группировка:**
- **Date headers**: "Today", "Yesterday", "Earlier This Week"
- **Sticky date**: Дата "залипает" наверху при скролле
- **Episode count**: Количество эпизодов в секции
- **Divider line**: Линия между датой и счетчиком

### **4. Форма карточки (UPDATED):**
- **Rounded corners**: 12dp (xl)
- **Compact height**: Минимальная высота 72dp
- **Hover states**: Изменение background на `surface-container-high`
- **Padding**: 16dp

### **5. Цветовая схема (NEW):**
- **Playing**: `colorAccentBlue` (#4DB6AC) - СИНИЙ
- **Favorite/Queue**: `colorPrimary` (#E6AF2E) - ЗОЛОТОЙ при активном
- **New Episode**: `colorNocturneSand` (#D3C5AE) - ПЕСОЧНЫЙ
- **Inactive**: `colorOnSurfaceVariant` - СЕРЫЙ
- **Background**: `colorSurface` -> `colorSurfaceContainerHigh` на hover

## 📋 **Что нужно создать (UPDATED):**

### **Новые файлы:**
1. **item_episode.xml** - Layout карточки эпизода
2. **item_date_header.xml** - Layout заголовка даты
3. **ic_favorite.xml** - Иконка избранного
4. **ic_play_circle.xml** - Иконка play в круге
5. **ic_pause_circle.xml** - Иконка pause в круге
6. **EpisodeFeedAdapter.kt** - Adapter с группировкой
7. **FeedItem.kt** - Sealed class для типов элементов
8. **EpisodeViewHolder.kt** - ViewHolder для эпизодов

### **Обновления:**
1. **themes.xml** - Добавить стили для EpisodeFeed
2. **colors.xml** - Добавить `colorAccentBlue`, `colorNocturneSand`
3. **button_ic_queue_add.xml** - Изменить цвет с зеленого на обычный
4. **Fragment/Activity** - Интегрировать новый RecyclerView
5. **fonts** - Добавить Inter font family

---
*План обновлен с учетом второго дизайна!*
