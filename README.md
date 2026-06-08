# 💑 CoupleRecipes — App de Recetas Saludables para Parejas

Una aplicación Android moderna para que dos personas gestionen sus recetas saludables juntos.

---

## 🏗️ Arquitectura

```
MVVM + Repository Pattern + Room Database
```

```
app/
└── src/main/
    ├── java/com/couplerecipes/
    │   ├── CoupleRecipesApp.kt          ← Application class (DI manual)
    │   ├── MainActivity.kt              ← Host + NavController + BottomNav
    │   ├── data/
    │   │   ├── local/
    │   │   │   ├── entities/
    │   │   │   │   └── Recipe.kt        ← Entidad Room + constantes de dominio
    │   │   │   ├── dao/
    │   │   │   │   └── RecipeDao.kt     ← Todas las queries SQL con LiveData
    │   │   │   └── RecipeDatabase.kt    ← Singleton Room Database
    │   │   └── repository/
    │   │       └── RecipeRepository.kt  ← Single Source of Truth
    │   └── ui/
    │       ├── RecipeViewModel.kt       ← ViewModel compartido (activityViewModels)
    │       ├── home/
    │       │   ├── HomeFragment.kt      ← Lista principal con chips y búsqueda
    │       │   ├── RecipeAdapter.kt     ← ListAdapter con DiffUtil
    │       │   └── FilterBottomSheet.kt ← Filtros por autor y salud
    │       ├── detail/
    │       │   └── RecipeDetailFragment.kt  ← Detalle completo + cocinar
    │       ├── add_edit/
    │       │   └── AddEditRecipeFragment.kt ← Formulario crear/editar
    │       ├── favorites/
    │       │   └── FavoritesFragment.kt ← Lista de favoritas
    │       └── history/
    │           └── HistoryFragment.kt   ← Historial + estadísticas de pareja
    └── res/
        ├── layout/                      ← Todos los layouts XML (Material 3)
        ├── navigation/nav_graph.xml     ← Navigation Component
        ├── menu/                        ← Menús toolbar y bottom nav
        ├── drawable/                    ← Iconos vectoriales MD3
        └── values/                      ← Temas, colores, strings
```

---

## 🚀 Cómo ejecutar en Android Studio

### Requisitos previos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17
- Android SDK 34
- Dispositivo o emulador con Android 7.0+ (API 24+)

### Pasos

1. **Clonar / descomprimir** el proyecto en tu máquina.

2. **Abrir en Android Studio:**
   ```
   File → Open → Selecciona la carpeta CoupleRecipes/
   ```

3. **Sincronizar Gradle:**
   Android Studio lo hará automáticamente. Si no:
   ```
   File → Sync Project with Gradle Files
   ```

4. **Ejecutar:**
   - Conecta un dispositivo o inicia un emulador (API 24+)
   - Presiona ▶ Run (Shift+F10)

---

## 📦 Dependencias principales

| Librería | Versión | Uso |
|---|---|---|
| Material Design 3 | 1.11.0 | UI components |
| Navigation Component | 2.7.7 | Navegación entre fragments |
| Room Database | 2.6.1 | Persistencia local |
| ViewModel + LiveData | 2.7.0 | MVVM |
| Glide | 4.16.0 | Carga de imágenes |
| Coroutines | 1.7.3 | Operaciones async |
| KSP | 1.9.22 | Procesador de anotaciones Room |

---

## 🎨 Características implementadas

### ✅ Funcionalidades principales
- [x] Crear recetas con nombre, ingredientes, pasos, tiempo, dificultad y categoría
- [x] Editar y eliminar recetas
- [x] Marcar como favoritas (toggle)
- [x] Buscar por nombre en tiempo real (SearchView)
- [x] Filtrar por categoría (chips horizontales)
- [x] Filtrar por autor (Mi pareja / Yo)
- [x] Filtros saludables (Vegetariana, Vegana, Sin gluten, Alta proteína, Bajas calorías)
- [x] Imágenes opcionales (galería del dispositivo)
- [x] Campo de calorías aproximadas
- [x] Campo de notas adicionales

### ✅ Funcionalidad de pareja
- [x] Campo "Autor" (Yo / Mi pareja)
- [x] Botón "Cocinar" con opción "Solo yo" o "¡Juntos! ❤️"
- [x] Historial de recetas cocinadas
- [x] Badge "Juntos" en recetas cocinadas en pareja
- [x] Estadísticas: total de recetas y cuántas hicieron juntos
- [x] Contador de veces cocinada por receta

### ✅ UI / Experiencia
- [x] Material Design 3 completo
- [x] Modo oscuro automático (DayNight)
- [x] Navigation Component con Bottom Navigation
- [x] RecyclerView con ListAdapter + DiffUtil (eficiente)
- [x] Cards con imagen hero y gradiente
- [x] Bottom Sheet para filtros avanzados
- [x] Snackbars de confirmación
- [x] AlertDialogs para acciones destructivas
- [x] Vista vacía cuando no hay recetas
- [x] Soporte offline 100% (Room local)

---

## 🔧 Puntos de extensión futura

- **Firebase Sync**: Agregar Firestore para sincronizar recetas entre dispositivos de la pareja
- **Notificaciones**: Recordatorio semanal "¿Qué cocinan esta semana?"
- **Planificador semanal**: Asignar recetas a días de la semana
- **Lista de compras**: Exportar ingredientes de recetas seleccionadas
- **Compartir recetas**: Intent share o deep links
- **Hilt/Dagger**: Reemplazar DI manual por Hilt para mayor escalabilidad
- **Compose**: Migrar a Jetpack Compose manteniendo el ViewModel

---

## 📝 Descripción de archivos clave

| Archivo | Responsabilidad |
|---|---|
| `Recipe.kt` | Entidad de BD + objetos de dominio (categorías, dificultad, filtros) |
| `RecipeDao.kt` | 15+ queries con LiveData reactivo para búsqueda y filtrado |
| `RecipeDatabase.kt` | Singleton Room, versión 1, configuración de migraciones |
| `RecipeRepository.kt` | Abstrae la fuente de datos; la UI nunca toca el DAO directamente |
| `RecipeViewModel.kt` | Estado de UI, filtros, y operaciones async con coroutines |
| `HomeFragment.kt` | Lista principal con búsqueda, categorías y FAB |
| `RecipeAdapter.kt` | ListAdapter con DiffUtil para updates eficientes |
| `AddEditRecipeFragment.kt` | Formulario reutilizable: detecta si crea o edita según estado del VM |
| `RecipeDetailFragment.kt` | Vista completa con acción "Cocinar" y diálogo de pareja |
| `HistoryFragment.kt` | Historial + estadísticas de pareja con cards de resumen |
| `nav_graph.xml` | Grafo de navegación con todas las acciones definidas |

---

## 🎨 Paleta de colores

| Token | Color | Uso |
|---|---|---|
| Primary | `#386A1F` (verde) | Botones, FAB, énfasis principal |
| Secondary | `#B5264C` (rosa) | Autores, acentos de pareja |
| Tertiary | `#386667` (verde azulado) | Filtros saludables |
| Surface | `#FDFDF6` | Fondos de cards |

En modo oscuro los colores se invierten automáticamente con el sistema DayNight.
