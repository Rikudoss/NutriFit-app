-- V2: Change MealItem calorie semantics
-- Before: calories = total kcal for the entry (quantity ignored)
-- After:  calories_per_100g = kcal per 100g; total = calories_per_100g * quantity / 100
ALTER TABLE meal_items RENAME COLUMN calories TO calories_per_100g;

-- quantity was nullable; make it required with a safe default for any stray rows
UPDATE meal_items SET quantity = 100 WHERE quantity IS NULL;
ALTER TABLE meal_items ALTER COLUMN quantity SET NOT NULL;
ALTER TABLE meal_items ALTER COLUMN quantity SET DEFAULT 100;
