# Postman Collection для NutriFit Backend API

## Базовый URL
```
http://localhost:8080
```

## 1. Регистрация пользователя

**POST** `/api/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "test@example.com",
  "password": "password123",
  "fullName": "Иван Иванов"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "test@example.com"
}
```

**Важно:** Сохраните `token` из ответа для последующих запросов!

---

## 2. Вход в систему

**POST** `/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "test@example.com"
}
```

---

## 3. Получить профиль

**GET** `/api/profile`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Response:**
```json
{
  "id": 1,
  "fullName": "Иван Иванов",
  "age": null,
  "gender": null,
  "heightCm": null,
  "weightKg": null,
  "goal": null
}
```

---

## 4. Обновить профиль

**PUT** `/api/profile`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "fullName": "Иван Иванов",
  "age": 30,
  "gender": "MALE",
  "heightCm": 180.0,
  "weightKg": 75.0,
  "goal": "LOSE_WEIGHT"
}
```

---

## 5. Создать прием пищи

**POST** `/api/nutrition/meals`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "name": "Завтрак",
  "mealDate": "2024-01-15T08:00:00",
  "items": [
    {
      "name": "Овсянка",
      "calories": 300.0,
      "protein": 10.0,
      "carbs": 50.0,
      "fat": 5.0,
      "quantity": 100.0
    },
    {
      "name": "Бананы",
      "calories": 100.0,
      "protein": 1.0,
      "carbs": 25.0,
      "fat": 0.0,
      "quantity": 1.0
    }
  ]
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Завтрак",
  "mealDate": "2024-01-15T08:00:00",
  "totalCalories": 400.0,
  "items": [...]
}
```

---

## 6. Получить все приемы пищи

**GET** `/api/nutrition/meals`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

---

## 7. Получить прием пищи по ID

**GET** `/api/nutrition/meals/{id}`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Пример:** `/api/nutrition/meals/1`

---

## 8. Обновить прием пищи

**PUT** `/api/nutrition/meals/{id}`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Body (JSON):** (такой же как при создании)

---

## 9. Удалить прием пищи

**DELETE** `/api/nutrition/meals/{id}`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

---

## 10. Создать тренировку

**POST** `/api/workouts`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "type": "Бег",
  "durationMinutes": 30,
  "caloriesBurned": 300.0,
  "workoutDate": "2024-01-15T18:00:00"
}
```

---

## 11. Получить все тренировки

**GET** `/api/workouts`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

---

## 12. Обновить тренировку

**PUT** `/api/workouts/{id}`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Body (JSON):** (такой же как при создании)

---

## 13. Удалить тренировку

**DELETE** `/api/workouts/{id}`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

---

## 14. Создать метрику здоровья

**POST** `/api/metrics`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "steps": 10000,
  "heartRate": 72,
  "caloriesBurned": 250.0,
  "sleepHours": 7.5,
  "recordedAt": "2024-01-15T22:00:00"
}
```

---

## 15. Получить все метрики

**GET** `/api/metrics`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

---

## 16. Обновить метрику

**PUT** `/api/metrics/{id}`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Body (JSON):** (такой же как при создании)

---

## 17. Удалить метрику

**DELETE** `/api/metrics/{id}`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

---

## 18. Получить AI рекомендацию

**POST** `/api/ai/recommend`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Body (JSON) - опционально:**
```json
{
  "prompt": "Дай мне рекомендации по питанию на сегодня"
}
```

**Или без body (пустой запрос):**
```
(без body - будет использован автоматический промпт на основе данных пользователя)
```

**Response:**
```json
{
  "recommendation": "На основе ваших данных, рекомендую..."
}
```

---

## Настройка переменных в Postman

### Создание переменной для токена:

1. После успешного логина/регистрации, скопируйте токен из ответа
2. В Postman создайте переменную окружения:
   - Название: `token`
   - Значение: `ваш_токен_здесь`
3. В заголовках используйте: `Authorization: Bearer {{token}}`

### Автоматическое сохранение токена:

1. В тестах (Tests) запроса `/api/auth/login` добавьте:
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.token);
}
```

2. Затем в других запросах используйте: `Authorization: Bearer {{token}}`

---

## Примеры ошибок

### 401 Unauthorized
```json
{
  "error": "Invalid or expired token",
  "status": "401"
}
```

### 404 Not Found
```json
{
  "error": "Meal not found",
  "status": "404"
}
```

### 400 Bad Request
```json
{
  "email": "must not be blank",
  "password": "size must be between 6 and 2147483647"
}
```

