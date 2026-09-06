# Speed Control V2 - Documentation & Future Plans

## 📋 **Current Stage Summary**
- ✅ **New layout completed**: `bottom_sheet_speed_control_v2.xml`
- ✅ **New colors added**: Nocturne Gold palette in `colors_raw.xml`
- ✅ **New chip styles**: `SpeedControlChipV2` and `SpeedControlChipV2.Active` in `themes.xml`
- ✅ **Fragment updated**: `SpeedControlBottomSheetFragment.kt` uses v2 binding and styles
- ✅ **Rounded corners**: `bg_bottom_sheet_rounded.xml` drawable
- ✅ **Behavior fixed**: No sticky states, smooth swipe
- 📋 **Legacy files documented**: Old layout and styles identified for future reference

## 🗂️ **Files Analysis**

### **NEW FILES (keep)**
```
✅ bottom_sheet_speed_control_v2.xml          - New layout with Nocturne Gold design
✅ bg_bottom_sheet_rounded.xml                 - Rounded corners for bottom sheet
✅ colors_raw.xml (extended)                    - Nocturne Gold palette
✅ themes.xml (extended)                        - New chip styles
```

### **OLD FILES (FOR FUTURE CLEANUP)**
```
📋 bottom_sheet_speed_control.xml               - Old layout (no dependencies) - DO NOT DELETE YET
📋 SpeedControlChip style in themes.xml          - Legacy style (only used by old layout) - DO NOT DELETE YET  
📋 old_light_color* colors in colors_raw.xml     - Unused legacy colors - DO NOT DELETE YET
📋 old_dark_color* colors in colors_raw.xml      - Unused legacy colors - DO NOT DELETE YET
```

## 🔍 **Dependency Analysis Results**

### **1. Layout Dependencies**
📋 **FUTURE CLEANUP CANDIDATE** - `bottom_sheet_speed_control.xml`
- **Only reference**: Old layout itself uses `style="@style/SpeedControlChip"`
- **No code references**: No fragments/activities reference `R.layout.bottom_sheet_speed_control`
- **Only in cleanup doc**: References are only in this instruction file

### **2. Style Dependencies**
📋 **LEGACY STYLE FOUND** - `SpeedControlChip` (without V2)
- **Used by**: Old layout `bottom_sheet_speed_control.xml` line 49
- **Legacy fallback**: Defined in `themes.xml` line 40 as `parent="SpeedControlChipV2"`
- **Future cleanup**: After old layout is removed

### **3. Color Dependencies**
📋 **LEGACY COLORS FOUND** - All `old_*` colors
- **old_light_color* colors**: Only defined in `colors_raw.xml`, no usage found
- **old_dark_color* colors**: Only defined in `colors_raw.xml`, no usage found
- **No references**: No code uses these color names
- **Future cleanup**: When migrating to final color scheme

## 📝 **Future Migration Plan**

### **Phase 1: Color Migration**
```xml
<!-- Move from colors_raw.xml to colors.xml -->
<color name="colorBackground">#131313</color>
<color name="colorSurface">#1C1C1C</color>
<color name="colorPrimary">#E6AF2E</color>
<color name="colorPrimaryVariant">#FFCC61</color>
<color name="colorDisabled">#353534</color>
<color name="colorInactiveChipBackground">#3D3522</color>
<color name="colorInactiveChipText">#E6AF2E</color>
<color name="colorChipActiveText">#131313</color>
<color name="colorChipActiveIcon">#131313</color>
<color name="colorTextPrimary">#FFFFFF</color>
<color name="colorTextSecondary">#D3C5AE</color>
```

### **Phase 2: Layout Rename**
```bash
# Rename v2 layout to main
bottom_sheet_speed_control_v2.xml → bottom_sheet_speed_control.xml
```

### **Phase 3: Code Updates**
```kotlin
// Update binding reference
BottomSheetSpeedControlV2Binding → BottomSheetSpeedControlBinding

// Update layout reference in fragment
R.layout.bottom_sheet_speed_control_v2 → R.layout.bottom_sheet_speed_control
```

### **Phase 4: Future Cleanup (when ready)**
```
📋 ARCHIVE: bottom_sheet_speed_control.xml (old)
📋 ARCHIVE: SpeedControlChip style in themes.xml  
📋 ARCHIVE: colors_raw.xml (after migration)
📋 ARCHIVE: Old color definitions
```

## 🎯 **Variables to Watch**

### **In SpeedControlBottomSheetFragment.kt**
```kotlin
private var _binding: BottomSheetSpeedControlV2Binding? = null  // Will change to BottomSheetSpeedControlBinding
```

### **In Constants.kt**
```kotlin
PLAYBACK_SPEED_PRESET_LIMIT = 4  // ✅ Already updated
```

### **Style Names in themes.xml**
```xml
SpeedControlChipV2          // ✅ Keep
SpeedControlChipV2.Active   // ✅ Keep
SpeedControlChip           // ❓ Check if still used
```

## ⚠️ **Current Status & Notes**

1. **All functionality working**: Speed control opens/closes properly with new design
2. **No breaking changes**: Old files can remain until ready for migration
3. **Future-ready**: Migration plan documented for when cleanup is desired
4. **Backup ready**: Current state stable and documented

## 📅 **Recommended Future Order**
1. **Phase 1**: Color migration (colors_raw → colors) - when ready
2. **Phase 2**: Layout rename (v2 → main) - when ready  
3. **Phase 3**: Code updates (binding references) - when ready
4. **Phase 4**: Archive old files - when ready

---
*Last updated: Current state with working V2 implementation + documentation*