# Генератор мапперов
Процессор для создания мапперов из `Api` моделей в `Domain` модели.
Работает на основе `kapt`.
Генерация запускается при сборке проекта, точнее во время задачи `kaptKotlin`.
Создает экстеншен `toDomain` у api класса, экстеншен помещается в тот же пакет, где находится api модель.

## Использование и механизм работы
Для использования необходимо повесить на класс-источник аннотацию `Mapper` с указанием класса domain модели.
- Поля в классах должны быть идентичны на соответствующих местах. Корректность работы иначе не гарантируется.
- Поддерживает примитивы, java.time, List, кастомные классы (с учетом того, что у них так же имеется экстеншен `toDomain()`).
- Поддерживает nullable поля и добавляет дефолтные значения.
- Классы java.time не дефолтит
- Мапперы генерируются для нуллейбл классов
- List не допускает внутри себя null элементы в любом случае путем фильтрации (не дефолта)

#### Api модель может выглядеть следующим образом
```kotlin
package ru.dmitriyt.mappergenerator.data.model

@Mapper(User::class)
data class ApiUser(
    val id: String?,
    val name: String?,
    val age: Int?,
    val parent: ApiUser?,
    val street: ApiStreet?,
    val birthday: LocalDate?,
    val product: List<ApiProduct?>?,
    val points: List<Int?>?,
    val city: ApiCity?,
)
```

#### Тогда вызов сгенерированного маппера может быть таким
```kotlin
import ru.dmitriyt.mappergenerator.data.model.toDomain

val user = apiUser.toDomain()
```
