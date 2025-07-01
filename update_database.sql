-- Шаг 6: Добавление поля для фиксации начисления баллов
ALTER TABLE "Мероприятия" ADD COLUMN IF NOT EXISTS "points_awarded" BOOLEAN DEFAULT FALSE; 